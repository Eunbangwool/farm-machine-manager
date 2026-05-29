package com.example.farmmachinemanager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.farmmachinemanager.data.UpdateChecker
import kotlinx.coroutines.launch
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.farmmachinemanager.data.Machine
import com.example.farmmachinemanager.data.MaintenanceRecord
import com.example.farmmachinemanager.notifications.ConsumableCheckWorker
import com.example.farmmachinemanager.notifications.NotificationHelper
import com.example.farmmachinemanager.ui.screens.AddMachineScreen
import com.example.farmmachinemanager.ui.screens.AddMaintenanceRecordScreen
import com.example.farmmachinemanager.ui.screens.AllConsumablesScreen
import com.example.farmmachinemanager.ui.screens.AllMaintenanceScreen
import com.example.farmmachinemanager.ui.screens.DailyInspectionScreen
import com.example.farmmachinemanager.ui.screens.EditMachineScreen
import com.example.farmmachinemanager.ui.screens.FuseGuideScreen
import com.example.farmmachinemanager.ui.screens.InspectionChecklistScreen
import com.example.farmmachinemanager.ui.screens.LubricationScheduleScreen
import com.example.farmmachinemanager.ui.screens.MachineDetailScreen
import com.example.farmmachinemanager.ui.screens.MachineListScreen
import com.example.farmmachinemanager.ui.screens.ManualEncyclopediaScreen
import com.example.farmmachinemanager.ui.screens.ManualMachineDetailScreen
import com.example.farmmachinemanager.ui.screens.ManualSearchScreen
import com.example.farmmachinemanager.ui.screens.PartsListScreen
import com.example.farmmachinemanager.ui.screens.SettingsScreen
import com.example.farmmachinemanager.ui.screens.SpecificationsScreen
import com.example.farmmachinemanager.ui.screens.StatisticsScreen
import com.example.farmmachinemanager.ui.screens.TractorInspectionChecklistScreen
import com.example.farmmachinemanager.ui.screens.TractorLubricationScheduleScreen
import com.example.farmmachinemanager.ui.screens.TractorPartsListScreen
import com.example.farmmachinemanager.ui.screens.TractorSpecificationsScreen
import com.example.farmmachinemanager.ui.screens.TractorTroubleshootingScreen
import com.example.farmmachinemanager.ui.screens.TractorWarningLightsScreen
import com.example.farmmachinemanager.ui.screens.TroubleshootingScreen
import com.example.farmmachinemanager.ui.screens.UpdateOperatingHoursScreen
import com.example.farmmachinemanager.ui.theme.FarmMachineTheme
import java.util.concurrent.TimeUnit

/**
 * 앱 진입점.
 *
 * 갤럭시 호환성: enableEdgeToEdge() + windowInsetsPadding(systemBars)
 * 알림: ConsumableCheckWorker를 24시간 주기로 등록
 */
class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* 허용/거부 결과는 무시: 다음 실행 때 다시 묻지 않음 */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContainer.init(applicationContext)
        NotificationHelper.ensureChannel(applicationContext)
        scheduleConsumableCheck()
        requestNotificationPermissionIfNeeded()
        checkForAppUpdate()

        enableEdgeToEdge()
        setContent {
            FarmMachineTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars)
                ) {
                    AppRoot()
                }
            }
        }
    }

    /** 매일 한 번 ConsumableCheckWorker 실행 (배터리 절약 모드에서도 동작). */
    private fun scheduleConsumableCheck() {
        val request = PeriodicWorkRequestBuilder<ConsumableCheckWorker>(
            repeatInterval = 1, repeatIntervalTimeUnit = TimeUnit.DAYS
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            ConsumableCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // 이미 등록되어 있으면 유지
            request
        )
    }

    /**
     * 앱 시작 시 GitHub release 의 최신 빌드를 확인.
     * 현재 설치본보다 새 버전이면 알림 1회. 같은 버전 중복 알림은 막는다.
     * 네트워크 실패/구버전이면 조용히 무시.
     */
    private fun checkForAppUpdate() {
        lifecycleScope.launch {
            val info = UpdateChecker.check() ?: return@launch
            val prefs = getSharedPreferences("update_check", MODE_PRIVATE)
            if (info.buildNumber > prefs.getInt("last_notified_build", 0)) {
                NotificationHelper.showUpdateAlert(
                    applicationContext, info.versionName, info.apkUrl
                )
                prefs.edit().putInt("last_notified_build", info.buildNumber).apply()
            }
        }
    }

    /** Android 13+ 에서 알림 권한 필요. 거부해도 앱은 동작 (알림만 안 옴). */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

/** 메인 화면 상단 탭. 농작이의 '작업 / 농지 / 설정' 처럼 농돌이는 '기계 관리 / 설정'. */
private enum class MainTab(val label: String) {
    Machines("기계 관리"),
    Settings("설정"),
}

private sealed interface AppScreen {
    data class Main(val tab: MainTab) : AppScreen
    data class Detail(val machine: Machine) : AppScreen
    data class AddMaintenance(val machine: Machine) : AppScreen
    /** 기존 정비기록 수정 (existingRecord와 함께 화면 진입) */
    data class EditMaintenance(val machine: Machine, val record: MaintenanceRecord) : AppScreen
    data object UpdateHours : AppScreen
    data object AddMachine : AppScreen
    data class EditMachine(val machine: Machine) : AppScreen
    data class DailyInspection(val machine: Machine) : AppScreen
    data object Statistics : AppScreen
    data object Troubleshooting : AppScreen
    data object InspectionChecklist : AppScreen
    data object PartsList : AppScreen
    data object FuseGuide : AppScreen
    data object Lubrication : AppScreen
    data object Specifications : AppScreen
    data object TractorTroubleshooting : AppScreen
    data object TractorInspectionChecklist : AppScreen
    data object TractorPartsList : AppScreen
    data object TractorLubrication : AppScreen
    data object TractorSpecifications : AppScreen
    data object TractorWarningLights : AppScreen
    data object ManualEncyclopedia : AppScreen
    data object ManualSearch : AppScreen
    data class ManualMachine(val machineId: String) : AppScreen
    data class AllMaintenance(val machine: Machine) : AppScreen
    data class AllConsumables(val machine: Machine) : AppScreen
    data class BatchMaintenance(val machine: Machine, val intervalKey: String) : AppScreen
}

@Composable
private fun AppRoot() {
    var screen: AppScreen by remember { mutableStateOf<AppScreen>(AppScreen.Main(MainTab.Machines)) }

    when (val current = screen) {
        is AppScreen.Main -> MainTabsScreen(
            currentTab = current.tab,
            onTabChange = { newTab -> screen = AppScreen.Main(newTab) },
            onMachineClick = { machine -> screen = AppScreen.Detail(machine) },
            onUpdateHoursClick = { screen = AppScreen.UpdateHours },
            onAddMachineClick = { screen = AppScreen.AddMachine },
            onStatisticsClick = { screen = AppScreen.Statistics },
            onTroubleshootingClick = { screen = AppScreen.Troubleshooting },
            onInspectionChecklistClick = { screen = AppScreen.InspectionChecklist },
            onPartsListClick = { screen = AppScreen.PartsList },
            onFuseGuideClick = { screen = AppScreen.FuseGuide },
            onLubricationClick = { screen = AppScreen.Lubrication },
            onSpecificationsClick = { screen = AppScreen.Specifications },
            onTractorTroubleshootingClick = { screen = AppScreen.TractorTroubleshooting },
            onTractorInspectionChecklistClick = { screen = AppScreen.TractorInspectionChecklist },
            onTractorPartsListClick = { screen = AppScreen.TractorPartsList },
            onTractorLubricationClick = { screen = AppScreen.TractorLubrication },
            onTractorSpecificationsClick = { screen = AppScreen.TractorSpecifications },
            onTractorWarningLightsClick = { screen = AppScreen.TractorWarningLights },
            onEncyclopediaClick = { screen = AppScreen.ManualEncyclopedia },
        )
        is AppScreen.Detail -> MachineDetailScreen(
            machine = current.machine,
            onBackClick = { screen = AppScreen.Main(MainTab.Machines) },
            onEditClick = { screen = AppScreen.EditMachine(current.machine) },
            onAddMaintenanceClick = { screen = AppScreen.AddMaintenance(current.machine) },
            onMarkRepairComplete = { screen = AppScreen.Main(MainTab.Machines) },
            onDailyInspectionClick = { screen = AppScreen.DailyInspection(current.machine) },
            onViewAllConsumables = { screen = AppScreen.AllConsumables(current.machine) },
            onViewAllMaintenance = { screen = AppScreen.AllMaintenance(current.machine) },
            onEditMaintenanceClick = { record ->
                screen = AppScreen.EditMaintenance(current.machine, record)
            },
            onBatchMaintenanceClick = { intervalKey ->
                screen = AppScreen.BatchMaintenance(current.machine, intervalKey)
            },
        )
        is AppScreen.AddMaintenance -> AddMaintenanceRecordScreen(
            machine = current.machine,
            onCancel = { screen = AppScreen.Detail(current.machine) },
            onSaveComplete = { screen = AppScreen.Detail(current.machine) }
        )
        is AppScreen.EditMaintenance -> AddMaintenanceRecordScreen(
            machine = current.machine,
            onCancel = { screen = AppScreen.Detail(current.machine) },
            onSaveComplete = { screen = AppScreen.Detail(current.machine) },
            existingRecord = current.record
        )
        is AppScreen.UpdateHours -> UpdateOperatingHoursScreen(
            onCancel = { screen = AppScreen.Main(MainTab.Machines) },
            onSaveComplete = { screen = AppScreen.Main(MainTab.Machines) }
        )
        is AppScreen.AddMachine -> AddMachineScreen(
            onCancel = { screen = AppScreen.Main(MainTab.Machines) },
            onSaveComplete = { screen = AppScreen.Main(MainTab.Machines) }
        )
        is AppScreen.EditMachine -> EditMachineScreen(
            machine = current.machine,
            onCancel = { screen = AppScreen.Detail(current.machine) },
            onSaveComplete = { screen = AppScreen.Main(MainTab.Machines) }
        )
        is AppScreen.DailyInspection -> DailyInspectionScreen(
            machine = current.machine,
            onCancel = { screen = AppScreen.Detail(current.machine) },
            onSaveComplete = { screen = AppScreen.Detail(current.machine) }
        )
        is AppScreen.Statistics -> StatisticsScreen(
            onBack = { screen = AppScreen.Main(MainTab.Machines) }
        )
        is AppScreen.Troubleshooting -> TroubleshootingScreen(
            onBack = { screen = AppScreen.Main(MainTab.Settings) }
        )
        is AppScreen.InspectionChecklist -> InspectionChecklistScreen(
            onBack = { screen = AppScreen.Main(MainTab.Settings) }
        )
        is AppScreen.PartsList -> PartsListScreen(
            onBack = { screen = AppScreen.Main(MainTab.Settings) }
        )
        is AppScreen.FuseGuide -> FuseGuideScreen(
            onBack = { screen = AppScreen.Main(MainTab.Settings) }
        )
        is AppScreen.Lubrication -> LubricationScheduleScreen(
            onBack = { screen = AppScreen.Main(MainTab.Settings) }
        )
        is AppScreen.Specifications -> SpecificationsScreen(
            onBack = { screen = AppScreen.Main(MainTab.Settings) }
        )
        is AppScreen.TractorTroubleshooting -> TractorTroubleshootingScreen(
            onBack = { screen = AppScreen.Main(MainTab.Settings) }
        )
        is AppScreen.TractorInspectionChecklist -> TractorInspectionChecklistScreen(
            onBack = { screen = AppScreen.Main(MainTab.Settings) }
        )
        is AppScreen.TractorPartsList -> TractorPartsListScreen(
            onBack = { screen = AppScreen.Main(MainTab.Settings) }
        )
        is AppScreen.TractorLubrication -> TractorLubricationScheduleScreen(
            onBack = { screen = AppScreen.Main(MainTab.Settings) }
        )
        is AppScreen.TractorSpecifications -> TractorSpecificationsScreen(
            onBack = { screen = AppScreen.Main(MainTab.Settings) }
        )
        is AppScreen.TractorWarningLights -> TractorWarningLightsScreen(
            onBack = { screen = AppScreen.Main(MainTab.Settings) }
        )
        is AppScreen.ManualEncyclopedia -> ManualEncyclopediaScreen(
            onBack = { screen = AppScreen.Main(MainTab.Settings) },
            onMachineClick = { id -> screen = AppScreen.ManualMachine(id) },
            onSearchClick = { screen = AppScreen.ManualSearch },
        )
        is AppScreen.ManualSearch -> ManualSearchScreen(
            onBack = { screen = AppScreen.ManualEncyclopedia },
            onMachineClick = { id -> screen = AppScreen.ManualMachine(id) },
        )
        is AppScreen.ManualMachine -> ManualMachineDetailScreen(
            machineId = current.machineId,
            onBack = { screen = AppScreen.ManualEncyclopedia },
        )
        is AppScreen.AllMaintenance -> AllMaintenanceScreen(
            machine = current.machine,
            onBack = { screen = AppScreen.Detail(current.machine) },
            onRecordClick = { record ->
                screen = AppScreen.EditMaintenance(current.machine, record)
            }
        )
        is AppScreen.AllConsumables -> AllConsumablesScreen(
            machine = current.machine,
            onBack = { screen = AppScreen.Detail(current.machine) }
        )
        is AppScreen.BatchMaintenance -> com.example.farmmachinemanager.ui.screens.BatchMaintenanceScreen(
            machine = current.machine,
            intervalKey = current.intervalKey,
            onCancel = { screen = AppScreen.Detail(current.machine) },
            onSaveComplete = { screen = AppScreen.Detail(current.machine) },
        )
    }
}

/**
 * 메인 화면 — 상단 TabRow ("기계 관리" / "설정") + 선택된 탭의 콘텐츠.
 * 농작이의 작업/농지/설정 탭 구조와 동일 패턴.
 *
 * 두 탭 모두 화면 안에 자체 TopBar / 헤더를 그리므로, TabRow 는 단순한 네비게이션
 * 라인 역할만 한다.
 */
@Composable
private fun MainTabsScreen(
    currentTab: MainTab,
    onTabChange: (MainTab) -> Unit,
    onMachineClick: (Machine) -> Unit,
    onUpdateHoursClick: () -> Unit,
    onAddMachineClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onTroubleshootingClick: () -> Unit,
    onInspectionChecklistClick: () -> Unit,
    onPartsListClick: () -> Unit,
    onFuseGuideClick: () -> Unit,
    onLubricationClick: () -> Unit,
    onSpecificationsClick: () -> Unit,
    onTractorTroubleshootingClick: () -> Unit,
    onTractorInspectionChecklistClick: () -> Unit,
    onTractorPartsListClick: () -> Unit,
    onTractorLubricationClick: () -> Unit,
    onTractorSpecificationsClick: () -> Unit,
    onTractorWarningLightsClick: () -> Unit,
    onEncyclopediaClick: () -> Unit,
) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxSize()
    ) {
        androidx.compose.material3.TabRow(
            selectedTabIndex = currentTab.ordinal,
            containerColor = com.example.farmmachinemanager.ui.theme.SurfacePrimary,
            contentColor = com.example.farmmachinemanager.ui.theme.TextPrimary,
        ) {
            MainTab.values().forEach { tab ->
                androidx.compose.material3.Tab(
                    selected = tab == currentTab,
                    onClick = { onTabChange(tab) },
                    text = {
                        androidx.compose.material3.Text(
                            text = tab.label,
                            fontSize = 14.sp,
                        )
                    },
                )
            }
        }
        when (currentTab) {
            MainTab.Machines -> MachineListScreen(
                onMachineClick = onMachineClick,
                onUpdateHoursClick = onUpdateHoursClick,
                onAddMachineClick = onAddMachineClick,
                onSettingsClick = { onTabChange(MainTab.Settings) },
                onStatisticsClick = onStatisticsClick,
            )
            MainTab.Settings -> SettingsScreen(
                onBack = { onTabChange(MainTab.Machines) },
                onTroubleshootingClick = onTroubleshootingClick,
                onInspectionChecklistClick = onInspectionChecklistClick,
                onPartsListClick = onPartsListClick,
                onFuseGuideClick = onFuseGuideClick,
                onLubricationClick = onLubricationClick,
                onSpecificationsClick = onSpecificationsClick,
                onTractorTroubleshootingClick = onTractorTroubleshootingClick,
                onTractorInspectionChecklistClick = onTractorInspectionChecklistClick,
                onTractorPartsListClick = onTractorPartsListClick,
                onTractorLubricationClick = onTractorLubricationClick,
                onTractorSpecificationsClick = onTractorSpecificationsClick,
                onTractorWarningLightsClick = onTractorWarningLightsClick,
                onEncyclopediaClick = onEncyclopediaClick,
            )
        }
    }
}

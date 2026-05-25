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
import androidx.core.content.ContextCompat
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
import com.example.farmmachinemanager.ui.screens.PartsListScreen
import com.example.farmmachinemanager.ui.screens.SettingsScreen
import com.example.farmmachinemanager.ui.screens.SpecificationsScreen
import com.example.farmmachinemanager.ui.screens.StatisticsScreen
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

private sealed interface AppScreen {
    data object List : AppScreen
    data class Detail(val machine: Machine) : AppScreen
    data class AddMaintenance(val machine: Machine) : AppScreen
    /** 기존 정비기록 수정 (existingRecord와 함께 화면 진입) */
    data class EditMaintenance(val machine: Machine, val record: MaintenanceRecord) : AppScreen
    data object UpdateHours : AppScreen
    data object AddMachine : AppScreen
    data class EditMachine(val machine: Machine) : AppScreen
    data class DailyInspection(val machine: Machine) : AppScreen
    data object Settings : AppScreen
    data object Statistics : AppScreen
    data object Troubleshooting : AppScreen
    data object InspectionChecklist : AppScreen
    data object PartsList : AppScreen
    data object FuseGuide : AppScreen
    data object Lubrication : AppScreen
    data object Specifications : AppScreen
    data class AllMaintenance(val machine: Machine) : AppScreen
    data class AllConsumables(val machine: Machine) : AppScreen
}

@Composable
private fun AppRoot() {
    var screen: AppScreen by remember { mutableStateOf(AppScreen.List) }

    when (val current = screen) {
        is AppScreen.List -> MachineListScreen(
            onMachineClick = { machine -> screen = AppScreen.Detail(machine) },
            onUpdateHoursClick = { screen = AppScreen.UpdateHours },
            onAddMachineClick = { screen = AppScreen.AddMachine },
            onSettingsClick = { screen = AppScreen.Settings },
            onStatisticsClick = { screen = AppScreen.Statistics }
        )
        is AppScreen.Detail -> MachineDetailScreen(
            machine = current.machine,
            onBackClick = { screen = AppScreen.List },
            onEditClick = { screen = AppScreen.EditMachine(current.machine) },
            onAddMaintenanceClick = { screen = AppScreen.AddMaintenance(current.machine) },
            onMarkRepairComplete = { screen = AppScreen.List },
            onDailyInspectionClick = { screen = AppScreen.DailyInspection(current.machine) },
            onViewAllConsumables = { screen = AppScreen.AllConsumables(current.machine) },
            onViewAllMaintenance = { screen = AppScreen.AllMaintenance(current.machine) },
            onEditMaintenanceClick = { record ->
                screen = AppScreen.EditMaintenance(current.machine, record)
            }
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
            onCancel = { screen = AppScreen.List },
            onSaveComplete = { screen = AppScreen.List }
        )
        is AppScreen.AddMachine -> AddMachineScreen(
            onCancel = { screen = AppScreen.List },
            onSaveComplete = { screen = AppScreen.List }
        )
        is AppScreen.EditMachine -> EditMachineScreen(
            machine = current.machine,
            onCancel = { screen = AppScreen.Detail(current.machine) },
            onSaveComplete = { screen = AppScreen.List }
        )
        is AppScreen.DailyInspection -> DailyInspectionScreen(
            machine = current.machine,
            onCancel = { screen = AppScreen.Detail(current.machine) },
            onSaveComplete = { screen = AppScreen.Detail(current.machine) }
        )
        is AppScreen.Settings -> SettingsScreen(
            onBack = { screen = AppScreen.List },
            onTroubleshootingClick = { screen = AppScreen.Troubleshooting },
            onInspectionChecklistClick = { screen = AppScreen.InspectionChecklist },
            onPartsListClick = { screen = AppScreen.PartsList },
            onFuseGuideClick = { screen = AppScreen.FuseGuide },
            onLubricationClick = { screen = AppScreen.Lubrication },
            onSpecificationsClick = { screen = AppScreen.Specifications }
        )
        is AppScreen.Statistics -> StatisticsScreen(
            onBack = { screen = AppScreen.List }
        )
        is AppScreen.Troubleshooting -> TroubleshootingScreen(
            onBack = { screen = AppScreen.Settings }
        )
        is AppScreen.InspectionChecklist -> InspectionChecklistScreen(
            onBack = { screen = AppScreen.Settings }
        )
        is AppScreen.PartsList -> PartsListScreen(
            onBack = { screen = AppScreen.Settings }
        )
        is AppScreen.FuseGuide -> FuseGuideScreen(
            onBack = { screen = AppScreen.Settings }
        )
        is AppScreen.Lubrication -> LubricationScheduleScreen(
            onBack = { screen = AppScreen.Settings }
        )
        is AppScreen.Specifications -> SpecificationsScreen(
            onBack = { screen = AppScreen.Settings }
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
    }
}

package com.example.farmmachinemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.example.farmmachinemanager.data.Machine
import com.example.farmmachinemanager.ui.screens.AddMachineScreen
import com.example.farmmachinemanager.ui.screens.AddMaintenanceRecordScreen
import com.example.farmmachinemanager.ui.screens.DailyInspectionScreen
import com.example.farmmachinemanager.ui.screens.EditMachineScreen
import com.example.farmmachinemanager.ui.screens.MachineDetailScreen
import com.example.farmmachinemanager.ui.screens.MachineListScreen
import com.example.farmmachinemanager.ui.screens.UpdateOperatingHoursScreen
import com.example.farmmachinemanager.ui.theme.FarmMachineTheme

/**
 * 앱 진입점.
 *
 * 갤럭시 호환성: enableEdgeToEdge() + windowInsetsPadding(systemBars)
 * → 상태바/네비게이션 바 영역과 콘텐츠가 겹치지 않도록 안전하게 패딩 적용.
 *   (One UI의 다양한 노치/펀치홀/제스처 영역에서도 안전)
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}

private sealed interface AppScreen {
    data object List : AppScreen
    data class Detail(val machine: Machine) : AppScreen
    data class AddMaintenance(val machine: Machine) : AppScreen
    data object UpdateHours : AppScreen
    data object AddMachine : AppScreen
    data class EditMachine(val machine: Machine) : AppScreen
    data class DailyInspection(val machine: Machine) : AppScreen
}

@Composable
private fun AppRoot() {
    var screen: AppScreen by remember { mutableStateOf(AppScreen.List) }

    when (val current = screen) {
        is AppScreen.List -> MachineListScreen(
            onMachineClick = { machine -> screen = AppScreen.Detail(machine) },
            onUpdateHoursClick = { screen = AppScreen.UpdateHours },
            onAddMachineClick = { screen = AppScreen.AddMachine }
        )
        is AppScreen.Detail -> MachineDetailScreen(
            machine = current.machine,
            onBackClick = { screen = AppScreen.List },
            onEditClick = { screen = AppScreen.EditMachine(current.machine) },
            onAddMaintenanceClick = { screen = AppScreen.AddMaintenance(current.machine) },
            onMarkRepairComplete = { screen = AppScreen.List },
            onDailyInspectionClick = { screen = AppScreen.DailyInspection(current.machine) }
        )
        is AppScreen.AddMaintenance -> AddMaintenanceRecordScreen(
            machine = current.machine,
            onCancel = { screen = AppScreen.Detail(current.machine) },
            onSaveComplete = { screen = AppScreen.Detail(current.machine) }
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
    }
}

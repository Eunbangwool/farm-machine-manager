package com.example.farmmachinemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.farmmachinemanager.data.Machine
import com.example.farmmachinemanager.ui.screens.AddMaintenanceRecordScreen
import com.example.farmmachinemanager.ui.screens.MachineDetailScreen
import com.example.farmmachinemanager.ui.screens.MachineListScreen
import com.example.farmmachinemanager.ui.theme.FarmMachineTheme

/**
 * 앱 진입점.
 *
 * 현재는 상태 기반의 단순한 네비게이션 (List → Detail → AddMaintenance).
 * 화면이 더 늘어나면 androidx.navigation:navigation-compose 로 전환.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FarmMachineTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot()
                }
            }
        }
    }
}

/** 어느 화면을 보여줄지 나타내는 상태 */
private sealed interface AppScreen {
    data object List : AppScreen
    data class Detail(val machine: Machine) : AppScreen
    data class AddMaintenance(val machine: Machine) : AppScreen
}

@Composable
private fun AppRoot() {
    var screen: AppScreen by remember { mutableStateOf(AppScreen.List) }

    when (val current = screen) {
        is AppScreen.List -> MachineListScreen(
            onMachineClick = { machine ->
                screen = AppScreen.Detail(machine)
            }
        )
        is AppScreen.Detail -> MachineDetailScreen(
            machine = current.machine,
            onBackClick = { screen = AppScreen.List },
            onEditClick = { /* TODO: 다음 단계에서 기계 정보 수정 화면 */ },
            onAddMaintenanceClick = {
                screen = AppScreen.AddMaintenance(current.machine)
            },
            onMarkRepairComplete = { /* TODO: 다음 단계에서 수리 완료 처리 */ }
        )
        is AppScreen.AddMaintenance -> AddMaintenanceRecordScreen(
            machine = current.machine,
            onCancel = { screen = AppScreen.Detail(current.machine) },
            onSaveComplete = { screen = AppScreen.Detail(current.machine) }
        )
    }
}

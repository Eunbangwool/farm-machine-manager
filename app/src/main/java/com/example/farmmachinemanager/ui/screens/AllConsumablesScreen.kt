package com.example.farmmachinemanager.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmmachinemanager.AppContainer
import com.example.farmmachinemanager.data.ConsumableStatus
import com.example.farmmachinemanager.data.Machine
import com.example.farmmachinemanager.data.MaintenanceRecord
import com.example.farmmachinemanager.data.MaintenanceType
import com.example.farmmachinemanager.ui.components.ConsumableRow
import com.example.farmmachinemanager.ui.theme.BorderColor
import com.example.farmmachinemanager.ui.theme.SurfacePrimary
import com.example.farmmachinemanager.ui.theme.SurfaceSecondary
import com.example.farmmachinemanager.ui.theme.TextPrimary
import com.example.farmmachinemanager.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * 한 기계의 모든 소모품 목록.
 * 빠른 교체 버튼 클릭 시 소모품 갱신 + 정비기록 생성.
 */
@Composable
fun AllConsumablesScreen(
    machine: Machine,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val consumables by AppContainer.consumableRepository
        .observeConsumablesFor(machine.id)
        .collectAsState(initial = emptyList())

    val today = LocalDate.now()
    val sortedConsumables = consumables.sortedBy { c ->
        when (c.status(machine.operatingHours, today)) {
            ConsumableStatus.OVERDUE -> 0
            ConsumableStatus.DUE_SOON -> 1
            ConsumableStatus.NORMAL -> 2
            ConsumableStatus.UNKNOWN -> 3
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TopBar(
            title = "소모품 (${consumables.size}개)",
            subtitle = machine.name,
            onBack = onBack
        )

        if (consumables.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "등록된 소모품이 없습니다",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedConsumables, key = { it.id }) { c ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfacePrimary)
                            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                    ) {
                        ConsumableRow(
                            consumable = c,
                            currentHours = machine.operatingHours,
                            today = today,
                            onQuickReplaceClick = {
                                coroutineScope.launch {
                                    try {
                                        val updated = c.copy(
                                            lastReplacedDate = today,
                                            lastReplacedHours = machine.operatingHours
                                        )
                                        AppContainer.consumableRepository.saveConsumable(updated)
                                        val record = MaintenanceRecord(
                                            id = "record_${System.currentTimeMillis()}",
                                            machineId = machine.id,
                                            date = today,
                                            type = MaintenanceType.CONSUMABLE_REPLACE,
                                            title = "${c.name} 교체",
                                            operatingHoursAtMaintenance = machine.operatingHours,
                                            replacedConsumableIds = listOf(c.id)
                                        )
                                        AppContainer.maintenanceRepository.addMaintenance(record)
                                    } catch (t: Throwable) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "교체 기록 저장 실패: ${com.example.farmmachinemanager.data.repository.describeFirestoreError(t)}",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(title: String, subtitle: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfacePrimary)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "뒤로",
                tint = TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        Column {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(text = subtitle, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

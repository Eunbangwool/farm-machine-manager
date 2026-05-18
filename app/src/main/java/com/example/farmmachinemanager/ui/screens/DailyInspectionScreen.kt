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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmmachinemanager.AppContainer
import com.example.farmmachinemanager.data.CheckpointAction
import com.example.farmmachinemanager.data.CheckpointItem
import com.example.farmmachinemanager.data.Machine
import com.example.farmmachinemanager.data.MaintenanceRecord
import com.example.farmmachinemanager.data.MaintenanceTemplates
import com.example.farmmachinemanager.data.MaintenanceType
import com.example.farmmachinemanager.ui.theme.BorderColor
import com.example.farmmachinemanager.ui.theme.StatusInspectionBg
import com.example.farmmachinemanager.ui.theme.StatusInspectionText
import com.example.farmmachinemanager.ui.theme.StatusNormalText
import com.example.farmmachinemanager.ui.theme.SurfacePrimary
import com.example.farmmachinemanager.ui.theme.SurfaceSecondary
import com.example.farmmachinemanager.ui.theme.TextPrimary
import com.example.farmmachinemanager.ui.theme.TextSecondary
import com.example.farmmachinemanager.ui.theme.TextTertiary
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * 일일 점검 화면.
 *
 * 기계 종류별 표준 체크리스트(트랙터/콤바인/이앙기)를 표시.
 * 사용자가 각 항목을 체크 후 저장하면 INSPECTION 타입의 정비기록 1건이 생성됨.
 *
 * 항목은 두 그룹으로 나눠 보임:
 * - 매일 점검 (intervalDays=1): 빨간 강조
 * - 시간 주기 점검 (intervalHours): 시간 정보와 함께 표시
 */
@Composable
fun DailyInspectionScreen(
    machine: Machine,
    onCancel: () -> Unit,
    onSaveComplete: () -> Unit
) {
    BackHandler { onCancel() }

    val coroutineScope = rememberCoroutineScope()
    val today = remember { LocalDate.now() }
    val checkpoints = remember(machine.type) {
        MaintenanceTemplates.defaultCheckpoints(machine.type)
    }

    // 분류: 매일 점검 vs 시간 주기 점검
    val dailyItems = remember(checkpoints) { checkpoints.filter { it.intervalDays == 1 } }
    val hourlyItems = remember(checkpoints) { checkpoints.filter { it.intervalHours != null } }

    var checkedNames by remember { mutableStateOf(emptySet<String>()) }
    var isSaving by remember { mutableStateOf(false) }
    val anyChecked = checkedNames.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        InspectionTopBar(
            title = "${machine.name} 일일 점검",
            subtitle = if (checkpoints.isEmpty())
                "${machine.typeDisplay}에 대한 표준 점검표가 아직 없습니다"
            else
                "${dailyItems.size + hourlyItems.size}개 점검 항목",
            onBack = onCancel
        )

        if (checkpoints.isEmpty()) {
            EmptyState(machineType = machine.typeDisplay)
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (dailyItems.isNotEmpty()) {
                    item { SectionLabel("매일 점검", emphasized = true) }
                    items(dailyItems, key = { it.name }) { item ->
                        CheckpointCard(
                            item = item,
                            isChecked = item.name in checkedNames,
                            onToggle = {
                                checkedNames = if (item.name in checkedNames)
                                    checkedNames - item.name
                                else
                                    checkedNames + item.name
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                }
                if (hourlyItems.isNotEmpty()) {
                    item { SectionLabel("주기 점검 (시간 기준)", emphasized = false) }
                    items(hourlyItems, key = { it.name }) { item ->
                        CheckpointCard(
                            item = item,
                            isChecked = item.name in checkedNames,
                            onToggle = {
                                checkedNames = if (item.name in checkedNames)
                                    checkedNames - item.name
                                else
                                    checkedNames + item.name
                            }
                        )
                    }
                }
            }

            // 저장 버튼
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfacePrimary)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                val canSave = anyChecked && !isSaving
                val bg = if (canSave) TextPrimary else TextTertiary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(bg)
                        .clickable(enabled = canSave) {
                            isSaving = true
                            coroutineScope.launch {
                                val checkedNamesText = checkpoints
                                    .filter { it.name in checkedNames }
                                    .joinToString("\n") { "✓ ${it.name}" }
                                val record = MaintenanceRecord(
                                    id = "insp_${System.currentTimeMillis()}",
                                    machineId = machine.id,
                                    date = today,
                                    type = MaintenanceType.INSPECTION,
                                    title = "일일 점검 (${checkedNames.size}항목)",
                                    description = checkedNamesText,
                                    operatingHoursAtMaintenance = machine.operatingHours
                                )
                                AppContainer.maintenanceRepository.addMaintenance(record)
                                onSaveComplete()
                            }
                        }
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = when {
                            isSaving -> "저장 중..."
                            !anyChecked -> "항목을 1개 이상 체크하세요"
                            else -> "점검 완료 저장 (${checkedNames.size}개)"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = SurfacePrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(label: String, emphasized: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (emphasized) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(StatusInspectionBg)
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "오늘",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = StatusInspectionText
                )
            }
        }
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

@Composable
private fun CheckpointCard(
    item: CheckpointItem,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = if (isChecked) Icons.Outlined.CheckCircle
            else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isChecked) StatusNormalText else TextTertiary,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                fontSize = 13.sp,
                fontWeight = if (isChecked) FontWeight.Normal else FontWeight.Medium,
                color = if (isChecked) TextSecondary else TextPrimary
            )
            Text(
                text = buildSubtitle(item),
                fontSize = 11.sp,
                color = TextTertiary
            )
        }
    }
}

private fun buildSubtitle(item: CheckpointItem): String = buildString {
    append(item.action.displayName)
    append(" · ")
    when {
        item.intervalDays == 1 -> append("매일")
        item.intervalHours != null -> append("매 ${item.intervalHours.toInt()}h")
        else -> append("정기")
    }
}

@Composable
private fun EmptyState(machineType: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "표준 점검표 없음",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$machineType 종류는 표준 점검 항목이 아직 등록되어 있지 않습니다.\n트랙터·콤바인·이앙기는 매뉴얼 기반 점검표를 제공합니다.",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun InspectionTopBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
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

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmmachinemanager.AppContainer
import com.example.farmmachinemanager.data.Machine
import com.example.farmmachinemanager.data.MachineType
import com.example.farmmachinemanager.ui.theme.BorderColor
import com.example.farmmachinemanager.ui.theme.CombineIconBg
import com.example.farmmachinemanager.ui.theme.CombineIconTint
import com.example.farmmachinemanager.ui.theme.OtherIconBg
import com.example.farmmachinemanager.ui.theme.OtherIconTint
import com.example.farmmachinemanager.ui.theme.StatusRepairBg
import com.example.farmmachinemanager.ui.theme.StatusRepairText
import com.example.farmmachinemanager.ui.theme.SurfacePrimary
import com.example.farmmachinemanager.ui.theme.SurfaceSecondary
import com.example.farmmachinemanager.ui.theme.TextPrimary
import com.example.farmmachinemanager.ui.theme.TextSecondary
import com.example.farmmachinemanager.ui.theme.TextTertiary
import com.example.farmmachinemanager.ui.theme.TractorIconBg
import com.example.farmmachinemanager.ui.theme.TractorIconTint
import com.example.farmmachinemanager.ui.theme.TransplanterIconBg
import com.example.farmmachinemanager.ui.theme.TransplanterIconTint
import com.example.farmmachinemanager.ui.theme.VehicleIconBg
import com.example.farmmachinemanager.ui.theme.VehicleIconTint
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * 가동시간 업데이트 화면.
 *
 * 2단계 흐름:
 * - 1단계: 기계 선택 (모든 기계를 카드 목록으로 표시)
 * - 2단계: 새 가동시간 입력 (선택한 기계의 정보 + 입력 필드)
 *
 * 하드웨어 뒤로가기:
 * - 1단계에서는 onCancel (목록으로 복귀)
 * - 2단계에서는 1단계로 복귀 (기계 선택 해제)
 */
@Composable
fun UpdateOperatingHoursScreen(
    onCancel: () -> Unit,
    onSaveComplete: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val machines by AppContainer.machineRepository
        .observeMachines()
        .collectAsState(initial = emptyList())

    var selectedMachine by remember { mutableStateOf<Machine?>(null) }

    val current = selectedMachine
    if (current == null) {
        BackHandler { onCancel() }
        PickMachineView(
            machines = machines,
            onMachineSelect = { machine -> selectedMachine = machine },
            onBack = onCancel
        )
    } else {
        BackHandler { selectedMachine = null }
        HoursInputView(
            machine = current,
            onSave = { newHours ->
                coroutineScope.launch {
                    val updated = current.copy(operatingHours = newHours)
                    AppContainer.machineRepository.saveMachine(updated)
                    onSaveComplete()
                }
            },
            onBack = { selectedMachine = null }
        )
    }
}

// ============ 1단계: 기계 선택 ============

@Composable
private fun PickMachineView(
    machines: List<Machine>,
    onMachineSelect: (Machine) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TopBar(
            title = "가동시간 업데이트",
            subtitle = "운행한 기계를 선택해주세요",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(machines, key = { it.id }) { machine ->
                PickableMachineCard(
                    machine = machine,
                    onClick = { onMachineSelect(machine) }
                )
            }
        }
    }
}

@Composable
private fun PickableMachineCard(
    machine: Machine,
    onClick: () -> Unit
) {
    val (iconBg, iconTint) = iconColorFor(machine.type)
    val formatter = NumberFormat.getNumberInstance(Locale.KOREA)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconFor(machine.type),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = machine.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = buildString {
                    append(machine.manufacturer)
                    append(" · ")
                    append(machine.type.displayName)
                    machine.horsepower?.let { append(" · ${it}마력") }
                },
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "현재 ${formatter.format(machine.operatingHours.toInt())} h",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ============ 2단계: 가동시간 입력 ============

@Composable
private fun HoursInputView(
    machine: Machine,
    onSave: (Double) -> Unit,
    onBack: () -> Unit
) {
    var hoursText by remember {
        mutableStateOf(machine.operatingHours.toInt().toString())
    }
    var isSaving by remember { mutableStateOf(false) }

    val newHours = hoursText.toDoubleOrNull()
    val diff = newHours?.let { it - machine.operatingHours }
    val isValid = newHours != null && newHours >= machine.operatingHours && !isSaving

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TopBar(
            title = "${machine.name} 가동시간",
            subtitle = "${machine.manufacturer} · ${machine.type.displayName}",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero (큰 기계 아이콘)
            MachineHero(machine = machine)

            // 현재 가동시간 표시
            CurrentHoursDisplay(currentHours = machine.operatingHours)

            // 새 가동시간 입력
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "새 가동시간",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                OutlinedTextField(
                    value = hoursText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            hoursText = newValue
                        }
                    },
                    suffix = {
                        Text("h", fontSize = 14.sp, color = TextSecondary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // 피드백 메시지
                FeedbackText(
                    currentHours = machine.operatingHours,
                    newHours = newHours,
                    diff = diff
                )
            }
        }

        SaveBar(
            enabled = isValid,
            isSaving = isSaving,
            onSave = {
                if (isValid && newHours != null) {
                    isSaving = true
                    onSave(newHours)
                }
            }
        )
    }
}

@Composable
private fun MachineHero(machine: Machine) {
    val (iconBg, iconTint) = iconColorFor(machine.type)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(iconBg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconFor(machine.type),
            contentDescription = machine.type.displayName,
            tint = iconTint,
            modifier = Modifier.size(72.dp)
        )
    }
}

@Composable
private fun CurrentHoursDisplay(currentHours: Double) {
    val formatter = NumberFormat.getNumberInstance(Locale.KOREA)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "현재 가동시간",
            fontSize = 13.sp,
            color = TextSecondary
        )
        Text(
            text = "${formatter.format(currentHours.toInt())} h",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

@Composable
private fun FeedbackText(
    currentHours: Double,
    newHours: Double?,
    diff: Double?
) {
    val formatter = NumberFormat.getNumberInstance(Locale.KOREA)

    when {
        newHours == null || diff == null -> {
            // 입력값 없음 - 안내 텍스트
            Text(
                text = "가동시간은 누적값입니다. 현재(${formatter.format(currentHours.toInt())}h)보다 큰 값을 입력해주세요.",
                fontSize = 12.sp,
                color = TextTertiary
            )
        }
        diff < 0 -> {
            // 입력값이 현재보다 작음 - 에러
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(StatusRepairBg)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "현재보다 ${formatter.format((-diff).toInt())}h 적습니다. 가동시간은 줄어들 수 없어요.",
                    fontSize = 12.sp,
                    color = StatusRepairText
                )
            }
        }
        diff == 0.0 -> {
            // 입력값이 현재와 같음
            Text(
                text = "현재와 동일합니다.",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
        else -> {
            // 정상 - 증가분 표시
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfacePrimary)
                    .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "증가분",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = "+${formatter.format(diff.toInt())} h",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
    }
}

// ============ 공통 컴포넌트 ============

@Composable
private fun TopBar(
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
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun SaveBar(
    enabled: Boolean,
    isSaving: Boolean,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfacePrimary)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        val bgColor = if (enabled) TextPrimary else TextTertiary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .clickable(enabled = enabled, onClick = onSave)
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isSaving) "저장 중..." else "저장",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SurfacePrimary
            )
        }
    }
}

// ============ 헬퍼 ============

private fun iconColorFor(type: MachineType): Pair<Color, Color> = when (type) {
    MachineType.TRACTOR -> TractorIconBg to TractorIconTint
    MachineType.COMBINE -> CombineIconBg to CombineIconTint
    MachineType.RICE_TRANSPLANTER -> TransplanterIconBg to TransplanterIconTint
    MachineType.VEHICLE -> VehicleIconBg to VehicleIconTint
    MachineType.CULTIVATOR, MachineType.OTHER -> OtherIconBg to OtherIconTint
}

private fun iconFor(type: MachineType): ImageVector = when (type) {
    MachineType.TRACTOR,
    MachineType.COMBINE,
    MachineType.CULTIVATOR -> Icons.Default.Agriculture
    MachineType.RICE_TRANSPLANTER -> Icons.Default.Grass
    MachineType.VEHICLE -> Icons.Default.DirectionsCar
    MachineType.OTHER -> Icons.Default.Build
}

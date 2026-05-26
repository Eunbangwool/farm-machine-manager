package com.example.farmmachinemanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.farmmachinemanager.AppContainer
import com.example.farmmachinemanager.data.Machine
import com.example.farmmachinemanager.data.MachineStatus
import com.example.farmmachinemanager.data.MachineType
import com.example.farmmachinemanager.ui.theme.BorderColor
import com.example.farmmachinemanager.ui.theme.SurfacePrimary
import com.example.farmmachinemanager.ui.theme.SurfaceSecondary
import com.example.farmmachinemanager.ui.theme.TextPrimary
import com.example.farmmachinemanager.ui.theme.TextSecondary
import com.example.farmmachinemanager.ui.theme.TextTertiary
import kotlinx.coroutines.launch

/**
 * 새 기계 등록 화면.
 *
 * 입력 필드: 이름, 제조사, 종류 (필수), 마력, 가동시간 (선택)
 * 저장 시 Repository에 추가하고, 종류에 맞는 표준 정비 템플릿도 자동 적용.
 */
@Composable
fun AddMachineScreen(
    onCancel: () -> Unit,
    onSaveComplete: () -> Unit
) {
    BackHandler { onCancel() }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("") }
    var type by remember { mutableStateOf<MachineType?>(null) }
    var customTypeName by remember { mutableStateOf("") }
    var horsepowerText by remember { mutableStateOf("") }
    var hoursText by remember { mutableStateOf("0") }
    var manualId by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val isValid = name.isNotBlank() &&
            manufacturer.isNotBlank() &&
            type != null &&
            // OTHER 선택 시 customTypeName 필수
            (type != MachineType.OTHER || customTypeName.isNotBlank()) &&
            !isSaving

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TopBar(
            title = "새 기계 등록",
            subtitle = "기본 정보를 입력해주세요",
            onBack = onCancel
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 이름
            FormField(label = "이름", required = true) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("예: DK7320, KC1200", color = TextTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaultColors()
                )
            }

            // 제조사
            FormField(label = "제조사", required = true) {
                OutlinedTextField(
                    value = manufacturer,
                    onValueChange = { manufacturer = it },
                    placeholder = { Text("예: 대동, 구보타, TYM, 국제", color = TextTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaultColors()
                )
            }

            // 종류 (chip group)
            FormField(label = "종류", required = true) {
                MachineTypeChips(
                    selected = type,
                    onSelect = { type = it }
                )
            }

            // 기타 선택 시 → 사용자 정의 종류 이름 입력
            if (type == MachineType.OTHER) {
                FormField(
                    label = "기계 종류 이름",
                    required = true,
                    hint = "예: 베일러, 방제기, 굴착기, 파종기 등"
                ) {
                    OutlinedTextField(
                        value = customTypeName,
                        onValueChange = { customTypeName = it },
                        placeholder = { Text("종류 이름을 직접 입력", color = TextTertiary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaultColors()
                    )
                }
            }

            // 마력 (선택)
            FormField(label = "마력", required = false, hint = "트랙터·콤바인에서만 사용") {
                OutlinedTextField(
                    value = horsepowerText,
                    onValueChange = { v ->
                        if (v.isEmpty() || v.all { it.isDigit() }) horsepowerText = v
                    },
                    placeholder = { Text("예: 73", color = TextTertiary) },
                    suffix = { Text("마력", fontSize = 13.sp, color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaultColors()
                )
            }

            // 가동시간
            FormField(
                label = "현재 가동시간",
                required = false,
                hint = "중고 기계라면 현재 시간 입력. 새 기계는 0."
            ) {
                OutlinedTextField(
                    value = hoursText,
                    onValueChange = { v ->
                        if (v.isEmpty() || v.all { it.isDigit() }) hoursText = v
                    },
                    suffix = { Text("h", fontSize = 13.sp, color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaultColors()
                )
            }

            FormField(
                label = "매뉴얼 모델",
                required = false,
                hint = "선택하면 정기 정비·점검 항목을 매뉴얼에서 불러옵니다",
            ) {
                ManualPickerChips(selected = manualId, onSelect = { manualId = it })
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        SaveBar(
            enabled = isValid,
            isSaving = isSaving,
            onSave = {
                if (isValid && type != null) {
                    isSaving = true
                    coroutineScope.launch {
                        val newId = "machine_${System.currentTimeMillis()}"
                        val machine = Machine(
                            id = newId,
                            name = name.trim(),
                            manufacturer = manufacturer.trim(),
                            type = type!!,
                            customTypeName = if (type == MachineType.OTHER)
                                customTypeName.trim().ifBlank { null }
                            else null,
                            horsepower = horsepowerText.toIntOrNull(),
                            operatingHours = hoursText.toDoubleOrNull() ?: 0.0,
                            manualId = manualId,
                            status = MachineStatus.NORMAL,
                            statusNote = null
                        )
                        try {
                            AppContainer.machineRepository.saveMachine(machine)
                            AppContainer.consumableRepository.applyStandardTemplate(newId, type!!)
                            onSaveComplete()
                        } catch (t: Throwable) {
                            isSaving = false
                            Toast.makeText(
                                context,
                                "저장 실패: ${t.message ?: "알 수 없는 오류"}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        )
    }
}

// ============ 컴포넌트 ============

@Composable
private fun FormField(
    label: String,
    required: Boolean,
    hint: String? = null,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            if (required) {
                Text(
                    text = " *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFD32F2F)
                )
            }
        }
        content()
        if (hint != null) {
            Text(
                text = hint,
                fontSize = 11.sp,
                color = TextTertiary
            )
        }
    }
}

@Composable
private fun MachineTypeChips(
    selected: MachineType?,
    onSelect: (MachineType) -> Unit
) {
    val items = listOf(
        MachineType.TRACTOR to "트랙터",
        MachineType.COMBINE to "콤바인",
        MachineType.RICE_TRANSPLANTER to "이앙기",
        MachineType.CULTIVATOR to "관리기",
        MachineType.ROTAVATOR to "로터베이터",
        MachineType.PLOW to "쟁기",
        MachineType.SEEDER to "파종기",
        MachineType.HARVESTER to "수확기",
        MachineType.SPRAYER to "농약살포기",
        MachineType.DRONE to "드론",
        MachineType.BALER to "베일러",
        MachineType.LAWN_MOWER to "예초기",
        MachineType.LOADER to "로더",
        MachineType.FORKLIFT to "지게차",
        MachineType.VEHICLE to "차량",
        MachineType.OTHER to "기타",
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { (type, label) ->
            val isSelected = selected == type
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) TextPrimary else SurfacePrimary)
                    .border(
                        width = 0.5.dp,
                        color = if (isSelected) TextPrimary else BorderColor,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelect(type) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    color = if (isSelected) SurfacePrimary else TextPrimary
                )
            }
        }
    }
}

@Composable
private fun ManualPickerChips(selected: String?, onSelect: (String?) -> Unit) {
    val options: List<Pair<String?, String>> = listOf(null to "없음") +
        com.example.farmmachinemanager.data.manual.ManualCatalog.entries.map { it.id to it.label }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (id, label) ->
            val isSelected = selected == id
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) TextPrimary else SurfacePrimary)
                    .border(
                        width = 0.5.dp,
                        color = if (isSelected) TextPrimary else BorderColor,
                        shape = RoundedCornerShape(20.dp),
                    )
                    .clickable { onSelect(id) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    color = if (isSelected) SurfacePrimary else TextPrimary,
                )
            }
        }
    }
}

@Composable
private fun OutlinedTextFieldDefaultColors() = TextFieldDefaults.colors(
    focusedContainerColor = SurfacePrimary,
    unfocusedContainerColor = SurfacePrimary,
    focusedIndicatorColor = TextPrimary,
    unfocusedIndicatorColor = BorderColor,
    cursorColor = TextPrimary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)

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
                text = if (isSaving) "등록 중..." else "등록",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SurfacePrimary
            )
        }
    }
}

package com.example.farmmachinemanager.ui.screens

import androidx.activity.compose.BackHandler
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
import com.example.farmmachinemanager.data.MachineType
import com.example.farmmachinemanager.ui.theme.BorderColor
import com.example.farmmachinemanager.ui.theme.SurfacePrimary
import com.example.farmmachinemanager.ui.theme.SurfaceSecondary
import com.example.farmmachinemanager.ui.theme.TextPrimary
import com.example.farmmachinemanager.ui.theme.TextSecondary
import com.example.farmmachinemanager.ui.theme.TextTertiary
import kotlinx.coroutines.launch

/**
 * 기계 정보 수정 화면.
 * 기존 Machine을 받아 폼에 미리 채워두고, 수정 후 Repository에 저장.
 * 가동시간은 별도의 "가동시간 업데이트" 화면을 통해 수정 (여기선 다루지 않음).
 */
@Composable
fun EditMachineScreen(
    machine: Machine,
    onCancel: () -> Unit,
    onSaveComplete: () -> Unit
) {
    BackHandler { onCancel() }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var name by remember { mutableStateOf(machine.name) }
    var manufacturer by remember { mutableStateOf(machine.manufacturer) }
    var type by remember { mutableStateOf(machine.type) }
    var horsepowerText by remember {
        mutableStateOf(machine.horsepower?.toString() ?: "")
    }
    var yearText by remember {
        mutableStateOf(machine.year?.toString() ?: "")
    }
    var serialNumber by remember { mutableStateOf(machine.serialNumber ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    val isValid = name.isNotBlank() &&
            manufacturer.isNotBlank() &&
            !isSaving

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        EditTopBar(
            title = "${machine.name} 정보 수정",
            subtitle = "변경할 항목만 수정하세요",
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
            EditFormField(label = "이름", required = true) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = editFieldColors()
                )
            }

            EditFormField(label = "제조사", required = true) {
                OutlinedTextField(
                    value = manufacturer,
                    onValueChange = { manufacturer = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = editFieldColors()
                )
            }

            EditFormField(label = "종류", required = true) {
                EditMachineTypeChips(selected = type, onSelect = { type = it })
            }

            EditFormField(label = "마력", required = false) {
                OutlinedTextField(
                    value = horsepowerText,
                    onValueChange = { v ->
                        if (v.isEmpty() || v.all { it.isDigit() }) horsepowerText = v
                    },
                    suffix = { Text("마력", fontSize = 13.sp, color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = editFieldColors()
                )
            }

            EditFormField(label = "연식", required = false) {
                OutlinedTextField(
                    value = yearText,
                    onValueChange = { v ->
                        if (v.isEmpty() || (v.all { it.isDigit() } && v.length <= 4)) yearText = v
                    },
                    placeholder = { Text("예: 2020", color = TextTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = editFieldColors()
                )
            }

            EditFormField(label = "차대번호", required = false) {
                OutlinedTextField(
                    value = serialNumber,
                    onValueChange = { serialNumber = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = editFieldColors()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        EditSaveBar(
            enabled = isValid,
            isSaving = isSaving,
            onSave = {
                if (isValid) {
                    isSaving = true
                    coroutineScope.launch {
                        val updated = machine.copy(
                            name = name.trim(),
                            manufacturer = manufacturer.trim(),
                            type = type,
                            horsepower = horsepowerText.toIntOrNull(),
                            year = yearText.toIntOrNull(),
                            serialNumber = serialNumber.trim().ifBlank { null }
                        )
                        try {
                            AppContainer.machineRepository.saveMachine(updated)
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

@Composable
private fun EditFormField(
    label: String,
    required: Boolean,
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
                Text(text = " *", fontSize = 13.sp, color = Color(0xFFD32F2F))
            }
        }
        content()
    }
}

@Composable
private fun EditMachineTypeChips(
    selected: MachineType,
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
        items.forEach { (t, label) ->
            val isSelected = selected == t
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) TextPrimary else SurfacePrimary)
                    .border(
                        width = 0.5.dp,
                        color = if (isSelected) TextPrimary else BorderColor,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelect(t) }
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
private fun editFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = SurfacePrimary,
    unfocusedContainerColor = SurfacePrimary,
    focusedIndicatorColor = TextPrimary,
    unfocusedIndicatorColor = BorderColor,
    cursorColor = TextPrimary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)

@Composable
private fun EditTopBar(
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

@Composable
private fun EditSaveBar(
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
        val bg = if (enabled) TextPrimary else TextTertiary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(bg)
                .clickable(enabled = enabled, onClick = onSave)
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center
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

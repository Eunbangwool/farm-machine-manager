package com.example.farmmachinemanager.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmmachinemanager.AppContainer
import com.example.farmmachinemanager.data.Consumable
import com.example.farmmachinemanager.data.Machine
import com.example.farmmachinemanager.data.MaintenanceRecord
import com.example.farmmachinemanager.data.MaintenanceType
import com.example.farmmachinemanager.ui.theme.BorderColor
import com.example.farmmachinemanager.ui.theme.SurfacePrimary
import com.example.farmmachinemanager.ui.theme.SurfaceSecondary
import com.example.farmmachinemanager.ui.theme.TextPrimary
import com.example.farmmachinemanager.ui.theme.TextSecondary
import com.example.farmmachinemanager.ui.theme.TextTertiary
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 정비 기록 추가 화면.
 *
 * 입력 내용을 저장하면:
 * 1. MaintenanceRecord를 저장소에 추가
 * 2. 사용자가 체크한 소모품이 있다면, 그 소모품들의 마지막 교체일/시간을 갱신
 * 3. onSaveComplete 콜백 호출 → 상세 화면으로 복귀
 *
 * Repository 직접 호출 방식이라 ViewModel 없이 동작.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMaintenanceRecordScreen(
    machine: Machine,
    onCancel: () -> Unit,
    onSaveComplete: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val consumables by AppContainer.consumableRepository
        .observeConsumablesFor(machine.id)
        .collectAsState(initial = emptyList())

    // ---- 폼 상태 ----
    var type by remember { mutableStateOf(MaintenanceType.CONSUMABLE_REPLACE) }
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var operatingHoursText by remember {
        mutableStateOf(machine.operatingHours.toInt().toString())
    }
    var costText by remember { mutableStateOf("") }
    var shopName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedConsumableIds by remember { mutableStateOf(emptySet<String>()) }
    var isInProgress by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var datePickerOpen by remember { mutableStateOf(false) }
    var photoUris by remember { mutableStateOf(emptyList<String>()) }

    // 시스템 사진 선택기 (API 33+ 네이티브, 그 이하는 자동 폴백). 권한 불필요.
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            // 앱 재시작 후에도 URI에 접근하기 위해 영속 권한 획득 시도
            uris.forEach { uri ->
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) {
                    // 일부 URI는 영속 권한 불가 - 무시
                }
            }
            photoUris = photoUris + uris.map { it.toString() }
        }
    }

    val isValid = title.isNotBlank() && !isSaving

    // 하드웨어 뒤로가기 버튼 처리
    BackHandler { onCancel() }

    val onSaveClick: () -> Unit = onSaveClick@{
        if (!isValid) return@onSaveClick
        coroutineScope.launch {
            isSaving = true
            val hoursAtMaintenance = operatingHoursText.toDoubleOrNull()
            val record = MaintenanceRecord(
                id = "m_${System.currentTimeMillis()}",
                machineId = machine.id,
                date = date,
                type = type,
                title = title.trim(),
                description = description.trim().ifBlank { null },
                cost = costText.replace(",", "").toIntOrNull(),
                shopName = shopName.trim().ifBlank { null },
                operatingHoursAtMaintenance = hoursAtMaintenance,
                replacedConsumableIds = selectedConsumableIds.toList(),
                isInProgress = isInProgress,
                photoUrls = photoUris.toList()
            )

            AppContainer.maintenanceRepository.addMaintenance(record)

            // 체크된 소모품들의 마지막 교체일/시간 갱신
            val replacementHours = hoursAtMaintenance ?: machine.operatingHours
            consumables
                .filter { it.id in selectedConsumableIds }
                .forEach { c ->
                    AppContainer.consumableRepository.saveConsumable(
                        c.copy(
                            lastReplacedHours = replacementHours,
                            lastReplacedDate = date
                        )
                    )
                }

            isSaving = false
            onSaveComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TopBar(
            machineName = machine.name,
            onBackClick = onCancel
        )

        // 폼 (스크롤 가능)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FormSection(label = "정비 종류") {
                TypeSelector(
                    selected = type,
                    onChange = { type = it }
                )
            }

            FormSection(label = "제목") {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = {
                        Text("예: 엔진오일 교체", fontSize = 14.sp)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FormSection(label = "날짜", modifier = Modifier.weight(1f)) {
                    DateField(
                        date = date,
                        onClick = { datePickerOpen = true }
                    )
                }
                FormSection(label = "가동시간 (h)", modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = operatingHoursText,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                operatingHoursText = newValue
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                }
            }

            if (consumables.isNotEmpty()) {
                FormSection(label = "교체한 소모품 (체크 시 마지막 교체일 자동 갱신)") {
                    ConsumableCheckList(
                        consumables = consumables,
                        selected = selectedConsumableIds,
                        onToggle = { id ->
                            selectedConsumableIds = if (id in selectedConsumableIds) {
                                selectedConsumableIds - id
                            } else {
                                selectedConsumableIds + id
                            }
                        }
                    )
                }
            }

            FormSection(label = "비용 (선택)") {
                OutlinedTextField(
                    value = costText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            costText = newValue
                        }
                    },
                    placeholder = { Text("0", fontSize = 14.sp) },
                    suffix = {
                        Text("원", fontSize = 13.sp, color = TextSecondary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            FormSection(label = "정비 업체 / 담당자 (선택)") {
                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    placeholder = {
                        Text("예: 농기계상사", fontSize = 14.sp)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            FormSection(label = "메모 (선택)") {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = {
                        Text("특이사항을 자유롭게 적어주세요", fontSize = 14.sp)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4
                )
            }

            FormSection(label = "사진 첨부 (선택, 최대 5장)") {
                PhotoAttachmentSection(
                    photoCount = photoUris.size,
                    onPickPhotos = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(
                                mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    onClearAll = { photoUris = emptyList() }
                )
            }

            InProgressToggle(
                isInProgress = isInProgress,
                onChange = { isInProgress = it }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        SaveBar(
            enabled = isValid,
            isSaving = isSaving,
            onSave = onSaveClick
        )
    }

    // 날짜 선택 다이얼로그
    if (datePickerOpen) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { datePickerOpen = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    datePickerOpen = false
                }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { datePickerOpen = false }) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// ============ 보조 컴포넌트 ============

@Composable
private fun TopBar(
    machineName: String,
    onBackClick: () -> Unit
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
                .clickable(onClick = onBackClick),
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
                text = "정비 기록 추가",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = machineName,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun FormSection(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
        content()
    }
}

@Composable
private fun TypeSelector(
    selected: MaintenanceType,
    onChange: (MaintenanceType) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MaintenanceType.entries.forEach { type ->
            TypePill(
                text = type.displayName,
                selected = type == selected,
                onClick = { onChange(type) }
            )
        }
    }
}

@Composable
private fun TypePill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) TextPrimary else SurfacePrimary
    val fg = if (selected) SurfacePrimary else TextPrimary
    val border = if (selected) TextPrimary else BorderColor

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(0.5.dp, border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = fg
        )
    }
}

@Composable
private fun DateField(
    date: LocalDate,
    onClick: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = date.format(formatter),
            fontSize = 14.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Outlined.CalendarMonth,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ConsumableCheckList(
    consumables: List<Consumable>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
    ) {
        consumables.forEach { c ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle(c.id) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = c.id in selected,
                    onCheckedChange = { onToggle(c.id) }
                )
                Spacer(modifier = Modifier.size(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = c.name,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = c.category.displayName,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun InProgressToggle(
    isInProgress: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "진행 중인 작업으로 표시",
                fontSize = 14.sp,
                color = TextPrimary
            )
            Text(
                text = "완료되지 않은 수리/점검에 체크",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
        Switch(
            checked = isInProgress,
            onCheckedChange = onChange
        )
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

/**
 * 사진 첨부 섹션. "사진 추가" 버튼 + 선택된 장수 표시 + 전체 삭제.
 * 실제 썸네일 표시는 별도 이미지 라이브러리(Coil 등)가 필요해 일단 카운트 + 상태만 보여줌.
 */
@Composable
private fun PhotoAttachmentSection(
    photoCount: Int,
    onPickPhotos: () -> Unit,
    onClearAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceSecondary)
                .clickable(onClick = onPickPhotos),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.AddPhotoAlternate,
                contentDescription = "사진 추가",
                tint = TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (photoCount == 0) "사진 없음" else "사진 ${photoCount}장 추가됨",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = "왼쪽 아이콘을 탭하여 사진을 선택하세요",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
        if (photoCount > 0) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onClearAll)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "전체 삭제",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }
    }
}

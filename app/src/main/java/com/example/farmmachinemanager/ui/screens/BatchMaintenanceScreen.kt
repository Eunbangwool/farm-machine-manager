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
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmmachinemanager.AppContainer
import com.example.farmmachinemanager.data.Machine
import com.example.farmmachinemanager.data.MachineType
import com.example.farmmachinemanager.data.MaintenanceRecord
import com.example.farmmachinemanager.data.MaintenanceType
import com.example.farmmachinemanager.data.manual.ManualKey
import com.example.farmmachinemanager.ui.theme.ActionPrimary
import com.example.farmmachinemanager.ui.theme.ActionPrimaryText
import com.example.farmmachinemanager.ui.theme.BorderColor
import com.example.farmmachinemanager.ui.theme.SurfacePrimary
import com.example.farmmachinemanager.ui.theme.SurfaceSecondary
import com.example.farmmachinemanager.ui.theme.TextPrimary
import com.example.farmmachinemanager.ui.theme.TextSecondary
import com.example.farmmachinemanager.ui.theme.TextTertiary
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * 정기 정비 시기 도래 시 일괄 입력 화면.
 *
 * 사용 흐름:
 *  1. 기계 상세에서 "정기 정비 (200h)" 같은 칩 클릭 → 진입
 *  2. 매뉴얼의 해당 인터벌 항목들 (점검/교환/청소) 이 체크박스로 나열
 *  3. 사용자가 실제 수행한 항목들만 체크 → "선택 항목 저장" → 각 항목별로
 *     MaintenanceRecord 생성 후 일괄 저장
 *
 * 이앙기 / 트랙터 매뉴얼 schema 가 달라 두 케이스 분기.
 */
@Composable
fun BatchMaintenanceScreen(
    machine: Machine,
    intervalKey: String,
    onCancel: () -> Unit,
    onSaveComplete: () -> Unit,
) {
    BackHandler { onCancel() }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val manualRepo = remember { AppContainer.manualRepository }

    var items by remember { mutableStateOf<List<BatchItem>>(emptyList()) }
    var checked by remember { mutableStateOf<Set<String>>(emptySet()) }
    var loading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    val manualKey = manualRepo.manualKeyForMachine(
        machineName = machine.name,
        isTractor = machine.type == MachineType.TRACTOR,
        isRicePlanter = machine.type == MachineType.RICE_TRANSPLANTER,
        manualId = machine.manualId,
    )

    LaunchedEffect(machine.id, intervalKey) {
        loading = true
        items = loadItemsFor(manualKey, intervalKey)
        // 기본적으로 모두 체크 — 사용자가 실제 안 한 항목만 해제하기 더 빠름
        checked = items.map { it.id }.toSet()
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary),
    ) {
        TopBar(
            title = "정기 정비 일괄 입력",
            subtitle = "$intervalKey · ${machine.name}",
            onBack = onCancel,
        )

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("불러오는 중…", fontSize = 13.sp, color = TextSecondary)
            }
            return@Column
        }

        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("이 인터벌에 등록된 매뉴얼 항목이 없어요", fontSize = 14.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (manualKey == null) "이 기계 모델은 아직 매뉴얼 데이터가 없습니다"
                        else "$intervalKey 항목이 매뉴얼에 없음",
                        fontSize = 12.sp,
                        color = TextTertiary,
                    )
                }
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceSecondary),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SummaryCard(total = items.size, checked = checked.size)
            }
            items(items, key = { it.id }) { it ->
                CheckableRow(
                    item = it,
                    checked = it.id in checked,
                    onToggle = {
                        checked = if (it.id in checked) checked - it.id else checked + it.id
                    },
                )
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        val canSave = checked.isNotEmpty() && !isSaving
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (canSave) ActionPrimary else TextTertiary)
                .clickable(enabled = canSave) {
                    isSaving = true
                    val today = LocalDate.now()
                    val selectedItems = items.filter { it.id in checked }
                    coroutineScope.launch {
                        try {
                            selectedItems.forEachIndexed { idx, batchItem ->
                                val record = MaintenanceRecord(
                                    id = "batch_${System.currentTimeMillis()}_$idx",
                                    machineId = machine.id,
                                    date = today,
                                    type = batchItem.maintenanceType,
                                    title = "$intervalKey · ${batchItem.title}",
                                    description = batchItem.actionLabel,
                                    operatingHoursAtMaintenance = machine.operatingHours,
                                )
                                AppContainer.maintenanceRepository.addMaintenance(record)
                            }
                            android.widget.Toast.makeText(
                                context,
                                "${selectedItems.size}개 항목 저장 완료",
                                android.widget.Toast.LENGTH_SHORT,
                            ).show()
                            onSaveComplete()
                        } catch (t: Throwable) {
                            isSaving = false
                            android.widget.Toast.makeText(
                                context,
                                "저장 실패: ${t.message ?: "알 수 없는 오류"}",
                                android.widget.Toast.LENGTH_LONG,
                            ).show()
                        }
                    }
                }
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = if (isSaving) "저장 중…" else "선택한 ${checked.size}개 일괄 저장",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = ActionPrimaryText,
            )
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
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "뒤로",
                tint = TextPrimary,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        Column {
            Text(text = title, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(text = subtitle, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun SummaryCard(total: Int, checked: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "이번 인터벌에 매뉴얼이 권장하는 항목",
                fontSize = 12.sp,
                color = TextSecondary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$total 개 항목 · 체크 $checked 개",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
        }
    }
}

@Composable
private fun CheckableRow(item: BatchItem, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (checked) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
            contentDescription = null,
            tint = if (checked) ActionPrimary else TextTertiary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.actionLabel,
                fontSize = 12.sp,
                color = TextSecondary,
            )
        }
    }
}

/** 화면에 표시할 단일 정비 항목. */
private data class BatchItem(
    val id: String,
    val title: String,
    val actionLabel: String,
    val maintenanceType: MaintenanceType,
)

/**
 * 머신·인터벌 → 매뉴얼에서 해당 항목들 추출.
 * 이앙기와 트랙터 schema 가 달라 별도 처리.
 */
private suspend fun loadItemsFor(manualKey: ManualKey?, intervalKey: String): List<BatchItem> {
    val repo = AppContainer.manualRepository
    return when (manualKey) {
        ManualKey.PLANTER -> {
            val data = runCatching { repo.loadInspectionSchedule() }.getOrNull() ?: return emptyList()
            data.items.flatMap { item ->
                item.actions.filter { it.intervalKo.startsWith(intervalKey) }.map { action ->
                    BatchItem(
                        id = "${item.id}-${action.type}",
                        title = item.nameKo,
                        actionLabel = action.typeKo,
                        maintenanceType = mapActionToType(action.type),
                    )
                }
            }
        }
        ManualKey.TRACTOR_MR1050 -> {
            val data = runCatching { repo.loadTractorInspectionSchedule() }.getOrNull() ?: return emptyList()
            data.items.filter { it.intervalKo.startsWith(intervalKey) }.map { item ->
                BatchItem(
                    id = item.id,
                    title = item.nameKo,
                    actionLabel = item.actionKo,
                    maintenanceType = mapActionToType(item.actionJa ?: item.actionKo),
                )
            }
        }
        null -> emptyList()
    }
}

private fun mapActionToType(actionToken: String): MaintenanceType {
    val lower = actionToken.lowercase()
    return when {
        "replace" in lower || "교환" in actionToken || "交換" in actionToken -> MaintenanceType.CONSUMABLE_REPLACE
        "repair" in lower || "수리" in actionToken -> MaintenanceType.REPAIR
        "clean" in lower || "청소" in actionToken -> MaintenanceType.INSPECTION
        else -> MaintenanceType.INSPECTION
    }
}

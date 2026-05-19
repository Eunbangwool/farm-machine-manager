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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmmachinemanager.AppContainer
import com.example.farmmachinemanager.R
import com.example.farmmachinemanager.data.Consumable
import com.example.farmmachinemanager.data.ConsumableStatus
import com.example.farmmachinemanager.data.Machine
import com.example.farmmachinemanager.data.MachineStatus
import com.example.farmmachinemanager.data.MachineType
import com.example.farmmachinemanager.data.MaintenanceRecord
import com.example.farmmachinemanager.data.MaintenanceType
import com.example.farmmachinemanager.data.SampleData
import com.example.farmmachinemanager.ui.components.ConsumableRow
import com.example.farmmachinemanager.ui.components.MaintenanceRecordRow
import com.example.farmmachinemanager.ui.components.SectionHeader
import com.example.farmmachinemanager.ui.components.StatTile
import com.example.farmmachinemanager.ui.theme.BorderColor
import com.example.farmmachinemanager.ui.theme.CombineIconBg
import com.example.farmmachinemanager.ui.theme.CombineIconTint
import com.example.farmmachinemanager.ui.theme.ForkliftIconBg
import com.example.farmmachinemanager.ui.theme.ForkliftIconTint
import com.example.farmmachinemanager.ui.theme.FarmMachineTheme
import com.example.farmmachinemanager.ui.theme.OtherIconBg
import com.example.farmmachinemanager.ui.theme.OtherIconTint
import com.example.farmmachinemanager.ui.theme.RepairAlertBg
import com.example.farmmachinemanager.ui.theme.RepairAlertBorder
import com.example.farmmachinemanager.ui.theme.RepairAlertIconBg
import com.example.farmmachinemanager.ui.theme.RepairAlertIconTint
import com.example.farmmachinemanager.ui.theme.RepairAlertSubtitle
import com.example.farmmachinemanager.ui.theme.RepairAlertTitle
import com.example.farmmachinemanager.ui.theme.StatusInspectionBg
import com.example.farmmachinemanager.ui.theme.StatusInspectionText
import com.example.farmmachinemanager.ui.theme.StatusNormalBg
import com.example.farmmachinemanager.ui.theme.StatusNormalText
import com.example.farmmachinemanager.ui.theme.StatusRepairBg
import com.example.farmmachinemanager.ui.theme.StatusRepairText
import com.example.farmmachinemanager.ui.theme.SurfacePrimary
import com.example.farmmachinemanager.ui.theme.SurfaceSecondary
import com.example.farmmachinemanager.ui.theme.TextPrimary
import com.example.farmmachinemanager.ui.theme.TextSecondary
import com.example.farmmachinemanager.ui.theme.TractorIconBg
import com.example.farmmachinemanager.ui.theme.TractorIconTint
import com.example.farmmachinemanager.ui.theme.TransplanterIconBg
import com.example.farmmachinemanager.ui.theme.TransplanterIconTint
import com.example.farmmachinemanager.ui.theme.VehicleIconBg
import com.example.farmmachinemanager.ui.theme.VehicleIconTint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

/**
 * 기계 상세 화면.
 *
 * 구조 (위에서 아래로):
 * 1. 상단바 (뒤로, 편집, 더보기)
 * 2. 헤로 영역 (큰 아이콘, 모델명, 제조사·종류, 상태)
 * 3. 통계 3열 (가동시간, 마력, 연식)
 * 4. 수리 중 알림 배너 (상태가 UNDER_REPAIR일 때만)
 * 5. 곧 교체할 소모품 (상위 3개)
 * 6. 정비 이력 (최근 3건)
 * 7. 기본 정보
 * 8. 하단 고정 버튼 (정비 기록 추가)
 */
@Composable
fun MachineDetailScreen(
    machine: Machine,
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onAddMaintenanceClick: () -> Unit = {},
    onMarkRepairComplete: () -> Unit = {},
    onDailyInspectionClick: () -> Unit = {},
    onViewAllConsumables: () -> Unit = {},
    onViewAllMaintenance: () -> Unit = {},
    /** 미리보기에 표시된 정비기록 카드를 직접 클릭한 경우 수정 화면으로 진입 */
    onEditMaintenanceClick: (MaintenanceRecord) -> Unit = {}
) {
    // 하드웨어 뒤로가기 버튼 처리 (앱이 꺼지지 않고 목록으로 돌아감)
    BackHandler { onBackClick() }

    val coroutineScope = rememberCoroutineScope()

    // Repository에서 실시간으로 데이터 읽기.
    // 정비 기록 추가 화면에서 저장한 새 데이터가 자동으로 여기 반영됨.
    val maintenanceRecords by AppContainer.maintenanceRepository
        .observeMaintenanceFor(machine.id)
        .collectAsState(initial = emptyList())

    val consumables by AppContainer.consumableRepository
        .observeConsumablesFor(machine.id)
        .collectAsState(initial = emptyList())

    // 소모품을 시급도(남은 시간/일수)별로 정렬
    val today = LocalDate.now()
    val sortedConsumables = remember(consumables, machine.operatingHours) {
        consumables.sortedBy { c ->
            val statusPriority = when (c.status(machine.operatingHours, today)) {
                ConsumableStatus.OVERDUE -> 0
                ConsumableStatus.DUE_SOON -> 1
                ConsumableStatus.NORMAL -> 2
                ConsumableStatus.UNKNOWN -> 3
            }
            // 시간 기반 + 날짜 기반을 통합한 시급도.
            // 1일 ≈ 8h 가동시간으로 환산하여 두 척도 통합.
            val hoursLeft = c.hoursUntilReplacement(machine.operatingHours)
            val daysAsHours = c.daysUntilReplacement(today)?.toDouble()?.times(8.0)
            val urgency = listOfNotNull(hoursLeft, daysAsHours).minOrNull() ?: Double.MAX_VALUE
            statusPriority * 100_000.0 + urgency
        }
    }
    val topConsumables = sortedConsumables.take(3)
    val recentMaintenance = maintenanceRecords.take(3)

    // 빠른 교체 실행 취소를 위한 상태
    // pendingUndo가 null이 아니면 Snackbar에 [실행 취소] 버튼 표시
    var pendingUndo by remember {
        mutableStateOf<QuickReplaceUndo?>(null)
    }
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    // Snackbar 표시 / 사용자 액션 처리
    LaunchedEffect(pendingUndo) {
        val undo = pendingUndo ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "'${undo.consumableName}' 교체 처리됨",
            actionLabel = "실행 취소",
            duration = androidx.compose.material3.SnackbarDuration.Long
        )
        if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
            // 1) 정비기록 삭제
            AppContainer.maintenanceRepository.deleteMaintenance(undo.recordId)
            // 2) 소모품 원상복구
            AppContainer.consumableRepository.saveConsumable(undo.previousConsumable)
        }
        pendingUndo = null  // 다시 null로 → Snackbar 사라짐
    }

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        DetailTopBar(
            onBackClick = onBackClick,
            onEditClick = onEditClick
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item { HeroSection(machine) }

            item { StatsRow(machine) }

            if (machine.status == MachineStatus.UNDER_REPAIR) {
                item {
                    RepairAlertBanner(
                        statusNote = machine.statusNote,
                        onMarkComplete = {
                            coroutineScope.launch {
                                // 1) 기계 상태를 NORMAL로 갱신
                                AppContainer.machineRepository.saveMachine(
                                    machine.copy(
                                        status = MachineStatus.NORMAL,
                                        statusNote = null
                                    )
                                )
                                // 2) 진행 중이던 수리 정비기록을 완료로 표시
                                maintenanceRecords
                                    .firstOrNull {
                                        it.type == MaintenanceType.REPAIR && it.isInProgress
                                    }
                                    ?.let { record ->
                                        AppContainer.maintenanceRepository
                                            .updateMaintenance(record.copy(isInProgress = false))
                                    }
                                // 3) 목록으로 복귀 (machine 스냅샷이 stale이므로)
                                onMarkRepairComplete()
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }

            if (topConsumables.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(
                            title = "곧 교체할 소모품",
                            actionLabel = "전체 보기",
                            onActionClick = onViewAllConsumables
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        CardSurface {
                            topConsumables.forEachIndexed { index, consumable ->
                                ConsumableRow(
                                    consumable = consumable,
                                    currentHours = machine.operatingHours,
                                    today = today,
                                    onQuickReplaceClick = {
                                        coroutineScope.launch {
                                            // 1) 소모품 갱신 - 교체 시점 기록
                                            val updated = consumable.copy(
                                                lastReplacedDate = today,
                                                lastReplacedHours = machine.operatingHours
                                            )
                                            AppContainer.consumableRepository.saveConsumable(updated)
                                            // 2) 정비기록 자동 생성
                                            val recordId = "record_${System.currentTimeMillis()}"
                                            val record = MaintenanceRecord(
                                                id = recordId,
                                                machineId = machine.id,
                                                date = today,
                                                type = MaintenanceType.CONSUMABLE_REPLACE,
                                                title = "${consumable.name} 교체",
                                                operatingHoursAtMaintenance = machine.operatingHours,
                                                replacedConsumableIds = listOf(consumable.id)
                                            )
                                            AppContainer.maintenanceRepository.addMaintenance(record)
                                            // 3) 실행 취소용 스냅샷 저장 → Snackbar 표시
                                            pendingUndo = QuickReplaceUndo(
                                                consumableName = consumable.name,
                                                previousConsumable = consumable,  // copy 전의 원본
                                                recordId = recordId
                                            )
                                        }
                                    }
                                )
                                if (index < topConsumables.lastIndex) {
                                    Divider(
                                        color = BorderColor,
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (recentMaintenance.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Spacer(modifier = Modifier.height(20.dp))
                        SectionHeader(
                            title = "정비 이력",
                            actionLabel = "전체 보기",
                            onActionClick = onViewAllMaintenance
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        CardSurface {
                            recentMaintenance.forEachIndexed { index, record ->
                                MaintenanceRecordRow(
                                    record = record,
                                    modifier = Modifier.clickable {
                                        onEditMaintenanceClick(record)
                                    }
                                )
                                if (index < recentMaintenance.lastIndex) {
                                    Divider(
                                        color = BorderColor,
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Spacer(modifier = Modifier.height(20.dp))
                    SectionHeader(title = "기본 정보")
                    Spacer(modifier = Modifier.height(10.dp))
                    BasicInfoCard(machine)
                }
            }
        }

        BottomActionBar(
            onInspectionClick = onDailyInspectionClick,
            onAddMaintenanceClick = onAddMaintenanceClick
        )
    }

    // SnackbarHost를 화면 하단(BottomActionBar 위)에 띄움
    androidx.compose.material3.SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
            .align(androidx.compose.ui.Alignment.BottomCenter)
            .padding(bottom = 80.dp, start = 12.dp, end = 12.dp)
    )
    }
}

/**
 * 빠른 교체 실행 취소를 위한 스냅샷.
 * - previousConsumable: 교체 전 상태 (lastReplacedDate/Hours 복원용)
 * - recordId: 자동 생성된 정비기록 ID (삭제용)
 */
private data class QuickReplaceUndo(
    val consumableName: String,
    val previousConsumable: com.example.farmmachinemanager.data.Consumable,
    val recordId: String
)

// ============ 컴포넌트들 ============

@Composable
private fun DetailTopBar(
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfacePrimary)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TopBarIconButton(Icons.Default.ArrowBack, "뒤로", onBackClick)

        Row {
            TopBarIconButton(Icons.Default.Edit, "수정", onEditClick)
            TopBarIconButton(Icons.Default.MoreVert, "더보기") { /* TODO */ }
        }
    }
}

@Composable
private fun TopBarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = TextPrimary,
            modifier = Modifier.size(if (icon == Icons.Default.ArrowBack) 22.dp else 20.dp)
        )
    }
}

@Composable
private fun HeroSection(machine: Machine) {
    val (iconBg, iconTint) = heroIconColor(machine.type)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfacePrimary)
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = heroIcon(machine.type),
                contentDescription = machine.type.displayName,
                tint = iconTint,
                modifier = Modifier.size(72.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = machine.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            StatusBadge(machine.status)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = buildString {
                append(machine.manufacturer)
                append(" · ")
                append(machine.type.displayName)
                machine.horsepower?.let { append(" · ${it}마력") }
            },
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun StatusBadge(status: MachineStatus) {
    val (bg, fg) = when (status) {
        MachineStatus.NORMAL -> StatusNormalBg to StatusNormalText
        MachineStatus.INSPECTION_NEEDED -> StatusInspectionBg to StatusInspectionText
        MachineStatus.UNDER_REPAIR -> StatusRepairBg to StatusRepairText
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = status.displayName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = fg
        )
    }
}

@Composable
private fun StatsRow(machine: Machine) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfacePrimary)
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatTile(
            label = "가동시간",
            value = "%,d".format(machine.operatingHours.toInt()),
            unit = "h",
            modifier = Modifier.weight(1f)
        )
        StatTile(
            label = "마력",
            value = machine.horsepower?.toString() ?: "-",
            unit = if (machine.horsepower != null) "HP" else null,
            modifier = Modifier.weight(1f)
        )
        StatTile(
            label = "연식",
            value = machine.year?.toString() ?: "-",
            unit = if (machine.year != null) "년" else null,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RepairAlertBanner(
    statusNote: String?,
    onMarkComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RepairAlertBg)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(RepairAlertIconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = null,
                tint = RepairAlertIconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "현재 수리 중",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = RepairAlertTitle
            )
            statusNote?.let {
                Text(
                    text = "$it 시작",
                    fontSize = 11.sp,
                    color = RepairAlertSubtitle
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(0.5.dp, RepairAlertBorder, RoundedCornerShape(8.dp))
                .clickable(onClick = onMarkComplete)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = "완료 표시",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = RepairAlertTitle
            )
        }
    }
}

@Composable
private fun CardSurface(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
    ) {
        content()
    }
}

@Composable
private fun BasicInfoCard(machine: Machine) {
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    val templateSource = com.example.farmmachinemanager.data.MaintenanceTemplates.sourceLabel(machine.type)
    val rows = listOfNotNull(
        machine.serialNumber?.let { "시리얼번호" to it },
        machine.registrationNumber?.let { "등록번호" to it },
        machine.year?.let { "연식" to "${it}년" },
        machine.lastMaintenanceDate?.let { "최근 정비" to it.format(formatter) },
        templateSource?.let { "정비 일정 출처" to it }
    )

    if (rows.isEmpty()) return

    CardSurface {
        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
            rows.forEachIndexed { index, (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, fontSize = 12.sp, color = TextSecondary)
                    Text(value, fontSize = 12.sp, color = TextPrimary)
                }
                if (index < rows.lastIndex) {
                    Divider(color = BorderColor, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun BottomActionBar(
    onInspectionClick: () -> Unit,
    onAddMaintenanceClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfacePrimary)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 점검표 버튼 (좌측 보조)
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceSecondary)
                .clickable(onClick = onInspectionClick)
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.FactCheck,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = "점검표",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
        // 정비기록 추가 (우측 주력)
        Row(
            modifier = Modifier
                .weight(1.4f)
                .clip(RoundedCornerShape(12.dp))
                .background(TextPrimary)
                .clickable(onClick = onAddMaintenanceClick)
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = SurfacePrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = "정비 기록 추가",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SurfacePrimary
            )
        }
    }
}

// ============ 헬퍼 ============

private fun heroIconColor(type: MachineType): Pair<Color, Color> = when (type) {
    MachineType.TRACTOR -> TractorIconBg to TractorIconTint
    MachineType.COMBINE -> CombineIconBg to CombineIconTint
    MachineType.RICE_TRANSPLANTER -> TransplanterIconBg to TransplanterIconTint
    MachineType.FORKLIFT -> ForkliftIconBg to ForkliftIconTint
    MachineType.VEHICLE -> VehicleIconBg to VehicleIconTint
    MachineType.CULTIVATOR, MachineType.OTHER -> OtherIconBg to OtherIconTint
}

@Composable
private fun heroIcon(type: MachineType): ImageVector = when (type) {
    MachineType.TRACTOR, MachineType.CULTIVATOR ->
        androidx.compose.material.icons.Icons.Default.Agriculture
    MachineType.COMBINE ->
        ImageVector.vectorResource(R.drawable.ic_combine)
    MachineType.RICE_TRANSPLANTER ->
        ImageVector.vectorResource(R.drawable.ic_transplanter)
    MachineType.FORKLIFT ->
        ImageVector.vectorResource(R.drawable.ic_forklift)
    MachineType.VEHICLE ->
        androidx.compose.material.icons.Icons.Default.DirectionsCar
    MachineType.OTHER ->
        androidx.compose.material.icons.Icons.Default.Build
}

// ============ Preview ============

@Preview(showBackground = true, widthDp = 380, heightDp = 1000)
@Composable
private fun MachineDetailScreenPreview() {
    FarmMachineTheme {
        MachineDetailScreen(machine = SampleData.machines[0])
    }
}

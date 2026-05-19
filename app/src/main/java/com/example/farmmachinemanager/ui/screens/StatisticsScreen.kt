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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmmachinemanager.AppContainer
import com.example.farmmachinemanager.data.MaintenanceRecord
import com.example.farmmachinemanager.data.MaintenanceType
import com.example.farmmachinemanager.ui.theme.BorderColor
import com.example.farmmachinemanager.ui.theme.StatusInspectionBg
import com.example.farmmachinemanager.ui.theme.StatusNormalText
import com.example.farmmachinemanager.ui.theme.SurfacePrimary
import com.example.farmmachinemanager.ui.theme.SurfaceSecondary
import com.example.farmmachinemanager.ui.theme.TextPrimary
import com.example.farmmachinemanager.ui.theme.TextSecondary
import com.example.farmmachinemanager.ui.theme.TextTertiary
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * 점검 이력 통계 화면.
 *
 * 표시 항목:
 * 1. 요약 카드: 총 정비 횟수 / 총 비용 / 평균 비용
 * 2. 월별 정비 비용 추이 (최근 6개월, 간단한 막대 차트)
 * 3. 정비 종류별 분포 (정기점검/수리/소모품/검사/기타)
 * 4. 기계별 정비 비용 순위 (top 5)
 *
 * Compose만으로 차트 그림 - 외부 라이브러리 불필요.
 */
@Composable
fun StatisticsScreen(onBack: () -> Unit) {
    BackHandler { onBack() }

    val machines by AppContainer.machineRepository.observeMachines()
        .collectAsState(initial = emptyList())
    val records by AppContainer.maintenanceRepository.observeAllMaintenance()
        .collectAsState(initial = emptyList())

    // 사전 계산
    val totalCount = records.size
    val totalCost = records.sumOf { it.cost ?: 0 }
    val avgCost = if (totalCount > 0) totalCost / totalCount else 0

    val machineNameById = remember(machines) {
        machines.associate { it.id to it.name }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        StatsTopBar(onBack = onBack)

        if (records.isEmpty()) {
            EmptyStatsView()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(PaddingValues(16.dp)),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 요약 카드 3종
                SummaryCards(
                    totalCount = totalCount,
                    totalCost = totalCost,
                    avgCost = avgCost
                )

                // 월별 비용 차트
                SectionTitle("월별 정비 비용 (최근 6개월)")
                MonthlyCostChart(records = records)

                // 정비 종류별 분포
                SectionTitle("정비 종류별 비율")
                TypeDistribution(records = records)

                // 기계별 비용 순위
                SectionTitle("기계별 정비 비용 TOP 5")
                MachineCostRanking(
                    records = records,
                    machineNameById = machineNameById
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SummaryCards(totalCount: Int, totalCost: Int, avgCost: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard(label = "총 정비", value = "${totalCount}건", modifier = Modifier.weight(1f))
        SummaryCard(
            label = "총 비용",
            value = formatWon(totalCost),
            modifier = Modifier.weight(1.4f)
        )
        SummaryCard(label = "건당 평균", value = formatWon(avgCost), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

/**
 * 월별 정비 비용 막대 차트 (최근 6개월).
 * Canvas 대신 Box weight로 간단히 표현.
 */
@Composable
private fun MonthlyCostChart(records: List<MaintenanceRecord>) {
    val today = remember { LocalDate.now() }
    val months = remember(today) {
        (5 downTo 0).map { YearMonth.from(today).minusMonths(it.toLong()) }
    }
    val monthlyCost = remember(records, months) {
        months.associateWith { ym ->
            records
                .filter {
                    val rDate = it.date
                    rDate.year == ym.year && rDate.monthValue == ym.monthValue
                }
                .sumOf { it.cost ?: 0 }
        }
    }
    val maxCost = (monthlyCost.values.maxOrNull() ?: 0).coerceAtLeast(1)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        // 가로 막대 6개를 균등하게 배치
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            months.forEach { ym ->
                val cost = monthlyCost[ym] ?: 0
                val heightRatio = cost.toFloat() / maxCost.toFloat()
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = if (cost > 0) shortWon(cost) else "",
                        fontSize = 9.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    // 막대 자체
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height((heightRatio * 100).coerceAtLeast(if (cost > 0) 4f else 0f).dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(StatusNormalText)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            months.forEach { ym ->
                Text(
                    text = "${ym.monthValue}월",
                    fontSize = 10.sp,
                    color = TextTertiary,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun TypeDistribution(records: List<MaintenanceRecord>) {
    val total = records.size
    val byType = remember(records) {
        MaintenanceType.values()
            .map { type -> type to records.count { it.type == type } }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        byType.forEach { (type, count) ->
            val pct = if (total > 0) (count * 100) / total else 0
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = type.displayName,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${count}건 (${pct}%)",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // 비율 막대
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(SurfaceSecondary)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(pct / 100f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(StatusNormalText)
                    )
                }
            }
        }
    }
}

@Composable
private fun MachineCostRanking(
    records: List<MaintenanceRecord>,
    machineNameById: Map<String, String>
) {
    val ranking = remember(records, machineNameById) {
        records
            .groupBy { it.machineId }
            .mapValues { (_, recs) -> recs.sumOf { it.cost ?: 0 } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
            .filter { it.second > 0 }
    }

    if (ranking.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfacePrimary)
                .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "비용 데이터가 없습니다", fontSize = 12.sp, color = TextSecondary)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
    ) {
        ranking.forEachIndexed { index, (machineId, cost) ->
            val name = machineNameById[machineId] ?: "(삭제된 기계)"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(StatusInspectionBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = name,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatWon(cost),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            if (index < ranking.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(BorderColor)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(label: String) {
    Text(
        text = label,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = TextSecondary,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun EmptyStatsView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "통계 데이터 없음",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "정비 기록을 추가하면\n통계가 자동으로 집계됩니다.",
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun StatsTopBar(onBack: () -> Unit) {
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
        Text(
            text = "통계",
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

// ============ 유틸 ============

private fun formatWon(amount: Int): String {
    if (amount == 0) return "₩0"
    return "₩" + "%,d".format(amount)
}

/** "₩1,234,567" → "1.2백만" 같이 짧게 */
private fun shortWon(amount: Int): String = when {
    amount >= 10_000_000 -> "${amount / 1_000_000}백만"
    amount >= 1_000_000 -> "%.1f백만".format(amount / 1_000_000.0)
    amount >= 10_000 -> "${amount / 10_000}만"
    amount >= 1_000 -> "${amount / 1_000}천"
    else -> amount.toString()
}

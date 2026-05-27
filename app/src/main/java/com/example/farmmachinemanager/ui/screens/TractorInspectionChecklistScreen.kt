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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmmachinemanager.AppContainer
import com.example.farmmachinemanager.data.manual.TractorInspectionItem
import com.example.farmmachinemanager.data.manual.TractorInspectionScheduleData
import com.example.farmmachinemanager.ui.theme.BorderColor
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
import com.example.farmmachinemanager.ui.theme.TextTertiary

/**
 * 쿠보타 트랙터(MR1050) 정기점검 일람표 화면.
 *
 * 이앙기와 달리 점검 항목이 평탄(flat) 구조이며 작업·간격이 item 에 직접 포함된다.
 * 2 단계 탐색:
 *   1) 점검 항목 리스트 (간격별 그룹 헤더) + 범례·사양 변형·주의사항
 *   2) 항목 상세 (작업, 간격, 적용 사양, 길들임/서비스 필요 여부, 매뉴얼 페이지)
 */
private sealed interface TractorInspScreen {
    data object List : TractorInspScreen
    data class Detail(val item: TractorInspectionItem) : TractorInspScreen
}

@Composable
fun TractorInspectionChecklistScreen(onBack: () -> Unit) {
    var data by remember { mutableStateOf<TractorInspectionScheduleData?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            data = AppContainer.manualRepository.loadTractorInspectionSchedule()
        } catch (t: Throwable) {
            loadError = t.message ?: "데이터를 불러올 수 없습니다"
        }
    }

    var screen: TractorInspScreen by remember { mutableStateOf(TractorInspScreen.List) }

    BackHandler {
        screen = when (screen) {
            is TractorInspScreen.List -> {
                onBack()
                TractorInspScreen.List
            }
            is TractorInspScreen.Detail -> TractorInspScreen.List
        }
    }

    val current = data
    when {
        loadError != null -> TractorInspErrorView(loadError!!, onBack)
        current == null -> TractorInspLoadingView(onBack)
        else -> when (val s = screen) {
            is TractorInspScreen.List -> TractorInspListView(
                data = current,
                onBack = onBack,
                onItemClick = { screen = TractorInspScreen.Detail(it) }
            )
            is TractorInspScreen.Detail -> TractorInspDetailView(
                item = s.item,
                onBack = { screen = TractorInspScreen.List }
            )
        }
    }
}

// ====================== Loading / Error ======================

@Composable
private fun TractorInspLoadingView(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorInspTopBar("쿠보타 트랙터 정기점검 일람", null, onBack)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun TractorInspErrorView(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorInspTopBar("쿠보타 트랙터 정기점검 일람", null, onBack)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("점검 데이터를 불러올 수 없습니다", fontSize = 16.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                Text(message, fontSize = 13.sp, color = TextTertiary)
            }
        }
    }
}

// ====================== List ======================

@Composable
private fun TractorInspListView(
    data: TractorInspectionScheduleData,
    onBack: () -> Unit,
    onItemClick: (TractorInspectionItem) -> Unit,
) {
    // 간격(interval_ko)별 그룹. legend 의 interval_groups_ko 순서를 우선 사용.
    val order = data.legend?.intervalGroupsKo ?: emptyList()
    val grouped: List<Pair<String, List<TractorInspectionItem>>> =
        data.items.groupBy { it.intervalKo }
            .toList()
            .sortedBy { (interval, _) ->
                val idx = order.indexOf(interval)
                if (idx >= 0) idx else Int.MAX_VALUE
            }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorInspTopBar("쿠보타 트랙터 정기점검 일람", data.machine.seriesNameKo, onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            grouped.forEach { (interval, items) ->
                item(key = "header-$interval") {
                    TractorInspIntervalHeader(interval, items.size)
                }
                items(items, key = { it.id }) { item ->
                    TractorInspItemRow(item) { onItemClick(item) }
                }
                item(key = "spacer-$interval") { Spacer(Modifier.height(8.dp)) }
            }

            if (data.specificationsVariantsKo.isNotEmpty()) {
                item(key = "variants") {
                    TractorInspDetailSection("사양 변형 설명") {
                        data.specificationsVariantsKo.entries.forEach { (code, desc) ->
                            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(StatusRepairBg)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(code, fontSize = 11.sp, color = StatusRepairText, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(desc, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            if (data.generalNotesKo.isNotEmpty()) {
                item(key = "notes") {
                    TractorInspDetailSection("일반 주의사항") {
                        data.generalNotesKo.forEach { TractorInspBullet(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TractorInspIntervalHeader(interval: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(StatusNormalBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Engineering, null, tint = StatusNormalText, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = interval,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
        )
        Spacer(Modifier.width(8.dp))
        Text("${count}개", fontSize = 12.sp, color = TextTertiary)
    }
}

@Composable
private fun TractorInspItemRow(item: TractorInspectionItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(item.id, fontSize = 11.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(8.dp))
            item.appliesToSpec?.let { TractorInspSpecBadge("${it} 사양") }
            if (item.serviceRequired) {
                Spacer(Modifier.width(6.dp))
                TractorInspServiceBadge()
            }
            Spacer(Modifier.weight(1f))
            item.pageRef?.let { Text("p.${it}", fontSize = 11.sp, color = TextTertiary) }
        }
        Spacer(Modifier.height(6.dp))
        Text(item.nameKo, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.padding(top = 2.dp)) {
            TractorInspActionChip(item.actionKo)
            Spacer(Modifier.width(8.dp))
            Text(item.intervalKo, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1f))
        }
    }
}

// ====================== Detail ======================

@Composable
private fun TractorInspDetailView(
    item: TractorInspectionItem,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorInspTopBar(item.id, item.nameKo, onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TractorInspDetailSection("점검 항목") {
                Text(item.nameKo, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                item.nameJa?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("原: $it", fontSize = 12.sp, color = TextTertiary)
                }
            }

            TractorInspDetailSection("작업 · 간격") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TractorInspActionChip(item.actionKo)
                    item.actionJa?.let {
                        Spacer(Modifier.width(8.dp))
                        Text("原: $it", fontSize = 11.sp, color = TextTertiary)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("간격 · ${item.intervalKo}", fontSize = 14.sp, color = TextPrimary)
                item.noteKo?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, fontSize = 13.sp, color = TextSecondary)
                }
            }

            if (item.serviceRequired || item.breakInRequired || item.appliesToSpec != null || item.appliesTo != null) {
                TractorInspDetailSection("적용 조건") {
                    item.appliesToSpec?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TractorInspSpecBadge("${it} 사양 전용")
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                    item.appliesTo?.let {
                        Text("적용 · $it", fontSize = 13.sp, color = TextSecondary)
                        Spacer(Modifier.height(6.dp))
                    }
                    if (item.breakInRequired) {
                        Text(
                            "길들임 운전 50시간 후 초회 점검이 필요한 항목입니다",
                            fontSize = 13.sp,
                            color = StatusInspectionText,
                        )
                        if (item.serviceRequired) Spacer(Modifier.height(6.dp))
                    }
                    if (item.serviceRequired) {
                        Text(
                            "구입처(서비스센터) 연락이 필요한 작업입니다",
                            fontSize = 13.sp,
                            color = StatusRepairText,
                        )
                    }
                }
            }

            TractorInspDetailSection("출처") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.MenuBook, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "쿠보타 트랙터 매뉴얼" + (item.pageRef?.let { " p.${it}" } ?: ""),
                        fontSize = 13.sp,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}

// ====================== Common ======================

@Composable
private fun TractorInspActionChip(actionKo: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(StatusNormalBg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(actionKo, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = StatusNormalText)
    }
}

@Composable
private fun TractorInspBullet(text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("• ", fontSize = 13.sp, color = TextSecondary)
        Text(text, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TractorInspDetailSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun TractorInspSpecBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(StatusRepairBg)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(text, fontSize = 11.sp, color = StatusRepairText, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TractorInspServiceBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(StatusInspectionBg)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text("서비스 필요", fontSize = 11.sp, color = StatusInspectionText, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TractorInspTopBar(title: String, subtitle: String?, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfacePrimary)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.ArrowBack, "뒤로", tint = TextPrimary)
        }
        Spacer(Modifier.width(4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            subtitle?.let { Text(it, fontSize = 12.sp, color = TextSecondary) }
        }
    }
}

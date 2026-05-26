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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmmachinemanager.AppContainer
import com.example.farmmachinemanager.data.manual.InspectionAction
import com.example.farmmachinemanager.data.manual.InspectionItem
import com.example.farmmachinemanager.data.manual.InspectionScheduleData
import com.example.farmmachinemanager.data.manual.InspectionSection
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
 * 쿠보타 이앙기 정기점검 일람표 화면.
 *
 * 3 단계 탐색:
 *   1) 섹션 (엔진부 / 주행·조작부 / 이앙부 / 시비부 / 전장부)
 *   2) 섹션 안의 점검 항목
 *   3) 항목 상세 (모든 작업·간격, 서비스 필요 여부, 매뉴얼 페이지)
 */
private sealed interface InspScreen {
    data object Sections : InspScreen
    data class Items(val section: InspectionSection) : InspScreen
    data class Detail(val item: InspectionItem, val section: InspectionSection) : InspScreen
}

@Composable
fun InspectionChecklistScreen(onBack: () -> Unit) {
    var data by remember { mutableStateOf<InspectionScheduleData?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            data = AppContainer.manualRepository.loadInspectionSchedule()
        } catch (t: Throwable) {
            loadError = t.message ?: "데이터를 불러올 수 없습니다"
        }
    }

    var screen: InspScreen by remember { mutableStateOf(InspScreen.Sections) }

    BackHandler {
        screen = when (val s = screen) {
            is InspScreen.Sections -> {
                onBack()
                s
            }
            is InspScreen.Items -> InspScreen.Sections
            is InspScreen.Detail -> InspScreen.Items(s.section)
        }
    }

    val current = data
    when {
        loadError != null -> InspErrorView(loadError!!, onBack)
        current == null -> InspLoadingView(onBack)
        else -> when (val s = screen) {
            is InspScreen.Sections -> SectionListView(
                data = current,
                onBack = onBack,
                onSectionClick = { screen = InspScreen.Items(it) }
            )
            is InspScreen.Items -> ItemListView(
                section = s.section,
                items = current.items.filter { it.sectionId == s.section.id },
                onBack = { screen = InspScreen.Sections },
                onItemClick = { screen = InspScreen.Detail(it, s.section) }
            )
            is InspScreen.Detail -> ItemDetailView(
                item = s.item,
                section = s.section,
                onBack = { screen = InspScreen.Items(s.section) }
            )
        }
    }
}

// ====================== Loading / Error ======================

@Composable
private fun InspLoadingView(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        InspTopBar("정기점검", null, onBack)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun InspErrorView(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        InspTopBar("정기점검", null, onBack)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("점검 데이터를 불러올 수 없습니다", fontSize = 16.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                Text(message, fontSize = 13.sp, color = TextTertiary)
            }
        }
    }
}

// ====================== Sections ======================

@Composable
private fun SectionListView(
    data: InspectionScheduleData,
    onBack: () -> Unit,
    onSectionClick: (InspectionSection) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        InspTopBar("정기점검", data.machine.seriesNameKo, onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(data.sections, key = { it.id }) { section ->
                val itemCount = data.items.count { it.sectionId == section.id }
                SectionCard(section, itemCount) { onSectionClick(section) }
            }
        }
    }
}

@Composable
private fun SectionCard(section: InspectionSection, itemCount: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(StatusNormalBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Engineering,
                contentDescription = null,
                tint = StatusNormalText,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(section.nameKo, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${itemCount}개 항목", fontSize = 12.sp, color = TextTertiary)
                section.appliesToSpec?.let {
                    Spacer(Modifier.width(8.dp))
                    InspSpecBadge("${it} 사양")
                }
                section.pageRef?.let {
                    Spacer(Modifier.width(8.dp))
                    Text("p.${it}", fontSize = 12.sp, color = TextTertiary)
                }
            }
        }
        Icon(Icons.Filled.ChevronRight, null, tint = TextTertiary)
    }
}

// ====================== Items ======================

@Composable
private fun ItemListView(
    section: InspectionSection,
    items: List<InspectionItem>,
    onBack: () -> Unit,
    onItemClick: (InspectionItem) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        InspTopBar(section.nameKo, "${items.size}개 점검 항목", onBack)
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("등록된 점검 항목이 없습니다", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    ItemRow(item) { onItemClick(item) }
                }
            }
        }
    }
}

@Composable
private fun ItemRow(item: InspectionItem, onClick: () -> Unit) {
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
            item.appliesToSpec?.let { InspSpecBadge("${it} 사양") }
            if (item.serviceRequired) {
                Spacer(Modifier.width(6.dp))
                ServiceBadge()
            }
            Spacer(Modifier.weight(1f))
            item.pageRef?.let { Text("p.${it}", fontSize = 11.sp, color = TextTertiary) }
        }
        Spacer(Modifier.height(6.dp))
        Text(item.nameKo, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        if (item.actions.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            item.actions.take(2).forEach { action ->
                Row(modifier = Modifier.padding(top = 2.dp)) {
                    ActionTypeChip(action)
                    Spacer(Modifier.width(8.dp))
                    Text(action.intervalKo, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1f))
                }
            }
            if (item.actions.size > 2) {
                Spacer(Modifier.height(2.dp))
                Text("+ ${item.actions.size - 2}개 작업 더", fontSize = 11.sp, color = TextTertiary)
            }
        }
    }
}

// ====================== Detail ======================

@Composable
private fun ItemDetailView(
    item: InspectionItem,
    section: InspectionSection,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        InspTopBar(item.id, section.nameKo, onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InspDetailSection("점검 항목") {
                Text(item.nameKo, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                item.nameJa?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("原: $it", fontSize = 12.sp, color = TextTertiary)
                }
            }
            if (item.actions.isNotEmpty()) {
                InspDetailSection("작업 · 간격 (${item.actions.size})") {
                    item.actions.forEachIndexed { idx, action ->
                        if (idx > 0) Spacer(Modifier.height(8.dp))
                        ActionRow(action)
                    }
                }
            }
            if (item.serviceRequired || item.appliesToSpec != null) {
                InspDetailSection("적용 조건") {
                    item.appliesToSpec?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            InspSpecBadge("${it} 사양 전용")
                        }
                        Spacer(Modifier.height(6.dp))
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
            InspDetailSection("출처") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.MenuBook, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "쿠보타 매뉴얼 PW600-9751-4" + (item.pageRef?.let { " p.${it}" } ?: ""),
                        fontSize = 13.sp,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionRow(action: InspectionAction) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceSecondary)
            .padding(12.dp)
    ) {
        ActionTypeChip(action)
        Spacer(Modifier.height(6.dp))
        Text(action.intervalKo, fontSize = 14.sp, color = TextPrimary)
    }
}

@Composable
private fun ActionTypeChip(action: InspectionAction) {
    val (bg, fg) = actionColor(action.type)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(action.typeKo, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = fg)
    }
}

private fun actionColor(type: String): Pair<Color, Color> = when (type) {
    "inspect", "inspect_adjust", "adjust" -> StatusNormalBg to StatusNormalText
    "replace", "replace_breakin" -> StatusRepairBg to StatusRepairText
    "clean" -> StatusInspectionBg to StatusInspectionText
    "charge" -> StatusInspectionBg to StatusInspectionText
    else -> StatusInspectionBg to StatusInspectionText
}

// ====================== Common ======================

@Composable
private fun InspDetailSection(title: String, content: @Composable () -> Unit) {
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
private fun InspSpecBadge(text: String) {
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
private fun ServiceBadge() {
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
private fun InspTopBar(title: String, subtitle: String?, onBack: () -> Unit) {
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

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
import androidx.compose.material.icons.outlined.Category
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
import com.example.farmmachinemanager.data.manual.AppliesTo
import com.example.farmmachinemanager.data.manual.ConsumableCategory
import com.example.farmmachinemanager.data.manual.ConsumablePart
import com.example.farmmachinemanager.data.manual.ConsumablesData
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
 * 쿠보타 이앙기 소모품 부품 일람 화면.
 *
 * 3 단계 탐색:
 *   1) 카테고리 (이앙발톱·램프·퓨즈·필터·벨트·배터리·시비부품·모대·차륜)
 *   2) 카테고리 안의 부품 (부품번호, 수량 미리보기)
 *   3) 부품 상세 (부품번호, 모델별·사양별 수량 표, 적용 조건, 매뉴얼 페이지)
 */
private sealed interface PartsScreen {
    data object Categories : PartsScreen
    data class Parts(val category: ConsumableCategory) : PartsScreen
    data class Detail(val part: ConsumablePart, val category: ConsumableCategory) : PartsScreen
}

@Composable
fun PartsListScreen(onBack: () -> Unit) {
    var data by remember { mutableStateOf<ConsumablesData?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            data = AppContainer.manualRepository.loadConsumables()
        } catch (t: Throwable) {
            loadError = t.message ?: "데이터를 불러올 수 없습니다"
        }
    }

    var screen: PartsScreen by remember { mutableStateOf(PartsScreen.Categories) }

    BackHandler {
        screen = when (val s = screen) {
            is PartsScreen.Categories -> {
                onBack()
                s
            }
            is PartsScreen.Parts -> PartsScreen.Categories
            is PartsScreen.Detail -> PartsScreen.Parts(s.category)
        }
    }

    val current = data
    when {
        loadError != null -> PartsErrorView(loadError!!, onBack)
        current == null -> PartsLoadingView(onBack)
        else -> when (val s = screen) {
            is PartsScreen.Categories -> CategoryListView(
                data = current,
                onBack = onBack,
                onCategoryClick = { screen = PartsScreen.Parts(it) }
            )
            is PartsScreen.Parts -> PartListView(
                category = s.category,
                parts = current.parts.filter { it.categoryId == s.category.id },
                onBack = { screen = PartsScreen.Categories },
                onPartClick = { screen = PartsScreen.Detail(it, s.category) }
            )
            is PartsScreen.Detail -> PartDetailView(
                part = s.part,
                category = s.category,
                onBack = { screen = PartsScreen.Parts(s.category) }
            )
        }
    }
}

// ====================== Loading / Error ======================

@Composable
private fun PartsLoadingView(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        PartsTopBar("소모품 부품", null, onBack)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun PartsErrorView(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        PartsTopBar("소모품 부품", null, onBack)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("부품 데이터를 불러올 수 없습니다", fontSize = 16.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                Text(message, fontSize = 13.sp, color = TextTertiary)
            }
        }
    }
}

// ====================== Categories ======================

@Composable
private fun CategoryListView(
    data: ConsumablesData,
    onBack: () -> Unit,
    onCategoryClick: (ConsumableCategory) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        PartsTopBar("소모품 부품", data.machine.seriesNameKo, onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(data.categories, key = { it.id }) { category ->
                val partCount = data.parts.count { it.categoryId == category.id }
                CategoryCardParts(category, partCount) { onCategoryClick(category) }
            }
        }
    }
}

@Composable
private fun CategoryCardParts(category: ConsumableCategory, partCount: Int, onClick: () -> Unit) {
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
                imageVector = Icons.Outlined.Category,
                contentDescription = null,
                tint = StatusNormalText,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(category.nameKo, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("${partCount}개 부품", fontSize = 12.sp, color = TextTertiary)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = TextTertiary)
    }
}

// ====================== Parts ======================

@Composable
private fun PartListView(
    category: ConsumableCategory,
    parts: List<ConsumablePart>,
    onBack: () -> Unit,
    onPartClick: (ConsumablePart) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        PartsTopBar(category.nameKo, "${parts.size}개 부품", onBack)
        if (parts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("등록된 부품이 없습니다", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(parts, key = { it.id }) { part ->
                    PartRow(part) { onPartClick(part) }
                }
            }
        }
    }
}

@Composable
private fun PartRow(part: ConsumablePart, onClick: () -> Unit) {
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
            Text(part.partNumber, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(8.dp))
            part.appliesToSpec?.let { PartsSpecBadge("${it} 사양") }
            if (part.serviceRequired) {
                Spacer(Modifier.width(6.dp))
                ServiceContactBadge()
            }
            Spacer(Modifier.weight(1f))
            part.pageRef?.let { Text("p.${it}", fontSize = 11.sp, color = TextTertiary) }
        }
        Spacer(Modifier.height(6.dp))
        Text(part.nameKo, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        quantitySummary(part)?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

private fun quantitySummary(part: ConsumablePart): String? {
    part.quantity?.let { return "수량: ${it}개" }
    part.quantitiesByModel?.let { byModel ->
        val grouped = byModel.entries.groupBy { it.value }
        val summary = grouped.entries.joinToString(" · ") { (qty, entries) ->
            val models = entries.joinToString("/") { it.key }
            "${models}: ${qty}개"
        }
        return summary
    }
    part.quantitiesBySpec?.let { bySpec ->
        return bySpec.entries.joinToString(" · ") { "${it.key}: ${it.value}개" }
    }
    return null
}

// ====================== Detail ======================

@Composable
private fun PartDetailView(
    part: ConsumablePart,
    category: ConsumableCategory,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        PartsTopBar(part.id, category.nameKo, onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PartsDetailSection("부품 정보") {
                Text(part.nameKo, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                part.nameJa?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("原: $it", fontSize = 12.sp, color = TextTertiary)
                }
                Spacer(Modifier.height(8.dp))
                Row {
                    Text("부품번호 ", fontSize = 13.sp, color = TextSecondary)
                    Text(part.partNumber, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                }
                part.specKo?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, fontSize = 13.sp, color = TextSecondary)
                }
            }

            PartsDetailSection("수량") {
                renderQuantities(part)
            }

            if (part.appliesTo != null || part.appliesToSpec != null || part.serviceRequired) {
                PartsDetailSection("적용 조건") {
                    when (val a = part.appliesTo) {
                        is AppliesTo.All -> Text("전체 모델 공통", fontSize = 13.sp, color = TextPrimary)
                        is AppliesTo.Models -> Text("적용 모델: ${a.list.joinToString(", ")}", fontSize = 13.sp, color = TextPrimary)
                        null -> Unit
                    }
                    part.appliesToSpec?.let {
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PartsSpecBadge("${it} 사양")
                        }
                    }
                    if (part.serviceRequired) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "구입처(서비스센터)에 연락하여 교환하세요",
                            fontSize = 13.sp,
                            color = StatusRepairText,
                        )
                    }
                }
            }

            PartsDetailSection("출처") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.MenuBook, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "쿠보타 매뉴얼 PW600-9751-4" + (part.pageRef?.let { " p.${it}" } ?: ""),
                        fontSize = 13.sp,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun renderQuantities(part: ConsumablePart) {
    part.quantity?.let {
        Text("${it}개 (모든 모델 공통)", fontSize = 14.sp, color = TextPrimary)
        return
    }
    part.quantitiesByModel?.let { byModel ->
        byModel.entries.sortedBy { it.key }.forEach { (model, qty) ->
            Row(modifier = Modifier.padding(vertical = 3.dp)) {
                Text(model, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.width(80.dp))
                Text("${qty}개", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            }
        }
        return
    }
    part.quantitiesBySpec?.let { bySpec ->
        bySpec.entries.forEach { (spec, qty) ->
            Row(modifier = Modifier.padding(vertical = 3.dp)) {
                Text(spec, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.width(80.dp))
                Text("${qty}개", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            }
        }
        return
    }
    Text("수량 정보 없음", fontSize = 13.sp, color = TextTertiary)
}

// ====================== Common ======================

@Composable
private fun PartsDetailSection(title: String, content: @Composable () -> Unit) {
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
private fun PartsSpecBadge(text: String) {
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
private fun ServiceContactBadge() {
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
private fun PartsTopBar(title: String, subtitle: String?, onBack: () -> Unit) {
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

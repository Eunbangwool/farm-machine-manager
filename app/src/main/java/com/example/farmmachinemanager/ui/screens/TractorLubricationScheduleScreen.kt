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
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.WaterDrop
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmmachinemanager.AppContainer
import com.example.farmmachinemanager.data.manual.TractorLubricationCategory
import com.example.farmmachinemanager.data.manual.TractorLubricationItem
import com.example.farmmachinemanager.data.manual.TractorLubricationScheduleData
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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * 쿠보타 트랙터(MR1050) 급유·주유 일람 화면.
 *
 * 2 단계 탐색:
 *   1) 카테고리별로 그룹된 단일 리스트 + 권장 윤활유 / 주의사항
 *   2) 항목 상세 (위치, 용량, 유체 종류, 경고, 적용 사양)
 */
private sealed interface TractorLubeScreen {
    data object List : TractorLubeScreen
    data class Detail(val item: TractorLubricationItem, val category: TractorLubricationCategory?) : TractorLubeScreen
}

@Composable
fun TractorLubricationScheduleScreen(onBack: () -> Unit) {
    var data by remember { mutableStateOf<TractorLubricationScheduleData?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            data = AppContainer.manualRepository.loadTractorLubricationSchedule()
        } catch (t: Throwable) {
            loadError = t.message ?: "데이터를 불러올 수 없습니다"
        }
    }

    var screen: TractorLubeScreen by remember { mutableStateOf(TractorLubeScreen.List) }

    BackHandler {
        screen = when (screen) {
            is TractorLubeScreen.List -> {
                onBack()
                TractorLubeScreen.List
            }
            is TractorLubeScreen.Detail -> TractorLubeScreen.List
        }
    }

    val current = data
    when {
        loadError != null -> TractorLubeErrorView(loadError!!, onBack)
        current == null -> TractorLubeLoadingView(onBack)
        else -> when (val s = screen) {
            is TractorLubeScreen.List -> TractorLubeListView(
                data = current,
                onBack = onBack,
                onItemClick = { item, category -> screen = TractorLubeScreen.Detail(item, category) }
            )
            is TractorLubeScreen.Detail -> TractorLubeDetailView(
                item = s.item,
                category = s.category,
                onBack = { screen = TractorLubeScreen.List }
            )
        }
    }
}

// ====================== Loading / Error ======================

@Composable
private fun TractorLubeLoadingView(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorLubeTopBar("쿠보타 트랙터 급유·주유 일람", null, onBack)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun TractorLubeErrorView(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorLubeTopBar("쿠보타 트랙터 급유·주유 일람", null, onBack)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("급유 데이터를 불러올 수 없습니다", fontSize = 16.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                Text(message, fontSize = 13.sp, color = TextTertiary)
            }
        }
    }
}

// ====================== List ======================

@Composable
private fun TractorLubeListView(
    data: TractorLubricationScheduleData,
    onBack: () -> Unit,
    onItemClick: (TractorLubricationItem, TractorLubricationCategory?) -> Unit,
) {
    val itemsByCategory: List<Pair<TractorLubricationCategory, List<TractorLubricationItem>>> =
        data.categories.map { cat -> cat to data.items.filter { it.categoryId == cat.id } }
    // 카테고리에 매칭되지 않는 항목.
    val categorizedIds = data.categories.map { it.id }.toSet()
    val uncategorized = data.items.filter { it.categoryId !in categorizedIds }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorLubeTopBar("쿠보타 트랙터 급유·주유 일람", data.machine.seriesNameKo, onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsByCategory.forEach { (category, items) ->
                if (items.isEmpty()) return@forEach
                item(key = "header-${category.id}") {
                    TractorLubeCategoryHeader(category)
                }
                items(items, key = { it.id }) { lubeItem ->
                    TractorLubeItemRow(item = lubeItem) { onItemClick(lubeItem, category) }
                }
                item(key = "spacer-${category.id}") {
                    Spacer(Modifier.height(8.dp))
                }
            }
            if (uncategorized.isNotEmpty()) {
                items(uncategorized, key = { it.id }) { lubeItem ->
                    TractorLubeItemRow(item = lubeItem) { onItemClick(lubeItem, null) }
                }
                item(key = "spacer-uncat") { Spacer(Modifier.height(8.dp)) }
            }

            if (data.recommendedLubricantsKo.isNotEmpty()) {
                item(key = "recommended") {
                    TractorLubeDetailSection("권장 윤활유") {
                        data.recommendedLubricantsKo.entries.forEachIndexed { idx, (key, value) ->
                            if (idx > 0) Spacer(Modifier.height(10.dp))
                            TractorLubeRecommendedRow(key = key, element = value)
                        }
                    }
                }
            }

            if (data.generalNotesKo.isNotEmpty()) {
                item(key = "notes") {
                    TractorLubeDetailSection("일반 주의사항") {
                        data.generalNotesKo.forEach { TractorLubeBullet(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TractorLubeCategoryHeader(category: TractorLubricationCategory) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val (icon, tint, bg) = tractorLubeCategoryVisual(category.id)
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = category.nameKo,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
        )
    }
}

private data class TractorLubeVisual(val icon: ImageVector, val tint: Color, val bg: Color)

private fun tractorLubeCategoryVisual(id: String): TractorLubeVisual = when {
    id.contains("fuel") -> TractorLubeVisual(Icons.Outlined.LocalGasStation, StatusRepairText, StatusRepairBg)
    id.contains("oil") -> TractorLubeVisual(Icons.Outlined.Opacity, StatusInspectionText, StatusInspectionBg)
    id.contains("coolant") || id.contains("water") || id.contains("urea") ->
        TractorLubeVisual(Icons.Outlined.WaterDrop, StatusNormalText, StatusNormalBg)
    id.contains("grease") -> TractorLubeVisual(Icons.Outlined.Opacity, StatusInspectionText, StatusInspectionBg)
    else -> TractorLubeVisual(Icons.Outlined.Opacity, StatusInspectionText, StatusInspectionBg)
}

@Composable
private fun TractorLubeItemRow(item: TractorLubricationItem, onClick: () -> Unit) {
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
            item.no?.let {
                Text("#$it", fontSize = 11.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(8.dp))
            }
            item.appliesToSpec?.let { TractorLubeSpecBadge("${it} 사양") }
            Spacer(Modifier.weight(1f))
            item.pageRef?.let { Text("p.${it}", fontSize = 11.sp, color = TextTertiary) }
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Filled.ChevronRight, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(item.locationKo, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        item.lubricantTypeKo?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, fontSize = 12.sp, color = TextSecondary)
        }
        item.capacityKo?.let {
            Spacer(Modifier.height(2.dp))
            Text("용량 · $it", fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun TractorLubeRecommendedRow(key: String, element: JsonElement) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceSecondary)
            .padding(12.dp)
    ) {
        Text(prettyLubricantKey(key), fontSize = 11.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        renderLubricantElement(element)
    }
}

@Composable
private fun renderLubricantElement(element: JsonElement) {
    when (element) {
        is JsonPrimitive -> Text(element.contentOrNull ?: element.toString(), fontSize = 14.sp, color = TextPrimary)
        is JsonObject -> {
            // name_ko 우선 표시 후 나머지 문자열 필드는 부가 정보로.
            (element["name_ko"] as? JsonPrimitive)?.contentOrNull?.let {
                Text(it, fontSize = 14.sp, color = TextPrimary)
            }
            element.entries.forEach { (k, v) ->
                if (k == "name_ko") return@forEach
                when (v) {
                    is JsonPrimitive -> {
                        v.contentOrNull?.let {
                            Spacer(Modifier.height(2.dp))
                            Text("$it", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                    is JsonArray -> {
                        v.forEach { el ->
                            (el as? JsonPrimitive)?.contentOrNull?.let { line ->
                                Row(modifier = Modifier.padding(top = 2.dp)) {
                                    Text("• ", fontSize = 12.sp, color = TextSecondary)
                                    Text(line, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }
        is JsonArray -> {
            element.forEach { el ->
                (el as? JsonPrimitive)?.contentOrNull?.let { line ->
                    Row(modifier = Modifier.padding(vertical = 1.dp)) {
                        Text("• ", fontSize = 13.sp, color = TextSecondary)
                        Text(line, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private fun prettyLubricantKey(key: String): String = when (key) {
    "engine_oil" -> "엔진 오일"
    "mission_oil" -> "미션 오일"
    "urea" -> "요소수 (AdBlue)"
    "grease_general" -> "범용 그리스"
    "grease_horn_contact" -> "혼 접점 그리스"
    "coolant" -> "냉각수"
    else -> key
}

// ====================== Detail ======================

@Composable
private fun TractorLubeDetailView(
    item: TractorLubricationItem,
    category: TractorLubricationCategory?,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorLubeTopBar(item.id, category?.nameKo, onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TractorLubeDetailSection("위치") {
                Text(item.locationKo, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                item.locationJa?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("原: $it", fontSize = 12.sp, color = TextTertiary)
                }
            }

            if (item.capacityKo != null || item.lubricantTypeKo != null) {
                TractorLubeDetailSection("용량 · 유체") {
                    item.lubricantTypeKo?.let {
                        TractorLubeLabelRow("유체 종류", it)
                        item.lubricantTypeJa?.let { ja ->
                            Spacer(Modifier.height(2.dp))
                            Text("原: $ja", fontSize = 12.sp, color = TextTertiary)
                        }
                    }
                    item.capacityKo?.let {
                        Spacer(Modifier.height(6.dp))
                        TractorLubeLabelRow("용량", it)
                    }
                }
            }

            item.warningKo?.let { warn ->
                TractorLubeDetailSection("경고") {
                    Text(warn, fontSize = 13.sp, color = StatusRepairText)
                }
            }

            if (item.appliesToSpec != null || item.appliesToSpecNoteKo != null) {
                TractorLubeDetailSection("적용 조건") {
                    item.appliesToSpec?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TractorLubeSpecBadge("${it} 사양")
                        }
                    }
                    item.appliesToSpecNoteKo?.let {
                        Spacer(Modifier.height(6.dp))
                        Text(it, fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }

            TractorLubeDetailSection("출처") {
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

@Composable
private fun TractorLubeLabelRow(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 14.sp, color = TextPrimary)
    }
}

// ====================== Common ======================

@Composable
private fun TractorLubeBullet(text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("• ", fontSize = 13.sp, color = TextSecondary)
        Text(text, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TractorLubeSpecBadge(text: String) {
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
private fun TractorLubeDetailSection(title: String, content: @Composable () -> Unit) {
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
private fun TractorLubeTopBar(title: String, subtitle: String?, onBack: () -> Unit) {
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

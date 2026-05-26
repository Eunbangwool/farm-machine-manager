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
import com.example.farmmachinemanager.data.manual.LubricationCategory
import com.example.farmmachinemanager.data.manual.LubricationItem
import com.example.farmmachinemanager.data.manual.LubricationScheduleData
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
 * 쿠보타 이앙기 급유·주유 일람 화면.
 *
 * 2 단계 탐색 (항목 수가 8개로 적어 카테고리 그룹 헤더만 둔다):
 *   1) 카테고리별로 그룹된 단일 리스트
 *   2) 항목 상세 (점검 간격, 교환 간격, 용량, 유체 종류, 적용 위치 목록)
 */
private sealed interface LubeScreen {
    data object List : LubeScreen
    data class Detail(val item: LubricationItem, val category: LubricationCategory) : LubeScreen
}

@Composable
fun LubricationScheduleScreen(onBack: () -> Unit) {
    var data by remember { mutableStateOf<LubricationScheduleData?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            data = AppContainer.manualRepository.loadLubricationSchedule()
        } catch (t: Throwable) {
            loadError = t.message ?: "데이터를 불러올 수 없습니다"
        }
    }

    var screen: LubeScreen by remember { mutableStateOf(LubeScreen.List) }

    BackHandler {
        screen = when (screen) {
            is LubeScreen.List -> {
                onBack()
                LubeScreen.List
            }
            is LubeScreen.Detail -> LubeScreen.List
        }
    }

    val current = data
    when {
        loadError != null -> LubeErrorView(loadError!!, onBack)
        current == null -> LubeLoadingView(onBack)
        else -> when (val s = screen) {
            is LubeScreen.List -> ItemListView(
                data = current,
                onBack = onBack,
                onItemClick = { item, category -> screen = LubeScreen.Detail(item, category) }
            )
            is LubeScreen.Detail -> ItemDetailView(
                item = s.item,
                category = s.category,
                onBack = { screen = LubeScreen.List }
            )
        }
    }
}

// ====================== Loading / Error ======================

@Composable
private fun LubeLoadingView(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        LubeTopBar("급유·주유 일람", null, onBack)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun LubeErrorView(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        LubeTopBar("급유·주유 일람", null, onBack)
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
private fun ItemListView(
    data: LubricationScheduleData,
    onBack: () -> Unit,
    onItemClick: (LubricationItem, LubricationCategory) -> Unit,
) {
    val itemsByCategory: List<Pair<LubricationCategory, List<LubricationItem>>> =
        data.categories.map { cat -> cat to data.items.filter { it.categoryId == cat.id } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        LubeTopBar("급유·주유 일람", data.machine.seriesNameKo, onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsByCategory.forEach { (category, items) ->
                if (items.isEmpty()) return@forEach
                item(key = "header-${category.id}") {
                    CategoryHeader(category)
                }
                items(items, key = { it.id }) { lubeItem ->
                    LubeItemRow(item = lubeItem) { onItemClick(lubeItem, category) }
                }
                item(key = "spacer-${category.id}") {
                    Spacer(Modifier.height(8.dp))
                }
            }
            if (data.generalNotesKo.isNotEmpty()) {
                item {
                    NotesCard(notes = data.generalNotesKo)
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(category: LubricationCategory) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val (icon, tint, bg) = categoryVisual(category.id)
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

private data class CategoryVisual(val icon: ImageVector, val tint: Color, val bg: Color)

private fun categoryVisual(id: String): CategoryVisual = when (id) {
    "fuel" -> CategoryVisual(Icons.Outlined.LocalGasStation, StatusRepairText, StatusRepairBg)
    "oil" -> CategoryVisual(Icons.Outlined.Opacity, StatusInspectionText, StatusInspectionBg)
    "coolant" -> CategoryVisual(Icons.Outlined.WaterDrop, StatusNormalText, StatusNormalBg)
    "grease" -> CategoryVisual(Icons.Outlined.Opacity, StatusInspectionText, StatusInspectionBg)
    else -> CategoryVisual(Icons.Outlined.Opacity, StatusInspectionText, StatusInspectionBg)
}

@Composable
private fun LubeItemRow(item: LubricationItem, onClick: () -> Unit) {
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
            Text(item.action, fontSize = 11.sp, color = TextTertiary)
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
private fun NotesCard(notes: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text("일반 주의사항", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        Spacer(Modifier.height(8.dp))
        notes.forEach {
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Text("• ", fontSize = 13.sp, color = TextSecondary)
                Text(it, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f))
            }
        }
    }
}

// ====================== Detail ======================

@Composable
private fun ItemDetailView(
    item: LubricationItem,
    category: LubricationCategory,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        LubeTopBar(item.id, category.nameKo, onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LubeDetailSection("위치 · 작업") {
                Text(item.locationKo, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                item.locationJa?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("原: $it", fontSize = 12.sp, color = TextTertiary)
                }
                Spacer(Modifier.height(8.dp))
                Text("작업 종류 · ${item.action}", fontSize = 13.sp, color = TextSecondary)
            }

            LubeDetailSection("점검·교환 간격") {
                item.inspectionIntervalKo?.let {
                    LabelRow("점검", it)
                }
                item.replacementIntervalKo?.let {
                    Spacer(Modifier.height(6.dp))
                    LabelRow("교환", it)
                }
            }

            if (item.capacityKo != null || item.specCapacityKo != null || item.lubricantTypeKo != null) {
                LubeDetailSection("용량 · 유체") {
                    item.lubricantTypeKo?.let { LabelRow("유체 종류", it) }
                    item.capacityKo?.let {
                        Spacer(Modifier.height(6.dp))
                        LabelRow("용량", it)
                    }
                    item.specCapacityKo?.let {
                        Spacer(Modifier.height(6.dp))
                        LabelRow("기준", it)
                    }
                }
            }

            if (item.appliesLocationsKo.isNotEmpty()) {
                LubeDetailSection("적용 위치 (${item.appliesLocationsKo.size})") {
                    item.appliesLocationsKo.forEach { loc ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("• ", fontSize = 13.sp, color = TextSecondary)
                            Text(loc, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            LubeDetailSection("출처") {
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
private fun LabelRow(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 14.sp, color = TextPrimary)
    }
}

// ====================== Common ======================

@Composable
private fun LubeDetailSection(title: String, content: @Composable () -> Unit) {
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
private fun LubeTopBar(title: String, subtitle: String?, onBack: () -> Unit) {
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

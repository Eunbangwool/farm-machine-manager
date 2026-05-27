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
import com.example.farmmachinemanager.data.manual.TractorConsumableCategory
import com.example.farmmachinemanager.data.manual.TractorConsumablePart
import com.example.farmmachinemanager.data.manual.TractorConsumablesData
import com.example.farmmachinemanager.ui.theme.BorderColor
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
 * 쿠보타 트랙터(MR1050) 소모품 부품 일람 화면 (퓨즈·전구·와이퍼).
 *
 * 3 단계 탐색:
 *   1) 카테고리
 *   2) 카테고리 안의 부품 (부품번호 미리보기)
 *   3) 부품 상세 (부품번호, 사양, 적용 조건, 매뉴얼 페이지)
 */
private sealed interface TractorPartsScreen {
    data object Categories : TractorPartsScreen
    data class Parts(val category: TractorConsumableCategory) : TractorPartsScreen
    data class Detail(val part: TractorConsumablePart, val category: TractorConsumableCategory) : TractorPartsScreen
}

@Composable
fun TractorPartsListScreen(onBack: () -> Unit) {
    var data by remember { mutableStateOf<TractorConsumablesData?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            data = AppContainer.manualRepository.loadTractorConsumables()
        } catch (t: Throwable) {
            loadError = t.message ?: "데이터를 불러올 수 없습니다"
        }
    }

    var screen: TractorPartsScreen by remember { mutableStateOf(TractorPartsScreen.Categories) }

    BackHandler {
        screen = when (val s = screen) {
            is TractorPartsScreen.Categories -> {
                onBack()
                s
            }
            is TractorPartsScreen.Parts -> TractorPartsScreen.Categories
            is TractorPartsScreen.Detail -> TractorPartsScreen.Parts(s.category)
        }
    }

    val current = data
    when {
        loadError != null -> TractorPartsErrorView(loadError!!, onBack)
        current == null -> TractorPartsLoadingView(onBack)
        else -> when (val s = screen) {
            is TractorPartsScreen.Categories -> TractorCategoryListView(
                data = current,
                onBack = onBack,
                onCategoryClick = { screen = TractorPartsScreen.Parts(it) }
            )
            is TractorPartsScreen.Parts -> TractorPartListView(
                category = s.category,
                parts = current.parts.filter { it.categoryId == s.category.id },
                onBack = { screen = TractorPartsScreen.Categories },
                onPartClick = { screen = TractorPartsScreen.Detail(it, s.category) }
            )
            is TractorPartsScreen.Detail -> TractorPartDetailView(
                part = s.part,
                category = s.category,
                onBack = { screen = TractorPartsScreen.Parts(s.category) }
            )
        }
    }
}

// ====================== Loading / Error ======================

@Composable
private fun TractorPartsLoadingView(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorPartsTopBar("쿠보타 트랙터 소모품 부품", null, onBack)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun TractorPartsErrorView(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorPartsTopBar("쿠보타 트랙터 소모품 부품", null, onBack)
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
private fun TractorCategoryListView(
    data: TractorConsumablesData,
    onBack: () -> Unit,
    onCategoryClick: (TractorConsumableCategory) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorPartsTopBar("쿠보타 트랙터 소모품 부품", data.machine.seriesNameKo, onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(data.categories, key = { it.id }) { category ->
                val partCount = data.parts.count { it.categoryId == category.id }
                TractorCategoryCardParts(category, partCount) { onCategoryClick(category) }
            }
            if (data.generalNotesKo.isNotEmpty()) {
                item(key = "notes") {
                    TractorPartsDetailSection("일반 주의사항") {
                        data.generalNotesKo.forEach {
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("• ", fontSize = 13.sp, color = TextSecondary)
                                Text(it, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TractorCategoryCardParts(category: TractorConsumableCategory, partCount: Int, onClick: () -> Unit) {
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
private fun TractorPartListView(
    category: TractorConsumableCategory,
    parts: List<TractorConsumablePart>,
    onBack: () -> Unit,
    onPartClick: (TractorConsumablePart) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorPartsTopBar(category.nameKo, "${parts.size}개 부품", onBack)
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
                    TractorPartRow(part) { onPartClick(part) }
                }
            }
        }
    }
}

@Composable
private fun TractorPartRow(part: TractorConsumablePart, onClick: () -> Unit) {
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
            part.partNumber?.let {
                Text(it, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(8.dp))
            }
            part.appliesTo?.takeIf { it != "all" }?.let { TractorPartsSpecBadge(it) }
            Spacer(Modifier.weight(1f))
            part.pageRef?.let { Text("p.${it}", fontSize = 11.sp, color = TextTertiary) }
        }
        Spacer(Modifier.height(6.dp))
        Text(part.nameKo, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        part.specKo?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

// ====================== Detail ======================

@Composable
private fun TractorPartDetailView(
    part: TractorConsumablePart,
    category: TractorConsumableCategory,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorPartsTopBar(part.id, category.nameKo, onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TractorPartsDetailSection("부품 정보") {
                Text(part.nameKo, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                part.nameJa?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("原: $it", fontSize = 12.sp, color = TextTertiary)
                }
                part.partNumber?.let {
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Text("부품번호 ", fontSize = 13.sp, color = TextSecondary)
                        Text(it, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                    }
                }
                part.specKo?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, fontSize = 13.sp, color = TextSecondary)
                }
            }

            part.appliesTo?.let { applies ->
                TractorPartsDetailSection("적용 조건") {
                    if (applies == "all") {
                        Text("전체 모델 공통", fontSize = 13.sp, color = TextPrimary)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TractorPartsSpecBadge(applies)
                        }
                    }
                }
            }

            TractorPartsDetailSection("출처") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.MenuBook, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "쿠보타 트랙터 매뉴얼" + (part.pageRef?.let { " p.${it}" } ?: ""),
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
private fun TractorPartsDetailSection(title: String, content: @Composable () -> Unit) {
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
private fun TractorPartsSpecBadge(text: String) {
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
private fun TractorPartsTopBar(title: String, subtitle: String?, onBack: () -> Unit) {
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

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
import androidx.compose.material.icons.outlined.Build
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
import com.example.farmmachinemanager.data.manual.Remedy
import com.example.farmmachinemanager.data.manual.TroubleshootingCase
import com.example.farmmachinemanager.data.manual.TroubleshootingCategory
import com.example.farmmachinemanager.data.manual.TroubleshootingData
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
 * 쿠보타 이앙기 트러블슈팅 화면.
 *
 * 3 단계 탐색을 한 화면 안의 sealed state 로 처리한다:
 *   1) 카테고리 리스트 (7개)
 *   2) 카테고리 안의 케이스 리스트
 *   3) 케이스 상세 (원인 · 증상 · 점검 포인트 · 처치 · 매뉴얼 페이지)
 *
 * 시스템/네비게이션 백 → 이전 단계 또는 onBack 호출.
 */
private sealed interface TsScreen {
    data object Categories : TsScreen
    data class Cases(val category: TroubleshootingCategory) : TsScreen
    data class Detail(val case: TroubleshootingCase, val category: TroubleshootingCategory) : TsScreen
}

@Composable
fun TroubleshootingScreen(onBack: () -> Unit) {
    var data by remember { mutableStateOf<TroubleshootingData?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            data = AppContainer.manualRepository.loadTroubleshooting()
        } catch (t: Throwable) {
            loadError = t.message ?: "데이터를 불러올 수 없습니다"
        }
    }

    var screen: TsScreen by remember { mutableStateOf(TsScreen.Categories) }

    BackHandler {
        screen = when (val s = screen) {
            is TsScreen.Categories -> {
                onBack()
                s
            }
            is TsScreen.Cases -> TsScreen.Categories
            is TsScreen.Detail -> TsScreen.Cases(s.category)
        }
    }

    val current = data
    when {
        loadError != null -> LoadErrorView(message = loadError!!, onBack = onBack)
        current == null -> LoadingView(onBack = onBack)
        else -> when (val s = screen) {
            is TsScreen.Categories -> CategoryListView(
                data = current,
                onBack = onBack,
                onCategoryClick = { screen = TsScreen.Cases(it) }
            )
            is TsScreen.Cases -> CaseListView(
                category = s.category,
                cases = current.cases.filter { it.categoryId == s.category.id },
                onBack = { screen = TsScreen.Categories },
                onCaseClick = { screen = TsScreen.Detail(it, s.category) }
            )
            is TsScreen.Detail -> CaseDetailView(
                case = s.case,
                category = s.category,
                onBack = { screen = TsScreen.Cases(s.category) }
            )
        }
    }
}

// ====================== Loading / Error ======================

@Composable
private fun LoadingView(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TsTopBar(title = "트러블슈팅", onBack = onBack)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun LoadErrorView(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TsTopBar(title = "트러블슈팅", onBack = onBack)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("매뉴얼 데이터를 불러올 수 없습니다", fontSize = 16.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                Text(message, fontSize = 13.sp, color = TextTertiary)
            }
        }
    }
}

// ====================== Categories ======================

@Composable
private fun CategoryListView(
    data: TroubleshootingData,
    onBack: () -> Unit,
    onCategoryClick: (TroubleshootingCategory) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TsTopBar(title = "트러블슈팅", subtitle = data.machine.seriesNameKo, onBack = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(data.categories, key = { it.id }) { category ->
                val caseCount = data.cases.count { it.categoryId == category.id }
                CategoryCard(category = category, caseCount = caseCount, onClick = { onCategoryClick(category) })
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: TroubleshootingCategory,
    caseCount: Int,
    onClick: () -> Unit,
) {
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
                .background(StatusInspectionBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Build,
                contentDescription = null,
                tint = StatusInspectionText,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.nameKo,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            category.descriptionKo?.let {
                Spacer(Modifier.height(2.dp))
                Text(text = it, fontSize = 12.sp, color = TextSecondary)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${caseCount}개 케이스", fontSize = 12.sp, color = TextTertiary)
                category.specRequired?.let {
                    Spacer(Modifier.width(8.dp))
                    SpecBadge(text = "${it} 사양")
                }
                category.pageRef?.let {
                    Spacer(Modifier.width(8.dp))
                    Text(text = "p.${it}", fontSize = 12.sp, color = TextTertiary)
                }
            }
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = TextTertiary,
        )
    }
}

// ====================== Cases ======================

@Composable
private fun CaseListView(
    category: TroubleshootingCategory,
    cases: List<TroubleshootingCase>,
    onBack: () -> Unit,
    onCaseClick: (TroubleshootingCase) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TsTopBar(title = category.nameKo, subtitle = "${cases.size}개 사례", onBack = onBack)
        if (cases.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("등록된 케이스가 없습니다", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cases, key = { it.id }) { case ->
                    CaseRow(case = case, onClick = { onCaseClick(case) })
                }
            }
        }
    }
}

@Composable
private fun CaseRow(case: TroubleshootingCase, onClick: () -> Unit) {
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
            Text(text = case.id, fontSize = 11.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(8.dp))
            case.appliesToSpec?.let { SpecBadge(text = "${it} 사양") }
            Spacer(Modifier.weight(1f))
            case.pageRef?.let { Text(text = "p.${it}", fontSize = 11.sp, color = TextTertiary) }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = case.causeKo,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
        )
        if (case.symptomsKo.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = case.symptomsKo.first(),
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 2,
            )
        }
    }
}

// ====================== Detail ======================

@Composable
private fun CaseDetailView(
    case: TroubleshootingCase,
    category: TroubleshootingCategory,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TsTopBar(title = case.id, subtitle = category.nameKo, onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DetailSection(title = "원인") {
                Text(case.causeKo, fontSize = 14.sp, color = TextPrimary)
                case.causeJa?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("原: $it", fontSize = 12.sp, color = TextTertiary)
                }
            }
            if (case.symptomsKo.isNotEmpty()) {
                DetailSection(title = "증상") {
                    case.symptomsKo.forEach { sym ->
                        BulletLine(text = sym)
                    }
                }
            }
            if (case.checkPointsKo.isNotEmpty()) {
                DetailSection(title = "점검 포인트") {
                    case.checkPointsKo.forEach { pt ->
                        BulletLine(text = pt)
                    }
                }
            }
            if (case.remedies.isNotEmpty()) {
                DetailSection(title = "처치 방법 (${case.remedies.size})") {
                    case.remedies.forEachIndexed { idx, remedy ->
                        if (idx > 0) Spacer(Modifier.height(10.dp))
                        RemedyCard(remedy = remedy)
                    }
                }
            }
            DetailSection(title = "출처") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.MenuBook,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "쿠보타 매뉴얼 PW600-9751-4" + (case.pageRef?.let { " p.${it}" } ?: ""),
                        fontSize = 13.sp,
                        color = TextSecondary,
                    )
                }
                case.appliesToSpec?.let {
                    Spacer(Modifier.height(6.dp))
                    SpecBadge(text = "${it} 사양 전용")
                }
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun BulletLine(text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(text = "•  ", fontSize = 14.sp, color = TextSecondary)
        Text(text = text, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun RemedyCard(remedy: Remedy) {
    val (bg, fg) = remedyColor(remedy.type)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceSecondary)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(bg)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(text = remedy.typeKo, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = fg)
            }
            remedy.manualPageRef?.let {
                Spacer(Modifier.weight(1f))
                Text(text = "p.${it}", fontSize = 11.sp, color = TextTertiary)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(text = remedy.actionKo, fontSize = 14.sp, color = TextPrimary)
    }
}

private fun remedyColor(type: String): Pair<Color, Color> = when (type) {
    "machine" -> StatusNormalBg to StatusNormalText
    "seedling" -> StatusInspectionBg to StatusInspectionText
    "field" -> StatusInspectionBg to StatusInspectionText
    "fertilizer" -> StatusNormalBg to StatusNormalText
    "service" -> StatusRepairBg to StatusRepairText
    "operation" -> StatusNormalBg to StatusNormalText
    else -> StatusInspectionBg to StatusInspectionText
}

// ====================== Common ======================

@Composable
private fun SpecBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(StatusRepairBg)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(text = text, fontSize = 11.sp, color = StatusRepairText, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TsTopBar(title: String, subtitle: String? = null, onBack: () -> Unit) {
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
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "뒤로",
                tint = TextPrimary,
            )
        }
        Spacer(Modifier.width(4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            subtitle?.let {
                Text(text = it, fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}

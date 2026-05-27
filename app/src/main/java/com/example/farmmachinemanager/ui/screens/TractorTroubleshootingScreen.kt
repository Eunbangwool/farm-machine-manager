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
import androidx.compose.material.icons.outlined.Warning
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
import com.example.farmmachinemanager.data.manual.TractorErrorCodes
import com.example.farmmachinemanager.data.manual.TractorRemedy
import com.example.farmmachinemanager.data.manual.TractorTroubleshootingCase
import com.example.farmmachinemanager.data.manual.TractorTroubleshootingCategory
import com.example.farmmachinemanager.data.manual.TractorTroubleshootingData
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
 * 쿠보타 트랙터(MR1050) 트러블슈팅 화면.
 *
 * 4 단계 탐색을 한 화면 안의 sealed state 로 처리한다:
 *   1) 카테고리 리스트 + 전자제어 에러 코드 진입 카드
 *   2) 카테고리 안의 케이스 리스트
 *   3) 케이스 상세 (원인 · 증상 · 처치 · 긴급 대응 · 매뉴얼 페이지)
 *   4) 에러 코드 (중요 카테고리 + SCR 에러 코드) 상세
 *
 * 시스템/네비게이션 백 → 이전 단계 또는 onBack 호출.
 */
private sealed interface TractorTsScreen {
    data object Categories : TractorTsScreen
    data class Cases(val category: TractorTroubleshootingCategory) : TractorTsScreen
    data class Detail(val case: TractorTroubleshootingCase, val category: TractorTroubleshootingCategory) : TractorTsScreen
    data class ErrorCodes(val errorCodes: TractorErrorCodes) : TractorTsScreen
}

@Composable
fun TractorTroubleshootingScreen(onBack: () -> Unit) {
    var data by remember { mutableStateOf<TractorTroubleshootingData?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            data = AppContainer.manualRepository.loadTractorTroubleshooting()
        } catch (t: Throwable) {
            loadError = t.message ?: "데이터를 불러올 수 없습니다"
        }
    }

    var screen: TractorTsScreen by remember { mutableStateOf(TractorTsScreen.Categories) }

    BackHandler {
        screen = when (val s = screen) {
            is TractorTsScreen.Categories -> {
                onBack()
                s
            }
            is TractorTsScreen.Cases -> TractorTsScreen.Categories
            is TractorTsScreen.Detail -> TractorTsScreen.Cases(s.category)
            is TractorTsScreen.ErrorCodes -> TractorTsScreen.Categories
        }
    }

    val current = data
    when {
        loadError != null -> TractorTsErrorView(message = loadError!!, onBack = onBack)
        current == null -> TractorTsLoadingView(onBack = onBack)
        else -> when (val s = screen) {
            is TractorTsScreen.Categories -> TractorCategoryListView(
                data = current,
                onBack = onBack,
                onCategoryClick = { screen = TractorTsScreen.Cases(it) },
                onErrorCodesClick = { current.errorCodes?.let { ec -> screen = TractorTsScreen.ErrorCodes(ec) } }
            )
            is TractorTsScreen.Cases -> TractorCaseListView(
                category = s.category,
                cases = current.cases.filter { it.categoryId == s.category.id },
                onBack = { screen = TractorTsScreen.Categories },
                onCaseClick = { screen = TractorTsScreen.Detail(it, s.category) }
            )
            is TractorTsScreen.Detail -> TractorCaseDetailView(
                case = s.case,
                category = s.category,
                onBack = { screen = TractorTsScreen.Cases(s.category) }
            )
            is TractorTsScreen.ErrorCodes -> TractorErrorCodesView(
                errorCodes = s.errorCodes,
                onBack = { screen = TractorTsScreen.Categories }
            )
        }
    }
}

// ====================== Loading / Error ======================

@Composable
private fun TractorTsLoadingView(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorTsTopBar(title = "쿠보타 트랙터 트러블슈팅", onBack = onBack)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun TractorTsErrorView(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorTsTopBar(title = "쿠보타 트랙터 트러블슈팅", onBack = onBack)
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
private fun TractorCategoryListView(
    data: TractorTroubleshootingData,
    onBack: () -> Unit,
    onCategoryClick: (TractorTroubleshootingCategory) -> Unit,
    onErrorCodesClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorTsTopBar(title = "쿠보타 트랙터 트러블슈팅", subtitle = data.machine.seriesNameKo, onBack = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(data.categories, key = { it.id }) { category ->
                val caseCount = data.cases.count { it.categoryId == category.id }
                TractorCategoryCard(category = category, caseCount = caseCount, onClick = { onCategoryClick(category) })
            }
            data.errorCodes?.let { ec ->
                item(key = "error-codes") {
                    TractorErrorCodesEntryCard(errorCodes = ec, onClick = onErrorCodesClick)
                }
            }
            if (data.scheduledMaintenanceReminderKo != null || data.generalNotesKo.isNotEmpty()) {
                item(key = "ts-notes") {
                    TractorTsDetailSection(title = "일반 주의사항") {
                        data.scheduledMaintenanceReminderKo?.let {
                            Text(it, fontSize = 13.sp, color = TextPrimary)
                            if (data.generalNotesKo.isNotEmpty()) Spacer(Modifier.height(8.dp))
                        }
                        data.generalNotesKo.forEach { TractorTsBulletLine(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TractorCategoryCard(
    category: TractorTroubleshootingCategory,
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

@Composable
private fun TractorErrorCodesEntryCard(errorCodes: TractorErrorCodes, onClick: () -> Unit) {
    val scrCount = errorCodes.scrErrorCodes?.exampleCodes?.size ?: 0
    val catCount = errorCodes.importantCategories.size
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
                .background(StatusRepairBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = null,
                tint = StatusRepairText,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "전자제어 에러 코드",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            errorCodes.infoKo?.let {
                Spacer(Modifier.height(2.dp))
                Text(text = it, fontSize = 12.sp, color = TextSecondary, maxLines = 2)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "중요 항목 ${catCount}개 · SCR ${scrCount}개", fontSize = 12.sp, color = TextTertiary)
                errorCodes.pageRef?.let {
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
private fun TractorCaseListView(
    category: TractorTroubleshootingCategory,
    cases: List<TractorTroubleshootingCase>,
    onBack: () -> Unit,
    onCaseClick: (TractorTroubleshootingCase) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorTsTopBar(title = category.nameKo, subtitle = "${cases.size}개 사례", onBack = onBack)
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
                    TractorCaseRow(case = case, onClick = { onCaseClick(case) })
                }
            }
        }
    }
}

@Composable
private fun TractorCaseRow(case: TractorTroubleshootingCase, onClick: () -> Unit) {
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
            if (case.serviceRequired) {
                Spacer(Modifier.width(8.dp))
                TractorTsServiceBadge()
            }
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
private fun TractorCaseDetailView(
    case: TractorTroubleshootingCase,
    category: TractorTroubleshootingCategory,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorTsTopBar(title = case.id, subtitle = category.nameKo, onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TractorTsDetailSection(title = "원인") {
                Text(case.causeKo, fontSize = 14.sp, color = TextPrimary)
                case.causeJa?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("原: $it", fontSize = 12.sp, color = TextTertiary)
                }
            }
            if (case.symptomsKo.isNotEmpty()) {
                TractorTsDetailSection(title = "증상") {
                    case.symptomsKo.forEach { sym ->
                        TractorTsBulletLine(text = sym)
                    }
                }
            }
            if (case.remedies.isNotEmpty()) {
                TractorTsDetailSection(title = "처치 방법 (${case.remedies.size})") {
                    case.remedies.forEachIndexed { idx, remedy ->
                        if (idx > 0) Spacer(Modifier.height(10.dp))
                        TractorRemedyCard(remedy = remedy)
                    }
                }
            }
            if (case.emergencyResponseKo.isNotEmpty()) {
                TractorTsDetailSection(title = "긴급 대응") {
                    case.emergencyResponseKo.forEach { step ->
                        TractorTsBulletLine(text = step)
                    }
                }
            }
            if (case.serviceRequired) {
                TractorTsDetailSection(title = "적용 조건") {
                    Text(
                        "구입처(서비스센터) 연락이 필요한 작업입니다",
                        fontSize = 13.sp,
                        color = StatusRepairText,
                    )
                }
            }
            TractorTsDetailSection(title = "출처") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.MenuBook,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "쿠보타 트랙터 매뉴얼" + (case.pageRef?.let { " p.${it}" } ?: ""),
                        fontSize = 13.sp,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}

// ====================== Error Codes ======================

@Composable
private fun TractorErrorCodesView(
    errorCodes: TractorErrorCodes,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorTsTopBar(title = "전자제어 에러 코드", subtitle = "중요 항목 · SCR 코드", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            errorCodes.infoKo?.let { info ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(StatusInspectionBg)
                        .padding(14.dp)
                ) {
                    Icon(Icons.Outlined.Warning, null, tint = StatusInspectionText, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(info, fontSize = 13.sp, color = StatusInspectionText)
                }
            }

            if (errorCodes.importantCategories.isNotEmpty()) {
                TractorTsDetailSection(title = "중요 에러 카테고리 (${errorCodes.importantCategories.size})") {
                    errorCodes.importantCategories.forEachIndexed { idx, cat ->
                        if (idx > 0) {
                            Spacer(Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(BorderColor)
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                        Text(cat.categoryKo, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        cat.commonActionKo?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(it, fontSize = 13.sp, color = TextSecondary)
                        }
                        cat.additionalNotesKo.forEach { note ->
                            Spacer(Modifier.height(2.dp))
                            TractorTsBulletLine(text = note)
                        }
                        cat.pageRef?.let {
                            Spacer(Modifier.height(4.dp))
                            Text("p.${it}", fontSize = 11.sp, color = TextTertiary)
                        }
                    }
                }
            }

            errorCodes.scrErrorCodes?.let { scr ->
                TractorTsDetailSection(title = "SCR 에러 코드 (${scr.exampleCodes.size})") {
                    scr.infoKo?.let {
                        Text(it, fontSize = 13.sp, color = TextSecondary)
                        Spacer(Modifier.height(10.dp))
                    }
                    scr.exampleCodes.forEachIndexed { idx, code ->
                        if (idx > 0) Spacer(Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceSecondary)
                                .padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StatusRepairBg)
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                            ) {
                                Text(code.code, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusRepairText)
                            }
                            code.meaningKo?.let {
                                Spacer(Modifier.height(6.dp))
                                Text(it, fontSize = 14.sp, color = TextPrimary)
                            }
                            code.actionKo?.let {
                                Spacer(Modifier.height(4.dp))
                                Text("처치 · $it", fontSize = 13.sp, color = TextSecondary)
                            }
                            code.engineRestrictionKo?.let {
                                Spacer(Modifier.height(4.dp))
                                Text("엔진 제한 · $it", fontSize = 13.sp, color = StatusRepairText)
                            }
                        }
                    }
                }
            }

            TractorTsDetailSection(title = "출처") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.MenuBook, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "쿠보타 트랙터 매뉴얼" + (errorCodes.pageRef?.let { " p.${it}" } ?: ""),
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
private fun TractorTsDetailSection(title: String, content: @Composable () -> Unit) {
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
private fun TractorTsBulletLine(text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(text = "•  ", fontSize = 14.sp, color = TextSecondary)
        Text(text = text, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TractorRemedyCard(remedy: TractorRemedy) {
    val (bg, fg) = tractorRemedyColor(remedy.type)
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

private fun tractorRemedyColor(type: String): Pair<Color, Color> = when (type) {
    "machine" -> StatusNormalBg to StatusNormalText
    "operation" -> StatusNormalBg to StatusNormalText
    "service" -> StatusRepairBg to StatusRepairText
    else -> StatusInspectionBg to StatusInspectionText
}

@Composable
private fun TractorTsServiceBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(StatusRepairBg)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(text = "서비스 필요", fontSize = 11.sp, color = StatusRepairText, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TractorTsTopBar(title: String, subtitle: String? = null, onBack: () -> Unit) {
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

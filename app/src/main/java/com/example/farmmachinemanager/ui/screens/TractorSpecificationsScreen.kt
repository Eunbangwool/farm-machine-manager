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
import androidx.compose.material.icons.outlined.Agriculture
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
import com.example.farmmachinemanager.data.manual.TractorSpecModel
import com.example.farmmachinemanager.data.manual.TractorSpecificationsData
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
 * 쿠보타 트랙터(MR1050) 주요 제원 화면.
 *
 * 2 단계 탐색:
 *   1) 모델 카드 목록 + 공통 제원 / 사양 변형 설명 / 주의사항
 *   2) 모델 상세 (출력, 구동 방식, 치수, 중량, 엔진, 전륜, 크롤러, 서스펜션, 사양 변형)
 *
 * common_specifications · dimensions_mm · engine · crawler 는 키·값 구조가 모델마다
 * 달라 JsonElement 로 보관 → 안전 렌더링.
 */
private sealed interface TractorSpecScreen {
    data object Models : TractorSpecScreen
    data class Detail(val model: TractorSpecModel) : TractorSpecScreen
}

@Composable
fun TractorSpecificationsScreen(onBack: () -> Unit) {
    var data by remember { mutableStateOf<TractorSpecificationsData?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            data = AppContainer.manualRepository.loadTractorSpecifications()
        } catch (t: Throwable) {
            loadError = t.message ?: "데이터를 불러올 수 없습니다"
        }
    }

    var screen: TractorSpecScreen by remember { mutableStateOf(TractorSpecScreen.Models) }

    BackHandler {
        screen = when (screen) {
            is TractorSpecScreen.Models -> {
                onBack()
                TractorSpecScreen.Models
            }
            is TractorSpecScreen.Detail -> TractorSpecScreen.Models
        }
    }

    val current = data
    when {
        loadError != null -> TractorSpecErrorView(loadError!!, onBack)
        current == null -> TractorSpecLoadingView(onBack)
        else -> when (val s = screen) {
            is TractorSpecScreen.Models -> TractorModelListView(
                data = current,
                onBack = onBack,
                onModelClick = { screen = TractorSpecScreen.Detail(it) }
            )
            is TractorSpecScreen.Detail -> TractorModelDetailView(
                model = s.model,
                data = current,
                onBack = { screen = TractorSpecScreen.Models }
            )
        }
    }
}

// ====================== Loading / Error ======================

@Composable
private fun TractorSpecLoadingView(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorSpecTopBar("쿠보타 트랙터 주요 제원", null, onBack)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun TractorSpecErrorView(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorSpecTopBar("쿠보타 트랙터 주요 제원", null, onBack)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("제원 데이터를 불러올 수 없습니다", fontSize = 16.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                Text(message, fontSize = 13.sp, color = TextTertiary)
            }
        }
    }
}

// ====================== Models ======================

@Composable
private fun TractorModelListView(
    data: TractorSpecificationsData,
    onBack: () -> Unit,
    onModelClick: (TractorSpecModel) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorSpecTopBar("쿠보타 트랙터 주요 제원", data.machine.seriesNameKo, onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { TractorSpecSectionLabel("${data.models.size}개 모델") }
            items(data.models, key = { it.model }) { model ->
                TractorModelCard(model) { onModelClick(model) }
            }

            if (data.commonSpecifications.isNotEmpty()) {
                item { Spacer(Modifier.height(4.dp)) }
                item { TractorSpecSectionLabel("공통 제원") }
                item {
                    TractorSpecDetailSection(title = null) {
                        data.commonSpecifications.entries.forEachIndexed { idx, (key, value) ->
                            if (idx > 0) Spacer(Modifier.height(8.dp))
                            TractorSpecElementBlock(label = prettyCommonSpecKey(key), element = value)
                        }
                    }
                }
            }

            if (data.specVariantsKo.isNotEmpty()) {
                item { Spacer(Modifier.height(4.dp)) }
                item { TractorSpecSectionLabel("사양 변형 설명") }
                item {
                    TractorSpecDetailSection(title = null) {
                        data.specVariantsKo.entries.forEach { (code, desc) ->
                            TractorSpecVariantRow(code = code, desc = desc)
                        }
                    }
                }
            }

            if (data.notesKo.isNotEmpty()) {
                item { Spacer(Modifier.height(4.dp)) }
                item { TractorSpecSectionLabel("주의사항") }
                item {
                    TractorSpecDetailSection(title = null) {
                        data.notesKo.forEach { TractorSpecBulletText(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TractorModelCard(model: TractorSpecModel, onClick: () -> Unit) {
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
                .size(44.dp)
                .clip(CircleShape)
                .background(StatusNormalBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Agriculture,
                null,
                tint = StatusNormalText,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(model.model, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                model.horsepowerPs?.let {
                    Spacer(Modifier.width(8.dp))
                    TractorHpBadge(it)
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(model.nameKo, fontSize = 13.sp, color = TextSecondary)
            model.specKo?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, fontSize = 12.sp, color = TextTertiary, maxLines = 2)
            }
            model.driveTypeKo?.let {
                Spacer(Modifier.height(4.dp))
                Text("구동 · $it", fontSize = 12.sp, color = TextSecondary)
            }
        }
        Icon(Icons.Filled.ChevronRight, null, tint = TextTertiary)
    }
}

// ====================== Detail ======================

@Composable
private fun TractorModelDetailView(
    model: TractorSpecModel,
    data: TractorSpecificationsData,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorSpecTopBar(model.model, model.nameKo, onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TractorSpecDetailSection(title = "기본 정보") {
                model.specKo?.let {
                    Text(it, fontSize = 14.sp, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                }
                model.horsepowerPs?.let { TractorSpecLabelLine("출력", it) }
                model.driveTypeKo?.let { TractorSpecLabelLine("구동 방식", it) }
                renderPrimitiveElement(model.massKg)?.let { TractorSpecLabelLine("기체 중량 (kg)", it) }
                model.frontTireKo?.let { TractorSpecLabelLine("전륜 타이어", it) }
                model.suspensionKo?.let { TractorSpecLabelLine("서스펜션", it) }
            }

            tractorMapToRows(model.dimensionsMm).takeIf { it.isNotEmpty() }?.let { dims ->
                TractorSpecDetailSection(title = "치수") {
                    dims.forEachIndexed { idx, (key, value) ->
                        if (idx > 0) Spacer(Modifier.height(6.dp))
                        TractorSpecLabelLine(prettyTractorDimensionLabel(key), value)
                    }
                }
            }

            tractorMapToRows(model.engine).takeIf { it.isNotEmpty() }?.let { engine ->
                TractorSpecDetailSection(title = "엔진") {
                    engine.forEachIndexed { idx, (key, value) ->
                        if (idx > 0) Spacer(Modifier.height(6.dp))
                        TractorSpecLabelLine(prettyTractorEngineLabel(key), value)
                    }
                }
            }

            tractorMapToRows(model.crawler).takeIf { it.isNotEmpty() }?.let { crawler ->
                TractorSpecDetailSection(title = "크롤러") {
                    crawler.forEachIndexed { idx, (key, value) ->
                        if (idx > 0) Spacer(Modifier.height(6.dp))
                        TractorSpecLabelLine(key, value)
                    }
                }
            }

            if (model.specVariantsList.isNotEmpty()) {
                TractorSpecDetailSection(title = "사양 변형") {
                    model.specVariantsList.forEach { TractorSpecBulletText(it) }
                }
            }

            TractorSpecDetailSection(title = "출처") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.MenuBook, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "쿠보타 트랙터 매뉴얼 ${data.machine.manualRef ?: ""}".trim(),
                        fontSize = 13.sp,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}

// ====================== JsonElement helpers ======================

/** Map<String, JsonElement> → 표시용 (key, value-string) 목록. 중첩 객체/배열은 평탄화. */
private fun tractorMapToRows(map: Map<String, JsonElement>): List<Pair<String, String>> {
    val rows = mutableListOf<Pair<String, String>>()
    map.forEach { (key, value) ->
        when (value) {
            is JsonPrimitive -> rows.add(key to (value.contentOrNull ?: value.toString()))
            is JsonArray -> {
                val joined = value.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }.joinToString(" / ")
                if (joined.isNotBlank()) rows.add(key to joined)
            }
            is JsonObject -> {
                value.forEach { (subKey, subVal) ->
                    (subVal as? JsonPrimitive)?.contentOrNull?.let {
                        rows.add("$key · $subKey" to it)
                    }
                }
            }
        }
    }
    return rows
}

/** primitive 값을 문자열로. 아니면 null. */
private fun renderPrimitiveElement(element: JsonElement?): String? =
    (element as? JsonPrimitive)?.contentOrNull

@Composable
private fun TractorSpecElementBlock(label: String, element: JsonElement) {
    when (element) {
        is JsonPrimitive -> TractorSpecLabelLine(label, element.contentOrNull ?: element.toString())
        is JsonArray -> {
            val joined = element.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }.joinToString(" / ")
            TractorSpecLabelLine(label, joined)
        }
        is JsonObject -> {
            Text(label, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            element.forEach { (k, v) ->
                (v as? JsonPrimitive)?.contentOrNull?.let {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
                        Text(k, fontSize = 12.sp, color = TextTertiary, modifier = Modifier.width(120.dp))
                        Text(it, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private fun prettyCommonSpecKey(key: String): String = when (key) {
    "drive_type_ko" -> "구동 방식"
    "engine" -> "엔진"
    "steering_ko" -> "스티어링"
    "transmission" -> "변속기"
    "brake_ko" -> "브레이크"
    "differential_ko" -> "차동장치"
    "clutch_ko" -> "클러치"
    "speed_range_ko" -> "속도 범위"
    "pto" -> "PTO"
    "implement_lift_ko" -> "작업기 승강"
    else -> key.removeSuffix("_ko")
}

private fun prettyTractorDimensionLabel(key: String): String = when (key) {
    "length" -> "전장"
    "width_standard" -> "전폭(표준)"
    "height" -> "전고"
    "wheelbase" -> "축거"
    "ground_clearance" -> "최저지상고"
    else -> key
}

private fun prettyTractorEngineLabel(key: String): String = when (key) {
    "model" -> "엔진 모델"
    "type_ko" -> "형식"
    "displacement_cc" -> "배기량 (cc)"
    "displacement_L" -> "배기량 (L)"
    "power_kw" -> "출력 (kW)"
    "power_ps" -> "출력 (PS)"
    "rpm" -> "정격 회전수"
    "tank_capacity_L" -> "연료 탱크 (L)"
    "cylinders" -> "기통 수"
    else -> key
}

// ====================== Common ======================

@Composable
private fun TractorSpecSectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = TextSecondary,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 2.dp),
    )
}

@Composable
private fun TractorSpecDetailSection(title: String?, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        if (title != null) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Spacer(Modifier.height(10.dp))
        }
        content()
    }
}

@Composable
private fun TractorSpecLabelLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 12.sp, color = TextTertiary, modifier = Modifier.width(140.dp))
        Text(value, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TractorSpecBulletText(text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("• ", fontSize = 13.sp, color = TextSecondary)
        Text(text, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TractorSpecVariantRow(code: String, desc: String) {
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

@Composable
private fun TractorHpBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(StatusInspectionBg)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(text, fontSize = 11.sp, color = StatusInspectionText, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TractorSpecTopBar(title: String, subtitle: String?, onBack: () -> Unit) {
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

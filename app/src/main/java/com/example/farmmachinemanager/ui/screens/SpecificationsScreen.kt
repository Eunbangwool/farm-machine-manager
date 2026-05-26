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
import com.example.farmmachinemanager.data.manual.ModelSpecAccess
import com.example.farmmachinemanager.data.manual.SpecificationsData
import com.example.farmmachinemanager.data.manual.prettyDimensionLabel
import com.example.farmmachinemanager.data.manual.prettyWheelLabel
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
import kotlinx.serialization.json.JsonObject

/**
 * 쿠보타 이앙기 주요 제원 화면.
 *
 * 2 단계 탐색:
 *   1) 6개 모델 카드 + 사양 변형 설명 / 공통 경고 장치 / 주의사항
 *   2) 모델 상세 (엔진, 치수, 차륜, 시비기 호퍼, 예비모 수, 작업 속도/효율)
 */
private sealed interface SpecScreen {
    data object Models : SpecScreen
    data class Detail(val model: JsonObject) : SpecScreen
}

@Composable
fun SpecificationsScreen(onBack: () -> Unit) {
    var data by remember { mutableStateOf<SpecificationsData?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            data = AppContainer.manualRepository.loadSpecifications()
        } catch (t: Throwable) {
            loadError = t.message ?: "데이터를 불러올 수 없습니다"
        }
    }

    var screen: SpecScreen by remember { mutableStateOf(SpecScreen.Models) }

    BackHandler {
        screen = when (screen) {
            is SpecScreen.Models -> {
                onBack()
                SpecScreen.Models
            }
            is SpecScreen.Detail -> SpecScreen.Models
        }
    }

    val current = data
    when {
        loadError != null -> SpecErrorView(loadError!!, onBack)
        current == null -> SpecLoadingView(onBack)
        else -> when (val s = screen) {
            is SpecScreen.Models -> ModelListView(
                data = current,
                onBack = onBack,
                onModelClick = { screen = SpecScreen.Detail(it) }
            )
            is SpecScreen.Detail -> ModelDetailView(
                model = s.model,
                data = current,
                onBack = { screen = SpecScreen.Models }
            )
        }
    }
}

// ====================== Loading / Error ======================

@Composable
private fun SpecLoadingView(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        SpecTopBar("주요 제원", null, onBack)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun SpecErrorView(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        SpecTopBar("주요 제원", null, onBack)
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
private fun ModelListView(
    data: SpecificationsData,
    onBack: () -> Unit,
    onModelClick: (JsonObject) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        SpecTopBar("주요 제원", data.machine.seriesNameKo, onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { SectionLabel("6개 모델") }
            items(
                items = data.models,
                key = { ModelSpecAccess.model(it) ?: it.hashCode().toString() }
            ) { model ->
                ModelCard(model) { onModelClick(model) }
            }

            if (data.specVariantsKo.isNotEmpty()) {
                item { Spacer(Modifier.height(4.dp)) }
                item { SectionLabel("사양 변형 설명") }
                item {
                    SpecDetailSection(title = null) {
                        data.specVariantsKo.entries.forEach { (code, desc) ->
                            VariantRow(code = code, desc = desc)
                        }
                    }
                }
            }

            if (data.warningDevicesCommonKo.isNotEmpty()) {
                item { Spacer(Modifier.height(4.dp)) }
                item { SectionLabel("공통 경고 장치") }
                item {
                    SpecDetailSection(title = null) {
                        data.warningDevicesCommonKo.forEach { BulletText(it) }
                    }
                }
            }

            if (data.warningDevicesSGSKo.isNotEmpty()) {
                item { SectionLabel("S·GS 사양 경고 장치") }
                item {
                    SpecDetailSection(title = null) {
                        data.warningDevicesSGSKo.forEach { BulletText(it) }
                    }
                }
            }

            if (data.notesKo.isNotEmpty()) {
                item { Spacer(Modifier.height(4.dp)) }
                item { SectionLabel("주의사항") }
                item {
                    SpecDetailSection(title = null) {
                        data.notesKo.forEach { BulletText(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelCard(model: JsonObject, onClick: () -> Unit) {
    val name = ModelSpecAccess.nameKo(model) ?: ModelSpecAccess.model(model) ?: "(이름 없음)"
    val code = ModelSpecAccess.model(model) ?: "?"
    val specs = ModelSpecAccess.specs(model)
    val rows = ModelSpecAccess.rows(model)
    val engine = ModelSpecAccess.engine(model)

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
                Text(code, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.width(8.dp))
                rows?.let { RowsBadge(it) }
            }
            Spacer(Modifier.height(2.dp))
            Text(name, fontSize = 13.sp, color = TextSecondary)
            specs?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, fontSize = 12.sp, color = TextTertiary, maxLines = 2)
            }
            engine?.powerPs?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    "엔진 ${engine.model ?: "?"} · ${it}PS",
                    fontSize = 12.sp,
                    color = TextSecondary,
                )
            }
        }
        Icon(Icons.Filled.ChevronRight, null, tint = TextTertiary)
    }
}

// ====================== Detail ======================

@Composable
private fun ModelDetailView(
    model: JsonObject,
    data: SpecificationsData,
    onBack: () -> Unit,
) {
    val code = ModelSpecAccess.model(model) ?: "?"
    val name = ModelSpecAccess.nameKo(model) ?: code

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        SpecTopBar(code, name, onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SpecDetailSection(title = "기본 정보") {
                ModelSpecAccess.specs(model)?.let {
                    Text(it, fontSize = 14.sp, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                }
                ModelSpecAccess.rows(model)?.let { LabelLine("이앙 조수", "${it}조") }
                ModelSpecAccess.massKg(model)?.let { LabelLine("기체 중량 (kg)", it) }
            }

            ModelSpecAccess.engine(model)?.let { engine ->
                SpecDetailSection(title = "엔진") {
                    engine.model?.let { LabelLine("엔진 모델", it) }
                    engine.displacementCc?.let { LabelLine("배기량", "${it}cc") }
                    engine.powerKw?.let { LabelLine("출력 (kW)", "$it") }
                    engine.powerPs?.let { LabelLine("출력 (PS)", "$it") }
                    engine.rpm?.let { LabelLine("정격 회전수", "${it}rpm") }
                    engine.tankCapacityL?.let { LabelLine("연료 탱크 (L)", "$it") }
                }
            }

            ModelSpecAccess.dimensions(model).takeIf { it.isNotEmpty() }?.let { dims ->
                SpecDetailSection(title = "치수 (mm)") {
                    dims.forEachIndexed { idx, (key, value) ->
                        if (idx > 0) Spacer(Modifier.height(6.dp))
                        LabelLine(prettyDimensionLabel(key), value)
                    }
                }
            }

            ModelSpecAccess.wheels(model).takeIf { it.isNotEmpty() }?.let { wheels ->
                SpecDetailSection(title = "차륜") {
                    wheels.forEachIndexed { idx, (key, value) ->
                        if (idx > 0) Spacer(Modifier.height(6.dp))
                        LabelLine(prettyWheelLabel(key), value)
                    }
                }
            }

            SpecDetailSection(title = "작업 능력") {
                ModelSpecAccess.fertilizerHopper(model)?.let { LabelLine("시비기 호퍼", it) }
                ModelSpecAccess.spareSeedlingCount(model)?.let { LabelLine("예비 모 적재 수", it) }
                ModelSpecAccess.workSpeed(model)?.let { LabelLine("작업 속도 (m/s)", it) }
                ModelSpecAccess.workEfficiency(model)?.let { LabelLine("작업 능률 (분/10a)", it) }
            }

            SpecDetailSection(title = "출처") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.MenuBook, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "쿠보타 매뉴얼 ${data.machine.manualRef ?: "PW600-9751-4"} p.252-255",
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
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = TextSecondary,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 2.dp),
    )
}

@Composable
private fun SpecDetailSection(title: String?, content: @Composable () -> Unit) {
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
private fun LabelLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 12.sp, color = TextTertiary, modifier = Modifier.width(140.dp))
        Text(value, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun BulletText(text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("• ", fontSize = 13.sp, color = TextSecondary)
        Text(text, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun VariantRow(code: String, desc: String) {
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
private fun RowsBadge(rows: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(StatusInspectionBg)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text("${rows}조식", fontSize = 11.sp, color = StatusInspectionText, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SpecTopBar(title: String, subtitle: String?, onBack: () -> Unit) {
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

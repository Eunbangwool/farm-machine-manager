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
import com.example.farmmachinemanager.data.manual.DpfWarningLevels
import com.example.farmmachinemanager.data.manual.OverheatResponse
import com.example.farmmachinemanager.data.manual.TractorWarningLightsData
import com.example.farmmachinemanager.data.manual.WarningLight
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
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * 쿠보타 트랙터(MR1050) 경고등 가이드 화면. 안전 직결 데이터.
 *
 * 4 단계 탐색:
 *   1) 경고등 목록 + DPF 경고 단계 / 오버히트 응급 처치 진입 카드
 *   2) 경고등 상세 (점등 조건, 거동, 심각도, 처치, 점등 시 증상, 경고)
 *   3) DPF 경고 단계 상세
 *   4) 오버히트 응급 처치 상세
 */
private sealed interface TractorWlScreen {
    data object List : TractorWlScreen
    data class Detail(val light: WarningLight) : TractorWlScreen
    data class Dpf(val dpf: DpfWarningLevels) : TractorWlScreen
    data class Overheat(val overheat: OverheatResponse) : TractorWlScreen
}

@Composable
fun TractorWarningLightsScreen(onBack: () -> Unit) {
    var data by remember { mutableStateOf<TractorWarningLightsData?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            data = AppContainer.manualRepository.loadTractorWarningLights()
        } catch (t: Throwable) {
            loadError = t.message ?: "데이터를 불러올 수 없습니다"
        }
    }

    var screen: TractorWlScreen by remember { mutableStateOf(TractorWlScreen.List) }

    BackHandler {
        screen = when (screen) {
            is TractorWlScreen.List -> {
                onBack()
                TractorWlScreen.List
            }
            else -> TractorWlScreen.List
        }
    }

    val current = data
    when {
        loadError != null -> TractorWlErrorView(loadError!!, onBack)
        current == null -> TractorWlLoadingView(onBack)
        else -> when (val s = screen) {
            is TractorWlScreen.List -> TractorWlListView(
                data = current,
                onBack = onBack,
                onLightClick = { screen = TractorWlScreen.Detail(it) },
                onDpfClick = { current.dpfWarningLevels?.let { d -> screen = TractorWlScreen.Dpf(d) } },
                onOverheatClick = { current.overheatResponseKo?.let { o -> screen = TractorWlScreen.Overheat(o) } }
            )
            is TractorWlScreen.Detail -> TractorWlDetailView(
                light = s.light,
                onBack = { screen = TractorWlScreen.List }
            )
            is TractorWlScreen.Dpf -> TractorDpfView(
                dpf = s.dpf,
                onBack = { screen = TractorWlScreen.List }
            )
            is TractorWlScreen.Overheat -> TractorOverheatView(
                overheat = s.overheat,
                onBack = { screen = TractorWlScreen.List }
            )
        }
    }
}

// ====================== Loading / Error ======================

@Composable
private fun TractorWlLoadingView(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorWlTopBar("쿠보타 트랙터 경고등 가이드", null, onBack)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun TractorWlErrorView(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorWlTopBar("쿠보타 트랙터 경고등 가이드", null, onBack)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("경고등 데이터를 불러올 수 없습니다", fontSize = 16.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                Text(message, fontSize = 13.sp, color = TextTertiary)
            }
        }
    }
}

// ====================== List ======================

@Composable
private fun TractorWlListView(
    data: TractorWarningLightsData,
    onBack: () -> Unit,
    onLightClick: (WarningLight) -> Unit,
    onDpfClick: () -> Unit,
    onOverheatClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorWlTopBar("쿠보타 트랙터 경고등 가이드", data.machine.seriesNameKo, onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            data.infoKo?.let { info ->
                item(key = "info") {
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
            }

            items(data.warningLights, key = { it.id }) { light ->
                TractorWlRow(light) { onLightClick(light) }
            }

            data.dpfWarningLevels?.let { dpf ->
                item(key = "dpf") {
                    TractorWlEntryCard(
                        title = "DPF 경고 단계",
                        subtitle = dpf.descriptionKo ?: "${dpf.levels.size}개 단계",
                        onClick = onDpfClick,
                    )
                }
            }

            data.overheatResponseKo?.let { ovh ->
                item(key = "overheat") {
                    TractorWlEntryCard(
                        title = "오버히트 응급 처치",
                        subtitle = ovh.triggerKo ?: "엔진 과열 시 대응",
                        onClick = onOverheatClick,
                    )
                }
            }

            if (data.generalNotesKo.isNotEmpty()) {
                item(key = "notes") {
                    TractorWlDetailSection("일반 주의사항") {
                        data.generalNotesKo.forEach { TractorWlBullet(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TractorWlRow(light: WarningLight, onClick: () -> Unit) {
    val (bg, fg) = severityColor(light.severityKo)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Warning, null, tint = fg, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(light.nameKo, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            light.severityKo?.let {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TractorSeverityBadge(text = it, bg = bg, fg = fg)
                    light.pageRef?.let { p ->
                        Spacer(Modifier.width(8.dp))
                        Text("p.${p}", fontSize = 12.sp, color = TextTertiary)
                    }
                }
            }
        }
        Icon(Icons.Filled.ChevronRight, null, tint = TextTertiary)
    }
}

@Composable
private fun TractorWlEntryCard(title: String, subtitle: String, onClick: () -> Unit) {
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
            Icon(Icons.Outlined.Warning, null, tint = StatusRepairText, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 12.sp, color = TextSecondary, maxLines = 2)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = TextTertiary)
    }
}

// ====================== Detail ======================

@Composable
private fun TractorWlDetailView(
    light: WarningLight,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorWlTopBar(light.nameKo, light.severityKo, onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TractorWlDetailSection("경고등 정보") {
                Text(light.nameKo, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                light.nameJa?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("原: $it", fontSize = 12.sp, color = TextTertiary)
                }
                light.severityKo?.let {
                    Spacer(Modifier.height(8.dp))
                    val (bg, fg) = severityColor(light.severityKo)
                    TractorSeverityBadge(text = it, bg = bg, fg = fg)
                }
            }

            if (light.triggerKo != null || light.behaviorKo != null) {
                TractorWlDetailSection("점등 조건 · 거동") {
                    light.triggerKo?.let { TractorWlLabelLine("점등 조건", it) }
                    light.behaviorKo?.let {
                        if (light.triggerKo != null) Spacer(Modifier.height(6.dp))
                        TractorWlLabelLine("거동", it)
                    }
                }
            }

            light.actionKo?.let { action ->
                TractorWlDetailSection("처치") {
                    Text(action, fontSize = 14.sp, color = TextPrimary)
                }
            }

            if (light.symptomsWhenLitKo.isNotEmpty()) {
                TractorWlDetailSection("점등 시 증상") {
                    light.symptomsWhenLitKo.forEach { TractorWlBullet(it) }
                }
            }

            light.warningKo?.let { warn ->
                TractorWlDetailSection("경고") {
                    Text(warn, fontSize = 13.sp, color = StatusRepairText)
                }
            }

            light.additionalNotesKo?.let { note ->
                TractorWlDetailSection("추가 정보") {
                    Text(note, fontSize = 13.sp, color = TextSecondary)
                }
            }

            TractorWlDetailSection("출처") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.MenuBook, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "쿠보타 트랙터 매뉴얼" + (light.pageRef?.let { " p.${it}" } ?: ""),
                        fontSize = 13.sp,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}

// ====================== DPF ======================

@Composable
private fun TractorDpfView(
    dpf: DpfWarningLevels,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorWlTopBar("DPF 경고 단계", "${dpf.levels.size}개 단계", onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (dpf.descriptionKo != null || dpf.autoRegenModeKo != null) {
                TractorWlDetailSection("개요") {
                    dpf.descriptionKo?.let { Text(it, fontSize = 14.sp, color = TextPrimary) }
                    dpf.autoRegenModeKo?.let {
                        if (dpf.descriptionKo != null) Spacer(Modifier.height(8.dp))
                        Text("자동 재생 모드 · $it", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }

            dpf.levels.forEach { level ->
                val levelLabel = (level.level as? JsonPrimitive)?.contentOrNull ?: level.level.toString()
                TractorWlDetailSection("레벨 $levelLabel") {
                    level.stateKo?.let { TractorWlLabelLine("상태", it) }
                    level.buzzerKo?.let {
                        Spacer(Modifier.height(6.dp))
                        TractorWlLabelLine("부저", it)
                    }
                    level.engineOutputKo?.let {
                        Spacer(Modifier.height(6.dp))
                        TractorWlLabelLine("엔진 출력", it)
                    }
                    level.actionKo?.let {
                        Spacer(Modifier.height(6.dp))
                        TractorWlLabelLine("처치", it)
                    }
                    level.warningKo?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, fontSize = 13.sp, color = StatusRepairText)
                    }
                    level.pageRef?.let {
                        Spacer(Modifier.height(6.dp))
                        Text("p.${it}", fontSize = 11.sp, color = TextTertiary)
                    }
                }
            }
        }
    }
}

// ====================== Overheat ======================

@Composable
private fun TractorOverheatView(
    overheat: OverheatResponse,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TractorWlTopBar("오버히트 응급 처치", "엔진 과열 대응", onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            overheat.triggerKo?.let { trigger ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(StatusRepairBg)
                        .padding(14.dp)
                ) {
                    Icon(Icons.Outlined.Warning, null, tint = StatusRepairText, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(trigger, fontSize = 13.sp, color = StatusRepairText)
                }
            }

            if (overheat.immediateActionKo.isNotEmpty()) {
                TractorWlDetailSection("즉시 조치") {
                    overheat.immediateActionKo.forEach { TractorWlBullet(it) }
                }
            }

            if (overheat.checkPointsKo.isNotEmpty()) {
                TractorWlDetailSection("점검 포인트") {
                    overheat.checkPointsKo.forEach { TractorWlBullet(it) }
                }
            }

            TractorWlDetailSection("출처") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.MenuBook, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "쿠보타 트랙터 매뉴얼" + (overheat.pageRef?.let { " p.${it}" } ?: ""),
                        fontSize = 13.sp,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}

// ====================== Common ======================

private fun severityColor(severityKo: String?): Pair<Color, Color> = when {
    severityKo == null -> StatusInspectionBg to StatusInspectionText
    severityKo.contains("위험") || severityKo.contains("긴급") || severityKo.contains("중대") ->
        StatusRepairBg to StatusRepairText
    severityKo.contains("주의") -> StatusInspectionBg to StatusInspectionText
    else -> StatusNormalBg to StatusNormalText
}

@Composable
private fun TractorSeverityBadge(text: String, bg: Color, fg: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(text, fontSize = 11.sp, color = fg, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TractorWlBullet(text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("• ", fontSize = 13.sp, color = TextSecondary)
        Text(text, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TractorWlLabelLine(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 14.sp, color = TextPrimary)
    }
}

@Composable
private fun TractorWlDetailSection(title: String, content: @Composable () -> Unit) {
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
private fun TractorWlTopBar(title: String, subtitle: String?, onBack: () -> Unit) {
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

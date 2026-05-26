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
import androidx.compose.material.icons.outlined.Bolt
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
import com.example.farmmachinemanager.data.manual.FuseBox
import com.example.farmmachinemanager.data.manual.FuseCircuit
import com.example.farmmachinemanager.data.manual.FuseCircuitsData
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
 * 쿠보타 이앙기 퓨즈 회로 가이드 화면.
 *
 * 3 단계 탐색:
 *   1) 퓨즈 박스 목록 (3개) + 교체 절차·팁
 *   2) 박스 안의 회로 목록 (번호 · 명칭 · 용량)
 *   3) 회로 상세 (용량, 사양 적용, 예비 여부)
 */
private sealed interface FuseScreen {
    data object Boxes : FuseScreen
    data class Circuits(val box: FuseBox) : FuseScreen
    data class Detail(val circuit: FuseCircuit, val box: FuseBox) : FuseScreen
}

@Composable
fun FuseGuideScreen(onBack: () -> Unit) {
    var data by remember { mutableStateOf<FuseCircuitsData?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            data = AppContainer.manualRepository.loadFuseCircuits()
        } catch (t: Throwable) {
            loadError = t.message ?: "데이터를 불러올 수 없습니다"
        }
    }

    var screen: FuseScreen by remember { mutableStateOf(FuseScreen.Boxes) }

    BackHandler {
        screen = when (val s = screen) {
            is FuseScreen.Boxes -> {
                onBack()
                s
            }
            is FuseScreen.Circuits -> FuseScreen.Boxes
            is FuseScreen.Detail -> FuseScreen.Circuits(s.box)
        }
    }

    val current = data
    when {
        loadError != null -> FuseErrorView(loadError!!, onBack)
        current == null -> FuseLoadingView(onBack)
        else -> when (val s = screen) {
            is FuseScreen.Boxes -> BoxListView(
                data = current,
                onBack = onBack,
                onBoxClick = { screen = FuseScreen.Circuits(it) }
            )
            is FuseScreen.Circuits -> CircuitListView(
                box = s.box,
                onBack = { screen = FuseScreen.Boxes },
                onCircuitClick = { screen = FuseScreen.Detail(it, s.box) }
            )
            is FuseScreen.Detail -> CircuitDetailView(
                circuit = s.circuit,
                box = s.box,
                onBack = { screen = FuseScreen.Circuits(s.box) }
            )
        }
    }
}

// ====================== Loading / Error ======================

@Composable
private fun FuseLoadingView(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        FuseTopBar("퓨즈 가이드", null, onBack)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun FuseErrorView(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        FuseTopBar("퓨즈 가이드", null, onBack)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("퓨즈 데이터를 불러올 수 없습니다", fontSize = 16.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                Text(message, fontSize = 13.sp, color = TextTertiary)
            }
        }
    }
}

// ====================== Boxes ======================

@Composable
private fun BoxListView(
    data: FuseCircuitsData,
    onBack: () -> Unit,
    onBoxClick: (FuseBox) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        FuseTopBar("퓨즈 가이드", data.machine.seriesNameKo, onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            data.infoKo?.let { info ->
                item {
                    InfoBanner(info)
                }
            }
            items(data.boxes, key = { it.id }) { box ->
                BoxCard(box) { onBoxClick(box) }
            }
            if (data.replacementProcedureKo.isNotEmpty()) {
                item {
                    FuseDetailSection("교체 절차") {
                        data.replacementProcedureKo.forEach { step ->
                            Text(step, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }
            }
            if (data.tipsKo.isNotEmpty()) {
                item {
                    FuseDetailSection("주의사항 · 팁") {
                        data.tipsKo.forEach { tip ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("• ", fontSize = 13.sp, color = TextSecondary)
                                Text(tip, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoBanner(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StatusInspectionBg)
            .padding(14.dp)
    ) {
        Icon(Icons.Outlined.Bolt, null, tint = StatusInspectionText, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 13.sp, color = StatusInspectionText)
    }
}

@Composable
private fun BoxCard(box: FuseBox, onClick: () -> Unit) {
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
                imageVector = Icons.Outlined.Bolt,
                contentDescription = null,
                tint = StatusNormalText,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(box.nameKo, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            box.locationKo?.let {
                Spacer(Modifier.height(2.dp))
                Text(it, fontSize = 12.sp, color = TextSecondary)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${box.circuits.size}개 회로", fontSize = 12.sp, color = TextTertiary)
                box.pageRef?.let {
                    Spacer(Modifier.width(8.dp))
                    Text("p.${it}", fontSize = 12.sp, color = TextTertiary)
                }
            }
        }
        Icon(Icons.Filled.ChevronRight, null, tint = TextTertiary)
    }
}

// ====================== Circuits ======================

@Composable
private fun CircuitListView(
    box: FuseBox,
    onBack: () -> Unit,
    onCircuitClick: (FuseCircuit) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        FuseTopBar(box.nameKo, box.locationKo, onBack)
        if (box.circuits.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("회로가 없습니다", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                box.descriptionKo?.let { desc ->
                    item {
                        Text(desc, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 4.dp))
                    }
                }
                items(box.circuits, key = { it.number }) { circuit ->
                    CircuitRow(circuit) { onCircuitClick(circuit) }
                }
            }
        }
    }
}

@Composable
private fun CircuitRow(circuit: FuseCircuit, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfacePrimary)
            .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (circuit.isSpare) SurfaceSecondary else StatusNormalBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "${circuit.number}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (circuit.isSpare) TextTertiary else StatusNormalText,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                circuit.circuitKo,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (circuit.isSpare) TextTertiary else TextPrimary,
            )
            if (circuit.appliesToSpec != null || circuit.isSpare) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (circuit.isSpare) {
                        SpareBadge()
                        Spacer(Modifier.width(6.dp))
                    }
                    circuit.appliesToSpec?.let { FuseSpecBadge("${it} 사양") }
                }
            }
        }
        AmpBadge(circuit.capacityAmp)
    }
}

// ====================== Detail ======================

@Composable
private fun CircuitDetailView(
    circuit: FuseCircuit,
    box: FuseBox,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        FuseTopBar("${box.nameKo} · ${circuit.number}", box.locationKo, onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FuseDetailSection("회로 정보") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(StatusNormalBg),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("${circuit.number}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StatusNormalText)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(circuit.circuitKo, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        circuit.circuitJa?.let {
                            Spacer(Modifier.height(2.dp))
                            Text("原: $it", fontSize = 12.sp, color = TextTertiary)
                        }
                    }
                    AmpBadge(circuit.capacityAmp)
                }
            }

            if (circuit.appliesToSpec != null || circuit.isSpare) {
                FuseDetailSection("적용 조건") {
                    if (circuit.isSpare) {
                        SpareBadge()
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "예비 퓨즈입니다. 같은 용량의 회로 퓨즈가 끊어졌을 때 교환에 사용합니다.",
                            fontSize = 13.sp,
                            color = TextPrimary,
                        )
                    }
                    circuit.appliesToSpec?.let {
                        if (circuit.isSpare) Spacer(Modifier.height(6.dp))
                        FuseSpecBadge("${it} 사양 전용")
                    }
                }
            }

            FuseDetailSection("위치") {
                box.locationKo?.let {
                    Text(it, fontSize = 13.sp, color = TextPrimary)
                }
                Spacer(Modifier.height(4.dp))
                Text(box.nameKo, fontSize = 12.sp, color = TextSecondary)
            }

            FuseDetailSection("출처") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.MenuBook, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "쿠보타 매뉴얼 PW600-9751-4" + (box.pageRef?.let { " p.${it}" } ?: ""),
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
private fun AmpBadge(amp: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(StatusInspectionBg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text("${amp}A", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusInspectionText)
    }
}

@Composable
private fun FuseSpecBadge(text: String) {
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
private fun SpareBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SurfaceSecondary)
            .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text("예비", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FuseDetailSection(title: String, content: @Composable () -> Unit) {
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
private fun FuseTopBar(title: String, subtitle: String?, onBack: () -> Unit) {
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

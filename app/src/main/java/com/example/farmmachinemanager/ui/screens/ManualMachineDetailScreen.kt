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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmmachinemanager.data.manual.MachineCatalogEntry
import com.example.farmmachinemanager.data.manual.ManualMachineCatalog
import com.example.farmmachinemanager.ui.theme.BorderColor
import com.example.farmmachinemanager.ui.theme.SurfacePrimary
import com.example.farmmachinemanager.ui.theme.SurfaceSecondary
import com.example.farmmachinemanager.ui.theme.TextPrimary
import com.example.farmmachinemanager.ui.theme.TextSecondary
import com.example.farmmachinemanager.ui.theme.TextTertiary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * 농기계 대백과의 머신 상세 — 데이터셋별 탭으로 구성.
 *
 * 데이터셋 스키마가 머신마다 자유로워 정형 화면 없이도 의미 있는 정보를
 * 제공하기 위해 raw JsonElement 트리 표시(JsonTreeView). 일관된 UX 로 9개
 * 머신 모두 동일 패턴.
 */
@Composable
fun ManualMachineDetailScreen(
    machineId: String,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    var entry by remember { mutableStateOf<MachineCatalogEntry?>(null) }
    LaunchedEffect(machineId) {
        entry = ManualMachineCatalog.byId(context, machineId)
    }
    val e = entry

    Column(modifier = Modifier.fillMaxSize().background(SurfaceSecondary)) {
        TopBar(
            title = e?.modelKo ?: machineId,
            subtitle = e?.let { "${it.manufacturerKo} · ${it.categoryKo}" } ?: "불러오는 중",
            onBack = onBack,
        )
        if (e == null) {
            Hint("불러오는 중…")
            return@Column
        }

        // 탭 순서: 정기점검 → 급유 → 트러블슈팅 → 경고등 → 소모품 → 제원 → 인덱스
        val tabOrder = listOf(
            "inspection_schedule" to "정기점검",
            "lubrication_schedule" to "급유",
            "troubleshooting" to "트러블슈팅",
            "warning_lights" to "경고등",
            "consumables" to "소모품",
            "specifications" to "제원",
            "fuse_circuits" to "퓨즈",
            "index" to "인덱스",
        )
        val availableTabs = tabOrder.filter { (ds, _) -> ds in e.datasets }
        if (availableTabs.isEmpty()) {
            Hint("이 모델에는 등록된 데이터셋이 없습니다")
            return@Column
        }
        var selected by remember(machineId) { mutableStateOf(0) }
        ScrollableTabRow(
            selectedTabIndex = selected.coerceAtMost(availableTabs.lastIndex),
            containerColor = SurfacePrimary,
            contentColor = TextPrimary,
            edgePadding = 12.dp,
        ) {
            availableTabs.forEachIndexed { i, (_, label) ->
                Tab(
                    selected = i == selected,
                    onClick = { selected = i },
                    text = { Text(label, fontSize = 13.sp) },
                )
            }
        }

        val currentDataset = availableTabs[selected.coerceAtMost(availableTabs.lastIndex)].first
        DatasetPane(machineId = machineId, dataset = currentDataset, entry = e)
    }
}

@Composable
private fun DatasetPane(machineId: String, dataset: String, entry: MachineCatalogEntry) {
    val context = LocalContext.current
    var jsonRoot by remember(machineId, dataset) { mutableStateOf<JsonElement?>(null) }
    LaunchedEffect(machineId, dataset) {
        jsonRoot = withContext(Dispatchers.IO) {
            runCatching {
                val text = context.assets
                    .open("manuals/$machineId/$dataset.json")
                    .bufferedReader().use { it.readText() }
                Json { ignoreUnknownKeys = true; isLenient = true }.parseToJsonElement(text)
            }.getOrNull()
        }
    }
    val root = jsonRoot ?: run { Hint("불러오는 중…"); return }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item { HeaderCard(entry = entry, dataset = dataset) }
        item { Spacer(Modifier.height(8.dp)) }
        item { JsonTreeView(element = root) }
    }
}

@Composable
private fun TopBar(title: String, subtitle: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfacePrimary)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로", tint = TextPrimary)
        }
        Spacer(modifier = Modifier.size(8.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(subtitle, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun HeaderCard(entry: MachineCatalogEntry, dataset: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(entry.emoji, fontSize = 24.sp)
            Spacer(Modifier.size(8.dp))
            Text(
                text = datasetLabel(dataset),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
        }
        Text(
            text = "${entry.manufacturerKo} · ${entry.modelKo}",
            fontSize = 11.sp,
            color = TextSecondary,
        )
        entry.noteKo?.let { Text(it, fontSize = 11.sp, color = TextTertiary) }
    }
}

@Composable
private fun Hint(text: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, fontSize = 13.sp, color = TextSecondary)
    }
}

private fun datasetLabel(name: String): String = when (name) {
    "index" -> "전체 인덱스"
    "inspection_schedule" -> "정기점검 일람"
    "lubrication_schedule" -> "급유·주유 일람"
    "consumables" -> "소모품 부품"
    "specifications" -> "주요 제원"
    "troubleshooting" -> "트러블슈팅"
    "warning_lights" -> "경고등 가이드"
    "fuse_circuits" -> "퓨즈 가이드"
    else -> name
}

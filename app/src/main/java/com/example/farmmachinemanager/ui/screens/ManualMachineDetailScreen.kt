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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

/**
 * 농기계 대백과의 머신 상세 (단순 — PR-E 에서 탭으로 확장 예정).
 * 현재는 메타데이터 + 보유 데이터셋 목록 표시.
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

    Column(modifier = Modifier.fillMaxSize().background(SurfaceSecondary)) {
        TopBar(
            title = entry?.modelKo ?: machineId,
            subtitle = entry?.let { "${it.manufacturerKo} · ${it.categoryKo}" } ?: "불러오는 중",
            onBack = onBack,
        )
        val e = entry ?: return@Column

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                InfoCard(entry = e)
            }
            item {
                SectionLabel("보유 데이터셋")
            }
            items(e.datasets) { ds ->
                DatasetRow(name = ds, available = e.has(ds))
            }
            e.noteKo?.let { note ->
                item { SectionLabel("비고") }
                item { NoteCard(note = note) }
            }
            e.compatibleKo?.let { compat ->
                item { SectionLabel("호환 모델") }
                item { NoteCard(note = compat) }
            }
        }
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
private fun InfoCard(entry: MachineCatalogEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(entry.emoji, fontSize = 30.sp)
            Spacer(Modifier.size(10.dp))
            Column {
                Text(entry.modelKo, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(
                    text = "${entry.manufacturerKo} · ${entry.categoryKo}",
                    fontSize = 12.sp,
                    color = TextSecondary,
                )
            }
        }
        entry.cropKo?.let {
            Spacer(Modifier.height(4.dp))
            Text(text = "주요 작물: $it", fontSize = 12.sp, color = TextSecondary)
        }
        Text(text = "데이터셋 ${entry.datasets.size}종 · 언어 ${entry.language}", fontSize = 11.sp, color = TextTertiary)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
    )
}

@Composable
private fun DatasetRow(name: String, available: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = datasetLabel(name),
            fontSize = 13.sp,
            color = TextPrimary,
            modifier = Modifier.padding(end = 8.dp),
        )
        Text(
            text = if (available) "보유" else "없음",
            fontSize = 11.sp,
            color = if (available) TextSecondary else TextTertiary,
        )
    }
}

@Composable
private fun NoteCard(note: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(10.dp))
            .padding(14.dp),
    ) {
        Text(text = note, fontSize = 12.sp, color = TextSecondary, lineHeight = 16.sp)
    }
}

/** "inspection_schedule" → "정기점검 일람" 같은 한글 라벨. */
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

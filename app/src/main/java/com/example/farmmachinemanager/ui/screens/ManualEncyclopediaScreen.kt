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
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
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
 * 농기계 대백과 메인 — 제조사별 그룹으로 9개 머신 카탈로그 노출.
 */
@Composable
fun ManualEncyclopediaScreen(
    onBack: () -> Unit,
    onMachineClick: (String) -> Unit,
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    var entries by remember { mutableStateOf<List<MachineCatalogEntry>>(emptyList()) }
    LaunchedEffect(Unit) {
        entries = ManualMachineCatalog.entries(context)
    }

    Column(modifier = Modifier.fillMaxSize().background(SurfaceSecondary)) {
        TopBar(title = "농기계 대백과", subtitle = "${entries.size}개 모델", onBack = onBack)
        if (entries.isEmpty()) {
            LoadingOrEmpty()
        } else {
            val grouped = entries.groupBy { it.manufacturerKo }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                grouped.entries.forEach { (manufacturer, machines) ->
                    item(key = "h_$manufacturer") {
                        ManufacturerHeader(name = manufacturer, count = machines.size)
                    }
                    items(machines, key = { it.id }) { entry ->
                        MachineCard(entry = entry, onClick = { onMachineClick(entry.id) })
                    }
                }
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
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(subtitle, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun ManufacturerHeader(name: String, count: Int) {
    Text(
        text = "$name · $count 모델",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
    )
}

@Composable
private fun MachineCard(entry: MachineCatalogEntry, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(entry.emoji, fontSize = 26.sp)
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = entry.modelKo,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
            )
            Text(
                text = "${entry.categoryKo} · 데이터셋 ${entry.datasets.size}종",
                fontSize = 11.sp,
                color = TextSecondary,
            )
            entry.noteKo?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = it, fontSize = 10.sp, color = TextTertiary)
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun LoadingOrEmpty() {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("불러오는 중…", fontSize = 13.sp, color = TextSecondary)
    }
}

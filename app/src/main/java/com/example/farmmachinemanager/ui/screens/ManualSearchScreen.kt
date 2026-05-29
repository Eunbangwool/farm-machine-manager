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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import com.example.farmmachinemanager.data.manual.ManualSearchIndex
import com.example.farmmachinemanager.ui.theme.BorderColor
import com.example.farmmachinemanager.ui.theme.SurfacePrimary
import com.example.farmmachinemanager.ui.theme.SurfaceSecondary
import com.example.farmmachinemanager.ui.theme.TextPrimary
import com.example.farmmachinemanager.ui.theme.TextSecondary
import com.example.farmmachinemanager.ui.theme.TextTertiary
import kotlinx.coroutines.delay

/**
 * 통합 검색 — 9개 머신 매뉴얼에서 모델·증상·부품번호·일반 텍스트 매칭.
 * 입력 디바운스(300ms) 후 ManualSearchIndex 호출.
 */
@Composable
fun ManualSearchScreen(
    onBack: () -> Unit,
    onMachineClick: (String) -> Unit,
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var hits by remember { mutableStateOf<List<ManualSearchIndex.Hit>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        if (query.trim().length < 2) { hits = emptyList(); return@LaunchedEffect }
        delay(300)
        searching = true
        hits = ManualSearchIndex.search(context, query.trim())
        searching = false
    }

    Column(modifier = Modifier.fillMaxSize().background(SurfaceSecondary)) {
        TopBar(onBack = onBack)
        SearchField(query = query, onChange = { query = it })

        val grouped = hits.groupBy { it.type }
        val ordered = listOf(
            ManualSearchIndex.HitType.MODEL,
            ManualSearchIndex.HitType.SYMPTOM,
            ManualSearchIndex.HitType.PART,
            ManualSearchIndex.HitType.GENERAL,
        )

        when {
            query.trim().length < 2 -> Hint("모델명·증상·부품번호 등을 2자 이상 입력하세요")
            searching && hits.isEmpty() -> Hint("검색 중…")
            hits.isEmpty() -> Hint("일치하는 항목이 없습니다")
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ordered.forEach { type ->
                    val rows = grouped[type].orEmpty()
                    if (rows.isNotEmpty()) {
                        item(key = "h_$type") { SectionLabel(label = sectionTitle(type, rows.size)) }
                        items(rows.take(50), key = { it.machineId + it.dataset + it.snippet.hashCode() }) { hit ->
                            HitCard(hit = hit, onClick = { onMachineClick(hit.machineId) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
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
        Text("매뉴얼 검색", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

@Composable
private fun SearchField(query: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onChange,
        placeholder = { Text("MR1050, 시동 안됨, BB10X-32430 …", color = TextTertiary) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = TextSecondary) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SurfacePrimary,
            unfocusedContainerColor = SurfacePrimary,
            focusedIndicatorColor = TextPrimary,
            unfocusedIndicatorColor = BorderColor,
            cursorColor = TextPrimary,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
        ),
    )
}

@Composable
private fun SectionLabel(label: String) {
    Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
    )
}

@Composable
private fun HitCard(hit: ManualSearchIndex.Hit, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = hit.snippet, fontSize = 13.sp, color = TextPrimary, lineHeight = 17.sp)
        Text(
            text = "${hit.machineLabel} · ${hit.dataset}",
            fontSize = 10.sp,
            color = TextTertiary,
        )
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

private fun sectionTitle(type: ManualSearchIndex.HitType, count: Int): String {
    val label = when (type) {
        ManualSearchIndex.HitType.MODEL -> "모델"
        ManualSearchIndex.HitType.SYMPTOM -> "증상·트러블슈팅"
        ManualSearchIndex.HitType.PART -> "부품번호"
        ManualSearchIndex.HitType.GENERAL -> "그 외"
    }
    return "$label · $count"
}

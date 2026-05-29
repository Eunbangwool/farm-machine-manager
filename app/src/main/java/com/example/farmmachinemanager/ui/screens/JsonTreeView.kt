package com.example.farmmachinemanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmmachinemanager.ui.theme.BorderColor
import com.example.farmmachinemanager.ui.theme.SurfacePrimary
import com.example.farmmachinemanager.ui.theme.TextPrimary
import com.example.farmmachinemanager.ui.theme.TextSecondary
import com.example.farmmachinemanager.ui.theme.TextTertiary
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * JsonElement 를 들여쓰기 카드 트리로 표시. 머신별 스키마가 자유로워
 * 정형 화면 없이도 의미 있는 정보를 보여주는 안전한 fallback.
 *
 * - JsonObject: 키 헤더 + 자식 들여쓰기
 * - JsonArray: 인덱스 헤더 + 자식 들여쓰기
 * - JsonPrimitive: 값 텍스트
 */
@Composable
fun JsonTreeView(element: JsonElement?, modifier: Modifier = Modifier) {
    if (element == null) return
    Column(modifier = modifier) {
        renderNode(label = null, element = element, depth = 0)
    }
}

@Composable
private fun renderNode(label: String?, element: JsonElement, depth: Int) {
    when (element) {
        is JsonObject -> {
            if (label != null) KeyLabel(label, depth)
            element.forEach { (k, v) -> renderNode(k, v, depth + (if (label == null) 0 else 1)) }
        }
        is JsonArray -> {
            if (label != null) KeyLabel("$label [${element.size}]", depth)
            element.forEachIndexed { i, child ->
                renderNode("[$i]", child, depth + (if (label == null) 0 else 1))
            }
        }
        is JsonPrimitive -> {
            val v = element.contentOrNull.orEmpty()
            if (v.isNotBlank()) Leaf(label, v, depth)
        }
        else -> {}
    }
}

@Composable
private fun KeyLabel(text: String, depth: Int) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 12).dp, top = 8.dp, bottom = 2.dp),
    )
}

@Composable
private fun Leaf(label: String?, value: String, depth: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 12).dp, end = 0.dp, top = 2.dp, bottom = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Column {
            if (!label.isNullOrBlank()) {
                Text(text = label, fontSize = 10.sp, color = TextTertiary)
            }
            Text(text = value, fontSize = 13.sp, color = TextPrimary, lineHeight = 17.sp)
        }
    }
}

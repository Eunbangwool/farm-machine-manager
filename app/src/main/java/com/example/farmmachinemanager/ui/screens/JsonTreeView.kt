package com.example.farmmachinemanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * 한국어 사용자에 맞춘 매뉴얼 JSON 뷰어.
 *
 * - 메타데이터 키($schema, version, created_at 등) 숨김
 * - 일본어 원문 필드(*_ja) 숨김 (한국어 필드와 짝)
 * - snake_case 키 → 한글 라벨로 변환
 * - 빈 객체/배열/문자열 숨김
 * - 배열은 인덱스 라벨 없이 카드 리스트로 (자식 객체의 대표 한글 필드를 제목)
 */
@Composable
fun JsonTreeView(element: JsonElement?, modifier: Modifier = Modifier) {
    if (element == null) return
    Column(modifier = modifier) {
        renderNode(label = null, element = element, depth = 0, isArrayItem = false)
    }
}

@Composable
private fun renderNode(label: String?, element: JsonElement, depth: Int, isArrayItem: Boolean) {
    if (label != null && label in hiddenKeys) return
    if (label != null && label.endsWith("_ja")) return
    if (element.isBlank()) return

    when (element) {
        is JsonObject -> renderObject(label, element, depth, isArrayItem)
        is JsonArray -> renderArray(label, element, depth)
        is JsonPrimitive -> renderPrimitive(label, element, depth, isArrayItem)
        is JsonNull -> Unit
    }
}

@Composable
private fun renderObject(label: String?, obj: JsonObject, depth: Int, isArrayItem: Boolean) {
    val visibleEntries = obj.entries.filter { (k, v) ->
        k !in hiddenKeys && !k.endsWith("_ja") && !v.isBlank()
    }
    if (visibleEntries.isEmpty()) return

    // 객체가 한 줄로 표현될 정도로 단순(작은 leaf 1~3개) 하면 인라인 카드.
    val allLeaf = visibleEntries.all { (_, v) -> v is JsonPrimitive }
    if (allLeaf && (isArrayItem || depth >= 1)) {
        InlineCard(label = label?.let { koreanLabel(it) }, obj = visibleEntries, depth = depth)
        return
    }

    if (label != null && !isArrayItem) {
        KeyLabel(koreanLabel(label), depth)
    } else if (isArrayItem) {
        // 배열 항목 카드 헤더: 대표 한글 필드 추출
        val title = representativeTitle(obj)
        if (title != null) ItemHeader(title, depth)
    }
    visibleEntries.forEach { (k, v) ->
        renderNode(k, v, depth + (if (label == null && !isArrayItem) 0 else 1), false)
    }
}

@Composable
private fun renderArray(label: String?, arr: JsonArray, depth: Int) {
    val visible = arr.filter { !it.isBlank() }
    if (visible.isEmpty()) return

    if (label != null) {
        KeyLabel(koreanLabel(label) + " · ${visible.size}", depth)
    }
    visible.forEach { child ->
        when (child) {
            is JsonObject, is JsonArray -> renderNode(null, child, depth + 1, isArrayItem = true)
            is JsonPrimitive -> {
                val s = child.contentOrNull.orEmpty()
                if (s.isNotBlank()) Bullet(s, depth + 1)
            }
            is JsonNull -> Unit
        }
    }
}

@Composable
private fun renderPrimitive(label: String?, p: JsonPrimitive, depth: Int, isArrayItem: Boolean) {
    val v = p.contentOrNull.orEmpty()
    if (v.isBlank()) return
    if (label == null) {
        Bullet(v, depth)
    } else {
        Leaf(koreanLabel(label), v, depth)
    }
}

// ── 컴포넌트 ──

@Composable
private fun KeyLabel(text: String, depth: Int) {
    Spacer(Modifier.height(8.dp))
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 10).dp, top = 4.dp, bottom = 4.dp),
    )
}

@Composable
private fun ItemHeader(text: String, depth: Int) {
    Spacer(Modifier.height(6.dp))
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 10).dp, top = 2.dp, bottom = 2.dp),
    )
}

@Composable
private fun Leaf(label: String, value: String, depth: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 10).dp, top = 3.dp, bottom = 3.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Column {
            Text(label, fontSize = 10.sp, color = TextTertiary)
            Text(value, fontSize = 13.sp, color = TextPrimary, lineHeight = 17.sp)
        }
    }
}

@Composable
private fun InlineCard(label: String?, obj: List<Map.Entry<String, JsonElement>>, depth: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 10).dp, top = 3.dp, bottom = 3.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Column {
            label?.let { Text(it, fontSize = 11.sp, color = TextTertiary) }
            obj.forEach { (k, v) ->
                val prim = (v as? JsonPrimitive)?.contentOrNull.orEmpty()
                if (prim.isBlank()) return@forEach
                Text(
                    text = "${koreanLabel(k)}: $prim",
                    fontSize = 13.sp,
                    color = TextPrimary,
                    lineHeight = 17.sp,
                )
            }
        }
    }
}

@Composable
private fun Bullet(text: String, depth: Int) {
    Text(
        text = "• $text",
        fontSize = 13.sp,
        color = TextPrimary,
        lineHeight = 18.sp,
        modifier = Modifier.padding(start = (depth * 10).dp, top = 2.dp, bottom = 2.dp),
    )
}

// ── 유틸 ──

private fun JsonElement.isBlank(): Boolean = when (this) {
    is JsonNull -> true
    is JsonPrimitive -> (contentOrNull ?: "").isBlank()
    is JsonObject -> entries.all { (k, v) ->
        k in hiddenKeys || k.endsWith("_ja") || v.isBlank()
    }
    is JsonArray -> all { it.isBlank() }
}

/** 배열 항목 카드의 제목으로 쓸 대표 한글 필드. */
private fun representativeTitle(obj: JsonObject): String? {
    val priority = listOf(
        "name_ko", "title_ko", "cause_ko", "symptom_ko", "category_ko",
        "label_ko", "description_ko", "manufacturer_ko",
        "name", "id",
    )
    for (key in priority) {
        val v = obj[key]
        val s = (v as? JsonPrimitive)?.contentOrNull?.trim()
        if (!s.isNullOrBlank()) return s
    }
    return null
}

/** 숨길 메타/내부 키. */
private val hiddenKeys = setOf(
    "\$schema", "version", "created_at", "source_pdf", "manual_ref", "published",
    "total_pages", "language", "manual_type_ko", "status", "dataset_count",
    "page_ref", "schema",
)

/** snake_case 키 → 한글 라벨. _ko 접미사는 제거하고 한글 라벨 매핑. */
private fun koreanLabel(key: String): String {
    val normalized = key.removeSuffix("_ko")
    return labels[normalized] ?: labels[key] ?: key.replace('_', ' ')
}

private val labels: Map<String, String> = mapOf(
    // 공통
    "id" to "ID", "no" to "번호", "code" to "코드",
    "name" to "이름", "title" to "제목", "label" to "라벨",
    "description" to "설명", "note" to "비고", "notes" to "비고",
    "category" to "분류", "categories" to "분류", "type" to "유형",
    "manufacturer" to "제조사", "model" to "모델", "models" to "모델",
    "machine" to "기계", "legend" to "범례", "items" to "항목",
    "actions" to "작업", "remedy" to "조치", "remedies" to "조치",
    "action" to "조치", "cause" to "원인", "causes" to "원인",
    "symptom" to "증상", "symptoms" to "증상",
    "cases" to "사례", "case" to "사례",
    "interval" to "주기", "intervals" to "주기",
    "warning" to "경고", "warnings" to "경고",
    "severity" to "심각도",
    "image" to "이미지", "icon" to "아이콘", "color" to "색상",
    "applies_to" to "적용 모델", "compatible" to "호환 모델", "crop" to "주요 작물",
    "manufacturer_ko" to "제조사", "category_ko" to "분류", "model_ko" to "모델",
    "manufacturer_ja" to "제조사",
    "series_name" to "시리즈",
    // 제원
    "specifications" to "제원", "common" to "공통", "by_model" to "모델별",
    "engine_type" to "엔진 형식", "engine_model" to "엔진 모델",
    "displacement_cc" to "배기량 (cc)",
    "power_kw_rpm" to "출력",
    "fuel_tank_L" to "연료 탱크 (L)",
    "transmission" to "변속기",
    "crawler" to "크롤러",
    "speed_m_s" to "주행 속도 (m/s)",
    "length_mm" to "전장 (mm)", "width_mm" to "전폭 (mm)", "height_mm" to "전고 (mm)",
    "mass_kg" to "중량 (kg)",
    "harvest_width_mm" to "수확 폭 (mm)",
    "alerts" to "경고",
    "safety_devices" to "안전 장치",
    // 정기점검·급유
    "inspection_schedule" to "정기점검 일람",
    "lubrication_schedule" to "급유·주유 일람",
    "lubricant_type" to "윤활유",
    "torque_specs_kgf_cm" to "토크 사양 (kgf·cm)",
    "fluids" to "유체",
    "location" to "위치",
    "initial" to "초기",
    "break_in_required" to "길들이기 필요",
    "service_required" to "정비 필요",
    "emergency_response" to "응급 조치",
    "scheduled_maintenance_reminder" to "정기 정비 안내",
    // 트러블슈팅·경고등
    "troubleshooting" to "트러블슈팅",
    "warning_lights" to "경고등",
    "error_codes" to "에러 코드",
    "scr_error_codes" to "SCR 에러 코드",
    "important_categories" to "중요 카테고리",
    "example_codes" to "예시 코드",
    "engine_restriction" to "엔진 제한",
    "meaning" to "의미",
    "dpf_warning_levels" to "DPF 경고 레벨",
    "overheat_response" to "과열 대처",
    "fuse_circuits" to "퓨즈 회로",
    "consumables" to "소모품",
)

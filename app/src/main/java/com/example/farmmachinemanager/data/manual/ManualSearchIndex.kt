package com.example.farmmachinemanager.data.manual

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * 9개 머신 매뉴얼 데이터 통합 검색.
 *
 * 첫 호출 시 모든 머신의 보유 데이터셋 JSON 을 파싱해 leaf string 들을
 * 인덱싱하고 메모리 캐시. 이후 검색은 substring 매칭(대소문자·공백 무시).
 *
 * 분류:
 *  - MODEL: 카탈로그 entry(model_ko/category_ko/manufacturer_ko/note/compatible/crop)
 *  - SYMPTOM: troubleshooting 의 증상/원인 필드 (symptoms_ko, cause_ko, name_ko)
 *  - PART: consumables/parts 의 부품번호·부품명 (id, name_ko, part_no, partNo)
 *  - GENERAL: 위 분류 외의 모든 string leaf (보조 매칭)
 */
object ManualSearchIndex {

    enum class HitType { MODEL, SYMPTOM, PART, GENERAL }

    data class Hit(
        val machineId: String,
        val machineLabel: String,
        val type: HitType,
        val dataset: String,
        val snippet: String,
    )

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private var cache: List<IndexedEntry>? = null

    private data class IndexedEntry(
        val machineId: String,
        val machineLabel: String,
        val dataset: String,
        val type: HitType,
        val text: String,
        val normalized: String,
    )

    private val symptomKeys = setOf("symptoms_ko", "cause_ko", "name_ko", "cause_ja", "remedy_ko", "action_ko")
    private val partKeys = setOf("id", "part_no", "partNo", "part_number", "code", "model")

    suspend fun search(context: Context, query: String): List<Hit> = withContext(Dispatchers.IO) {
        if (query.trim().length < 1) return@withContext emptyList()
        val index = cache ?: buildIndex(context).also { cache = it }
        val q = query.normalize()
        index.asSequence()
            .filter { it.normalized.contains(q) }
            .take(120)
            .map { Hit(it.machineId, it.machineLabel, it.type, it.dataset, snippet(it.text, q)) }
            .toList()
    }

    private suspend fun buildIndex(context: Context): List<IndexedEntry> {
        val entries = ManualMachineCatalog.entries(context)
        val out = mutableListOf<IndexedEntry>()
        for (entry in entries) {
            val label = "${entry.manufacturerKo} ${entry.modelKo}"
            // 카탈로그 자체 → MODEL hit
            listOfNotNull(
                entry.modelKo, entry.categoryKo, entry.manufacturerKo,
                entry.noteKo, entry.compatibleKo, entry.cropKo,
            ).forEach { t ->
                out += IndexedEntry(entry.id, label, "catalog", HitType.MODEL, t, t.normalize())
            }
            // 각 데이터셋 JSON leaf 수집
            for (ds in entry.datasets) {
                val text = readDataset(context, entry.id, ds) ?: continue
                val element = runCatching { json.parseToJsonElement(text) }.getOrNull() ?: continue
                collectLeaves(element, parentKey = null, sink = { type, leaf ->
                    if (leaf.length in 1..200) {
                        out += IndexedEntry(entry.id, label, ds, type, leaf, leaf.normalize())
                    }
                })
            }
        }
        return out
    }

    private fun readDataset(context: Context, machineId: String, ds: String): String? = runCatching {
        context.assets.open("manuals/$machineId/$ds.json").bufferedReader().use { it.readText() }
    }.getOrNull()

    private fun collectLeaves(
        e: JsonElement,
        parentKey: String?,
        sink: (HitType, String) -> Unit,
    ) {
        when (e) {
            is JsonObject -> e.forEach { (k, v) -> collectLeaves(v, k, sink) }
            is JsonArray -> e.forEach { collectLeaves(it, parentKey, sink) }
            is JsonPrimitive -> {
                val s = e.contentOrNull?.trim().orEmpty()
                if (s.isBlank()) return
                val type = when {
                    parentKey in symptomKeys -> HitType.SYMPTOM
                    parentKey in partKeys -> HitType.PART
                    else -> HitType.GENERAL
                }
                sink(type, s)
            }
            else -> {}
        }
    }

    private fun String.normalize(): String =
        lowercase().replace(Regex("\\s+"), "")

    private fun snippet(text: String, query: String): String {
        val src = text
        val idx = src.normalize().indexOf(query)
        if (idx < 0) return src.take(80)
        val start = (idx - 20).coerceAtLeast(0)
        val end = (idx + query.length + 40).coerceAtMost(src.length)
        return (if (start > 0) "…" else "") + src.substring(start, end) +
            (if (end < src.length) "…" else "")
    }
}

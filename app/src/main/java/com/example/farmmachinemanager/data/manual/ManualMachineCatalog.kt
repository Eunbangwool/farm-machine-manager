package com.example.farmmachinemanager.data.manual

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 농기계 대백과(매뉴얼 카탈로그) 메타데이터.
 *
 * assets/manuals/MASTER_INDEX.json 을 로드해서 9개(현재) 머신의 제조사·기종·모델·
 * 보유 데이터셋 정보를 제공한다. 대백과 메인 화면, 통합 검색 등에서 사용.
 *
 * 데이터셋 가용성은 [MachineCatalogEntry.datasets] 로 알 수 있다. 머신마다
 * 보유 데이터셋이 다르므로(예: km_combine_kc100 은 index 만, mr1050 은 7종)
 * UI 는 항상 가용성을 체크해야 한다.
 */
object ManualMachineCatalog {

    @Serializable
    data class Index(
        val version: String = "",
        @SerialName("total_machines") val totalMachines: Int = 0,
        val machines: List<MachineCatalogEntry> = emptyList(),
    )

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private var cache: List<MachineCatalogEntry>? = null

    suspend fun entries(context: Context): List<MachineCatalogEntry> = withContext(Dispatchers.IO) {
        cache ?: run {
            val text = context.assets.open("manuals/MASTER_INDEX.json")
                .bufferedReader().use { it.readText() }
            json.decodeFromString<Index>(text).machines.also { cache = it }
        }
    }

    suspend fun byId(context: Context, id: String): MachineCatalogEntry? =
        entries(context).firstOrNull { it.id == id }

    /** 제조사명으로 그룹핑한 카탈로그. UI 트리 표시용. */
    suspend fun groupedByManufacturer(context: Context): Map<String, List<MachineCatalogEntry>> =
        entries(context).groupBy { it.manufacturerKo }
}

@Serializable
data class MachineCatalogEntry(
    val id: String,
    @SerialName("manufacturer_ko") val manufacturerKo: String,
    @SerialName("category_ko") val categoryKo: String,
    @SerialName("model_ko") val modelKo: String,
    val datasets: List<String> = emptyList(),
    @SerialName("dataset_count") val datasetCount: Int = 0,
    val language: String = "ko",
    @SerialName("note_ko") val noteKo: String? = null,
    @SerialName("compatible_ko") val compatibleKo: String? = null,
    @SerialName("crop_ko") val cropKo: String? = null,
    val status: String? = null,
) {
    /** 화면 표시용 보조 — 데이터셋 가용 여부. */
    fun has(dataset: String): Boolean = dataset in datasets

    /** 카테고리에 어울리는 이모지(트랙터/이앙기/콤바인). */
    val emoji: String
        get() = when {
            "트랙터" in categoryKo -> "🚜"
            "이앙기" in categoryKo -> "🌾"
            "콤바인" in categoryKo -> "🌾"
            else -> "🛠️"
        }
}

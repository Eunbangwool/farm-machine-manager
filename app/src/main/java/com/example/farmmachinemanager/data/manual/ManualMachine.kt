package com.example.farmmachinemanager.data.manual

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 각 데이터셋 최상위에 공통으로 들어 있는 machine 헤더. */
@Serializable
data class ManualMachine(
    val manufacturer: String,
    @SerialName("manufacturer_ko") val manufacturerKo: String? = null,
    val category: String? = null,
    @SerialName("category_ko") val categoryKo: String? = null,
    @SerialName("series_name") val seriesName: String? = null,
    @SerialName("series_name_ja") val seriesNameJa: String? = null,
    @SerialName("series_name_ko") val seriesNameKo: String? = null,
    val models: List<String> = emptyList(),
    @SerialName("manual_ref") val manualRef: String? = null,
    val source: String? = null,
)

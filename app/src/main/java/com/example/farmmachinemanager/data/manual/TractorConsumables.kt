package com.example.farmmachinemanager.data.manual

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * MR1050 트랙터 소모품 부품 일람 (퓨즈·전구·와이퍼).
 */
@Serializable
data class TractorConsumablesData(
    val machine: ManualMachine,
    val categories: List<TractorConsumableCategory> = emptyList(),
    val parts: List<TractorConsumablePart> = emptyList(),
    @SerialName("general_notes_ko") val generalNotesKo: List<String> = emptyList(),
)

@Serializable
data class TractorConsumableCategory(
    val id: String,
    @SerialName("name_ko") val nameKo: String,
)

@Serializable
data class TractorConsumablePart(
    val id: String,
    @SerialName("category_id") val categoryId: String,
    @SerialName("name_ja") val nameJa: String? = null,
    @SerialName("name_ko") val nameKo: String,
    @SerialName("part_number") val partNumber: String? = null,
    @SerialName("spec_ko") val specKo: String? = null,
    @SerialName("applies_to") val appliesTo: String? = null,
    @SerialName("page_ref") val pageRef: Int? = null,
)

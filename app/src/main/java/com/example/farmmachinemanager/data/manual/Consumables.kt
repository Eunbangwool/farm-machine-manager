package com.example.farmmachinemanager.data.manual

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConsumablesData(
    val machine: ManualMachine,
    val categories: List<ConsumableCategory>,
    val parts: List<ConsumablePart>,
    @SerialName("general_notes_ko") val generalNotesKo: List<String> = emptyList(),
)

@Serializable
data class ConsumableCategory(
    val id: String,
    @SerialName("name_ko") val nameKo: String,
)

@Serializable
data class ConsumablePart(
    val id: String,
    @SerialName("category_id") val categoryId: String,
    @SerialName("name_ja") val nameJa: String? = null,
    @SerialName("name_ko") val nameKo: String,
    @SerialName("part_number") val partNumber: String,
    val quantity: Int? = null,
    @SerialName("quantities_by_model") val quantitiesByModel: Map<String, Int>? = null,
    @SerialName("quantities_by_spec") val quantitiesBySpec: Map<String, Int>? = null,
    @SerialName("applies_to") val appliesTo: AppliesTo? = null,
    @SerialName("applies_to_spec") val appliesToSpec: String? = null,
    @SerialName("spec_ko") val specKo: String? = null,
    @SerialName("page_ref") val pageRef: Int? = null,
    @SerialName("service_required") val serviceRequired: Boolean = false,
)

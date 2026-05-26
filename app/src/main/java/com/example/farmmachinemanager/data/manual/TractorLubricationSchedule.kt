package com.example.farmmachinemanager.data.manual

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * MR1050 트랙터 급유·주유 일람.
 */
@Serializable
data class TractorLubricationScheduleData(
    val machine: ManualMachine,
    val categories: List<TractorLubricationCategory> = emptyList(),
    val items: List<TractorLubricationItem> = emptyList(),
    @SerialName("recommended_lubricants_ko") val recommendedLubricantsKo: Map<String, JsonElement> = emptyMap(),
    @SerialName("general_notes_ko") val generalNotesKo: List<String> = emptyList(),
)

@Serializable
data class TractorLubricationCategory(
    val id: String,
    @SerialName("name_ko") val nameKo: String,
)

@Serializable
data class TractorLubricationItem(
    val id: String,
    val no: Int? = null,
    @SerialName("category_id") val categoryId: String,
    @SerialName("location_ja") val locationJa: String? = null,
    @SerialName("location_ko") val locationKo: String,
    @SerialName("capacity_ko") val capacityKo: String? = null,
    @SerialName("lubricant_type_ko") val lubricantTypeKo: String? = null,
    @SerialName("lubricant_type_ja") val lubricantTypeJa: String? = null,
    @SerialName("warning_ko") val warningKo: String? = null,
    @SerialName("applies_to_spec") val appliesToSpec: String? = null,
    @SerialName("applies_to_spec_note_ko") val appliesToSpecNoteKo: String? = null,
    @SerialName("page_ref") val pageRef: Int? = null,
)

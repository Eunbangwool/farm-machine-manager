package com.example.farmmachinemanager.data.manual

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LubricationScheduleData(
    val machine: ManualMachine,
    val categories: List<LubricationCategory>,
    val items: List<LubricationItem>,
    @SerialName("general_notes_ko") val generalNotesKo: List<String> = emptyList(),
)

@Serializable
data class LubricationCategory(
    val id: String,
    @SerialName("name_ko") val nameKo: String,
)

@Serializable
data class LubricationItem(
    val id: String,
    @SerialName("category_id") val categoryId: String,
    @SerialName("location_ja") val locationJa: String? = null,
    @SerialName("location_ko") val locationKo: String,
    val action: String,
    @SerialName("inspection_interval_ko") val inspectionIntervalKo: String? = null,
    @SerialName("replacement_interval_ko") val replacementIntervalKo: String? = null,
    @SerialName("spec_capacity_ko") val specCapacityKo: String? = null,
    @SerialName("capacity_ko") val capacityKo: String? = null,
    @SerialName("lubricant_type_ko") val lubricantTypeKo: String? = null,
    @SerialName("page_ref") val pageRef: Int? = null,
    @SerialName("applies_locations_ko") val appliesLocationsKo: List<String> = emptyList(),
)

package com.example.farmmachinemanager.data.manual

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * MR1050 트랙터 주요 제원. nested 구조가 많아 일부는 JsonElement 로 받음.
 */
@Serializable
data class TractorSpecificationsData(
    val machine: ManualMachine,
    @SerialName("common_specifications") val commonSpecifications: Map<String, JsonElement> = emptyMap(),
    val models: List<TractorSpecModel> = emptyList(),
    @SerialName("spec_variants_ko") val specVariantsKo: Map<String, String> = emptyMap(),
    @SerialName("notes_ko") val notesKo: List<String> = emptyList(),
)

@Serializable
data class TractorSpecModel(
    val model: String,
    @SerialName("name_ko") val nameKo: String,
    @SerialName("spec_ko") val specKo: String? = null,
    @SerialName("horsepower_ps") val horsepowerPs: String? = null,
    @SerialName("drive_type_ko") val driveTypeKo: String? = null,
    @SerialName("dimensions_mm") val dimensionsMm: Map<String, JsonElement> = emptyMap(),
    @SerialName("mass_kg") val massKg: JsonElement? = null,
    val engine: Map<String, JsonElement> = emptyMap(),
    @SerialName("front_tire_ko") val frontTireKo: String? = null,
    val crawler: Map<String, JsonElement> = emptyMap(),
    @SerialName("suspension_ko") val suspensionKo: String? = null,
    @SerialName("spec_variants_ko") val specVariantsList: List<String> = emptyList(),
)

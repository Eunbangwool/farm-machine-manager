package com.example.farmmachinemanager.data.manual

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TroubleshootingData(
    val machine: ManualMachine,
    val categories: List<TroubleshootingCategory>,
    val cases: List<TroubleshootingCase>,
    @SerialName("general_notes") val generalNotes: List<String> = emptyList(),
)

@Serializable
data class TroubleshootingCategory(
    val id: String,
    @SerialName("name_ja") val nameJa: String? = null,
    @SerialName("name_ko") val nameKo: String,
    @SerialName("description_ko") val descriptionKo: String? = null,
    @SerialName("page_ref") val pageRef: Int? = null,
    @SerialName("applies_to") val appliesTo: AppliesTo? = null,
    @SerialName("spec_required") val specRequired: String? = null,
)

@Serializable
data class TroubleshootingCase(
    val id: String,
    @SerialName("category_id") val categoryId: String,
    @SerialName("cause_ja") val causeJa: String? = null,
    @SerialName("cause_ko") val causeKo: String,
    @SerialName("symptoms_ko") val symptomsKo: List<String> = emptyList(),
    @SerialName("check_points_ko") val checkPointsKo: List<String> = emptyList(),
    val remedies: List<Remedy> = emptyList(),
    @SerialName("page_ref") val pageRef: Int? = null,
    @SerialName("applies_to") val appliesTo: AppliesTo? = null,
    @SerialName("applies_to_spec") val appliesToSpec: String? = null,
    @SerialName("spec_required") val specRequired: String? = null,
)

@Serializable
data class Remedy(
    val type: String,
    @SerialName("type_ko") val typeKo: String,
    @SerialName("action_ko") val actionKo: String,
    @SerialName("manual_page_ref") val manualPageRef: Int? = null,
)

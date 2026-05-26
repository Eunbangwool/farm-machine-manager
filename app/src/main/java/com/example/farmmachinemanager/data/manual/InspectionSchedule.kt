package com.example.farmmachinemanager.data.manual

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InspectionScheduleData(
    val machine: ManualMachine,
    val legend: InspectionLegend? = null,
    val sections: List<InspectionSection>,
    val items: List<InspectionItem>,
    @SerialName("general_notes_ko") val generalNotesKo: List<String> = emptyList(),
)

@Serializable
data class InspectionLegend(
    @SerialName("action_symbols") val actionSymbols: Map<String, ActionSymbol> = emptyMap(),
    @SerialName("interval_types") val intervalTypes: List<String> = emptyList(),
)

@Serializable
data class ActionSymbol(
    @SerialName("meaning_ja") val meaningJa: String? = null,
    @SerialName("meaning_ko") val meaningKo: String,
)

@Serializable
data class InspectionSection(
    val id: String,
    @SerialName("name_ja") val nameJa: String? = null,
    @SerialName("name_ko") val nameKo: String,
    @SerialName("page_ref") val pageRef: Int? = null,
    @SerialName("applies_to_spec") val appliesToSpec: String? = null,
)

@Serializable
data class InspectionItem(
    val id: String,
    @SerialName("section_id") val sectionId: String,
    @SerialName("name_ja") val nameJa: String? = null,
    @SerialName("name_ko") val nameKo: String,
    val actions: List<InspectionAction> = emptyList(),
    @SerialName("applies_to") val appliesTo: AppliesTo? = null,
    @SerialName("applies_to_spec") val appliesToSpec: String? = null,
    @SerialName("service_required") val serviceRequired: Boolean = false,
    @SerialName("page_ref") val pageRef: Int? = null,
)

@Serializable
data class InspectionAction(
    val type: String,
    @SerialName("type_ko") val typeKo: String,
    @SerialName("interval_ko") val intervalKo: String,
)

package com.example.farmmachinemanager.data.manual

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * MR1050 트랙터 정기점검 일람.
 * 이앙기 InspectionScheduleData 와 schema 가 달라 별도 모델 사용 (items 평탄, action·interval 이 item 에 직접 포함).
 */
@Serializable
data class TractorInspectionScheduleData(
    val machine: ManualMachine,
    val legend: TractorInspectionLegend? = null,
    @SerialName("specifications_variants_ko") val specificationsVariantsKo: Map<String, String> = emptyMap(),
    val items: List<TractorInspectionItem> = emptyList(),
    @SerialName("general_notes_ko") val generalNotesKo: List<String> = emptyList(),
)

@Serializable
data class TractorInspectionLegend(
    @SerialName("action_symbols") val actionSymbols: Map<String, ActionSymbol> = emptyMap(),
    @SerialName("interval_groups_ko") val intervalGroupsKo: List<String> = emptyList(),
)

@Serializable
data class TractorInspectionItem(
    val id: String,
    @SerialName("name_ja") val nameJa: String? = null,
    @SerialName("name_ko") val nameKo: String,
    @SerialName("action_ja") val actionJa: String? = null,
    @SerialName("action_ko") val actionKo: String,
    @SerialName("interval_ko") val intervalKo: String,
    @SerialName("applies_to") val appliesTo: String? = null,
    @SerialName("applies_to_spec") val appliesToSpec: String? = null,
    @SerialName("page_ref") val pageRef: Int? = null,
    @SerialName("break_in_required") val breakInRequired: Boolean = false,
    @SerialName("service_required") val serviceRequired: Boolean = false,
    @SerialName("note_ko") val noteKo: String? = null,
)

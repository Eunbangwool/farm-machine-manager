package com.example.farmmachinemanager.data.manual

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * 트랙터 경고등 + DPF 경고 단계 + 오버히트 응급 처치.
 * 안전 직결 데이터.
 */
@Serializable
data class TractorWarningLightsData(
    val machine: ManualMachine,
    @SerialName("info_ko") val infoKo: String? = null,
    @SerialName("warning_lights") val warningLights: List<WarningLight> = emptyList(),
    @SerialName("dpf_warning_levels") val dpfWarningLevels: DpfWarningLevels? = null,
    @SerialName("overheat_response_ko") val overheatResponseKo: OverheatResponse? = null,
    @SerialName("general_notes_ko") val generalNotesKo: List<String> = emptyList(),
)

@Serializable
data class WarningLight(
    val id: String,
    val no: Int? = null,
    @SerialName("name_ja") val nameJa: String? = null,
    @SerialName("name_ko") val nameKo: String,
    @SerialName("trigger_ko") val triggerKo: String? = null,
    @SerialName("behavior_ko") val behaviorKo: String? = null,
    @SerialName("severity_ko") val severityKo: String? = null,
    @SerialName("action_ko") val actionKo: String? = null,
    @SerialName("symptoms_when_lit_ko") val symptomsWhenLitKo: List<String> = emptyList(),
    @SerialName("warning_ko") val warningKo: String? = null,
    @SerialName("additional_notes_ko") val additionalNotesKo: String? = null,
    @SerialName("page_ref") val pageRef: Int? = null,
)

@Serializable
data class DpfWarningLevels(
    @SerialName("description_ko") val descriptionKo: String? = null,
    @SerialName("auto_regen_mode_ko") val autoRegenModeKo: String? = null,
    val levels: List<DpfWarningLevel> = emptyList(),
)

@Serializable
data class DpfWarningLevel(
    val level: JsonElement,
    @SerialName("buzzer_ko") val buzzerKo: String? = null,
    @SerialName("state_ko") val stateKo: String? = null,
    @SerialName("action_ko") val actionKo: String? = null,
    @SerialName("engine_output_ko") val engineOutputKo: String? = null,
    @SerialName("warning_ko") val warningKo: String? = null,
    @SerialName("page_ref") val pageRef: Int? = null,
)

@Serializable
data class OverheatResponse(
    @SerialName("trigger_ko") val triggerKo: String? = null,
    @SerialName("immediate_action_ko") val immediateActionKo: List<String> = emptyList(),
    @SerialName("check_points_ko") val checkPointsKo: List<String> = emptyList(),
    @SerialName("page_ref") val pageRef: Int? = null,
)

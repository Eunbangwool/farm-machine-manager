package com.example.farmmachinemanager.data.manual

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * 트랙터 트러블슈팅 + 전자 제어 에러 코드.
 */
@Serializable
data class TractorTroubleshootingData(
    val machine: ManualMachine,
    val categories: List<TractorTroubleshootingCategory> = emptyList(),
    val cases: List<TractorTroubleshootingCase> = emptyList(),
    @SerialName("error_codes") val errorCodes: TractorErrorCodes? = null,
    @SerialName("scheduled_maintenance_reminder_ko") val scheduledMaintenanceReminderKo: String? = null,
    @SerialName("general_notes_ko") val generalNotesKo: List<String> = emptyList(),
)

@Serializable
data class TractorTroubleshootingCategory(
    val id: String,
    @SerialName("name_ja") val nameJa: String? = null,
    @SerialName("name_ko") val nameKo: String,
    @SerialName("description_ko") val descriptionKo: String? = null,
    @SerialName("page_ref") val pageRef: Int? = null,
)

@Serializable
data class TractorTroubleshootingCase(
    val id: String,
    @SerialName("category_id") val categoryId: String,
    @SerialName("cause_ja") val causeJa: String? = null,
    @SerialName("cause_ko") val causeKo: String,
    @SerialName("symptoms_ko") val symptomsKo: List<String> = emptyList(),
    val remedies: List<TractorRemedy> = emptyList(),
    @SerialName("page_ref") val pageRef: Int? = null,
    @SerialName("emergency_response_ko") val emergencyResponseKo: List<String> = emptyList(),
    @SerialName("service_required") val serviceRequired: Boolean = false,
)

@Serializable
data class TractorRemedy(
    val type: String,
    @SerialName("type_ko") val typeKo: String,
    @SerialName("action_ko") val actionKo: String,
    @SerialName("manual_page_ref") val manualPageRef: Int? = null,
)

@Serializable
data class TractorErrorCodes(
    @SerialName("info_ko") val infoKo: String? = null,
    @SerialName("page_ref") val pageRef: Int? = null,
    @SerialName("important_categories") val importantCategories: List<ErrorCodeImportantCategory> = emptyList(),
    @SerialName("scr_error_codes") val scrErrorCodes: ScrErrorCodes? = null,
)

@Serializable
data class ErrorCodeImportantCategory(
    @SerialName("category_ja") val categoryJa: String? = null,
    @SerialName("category_ko") val categoryKo: String,
    @SerialName("common_action_ko") val commonActionKo: String? = null,
    @SerialName("page_ref") val pageRef: Int? = null,
    @SerialName("additional_notes_ko") val additionalNotesKo: List<String> = emptyList(),
)

@Serializable
data class ScrErrorCodes(
    @SerialName("info_ko") val infoKo: String? = null,
    @SerialName("example_codes") val exampleCodes: List<ScrErrorCode> = emptyList(),
    @SerialName("page_ref") val pageRef: Int? = null,
)

@Serializable
data class ScrErrorCode(
    val code: String,
    @SerialName("meaning_ko") val meaningKo: String? = null,
    @SerialName("action_ko") val actionKo: String? = null,
    @SerialName("engine_restriction_ko") val engineRestrictionKo: String? = null,
)

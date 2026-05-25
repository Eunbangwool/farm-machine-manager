package com.example.farmmachinemanager.data.manual

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * 모델별 제원. 일부 필드(치수·차륜·예비모 수 등)는 모델·사양에 따라 키 셋과 값 타입이
 * 달라 정형 모델로 다루기 어렵다. 그래서 models는 JsonObject로 보관하고, 화면이
 * 필요로 하는 값은 헬퍼로 안전하게 추출한다.
 */
@Serializable
data class SpecificationsData(
    val machine: ManualMachine,
    @SerialName("common_specifications") val commonSpecifications: JsonObject? = null,
    val models: List<JsonObject> = emptyList(),
    @SerialName("spec_variants_ko") val specVariantsKo: Map<String, String> = emptyMap(),
    @SerialName("warning_devices_common_ko") val warningDevicesCommonKo: List<String> = emptyList(),
    @SerialName("warning_devices_S_GS_ko") val warningDevicesSGSKo: List<String> = emptyList(),
    @SerialName("notes_ko") val notesKo: List<String> = emptyList(),
)

/** 모델 JsonObject에서 안전하게 값을 꺼내는 헬퍼. */
object ModelSpecAccess {
    fun model(obj: JsonObject): String? = obj["model"]?.asPrimitiveString()
    fun nameKo(obj: JsonObject): String? = obj["name_ko"]?.asPrimitiveString()
    fun specs(obj: JsonObject): String? = obj["specs"]?.asPrimitiveString()
    fun rows(obj: JsonObject): Int? = obj["rows"]?.jsonPrimitive?.contentOrNull?.toIntOrNull()

    private fun JsonElement.asPrimitiveString(): String? = (this as? JsonPrimitive)?.contentOrNull
}

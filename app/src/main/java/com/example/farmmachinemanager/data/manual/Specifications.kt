package com.example.farmmachinemanager.data.manual

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

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
    fun rows(obj: JsonObject): Int? = obj["rows"]?.jsonPrimitive?.longOrNull?.toInt()
    fun massKg(obj: JsonObject): String? = obj["mass_kg"]?.asPrimitiveString()
    fun fertilizerHopper(obj: JsonObject): String? = obj["fertilizer_hopper_L_kg"]?.asPrimitiveString()

    /** spare_seedling_count 는 Int 또는 String 모두 가능. 문자열로 정규화. */
    fun spareSeedlingCount(obj: JsonObject): String? = obj["spare_seedling_count"]?.let {
        when (it) {
            is JsonPrimitive -> it.contentOrNull
            else -> it.toString()
        }
    }

    fun workSpeed(obj: JsonObject): String? = obj["work_speed_mps"]?.asPrimitiveString()
    fun workEfficiency(obj: JsonObject): String? = obj["work_efficiency_min_per_10a"]?.asPrimitiveString()

    /** dimensions_mm: Map<String, Int> 형태이지만 키가 모델마다 다양. */
    fun dimensions(obj: JsonObject): List<Pair<String, String>> {
        val raw = (obj["dimensions_mm"] as? JsonObject) ?: return emptyList()
        return raw.entries.map { (k, v) ->
            val str = when (v) {
                is JsonPrimitive -> v.contentOrNull ?: v.toString()
                else -> v.toString()
            }
            k to str
        }
    }

    /** wheels: 키별로 String 또는 Int. 표시는 모두 String 으로 통일. */
    fun wheels(obj: JsonObject): List<Pair<String, String>> {
        val raw = (obj["wheels"] as? JsonObject) ?: return emptyList()
        return raw.entries.map { (k, v) ->
            val str = when (v) {
                is JsonPrimitive -> v.contentOrNull ?: v.toString()
                else -> v.toString()
            }
            k to str
        }
    }

    data class Engine(
        val model: String?,
        val displacementCc: Int?,
        val powerKw: Double?,
        val powerPs: Double?,
        val rpm: Int?,
        val tankCapacityL: Int?,
    )

    fun engine(obj: JsonObject): Engine? {
        val e = (obj["engine"] as? JsonObject) ?: return null
        return Engine(
            model = e["model"]?.asPrimitiveString(),
            displacementCc = e["displacement_cc"]?.jsonPrimitive?.longOrNull?.toInt(),
            powerKw = e["power_kw"]?.jsonPrimitive?.doubleOrNull,
            powerPs = e["power_ps"]?.jsonPrimitive?.doubleOrNull,
            rpm = e["rpm"]?.jsonPrimitive?.longOrNull?.toInt(),
            tankCapacityL = e["tank_capacity_L"]?.jsonPrimitive?.longOrNull?.toInt(),
        )
    }

    private fun JsonElement.asPrimitiveString(): String? = (this as? JsonPrimitive)?.contentOrNull
}

/** 사람이 읽기 좋은 dimension 키 한국어 라벨. 없는 키는 그대로 반환. */
fun prettyDimensionLabel(rawKey: String): String = when (rawKey) {
    "length" -> "전장"
    "width_stowed" -> "전폭(접힘)"
    "height" -> "전고"
    "ground_clearance" -> "최저지상고"
    "length_CY24" -> "전장 (CY24)"
    "width_CY24" -> "전폭 (CY24)"
    "height_CY24" -> "전고 (CY24)"
    "length_Y32_CY32_MY32" -> "전장 (Y32/CY32/MY32)"
    "width_Y32_CY32_MY32" -> "전폭 (Y32/CY32/MY32)"
    "height_Y32_CY32_MY32" -> "전고 (Y32/CY32/MY32)"
    else -> rawKey
}

fun prettyWheelLabel(rawKey: String): String = when (rawKey) {
    "front_outer_diameter_x_width_mm" -> "전륜 외경 × 폭 (mm)"
    "rear_outer_diameter_x_width_mm" -> "후륜 외경 × 폭 (mm)"
    "front_wheelbase_mm" -> "전륜 윤거 (mm)"
    "rear_wheelbase_mm" -> "후륜 윤거 (mm)"
    "axle_distance_mm" -> "차축거리 (mm)"
    else -> rawKey
}

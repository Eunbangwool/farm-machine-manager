package com.example.farmmachinemanager.data.manual

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonPrimitive

/**
 * applies_to 필드 표현. JSON 원본은 "all" 문자열이거나 모델 코드 배열.
 *
 * - "all" → AppliesTo.All
 * - ["NW80N", "NW80S"] → AppliesTo.Models(...)
 */
@Serializable(with = AppliesToSerializer::class)
sealed interface AppliesTo {
    data object All : AppliesTo
    data class Models(val list: List<String>) : AppliesTo

    fun matches(model: String): Boolean = when (this) {
        All -> true
        is Models -> model in list
    }
}

object AppliesToSerializer : KSerializer<AppliesTo> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("AppliesTo")

    override fun deserialize(decoder: Decoder): AppliesTo {
        require(decoder is JsonDecoder) { "AppliesTo requires Json decoder" }
        return when (val element = decoder.decodeJsonElement()) {
            is JsonPrimitive -> if (element.content == "all") AppliesTo.All else AppliesTo.Models(listOf(element.content))
            is JsonArray -> AppliesTo.Models(element.map { it.jsonPrimitive.content })
            else -> AppliesTo.All
        }
    }

    override fun serialize(encoder: Encoder, value: AppliesTo) {
        require(encoder is JsonEncoder) { "AppliesTo requires Json encoder" }
        when (value) {
            AppliesTo.All -> encoder.encodeJsonElement(JsonPrimitive("all"))
            is AppliesTo.Models -> encoder.encodeJsonElement(buildJsonArray { value.list.forEach { add(JsonPrimitive(it)) } })
        }
    }
}

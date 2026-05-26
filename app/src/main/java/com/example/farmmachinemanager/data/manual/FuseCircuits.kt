package com.example.farmmachinemanager.data.manual

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FuseCircuitsData(
    val machine: ManualMachine,
    @SerialName("info_ko") val infoKo: String? = null,
    val boxes: List<FuseBox>,
    @SerialName("replacement_procedure_ko") val replacementProcedureKo: List<String> = emptyList(),
    @SerialName("tips_ko") val tipsKo: List<String> = emptyList(),
)

@Serializable
data class FuseBox(
    val id: String,
    @SerialName("name_ko") val nameKo: String,
    @SerialName("location_ko") val locationKo: String? = null,
    @SerialName("description_ko") val descriptionKo: String? = null,
    @SerialName("page_ref") val pageRef: Int? = null,
    val circuits: List<FuseCircuit> = emptyList(),
)

@Serializable
data class FuseCircuit(
    val number: Int,
    @SerialName("circuit_ja") val circuitJa: String? = null,
    @SerialName("circuit_ko") val circuitKo: String,
    @SerialName("capacity_amp") val capacityAmp: Int,
    @SerialName("applies_to") val appliesTo: AppliesTo? = null,
    @SerialName("applies_to_spec") val appliesToSpec: String? = null,
    @SerialName("is_spare") val isSpare: Boolean = false,
)

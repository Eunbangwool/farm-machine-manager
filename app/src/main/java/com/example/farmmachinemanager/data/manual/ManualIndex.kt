package com.example.farmmachinemanager.data.manual

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ManualIndex(
    val version: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("source_pdf") val sourcePdf: String? = null,
    val machine: ManualMachine,
    val datasets: List<DatasetDescriptor> = emptyList(),
)

@Serializable
data class DatasetDescriptor(
    val id: String,
    val file: String,
    @SerialName("name_ko") val nameKo: String,
    @SerialName("description_ko") val descriptionKo: String? = null,
    @SerialName("manual_pages") val manualPages: String? = null,
    @SerialName("use_case_ko") val useCaseKo: String? = null,
)

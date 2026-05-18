package com.example.farmmachinemanager.data

import java.time.LocalDate

/**
 * 정비 종류.
 */
enum class MaintenanceType(val displayName: String) {
    REGULAR_CHECK("정기점검"),
    REPAIR("수리"),
    CONSUMABLE_REPLACE("소모품 교체"),
    INSPECTION("검사"),
    OTHER("기타")
}

/**
 * 정비 기록.
 *
 * @param machineId 어느 기계에 대한 정비인지
 * @param operatingHoursAtMaintenance 정비 시점의 가동시간 (소모품 교체 추적용)
 * @param replacedConsumableIds 이 정비에서 교체된 소모품 ID들
 * @param isInProgress 진행 중인 정비인지 (true면 완료 안 된 상태)
 */
data class MaintenanceRecord(
    val id: String = "",
    val machineId: String = "",
    val date: LocalDate = LocalDate.now(),
    val type: MaintenanceType = MaintenanceType.OTHER,
    val title: String = "",
    val description: String? = null,
    val cost: Int? = null,
    val performedBy: String? = null,
    val shopName: String? = null,
    val operatingHoursAtMaintenance: Double? = null,
    val photoUrls: List<String> = emptyList(),
    val replacedConsumableIds: List<String> = emptyList(),
    val isInProgress: Boolean = false
)

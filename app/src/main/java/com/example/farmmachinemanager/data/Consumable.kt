package com.example.farmmachinemanager.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 소모품 카테고리.
 * TYM 트랙터 정비점검 일람표(매뉴얼)를 참고하여 구성.
 */
enum class ConsumableCategory(val displayName: String) {
    ENGINE_OIL("엔진오일"),
    TRANSMISSION_OIL("미션오일"),
    AXLE_OIL("전차축 오일"),
    HYDRAULIC_OIL("유압오일"),
    COOLANT("냉각수"),
    ENGINE_OIL_FILTER("엔진오일 필터"),
    TRANSMISSION_OIL_FILTER("미션오일 필터"),
    FUEL_FILTER("연료필터"),
    AIR_FILTER("에어필터"),
    DEF_FILTER("요소 필터"),     // 콤바인/대형 엔진 SCR 시스템용 (AdBlue 필터)
    HOSE("호스"),
    BELT("벨트"),
    TIRE("타이어"),
    BATTERY("배터리"),
    BLADE("칼날"),
    OTHER("기타")
}

/**
 * 소모품 상태 - 교체 시기 기준.
 */
enum class ConsumableStatus(val displayName: String) {
    NORMAL("정상"),
    DUE_SOON("임박"),      // 50시간 이내 또는 30일 이내
    OVERDUE("초과"),       // 교체 시기 지남
    UNKNOWN("미설정")      // 주기 또는 마지막 교체 정보 없음
}

/**
 * 소모품 (교환 주기로 관리되는 부품/오일).
 *
 * 두 가지 교체 기준이 가능:
 * - 시간 기반: replacementIntervalHours (예: 엔진오일 250시간)
 * - 날짜 기반: replacementIntervalMonths (예: 냉각수 12개월)
 *
 * 둘 다 설정된 경우 둘 중 먼저 도달하는 쪽이 교체 시점.
 */
data class Consumable(
    val id: String = "",
    val machineId: String = "",
    val name: String = "",
    val category: ConsumableCategory = ConsumableCategory.OTHER,
    val replacementIntervalHours: Double? = null,
    val replacementIntervalMonths: Int? = null,
    val lastReplacedDate: LocalDate? = null,
    val lastReplacedHours: Double? = null,
    val notes: String? = null
) {
    /**
     * 현재 가동시간 기준 남은 시간을 계산.
     * null이면 시간 기반 주기 또는 마지막 교체 시점이 없는 것.
     */
    fun hoursUntilReplacement(currentHours: Double): Double? {
        val interval = replacementIntervalHours ?: return null
        val last = lastReplacedHours ?: return null
        return (last + interval) - currentHours
    }

    /**
     * 오늘 기준 남은 일수를 계산.
     */
    fun daysUntilReplacement(today: LocalDate = LocalDate.now()): Long? {
        val interval = replacementIntervalMonths ?: return null
        val last = lastReplacedDate ?: return null
        val nextDate = last.plusMonths(interval.toLong())
        return ChronoUnit.DAYS.between(today, nextDate)
    }

    /**
     * 종합 상태. 시간/날짜 기준 중 더 시급한 쪽을 따름.
     */
    fun status(currentHours: Double, today: LocalDate = LocalDate.now()): ConsumableStatus {
        val hoursLeft = hoursUntilReplacement(currentHours)
        val daysLeft = daysUntilReplacement(today)

        if (hoursLeft == null && daysLeft == null) return ConsumableStatus.UNKNOWN

        val statuses = listOfNotNull(
            hoursLeft?.let { hoursToStatus(it) },
            daysLeft?.let { daysToStatus(it) }
        )

        // 가장 시급한 상태 반환 (OVERDUE > DUE_SOON > NORMAL)
        return when {
            ConsumableStatus.OVERDUE in statuses -> ConsumableStatus.OVERDUE
            ConsumableStatus.DUE_SOON in statuses -> ConsumableStatus.DUE_SOON
            else -> ConsumableStatus.NORMAL
        }
    }

    /**
     * 표시용 텍스트 (예: "5h 남음", "20h 초과", "12일 남음").
     */
    fun remainingText(currentHours: Double, today: LocalDate = LocalDate.now()): String? {
        val hoursLeft = hoursUntilReplacement(currentHours)
        val daysLeft = daysUntilReplacement(today)

        return when {
            hoursLeft != null && daysLeft != null -> {
                // 더 시급한 쪽 표시
                if (hoursLeft <= daysLeft * 8) formatHours(hoursLeft)  // 하루 평균 8시간 가동 가정
                else formatDays(daysLeft)
            }
            hoursLeft != null -> formatHours(hoursLeft)
            daysLeft != null -> formatDays(daysLeft)
            else -> null
        }
    }

    private fun formatHours(h: Double): String {
        val rounded = h.toInt()
        return if (rounded < 0) "${-rounded}h 초과" else "${rounded}h 남음"
    }

    private fun formatDays(d: Long): String {
        return if (d < 0) "${-d}일 초과" else "${d}일 남음"
    }

    private fun hoursToStatus(h: Double): ConsumableStatus = when {
        h <= 0 -> ConsumableStatus.OVERDUE
        h <= 50 -> ConsumableStatus.DUE_SOON
        else -> ConsumableStatus.NORMAL
    }

    private fun daysToStatus(d: Long): ConsumableStatus = when {
        d <= 0 -> ConsumableStatus.OVERDUE
        d <= 30 -> ConsumableStatus.DUE_SOON
        else -> ConsumableStatus.NORMAL
    }
}

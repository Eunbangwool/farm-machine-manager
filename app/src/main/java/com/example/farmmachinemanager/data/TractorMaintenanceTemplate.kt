package com.example.farmmachinemanager.data

/**
 * 트랙터 표준 정비 일정 템플릿.
 *
 * TYM 트랙터 정비점검 일람표(매뉴얼)를 기반으로 구성.
 * 새 트랙터를 등록할 때 이 템플릿을 적용해 기본 소모품 목록을 자동 생성한다.
 *
 * 출처: TYM TRACTOR 정비점검 매뉴얼 (50~600시간 정기점검 일람표)
 *
 * 사용 예:
 * ```
 * val newTractorConsumables = TractorMaintenanceTemplate.defaultConsumables("machine_001")
 * ```
 *
 * 주의:
 * - 제조사/모델에 따라 주기가 다를 수 있으므로 등록 후 사용자가 조정 가능하도록 한다.
 * - 매뉴얼의 "△ = 교환" 표기를 기준으로 교체 주기를 정리.
 */
object TractorMaintenanceTemplate {

    /**
     * 트랙터용 표준 소모품 목록.
     * 사용자가 새 트랙터를 등록할 때 이 목록을 기본값으로 채워준다.
     * lastReplacedDate / lastReplacedHours는 사용자가 직접 입력.
     */
    fun defaultConsumables(machineId: String): List<Consumable> = listOf(
        Consumable(
            id = "${machineId}_engine_oil",
            machineId = machineId,
            name = "엔진오일",
            category = ConsumableCategory.ENGINE_OIL,
            replacementIntervalHours = 250.0,
            notes = "TYM 매뉴얼 기준 250시간 주기"
        ),
        Consumable(
            id = "${machineId}_engine_oil_filter",
            machineId = machineId,
            name = "엔진오일 필터",
            category = ConsumableCategory.ENGINE_OIL_FILTER,
            replacementIntervalHours = 300.0
        ),
        Consumable(
            id = "${machineId}_transmission_oil",
            machineId = machineId,
            name = "미션오일",
            category = ConsumableCategory.TRANSMISSION_OIL,
            replacementIntervalHours = 400.0,
            notes = "초기 50시간 후 교환, 이후 200~250시간 주기"
        ),
        Consumable(
            id = "${machineId}_transmission_oil_filter",
            machineId = machineId,
            name = "미션오일 필터",
            category = ConsumableCategory.TRANSMISSION_OIL_FILTER,
            replacementIntervalHours = 400.0
        ),
        Consumable(
            id = "${machineId}_axle_oil",
            machineId = machineId,
            name = "전차축 오일",
            category = ConsumableCategory.AXLE_OIL,
            replacementIntervalHours = 400.0
        ),
        Consumable(
            id = "${machineId}_air_cleaner",
            machineId = machineId,
            name = "에어크리너 엘리먼트",
            category = ConsumableCategory.AIR_FILTER,
            replacementIntervalHours = 600.0,
            notes = "50시간마다 점검, 상태에 따라 교환"
        ),
        Consumable(
            id = "${machineId}_fuel_filter",
            machineId = machineId,
            name = "연료필터",
            category = ConsumableCategory.FUEL_FILTER,
            replacementIntervalHours = 300.0,
            notes = "100시간마다 세척, 300시간마다 교환"
        ),
        Consumable(
            id = "${machineId}_coolant",
            machineId = machineId,
            name = "냉각수",
            category = ConsumableCategory.COOLANT,
            replacementIntervalMonths = 12,
            notes = "1년마다 교환 (일할때마다 점검)"
        ),
        Consumable(
            id = "${machineId}_radiator_hose",
            machineId = machineId,
            name = "라디에이터 호스",
            category = ConsumableCategory.HOSE,
            replacementIntervalMonths = 24
        ),
        Consumable(
            id = "${machineId}_hydraulic_hose",
            machineId = machineId,
            name = "유압부 호스",
            category = ConsumableCategory.HOSE,
            replacementIntervalMonths = 24,
            notes = "유압핸들 고압호스 포함"
        ),
        Consumable(
            id = "${machineId}_fuel_pipe_wiring",
            machineId = machineId,
            name = "연료파이프 · 전기배선",
            category = ConsumableCategory.OTHER,
            replacementIntervalMonths = 24
        )
    )

    /**
     * 정기점검 체크리스트 항목 (교환 아닌 점검/청소/보충 항목).
     * 추후 별도 화면에서 활용 예정.
     */
    val standardCheckpoints: List<CheckpointItem> = listOf(
        CheckpointItem("본네트 개폐방법", CheckpointAction.INSPECT, intervalHours = null),
        CheckpointItem("방진망 먼지막힘 청소", CheckpointAction.CLEAN, intervalHours = 50.0),
        CheckpointItem("냉각팬·라디에이터 청소", CheckpointAction.CLEAN, intervalHours = 50.0),
        CheckpointItem("배터리 액량 점검", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("배터리액 비중 점검 및 보충", CheckpointAction.REFILL, intervalHours = 50.0),
        CheckpointItem("연료파이프 및 결합부 점검", CheckpointAction.INSPECT, intervalHours = 50.0),
        CheckpointItem("그리스 주입", CheckpointAction.REFILL, intervalHours = 50.0),
        CheckpointItem("핸들 각부 조임 점검", CheckpointAction.INSPECT, intervalHours = 100.0),
        CheckpointItem("중요볼트·너트 점검", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("냉각 팬벨트 점검", CheckpointAction.INSPECT, intervalHours = 50.0),
        CheckpointItem("전기배선 점검", CheckpointAction.INSPECT, intervalHours = 50.0),
        CheckpointItem("엔진 크랭크 케이스 세척", CheckpointAction.CLEAN, intervalHours = 300.0),
        CheckpointItem("흡배기 밸브 틈새 점검", CheckpointAction.INSPECT, intervalHours = 600.0),
        CheckpointItem("연료분사 밸브 점검", CheckpointAction.INSPECT, intervalHours = 600.0)
    )
}

/**
 * 정기점검 체크리스트 항목 (소모품과 달리 "교환" 추적 안 함).
 */
data class CheckpointItem(
    val name: String,
    val action: CheckpointAction,
    val intervalHours: Double? = null,
    val intervalDays: Int? = null
)

enum class CheckpointAction(val displayName: String, val symbol: String) {
    INSPECT("점검", "○"),
    CLEAN("청소", "◎"),
    REFILL("보충", "☆")
}

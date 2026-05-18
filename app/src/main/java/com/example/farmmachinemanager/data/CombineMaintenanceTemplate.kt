package com.example.farmmachinemanager.data

/**
 * 콤바인 표준 정비 일정 템플릿.
 *
 * 국제 KC1200 콤바인 엔진 정기점검표(매뉴얼 제2장 4.2)를 기반으로 구성.
 * 새 콤바인을 등록할 때 이 템플릿을 적용해 기본 소모품 목록을 자동 생성.
 *
 * 출처: 국제 KC1200 콤바인 엔진 정기 점검표 (500~5,000시간 간격)
 *
 * 트랙터와의 주요 차이:
 * - 엔진오일/필터 교체 주기가 더 길다 (500h vs 트랙터 250h)
 * - 냉각수: 1,200시간 (트랙터는 12개월)
 * - 요소필터(SM): 콤바인 특유 - SCR(선택적 촉매 환원) 시스템의 DEF/AdBlue 필터
 * - 미션/전차축 오일은 별도 표에 있어 여기엔 미포함
 *
 * 주의:
 * - 본 표는 KC1200 기준. 다른 모델(쿠보타 DC-105, 얀마 AG6114 등)은 매뉴얼 참조 후 조정.
 * - 매뉴얼 주석:
 *   a. 필요 시 냉각수를 보충한다.
 *   b. 부동액 농도 유지를 위해 500시간마다 냉각수 점검.
 *   c. 엔진오일/필터는 최소 50시간 후 첫 교환, 이후 500시간마다.
 */
object CombineMaintenanceTemplate {

    /**
     * 콤바인용 표준 소모품 목록.
     * 사용자가 새 콤바인을 등록할 때 이 목록을 기본값으로 채워준다.
     */
    fun defaultConsumables(machineId: String): List<Consumable> = listOf(
        Consumable(
            id = "${machineId}_engine_oil",
            machineId = machineId,
            name = "엔진오일",
            category = ConsumableCategory.ENGINE_OIL,
            replacementIntervalHours = 500.0,
            notes = "첫 50시간 후 첫 교환, 이후 500시간마다"
        ),
        Consumable(
            id = "${machineId}_engine_oil_filter",
            machineId = machineId,
            name = "엔진오일 필터",
            category = ConsumableCategory.ENGINE_OIL_FILTER,
            replacementIntervalHours = 500.0,
            notes = "엔진오일과 함께 교환"
        ),
        Consumable(
            id = "${machineId}_fuel_filter",
            machineId = machineId,
            name = "연료필터",
            category = ConsumableCategory.FUEL_FILTER,
            replacementIntervalHours = 500.0
        ),
        Consumable(
            id = "${machineId}_coolant",
            machineId = machineId,
            name = "냉각수",
            category = ConsumableCategory.COOLANT,
            replacementIntervalHours = 1200.0,
            notes = "500시간마다 부동액 농도 점검"
        ),
        Consumable(
            id = "${machineId}_def_filter",
            machineId = machineId,
            name = "요소 필터(SM)",
            category = ConsumableCategory.DEF_FILTER,
            replacementIntervalHours = 3000.0,
            replacementIntervalMonths = 36,
            notes = "3,000시간 또는 36개월 중 먼저 도래 시점"
        ),
        Consumable(
            id = "${machineId}_air_cleaner",
            machineId = machineId,
            name = "에어 클리너",
            category = ConsumableCategory.AIR_FILTER,
            replacementIntervalHours = 1000.0,
            notes = "청소 후 필요 시 교체 (권장 1,000시간)"
        )
    )

    /**
     * 콤바인 엔진 정기점검 체크리스트.
     * 매일 점검 / 일정 시간마다 점검 항목.
     */
    val standardCheckpoints: List<CheckpointItem> = listOf(
        // === 냉각수 시스템 ===
        CheckpointItem("쿨러와 냉각수 호스 연결 상태", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("냉각 팬벨트 장력", CheckpointAction.INSPECT, intervalDays = 1),

        // === 윤활 시스템 ===
        CheckpointItem("윤활 장치 및 누유", CheckpointAction.INSPECT, intervalDays = 1),

        // === 연료 시스템 ===
        CheckpointItem("연료 라인과 연결 장치", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("연료량", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("수분 분리기에서 수분 배출", CheckpointAction.REFILL, intervalDays = 1),

        // === 흡기/배기 시스템 ===
        CheckpointItem("에어 클리너 청소", CheckpointAction.CLEAN, intervalDays = 1),
        CheckpointItem("스로틀 본체 청소", CheckpointAction.CLEAN, intervalDays = 1),
        CheckpointItem("흡기 라인 및 연결장치", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("배기 라인 및 연결장치", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("배기 가스 상태", CheckpointAction.INSPECT, intervalDays = 1),

        // === 실린더헤드 ===
        CheckpointItem("흡기/배기밸브 갭 상태", CheckpointAction.INSPECT, intervalHours = 1000.0),

        // === 전기 시스템 ===
        CheckpointItem("배터리 충전 상태", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("전기 장치", CheckpointAction.INSPECT, intervalDays = 1),

        // === 점검하고 필요 시 조정 (간격 미정) ===
        CheckpointItem("연료 분사 타이밍", CheckpointAction.INSPECT),
        CheckpointItem("인젝터", CheckpointAction.INSPECT),
        CheckpointItem("압축 압력", CheckpointAction.INSPECT)
    )
}

package com.example.farmmachinemanager.data

/**
 * 이앙기 표준 정비 일정 템플릿.
 *
 * 이앙기 NW6S/NW8S 매뉴얼 정기점검 일람표를 기반으로 구성.
 *
 * 출처: 이앙기 NW시리즈 매뉴얼 (작업 전후 ~ 2년 점검 일람표)
 *
 * 트랙터/콤바인과의 주요 차이:
 * - **계절성 장비**: 시즌 전후 점검 강조
 * - **첫 교환(▲)이 명시됨**: 미션오일 필터는 50시간째 길들이기 후 첫 교환
 * - **이앙기 특유 부품**: 식부 발톱(植付爪), 미션 구동 벨트, 라인 마커 등
 * - **시비부 옵션(F사양)**: 시비 브러시·호스·로트롤 등 별도 점검 항목
 * - **6S vs 8S 차이**: 엔진오일 필터 초기 교환이 35h(6S) / 50h(8S)
 *
 * 본 템플릿은 **8S 기준**. 6S 모델은 등록 후 사용자가 조정.
 *
 * 매뉴얼 주석:
 * - 표의 시간은 목안. 사용 조건/환경에 따라 교환 시기는 달라짐.
 * - 사용 시간은 메인 패널의 디지털 표시 참조.
 * - 외관에 균열/파손 보이면 정비공장에서 교환.
 */
object TransplanterMaintenanceTemplate {

    fun defaultConsumables(machineId: String): List<Consumable> = listOf(
        // === 엔진부 ===
        Consumable(
            id = "${machineId}_oil_filter_cartridge",
            machineId = machineId,
            name = "엔진오일 필터",
            category = ConsumableCategory.ENGINE_OIL_FILTER,
            replacementIntervalHours = 200.0,
            notes = "초회 50시간 후 첫 교환 (8S 기준, 6S는 35h), 이후 200h마다"
        ),
        Consumable(
            id = "${machineId}_fuel_filter",
            machineId = machineId,
            name = "연료필터",
            category = ConsumableCategory.FUEL_FILTER,
            replacementIntervalHours = 200.0,
            notes = "100시간마다 점검, 200시간마다 교환"
        ),
        Consumable(
            id = "${machineId}_alternator_belt",
            machineId = machineId,
            name = "알터네이터 구동 벨트",
            category = ConsumableCategory.BELT,
            replacementIntervalHours = 200.0,
            notes = "100시간마다 조정·점검"
        ),
        Consumable(
            id = "${machineId}_air_cleaner",
            machineId = machineId,
            name = "에어크리너 엘리먼트",
            category = ConsumableCategory.AIR_FILTER,
            replacementIntervalMonths = 12,
            notes = "100시간마다 청소, 1년 또는 6회 청소 후 교환"
        ),
        Consumable(
            id = "${machineId}_inlet_pipe",
            machineId = machineId,
            name = "인렛 파이프(에어크리너)",
            category = ConsumableCategory.HOSE,
            replacementIntervalMonths = 24,
            notes = "200시간마다 점검, 2년마다 교환"
        ),
        Consumable(
            id = "${machineId}_fuel_pipe",
            machineId = machineId,
            name = "연료 파이프",
            category = ConsumableCategory.HOSE,
            replacementIntervalMonths = 24,
            notes = "작업 전후 점검, 누유 시 즉시 교환"
        ),
        Consumable(
            id = "${machineId}_radiator_hose",
            machineId = machineId,
            name = "라디에이터 호스",
            category = ConsumableCategory.HOSE,
            replacementIntervalMonths = 24,
            notes = "작업 전후 점검, 누수 시 밴드 조임 또는 교환"
        ),
        Consumable(
            id = "${machineId}_bands",
            machineId = machineId,
            name = "각 밴드",
            category = ConsumableCategory.OTHER,
            replacementIntervalMonths = 24,
            notes = "호스 밴드·클램프류 일괄"
        ),

        // === 주행부 ===
        Consumable(
            id = "${machineId}_transmission_oil_filter",
            machineId = machineId,
            name = "미션오일 필터",
            category = ConsumableCategory.TRANSMISSION_OIL_FILTER,
            replacementIntervalHours = 100.0,
            notes = "길들이기 50시간 후 첫 교환, 이후 100h마다"
        ),
        Consumable(
            id = "${machineId}_transmission_belt",
            machineId = machineId,
            name = "미션 구동 벨트",
            category = ConsumableCategory.BELT,
            replacementIntervalHours = null,
            notes = "100시간마다 텐션 점검. 마모·균열 시 교환."
        ),

        // === 식부부 (이앙기 특유) ===
        Consumable(
            id = "${machineId}_planting_claws",
            machineId = machineId,
            name = "식부 발톱(植付爪)",
            category = ConsumableCategory.BLADE,
            replacementIntervalHours = null,
            notes = "3mm 이상 마모 시 교환. 작업 전후 점검."
        )
    )

    /**
     * 이앙기 정기점검 체크리스트.
     *
     * "시즌 전후"/"작업 전후" 항목이 많음 - 이앙기는 계절성 장비.
     */
    val standardCheckpoints: List<CheckpointItem> = listOf(
        // === 엔진부 ===
        CheckpointItem("인렛 매니폴드", CheckpointAction.INSPECT, intervalHours = 200.0),
        CheckpointItem("연료탱크 연료망 청소", CheckpointAction.CLEAN, intervalHours = 200.0),
        CheckpointItem("연료 분사관 청소", CheckpointAction.CLEAN, intervalHours = 200.0),
        CheckpointItem("연료 분사 노즐 개변압", CheckpointAction.INSPECT, intervalHours = 400.0),
        CheckpointItem("라디에이터 핀·네트 청소", CheckpointAction.CLEAN, intervalHours = 200.0),

        // === 주행부 ===
        CheckpointItem("타이어 마모 점검", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("에어 타이어 공기압", CheckpointAction.REFILL, intervalDays = 1),
        CheckpointItem("라인 마커", CheckpointAction.INSPECT, intervalDays = 1),

        // === 식부부 (이앙기 특유) ===
        CheckpointItem("밀어내기 금구", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("세로 송출 벨트", CheckpointAction.CLEAN, intervalDays = 1),
        CheckpointItem("활동판·묘 받침대", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("각 와이어", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("정지 로터 구동축", CheckpointAction.INSPECT, intervalHours = 100.0),

        // === 시비부 (F사양 옵션) ===
        CheckpointItem("시비 브러시", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("시비 호스", CheckpointAction.INSPECT),
        CheckpointItem("로트·롤 청소", CheckpointAction.CLEAN, intervalDays = 1),
        CheckpointItem("시비 구동부(원웨이)", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("블로워 덕트 흡입구 청소", CheckpointAction.CLEAN, intervalDays = 1),

        // === 전장부 ===
        CheckpointItem("배터리 충전 상태", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("와이어 하네스·배터리 코드", CheckpointAction.INSPECT, intervalDays = 1)
    )
}

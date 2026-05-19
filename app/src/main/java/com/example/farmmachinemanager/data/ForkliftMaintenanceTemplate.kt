package com.example.farmmachinemanager.data

/**
 * 지게차 표준 정비 일정 (CLARK D20/25/30/33-7, D24 디젤엔진 기준).
 *
 * 주기 분류:
 * - 250시간/월간: 엔진 오일 + 필터
 * - 500시간/3개월: 트랜스미션 오일 필터, 드라이브 액슬 오일, 연료 필터
 * - 1000시간/6개월: 트랜스미션 오일, 유압 리턴 필터, 에어 클리너 (1차/2차)
 * - 2000시간/연간: 냉각수
 */
object ForkliftMaintenanceTemplate {

    /**
     * 새 지게차 등록 시 자동 생성되는 소모품 목록.
     */
    fun defaultConsumables(machineId: String): List<Consumable> = listOf(
        // ===== 250시간 / 1개월 =====
        Consumable(
            id = "${machineId}_engine_oil",
            machineId = machineId,
            name = "엔진 오일",
            category = ConsumableCategory.ENGINE_OIL,
            replacementIntervalHours = 250.0,
            replacementIntervalMonths = 1,
            notes = "D24 디젤 용량 9.2L (최대 8.6L / 최소 4.5L). 배출 플러그 토크 29.4N·m"
        ),
        Consumable(
            id = "${machineId}_engine_oil_filter",
            machineId = machineId,
            name = "엔진 오일 필터",
            category = ConsumableCategory.ENGINE_OIL_FILTER,
            replacementIntervalHours = 250.0,
            replacementIntervalMonths = 1,
            notes = "오일 필터 캡 110910-00628"
        ),

        // ===== 500시간 / 3개월 =====
        Consumable(
            id = "${machineId}_transmission_oil_filter",
            machineId = machineId,
            name = "트랜스미션 오일 필터",
            category = ConsumableCategory.TRANSMISSION_OIL_FILTER,
            replacementIntervalHours = 500.0,
            replacementIntervalMonths = 3,
            notes = "오일 레벨 점검: 40°C(저온측), 80°C(고온측)"
        ),
        Consumable(
            id = "${machineId}_drive_axle_oil",
            machineId = machineId,
            name = "드라이브 액슬 오일",
            category = ConsumableCategory.AXLE_OIL,
            replacementIntervalHours = 500.0,
            replacementIntervalMonths = 3,
            notes = "오일 및 스트레이너 점검·교환"
        ),
        Consumable(
            id = "${machineId}_fuel_filter",
            machineId = machineId,
            name = "연료 여과기",
            category = ConsumableCategory.FUEL_FILTER,
            replacementIntervalHours = 500.0,
            replacementIntervalMonths = 3
        ),

        // ===== 1000시간 / 6개월 =====
        Consumable(
            id = "${machineId}_transmission_oil",
            machineId = machineId,
            name = "트랜스미션 오일",
            category = ConsumableCategory.TRANSMISSION_OIL,
            replacementIntervalHours = 1000.0,
            replacementIntervalMonths = 6,
            notes = "오일·필터·스트레이너 점검, 청소, 교환"
        ),
        Consumable(
            id = "${machineId}_hydraulic_filter",
            machineId = machineId,
            name = "유압 리턴 필터",
            category = ConsumableCategory.HYDRAULIC_OIL,
            replacementIntervalHours = 1000.0,
            replacementIntervalMonths = 6,
            notes = "유압 탱크의 리턴 필터 교체"
        ),
        Consumable(
            id = "${machineId}_air_filter",
            machineId = machineId,
            name = "에어 클리너 엘리먼트",
            category = ConsumableCategory.AIR_FILTER,
            replacementIntervalHours = 1000.0,
            replacementIntervalMonths = 6,
            notes = "1차/2차 엘리먼트 모두 교환. 청소 시 압축공기 2.05bar 이하"
        ),

        // ===== 2000시간 / 1년 =====
        Consumable(
            id = "${machineId}_coolant",
            machineId = machineId,
            name = "냉각수",
            category = ConsumableCategory.COOLANT,
            replacementIntervalHours = 2000.0,
            replacementIntervalMonths = 12
        )
    )

    /**
     * 일일/주기 점검표.
     * intervalDays=1 → "매일 점검" 그룹
     * intervalHours 지정 → "주기 점검" 그룹
     */
    val standardCheckpoints: List<CheckpointItem> = listOf(
        // ===== 일일 점검 (운전자) =====
        CheckpointItem("느슨해진 부품 유무 확인", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("계기판 표시등 이상유무", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("경적 및 경고장치 작동 확인", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("마스트·리프트 체인 마모 및 핀/롤러", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("캐리지·포크·어태치먼트 손상", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("타이어·밸브·휠 이상유무", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("오버헤드 가드 손상", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("유압 시스템 누수 및 손상", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("엔진실 윤활유·냉각수·연료 누수", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("냉각계통 누수, 호스 마모", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("드라이브 액슬 오일 누수", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("엔진 오일·냉각수 레벨", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("에어 클리너 서비스 지시기 (적색 밴드)", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("배기가스 누출 검사", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("마스트 롤러 빔에 윤활유 도포", CheckpointAction.REFILL, intervalDays = 1),
        CheckpointItem("트랜스미션 오일 레벨", CheckpointAction.INSPECT, intervalDays = 1),
        CheckpointItem("주차 브레이크 작동 확인", CheckpointAction.INSPECT, intervalDays = 1),

        // ===== 250시간 주기 =====
        CheckpointItem("프리 클리너 오염 점검·세척", CheckpointAction.CLEAN, intervalHours = 250.0),
        CheckpointItem("스티어링 액슬 킹핀·링크 베어링 주유 (8개소)", CheckpointAction.REFILL, intervalHours = 250.0),
        CheckpointItem("배터리 단자 부식 점검·청소", CheckpointAction.CLEAN, intervalHours = 250.0),
        CheckpointItem("휠 볼트·너트 토크 점검 (스티어 110N·m / 드라이브 610N·m)", CheckpointAction.INSPECT, intervalHours = 250.0),

        // ===== 500시간 주기 =====
        CheckpointItem("발전기 구동벨트 점검·조정 (처짐 10mm/110N)", CheckpointAction.INSPECT, intervalHours = 500.0),
        CheckpointItem("틸트 실린더 로드 연장길이 측정 (3.18mm 이내)", CheckpointAction.INSPECT, intervalHours = 500.0),
        CheckpointItem("마스트 힌지 핀 피팅 주유", CheckpointAction.REFILL, intervalHours = 500.0),
        CheckpointItem("스티어링 액슬 피팅 주유 (8개)", CheckpointAction.REFILL, intervalHours = 500.0),
        CheckpointItem("오버헤드가드 손상·볼트 점검 (95N·m)", CheckpointAction.INSPECT, intervalHours = 500.0),
        CheckpointItem("주차 브레이크 시험·조정", CheckpointAction.INSPECT, intervalHours = 500.0),

        // ===== 1000시간 주기 =====
        CheckpointItem("리프트 체인 마모 시험 (2% 이상이면 교체)", CheckpointAction.INSPECT, intervalHours = 1000.0),
        CheckpointItem("유니버설 조인트 베어링 마모 점검", CheckpointAction.INSPECT, intervalHours = 1000.0),
        CheckpointItem("엔진 밸브 래쉬 점검·조정 (배기/흡기 0.2mm)", CheckpointAction.INSPECT, intervalHours = 1000.0),
        CheckpointItem("에어 브리더 교체", CheckpointAction.INSPECT, intervalHours = 1000.0),

        // ===== 2000시간 주기 =====
        CheckpointItem("스티어 휠 베어링 재조립 (잠금 너트 135N·m)", CheckpointAction.INSPECT, intervalHours = 2000.0),
        CheckpointItem("드라이브 휠 베어링 재조립 (644±34N·m)", CheckpointAction.INSPECT, intervalHours = 2000.0),
        CheckpointItem("포크 정기 점검 (균열·마모 90% 이하·진직도)", CheckpointAction.INSPECT, intervalHours = 2000.0),

        // ===== 2500시간 주기 =====
        CheckpointItem("유압 탱크 드레인 플러그 청소", CheckpointAction.CLEAN, intervalHours = 2500.0),
        CheckpointItem("배터리 외부 표면 청소·점검", CheckpointAction.CLEAN, intervalHours = 2500.0)
    )
}

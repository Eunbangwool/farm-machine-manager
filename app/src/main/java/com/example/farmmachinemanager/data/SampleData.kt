package com.example.farmmachinemanager.data

import java.time.LocalDate

/**
 * 개발/미리보기용 샘플 데이터.
 *
 * 실제 운용 시에는 Firebase Firestore에서 가져옵니다.
 *
 * 기계별 적용된 매뉴얼:
 * - DK7320 (트랙터): TYM 매뉴얼 기준 정비 일정
 * - DC-105 (콤바인): 국제 KC1200 매뉴얼 기준 정비 일정
 */
object SampleData {

    val machines: List<Machine> = listOf(
        Machine(
            id = "1",
            name = "DK7320",
            manufacturer = "대동",
            type = MachineType.TRACTOR,
            horsepower = 73,
            year = 2021,
            serialNumber = "23A-DK7320-001847",
            registrationNumber = "충북 12가 3456",
            operatingHours = 1245.0,
            status = MachineStatus.UNDER_REPAIR,
            statusNote = "3일 전"
        ),
        Machine(
            id = "2",
            name = "DC-105",
            manufacturer = "구보타",
            type = MachineType.COMBINE,
            horsepower = 105,
            year = 2022,
            serialNumber = "KBT-DC105-2022-0033",
            operatingHours = 540.0,         // 500h 정기교환 시기 지남
            status = MachineStatus.INSPECTION_NEEDED,
            statusNote = "연료필터 초과"
        ),
        Machine(
            id = "3",
            name = "MT3.50",
            manufacturer = "LS엠트론",
            type = MachineType.TRACTOR,
            horsepower = 50,
            year = 2018,
            operatingHours = 2890.0,
            status = MachineStatus.NORMAL,
            statusNote = "다음 점검 12일"
        ),
        Machine(
            id = "4",
            name = "KC1200",
            manufacturer = "국제",
            type = MachineType.COMBINE,
            horsepower = 120,
            year = 2023,
            serialNumber = "KOOK-KC1200-2023-021",
            operatingHours = 156.0,
            status = MachineStatus.NORMAL,
            statusNote = "최근 점검 완료"
        ),
        Machine(
            id = "5",
            name = "DK5510",
            manufacturer = "대동",
            type = MachineType.TRACTOR,
            horsepower = 55,
            year = 2016,
            operatingHours = 3420.0,
            status = MachineStatus.NORMAL
        ),
        Machine(
            id = "6",
            name = "NW8S",
            manufacturer = "얀마",
            type = MachineType.RICE_TRANSPLANTER,
            horsepower = 17,
            year = 2022,
            serialNumber = "YNM-NW8S-2022-0142",
            registrationNumber = null,         // 이앙기는 등록번호 없는 경우 많음
            operatingHours = 125.0,             // 계절성 장비, 누적 시간 적음
            status = MachineStatus.INSPECTION_NEEDED,
            statusNote = "시즌 점검 필요"
        )
    )

    // ============ 정비 기록 ============
    val maintenanceRecords: List<MaintenanceRecord> = listOf(
        // DK7320 트랙터 (id=1)
        MaintenanceRecord(
            id = "m1",
            machineId = "1",
            date = LocalDate.now().minusDays(3),
            type = MaintenanceType.REPAIR,
            title = "변속기 수리",
            description = "변속이 잘 안 됨 - 클러치 점검 필요",
            shopName = "농기계상사",
            performedBy = "김기사",
            operatingHoursAtMaintenance = 1245.0,
            isInProgress = true
        ),
        MaintenanceRecord(
            id = "m2",
            machineId = "1",
            date = LocalDate.of(2026, 3, 2),
            type = MaintenanceType.REGULAR_CHECK,
            title = "정기점검",
            description = "1000시간 정기점검",
            cost = 120_000,
            shopName = "농기계상사",
            operatingHoursAtMaintenance = 1180.0
        ),
        MaintenanceRecord(
            id = "m3",
            machineId = "1",
            date = LocalDate.of(2026, 1, 18),
            type = MaintenanceType.CONSUMABLE_REPLACE,
            title = "엔진오일 · 필터 교체",
            cost = 75_000,
            shopName = "농기계상사",
            operatingHoursAtMaintenance = 1000.0,
            replacedConsumableIds = listOf("1_engine_oil", "1_engine_oil_filter")
        ),
        MaintenanceRecord(
            id = "m4",
            machineId = "1",
            date = LocalDate.of(2025, 10, 25),
            type = MaintenanceType.REGULAR_CHECK,
            title = "정기점검",
            cost = 95_000,
            shopName = "농기계상사",
            operatingHoursAtMaintenance = 850.0
        ),
        // DC-105 콤바인 (id=2)
        MaintenanceRecord(
            id = "m5",
            machineId = "2",
            date = LocalDate.of(2025, 11, 5),
            type = MaintenanceType.REGULAR_CHECK,
            title = "수확 후 정기점검",
            cost = 180_000,
            shopName = "구보타 서비스센터",
            operatingHoursAtMaintenance = 350.0
        ),
        MaintenanceRecord(
            id = "m6",
            machineId = "2",
            date = LocalDate.of(2025, 8, 15),
            type = MaintenanceType.CONSUMABLE_REPLACE,
            title = "엔진오일·필터 첫 교환 (50h)",
            description = "신차 50시간 후 첫 교환",
            cost = 130_000,
            shopName = "구보타 서비스센터",
            operatingHoursAtMaintenance = 52.0,
            replacedConsumableIds = listOf("2_engine_oil", "2_engine_oil_filter")
        ),
        // NW8S 이앙기 (id=6) - 작년 시즌 후 50h 길들이기 교환만 한 상태
        MaintenanceRecord(
            id = "m7",
            machineId = "6",
            date = LocalDate.of(2025, 5, 10),
            type = MaintenanceType.CONSUMABLE_REPLACE,
            title = "미션오일 필터·엔진오일 필터 초회 교환",
            description = "길들이기(ならし) 50시간 후 첫 교환",
            cost = 95_000,
            shopName = "얀마 서비스센터",
            operatingHoursAtMaintenance = 50.0,
            replacedConsumableIds = listOf(
                "6_transmission_oil_filter",
                "6_oil_filter_cartridge",
                "6_fuel_filter"
            )
        )
    )

    // ============ 소모품 ============
    val consumables: List<Consumable> = run {
        // === DK7320 트랙터 (TYM 매뉴얼 적용, 1,245시간 운용 중) ===
        val dk7320 = TractorMaintenanceTemplate.defaultConsumables("1").map { template ->
            when (template.id) {
                "1_engine_oil" -> template.copy(
                    lastReplacedHours = 1000.0,
                    lastReplacedDate = LocalDate.of(2026, 1, 18)
                ) // 1000 + 250 = 1250 → 5h 남음 (임박)
                "1_engine_oil_filter" -> template.copy(
                    lastReplacedHours = 950.0,
                    lastReplacedDate = LocalDate.of(2025, 12, 20)
                )
                "1_transmission_oil" -> template.copy(
                    lastReplacedHours = 880.0,
                    lastReplacedDate = LocalDate.of(2025, 10, 15)
                )
                "1_transmission_oil_filter" -> template.copy(
                    lastReplacedHours = 880.0,
                    lastReplacedDate = LocalDate.of(2025, 10, 15)
                )
                "1_axle_oil" -> template.copy(
                    lastReplacedHours = 900.0,
                    lastReplacedDate = LocalDate.of(2025, 11, 1)
                )
                "1_air_cleaner" -> template.copy(
                    lastReplacedHours = 625.0,
                    lastReplacedDate = LocalDate.of(2025, 5, 10)
                ) // 625 + 600 = 1225 → -20h (초과!)
                "1_fuel_filter" -> template.copy(
                    lastReplacedHours = 970.0,
                    lastReplacedDate = LocalDate.of(2026, 1, 5)
                )
                "1_coolant" -> template.copy(
                    lastReplacedDate = LocalDate.of(2025, 8, 10)
                )
                "1_radiator_hose" -> template.copy(
                    lastReplacedDate = LocalDate.of(2024, 6, 1)
                )
                else -> template
            }
        }

        // === DC-105 콤바인 (국제 KC1200 매뉴얼 적용, 487시간 운용 중) ===
        // 신차 50h 첫 교환만 한 상태. 다음 500h 정기교환 임박.
        val dc105 = CombineMaintenanceTemplate.defaultConsumables("2").map { template ->
            when (template.id) {
                "2_engine_oil" -> template.copy(
                    lastReplacedHours = 52.0,
                    lastReplacedDate = LocalDate.of(2025, 8, 15)
                ) // 52 + 500 = 552 → 65h 남음. Hmm 정상. 더 조이게:
                "2_engine_oil_filter" -> template.copy(
                    lastReplacedHours = 52.0,
                    lastReplacedDate = LocalDate.of(2025, 8, 15)
                )
                "2_fuel_filter" -> template.copy(
                    lastReplacedHours = 0.0,
                    lastReplacedDate = LocalDate.of(2025, 6, 10)
                ) // 0 + 500 = 500 → 13h 남음 (임박)
                "2_coolant" -> template.copy(
                    lastReplacedHours = 0.0,
                    lastReplacedDate = LocalDate.of(2025, 6, 10)
                ) // 0 + 1200 = 1200 → 713h 남음 (정상)
                "2_def_filter" -> template.copy(
                    lastReplacedHours = 0.0,
                    lastReplacedDate = LocalDate.of(2025, 6, 10)
                ) // 새 콤바인 - 첫 교환 아직 안 함
                else -> template
            }
        }

        // === KC1200 콤바인 (id=4, 신차 156h - 50h 첫 교환만 한 상태) ===
        val kc1200 = CombineMaintenanceTemplate.defaultConsumables("4").map { template ->
            when (template.id) {
                "4_engine_oil" -> template.copy(
                    lastReplacedHours = 52.0,
                    lastReplacedDate = LocalDate.of(2026, 2, 1)
                )
                "4_engine_oil_filter" -> template.copy(
                    lastReplacedHours = 52.0,
                    lastReplacedDate = LocalDate.of(2026, 2, 1)
                )
                else -> template.copy(
                    lastReplacedHours = 0.0,
                    lastReplacedDate = LocalDate.of(2025, 12, 1)
                )
            }
        }

        // === NW8S 이앙기 (id=6, 125h, 작년 시즌 후 50h 교환) ===
        // 시연: 에어크리너 1년 초과 + 미션오일 필터 임박
        val nw8s = TransplanterMaintenanceTemplate.defaultConsumables("6").map { template ->
            when (template.id) {
                "6_transmission_oil_filter" -> template.copy(
                    lastReplacedHours = 50.0,
                    lastReplacedDate = LocalDate.of(2025, 5, 10)
                ) // 50 + 100 = 150 → 25h 남음 (임박)
                "6_oil_filter_cartridge" -> template.copy(
                    lastReplacedHours = 50.0,
                    lastReplacedDate = LocalDate.of(2025, 5, 10)
                ) // 50 + 200 = 250 → 125h 남음 (정상)
                "6_fuel_filter" -> template.copy(
                    lastReplacedHours = 50.0,
                    lastReplacedDate = LocalDate.of(2025, 5, 10)
                ) // 정상
                "6_alternator_belt" -> template.copy(
                    lastReplacedHours = 0.0,
                    lastReplacedDate = LocalDate.of(2024, 4, 1)
                ) // 0 + 200 = 200 → 75h 남음 (정상)
                "6_air_cleaner" -> template.copy(
                    lastReplacedDate = LocalDate.of(2025, 5, 10)
                ) // 2025/5/10 + 12개월 = 2026/5/10 → 오늘(5/18) 기준 8일 초과
                "6_inlet_pipe" -> template.copy(
                    lastReplacedDate = LocalDate.of(2024, 4, 1)
                ) // 2024/4 + 24개월 = 2026/4 → 약 47일 초과
                "6_radiator_hose" -> template.copy(
                    lastReplacedDate = LocalDate.of(2024, 4, 1)
                ) // 2024/4 + 24개월 = 2026/4 → 약간 초과
                "6_fuel_pipe" -> template.copy(
                    lastReplacedDate = LocalDate.of(2024, 4, 1)
                )
                "6_bands" -> template.copy(
                    lastReplacedDate = LocalDate.of(2024, 4, 1)
                )
                // 식부 발톱, 미션 벨트는 마모 기준 → 기본값 유지 (UNKNOWN)
                else -> template
            }
        }

        dk7320 + dc105 + kc1200 + nw8s
    }

    fun maintenanceFor(machineId: String): List<MaintenanceRecord> =
        maintenanceRecords
            .filter { it.machineId == machineId }
            .sortedByDescending { it.date }

    fun consumablesFor(machineId: String): List<Consumable> =
        consumables.filter { it.machineId == machineId }
}

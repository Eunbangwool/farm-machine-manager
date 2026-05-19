package com.example.farmmachinemanager.data

/**
 * 기계 종류에 맞는 표준 정비 일정 템플릿을 디스패치.
 *
 * 사용 예 (기계 등록 시):
 * ```
 * val newMachine = Machine(id="m_007", type=MachineType.COMBINE, ...)
 * val starterConsumables = MaintenanceTemplates.defaultConsumables(newMachine.id, newMachine.type)
 * // → 콤바인 표준 정비 일정이 자동 적용됨
 * ```
 *
 * 지원 모델:
 * - 트랙터 (TYM 표준): TractorMaintenanceTemplate
 * - 콤바인 (국제 KC1200 표준): CombineMaintenanceTemplate
 * - 이앙기 (NW시리즈 표준): TransplanterMaintenanceTemplate
 * - 관리기/차량 등: 추후 추가 예정
 */
object MaintenanceTemplates {

    /**
     * 기본 소모품 목록을 반환. 지원되지 않는 종류는 빈 목록.
     */
    fun defaultConsumables(machineId: String, type: MachineType): List<Consumable> = when (type) {
        MachineType.TRACTOR -> TractorMaintenanceTemplate.defaultConsumables(machineId)
        MachineType.COMBINE -> CombineMaintenanceTemplate.defaultConsumables(machineId)
        MachineType.RICE_TRANSPLANTER -> TransplanterMaintenanceTemplate.defaultConsumables(machineId)
        MachineType.CULTIVATOR,
        MachineType.FORKLIFT,
        MachineType.VEHICLE,
        MachineType.OTHER -> emptyList()
    }

    /**
     * 정기점검 체크리스트 항목을 반환.
     */
    fun defaultCheckpoints(type: MachineType): List<CheckpointItem> = when (type) {
        MachineType.TRACTOR -> TractorMaintenanceTemplate.standardCheckpoints
        MachineType.COMBINE -> CombineMaintenanceTemplate.standardCheckpoints
        MachineType.RICE_TRANSPLANTER -> TransplanterMaintenanceTemplate.standardCheckpoints
        else -> emptyList()
    }

    /**
     * 템플릿의 출처를 사용자에게 보여줄 때 사용.
     */
    fun sourceLabel(type: MachineType): String? = when (type) {
        MachineType.TRACTOR -> "TYM 트랙터 매뉴얼 기준"
        MachineType.COMBINE -> "국제 KC1200 콤바인 매뉴얼 기준"
        MachineType.RICE_TRANSPLANTER -> "이앙기 NW시리즈 매뉴얼 기준 (8S)"
        else -> null
    }
}

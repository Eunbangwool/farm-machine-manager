package com.example.farmmachinemanager.data.repository

import com.example.farmmachinemanager.data.Consumable
import com.example.farmmachinemanager.data.Machine
import com.example.farmmachinemanager.data.MachineType
import com.example.farmmachinemanager.data.MaintenanceRecord
import kotlinx.coroutines.flow.Flow

/**
 * 기계/정비/소모품 데이터 저장소 인터페이스.
 *
 * 구현체:
 * - SampleRepository: 개발용 더미 데이터 (현재 사용 중)
 * - FirestoreRepository: Firebase Firestore 백엔드 (FIREBASE_SETUP.md 참조)
 *
 * 화면 코드는 이 인터페이스만 사용하므로 백엔드를 갈아끼워도 화면은 변경 불필요.
 */
interface MachineRepository {
    fun observeMachines(): Flow<List<Machine>>
    suspend fun getMachine(id: String): Machine?
    suspend fun saveMachine(machine: Machine)
    suspend fun deleteMachine(id: String)
}

interface MaintenanceRepository {
    fun observeMaintenanceFor(machineId: String): Flow<List<MaintenanceRecord>>
    suspend fun addMaintenance(record: MaintenanceRecord)
    suspend fun updateMaintenance(record: MaintenanceRecord)
    suspend fun deleteMaintenance(id: String)
}

interface ConsumableRepository {
    fun observeConsumablesFor(machineId: String): Flow<List<Consumable>>
    suspend fun saveConsumable(consumable: Consumable)
    suspend fun deleteConsumable(id: String)
    /**
     * 기계가 새로 등록될 때 표준 정비 일정 자동 적용.
     * 기계 종류에 맞는 템플릿이 자동 선택됨:
     * - 트랙터: TYM 매뉴얼 기준
     * - 콤바인: 국제 KC1200 매뉴얼 기준
     * - 그 외: 빈 목록 (사용자가 직접 추가)
     */
    suspend fun applyStandardTemplate(machineId: String, machineType: MachineType)
}

package com.example.farmmachinemanager.data.repository

import com.example.farmmachinemanager.data.Consumable
import com.example.farmmachinemanager.data.Machine
import com.example.farmmachinemanager.data.MachineType
import com.example.farmmachinemanager.data.MaintenanceRecord
import com.example.farmmachinemanager.data.MaintenanceTemplates
import com.example.farmmachinemanager.data.SampleData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow

/**
 * 메모리 기반 Repository 구현.
 *
 * 앱이 종료되면 변경 내용이 사라집니다. 개발 및 디자인 미리보기 용도.
 * 실 운용 시에는 FirestoreRepository로 교체.
 */
class SampleMachineRepository : MachineRepository {
    private val state = MutableStateFlow(SampleData.machines)

    override fun observeMachines(): Flow<List<Machine>> = state.asStateFlow()

    override suspend fun getMachine(id: String): Machine? =
        state.value.find { it.id == id }

    override suspend fun saveMachine(machine: Machine) {
        state.value = state.value
            .filter { it.id != machine.id }
            .plus(machine)
    }

    override suspend fun deleteMachine(id: String) {
        state.value = state.value.filter { it.id != id }
    }
}

class SampleMaintenanceRepository : MaintenanceRepository {
    private val state = MutableStateFlow(SampleData.maintenanceRecords)

    override fun observeMaintenanceFor(machineId: String): Flow<List<MaintenanceRecord>> =
        state.map { records ->
            records.filter { it.machineId == machineId }
                .sortedByDescending { it.date }
        }

    override suspend fun addMaintenance(record: MaintenanceRecord) {
        state.value = state.value + record
    }

    override suspend fun updateMaintenance(record: MaintenanceRecord) {
        state.value = state.value.map { if (it.id == record.id) record else it }
    }

    override suspend fun deleteMaintenance(id: String) {
        state.value = state.value.filter { it.id != id }
    }
}

class SampleConsumableRepository : ConsumableRepository {
    private val state = MutableStateFlow(SampleData.consumables)

    override fun observeConsumablesFor(machineId: String): Flow<List<Consumable>> =
        state.map { items -> items.filter { it.machineId == machineId } }

    override suspend fun saveConsumable(consumable: Consumable) {
        state.value = state.value
            .filter { it.id != consumable.id }
            .plus(consumable)
    }

    override suspend fun deleteConsumable(id: String) {
        state.value = state.value.filter { it.id != id }
    }

    override suspend fun applyStandardTemplate(machineId: String, machineType: MachineType) {
        val existing = state.value.filter { it.machineId == machineId }.map { it.id }.toSet()
        val newItems = MaintenanceTemplates.defaultConsumables(machineId, machineType)
            .filter { it.id !in existing }
        state.value = state.value + newItems
    }
}

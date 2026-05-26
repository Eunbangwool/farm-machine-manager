package com.example.farmmachinemanager.data.repository

import com.example.farmmachinemanager.data.Machine
import com.example.farmmachinemanager.data.MachineStatus
import com.example.farmmachinemanager.data.MachineType
import com.example.farmmachinemanager.data.MaintenanceRecord
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

/**
 * Firestore 기반 Machine Repository.
 *
 * 컬렉션 구조: farms/{farmCode}/machines/{machineId}
 *
 * - observeMachines(): Firestore snapshot listener로 실시간 변경 감지.
 *   다른 폰에서 저장한 변경이 즉시 반영됨.
 * - 오프라인 캐시는 Firestore SDK가 자동 처리 (기본 활성화).
 *
 * LocalDate, Enum은 Firestore가 직접 지원 안 하므로 Map ↔ Object 수동 변환.
 */
class FirestoreMachineRepository(
    farmCode: String,
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : MachineRepository {

    private val collection = db
        .collection("farms")
        .document(farmCode)
        .collection("machines")

    override fun observeMachines(): Flow<List<Machine>> = callbackFlow {
        val registration = collection.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            if (error != null) {
                // 권한 오류·네트워크 오류 등은 마지막으로 본 데이터를 유지한다.
                // 빈 리스트를 emit 하면 UI 가 깜빡거린다.
                return@addSnapshotListener
            }
            val machines = snapshot?.documents
                ?.mapNotNull { doc -> doc.data?.let { mapToMachine(doc.id, it) } }
                ?: emptyList()
            trySend(machines)
        }
        awaitClose { registration.remove() }
    }

    override suspend fun getMachine(id: String): Machine? {
        val doc = collection.document(id).get().await()
        return doc.data?.let { mapToMachine(doc.id, it) }
    }

    override suspend fun saveMachine(machine: Machine) {
        collection.document(machine.id).set(machineToMap(machine)).await()
    }

    override suspend fun deleteMachine(id: String) {
        collection.document(id).delete().await()
    }

    // ============ 변환 함수 (LocalDate, Enum 처리) ============

    private fun machineToMap(m: Machine): Map<String, Any?> = mapOf(
        "id" to m.id,
        "name" to m.name,
        "manufacturer" to m.manufacturer,
        "type" to m.type.name,
        "customTypeName" to m.customTypeName,
        "horsepower" to m.horsepower,
        "serialNumber" to m.serialNumber,
        "registrationNumber" to m.registrationNumber,
        "year" to m.year,
        "operatingHours" to m.operatingHours,
        "status" to m.status.name,
        "statusNote" to m.statusNote,
        "lastMaintenanceDate" to m.lastMaintenanceDate?.toString(),
        "photoUrl" to m.photoUrl,
        "notes" to m.notes
    )

    private fun mapToMachine(id: String, data: Map<String, Any?>): Machine? = try {
        Machine(
            id = id,
            name = data["name"] as? String ?: "",
            manufacturer = data["manufacturer"] as? String ?: "",
            type = (data["type"] as? String)
                ?.let { runCatching { MachineType.valueOf(it) }.getOrNull() }
                ?: MachineType.OTHER,
            customTypeName = data["customTypeName"] as? String,
            horsepower = (data["horsepower"] as? Number)?.toInt(),
            serialNumber = data["serialNumber"] as? String,
            registrationNumber = data["registrationNumber"] as? String,
            year = (data["year"] as? Number)?.toInt(),
            operatingHours = (data["operatingHours"] as? Number)?.toDouble() ?: 0.0,
            status = (data["status"] as? String)
                ?.let { runCatching { MachineStatus.valueOf(it) }.getOrNull() }
                ?: MachineStatus.NORMAL,
            statusNote = data["statusNote"] as? String,
            lastMaintenanceDate = (data["lastMaintenanceDate"] as? String)
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
            photoUrl = data["photoUrl"] as? String,
            notes = data["notes"] as? String
        )
    } catch (e: Exception) {
        null
    }
}

// ============================================================================
// Maintenance Repository (Firestore 기반)
// ============================================================================

/**
 * 컬렉션 구조: farms/{farmCode}/maintenance/{recordId}
 * machineId 필드로 필터링하여 특정 기계의 정비 기록만 조회.
 */
class FirestoreMaintenanceRepository(
    farmCode: String,
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : MaintenanceRepository {

    private val collection = db
        .collection("farms")
        .document(farmCode)
        .collection("maintenance")

    override fun observeMaintenanceFor(machineId: String): Flow<List<MaintenanceRecord>> = callbackFlow {
        val registration = collection
            .whereEqualTo("machineId", machineId)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    // 마지막 데이터 유지
                    return@addSnapshotListener
                }
                val records = snapshot?.documents
                    ?.mapNotNull { doc -> doc.data?.let { recordFromMap(doc.id, it) } }
                    ?.sortedByDescending { it.date }
                    ?: emptyList()
                trySend(records)
            }
        awaitClose { registration.remove() }
    }

    override fun observeAllMaintenance(): Flow<List<MaintenanceRecord>> = callbackFlow {
        val registration = collection.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            if (error != null) {
                // 마지막 데이터 유지
                return@addSnapshotListener
            }
            val records = snapshot?.documents
                ?.mapNotNull { doc -> doc.data?.let { recordFromMap(doc.id, it) } }
                ?.sortedByDescending { it.date }
                ?: emptyList()
            trySend(records)
        }
        awaitClose { registration.remove() }
    }

    override suspend fun addMaintenance(record: MaintenanceRecord) {
        collection.document(record.id).set(recordToMap(record)).await()
    }

    override suspend fun updateMaintenance(record: MaintenanceRecord) {
        collection.document(record.id).set(recordToMap(record)).await()
    }

    override suspend fun deleteMaintenance(id: String) {
        collection.document(id).delete().await()
    }

    private fun recordToMap(r: MaintenanceRecord): Map<String, Any?> = mapOf(
        "id" to r.id,
        "machineId" to r.machineId,
        "date" to r.date.toString(),
        "type" to r.type.name,
        "title" to r.title,
        "description" to r.description,
        "cost" to r.cost,
        "performedBy" to r.performedBy,
        "shopName" to r.shopName,
        "operatingHoursAtMaintenance" to r.operatingHoursAtMaintenance,
        "photoUrls" to r.photoUrls,
        "replacedConsumableIds" to r.replacedConsumableIds,
        "isInProgress" to r.isInProgress
    )

    @Suppress("UNCHECKED_CAST")
    private fun recordFromMap(id: String, data: Map<String, Any?>): MaintenanceRecord? = try {
        MaintenanceRecord(
            id = id,
            machineId = data["machineId"] as? String ?: "",
            date = (data["date"] as? String)
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                ?: LocalDate.now(),
            type = (data["type"] as? String)
                ?.let { runCatching { com.example.farmmachinemanager.data.MaintenanceType.valueOf(it) }.getOrNull() }
                ?: com.example.farmmachinemanager.data.MaintenanceType.OTHER,
            title = data["title"] as? String ?: "",
            description = data["description"] as? String,
            cost = (data["cost"] as? Number)?.toInt(),
            performedBy = data["performedBy"] as? String,
            shopName = data["shopName"] as? String,
            operatingHoursAtMaintenance = (data["operatingHoursAtMaintenance"] as? Number)?.toDouble(),
            photoUrls = (data["photoUrls"] as? List<String>) ?: emptyList(),
            replacedConsumableIds = (data["replacedConsumableIds"] as? List<String>) ?: emptyList(),
            isInProgress = data["isInProgress"] as? Boolean ?: false
        )
    } catch (e: Exception) {
        null
    }
}

// ============================================================================
// Consumable Repository (Firestore 기반)
// ============================================================================

/**
 * 컬렉션 구조: farms/{farmCode}/consumables/{consumableId}
 * machineId 필드로 필터링하여 특정 기계의 소모품만 조회.
 *
 * applyStandardTemplate은 기계 종류별 기본 소모품 목록을 Firestore에 일괄 저장.
 */
class FirestoreConsumableRepository(
    private val farmCode: String,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ConsumableRepository {

    private val collection = db
        .collection("farms")
        .document(farmCode)
        .collection("consumables")

    override fun observeConsumablesFor(machineId: String): Flow<List<com.example.farmmachinemanager.data.Consumable>> = callbackFlow {
        val registration = collection
            .whereEqualTo("machineId", machineId)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    // 마지막 데이터 유지
                    return@addSnapshotListener
                }
                val consumables = snapshot?.documents
                    ?.mapNotNull { doc -> doc.data?.let { consumableFromMap(doc.id, it) } }
                    ?: emptyList()
                trySend(consumables)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun saveConsumable(consumable: com.example.farmmachinemanager.data.Consumable) {
        collection.document(consumable.id).set(consumableToMap(consumable)).await()
    }

    override suspend fun deleteConsumable(id: String) {
        collection.document(id).delete().await()
    }

    override suspend fun applyStandardTemplate(
        machineId: String,
        machineType: com.example.farmmachinemanager.data.MachineType
    ) {
        // 종류별 표준 소모품 템플릿 (Sample Repository와 동일한 데이터)
        val template = com.example.farmmachinemanager.data.MaintenanceTemplates
            .defaultConsumables(machineId, machineType)
        // Firestore에 일괄 저장 (병렬 처리)
        template.forEach { consumable ->
            collection.document(consumable.id).set(consumableToMap(consumable)).await()
        }
    }

    private fun consumableToMap(c: com.example.farmmachinemanager.data.Consumable): Map<String, Any?> = mapOf(
        "id" to c.id,
        "machineId" to c.machineId,
        "name" to c.name,
        "category" to c.category.name,
        "replacementIntervalHours" to c.replacementIntervalHours,
        "replacementIntervalMonths" to c.replacementIntervalMonths,
        "lastReplacedDate" to c.lastReplacedDate?.toString(),
        "lastReplacedHours" to c.lastReplacedHours,
        "notes" to c.notes
    )

    private fun consumableFromMap(
        id: String,
        data: Map<String, Any?>
    ): com.example.farmmachinemanager.data.Consumable? = try {
        com.example.farmmachinemanager.data.Consumable(
            id = id,
            machineId = data["machineId"] as? String ?: "",
            name = data["name"] as? String ?: "",
            category = (data["category"] as? String)
                ?.let { runCatching { com.example.farmmachinemanager.data.ConsumableCategory.valueOf(it) }.getOrNull() }
                ?: com.example.farmmachinemanager.data.ConsumableCategory.OTHER,
            replacementIntervalHours = (data["replacementIntervalHours"] as? Number)?.toDouble(),
            replacementIntervalMonths = (data["replacementIntervalMonths"] as? Number)?.toInt(),
            lastReplacedDate = (data["lastReplacedDate"] as? String)
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
            lastReplacedHours = (data["lastReplacedHours"] as? Number)?.toDouble(),
            notes = data["notes"] as? String
        )
    } catch (e: Exception) {
        null
    }
}

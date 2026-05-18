package com.example.farmmachinemanager.data.repository

import com.example.farmmachinemanager.data.Machine
import com.example.farmmachinemanager.data.MachineStatus
import com.example.farmmachinemanager.data.MachineType
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
                trySend(emptyList())
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

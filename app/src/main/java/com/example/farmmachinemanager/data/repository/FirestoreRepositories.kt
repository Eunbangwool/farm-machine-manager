package com.example.farmmachinemanager.data.repository

/*
 * Firestore Repository 구현
 *
 * ⚠️ 활성화 전 준비 작업:
 * 1. FIREBASE_SETUP.md 가이드에 따라 Firebase 프로젝트 생성
 * 2. google-services.json을 app/ 폴더에 배치
 * 3. build.gradle.kts에 Firebase 의존성 추가
 * 4. 파일 상단의 `/* */` 주석 해제
 *
 * 의존성 추가 후 컴파일 가능합니다.
 *
 * 사용 컬렉션 구조 (Firestore):
 *   companies/{companyId}/machines/{machineId}
 *   companies/{companyId}/machines/{machineId}/maintenance/{recordId}
 *   companies/{companyId}/machines/{machineId}/consumables/{consumableId}
 *
 * 회사 단위로 데이터를 격리해서 여러 회사가 같은 앱을 사용해도 데이터가 섞이지 않습니다.
 *
 * ───────────────────────────────────────────────────────────────────
 *
 * Firebase 의존성 추가 후 아래 코드 주석 해제:
 *
 * import com.example.farmmachinemanager.data.Consumable
 * import com.example.farmmachinemanager.data.Machine
 * import com.example.farmmachinemanager.data.MaintenanceRecord
 * import com.example.farmmachinemanager.data.TractorMaintenanceTemplate
 * import com.google.firebase.Timestamp
 * import com.google.firebase.firestore.FirebaseFirestore
 * import com.google.firebase.firestore.ktx.snapshots
 * import kotlinx.coroutines.flow.Flow
 * import kotlinx.coroutines.flow.map
 * import kotlinx.coroutines.tasks.await
 * import java.time.LocalDate
 * import java.time.ZoneId
 *
 * class FirestoreMachineRepository(
 *     private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
 *     private val companyId: String  // 로그인한 사용자의 회사 ID
 * ) : MachineRepository {
 *
 *     private val collection = db.collection("companies").document(companyId).collection("machines")
 *
 *     override fun observeMachines(): Flow<List<Machine>> =
 *         collection.snapshots().map { snapshot ->
 *             snapshot.documents.mapNotNull { it.toObject(Machine::class.java) }
 *         }
 *
 *     override suspend fun getMachine(id: String): Machine? =
 *         collection.document(id).get().await().toObject(Machine::class.java)
 *
 *     override suspend fun saveMachine(machine: Machine) {
 *         val docId = machine.id.ifBlank { collection.document().id }
 *         collection.document(docId).set(machine.copy(id = docId)).await()
 *     }
 *
 *     override suspend fun deleteMachine(id: String) {
 *         collection.document(id).delete().await()
 *     }
 * }
 *
 * // 같은 방식으로 FirestoreMaintenanceRepository, FirestoreConsumableRepository 구현.
 *
 * ───────────────────────────────────────────────────────────────────
 *
 * 데이터 모델 Firestore 호환성:
 * - Machine, MaintenanceRecord, Consumable 모두 빈 생성자 + 기본값 가짐 → ✅
 * - LocalDate는 Firestore가 직접 지원 안 함 → @ServerTimestamp + Timestamp로 변환 필요
 *   (또는 컨버터: LocalDate ↔ Timestamp)
 *
 * 다음 단계에서 본격 구현 예정.
 */

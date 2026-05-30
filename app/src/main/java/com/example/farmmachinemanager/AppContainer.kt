package com.example.farmmachinemanager

import android.content.Context
import com.example.farmmachinemanager.data.FarmCodeManager
import com.example.farmmachinemanager.data.FirebaseAvailability
import com.example.farmmachinemanager.data.manual.ManualRepository
import com.example.farmmachinemanager.data.repository.ConsumableRepository
import com.example.farmmachinemanager.data.repository.FirestoreConsumableRepository
import com.example.farmmachinemanager.data.repository.FirestoreMachineRepository
import com.example.farmmachinemanager.data.repository.FirestoreMaintenanceRepository
import com.example.farmmachinemanager.data.repository.MachineRepository
import com.example.farmmachinemanager.data.repository.MaintenanceRepository
import com.example.farmmachinemanager.data.repository.SampleConsumableRepository
import com.example.farmmachinemanager.data.repository.SampleMachineRepository
import com.example.farmmachinemanager.data.repository.SampleMaintenanceRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * 서비스 로케이터.
 *
 * 초기화 순서:
 * 1. MainActivity.onCreate()에서 AppContainer.init(applicationContext) 호출
 * 2. 농장 코드 + Firebase 가용성에 따라 currentMode가 결정됨
 * 3. machineRepository / maintenanceRepository / consumableRepository는
 *    호출 시점에 현재 코드/모드에 맞는 인스턴스를 반환하고 캐시한다.
 *    농장 코드가 변경되면 refreshSyncMode()로 캐시를 무효화 한 뒤 새 인스턴스가 생성된다.
 *
 * 이미 collect 중인 Flow는 옛 repository에 묶여 있으므로, 화면을 다시 진입하면 새 Flow가
 * 시작된다. 현재 코드는 변경 후 List 화면으로 복귀하는 흐름을 가정한다.
 */
object AppContainer {

    private var initialized = false
    lateinit var farmCodeManager: FarmCodeManager
        private set
    lateinit var manualRepository: ManualRepository
        private set

    /** 사용 중인 모드 (UI에서 상태 표시용) */
    enum class SyncMode { LOCAL_ONLY, FIRESTORE_SYNCED, FIREBASE_NOT_CONFIGURED }

    @Volatile
    var currentMode: SyncMode = SyncMode.LOCAL_ONLY
        private set

    /**
     * Firestore observer 가 받은 가장 최근 에러. UI 에서 사용자에게 노출한다.
     * 권한 거부·네트워크 오류 등은 침묵하지 않고 여기 적재되어 설정 화면에 표시된다.
     * 정상 응답이 들어오면 null 로 초기화 (recoverFromError).
     */
    private val _lastFirestoreError = MutableStateFlow<String?>(null)
    val lastFirestoreError: StateFlow<String?> get() = _lastFirestoreError

    fun reportFirestoreError(message: String?) {
        _lastFirestoreError.value = message
    }

    fun clearFirestoreError() {
        _lastFirestoreError.value = null
    }

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        farmCodeManager = FarmCodeManager(context)
        manualRepository = ManualRepository(context.applicationContext)
        refreshSyncMode()
    }

    /** 농장 코드 또는 Firebase 상태가 바뀐 뒤 호출. mode 재평가 + repository 캐시 무효화. */
    fun refreshSyncMode() {
        currentMode = when {
            !FirebaseAvailability.isAvailable -> SyncMode.FIREBASE_NOT_CONFIGURED
            farmCodeManager.farmCode.isNullOrBlank() -> SyncMode.LOCAL_ONLY
            else -> SyncMode.FIRESTORE_SYNCED
        }
        cachedMachineCode = null
        cachedMaintenanceCode = null
        cachedConsumableCode = null
        cachedMachineRepository = null
        cachedMaintenanceRepository = null
        cachedConsumableRepository = null

        // Firestore 사용 가능하면 익명 인증 + 농장 멤버 등록을 백그라운드로 보장.
        if (FirebaseAvailability.isAvailable) {
            val code = farmCodeManager.farmCode?.takeIf { it.isNotBlank() }
            scope.launch {
                runCatching {
                    ensureAuthReady()
                    if (code != null) ensureMachineMembership(code)
                }  // 실패는 reportFirestoreError 로 이미 가시화됨. crash 방지.
            }
        }
    }

    // ---- 익명 인증 + 농장 멤버 등록 ----------------------------------------
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** 익명 인증이 끝났을 때 완료되는 deferred. 중복 호출은 같은 job 재사용. */
    @Volatile private var authReady: CompletableDeferred<Unit>? = null

    /**
     * 익명 Firebase Auth 가 준비될 때까지 대기. 이미 로그인되어 있으면 즉시 반환.
     * Firestore Repository 가 실제 read/write 직전에 await 해야 보안 규칙(isMachineMember)
     * 게이트를 통과한다. 실패는 reportFirestoreError 로 사용자에게 노출.
     */
    suspend fun ensureAuthReady() {
        val existing = authReady
        if (existing != null) {
            existing.await(); return
        }
        val deferred = CompletableDeferred<Unit>()
        authReady = deferred
        try {
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) {
                auth.signInAnonymously().await()
            }
            deferred.complete(Unit)
        } catch (t: Throwable) {
            authReady = null  // 재시도 가능하도록 초기화
            val hint = if (t.message?.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) == true ||
                t.message?.contains("ADMIN_ONLY", ignoreCase = true) == true)
                " (Firebase Console → Authentication → Sign-in method 에서 익명 인증 활성화 필요)"
            else ""
            reportFirestoreError("익명 로그인 실패: ${t.message ?: t::class.java.simpleName}$hint")
            deferred.complete(Unit)  // 대기자들이 영구 차단되지 않도록 풀어준다
            // throw 제거 — 호출자 코루틴이 unhandled 예외로 크래시하는 것 방지.
        }
    }

    /**
     * 농장 코드 하위에 본인의 machineMembers 자기 doc 을 생성(없으면). Rules 의
     * isMachineMember(farmCode) 가 통과하려면 이 doc 이 존재해야 한다.
     * 코드 변경 시마다 한 번 호출.
     */
    suspend fun ensureMachineMembership(farmCode: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        try {
            FirebaseFirestore.getInstance()
                .collection("farms").document(farmCode)
                .collection("machineMembers").document(uid)
                .set(
                    mapOf(
                        "uid" to uid,
                        "joinedAt" to FieldValue.serverTimestamp(),
                    ),
                    com.google.firebase.firestore.SetOptions.merge(),
                )
                .await()
        } catch (t: Throwable) {
            reportFirestoreError("멤버 등록 실패: ${t.message ?: t::class.java.simpleName}")
        }
    }

    // ---- repository 캐시 ----------------------------------------------------
    // key 는 "Firestore 모드일 때의 농장코드" 또는 null(로컬). key 가 바뀌면 새 인스턴스 발급.

    @Volatile private var cachedMachineCode: String? = null
    @Volatile private var cachedMachineRepository: MachineRepository? = null

    @Volatile private var cachedMaintenanceCode: String? = null
    @Volatile private var cachedMaintenanceRepository: MaintenanceRepository? = null

    @Volatile private var cachedConsumableCode: String? = null
    @Volatile private var cachedConsumableRepository: ConsumableRepository? = null

    private fun activeFarmCode(): String? =
        if (currentMode == SyncMode.FIRESTORE_SYNCED) farmCodeManager.farmCode?.takeIf { it.isNotBlank() } else null

    val machineRepository: MachineRepository
        get() {
            val key = activeFarmCode()
            cachedMachineRepository?.let { if (cachedMachineCode == key) return it }
            val fresh: MachineRepository =
                if (key != null) FirestoreMachineRepository(farmCode = key) else SampleMachineRepository()
            cachedMachineRepository = fresh
            cachedMachineCode = key
            return fresh
        }

    val maintenanceRepository: MaintenanceRepository
        get() {
            val key = activeFarmCode()
            cachedMaintenanceRepository?.let { if (cachedMaintenanceCode == key) return it }
            val fresh: MaintenanceRepository =
                if (key != null) FirestoreMaintenanceRepository(farmCode = key) else SampleMaintenanceRepository()
            cachedMaintenanceRepository = fresh
            cachedMaintenanceCode = key
            return fresh
        }

    val consumableRepository: ConsumableRepository
        get() {
            val key = activeFarmCode()
            cachedConsumableRepository?.let { if (cachedConsumableCode == key) return it }
            val fresh: ConsumableRepository =
                if (key != null) FirestoreConsumableRepository(farmCode = key) else SampleConsumableRepository()
            cachedConsumableRepository = fresh
            cachedConsumableCode = key
            return fresh
        }
}

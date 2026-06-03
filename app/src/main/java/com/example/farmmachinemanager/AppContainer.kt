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

    /** 익명 인증이 끝났을 때 완료되는 deferred. 동시 첫 호출 race 를 막기 위해
     *  synchronized 로 보호. 실패 시 completeExceptionally 로 호출자에게 전파. */
    private var authReady: CompletableDeferred<Unit>? = null
    private val authLock = Any()

    /** 농장 코드별 멤버 등록 deferred. ConcurrentHashMap 으로 thread-safe. */
    private val membershipReady = java.util.concurrent.ConcurrentHashMap<String, CompletableDeferred<Unit>>()

    /**
     * 익명 Firebase Auth 가 준비될 때까지 대기. 실제 인증은 AppContainer.scope 에서
     * 실행되므로 호출자(Compose callbackFlow) cancel 영향 없음. 실패 시 호출자에게
     * 예외 전파 — 후속 Firestore 호출이 PERMISSION_DENIED 로 헷갈리는 메시지를
     * 띄우는 대신 정확한 '익명 로그인 실패' 메시지가 보임.
     */
    suspend fun ensureAuthReady() {
        val deferred = synchronized(authLock) {
            authReady?.let { return@synchronized it }
            val d = CompletableDeferred<Unit>()
            authReady = d
            scope.launch {
                try {
                    val auth = FirebaseAuth.getInstance()
                    if (auth.currentUser == null) {
                        auth.signInAnonymously().await()
                    }
                    d.complete(Unit)
                } catch (t: Throwable) {
                    synchronized(authLock) { authReady = null }
                    val hint = if (t.message?.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) == true ||
                        t.message?.contains("ADMIN_ONLY", ignoreCase = true) == true)
                        " (Firebase Console → Authentication → Sign-in method 에서 익명 인증 활성화 필요)"
                    else ""
                    reportFirestoreError("익명 로그인 실패: ${t.message ?: t::class.java.simpleName}$hint")
                    d.completeExceptionally(t)
                }
            }
            d
        }
        deferred.await()
    }

    /**
     * 농장 코드 하위 본인 machineMembers 자기 doc 보장.
     * PERMISSION_DENIED 발생 시 옛 익명 UID 가 무효(폰 데이터 캐시 충돌 등)일 수
     * 있어 1회 자가치유: signOut → 새 익명 로그인 → 멤버 등록 재시도.
     */
    suspend fun ensureMachineMembership(farmCode: String) {
        val deferred = membershipReady.compute(farmCode) { _, existing ->
            existing ?: CompletableDeferred<Unit>().also { d ->
                scope.launch {
                    // 1차 시도 — 실패해도 silent.
                    if (tryMembershipSet(farmCode)) {
                        d.complete(Unit); return@launch
                    }
                    // 자가치유 — 옛 UID 폐기 + 새 익명 로그인 + 1회 재시도.
                    runCatching {
                        FirebaseAuth.getInstance().signOut()
                        synchronized(authLock) { authReady = null }
                        ensureAuthReady()
                    }
                    if (tryMembershipSet(farmCode)) {
                        d.complete(Unit); return@launch
                    }
                    // 2차도 실패 — 사용자에게 노출.
                    membershipReady.remove(farmCode)
                    reportFirestoreError("멤버 등록 실패 (재시도 후에도). Firestore 규칙 또는 익명 인증 설정을 확인하세요.")
                    d.completeExceptionally(IllegalStateException("machineMembers set denied"))
                }
            }
        }!!
        deferred.await()
    }

    /** 멤버 doc set 1회 시도. 성공/실패만 반환 (사용자 노출은 호출자 책임). */
    private suspend fun tryMembershipSet(farmCode: String): Boolean {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return false
        return runCatching {
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
            true
        }.getOrElse { false }
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

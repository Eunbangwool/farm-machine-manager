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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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

    /**
     * 농장 코드 또는 Firebase 상태가 바뀐 뒤 호출. mode 재평가 + repository 캐시 무효화.
     */
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

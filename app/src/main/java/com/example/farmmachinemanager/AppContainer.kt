package com.example.farmmachinemanager

import android.content.Context
import com.example.farmmachinemanager.data.FarmCodeManager
import com.example.farmmachinemanager.data.FirebaseAvailability
import com.example.farmmachinemanager.data.repository.ConsumableRepository
import com.example.farmmachinemanager.data.repository.FirestoreMachineRepository
import com.example.farmmachinemanager.data.repository.MachineRepository
import com.example.farmmachinemanager.data.repository.MaintenanceRepository
import com.example.farmmachinemanager.data.repository.SampleConsumableRepository
import com.example.farmmachinemanager.data.repository.SampleMachineRepository
import com.example.farmmachinemanager.data.repository.SampleMaintenanceRepository

/**
 * 서비스 로케이터.
 *
 * 초기화 순서:
 * 1. MainActivity.onCreate()에서 AppContainer.init(applicationContext) 호출
 * 2. AppContainer가 농장 코드 + Firebase 가용성 검사
 * 3. 두 조건 모두 충족 → Firestore Repository (실시간 동기화 활성)
 *    아니면 → Sample Repository (메모리 기반, 로컬만)
 *
 * 농장 코드 변경 시에는 앱 재시작 필요.
 */
object AppContainer {

    private var initialized = false
    lateinit var farmCodeManager: FarmCodeManager
        private set

    /** 사용 중인 모드 (UI에서 상태 표시용) */
    enum class SyncMode { LOCAL_ONLY, FIRESTORE_SYNCED, FIREBASE_NOT_CONFIGURED }
    var currentMode: SyncMode = SyncMode.LOCAL_ONLY
        private set

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        farmCodeManager = FarmCodeManager(context)

        currentMode = when {
            !FirebaseAvailability.isAvailable -> SyncMode.FIREBASE_NOT_CONFIGURED
            farmCodeManager.farmCode.isNullOrBlank() -> SyncMode.LOCAL_ONLY
            else -> SyncMode.FIRESTORE_SYNCED
        }
    }

    val machineRepository: MachineRepository by lazy {
        val code = farmCodeManager.farmCode
        if (currentMode == SyncMode.FIRESTORE_SYNCED && !code.isNullOrBlank()) {
            FirestoreMachineRepository(farmCode = code)
        } else {
            SampleMachineRepository()
        }
    }

    // Maintenance + Consumable은 다음 round에서 Firestore 마이그레이션.
    // 현재는 메모리 기반으로 유지 (앱 재시작 시 사라짐).
    val maintenanceRepository: MaintenanceRepository by lazy {
        SampleMaintenanceRepository()
    }

    val consumableRepository: ConsumableRepository by lazy {
        SampleConsumableRepository()
    }
}

package com.example.farmmachinemanager

import com.example.farmmachinemanager.data.repository.ConsumableRepository
import com.example.farmmachinemanager.data.repository.MachineRepository
import com.example.farmmachinemanager.data.repository.MaintenanceRepository
import com.example.farmmachinemanager.data.repository.SampleConsumableRepository
import com.example.farmmachinemanager.data.repository.SampleMachineRepository
import com.example.farmmachinemanager.data.repository.SampleMaintenanceRepository

/**
 * 간단한 서비스 로케이터.
 *
 * 현재는 SampleRepository를 사용합니다.
 * Firebase 연동이 완료되면 아래 useFirebase = true 로 변경하세요.
 * (Hilt 같은 본격적인 DI 프레임워크는 화면이 많아진 뒤에 도입 예정)
 */
object AppContainer {

    private const val useFirebase = false

    // 백엔드 스위치는 한 곳에서. 화면 코드는 변경 불필요.
    val machineRepository: MachineRepository by lazy {
        if (useFirebase) {
            TODO("Firebase 설정 완료 후 FirestoreMachineRepository(...) 반환")
        } else {
            SampleMachineRepository()
        }
    }

    val maintenanceRepository: MaintenanceRepository by lazy {
        if (useFirebase) {
            TODO("Firebase 설정 완료 후 FirestoreMaintenanceRepository(...) 반환")
        } else {
            SampleMaintenanceRepository()
        }
    }

    val consumableRepository: ConsumableRepository by lazy {
        if (useFirebase) {
            TODO("Firebase 설정 완료 후 FirestoreConsumableRepository(...) 반환")
        } else {
            SampleConsumableRepository()
        }
    }
}

package com.example.farmmachinemanager.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.farmmachinemanager.AppContainer
import com.example.farmmachinemanager.data.Consumable
import com.example.farmmachinemanager.data.ConsumableStatus
import kotlinx.coroutines.flow.first

/**
 * 주기적으로 (하루 1회) 실행되어 소모품 교체 임박/초과 항목 검사.
 *
 * 검사 결과에 따라:
 * - DUE_SOON 또는 OVERDUE 항목이 있으면 알림 1건 발송
 * - 없으면 알림 없음
 *
 * MainActivity에서 WorkManager에 PeriodicWorkRequest로 등록.
 */
class ConsumableCheckWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // 사용자가 알림 끈 경우 작업 종료
        val prefs = NotificationPreferences(applicationContext)
        if (!prefs.enabled) return Result.success()

        // AppContainer 초기화 보장 (백그라운드 실행 시 process 새로 시작될 수 있음)
        AppContainer.init(applicationContext)

        return try {
            val machines = AppContainer.machineRepository.observeMachines().first()
            if (machines.isEmpty()) return Result.success()

            val urgent = mutableListOf<Pair<String, Consumable>>()  // 기계이름 + 소모품
            machines.forEach { machine ->
                val consumables = AppContainer.consumableRepository
                    .observeConsumablesFor(machine.id).first()
                consumables.forEach { c ->
                    val status = c.status(machine.operatingHours)
                    if (status == ConsumableStatus.DUE_SOON || status == ConsumableStatus.OVERDUE) {
                        urgent += machine.name to c
                    }
                }
            }

            if (urgent.isNotEmpty()) {
                val title = "교체 시기 ${urgent.size}건 확인 필요"
                val body = urgent.take(5).joinToString("\n") { (m, c) ->
                    "• $m - ${c.name} (${c.category.displayName})"
                } + if (urgent.size > 5) "\n외 ${urgent.size - 5}건" else ""
                NotificationHelper.showConsumableAlert(applicationContext, title, body)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "consumable_check_daily"
    }
}

package com.example.farmmachinemanager.data

import android.content.Context
import com.example.farmmachinemanager.notifications.NotificationHelper

/**
 * 머신별 마지막 알림 milestone(50h 배수)을 SharedPreferences 로 추적해
 * 가동시간이 새로운 50h 배수에 도달하면 알림 발송.
 *
 * 호출 시점:
 *  - 가동시간 업데이트(UpdateOperatingHoursScreen) 저장 후
 *  - 기계 편집(EditMachineScreen) 가동시간 변경 후
 *
 * 신규 머신 등록(AddMachineScreen) 직후엔 seedBaseline 으로 초기 가동시간의
 * 50h floor 를 기준선으로 잡아 기존 시간만큼은 알림이 한꺼번에 안 뜨게 한다.
 */
object MaintenanceMilestoneTracker {

    private const val PREFS = "hour_milestones"
    private const val INTERVAL = 50

    /** 머신 등록 시 호출. 현재 가동시간의 50h floor 를 baseline 으로 저장(알림 X). */
    fun seedBaseline(context: Context, machineId: String, currentHours: Double) {
        val baseline = floorMultiple(currentHours)
        prefs(context).edit().putInt(key(machineId), baseline).apply()
    }

    /**
     * 가동시간 갱신 후 호출. 새 50h 배수에 도달했으면 알림 발송 + 기준선 갱신.
     * 도달한 가장 큰 배수 1건만 알림(예: 48→210 으로 점프 시 200h 알림 1번).
     */
    fun checkAndNotify(context: Context, machineId: String, machineName: String, newHours: Double) {
        val sp = prefs(context)
        val last = sp.getInt(key(machineId), 0)
        val current = floorMultiple(newHours)
        if (current > last) {
            NotificationHelper.showMilestoneAlert(context, machineId, machineName, current)
            sp.edit().putInt(key(machineId), current).apply()
        }
    }

    /** 머신 삭제 시 호출 (선택). */
    fun clear(context: Context, machineId: String) {
        prefs(context).edit().remove(key(machineId)).apply()
    }

    private fun floorMultiple(hours: Double): Int =
        (hours.toInt() / INTERVAL) * INTERVAL

    private fun key(machineId: String) = "last_notified_$machineId"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}

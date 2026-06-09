package com.example.farmmachinemanager.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.farmmachinemanager.MainActivity
import com.example.farmmachinemanager.R

/**
 * 알림 채널 / 발송 헬퍼.
 *
 * Android 8.0+ NotificationChannel 필수.
 * Channel ID는 한 번 만들어지면 사용자가 직접 변경 가능 (앱 정보 → 알림).
 */
object NotificationHelper {
    const val CHANNEL_ID_CONSUMABLE = "consumable_alerts"
    const val CHANNEL_NAME = "소모품 교체 알림"
    const val NOTIFICATION_ID_CONSUMABLE = 1001

    const val CHANNEL_ID_UPDATE = "app_update_alerts"
    const val CHANNEL_NAME_UPDATE = "앱 업데이트 알림"
    const val NOTIFICATION_ID_UPDATE = 1002

    const val CHANNEL_ID_MILESTONE = "maintenance_milestone"
    const val CHANNEL_NAME_MILESTONE = "정기 정비 시기 알림"
    /** 머신마다 별도 알림 ID 가 필요해서 1100 + machineId.hashCode() 형태로 사용. */
    const val NOTIFICATION_ID_MILESTONE_BASE = 1100

    /** 앱 시작 시 또는 첫 발송 전에 호출. 이미 만들어진 채널은 중복 등록 무시됨. */
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = ContextCompat.getSystemService(context, NotificationManager::class.java)
            nm?.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_CONSUMABLE,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "엔진오일/필터 등 소모품 교체 시기가 다가오면 알림"
                }
            )
            nm?.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_UPDATE,
                    CHANNEL_NAME_UPDATE,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "새 앱 버전이 배포되면 알림"
                }
            )
            nm?.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_MILESTONE,
                    CHANNEL_NAME_MILESTONE,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "기계 가동시간이 50시간 단위(50/100/200/400h…)에 도달하면 알림"
                }
            )
        }
    }

    /**
     * 소모품 교체 임박/초과 알림 표시.
     * 알림 탭 시 앱 메인 화면 열림.
     */
    fun showConsumableAlert(
        context: Context,
        title: String,
        message: String
    ) {
        ensureChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_CONSUMABLE)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // 기본 안드로이드 아이콘 사용
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val nm = ContextCompat.getSystemService(context, NotificationManager::class.java)
        nm?.notify(NOTIFICATION_ID_CONSUMABLE, notification)
    }

    /**
     * 새 앱 버전 알림. 탭하면 APK 다운로드 페이지/파일을 브라우저로 연다.
     */
    fun showUpdateAlert(
        context: Context,
        versionName: String,
        apkUrl: String
    ) {
        ensureChannel(context)

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = "새 버전 $versionName 이(가) 나왔습니다. 탭하여 설치하세요."
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_UPDATE)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("농식이 업데이트")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val nm = ContextCompat.getSystemService(context, NotificationManager::class.java)
        nm?.notify(NOTIFICATION_ID_UPDATE, notification)
    }

    /**
     * 가동시간 50시간 단위 정기 정비 시기 알림.
     * 탭하면 앱이 열림 — 사용자가 해당 머신 상세에서 정기 정비 일괄 입력 가능.
     */
    fun showMilestoneAlert(
        context: Context,
        machineId: String,
        machineName: String,
        milestoneHours: Int,
    ) {
        ensureChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, machineId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "$machineName · ${milestoneHours}시간 정비 시기"
        val message = "가동시간이 ${milestoneHours}h 에 도달했습니다. " +
            "50시간 정기 정비 항목을 확인하세요."
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_MILESTONE)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val nm = ContextCompat.getSystemService(context, NotificationManager::class.java)
        // 머신별 고유 ID — 동시에 여러 머신 알림 노출 가능.
        nm?.notify(NOTIFICATION_ID_MILESTONE_BASE + machineId.hashCode(), notification)
    }
}

package com.example.farmmachinemanager.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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

    /** 앱 시작 시 또는 첫 발송 전에 호출. 이미 만들어진 채널은 중복 등록 무시됨. */
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_CONSUMABLE,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "엔진오일/필터 등 소모품 교체 시기가 다가오면 알림"
            }
            val nm = ContextCompat.getSystemService(context, NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
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
}

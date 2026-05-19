package com.example.farmmachinemanager.notifications

import android.content.Context
import android.content.SharedPreferences

/**
 * 알림 설정 영구 저장.
 * - enabled: 사용자가 Settings에서 토글한 값
 */
class NotificationPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_ENABLED, value).apply()
        }

    companion object {
        private const val PREFS_NAME = "notification_settings"
        private const val KEY_ENABLED = "alerts_enabled"
    }
}

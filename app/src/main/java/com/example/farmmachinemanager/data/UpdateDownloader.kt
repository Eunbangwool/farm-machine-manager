package com.example.farmmachinemanager.data

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

/**
 * 새 APK 빌드를 시스템 DownloadManager 로 직접 다운로드.
 * 다운로드 진행/완료 알림은 시스템이 자동으로 띄움. 사용자가 알림 탭 →
 * APK 설치 화면(앱 측에서 별도 권한 요구 없이 시스템 다운로드 알림 경유).
 */
object UpdateDownloader {

    /** DownloadManager 큐 ID 반환. 시스템 미지원 시 null. */
    fun startDownload(context: Context, info: UpdateChecker.UpdateInfo): Long? {
        val fileName = "FarmMachineManager-${info.versionName}.apk"
        val request = DownloadManager.Request(Uri.parse(info.apkUrl))
            .setTitle("농돌이 ${info.versionName}")
            .setDescription("새 빌드(#${info.buildNumber}) APK 다운로드")
            .setDestinationInExternalFilesDir(
                context, Environment.DIRECTORY_DOWNLOADS, fileName
            )
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            .setMimeType("application/vnd.android.package-archive")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        val dm = context.getSystemService(DownloadManager::class.java) ?: return null
        return runCatching { dm.enqueue(request) }.getOrNull()
    }
}

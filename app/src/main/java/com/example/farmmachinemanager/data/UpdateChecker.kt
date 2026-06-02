package com.example.farmmachinemanager.data

import com.example.farmmachinemanager.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * GitHub release(tag=latest)를 조회해 현재 설치본보다 새 빌드가 있는지 확인.
 *
 * versionCode 는 build.gradle 에서 GITHUB_RUN_NUMBER(=release "빌드 번호: N")로
 * 설정되므로, release 본문의 빌드 번호와 BuildConfig.VERSION_CODE 를 그대로 비교한다.
 */
object UpdateChecker {

    private const val LATEST_API =
        "https://api.github.com/repos/Eunbangwool/farm-machine-manager/releases/tags/latest"
    const val APK_URL =
        "https://github.com/Eunbangwool/farm-machine-manager/releases/latest/download/FarmMachineManager.apk"

    data class UpdateInfo(val buildNumber: Int, val versionName: String, val apkUrl: String)

    /** 새 버전이 있으면 UpdateInfo, 없거나 조회 실패면 null.
     *  로컬 빌드(VERSION_CODE<=1)는 매 빌드마다 'new version' 으로 인식되니 스킵. */
    suspend fun check(): UpdateInfo? = withContext(Dispatchers.IO) {
        if (BuildConfig.VERSION_CODE <= 1) return@withContext null
        runCatching {
            val conn = (URL(LATEST_API).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github+json")
            }
            val text = try {
                conn.inputStream.bufferedReader().use { it.readText() }
            } finally {
                conn.disconnect()
            }
            // release 본문의 "빌드 번호: `57`" — 백틱·콜론·공백 만 허용해 다른
            // 숫자 토큰(다운로드 카운트 등)을 잘못 잡지 않도록 한다.
            val latest = Regex("""빌드 번호[:\s`]*([0-9]+)""")
                .find(text)?.groupValues?.get(1)?.toIntOrNull() ?: return@runCatching null
            if (latest <= BuildConfig.VERSION_CODE) return@runCatching null
            UpdateInfo(
                buildNumber = latest,
                versionName = "0.1.$latest",
                apkUrl = APK_URL,
            )
        }.getOrNull()
    }
}

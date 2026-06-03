package com.example.farmmachinemanager.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.example.farmmachinemanager.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * GitHub Releases API로 새 버전 확인 + APK 다운로드 + 자동 설치.
 * 농작이 (sangwolnongsan/farmwork)의 AppUpdateChecker 와 동일 패턴.
 *
 * 동작:
 * 1. GET https://api.github.com/repos/Eunbangwool/farm-machine-manager/releases/latest
 * 2. body 에서 "빌드 번호: N" 파싱 (농돌이 workflow 가 release body 에 적는 형식)
 * 3. BuildConfig.VERSION_CODE 보다 크면 새 버전 있음
 * 4. assets 중 "FarmMachineManager.apk" 다운로드 → 완료 시 설치 화면 자동 띄움
 */
object AppUpdateChecker {

    private const val TAG = "AppUpdateChecker"
    private const val REPO_OWNER = "Eunbangwool"
    private const val REPO_NAME = "farm-machine-manager"
    private const val API_URL = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases/latest"

    data class UpdateInfo(
        val latestVersionCode: Int,
        val latestVersionName: String?,
        val downloadUrl: String,
        val sizeBytes: Long,
        val buildTime: String?
    )

    sealed class CheckResult {
        data class UpdateAvailable(val info: UpdateInfo) : CheckResult()
        object UpToDate : CheckResult()
        data class Error(val message: String) : CheckResult()
    }

    /** 백그라운드에서 GitHub Releases API 호출 + 비교. 로컬 빌드(VERSION_CODE<=1)는 스킵. */
    suspend fun checkForUpdate(): CheckResult = withContext(Dispatchers.IO) {
        if (BuildConfig.VERSION_CODE <= 1) return@withContext CheckResult.UpToDate
        try {
            val conn = (URL(API_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "FarmMachineManager-App")
                connectTimeout = 10_000
                readTimeout = 10_000
            }
            val code = conn.responseCode
            if (code != 200) {
                val errBody = try {
                    conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                } catch (_: Exception) { "" }
                conn.disconnect()
                val hint = when (code) {
                    403 -> {
                        if (errBody.contains("rate limit", ignoreCase = true))
                            "GitHub API 요청 한도 초과 (시간당 60회). 잠시 후 다시 시도해주세요."
                        else "GitHub 접근 거부 (403)"
                    }
                    404 -> "Release 가 없거나 비공개"
                    in 500..599 -> "GitHub 서버 일시 장애 ($code) — 잠시 후 재시도"
                    else -> "GitHub 응답 코드 $code"
                }
                return@withContext CheckResult.Error(hint)
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            val json = JSONObject(body)
            val releaseBody = json.optString("body", "")
            val assets = json.optJSONArray("assets")
            if (assets == null || assets.length() == 0) {
                return@withContext CheckResult.Error("Release 에 APK 파일이 없습니다")
            }
            val firstApk = (0 until assets.length())
                .map { assets.getJSONObject(it) }
                .firstOrNull { it.optString("name", "").endsWith(".apk") }
                ?: return@withContext CheckResult.Error("APK 파일을 찾을 수 없습니다")

            // 농돌이 release body 형식: "빌드 번호: `73`"
            val versionCode = Regex("""빌드 번호[:\s`]*([0-9]+)""")
                .find(releaseBody)?.groupValues?.get(1)?.toIntOrNull()
                ?: return@withContext CheckResult.Error("Release body 에 빌드 번호 정보가 없습니다")

            val buildTime = Regex("""일시[:\s`]*([\d\-:\s]+)""")
                .find(releaseBody)?.groupValues?.get(1)?.trim()

            val info = UpdateInfo(
                latestVersionCode = versionCode,
                latestVersionName = "0.1.$versionCode",
                downloadUrl = firstApk.getString("browser_download_url"),
                sizeBytes = firstApk.optLong("size", 0),
                buildTime = buildTime
            )

            if (versionCode > BuildConfig.VERSION_CODE) {
                Log.d(TAG, "새 버전: v$versionCode (현재 v${BuildConfig.VERSION_CODE})")
                CheckResult.UpdateAvailable(info)
            } else {
                Log.d(TAG, "최신 버전 (v${BuildConfig.VERSION_CODE})")
                CheckResult.UpToDate
            }
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "checkForUpdate 네트워크 오류", e)
            CheckResult.Error("인터넷 연결 확인 필요 (api.github.com 접근 불가)")
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "checkForUpdate 타임아웃", e)
            CheckResult.Error("GitHub 응답 지연 — 네트워크 상태 확인 후 재시도")
        } catch (e: javax.net.ssl.SSLException) {
            Log.e(TAG, "checkForUpdate SSL 오류", e)
            CheckResult.Error("SSL 인증서 오류 — 폰 날짜·시간 확인")
        } catch (e: Exception) {
            Log.e(TAG, "checkForUpdate 실패", e)
            CheckResult.Error(e.message ?: e.javaClass.simpleName ?: "알 수 없는 오류")
        }
    }

    /**
     * DownloadManager 로 APK 다운로드 + 완료 시 자동 설치 화면.
     * 실패 시 브라우저 fallback.
     */
    fun downloadAndInstall(context: Context, info: UpdateInfo) {
        val apkFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "FarmMachineManager-v${info.latestVersionCode}.apk"
        )
        if (apkFile.exists()) apkFile.delete()

        val downloadId = try {
            val request = DownloadManager.Request(Uri.parse(info.downloadUrl)).apply {
                setTitle("농돌이 업데이트")
                setDescription("v${info.latestVersionCode} 다운로드 중")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationUri(Uri.fromFile(apkFile))
                setMimeType("application/vnd.android.package-archive")
                setAllowedOverRoaming(true)
                setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
                )
            }
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val id = dm.enqueue(request)
            android.widget.Toast.makeText(
                context,
                "📥 다운로드 시작… 알림창에서 진행 상황 확인",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            Log.d(TAG, "다운로드 시작: id=$id, file=${apkFile.absolutePath}, url=${info.downloadUrl}")
            id
        } catch (e: Exception) {
            Log.e(TAG, "DownloadManager 실패 — 브라우저 fallback", e)
            android.widget.Toast.makeText(
                context,
                "다운로드 실패. 브라우저로 엽니다.",
                android.widget.Toast.LENGTH_LONG
            ).show()
            try {
                val browse = Intent(Intent.ACTION_VIEW, Uri.parse(info.downloadUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browse)
            } catch (e2: Exception) {
                Log.e(TAG, "브라우저 fallback 도 실패", e2)
                android.widget.Toast.makeText(
                    context,
                    "다운로드 못 함: ${e2.message}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
            return
        }

        // 다운로드 완료 broadcast → 자동 설치 화면. 실패 시 브라우저 fallback.
        val appCtx = context.applicationContext
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: -1L
                if (id != downloadId) return
                try { appCtx.unregisterReceiver(this) } catch (_: Exception) {}

                val dm = appCtx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val q = DownloadManager.Query().setFilterById(id)
                var status = -1
                var reason = -1
                dm.query(q).use { c ->
                    if (c != null && c.moveToFirst()) {
                        status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        reason = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                    }
                }
                val success = status == DownloadManager.STATUS_SUCCESSFUL &&
                    apkFile.exists() && apkFile.length() > 0
                if (success) {
                    triggerInstall(appCtx, apkFile)
                    return
                }

                val reasonText = when (reason) {
                    DownloadManager.ERROR_HTTP_DATA_ERROR -> "HTTP 데이터 오류 (서버 redirect 실패)"
                    DownloadManager.ERROR_INSUFFICIENT_SPACE -> "저장 공간 부족"
                    DownloadManager.ERROR_DEVICE_NOT_FOUND -> "외부 저장소 접근 실패"
                    DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "redirect 너무 많음 (GitHub CDN 이슈)"
                    DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "HTTP 코드 처리 실패"
                    DownloadManager.ERROR_CANNOT_RESUME -> "다운로드 재개 불가"
                    DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "파일 이미 존재"
                    DownloadManager.ERROR_FILE_ERROR -> "파일 시스템 오류"
                    DownloadManager.ERROR_UNKNOWN -> "알 수 없는 오류"
                    else -> "코드=$reason"
                }
                Log.e(TAG, "다운로드 실패: $reasonText (status=$status)")
                android.widget.Toast.makeText(
                    appCtx,
                    "다운로드 실패: $reasonText\n브라우저로 다시 시도합니다.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                try {
                    val browse = Intent(Intent.ACTION_VIEW, Uri.parse(info.downloadUrl)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    appCtx.startActivity(browse)
                } catch (e: Exception) {
                    Log.e(TAG, "브라우저 fallback 실패", e)
                }
            }
        }
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.applicationContext.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.applicationContext.registerReceiver(receiver, filter)
        }
    }

    /** APK 파일 → FileProvider URI → 시스템 패키지 설치 화면 */
    private fun triggerInstall(context: Context, apkFile: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, apkFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
            Log.d(TAG, "설치 화면 띄움: $uri")
        } catch (e: Exception) {
            Log.e(TAG, "설치 화면 띄우기 실패", e)
        }
    }
}

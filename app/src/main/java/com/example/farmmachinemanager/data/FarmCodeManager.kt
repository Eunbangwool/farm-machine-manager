package com.example.farmmachinemanager.data

import android.content.Context
import android.content.SharedPreferences

/**
 * 농장 코드 관리.
 *
 * 농장 코드(6자리 숫자)는 여러 폰이 같은 데이터를 공유하기 위한 식별자.
 * - 새 농장 시작: generateNewCode() → 다른 폰에 그 코드 알려줌
 * - 기존 농장 참여: setCode(받은 코드)
 *
 * 코드가 설정되어 있으면 AppContainer가 Firestore Repository를 사용.
 * 없으면 메모리 기반 Repository (단일 폰 로컬 저장).
 */
class FarmCodeManager(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val farmCode: String? get() = prefs.getString(KEY_FARM_CODE, null)

    fun setCode(code: String) {
        prefs.edit().putString(KEY_FARM_CODE, code.trim()).apply()
    }

    fun clearCode() {
        prefs.edit().remove(KEY_FARM_CODE).apply()
    }

    /** 6자리 랜덤 코드 생성 후 저장. 예: "493728" */
    fun generateNewCode(): String {
        val code = (100000..999999).random().toString()
        setCode(code)
        return code
    }

    companion object {
        private const val PREFS_NAME = "farm_settings"
        private const val KEY_FARM_CODE = "farm_code"
    }
}

/**
 * Firebase 사용 가능 여부 확인.
 * google-services.json이 없으면 FirebaseApp 초기화가 안 되어 getInstance()가 throw.
 */
object FirebaseAvailability {
    val isAvailable: Boolean by lazy {
        try {
            com.google.firebase.FirebaseApp.getInstance()
            true
        } catch (e: IllegalStateException) {
            false
        } catch (e: Throwable) {
            false
        }
    }
}

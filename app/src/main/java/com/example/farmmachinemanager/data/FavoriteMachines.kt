package com.example.farmmachinemanager.data

import android.content.Context

/**
 * 즐겨찾기 머신 ID 집합 — SharedPreferences 로 영구 저장.
 * Machine 데이터에 필드 추가 대신 별도 보관해 Firestore schema 변경 없이 처리.
 *
 * 사용:
 *  - MachineListScreen 정렬: 즐겨찾기 → 그 외
 *  - MachineCard 의 별 아이콘 토글
 */
object FavoriteMachines {

    private const val PREFS = "favorite_machines"
    private const val KEY = "ids"

    fun load(context: Context): Set<String> =
        prefs(context).getStringSet(KEY, emptySet())?.toSet() ?: emptySet()

    fun toggle(context: Context, machineId: String): Boolean {
        val current = load(context).toMutableSet()
        val nowFavorite = if (machineId in current) {
            current.remove(machineId); false
        } else {
            current.add(machineId); true
        }
        prefs(context).edit().putStringSet(KEY, current).apply()
        return nowFavorite
    }

    fun isFavorite(context: Context, machineId: String): Boolean =
        machineId in load(context)

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}

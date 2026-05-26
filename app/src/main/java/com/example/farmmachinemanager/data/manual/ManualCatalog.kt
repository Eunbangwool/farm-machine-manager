package com.example.farmmachinemanager.data.manual

/**
 * 앱이 보유한 매뉴얼 데이터셋 카탈로그.
 *
 * 새 브랜드·모델 매뉴얼을 추가하려면:
 *  1. app/src/main/assets/manuals/{새_디렉토리}/ 에 JSON 데이터셋 배치
 *  2. ManualKey 에 새 항목 추가 + ManualRepository 에 load 메서드 추가
 *  3. 아래 entries 에 Entry 한 줄 추가
 *  → 기계 등록 화면의 '매뉴얼 모델' 선택지에 자동 노출됨.
 */
object ManualCatalog {

    data class Entry(
        /** 안정적 ID. Machine.manualId 에 저장됨. ManualKey.name 과 일치시킨다. */
        val id: String,
        val key: ManualKey,
        val brand: String,
        /** 선택 칩에 표시할 라벨. */
        val label: String,
    )

    val entries: List<Entry> = listOf(
        Entry(
            id = ManualKey.PLANTER.name,
            key = ManualKey.PLANTER,
            brand = "쿠보타",
            label = "쿠보타 이앙기 (NW 시리즈)",
        ),
        Entry(
            id = ManualKey.TRACTOR_MR1050.name,
            key = ManualKey.TRACTOR_MR1050,
            brand = "쿠보타",
            label = "쿠보타 트랙터 (MR1050·MR1157)",
        ),
    )

    fun keyForId(id: String?): ManualKey? =
        id?.let { entries.firstOrNull { e -> e.id == it }?.key }

    fun labelForId(id: String?): String? =
        id?.let { entries.firstOrNull { e -> e.id == it }?.label }
}

package com.example.farmmachinemanager.data.manual

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * 안드로이드 assets 폴더에서 농기계 매뉴얼 JSON 을 읽어 파싱한다.
 *
 * 데이터셋 경로:
 *  - 이앙기 (kubota_planter): app/src/main/assets/manuals/kubota_planter/(category).json
 *  - 트랙터 (kubota_tractor_mr1050): app/src/main/assets/manuals/kubota_tractor_mr1050/(category).json
 *
 * 데이터는 lazy + 메모리 캐싱.
 */
class ManualRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    // MASTER_INDEX 의 정식 ID. 이전 'kubota_planter' 폴더는 삭제됨.
    private val planterBase = "manuals/kubota_transplanter_nw50_80"
    private val tractorMr1050Base = "manuals/kubota_tractor_mr1050"

    // ----- 이앙기 캐시 -----
    private var indexCache: ManualIndex? = null
    private var troubleshootingCache: TroubleshootingData? = null
    private var inspectionCache: InspectionScheduleData? = null
    private var lubricationCache: LubricationScheduleData? = null
    private var consumablesCache: ConsumablesData? = null
    private var fuseCache: FuseCircuitsData? = null
    private var specificationsCache: SpecificationsData? = null

    // ----- 트랙터 (MR1050) 캐시 -----
    private var tractorIndexCache: ManualIndex? = null
    private var tractorInspectionCache: TractorInspectionScheduleData? = null
    private var tractorLubricationCache: TractorLubricationScheduleData? = null
    private var tractorConsumablesCache: TractorConsumablesData? = null
    private var tractorSpecificationsCache: TractorSpecificationsData? = null
    private var tractorWarningLightsCache: TractorWarningLightsData? = null
    private var tractorTroubleshootingCache: TractorTroubleshootingData? = null

    // ===== 이앙기 (기존) =====

    suspend fun loadIndex(): ManualIndex = withContext(Dispatchers.IO) {
        indexCache ?: decode<ManualIndex>(planterBase, "index.json").also { indexCache = it }
    }

    suspend fun loadTroubleshooting(): TroubleshootingData = withContext(Dispatchers.IO) {
        troubleshootingCache ?: decode<TroubleshootingData>(planterBase, "troubleshooting.json").also { troubleshootingCache = it }
    }

    suspend fun loadInspectionSchedule(): InspectionScheduleData = withContext(Dispatchers.IO) {
        inspectionCache ?: decode<InspectionScheduleData>(planterBase, "inspection_schedule.json").also { inspectionCache = it }
    }

    suspend fun loadLubricationSchedule(): LubricationScheduleData = withContext(Dispatchers.IO) {
        lubricationCache ?: decode<LubricationScheduleData>(planterBase, "lubrication_schedule.json").also { lubricationCache = it }
    }

    suspend fun loadConsumables(): ConsumablesData = withContext(Dispatchers.IO) {
        consumablesCache ?: decode<ConsumablesData>(planterBase, "consumables.json").also { consumablesCache = it }
    }

    suspend fun loadFuseCircuits(): FuseCircuitsData = withContext(Dispatchers.IO) {
        fuseCache ?: decode<FuseCircuitsData>(planterBase, "fuse_circuits.json").also { fuseCache = it }
    }

    suspend fun loadSpecifications(): SpecificationsData = withContext(Dispatchers.IO) {
        specificationsCache ?: decode<SpecificationsData>(planterBase, "specifications.json").also { specificationsCache = it }
    }

    // ===== 트랙터 MR1050 (신규) =====
    // MR1050 과 MR1157 은 엔진 외 구조 동일하므로 본 데이터셋을 둘 다 참조.

    suspend fun loadTractorIndex(): ManualIndex = withContext(Dispatchers.IO) {
        tractorIndexCache ?: decode<ManualIndex>(tractorMr1050Base, "index.json").also { tractorIndexCache = it }
    }

    suspend fun loadTractorInspectionSchedule(): TractorInspectionScheduleData = withContext(Dispatchers.IO) {
        tractorInspectionCache ?: decode<TractorInspectionScheduleData>(tractorMr1050Base, "inspection_schedule.json")
            .also { tractorInspectionCache = it }
    }

    suspend fun loadTractorLubricationSchedule(): TractorLubricationScheduleData = withContext(Dispatchers.IO) {
        tractorLubricationCache ?: decode<TractorLubricationScheduleData>(tractorMr1050Base, "lubrication_schedule.json")
            .also { tractorLubricationCache = it }
    }

    suspend fun loadTractorConsumables(): TractorConsumablesData = withContext(Dispatchers.IO) {
        tractorConsumablesCache ?: decode<TractorConsumablesData>(tractorMr1050Base, "consumables.json")
            .also { tractorConsumablesCache = it }
    }

    suspend fun loadTractorSpecifications(): TractorSpecificationsData = withContext(Dispatchers.IO) {
        tractorSpecificationsCache ?: decode<TractorSpecificationsData>(tractorMr1050Base, "specifications.json")
            .also { tractorSpecificationsCache = it }
    }

    suspend fun loadTractorWarningLights(): TractorWarningLightsData = withContext(Dispatchers.IO) {
        tractorWarningLightsCache ?: decode<TractorWarningLightsData>(tractorMr1050Base, "warning_lights.json")
            .also { tractorWarningLightsCache = it }
    }

    suspend fun loadTractorTroubleshooting(): TractorTroubleshootingData = withContext(Dispatchers.IO) {
        tractorTroubleshootingCache ?: decode<TractorTroubleshootingData>(tractorMr1050Base, "troubleshooting.json")
            .also { tractorTroubleshootingCache = it }
    }

    // ----- 머신 → 매뉴얼 매핑 -----

    /**
     * 머신 모델 이름으로 어떤 매뉴얼 데이터셋을 보여줄지 결정.
     * - "MR1050" / "MR1157" → 트랙터 MR1050 매뉴얼 (엔진 외 구조 동일)
     * - 그 외 트랙터 → 매뉴얼 없음 (null)
     * - 이앙기 → 이앙기 매뉴얼 (null 이 아닌 임의 token)
     */
    fun manualKeyForMachine(
        machineName: String,
        isTractor: Boolean,
        isRicePlanter: Boolean,
        manualId: String? = null,
    ): ManualKey? {
        // 1) 등록 시 사용자가 명시적으로 선택한 매뉴얼이 있으면 그것을 우선.
        ManualCatalog.keyForId(manualId)?.let { return it }
        // 2) 미선택 시 모델명·종류로 자동 추론 (하위 호환).
        val upper = machineName.uppercase().replace(" ", "")
        return when {
            upper.contains("MR1050") || upper.contains("MR1157") -> ManualKey.TRACTOR_MR1050
            isRicePlanter -> ManualKey.PLANTER
            else -> null
        }
    }

    private inline fun <reified T> decode(basePath: String, fileName: String): T {
        val text = context.assets.open("$basePath/$fileName").bufferedReader().use { it.readText() }
        return json.decodeFromString(text)
    }

    /**
     * 임의 머신의 index.json 원문을 읽어 JsonElement 로 반환.
     * 머신별 스키마가 자유롭기 때문에(콤바인 통합 index, 트랙터 메타 index 등)
     * 대백과 화면은 일반 트리 구조로 표시하기 위해 raw 노드를 받아간다.
     */
    suspend fun loadMachineIndexElement(machineId: String): kotlinx.serialization.json.JsonElement? =
        withContext(Dispatchers.IO) {
            runCatching {
                val text = context.assets.open("manuals/$machineId/index.json")
                    .bufferedReader().use { it.readText() }
                json.parseToJsonElement(text)
            }.getOrNull()
        }

    /** 임의 머신·데이터셋 파일 존재 확인 (예: "inspection_schedule.json"). */
    fun hasDataset(machineId: String, fileName: String): Boolean = runCatching {
        context.assets.open("manuals/$machineId/$fileName").close()
        true
    }.getOrDefault(false)
}

enum class ManualKey {
    PLANTER,
    TRACTOR_MR1050,
}

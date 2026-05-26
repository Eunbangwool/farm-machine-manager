package com.example.farmmachinemanager.data.manual

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * 안드로이드 assets 폴더에서 쿠보타 이앙기 매뉴얼 JSON을 읽어 파싱한다.
 *
 * 파일 경로: app/src/main/assets/manuals/kubota_planter/(category).json
 *
 * 데이터는 lazy + 메모리 캐싱. 같은 데이터셋을 두 번 요청하면 두 번째부터는
 * 디스크 I/O 없이 캐시에서 반환한다.
 */
class ManualRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val basePath = "manuals/kubota_planter"

    private var indexCache: ManualIndex? = null
    private var troubleshootingCache: TroubleshootingData? = null
    private var inspectionCache: InspectionScheduleData? = null
    private var lubricationCache: LubricationScheduleData? = null
    private var consumablesCache: ConsumablesData? = null
    private var fuseCache: FuseCircuitsData? = null
    private var specificationsCache: SpecificationsData? = null

    suspend fun loadIndex(): ManualIndex = withContext(Dispatchers.IO) {
        indexCache ?: decode<ManualIndex>("index.json").also { indexCache = it }
    }

    suspend fun loadTroubleshooting(): TroubleshootingData = withContext(Dispatchers.IO) {
        troubleshootingCache ?: decode<TroubleshootingData>("troubleshooting.json").also { troubleshootingCache = it }
    }

    suspend fun loadInspectionSchedule(): InspectionScheduleData = withContext(Dispatchers.IO) {
        inspectionCache ?: decode<InspectionScheduleData>("inspection_schedule.json").also { inspectionCache = it }
    }

    suspend fun loadLubricationSchedule(): LubricationScheduleData = withContext(Dispatchers.IO) {
        lubricationCache ?: decode<LubricationScheduleData>("lubrication_schedule.json").also { lubricationCache = it }
    }

    suspend fun loadConsumables(): ConsumablesData = withContext(Dispatchers.IO) {
        consumablesCache ?: decode<ConsumablesData>("consumables.json").also { consumablesCache = it }
    }

    suspend fun loadFuseCircuits(): FuseCircuitsData = withContext(Dispatchers.IO) {
        fuseCache ?: decode<FuseCircuitsData>("fuse_circuits.json").also { fuseCache = it }
    }

    suspend fun loadSpecifications(): SpecificationsData = withContext(Dispatchers.IO) {
        specificationsCache ?: decode<SpecificationsData>("specifications.json").also { specificationsCache = it }
    }

    private inline fun <reified T> decode(fileName: String): T {
        val text = context.assets.open("$basePath/$fileName").bufferedReader().use { it.readText() }
        return json.decodeFromString(text)
    }
}

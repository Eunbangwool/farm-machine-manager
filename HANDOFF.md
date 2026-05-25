# FarmMachineManager - Claude Code 인계 문서

> **목적**: 이 문서는 Claude.ai 모바일 채팅에서 진행한 작업을 Claude Code(터미널 환경)로 이전하기 위한 완전한 컨텍스트 문서입니다.
> **작성일**: 2026-05-22
> **다음 환경**: Claude Code (로컬 macOS / `/Users/soorin/AndroidStudioProjects/FarmMachineManager`)

---

## 1. 프로젝트 개요

### 1.1 기본 정보
- **프로젝트명**: FarmMachineManager (농기계 관리 안드로이드 앱)
- **GitHub**: https://github.com/Eunbangwool/farm-machine-manager
- **로컬 경로**: `/Users/soorin/AndroidStudioProjects/FarmMachineManager`
- **기술 스택**: Kotlin + Jetpack Compose
- **빌드**: GitHub Actions → APK 자동 생성 → Releases 배포
- **마지막 버전**: v19 fix 부근 (정확한 버전은 GitHub Releases 확인 필요)

### 1.2 현재 작업 컨텍스트
사용자가 쿠보타(KUBOTA) 이앙기 일본어 취급설명서 PDF(`kubota_planter.pdf`, 283페이지, 38MB)에서 정보를 추출해 앱 데이터로 활용하는 작업을 진행 중. 이전 채팅에서 **7개 JSON 데이터셋**을 생성 완료했고, 이제 이를 안드로이드 앱에 통합하는 단계임.

---

## 2. 완료된 작업 (생성된 JSON 데이터)

### 2.1 데이터 출처
- **PDF 파일**: `kubota_planter.pdf` (사용자가 업로드)
- **기계**: 쿠보타 NW50N/50S/60N/60S/80N/80S "나비웰(ナビウェル)" 시리즈
- **매뉴얼 번호**: PW600-9751-4
- **언어**: 일본어 → 한국어 번역 추가

### 2.2 7개 JSON 파일 일람

| 파일명 | 내용 | 크기 | 데이터 양 |
|---|---|---|---|
| `index.json` | 마스터 인덱스 | 4.7KB | - |
| `troubleshooting.json` | 트러블슈팅 | 29.3KB | 7개 카테고리, 32개 케이스 |
| `inspection_schedule.json` | 정기점검 일람표 | 15.9KB | 5개 섹션, 34개 항목 |
| `lubrication_schedule.json` | 급유·주유 일람표 | 7.0KB | 8개 항목 |
| `consumables.json` | 소모품 부품 카탈로그 | 13.8KB | 9개 카테고리, 39개 부품 |
| `fuse_circuits.json` | 퓨즈 회로표 | 6.4KB | 3개 박스, 37개 회로 |
| `specifications.json` | 주요 제원 | 7.9KB | 6개 모델, 11개 사양 변형 |

**총 크기**: 약 96KB

### 2.3 데이터 설계 원칙

1. **이중 언어 필드**: 모든 텍스트는 `_ja` (일본어 원문) + `_ko` (한국어 번역) 페어
   - 예: `name_ja: "苗ステー"`, `name_ko: "모 스테이"`
   - 일본어로도 검색 가능하게 의도

2. **모델별 필터링**:
   - `applies_to: ["NW80N", "NW80S"]` — 특정 모델 배열
   - `applies_to_spec: "F"` — 사양 변형 (F=시비기, GS=직진키프, Q/Q2/PF/CY/Y/M/W 등)
   - `applies_to: "all"` — 전체 적용

3. **매뉴얼 페이지 추적**: `page_ref` 필드로 PDF 원본 페이지 추적 가능
   - 앱에서 "더 보기 → 매뉴얼 p.240 참조" 형식으로 안내 가능

4. **ID 체계**:
   - `TS-XXX` 트러블슈팅 (TS-001 ~ TS-032)
   - `IN-XXX` 점검 (IN-001 ~ IN-034)
   - `LU-XXX` 급유·주유 (LU-001 ~ LU-008)
   - `PT-XXX` 부품 (PT-001 ~ PT-039)

5. **Room DB 매핑 친화적**: 평면(flat) 구조로 변환 용이

### 2.4 각 데이터셋 상세 구조

#### troubleshooting.json
```json
{
  "machine": {...},
  "categories": [
    {"id": "missing_plant", "name_ko": "결주(빈 그루) 발생", ...}
  ],
  "cases": [
    {
      "id": "TS-001",
      "category_id": "missing_plant",
      "cause_ja": "...",
      "cause_ko": "...",
      "symptoms_ko": [...],
      "remedies": [
        {"type": "seedling", "type_ko": "모 처치", "action_ko": "..."}
      ],
      "page_ref": 240
    }
  ]
}
```

7개 카테고리:
- `missing_plant` 결주
- `floating_plant_disorder` 부묘+식부 흐트러짐
- `floating_plant_poor` 부묘+식부 불량
- `disorder_and_missing` 식부 흐트러짐+결주
- `fertilizer_problem` 시비 불량 [F사양]
- `other_seedling_field` 모·논 기타 불량
- `straight_keep_problem` 직진 키프 불량 [GS사양]

처치 타입(`type`): `machine`(기계), `seedling`(모), `field`(논), `fertilizer`(비료), `operation`(조작), `note`(보충), `reference`(참조), `service`(서비스)

#### inspection_schedule.json
```json
{
  "legend": {
    "action_symbols": {"○": "점검과 조정", "△": "교환", "▲": "초기 길들임 후 교환", "☆": "구입처 연락 필요"}
  },
  "sections": [
    {"id": "engine", "name_ko": "엔진부"}, ...
  ],
  "items": [
    {
      "id": "IN-001",
      "section_id": "engine",
      "name_ko": "올터네이터 구동 벨트",
      "actions": [
        {"type": "inspect_adjust", "interval_ko": "100시간마다"},
        {"type": "replace", "interval_ko": "200시간마다"}
      ],
      "service_required": true,
      "page_ref": 223
    }
  ]
}
```

5개 섹션: `engine` 엔진부, `driving_operation` 주행·조작부, `planting` 이앙부, `fertilizer` 시비부[F사양], `electrical` 전장부

#### lubrication_schedule.json
```json
{
  "categories": [
    {"id": "fuel", "name_ko": "연료"},
    {"id": "oil", "name_ko": "오일"},
    {"id": "coolant", "name_ko": "수·액"},
    {"id": "grease", "name_ko": "그리스"}
  ],
  "items": [
    {
      "id": "LU-002",
      "category_id": "oil",
      "location_ko": "엔진 오일",
      "replacement_interval_ko": "초회 35시간[50/60S]·50시간[80S] / 이후 200시간마다",
      "capacity_ko": "약 3.7L [50/60S] / 약 3.4L [80S]",
      "lubricant_type_ko": "쿠보타 순정 오일 D10W-30"
    }
  ]
}
```

#### consumables.json
```json
{
  "parts": [
    {
      "id": "PT-001",
      "category_id": "planting_blade",
      "name_ko": "이앙 발톱(식부조) RIS13",
      "part_number": "PA401-5171-0",
      "quantities_by_model": {
        "NW50N": 10, "NW50S": 10,
        "NW60N": 12, "NW60S": 12,
        "NW80N": 16, "NW80S": 16
      }
    }
  ]
}
```

9개 카테고리: `planting_blade` 이앙·식부, `lamp` 램프, `fuse` 퓨즈, `filter` 필터, `belt` 벨트, `battery` 배터리, `fertilizer_part` 시비부품[F], `seedling_table` 모대, `wheel` 차륜

#### fuse_circuits.json
```json
{
  "boxes": [
    {
      "id": "fuse_box_1",
      "name_ko": "퓨즈 박스 1",
      "location_ko": "운전석 아래 커버(우측) 안",
      "circuits": [
        {"number": 1, "circuit_ko": "조향 드라이버", "capacity_amp": 30}
      ]
    }
  ]
}
```

3개 박스: 퓨즈박스1(25회로), 퓨즈박스2(8회로), 슬로우블로우퓨즈(4회로)

#### specifications.json
6개 모델별 상세 제원 + 11개 사양 변형 설명.
사양 변형: F, GS, Q, Q2, PF, CY24, Y32/CY32/MY32, M, W, W2, DCU

---

## 3. 다음 작업 (앱 통합 단계)

사용자가 진행하길 원했던 단계:

### 3.1 권장 통합 경로
```
app/src/main/assets/manuals/kubota_planter/
  ├── index.json
  ├── troubleshooting.json
  ├── inspection_schedule.json
  ├── lubrication_schedule.json
  ├── consumables.json
  ├── fuse_circuits.json
  └── specifications.json
```

### 3.2 권장 다음 작업 (사용자에게 마지막으로 제안한 것)

> "원하시면 위 4개 중 어느 것부터 코드 작업할까요? 아니면 다른 이앙기 매뉴얼 PDF도 추가로 같은 형식으로 정리할까요?"

**제안한 4가지 UI 화면**:
1. **TroubleshootingScreen**: 카테고리 → 케이스 리스트 → 상세 화면
2. **InspectionChecklistScreen**: 사용 시간 기반 알림 + 체크리스트
3. **PartsListScreen**: 부품번호 검색
4. **FuseGuideScreen**: 퓨즈 박스 시각화

**필요한 인프라**:
1. Kotlin 데이터 클래스 (kotlinx.serialization 사용)
2. Repository 패턴 — JSON 파싱 + 캐싱
3. ViewModel 계층
4. Navigation 통합

### 3.3 권장 Kotlin 데이터 클래스 구조

```kotlin
@Serializable
data class TroubleshootingCase(
    val id: String,
    @SerialName("category_id") val categoryId: String,
    @SerialName("cause_ja") val causeJa: String? = null,
    @SerialName("cause_ko") val causeKo: String,
    @SerialName("symptoms_ko") val symptomsKo: List<String>,
    val remedies: List<Remedy>,
    @SerialName("page_ref") val pageRef: Int,
    @SerialName("applies_to") val appliesTo: List<String>? = null,
    @SerialName("applies_to_spec") val appliesToSpec: String? = null
)

@Serializable
data class Remedy(
    val type: String,
    @SerialName("type_ko") val typeKo: String,
    @SerialName("action_ko") val actionKo: String,
    @SerialName("manual_page_ref") val manualPageRef: Int? = null
)

@Serializable
data class InspectionItem(
    val id: String,
    @SerialName("section_id") val sectionId: String,
    @SerialName("name_ja") val nameJa: String,
    @SerialName("name_ko") val nameKo: String,
    val actions: List<InspectionAction>,
    @SerialName("applies_to") val appliesTo: String? = null,
    @SerialName("applies_to_spec") val appliesToSpec: String? = null,
    @SerialName("service_required") val serviceRequired: Boolean = false,
    @SerialName("page_ref") val pageRef: Int? = null
)

@Serializable
data class ConsumablePart(
    val id: String,
    @SerialName("category_id") val categoryId: String,
    @SerialName("name_ja") val nameJa: String,
    @SerialName("name_ko") val nameKo: String,
    @SerialName("part_number") val partNumber: String,
    val quantity: Int? = null,
    @SerialName("quantities_by_model") val quantitiesByModel: Map<String, Int>? = null,
    @SerialName("applies_to") val appliesTo: List<String>? = null,
    @SerialName("page_ref") val pageRef: Int? = null
)
```

### 3.4 권장 Repository 패턴

```kotlin
class ManualRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    
    suspend fun loadTroubleshooting(): TroubleshootingData = withContext(Dispatchers.IO) {
        context.assets.open("manuals/kubota_planter/troubleshooting.json")
            .bufferedReader().use { json.decodeFromString(it.readText()) }
    }
    
    // 동일 패턴으로 나머지 6개 메소드
}
```

---

## 4. 인계 절차 (Claude Code에서 할 일)

### 4.1 첫 번째로 할 일
1. 이 문서(`HANDOFF.md`) 읽기
2. `/Users/soorin/AndroidStudioProjects/FarmMachineManager` 디렉토리 구조 파악
3. 7개 JSON 파일을 `app/src/main/assets/manuals/kubota_planter/` 에 배치
   - JSON 파일들은 사용자가 다운로드 받아서 직접 옮겨야 함
   - 또는 사용자가 Claude.ai에서 받은 파일을 첨부해주면 그걸 사용
4. 사용자에게 어느 UI부터 시작할지 확인 후 작업 시작

### 4.2 사용자가 Claude Code에 처음 보낼 메시지 (예시)

```
이 HANDOFF.md 문서를 읽고 FarmMachineManager 프로젝트 상태를 파악해줘.
이전에 모바일 채팅에서 쿠보타 이앙기 매뉴얼 PDF에서 7개 JSON 데이터셋을
추출했고, 이제 안드로이드 앱에 통합하는 단계야.

JSON 파일들은 ~/Downloads에 있어 (또는 첨부할게).
먼저 프로젝트 현재 구조 확인하고, JSON을 app/src/main/assets에 배치해.
그다음 TroubleshootingScreen부터 만들어줘 (Compose + Material 3).
```

### 4.3 Claude Code 작업 순서 권장

1. **프로젝트 파악**
   ```bash
   cd ~/AndroidStudioProjects/FarmMachineManager
   ls -la app/src/main/
   cat app/build.gradle.kts  # 또는 build.gradle
   ```

2. **kotlinx.serialization 의존성 추가** (없다면)
   ```kotlin
   // app/build.gradle.kts
   plugins {
       kotlin("plugin.serialization") version "1.9.x"
   }
   dependencies {
       implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.x")
   }
   ```

3. **JSON 파일 배치**
   ```bash
   mkdir -p app/src/main/assets/manuals/kubota_planter
   cp ~/Downloads/{index,troubleshooting,inspection_schedule,lubrication_schedule,consumables,fuse_circuits,specifications}.json \
      app/src/main/assets/manuals/kubota_planter/
   ```

4. **데이터 모델 + Repository 작성** (위 3.3 참조)

5. **첫 화면 (TroubleshootingScreen) 구현**

6. **GitHub Actions 빌드 확인** → 새 버전 릴리즈

---

## 5. 중요한 컨텍스트 정보

### 5.1 사용자 환경
- macOS 사용자
- Android Studio 사용
- `~/AndroidStudioProjects/FarmMachineManager` 경로
- GitHub Actions로 APK 자동 빌드 → Releases 배포
- 모바일 채팅에서는 PDF 분석 + 데이터 추출 중심으로 작업

### 5.2 사용자 작업 스타일 (이전 대화에서 관찰됨)
- 단계적 진행 선호 ("전체 다, 단계적으로")
- JSON 같은 표준 데이터 형식 선호
- 빠른 시연 + 즉시 검증 가능한 결과물 좋아함

### 5.3 추가 컨텍스트
- 이앙기는 한국 농가에서도 자주 사용
- 일본어 매뉴얼이라 일본어 부품명 검색도 가능하게 설계함
- 향후 다른 농기계 매뉴얼(트랙터, 콤바인 등)도 같은 형식으로 확장 가능

---

## 6. 트러블슈팅 케이스 샘플 (참고용)

```json
{
  "id": "TS-007",
  "category_id": "missing_plant",
  "cause_ja": "床土が粘土質で粘りが強い苗 / 粘土質のほ場でしかも水が少ない",
  "cause_ko": "상토가 점토질로 점도가 강함 / 점토질 논에 물이 적음",
  "symptoms_ko": ["이앙 시 모가 식부조에서 떨어지지 않아 결주 발생"],
  "remedies": [
    {"type": "seedling", "type_ko": "모 처치", "action_ko": "모판을 약간 건조시키거나 물에 담가 수분을 충분히 머금게 한다"},
    {"type": "field", "type_ko": "논 처치", "action_ko": "논에 물을 1~3cm 정도 채워 모가 식부조에서 잘 떨어지도록 한다"},
    {"type": "machine", "type_ko": "기계 처치", "action_ko": "클리너(밀어내기 금구)를 식부 암의 스터드 볼트에 함께 체결하여 장착한다"}
  ],
  "page_ref": 241
}
```

---

## 7. 마지막 사용자 메시지 컨텍스트

사용자의 마지막 두 답변:
1. "어떤 정보를 우선 앱 데이터로 만들까요?" → **"전체 다 (단계적으로)"**
2. "데이터 형식은 어떻게 할까요?" → **"JSON (앱에서 바로 import)"**

→ 7개 JSON 모두 생성 완료 후, 사용자가 "이 대화를 claude code로 전부 이전" 요청 → 이 문서 생성으로 이어짐.

---

## 8. 체크리스트 (Claude Code에서 확인할 것)

- [ ] HANDOFF.md 읽고 컨텍스트 파악
- [ ] 프로젝트 현재 구조 확인 (`ls`, `find`, 등)
- [ ] 7개 JSON 파일 위치 확인 (사용자에게 물어볼 수 있음)
- [ ] JSON을 `app/src/main/assets/manuals/kubota_planter/` 로 이동
- [ ] kotlinx.serialization 의존성 확인/추가
- [ ] 데이터 모델 클래스 생성
- [ ] Repository 패턴 구현
- [ ] 첫 화면 (TroubleshootingScreen) 구현
- [ ] Navigation 통합
- [ ] 빌드 테스트 (로컬 또는 GitHub Actions)

---

**문서 끝**. 추가 질문이 있으면 사용자에게 직접 물어보세요.

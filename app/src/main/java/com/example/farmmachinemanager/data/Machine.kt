package com.example.farmmachinemanager.data

import java.time.LocalDate

/**
 * 기계 종류
 * 한국 농가에서 흔히 운용하는 농기계 위주로 구성
 */
enum class MachineType(val displayName: String) {
    TRACTOR("트랙터"),
    COMBINE("콤바인"),
    RICE_TRANSPLANTER("이앙기"),
    CULTIVATOR("관리기"),
    FORKLIFT("지게차"),
    VEHICLE("차량"),
    OTHER("기타")
}

/**
 * 기계 상태
 * NORMAL: 정상 운용 중
 * INSPECTION_NEEDED: 점검 필요 (소모품 교체 임박 등)
 * UNDER_REPAIR: 수리 중 (현재 사용 불가)
 */
enum class MachineStatus(val displayName: String) {
    NORMAL("정상"),
    INSPECTION_NEEDED("점검필요"),
    UNDER_REPAIR("수리중")
}

/**
 * 기계 데이터 모델
 *
 * @param id Firestore 문서 ID (백엔드 연동 시 자동 생성)
 * @param name 모델명 (예: "DK7320")
 * @param manufacturer 제조사 (예: "대동", "구보타")
 * @param type 기계 종류
 * @param horsepower 마력 (트랙터/콤바인에서 핵심 스펙)
 * @param operatingHours 누적 가동시간 (농기계는 시간 단위가 표준)
 * @param status 현재 상태
 * @param statusNote 상태 부가 설명 (예: "엔진오일 임박", "3일 전")
 */
data class Machine(
    val id: String,
    val name: String,
    val manufacturer: String,
    val type: MachineType,
    val customTypeName: String? = null,
    val horsepower: Int? = null,
    val serialNumber: String? = null,
    val registrationNumber: String? = null,
    val year: Int? = null,
    val operatingHours: Double = 0.0,
    val status: MachineStatus = MachineStatus.NORMAL,
    val statusNote: String? = null,
    val lastMaintenanceDate: LocalDate? = null,
    val photoUrl: String? = null,
    val notes: String? = null
) {
    /** 화면에 표시할 종류 라벨. customTypeName이 있으면 우선 사용. */
    val typeDisplay: String
        get() = customTypeName ?: type.displayName
}

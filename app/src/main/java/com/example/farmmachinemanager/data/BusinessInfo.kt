package com.example.farmmachinemanager.data

/**
 * 사업자 정보. 전자상거래법상 통신판매업 신고된 앱은 다음 정보를 노출해야 함.
 *
 * 변경 시 이 파일과 sangwolnongsan.com 의 약관/처리방침 HTML 둘 다 갱신 필요.
 * 농작이(farm-work-manager)와 같은 회사 운영 — 두 앱 동일.
 */
object BusinessInfo {
    const val COMPANY_NAME = "농업회사법인 상월농산"
    const val REPRESENTATIVE = "강정애"
    const val BUSINESS_REG_NUMBER = "308-81-13067"
    const val ADDRESS = "충남 논산시 상월면 산성리 12-1"
    const val PHONE = "041-732-4029"

    /** 통신판매업 신고번호 - 논산시청 등록 (2017-01-19) */
    const val ECOMMERCE_REG_NUMBER = "제2017-충남논산-0010호"

    const val EMAIL = "kja288@hanmail.net"

    /** 공식 웹사이트 - 가비아 등록 (2026, .com 도메인) */
    const val WEBSITE_URL = "https://www.sangwolnongsan.com"

    const val ESTABLISHED_YEAR = "2000"
    const val INDUSTRY = "곡물 도정업 · 농기계 관리 솔루션"
    const val HOSTING_PROVIDER = "Google Firebase (Cloud Firestore)"
}

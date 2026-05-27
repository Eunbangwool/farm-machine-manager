package com.example.farmmachinemanager.data

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

/**
 * 정비 영수증·부품 거래명세서 사진을 ML Kit 로 인식 후 정비 기록 필드로 파싱.
 *
 * on-device 인식 (인터넷 불필요, 무료). 한국어 + 라틴 문자.
 * 영수증/명세서 형식이 제각각이라 휴리스틱 — 사용자가 결과를 보고 수정하는 전제.
 *
 * 인식 대상 예: 농협 등 부품 거래명세서(품번·품명·수량·단가·금액 표).
 */
object ReceiptOcr {

    data class Parsed(
        val cost: Int? = null,
        val vendor: String? = null,
        val date: LocalDate? = null,
        /** 인식된 정비/부품 품목명 목록 (중복 제거). */
        val items: List<String> = emptyList(),
        /** 품목으로 추론한 정비 종류. */
        val suggestedType: MaintenanceType? = null,
        val rawText: String = "",
    )

    private val recognizer by lazy {
        TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
    }

    suspend fun scan(context: Context, uri: Uri): Parsed {
        val image = InputImage.fromFilePath(context, uri)
        val result = recognizer.process(image).await()
        val raw = result.text
        val lines = raw.lines().map { it.trim() }.filter { it.isNotBlank() }
        val items = parseItems(lines)
        return Parsed(
            cost = parseCost(lines),
            vendor = parseVendor(lines),
            date = parseDate(raw),
            items = items,
            suggestedType = suggestType(items),
            rawText = raw,
        )
    }

    // ── 금액: W/₩ 접두 또는 합계 키워드 줄 우선, 없으면 가장 큰 금액 ──
    private val amountRegex = Regex("""([0-9]{1,3}(?:[,.][0-9]{3})+|[0-9]{4,})\s*원?""")
    private val wonPrefixRegex = Regex("""[W₩]\s*([0-9]{1,3}(?:,[0-9]{3})+)""")
    private val totalKeywords =
        listOf("합계", "합 계", "총액", "총 금액", "공급가액", "공급가", "결제", "받을금액", "청구")

    private fun parseCost(lines: List<String>): Int? {
        // 1) W/₩ 접두 금액 (거래명세서 합계가 흔히 "W790,560") — 그 중 최대
        lines.asSequence()
            .flatMap { wonPrefixRegex.findAll(it) }
            .mapNotNull { it.groupValues[1].replace(",", "").toIntOrNull() }
            .filter { it in 100..100_000_000 }
            .maxOrNull()
            ?.let { return it }
        // 2) 합계 키워드가 있는 줄에서 숫자
        lines.firstOrNull { line -> totalKeywords.any { line.contains(it) } }
            ?.let { line -> amountInLine(line)?.let { return it } }
        // 3) fallback: 전체에서 가장 큰 금액
        return lines.mapNotNull { amountInLine(it) }.maxOrNull()
    }

    private fun amountInLine(line: String): Int? =
        amountRegex.findAll(line)
            .mapNotNull { it.groupValues[1].replace(",", "").replace(".", "").toIntOrNull() }
            .filter { it in 100..100_000_000 }
            .maxOrNull()

    // ── 날짜: 2026.05.25 / 2026-05-25 / 2026년 5월 25일 ──
    private val dateRegex = Regex("""(20\d{2})\s*[.\-/년]\s*(\d{1,2})\s*[.\-/월]\s*(\d{1,2})""")

    private fun parseDate(raw: String): LocalDate? {
        val m = dateRegex.find(raw) ?: return null
        return runCatching {
            LocalDate.of(m.groupValues[1].toInt(), m.groupValues[2].toInt(), m.groupValues[3].toInt())
        }.getOrNull()
    }

    // ── 업체명: 상호/사업자 라인 → 없으면 첫 텍스트 줄 ──
    private fun parseVendor(lines: List<String>): String? {
        lines.firstOrNull { it.contains("상호") || it.contains("사업자") }
            ?.let { line ->
                val after = line.substringAfter("상호", "").substringAfter("사업자", "")
                    .trim().trimStart(':', ' ', ')')
                if (after.isNotBlank()) return after.take(30)
            }
        return lines.firstOrNull { line ->
            line.length in 2..30 && line.any { it.isLetter() } && !line.all { it.isDigit() || it == ',' }
        }?.take(30)
    }

    // ── 품목: 농기계 정비/부품 키워드(한글+영문)로 라인 추출 ──
    private val partKeywordsKo = listOf(
        "엔진오일", "미션오일", "기어오일", "유압", "오일씰", "오일", "그리스",
        "에어크리너", "연료필터", "오일필터", "필터", "베어링", "부싱", "부쉬", "칼라",
        "스프링", "로터", "샤프트", "클로", "마커", "엘리먼트", "플레이트", "가이드",
        "가스켓", "패킹", "볼트", "너트", "와셔", "호스", "벨트", "체인", "스프로켓",
        "플러그", "배터리", "타이어", "튜브", "노즐", "펌프", "기어", "클러치", "브레이크",
        "라이너", "피스톤", "손잡이", "레버", "조립", "공임", "점검", "교체", "수리", "씰",
    )
    private val partKeywordsEn = listOf(
        "OIL", "FILTER", "BEARING", "SEAL", "BUSH", "SPRING", "ROTOR", "SHAFT", "CLAW",
        "MARKER", "ELEMENT", "PLATE", "GUIDE", "GASKET", "BOLT", "NUT", "HOSE", "BELT",
        "CHAIN", "PLUG", "BATTERY", "TIRE", "KIT", "ASSY", "PLANTING", "TORSION", "BALL",
        "COLLAR", "PIN", "WASHER", "VALVE", "PUMP", "GEAR", "CLUTCH", "BRAKE", "LINER",
        "PISTON", "RING", "CARTRIDGE", "FUEL", "SHEET", "COVER", "ARM", "LEVER",
    )
    private val excludeKeywords = listOf(
        "합계", "합 계", "소계", "부가세", "공급가", "세액", "사업자", "등록번호", "전화",
        "TEL", "대표", "주소", "거래처", "품번", "품명", "규격", "수량", "단가", "금액",
        "비고", "PAGE", "DATE", "NO.",
    )

    private fun parseItems(lines: List<String>): List<String> =
        lines.asSequence()
            .filter { line ->
                val upper = line.uppercase()
                excludeKeywords.none { upper.contains(it.uppercase()) } &&
                    (partKeywordsKo.any { line.contains(it) } ||
                        partKeywordsEn.any { upper.contains(it) })
            }
            .map { cleanItemName(it) }
            .filter { it.length in 2..40 && it.any { c -> c.isLetter() } }
            .distinct()
            .take(30)
            .toList()

    /** 라인에서 부품번호류·금액·수량 토큰을 떼고 품명만 남긴다. */
    private fun cleanItemName(line: String): String =
        line.split(Regex("\\s+"))
            .filterNot { tok ->
                // 부품번호(영숫자+하이픈), 순수 숫자/금액, 단위 토큰 제거
                tok.matches(Regex("[A-Za-z0-9]*[-/][A-Za-z0-9-/]*")) ||
                    tok.matches(Regex("[0-9][0-9,.\\s]*원?")) ||
                    tok.matches(Regex("[0-9]+(EA|ea|개|EA\\.)?"))
            }
            .joinToString(" ")
            .replace(Regex("\\s{2,}"), " ")
            .trim()
            .ifBlank { line.trim() }
            .take(40)

    private fun suggestType(items: List<String>): MaintenanceType? {
        if (items.isEmpty()) return null
        val joined = items.joinToString(" ")
        return when {
            listOf("점검", "검사", "INSPECT").any { joined.contains(it, ignoreCase = true) } ->
                MaintenanceType.INSPECTION
            listOf("수리", "공임", "REPAIR").any { joined.contains(it, ignoreCase = true) } ->
                MaintenanceType.REPAIR
            else -> MaintenanceType.CONSUMABLE_REPLACE
        }
    }
}

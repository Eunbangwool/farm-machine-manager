package com.example.farmmachinemanager.data

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

/**
 * 정비 영수증 사진을 ML Kit 로 텍스트 인식 후 정비 기록 필드로 파싱.
 *
 * on-device 인식 (인터넷 불필요, 무료). 한국어 + 라틴 문자.
 * 영수증 형식이 제각각이라 휴리스틱 파싱 — 사용자가 결과를 보고 수정하는 전제.
 */
object ReceiptOcr {

    data class Parsed(
        val cost: Int? = null,
        val vendor: String? = null,
        val date: LocalDate? = null,
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
        return Parsed(
            cost = parseCost(lines),
            vendor = parseVendor(lines),
            date = parseDate(raw),
            rawText = raw,
        )
    }

    // ── 금액: "합계/총액/결제/금액" 키워드 줄 우선, 없으면 가장 큰 숫자 ──
    private val amountRegex = Regex("""([0-9]{1,3}(?:[,.][0-9]{3})+|[0-9]{4,})\s*원?""")
    private val totalKeywords = listOf("합계", "총액", "총 금액", "결제", "받을금액", "청구", "합 계")

    private fun parseCost(lines: List<String>): Int? {
        // 1) 합계 키워드가 있는 줄에서 숫자 추출
        lines.firstOrNull { line -> totalKeywords.any { line.contains(it) } }
            ?.let { line -> amountInLine(line)?.let { return it } }
        // 2) fallback: 전체에서 가장 큰 금액
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
            LocalDate.of(
                m.groupValues[1].toInt(),
                m.groupValues[2].toInt(),
                m.groupValues[3].toInt(),
            )
        }.getOrNull()
    }

    // ── 업체명: 사업자/상호 라인, 없으면 첫 줄 (숫자만 줄 제외) ──
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
}

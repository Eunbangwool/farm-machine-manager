package com.example.farmmachinemanager.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmmachinemanager.BuildConfig
import com.example.farmmachinemanager.ui.theme.ActionDanger
import com.example.farmmachinemanager.ui.theme.ActionPrimary
import com.example.farmmachinemanager.ui.theme.ActionPrimaryText
import com.example.farmmachinemanager.ui.theme.BorderColor
import com.example.farmmachinemanager.ui.theme.StatusInspectionBg
import com.example.farmmachinemanager.ui.theme.StatusInspectionText
import com.example.farmmachinemanager.ui.theme.StatusRepairText
import com.example.farmmachinemanager.ui.theme.SurfacePrimary
import com.example.farmmachinemanager.ui.theme.SurfaceSecondary
import com.example.farmmachinemanager.ui.theme.TextPrimary
import com.example.farmmachinemanager.ui.theme.TextSecondary
import com.example.farmmachinemanager.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 설정 화면.
 *
 * 현재는 앱 정보(버전, 기기 정보)만 표시.
 * 향후 추가할 항목: 다크모드 토글, 회사 이름 변경, 데이터 백업/복원, Firebase 연동 등.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onTroubleshootingClick: () -> Unit = {},
    onInspectionChecklistClick: () -> Unit = {},
    onPartsListClick: () -> Unit = {},
    onFuseGuideClick: () -> Unit = {},
    onLubricationClick: () -> Unit = {},
    onSpecificationsClick: () -> Unit = {},
    onTractorTroubleshootingClick: () -> Unit = {},
    onTractorInspectionChecklistClick: () -> Unit = {},
    onTractorPartsListClick: () -> Unit = {},
    onTractorLubricationClick: () -> Unit = {},
    onTractorSpecificationsClick: () -> Unit = {},
    onTractorWarningLightsClick: () -> Unit = {},
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val info = remember { collectAppInfo(context) }

    var showBusiness by remember { mutableStateOf(false) }
    var showAttributions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        SettingsTopBar(onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(PaddingValues(16.dp)),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 앱 정보 카드 (카드 자체가 그룹 — 외부 라벨 없음)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfacePrimary)
                    .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            ) {
                CardHeader(title = "앱 정보")
                Divider()
                InfoRow(
                    icon = Icons.Outlined.Info,
                    label = "앱 이름",
                    value = if (BuildConfig.IS_DEBUG_APP) "농돌이 (디버그)" else "농돌이"
                )
                Divider()
                InfoRow(
                    icon = Icons.Outlined.Code,
                    label = "버전",
                    value = "${info.versionName} (build ${info.versionCode})"
                )
                Divider()
                InfoRow(
                    icon = Icons.Outlined.Schedule,
                    label = "빌드 시간",
                    value = formatBuildTime(BuildConfig.BUILD_TIME_MS)
                )
                Divider()
                InfoRow(
                    icon = Icons.Outlined.Update,
                    label = "패키지명",
                    value = info.packageName
                )
            }

            // 디버그 빌드일 때만 노출되는 진단 카드.
            if (BuildConfig.IS_DEBUG_APP) {
                DebugDiagnosticsCard()
            }

            // 동기화 카드 (FirebaseSyncSection 내부에 헤더 포함)
            FirebaseSyncSection()

            // 알림 카드 (NotificationSection 내부에 헤더 포함)
            NotificationSection()

            // 매뉴얼 카드 — 농작이 StatsMenuCard 풍 (이모지 + 라벨 + 설명 + chevron)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfacePrimary)
                    .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            ) {
                CardHeader(title = "매뉴얼 · 쿠보타 이앙기")
                Divider()
                ManualMenuRow(
                    emoji = "🛠️",
                    title = "쿠보타 이앙기 트러블슈팅",
                    subtitle = "고장 증상 → 원인 → 처치",
                    onClick = onTroubleshootingClick,
                )
                Divider()
                ManualMenuRow(
                    emoji = "📋",
                    title = "쿠보타 이앙기 정기점검 일람",
                    subtitle = "시간/시즌별 점검 일정",
                    onClick = onInspectionChecklistClick,
                )
                Divider()
                ManualMenuRow(
                    emoji = "⚙️",
                    title = "쿠보타 이앙기 소모품 부품",
                    subtitle = "부품번호 + 모델별 수량",
                    onClick = onPartsListClick,
                )
                Divider()
                ManualMenuRow(
                    emoji = "🔌",
                    title = "쿠보타 이앙기 퓨즈 가이드",
                    subtitle = "회로별 퓨즈 위치·정격",
                    onClick = onFuseGuideClick,
                )
                Divider()
                ManualMenuRow(
                    emoji = "🛢️",
                    title = "쿠보타 이앙기 급유·주유 일람",
                    subtitle = "오일·그리스 주입 위치",
                    onClick = onLubricationClick,
                )
                Divider()
                ManualMenuRow(
                    emoji = "📐",
                    title = "쿠보타 이앙기 주요 제원",
                    subtitle = "모델별 사양",
                    onClick = onSpecificationsClick,
                )
                Divider()
                ManualMenuRow(
                    emoji = "🔗",
                    title = "쿠보타 이앙기 공식 점검 가이드",
                    subtitle = "agriculture.kubota.co.jp (외부 브라우저)",
                    onClick = { openUrl(context, KUBOTA_TAUEKI_GUIDE_URL) },
                )
            }

            // 매뉴얼 카드 — 쿠보타 트랙터 (MR1050·MR1157)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfacePrimary)
                    .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            ) {
                CardHeader(title = "매뉴얼 · 쿠보타 트랙터")
                Divider()
                ManualMenuRow(
                    emoji = "🛠️",
                    title = "쿠보타 트랙터 트러블슈팅",
                    subtitle = "고장 증상 → 원인 → 처치 + 에러코드",
                    onClick = onTractorTroubleshootingClick,
                )
                Divider()
                ManualMenuRow(
                    emoji = "📋",
                    title = "쿠보타 트랙터 정기점검 일람",
                    subtitle = "시간/시즌별 점검 일정",
                    onClick = onTractorInspectionChecklistClick,
                )
                Divider()
                ManualMenuRow(
                    emoji = "⚙️",
                    title = "쿠보타 트랙터 소모품 부품",
                    subtitle = "부품번호 + 모델별 수량",
                    onClick = onTractorPartsListClick,
                )
                Divider()
                ManualMenuRow(
                    emoji = "🛢️",
                    title = "쿠보타 트랙터 급유·주유 일람",
                    subtitle = "오일·그리스 주입 위치",
                    onClick = onTractorLubricationClick,
                )
                Divider()
                ManualMenuRow(
                    emoji = "🚨",
                    title = "쿠보타 트랙터 경고등 가이드",
                    subtitle = "계기판 경고등 의미·대처",
                    onClick = onTractorWarningLightsClick,
                )
                Divider()
                ManualMenuRow(
                    emoji = "📐",
                    title = "쿠보타 트랙터 주요 제원",
                    subtitle = "모델별 사양",
                    onClick = onTractorSpecificationsClick,
                )
                Divider()
                ManualMenuRow(
                    emoji = "🔗",
                    title = "쿠보타 트랙터 공식 점검 가이드",
                    subtitle = "agriculture.kubota.co.jp (외부 브라우저)",
                    onClick = { openUrl(context, KUBOTA_TRACTOR_GUIDE_URL) },
                )
            }

            // 준비 중 카드
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfacePrimary)
                    .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            ) {
                CardHeader(title = "준비 중")
                Divider()
                NavRow(label = "회사 이름 변경", enabled = false)
                Divider()
                NavRow(label = "데이터 백업·복원", enabled = false)
                Divider()
                NavRow(label = "다크 모드", enabled = false)
            }

            // 더 보기 카드 (농작이 패턴 매칭)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfacePrimary)
                    .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            ) {
                CardHeader(title = "더 보기")
                Divider()
                LegalRow("이용약관") {
                    openExternalUrl(context, "https://www.sangwolnongsan.com/terms")
                }
                Divider()
                LegalRow("개인정보처리방침") {
                    openExternalUrl(context, "https://www.sangwolnongsan.com/privacy")
                }
                Divider()
                LegalRow("사업자 정보") { showBusiness = true }
                Divider()
                LegalRow("오픈소스 · 데이터 출처") { showAttributions = true }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // ── 다이얼로그 (Column 외부 emit — 본문 layout 영향 없음) ──

    if (showBusiness) {
        val biz = com.example.farmmachinemanager.data.BusinessInfo
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showBusiness = false },
            title = { Text("사업자 정보", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    BusinessRow("상호", biz.COMPANY_NAME)
                    BusinessRow("대표자", biz.REPRESENTATIVE)
                    BusinessRow("사업자등록번호", biz.BUSINESS_REG_NUMBER)
                    BusinessRow("통신판매업 신고", biz.ECOMMERCE_REG_NUMBER)
                    BusinessRow("사업장 주소", biz.ADDRESS)
                    BusinessRow("대표 전화", biz.PHONE)
                    BusinessRow("이메일", biz.EMAIL)
                    BusinessRow("웹사이트", biz.WEBSITE_URL)
                    BusinessRow("업종", biz.INDUSTRY)
                    BusinessRow("호스팅 제공", biz.HOSTING_PROVIDER)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "전자상거래법에 의거하여 사업자 정보를 공개합니다. 결제·환불·취소 등 문의는 위 전화나 이메일로 연락 주세요.",
                        fontSize = 10.sp,
                        color = TextTertiary,
                        lineHeight = 14.sp
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showBusiness = false }) {
                    Text("닫기")
                }
            }
        )
    }

    if (showAttributions) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAttributions = false },
            title = { Text("오픈소스 · 데이터 출처", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "이 앱은 다음 공공 데이터·오픈소스 라이브러리·매뉴얼을 사용합니다.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AttributionItem(
                        title = "쿠보타 매뉴얼 데이터",
                        body = "이앙기·트랙터 점검·소모품·트러블슈팅 데이터. © Kubota Corporation. 농돌이는 사용자의 정비 기록 보조 목적으로 일부 발췌 재구성.",
                    )
                    AttributionItem(
                        title = "Firebase / Google Play Services",
                        body = "Firestore 동기화. © Google Inc.",
                    )
                    AttributionItem(
                        title = "Jetpack Compose · Material Icons",
                        body = "Apache License 2.0. Android Open Source Project.",
                    )
                    AttributionItem(
                        title = "kotlinx-serialization",
                        body = "Apache License 2.0. JetBrains.",
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showAttributions = false }) {
                    Text("닫기")
                }
            }
        )
    }
}

/**
 * 더보기 카드 안의 행. 단순 라벨 + chevron.
 */
@Composable
private fun LegalRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun BusinessRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            modifier = Modifier.width(96.dp),
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AttributionItem(title: String, body: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = body,
            fontSize = 11.sp,
            color = TextSecondary,
            lineHeight = 16.sp,
        )
    }
}

/** 외부 브라우저로 URL 열기. */
private fun openExternalUrl(context: Context, url: String) {
    val intent = android.content.Intent(
        android.content.Intent.ACTION_VIEW,
        android.net.Uri.parse(url),
    )
    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}

private data class AppInfo(
    val versionName: String,
    val versionCode: Long,
    val packageName: String
)

private fun collectAppInfo(context: Context): AppInfo {
    return try {
        val pm = context.packageManager
        val pkgInfo: PackageInfo = pm.getPackageInfo(context.packageName, 0)
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            pkgInfo.longVersionCode
        else
            @Suppress("DEPRECATION") pkgInfo.versionCode.toLong()
        AppInfo(
            versionName = pkgInfo.versionName ?: "(없음)",
            versionCode = versionCode,
            packageName = context.packageName
        )
    } catch (_: Exception) {
        AppInfo("(불러오기 실패)", 0L, context.packageName)
    }
}

private val buildTimeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)

private fun formatBuildTime(ms: Long): String =
    if (ms <= 0L) "(없음)" else buildTimeFormatter.format(Date(ms))

@Composable
private fun DebugDiagnosticsCard() {
    val mode = com.example.farmmachinemanager.AppContainer.currentMode
    val farmCode = remember {
        runCatching { com.example.farmmachinemanager.AppContainer.farmCodeManager.farmCode }
            .getOrNull()
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StatusInspectionBg)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.BugReport,
                contentDescription = null,
                tint = StatusInspectionText,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "디버그 빌드 진단",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = StatusInspectionText
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        DiagnosticRow("IS_DEBUG_APP", BuildConfig.IS_DEBUG_APP.toString())
        DiagnosticRow("BUILD_TYPE", BuildConfig.BUILD_TYPE)
        DiagnosticRow("APPLICATION_ID", BuildConfig.APPLICATION_ID)
        DiagnosticRow("동기화 모드", mode.name)
        DiagnosticRow("농장 코드", farmCode ?: "(없음)")
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            label,
            fontSize = 11.sp,
            color = StatusInspectionText,
            modifier = Modifier.padding(end = 10.dp).width(120.dp)
        )
        Text(value, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = TextSecondary,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

/**
 * 농작이 StatsMenuCard 풍의 메뉴 행 — 이모지 + 라벨 + 부제 + chevron.
 * 매뉴얼 항목처럼 "탭하면 화면 이동" 패턴에 사용.
 */
@Composable
private fun ManualMenuRow(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(emoji, fontSize = 20.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun NavRow(label: String, enabled: Boolean, onClick: (() -> Unit)? = null) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .let { if (enabled && onClick != null) it.clickable(onClick = onClick) else it }
        .padding(horizontal = 16.dp, vertical = 14.dp)
    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (enabled) TextPrimary else TextTertiary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (enabled) "" else "곧 추가",
            fontSize = 11.sp,
            color = TextTertiary,
            modifier = Modifier.padding(end = 6.dp)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(BorderColor)
    )
}

@Composable
@Suppress("UNUSED_PARAMETER")
private fun SettingsTopBar(onBack: () -> Unit) {
    // 농작이 Header() 패턴: 뒤로가기 없이 큰 페이지 타이틀만.
    // 뒤로가기는 시스템 백버튼(BackHandler) 으로 처리.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfacePrimary)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "설정",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

/**
 * 카드 안 첫 행의 그룹 라벨. 농작이의 FontScaleSection 같은 카드 헤더 패턴 매칭.
 * 카드 헤더 + Divider + 본문 행 구조.
 */
@Composable
private fun CardHeader(title: String) {
    Text(
        text = title,
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary,
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)
    )
}

// ============ Firebase 동기화 섹션 ============

@Composable
private fun FirebaseSyncSection() {
    val mode = com.example.farmmachinemanager.AppContainer.currentMode
    val farmCodeManager = remember {
        runCatching { com.example.farmmachinemanager.AppContainer.farmCodeManager }.getOrNull()
    }
    var currentCode by remember {
        mutableStateOf(farmCodeManager?.farmCode)
    }
    val lastError by com.example.farmmachinemanager.AppContainer.lastFirestoreError
        .collectAsState()
    var showJoinDialog by remember { mutableStateOf(false) }
    var joinCodeInput by remember { mutableStateOf("") }
    var showLeaveDialog by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    var showRestartDialog by remember { mutableStateOf(false) }

    val (statusLabel, statusColor, statusDesc) = when (mode) {
        com.example.farmmachinemanager.AppContainer.SyncMode.FIRESTORE_SYNCED ->
            Triple(
                "연결됨",
                com.example.farmmachinemanager.ui.theme.StatusNormalText,
                if (currentCode != null) "농장 코드 $currentCode 로 다른 폰과 자동 동기화 중"
                else "Firestore 에 연결됨"
            )
        com.example.farmmachinemanager.AppContainer.SyncMode.LOCAL_ONLY ->
            Triple(
                "로컬 전용",
                TextSecondary,
                "농장 코드를 설정하면 다른 폰과 자동 동기화돼요"
            )
        com.example.farmmachinemanager.AppContainer.SyncMode.FIREBASE_NOT_CONFIGURED ->
            Triple(
                "Firebase 미설정",
                TextTertiary,
                "google-services.json 을 app/ 폴더에 추가 후 빌드 필요"
            )
    }

    // 카드 1: 동기화 상태 (정보 카드)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Sync,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "동기화 — $statusLabel",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
        }
        Text(
            text = statusDesc,
            fontSize = 12.sp,
            color = TextSecondary,
        )
        lastError?.let { err ->
            Spacer(modifier = Modifier.height(6.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(StatusInspectionBg)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "최근 동기화 오류",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StatusRepairText,
                )
                Text(
                    text = err,
                    fontSize = 11.sp,
                    color = TextPrimary,
                )
            }
        }
    }

    // Firebase 사용 가능 + 농장 코드 관리 가능 시에만 액션 카드들 표시
    if (mode != com.example.farmmachinemanager.AppContainer.SyncMode.FIREBASE_NOT_CONFIGURED &&
        farmCodeManager != null
    ) {
        // 카드 2: 새 농장 코드 생성
        FarmActionCard(
            title = if (currentCode == null) "내 농장 만들기" else "새 코드로 재시작",
            subtitle = if (currentCode == null)
                "6자리 코드가 자동 생성됩니다. 이 폰을 새 농장의 첫 폰으로 시작해요."
            else
                "기존 코드를 버리고 새 농장을 시작합니다. 이전 데이터는 Firestore 에 남습니다.",
            button = {
                PrimaryButton(label = if (currentCode == null) "내 농장 만들기" else "새 코드 생성") {
                    val code = farmCodeManager.generateNewCode()
                    com.example.farmmachinemanager.AppContainer.refreshSyncMode()
                    currentCode = code
                    showRestartDialog = true
                }
            },
        )

        // 카드 3: 기존 농장 참여
        FarmActionCard(
            title = "다른 폰 코드로 참여",
            subtitle = "이미 사용 중인 농장 코드를 입력해 같은 데이터를 함께 보세요.",
            button = {
                PrimaryButton(label = "참여하기") {
                    joinCodeInput = ""
                    showJoinDialog = true
                }
            },
        )

        // 카드 4: 농장 떠나기 (현재 코드 있을 때만)
        if (currentCode != null) {
            FarmActionCard(
                title = "농장 떠나기",
                subtitle = "이 폰을 로컬 전용으로 전환합니다. 데이터는 Firestore 에 남고, 같은 코드로 다시 참여할 수 있어요.",
                button = {
                    DangerButton(label = "농장 떠나기") {
                        showLeaveDialog = true
                    }
                },
                trailingInfo = "현재 코드: $currentCode",
            )
        }
    }

    // 농장 떠나기 확인
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("농장 떠나기") },
            text = {
                Text(
                    "이 폰의 농장 연결을 끊고 로컬 전용으로 전환합니다.\n" +
                            "농장 데이터는 Firestore에 그대로 남고, 같은 코드로 다시 참여할 수 있어요."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    farmCodeManager?.clearCode()
                    com.example.farmmachinemanager.AppContainer.refreshSyncMode()
                    currentCode = null
                    showLeaveDialog = false
                    showRestartDialog = true
                }) { Text("떠나기", color = StatusRepairText) }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) { Text("취소") }
            }
        )
    }

    // 코드 입력 다이얼로그
    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text("농장 코드 입력") },
            text = {
                OutlinedTextField(
                    value = joinCodeInput,
                    onValueChange = { v -> joinCodeInput = v.filter { it.isDigit() }.take(6) },
                    placeholder = { Text("6자리 숫자") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (joinCodeInput.length == 6) {
                            farmCodeManager?.setCode(joinCodeInput)
                            com.example.farmmachinemanager.AppContainer.refreshSyncMode()
                            currentCode = joinCodeInput
                            showJoinDialog = false
                            showRestartDialog = true
                        }
                    }
                ) { Text("참여") }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    // 재시작 안내 다이얼로그 - 코드 적용을 위해 앱 재시작 필수
    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { /* 닫지 못하게 */ },
            title = { Text("앱 재시작 필요") },
            text = {
                Text(
                    "농장 코드가 적용되려면 앱을 완전히 종료한 후 다시 실행해야 합니다.\n\n" +
                            "지금 자동으로 앱을 종료할까요? (앱 아이콘을 다시 탭해서 실행해 주세요)"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 현재 Activity finish + process kill → 사용자가 아이콘 탭하면 새 process 시작
                        (context as? android.app.Activity)?.finishAndRemoveTask()
                        android.os.Process.killProcess(android.os.Process.myPid())
                    }
                ) { Text("앱 종료") }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) {
                    Text("나중에")
                }
            }
        )
    }
}

@Composable
private fun ActionButton(
    label: String,
    hint: String,
    danger: Boolean = false,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    // 더 이상 사용 안 함 — FarmActionCard + PrimaryButton/DangerButton 으로 대체.
    // 호출처 없어 deprecated 상태이지만 호환을 위해 잠시 유지.
    val labelColor = if (danger) StatusRepairText else TextPrimary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = labelColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(10.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = labelColor
            )
            Text(text = hint, fontSize = 11.sp, color = TextSecondary)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * 농작이의 농장 코드 카드 패턴 (line 156-218 등).
 * 카드 = padding 16dp + 제목 14sp SemiBold + 부제 12sp + Spacer + 버튼.
 * trailingInfo 가 있으면 버튼 아래에 "현재 코드: 836433" 같은 작은 라벨.
 */
@Composable
private fun FarmActionCard(
    title: String,
    subtitle: String,
    button: @Composable () -> Unit,
    trailingInfo: String? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = TextSecondary,
            lineHeight = 18.sp,
        )
        Spacer(modifier = Modifier.height(12.dp))
        button()
        if (trailingInfo != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = trailingInfo,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
            )
        }
    }
}

/**
 * 농작이 PrimaryButton (line 2632-2646) 그대로 매칭.
 * RoundedCornerShape(10dp) + ActionPrimary 배경 + ActionPrimaryText 색.
 */
@Composable
private fun PrimaryButton(
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val bg = if (enabled) ActionPrimary else TextTertiary
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = ActionPrimaryText,
        )
    }
}

/**
 * 농작이 DangerButton (line 2648-2661) 그대로 매칭.
 * RoundedCornerShape(10dp) + ActionDanger 보더 + ActionDanger 텍스트.
 */
@Composable
private fun DangerButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(0.5.dp, ActionDanger, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = ActionDanger,
        )
    }
}

// ============ 알림 섹션 ============

@Composable
private fun NotificationSection() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember {
        com.example.farmmachinemanager.notifications.NotificationPreferences(context)
    }
    var enabled by remember { mutableStateOf(prefs.enabled) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
    ) {
        CardHeader(title = "알림")
        Divider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    enabled = !enabled
                    prefs.enabled = enabled
                }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (enabled) Icons.Outlined.Notifications else Icons.Outlined.NotificationsOff,
                contentDescription = null,
                tint = if (enabled) com.example.farmmachinemanager.ui.theme.StatusNormalText else TextTertiary,
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "소모품 교체 알림",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = if (enabled)
                        "교체 시기가 다가오거나 지난 항목을 매일 확인하여 알림"
                    else
                        "현재 알림이 꺼져 있습니다",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            androidx.compose.material3.Switch(
                checked = enabled,
                onCheckedChange = { v ->
                    enabled = v
                    prefs.enabled = v
                }
            )
        }
    }
}

// 쿠보타 공식 셀프 점검 가이드 (자가정비) — 외부 브라우저로 연결.
private const val KUBOTA_TRACTOR_GUIDE_URL =
    "https://agriculture.kubota.co.jp/after-support/self-maintenance/tractor/"
private const val KUBOTA_TAUEKI_GUIDE_URL =
    "https://agriculture.kubota.co.jp/after-support/self-maintenance/taueki/"

private fun openUrl(context: Context, url: String) {
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}

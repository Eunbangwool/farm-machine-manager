package com.example.farmmachinemanager.ui.screens

import android.content.Context
import android.content.pm.PackageInfo
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
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val info = remember { collectAppInfo(context) }

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
            // 앱 정보 섹션
            SectionHeader(title = "앱 정보")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfacePrimary)
                    .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            ) {
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
            // IS_DEBUG_APP 만 체크 (BuildConfig.DEBUG 는 일반 debug 빌드도 true).
            if (BuildConfig.IS_DEBUG_APP) {
                SectionHeader(title = "디버그")
                DebugDiagnosticsCard()
            }

            // 기기 정보 섹션
            SectionHeader(title = "기기 정보")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfacePrimary)
                    .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            ) {
                InfoRow(
                    icon = Icons.Outlined.PhoneAndroid,
                    label = "모델",
                    value = "${Build.MANUFACTURER} ${Build.MODEL}"
                )
                Divider()
                InfoRow(
                    icon = Icons.Outlined.PhoneAndroid,
                    label = "Android 버전",
                    value = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
                )
            }

            // 동기화 섹션 (Firebase + 농장 코드)
            SectionHeader(title = "동기화")
            FirebaseSyncSection()

            // 알림 섹션
            SectionHeader(title = "알림")
            NotificationSection()

            // 매뉴얼 섹션
            SectionHeader(title = "매뉴얼")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfacePrimary)
                    .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            ) {
                NavRow(
                    label = "쿠보타 이앙기 트러블슈팅",
                    enabled = true,
                    onClick = onTroubleshootingClick
                )
                Divider()
                NavRow(
                    label = "쿠보타 이앙기 정기점검 일람",
                    enabled = true,
                    onClick = onInspectionChecklistClick
                )
                Divider()
                NavRow(
                    label = "쿠보타 이앙기 소모품 부품",
                    enabled = true,
                    onClick = onPartsListClick
                )
                Divider()
                NavRow(
                    label = "쿠보타 이앙기 퓨즈 가이드",
                    enabled = true,
                    onClick = onFuseGuideClick
                )
                Divider()
                NavRow(
                    label = "쿠보타 이앙기 급유·주유 일람",
                    enabled = true,
                    onClick = onLubricationClick
                )
                Divider()
                NavRow(
                    label = "쿠보타 이앙기 주요 제원",
                    enabled = true,
                    onClick = onSpecificationsClick
                )
            }

            // 향후 옵션 자리 (현재는 비활성)
            SectionHeader(title = "준비 중")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfacePrimary)
                    .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            ) {
                NavRow(label = "회사 이름 변경", enabled = false)
                Divider()
                NavRow(label = "데이터 백업·복원", enabled = false)
                Divider()
                NavRow(label = "다크 모드", enabled = false)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
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
        modifier = Modifier.padding(start = 4.dp)
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
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            fontSize = 13.sp,
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

@Composable
private fun NavRow(label: String, enabled: Boolean, onClick: (() -> Unit)? = null) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .let { if (enabled && onClick != null) it.clickable(onClick = onClick) else it }
        .padding(horizontal = 14.dp, vertical = 14.dp)
    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
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
            imageVector = Icons.Default.ChevronRight,
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
private fun SettingsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfacePrimary)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "뒤로",
                tint = TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = "설정",
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
    ) {
        // 1) 상태 표시
        val (statusLabel, statusColor, statusDesc) = when (mode) {
            com.example.farmmachinemanager.AppContainer.SyncMode.FIRESTORE_SYNCED ->
                Triple(
                    "연결됨",
                    com.example.farmmachinemanager.ui.theme.StatusNormalText,
                    "농장 코드: ${currentCode ?: "(없음)"}"
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
                    "google-services.json을 app/ 폴더에 추가 후 빌드 필요"
                )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Sync,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "상태: $statusLabel",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = statusDesc,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        // 최근 Firestore 에러를 사용자에게 노출. PERMISSION_DENIED 같은 콘솔 규칙 문제를 침묵 처리하지 않는다.
        lastError?.let { err ->
            Divider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StatusInspectionBg)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.BugReport,
                    contentDescription = null,
                    tint = StatusRepairText,
                    modifier = Modifier.size(18.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "최근 동기화 오류",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StatusRepairText
                    )
                    Text(
                        text = err,
                        fontSize = 11.sp,
                        color = TextPrimary
                    )
                }
            }
        }
        Divider()

        // 2) Firebase 사용 가능 + 농장 코드 관리 가능 시에만 버튼 표시
        if (mode != com.example.farmmachinemanager.AppContainer.SyncMode.FIREBASE_NOT_CONFIGURED &&
            farmCodeManager != null
        ) {
            // 새 농장 시작
            ActionButton(
                label = if (currentCode == null) "새 농장 코드 생성" else "새 코드로 재시작",
                hint = if (currentCode == null) "이 폰을 새 농장의 첫 폰으로" else "기존 코드를 버리고 새로 시작"
            ) {
                val code = farmCodeManager.generateNewCode()
                com.example.farmmachinemanager.AppContainer.refreshSyncMode()
                currentCode = code
                showRestartDialog = true
            }
            Divider()
            // 기존 농장 참여
            ActionButton(
                label = "다른 폰 코드로 참여",
                hint = "이미 사용 중인 농장 코드 입력"
            ) {
                joinCodeInput = ""
                showJoinDialog = true
            }
            // 농장 떠나기 (현재 코드 있을 때만)
            if (currentCode != null) {
                Divider()
                ActionButton(
                    label = "농장 떠나기",
                    hint = "이 폰을 로컬 전용으로 전환 (데이터는 Firestore에 남음)",
                    danger = true,
                    icon = Icons.Outlined.ExitToApp,
                ) {
                    showLeaveDialog = true
                }
                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "현재 코드: $currentCode",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }
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

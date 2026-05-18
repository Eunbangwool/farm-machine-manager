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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmmachinemanager.ui.theme.BorderColor
import com.example.farmmachinemanager.ui.theme.SurfacePrimary
import com.example.farmmachinemanager.ui.theme.SurfaceSecondary
import com.example.farmmachinemanager.ui.theme.TextPrimary
import com.example.farmmachinemanager.ui.theme.TextSecondary
import com.example.farmmachinemanager.ui.theme.TextTertiary

/**
 * 설정 화면.
 *
 * 현재는 앱 정보(버전, 기기 정보)만 표시.
 * 향후 추가할 항목: 다크모드 토글, 회사 이름 변경, 데이터 백업/복원, Firebase 연동 등.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit
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
                    value = "농기계 관리"
                )
                Divider()
                InfoRow(
                    icon = Icons.Outlined.Code,
                    label = "버전",
                    value = "${info.versionName} (build ${info.versionCode})"
                )
                Divider()
                InfoRow(
                    icon = Icons.Outlined.Update,
                    label = "패키지명",
                    value = info.packageName
                )
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
                Divider()
                NavRow(label = "Firebase 동기화", enabled = false)
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
private fun NavRow(label: String, enabled: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 14.dp),
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

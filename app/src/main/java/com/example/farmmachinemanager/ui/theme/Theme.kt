package com.example.farmmachinemanager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = ActionPrimary,
    onPrimary = ActionPrimaryText,
    background = SurfaceSecondary,
    onBackground = TextPrimary,
    surface = SurfacePrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = ActionDanger,
)

/**
 * 농돌이 테마 — 농작이와 같은 sage 농업 톤.
 * dynamic color 는 사용 안 함 (기기마다 색이 달라지면 기계 상태 색 의미가 흔들림).
 */
@Composable
fun FarmMachineTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content
    )
}

package com.example.farmmachinemanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.DonutLarge
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmmachinemanager.data.Consumable
import com.example.farmmachinemanager.data.ConsumableCategory
import com.example.farmmachinemanager.data.ConsumableStatus
import com.example.farmmachinemanager.ui.theme.StatusInspectionBg
import com.example.farmmachinemanager.ui.theme.StatusInspectionText
import com.example.farmmachinemanager.ui.theme.StatusNormalBg
import com.example.farmmachinemanager.ui.theme.StatusNormalText
import com.example.farmmachinemanager.ui.theme.StatusRepairBg
import com.example.farmmachinemanager.ui.theme.StatusRepairText
import com.example.farmmachinemanager.ui.theme.SurfacePrimary
import com.example.farmmachinemanager.ui.theme.TextPrimary
import com.example.farmmachinemanager.ui.theme.TextSecondary
import com.example.farmmachinemanager.ui.theme.TextTertiary
import java.time.LocalDate

@Composable
fun ConsumableRow(
    consumable: Consumable,
    currentHours: Double,
    today: LocalDate = LocalDate.now(),
    onQuickReplaceClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val status = consumable.status(currentHours, today)
    val (iconBg, iconTint) = statusColor(status)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = categoryIcon(consumable.category),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = consumable.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = subtitle(consumable),
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            consumable.remainingText(currentHours, today)?.let { text ->
                Text(
                    text = text,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = statusTextColor(status)
                )
            }
            Text(
                text = status.displayName,
                fontSize = 10.sp,
                color = TextTertiary
            )
        }

        // 빠른 교체 버튼 (콜백이 제공된 경우만 표시)
        if (onQuickReplaceClick != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfacePrimary)
                    .clickable(onClick = onQuickReplaceClick)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "교체",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
    }
}

private fun categoryIcon(category: ConsumableCategory): ImageVector = when (category) {
    ConsumableCategory.ENGINE_OIL,
    ConsumableCategory.TRANSMISSION_OIL,
    ConsumableCategory.AXLE_OIL,
    ConsumableCategory.HYDRAULIC_OIL,
    ConsumableCategory.COOLANT -> Icons.Outlined.WaterDrop
    ConsumableCategory.ENGINE_OIL_FILTER,
    ConsumableCategory.TRANSMISSION_OIL_FILTER,
    ConsumableCategory.FUEL_FILTER,
    ConsumableCategory.DEF_FILTER -> Icons.Outlined.FilterAlt
    ConsumableCategory.AIR_FILTER -> Icons.Outlined.Air
    ConsumableCategory.HOSE -> Icons.Default.SettingsInputComponent
    ConsumableCategory.BELT -> Icons.Outlined.Sync
    ConsumableCategory.TIRE -> Icons.Outlined.DonutLarge
    ConsumableCategory.BATTERY -> Icons.Outlined.BatteryFull
    ConsumableCategory.BLADE -> Icons.Outlined.ContentCut
    ConsumableCategory.OTHER -> Icons.Outlined.Sync
}

private fun subtitle(c: Consumable): String = buildString {
    c.replacementIntervalHours?.let { append("${it.toInt()}시간 주기") }
    c.replacementIntervalMonths?.let {
        if (isNotEmpty()) append(" · ")
        append("${it}개월 주기")
    }
    c.lastReplacedHours?.let {
        if (isNotEmpty()) append(" · ")
        append("마지막 ${it.toInt()}h")
    }
}

private fun statusColor(status: ConsumableStatus): Pair<Color, Color> = when (status) {
    ConsumableStatus.OVERDUE -> StatusRepairBg to StatusRepairText
    ConsumableStatus.DUE_SOON -> StatusInspectionBg to StatusInspectionText
    ConsumableStatus.NORMAL -> StatusNormalBg to StatusNormalText
    ConsumableStatus.UNKNOWN -> StatusNormalBg to TextTertiary
}

private fun statusTextColor(status: ConsumableStatus): Color = when (status) {
    ConsumableStatus.OVERDUE -> StatusRepairText
    ConsumableStatus.DUE_SOON -> StatusInspectionText
    ConsumableStatus.NORMAL -> TextPrimary
    ConsumableStatus.UNKNOWN -> TextTertiary
}

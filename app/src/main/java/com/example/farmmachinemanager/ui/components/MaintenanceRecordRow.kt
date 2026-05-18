package com.example.farmmachinemanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmmachinemanager.data.MaintenanceRecord
import com.example.farmmachinemanager.data.MaintenanceType
import com.example.farmmachinemanager.ui.theme.MaintenanceCheckTint
import com.example.farmmachinemanager.ui.theme.MaintenanceInspectionTint
import com.example.farmmachinemanager.ui.theme.MaintenanceRepairTint
import com.example.farmmachinemanager.ui.theme.MaintenanceReplaceTint
import com.example.farmmachinemanager.ui.theme.OtherIconTint
import com.example.farmmachinemanager.ui.theme.TextPrimary
import com.example.farmmachinemanager.ui.theme.TextSecondary
import com.example.farmmachinemanager.ui.theme.TextTertiary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MaintenanceRecordRow(
    record: MaintenanceRecord,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 날짜 (일/월)
        Column(
            modifier = Modifier.width(38.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "%02d".format(record.date.dayOfMonth),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = "${record.date.monthValue}월",
                fontSize = 10.sp,
                color = TextSecondary
            )
        }

        // 종류 아이콘 + 제목
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = typeIcon(record.type),
                    contentDescription = null,
                    tint = typeColor(record.type),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = record.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
            Text(
                text = subtitle(record),
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        // 비용
        Text(
            text = formatCost(record),
            fontSize = 12.sp,
            color = if (record.cost != null) TextPrimary else TextTertiary
        )
    }
}

private fun typeIcon(type: MaintenanceType): ImageVector = when (type) {
    MaintenanceType.REGULAR_CHECK -> Icons.Outlined.CheckCircle
    MaintenanceType.REPAIR -> Icons.Default.Build
    MaintenanceType.CONSUMABLE_REPLACE -> Icons.Default.Refresh
    MaintenanceType.INSPECTION -> Icons.Outlined.Search
    MaintenanceType.OTHER -> Icons.Outlined.Description
}

private fun typeColor(type: MaintenanceType): Color = when (type) {
    MaintenanceType.REGULAR_CHECK -> MaintenanceCheckTint
    MaintenanceType.REPAIR -> MaintenanceRepairTint
    MaintenanceType.CONSUMABLE_REPLACE -> MaintenanceReplaceTint
    MaintenanceType.INSPECTION -> MaintenanceInspectionTint
    MaintenanceType.OTHER -> OtherIconTint
}

private fun subtitle(record: MaintenanceRecord): String {
    if (record.isInProgress) {
        return buildString {
            append("진행 중")
            record.shopName?.let { append(" · $it") }
        }
    }
    return record.shopName ?: record.performedBy ?: ""
}

private fun formatCost(record: MaintenanceRecord): String {
    if (record.isInProgress) return "-"
    val cost = record.cost ?: return "-"
    val formatted = NumberFormat.getNumberInstance(Locale.KOREA).format(cost)
    return "${formatted}원"
}

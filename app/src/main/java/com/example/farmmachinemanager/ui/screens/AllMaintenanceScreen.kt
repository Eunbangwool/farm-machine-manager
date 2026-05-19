package com.example.farmmachinemanager.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmmachinemanager.AppContainer
import com.example.farmmachinemanager.data.Machine
import com.example.farmmachinemanager.data.MaintenanceRecord
import com.example.farmmachinemanager.ui.theme.BorderColor
import com.example.farmmachinemanager.ui.theme.SurfacePrimary
import com.example.farmmachinemanager.ui.theme.SurfaceSecondary
import com.example.farmmachinemanager.ui.theme.TextPrimary
import com.example.farmmachinemanager.ui.theme.TextSecondary
import com.example.farmmachinemanager.ui.theme.TextTertiary
import java.time.format.DateTimeFormatter

/**
 * 한 기계의 모든 정비 기록 목록.
 * 각 카드 탭 → AddMaintenanceRecordScreen에 existingRecord 전달하여 수정 모드 진입.
 */
@Composable
fun AllMaintenanceScreen(
    machine: Machine,
    onBack: () -> Unit,
    onRecordClick: (MaintenanceRecord) -> Unit
) {
    BackHandler { onBack() }

    val records by AppContainer.maintenanceRepository
        .observeMaintenanceFor(machine.id)
        .collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        AllRecordsTopBar(
            title = "정비 이력 (${records.size}건)",
            subtitle = machine.name,
            onBack = onBack
        )

        if (records.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "정비 기록이 없습니다",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(records, key = { it.id }) { record ->
                    RecordRow(record = record, onClick = { onRecordClick(record) })
                }
            }
        }
    }
}

@Composable
private fun RecordRow(record: MaintenanceRecord, onClick: () -> Unit) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = record.type.displayName,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextTertiary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(SurfaceSecondary)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                if (record.isInProgress) {
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "진행 중",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = com.example.farmmachinemanager.ui.theme.StatusInspectionText,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(com.example.farmmachinemanager.ui.theme.StatusInspectionBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = record.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row {
                Text(
                    text = record.date.format(dateFormatter),
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                record.cost?.let {
                    Text(
                        text = "  ·  ₩${"%,d".format(it)}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                record.operatingHoursAtMaintenance?.let {
                    Text(
                        text = "  ·  ${it.toInt()}h",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun AllRecordsTopBar(title: String, subtitle: String, onBack: () -> Unit) {
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
        Column {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(text = subtitle, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

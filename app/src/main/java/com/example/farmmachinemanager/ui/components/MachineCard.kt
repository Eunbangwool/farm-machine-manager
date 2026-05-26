package com.example.farmmachinemanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PrecisionManufacturing
import androidx.compose.material.icons.outlined.Shower
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.example.farmmachinemanager.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmmachinemanager.data.Machine
import com.example.farmmachinemanager.data.MachineStatus
import com.example.farmmachinemanager.data.MachineType
import com.example.farmmachinemanager.ui.theme.BorderColor
import com.example.farmmachinemanager.ui.theme.CombineIconBg
import com.example.farmmachinemanager.ui.theme.CombineIconTint
import com.example.farmmachinemanager.ui.theme.ForkliftIconBg
import com.example.farmmachinemanager.ui.theme.ForkliftIconTint
import com.example.farmmachinemanager.ui.theme.OtherIconBg
import com.example.farmmachinemanager.ui.theme.OtherIconTint
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
import com.example.farmmachinemanager.ui.theme.TractorIconBg
import com.example.farmmachinemanager.ui.theme.TractorIconTint
import com.example.farmmachinemanager.ui.theme.TransplanterIconBg
import com.example.farmmachinemanager.ui.theme.TransplanterIconTint
import com.example.farmmachinemanager.ui.theme.VehicleIconBg
import com.example.farmmachinemanager.ui.theme.VehicleIconTint
import com.example.farmmachinemanager.ui.theme.RotavatorIconBg
import com.example.farmmachinemanager.ui.theme.RotavatorIconTint
import com.example.farmmachinemanager.ui.theme.PlowIconBg
import com.example.farmmachinemanager.ui.theme.PlowIconTint
import com.example.farmmachinemanager.ui.theme.SeederIconBg
import com.example.farmmachinemanager.ui.theme.SeederIconTint
import com.example.farmmachinemanager.ui.theme.HarvesterIconBg
import com.example.farmmachinemanager.ui.theme.HarvesterIconTint
import com.example.farmmachinemanager.ui.theme.SprayerIconBg
import com.example.farmmachinemanager.ui.theme.SprayerIconTint
import com.example.farmmachinemanager.ui.theme.DroneIconBg
import com.example.farmmachinemanager.ui.theme.DroneIconTint
import com.example.farmmachinemanager.ui.theme.BalerIconBg
import com.example.farmmachinemanager.ui.theme.BalerIconTint
import com.example.farmmachinemanager.ui.theme.LawnMowerIconBg
import com.example.farmmachinemanager.ui.theme.LawnMowerIconTint
import com.example.farmmachinemanager.ui.theme.LoaderIconBg
import com.example.farmmachinemanager.ui.theme.LoaderIconTint

/**
 * 기계 한 대를 표현하는 카드.
 * 현장에서 장갑 끼고도 누를 수 있도록 충분한 터치 영역(높이 ~84dp)을 확보.
 */
@Composable
fun MachineCard(
    machine: Machine,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (iconBg, iconTint, iconVector) = iconConfig(machine.type)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfacePrimary)
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 기계 종류 아이콘
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = machine.type.displayName,
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )
        }

        // 정보 영역
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = machine.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                StatusBadge(status = machine.status)
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = buildSubtitle(machine),
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoChip(
                    icon = Icons.Outlined.Schedule,
                    text = "${machine.operatingHours.toInt()}시간"
                )
                machine.statusNote?.let { note ->
                    InfoChip(
                        icon = noteIcon(machine.status),
                        text = note
                    )
                }
            }
        }

        // 우측 화살표
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun StatusBadge(status: MachineStatus) {
    val (bg, fg) = when (status) {
        MachineStatus.NORMAL -> StatusNormalBg to StatusNormalText
        MachineStatus.INSPECTION_NEEDED -> StatusInspectionBg to StatusInspectionText
        MachineStatus.UNDER_REPAIR -> StatusRepairBg to StatusRepairText
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = status.displayName,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = fg
        )
    }
}

@Composable
private fun InfoChip(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = text,
            fontSize = 11.sp,
            color = TextTertiary
        )
    }
}

// 기계 종류별 아이콘과 색상
@Composable
private fun iconConfig(type: MachineType): Triple<Color, Color, ImageVector> = when (type) {
    MachineType.TRACTOR -> Triple(TractorIconBg, TractorIconTint, Icons.Default.Agriculture)
    MachineType.COMBINE -> Triple(
        CombineIconBg, CombineIconTint,
        ImageVector.vectorResource(R.drawable.ic_combine)
    )
    MachineType.RICE_TRANSPLANTER -> Triple(
        TransplanterIconBg, TransplanterIconTint,
        ImageVector.vectorResource(R.drawable.ic_transplanter)
    )
    MachineType.CULTIVATOR -> Triple(OtherIconBg, OtherIconTint, Icons.Default.Agriculture)
    MachineType.ROTAVATOR -> Triple(RotavatorIconBg, RotavatorIconTint, Icons.Outlined.PrecisionManufacturing)
    MachineType.PLOW -> Triple(PlowIconBg, PlowIconTint, Icons.Outlined.Construction)
    MachineType.SEEDER -> Triple(SeederIconBg, SeederIconTint, Icons.Outlined.Grain)
    MachineType.HARVESTER -> Triple(HarvesterIconBg, HarvesterIconTint, Icons.Outlined.Grass)
    MachineType.SPRAYER -> Triple(SprayerIconBg, SprayerIconTint, Icons.Outlined.Shower)
    MachineType.DRONE -> Triple(DroneIconBg, DroneIconTint, Icons.Outlined.FlightTakeoff)
    MachineType.BALER -> Triple(BalerIconBg, BalerIconTint, Icons.Outlined.Inventory2)
    MachineType.LAWN_MOWER -> Triple(LawnMowerIconBg, LawnMowerIconTint, Icons.Outlined.Grass)
    MachineType.LOADER -> Triple(LoaderIconBg, LoaderIconTint, Icons.Outlined.Construction)
    MachineType.FORKLIFT -> Triple(
        ForkliftIconBg, ForkliftIconTint,
        ImageVector.vectorResource(R.drawable.ic_forklift)
    )
    MachineType.VEHICLE -> Triple(VehicleIconBg, VehicleIconTint, Icons.Default.DirectionsCar)
    MachineType.OTHER -> Triple(OtherIconBg, OtherIconTint, Icons.Default.Build)
}

private fun buildSubtitle(machine: Machine): String = buildString {
    append(machine.manufacturer)
    append(" · ")
    append(machine.typeDisplay)
    machine.horsepower?.let { append(" · ${it}마력") }
}

private fun noteIcon(status: MachineStatus): ImageVector = when (status) {
    MachineStatus.UNDER_REPAIR -> Icons.Default.Build
    MachineStatus.INSPECTION_NEEDED -> Icons.Outlined.WaterDrop
    MachineStatus.NORMAL -> Icons.Default.CheckCircle
}

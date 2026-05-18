package com.example.farmmachinemanager.ui.screens

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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmmachinemanager.AppContainer
import com.example.farmmachinemanager.data.Machine
import com.example.farmmachinemanager.data.MachineType
import com.example.farmmachinemanager.ui.components.FilterChipGroup
import com.example.farmmachinemanager.ui.components.FilterOption
import com.example.farmmachinemanager.ui.components.MachineCard
import com.example.farmmachinemanager.ui.theme.BorderColor
import com.example.farmmachinemanager.ui.theme.FarmMachineTheme
import com.example.farmmachinemanager.ui.theme.StatusInspectionBg
import com.example.farmmachinemanager.ui.theme.StatusInspectionText
import com.example.farmmachinemanager.ui.theme.SurfacePrimary
import com.example.farmmachinemanager.ui.theme.SurfaceSecondary
import com.example.farmmachinemanager.ui.theme.TextPrimary
import com.example.farmmachinemanager.ui.theme.TextSecondary

/**
 * 기계 목록 화면.
 *
 * 향후 변경 사항:
 * - allMachines를 ViewModel에서 StateFlow로 주입받도록 변경
 * - 필터/검색 로직도 ViewModel로 이동
 * - 회사 이름은 인증된 사용자 정보에서 가져오기
 */
@Composable
fun MachineListScreen(
    companyName: String = "한농산업",
    onMachineClick: (Machine) -> Unit = {},
    onUpdateHoursClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onFilterClick: () -> Unit = {}
) {
    // Repository에서 실시간으로 기계 목록 읽기.
    // 가동시간 업데이트 화면에서 저장한 새 값이 자동 반영됨.
    val machines by AppContainer.machineRepository
        .observeMachines()
        .collectAsState(initial = emptyList())

    var selectedFilter by remember { mutableStateOf<MachineType?>(null) }

    val filterOptions = remember(machines) {
        listOf(
            null to "전체",
            MachineType.TRACTOR to "트랙터",
            MachineType.COMBINE to "콤바인",
            MachineType.RICE_TRANSPLANTER to "이앙기"
        ).map { (type, label) ->
            type to FilterOption(
                label = label,
                count = if (type == null) machines.size else machines.count { it.type == type }
            )
        }
    }

    val selectedIndex = filterOptions.indexOfFirst { it.first == selectedFilter }
        .coerceAtLeast(0)

    val visibleMachines = remember(selectedFilter, machines) {
        if (selectedFilter == null) machines
        else machines.filter { it.type == selectedFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceSecondary)
    ) {
        TopBar(
            companyName = companyName,
            filterOptions = filterOptions.map { it.second },
            selectedIndex = selectedIndex,
            onFilterSelect = { index -> selectedFilter = filterOptions[index].first },
            onSearchClick = onSearchClick,
            onFilterClick = onFilterClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                UpdateHoursReminderCard(onClick = onUpdateHoursClick)
            }
            items(visibleMachines, key = { it.id }) { machine ->
                MachineCard(
                    machine = machine,
                    onClick = { onMachineClick(machine) }
                )
            }
        }
    }
}

/**
 * "가동시간을 업데이트 해주세요" 알림 카드.
 * 메인 화면 최상단에 항상 표시되어 사용자에게 일일 입력을 유도.
 */
@Composable
private fun UpdateHoursReminderCard(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StatusInspectionBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfacePrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = StatusInspectionText,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "운행한 기계의 가동시간을 업데이트 해주세요",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = StatusInspectionText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "정확한 정비 일정을 위해 작업 후 기록해주세요",
                fontSize = 11.sp,
                color = StatusInspectionText
            )
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(SurfacePrimary)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "기계선택",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = StatusInspectionText
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = StatusInspectionText,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun TopBar(
    companyName: String,
    filterOptions: List<FilterOption>,
    selectedIndex: Int,
    onFilterSelect: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfacePrimary)
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = companyName,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "기계 관리",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconCircleButton(
                    icon = Icons.Default.Search,
                    contentDescription = "검색",
                    onClick = onSearchClick
                )
                IconCircleButton(
                    icon = Icons.Default.Tune,
                    contentDescription = "필터",
                    onClick = onFilterClick
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        FilterChipGroup(
            options = filterOptions,
            selectedIndex = selectedIndex,
            onSelect = onFilterSelect
        )
    }
}

@Composable
private fun IconCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(0.5.dp, BorderColor, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = TextPrimary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun MachineListScreenPreview() {
    FarmMachineTheme {
        MachineListScreen()
    }
}

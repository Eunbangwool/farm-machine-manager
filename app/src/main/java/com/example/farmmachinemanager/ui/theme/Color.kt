package com.example.farmmachinemanager.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// 농돌이 컬러 팔레트 — 농작이와 같은 sage 농업 톤.
// (농기계 도메인 전용 색상은 아래 별도 섹션에서 유지)
// ============================================================

// 농작이 공유 톤 -----------------------------------------------
val Forest = Color(0xFF14301E)
val Primary = Color(0xFF2F5D3D)
val Fresh = Color(0xFF5E8F66)
val Tint = Color(0xFFEFEEE6)

val ActionPrimary = Color(0xFF2F5D3D)
val ActionPrimaryText = Color(0xFFFAF9F4)
val ActionDanger = Color(0xFF8C3A33)

// 표면 / 텍스트 / 보더 -----------------------------------------
val SurfacePrimary = Color(0xFFFAF9F4)
val SurfaceSecondary = Color(0xFFEFEEE6)
val TextPrimary = Color(0xFF1F2421)
val TextSecondary = Color(0xFF6B7068)
val TextTertiary = Color(0xFFA8AAA5)
val BorderColor = Color(0xFFDDD9CC)

// ============================================================
// 농기계 도메인 전용 — 기계 상태 / 종류 아이콘 / 정비 종류
// (농작이에는 없는 농돌이 고유 색상. sage 톤과 조화롭게 그대로 유지)
// ============================================================

// 상태
val StatusNormalBg = Color(0xFFEAF3DE)
val StatusNormalText = Color(0xFF27500A)

val StatusInspectionBg = Color(0xFFFAEEDA)
val StatusInspectionText = Color(0xFF633806)

val StatusRepairBg = Color(0xFFFCEBEB)
val StatusRepairText = Color(0xFF791F1F)

// 수리 중 경고 배너
val RepairAlertBg = Color(0xFFFCEBEB)
val RepairAlertIconBg = Color(0xFFF09595)
val RepairAlertIconTint = Color(0xFF4B1313)
val RepairAlertTitle = Color(0xFF791F1F)
val RepairAlertSubtitle = Color(0xFFA32D2D)
val RepairAlertBorder = Color(0xFFA32D2D)

// 기계 종류 아이콘
val TractorIconBg = Color(0xFFEEEDFE)
val TractorIconTint = Color(0xFF3C3489)

val CombineIconBg = Color(0xFFE1F5EE)
val CombineIconTint = Color(0xFF085041)

val TransplanterIconBg = Color(0xFFE8F3D8)
val TransplanterIconTint = Color(0xFF3B6D11)

val VehicleIconBg = Color(0xFFE6F1FB)
val VehicleIconTint = Color(0xFF0C447C)

val ForkliftIconBg = Color(0xFFFFF4E0)
val ForkliftIconTint = Color(0xFFB45A00)

val OtherIconBg = Color(0xFFF1EFE8)
val OtherIconTint = Color(0xFF444441)

// 정비 종류 아이콘
val MaintenanceRepairTint = Color(0xFF791F1F)
val MaintenanceCheckTint = Color(0xFF27500A)
val MaintenanceReplaceTint = Color(0xFF185FA5)
val MaintenanceInspectionTint = Color(0xFF633806)

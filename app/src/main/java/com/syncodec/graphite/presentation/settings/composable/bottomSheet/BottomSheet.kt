package com.syncodec.graphite.presentation.settings.composable.bottomSheet

import androidx.compose.runtime.Composable


enum class SettingsBottomSheetType {
	PROFILE
}

@Composable
fun SheetLayout(
	bottomSheetType: SettingsBottomSheetType,
	closeSheet: () -> Unit
) {
	when (bottomSheetType) {
		SettingsBottomSheetType.PROFILE -> ProfileBottomSheet(closeSheet)
	}
}

package com.syncodec.graphite.presentation.settings.composable.bottomSheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.presentation.main.MainViewModel
import com.syncodec.graphite.presentation.main.composable.bottomSheet.BucketBottomSheet
import com.syncodec.graphite.presentation.main.composable.bottomSheet.FilterBottomSheet
import com.syncodec.graphite.presentation.main.composable.bottomSheet.MainBottomSheetType
import com.syncodec.graphite.presentation.main.composable.bottomSheet.MenuBottomSheet
import com.syncodec.graphite.presentation.main.composable.bottomSheet.NotebookBottomSheet


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

package com.syncodec.graphite.presentation.main.composable.bottomSheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.presentation.main.MainViewModel


enum class MainBottomSheetType {
	MENU,
	FILTER,
	NOTEBOOK,
	BUCKET
}

@Composable
fun SheetLayout(
	bottomSheetType: MainBottomSheetType,
	closeSheet: () -> Unit
) {
	val viewModel: MainViewModel = viewModel()

	var filterInclusivityState by viewModel.filterInclusivityState
	var sortOn by viewModel.sortOn
	var sortBy by viewModel.sortBy
	var viewType by viewModel.viewType

	when (bottomSheetType) {
		MainBottomSheetType.MENU -> MenuBottomSheet(closeSheet = closeSheet)
		MainBottomSheetType.FILTER -> FilterBottomSheet(
			filterInclusivityState = filterInclusivityState,
			sortOn = sortOn,
			sortBy = sortBy,
			viewType = viewType,
			onTagFilterChange = { filterInclusivityState = it },
			onUpdateSortOn = { sortOn = it },
			onUpdateSortBy = { sortBy = it },
			onUpdateViewType = { viewType = it },
			closeSheet = closeSheet
		)
		MainBottomSheetType.NOTEBOOK -> NotebookBottomSheet(closeSheet = closeSheet)
		MainBottomSheetType.BUCKET -> BucketBottomSheet(closeSheet = closeSheet)
	}
}

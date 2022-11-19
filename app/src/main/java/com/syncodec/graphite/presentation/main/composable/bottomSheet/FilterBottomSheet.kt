package com.syncodec.graphite.presentation.main.composable.bottomSheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetKeyText
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.common.button.LargeButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateData
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.ViewType


@Composable
fun FilterBottomSheet(
	showViewTypeOption: Boolean = true,
) {
	val context = LocalContext.current
	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val sortOn by dataStoreInstance.getSortOn.collectAsState(initial = SortOn.TIMESTAMP)
	val sortBy by dataStoreInstance.getSortBy.collectAsState(initial = SortBy.DESCENDING)
	val viewType by dataStoreInstance.getViewType.collectAsState(initial = ViewType.LIST)

	val sortOnStateList : List<StateData> = listOf(
		StateData(
			title = "Title",
			icon = R.drawable.ic_title,
			stateTint = MaterialTheme.colorScheme.primary
		),
		StateData(
			title = "Timestamp",
			icon = R.drawable.ic_clock,
			stateTint = MaterialTheme.colorScheme.primary
		),
		StateData(
			title = "Modified",
			icon = R.drawable.ic_clock_transparent,
			stateTint = MaterialTheme.colorScheme.primary
		)
	)

	val sortByStateList : List<StateData> = listOf(
		StateData(
			title = "Ascending",
			icon = R.drawable.ic_sort_ascending,
			stateTint = MaterialTheme.colorScheme.primary
		),
		StateData(
			title = "Descending",
			icon = R.drawable.ic_sort_descending,
			stateTint = MaterialTheme.colorScheme.primary
		)
	)

	val viewTypeStateList : List<StateData> = listOf(
		StateData(
			title = "List",
			icon = R.drawable.ic_list_view,
			stateTint = MaterialTheme.colorScheme.primary
		),
		StateData(
			title = "Grid",
			icon = R.drawable.ic_grid_view,
			stateTint = MaterialTheme.colorScheme.primary
		)
	)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface)
	) {

		BottomSheetStrip()

		BottomSheetHeader(
			title = "Filter And View",
			icon = R.drawable.ic_filter
		)

		Spacer(modifier = Modifier.height(8.dp))

		BottomSheetKeyText(text = "Sort On")

		Spacer(modifier = Modifier.height(8.dp))

		StateButton(
			stateList = sortOnStateList,
			currentState = sortOn.ordinal,
			containerColor = MaterialTheme.colorScheme.background,
			modifier = Modifier
				.height(36.dp)
				.padding(24.dp, 0.dp)
		) { dataStoreInstance.putSortOn(SortOn.values().getOrElse(it) { SortOn.TIMESTAMP }) }

		Spacer(modifier = Modifier.height(12.dp))

		BottomSheetKeyText(text = "Sort By")

		Spacer(modifier = Modifier.height(8.dp))

		StateButton(
			stateList = sortByStateList,
			currentState = sortBy.ordinal,
			containerColor = MaterialTheme.colorScheme.background,
			modifier = Modifier
				.height(36.dp)
				.padding(24.dp, 0.dp),
		) { dataStoreInstance.putSortBy(SortBy.values().getOrElse(it) { SortBy.DESCENDING }) }

		Spacer(modifier = Modifier.height(12.dp))

		if (showViewTypeOption) {
			BottomSheetKeyText(text = "View Type")

			Spacer(modifier = Modifier.height(8.dp))

			StateButton(
				stateList = viewTypeStateList,
				currentState = viewType.ordinal,
				containerColor = MaterialTheme.colorScheme.background,
				modifier = Modifier
					.height(36.dp)
					.padding(24.dp, 0.dp),
			) { dataStoreInstance.putViewType(ViewType.values().getOrElse(it) { ViewType.LIST }) }
		}

		LargeButton(
			text = "Default",
			enabled = true,
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp)
		) {
			dataStoreInstance.putSortOn(SortOn.TIMESTAMP)
			dataStoreInstance.putSortBy(SortBy.DESCENDING)
		}
	}
}

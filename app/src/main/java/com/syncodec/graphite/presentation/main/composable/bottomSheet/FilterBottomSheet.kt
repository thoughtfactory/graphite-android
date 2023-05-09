package com.syncodec.graphite.presentation.main.composable.bottomSheet

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetKeyText
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.ViewType


@Preview
@Composable
fun FilterBottomSheet(
	showViewTypeOption : Boolean = true,
) {
	val context = LocalContext.current
	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val sortOn by dataStoreInstance.getSortOn.collectAsState(initial = SortOn.Timestamp)
	val sortBy by dataStoreInstance.getSortBy.collectAsState(initial = SortBy.Descending)
	val viewType by dataStoreInstance.getViewType.collectAsState(initial = ViewType.List)

	GenericBottomSheet(
		title = "Filter and View",
		icon = R.drawable.ic_filter,
	) {

		SortOnView(sortOn = sortOn) { dataStoreInstance.putSortOn(it) }

		Spacer(modifier = Modifier.height(12.dp))

		SortByView(sortBy = sortBy) { dataStoreInstance.putSortBy(it) }

		Spacer(modifier = Modifier.height(8.dp))

		if (showViewTypeOption) {
			Spacer(modifier = Modifier.height(4.dp))
			ViewTypeView(viewType = viewType) { dataStoreInstance.putViewType(it) }
			Spacer(modifier = Modifier.height(8.dp))
		}

		Button(
			shape = MaterialTheme.shapes.medium,
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.primary,
				contentColor = MaterialTheme.colorScheme.onPrimary,
			),
			modifier = Modifier.fillMaxWidth(),
			onClick = {
				dataStoreInstance.putSortOn(SortOn.Timestamp)
				dataStoreInstance.putSortBy(SortBy.Descending)
			}
		) {
			Text(text = "Default")
		}
	}
}

@Preview
@Composable
private fun ColumnScope.SortOnView(
	sortOn : SortOn = SortOn.Timestamp,
	onClick : (SortOn) -> Unit = {},
) {
	BottomSheetKeyText(text = "Sort On")
	Spacer(modifier = Modifier.height(8.dp))
	Row(
		modifier = Modifier.fillMaxWidth()
	) {
		FilterButton(
			text = "Title",
			icon = R.drawable.ic_title,
			highlight = sortOn == SortOn.Title,
			modifier = Modifier.weight(1f),
		) { onClick(SortOn.Title) }
		Spacer(modifier = Modifier.width(8.dp))
		FilterButton(
			text = "Timestamp",
			icon = R.drawable.ic_clock,
			highlight = sortOn == SortOn.Timestamp,
			modifier = Modifier.weight(1f),
		) { onClick(SortOn.Timestamp) }
	}
	Spacer(modifier = Modifier.height(6.dp))
	Row(
		modifier = Modifier.fillMaxWidth()
	) {
		FilterButton(
			text = "Modified",
			icon = R.drawable.ic_clock_transparent,
			highlight = sortOn == SortOn.Modified,
			modifier = Modifier.weight(1f),
		) { onClick(SortOn.Modified) }
		Spacer(modifier = Modifier.width(8.dp))
		FilterButton(
			text = "Custom",
			icon = R.drawable.ic_reorder,
			highlight = sortOn == SortOn.Custom,
			modifier = Modifier.weight(1f),
		) { onClick(SortOn.Custom) }
	}
}

@Preview
@Composable
private fun ColumnScope.SortByView(
	sortBy : SortBy = SortBy.Descending,
	onClick : (SortBy) -> Unit = {},
) {
	BottomSheetKeyText(text = "Sort By")
	Spacer(modifier = Modifier.height(8.dp))
	Row(
		modifier = Modifier.fillMaxWidth()
	) {
		FilterButton(
			text = "Ascending",
			icon = R.drawable.ic_sort_ascending,
			highlight = sortBy == SortBy.Ascending,
			modifier = Modifier.weight(1f),
		) { onClick(SortBy.Ascending) }
		Spacer(modifier = Modifier.width(8.dp))
		FilterButton(
			text = "Descending",
			icon = R.drawable.ic_sort_descending,
			highlight = sortBy == SortBy.Descending,
			modifier = Modifier.weight(1f),
		) { onClick(SortBy.Descending) }
	}
}

@Preview
@Composable
private fun ColumnScope.ViewTypeView(
	viewType : ViewType = ViewType.List,
	onClick : (ViewType) -> Unit = {},
) {
	BottomSheetKeyText(text = "View Type")
	Spacer(modifier = Modifier.height(8.dp))
	Row(
		modifier = Modifier.fillMaxWidth()
	) {
		FilterButton(
			text = "List",
			icon = R.drawable.ic_view_list,
			highlight = viewType == ViewType.List,
			modifier = Modifier.weight(1f),
		) { onClick(ViewType.List) }
		Spacer(modifier = Modifier.width(8.dp))
		FilterButton(
			text = "Grid",
			icon = R.drawable.ic_view_grid,
			highlight = viewType == ViewType.Grid,
			modifier = Modifier.weight(1f),
		) { onClick(ViewType.Grid) }
	}
}

@Composable
private fun FilterButton(
	modifier : Modifier = Modifier,
	text : String = "Filter",
	icon : Int = R.drawable.ic_filter,
	highlight : Boolean = false,
	onClick : () -> Unit = {},
) {
	val containerColor by animateColorAsState(
		targetValue = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceColorAtElevation(
			8.dp
		).copy(alpha = 0.47f)
	)
	val contentColor by animateColorAsState(targetValue = if (highlight) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)

	Box(
		modifier = modifier
			.background(containerColor, MaterialTheme.shapes.medium)
			.clip(MaterialTheme.shapes.medium)
			.clickable { onClick() },
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Center,
			modifier = Modifier
				.fillMaxWidth()
				.height(40.dp)
				.padding(8.dp, 4.dp),
		) {
			Icon(
				painter = painterResource(id = icon),
				contentDescription = text,
				tint = contentColor,
				modifier = Modifier.size(20.dp),
			)
			Spacer(modifier = Modifier.width(8.dp))
			Text(
				text = text,
				color = contentColor,
				style = MaterialTheme.typography.bodyMedium,
				modifier = Modifier,
			)
		}
	}
}

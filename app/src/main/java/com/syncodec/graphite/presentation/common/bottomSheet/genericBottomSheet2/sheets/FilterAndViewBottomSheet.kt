package com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.sheets

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetKeyText
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.button.MinusButton
import com.syncodec.graphite.presentation.common.button.PlusButton
import com.syncodec.graphite.presentation.base.LocalAppDataStore
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.ViewType


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun FilterAndViewBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = true,
	showViewTypeOption: Boolean = true,
	onDismissRequest: () -> Unit = { },
) {
	val appDataStore = LocalAppDataStore.current

	val sortOn by appDataStore.getSortOn.collectAsState(initial = SortOn.Timestamp)
	val sortBy by appDataStore.getSortBy.collectAsState(initial = SortBy.Descending)
	val viewType by appDataStore.getViewType.collectAsState(initial = ViewType.List)
	val componentHeight by appDataStore.componentHeight.collectAsState(initial = 144)
	val componentColumnCount by appDataStore.componentColumnCount.collectAsState(initial = 2)

	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		GenericBottomSheetSkeleton2(
			title = stringResource(id = R.string.filter_and_view),
		) {
			SortOnView(sortOn = sortOn) { appDataStore.putSortOn(it) }

			Spacer(modifier = Modifier.height(12.dp))

			SortByView(sortBy = sortBy) { appDataStore.putSortBy(it) }

			Spacer(modifier = Modifier.height(8.dp))

			if (showViewTypeOption) {
				Spacer(modifier = Modifier.height(4.dp))
				ViewTypeView(viewType = viewType) { appDataStore.putViewType(it) }
				Spacer(modifier = Modifier.height(8.dp))
			}

			Spacer(modifier = Modifier.height(4.dp))

			AnimatedContent(
				targetState = viewType,
				transitionSpec = { fadeIn(tween(470)) togetherWith fadeOut(tween(470)) },
				label = "viewType_animation"
			) {
				when (it) {
					ViewType.List -> ComponentHeight(
						componentHeight = componentHeight,
						onIncreaseSize = { appDataStore.putComponentHeight(componentHeight + 4) },
						onDecreaseSize = { appDataStore.putComponentHeight(componentHeight - 4) },
					)

					ViewType.Grid -> ComponentColumnCount(
						componentColumnCount = componentColumnCount,
						onIncreaseColumnCount = { appDataStore.putComponentColumnCount(componentColumnCount + 1) },
						onDecreaseCount = { appDataStore.putComponentColumnCount(componentColumnCount - 1) },
					)
				}
			}

			Spacer(modifier = Modifier.height(12.dp))

			Button(
				shape = MaterialTheme.shapes.medium,
				colors = ButtonDefaults.buttonColors(
					containerColor = MaterialTheme.colorScheme.primary,
					contentColor = MaterialTheme.colorScheme.onPrimary,
				),
				modifier = Modifier.fillMaxWidth(),
				onClick = {
					appDataStore.putSortOn(SortOn.Timestamp)
					appDataStore.putSortBy(SortBy.Descending)
					appDataStore.putComponentHeight(144)
				}
			) {
				Text(text = stringResource(id = R.string.restore_default))
			}
		}
	}
}

@Preview
@Composable
private fun SortOnView(
	sortOn: SortOn = SortOn.Timestamp,
	onClick: (SortOn) -> Unit = {},
) {
	BottomSheetKeyText(text = stringResource(id = R.string.sort_on))
	Spacer(modifier = Modifier.height(8.dp))
	Row(
		modifier = Modifier.fillMaxWidth()
	) {
		FilterButton(
			text = stringResource(id = R.string.title),
			icon = R.drawable.ic_fa_title,
			highlight = sortOn == SortOn.Title,
			modifier = Modifier.weight(1f),
		) { onClick(SortOn.Title) }
		Spacer(modifier = Modifier.width(8.dp))
		FilterButton(
			text = stringResource(id = R.string.timestamp),
			icon = R.drawable.ic_fa_clock,
			highlight = sortOn == SortOn.Timestamp,
			modifier = Modifier.weight(1f),
		) { onClick(SortOn.Timestamp) }
	}
	Spacer(modifier = Modifier.height(6.dp))
	Row(
		modifier = Modifier.fillMaxWidth()
	) {
		FilterButton(
			text = stringResource(id = R.string.modified),
			icon = R.drawable.ic_fa_clock_duotone,
			highlight = sortOn == SortOn.Modified,
			modifier = Modifier.weight(1f),
		) { onClick(SortOn.Modified) }
		Spacer(modifier = Modifier.width(8.dp))
		FilterButton(
			text = stringResource(id = R.string.custom),
			icon = R.drawable.ic_fa_grip,
			highlight = sortOn == SortOn.Custom,
			modifier = Modifier.weight(1f),
		) { onClick(SortOn.Custom) }
	}
}

@Preview
@Composable
private fun SortByView(
	sortBy: SortBy = SortBy.Descending,
	onClick: (SortBy) -> Unit = {},
) {
	BottomSheetKeyText(text = stringResource(id = R.string.sort_by))
	Spacer(modifier = Modifier.height(8.dp))
	Row(
		modifier = Modifier.fillMaxWidth()
	) {
		val ascendingContainerColor by animateColorAsState(
			targetValue = if (sortBy == SortBy.Ascending) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.47f),
			label = "ascendingContainerColor_animation"
		)
		val ascendingContentColor by animateColorAsState(
			targetValue = if (sortBy == SortBy.Ascending) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
			label = "ascendingContentColor_animation"
		)

		Box(
			modifier = Modifier
				.weight(1f)
				.background(ascendingContainerColor, MaterialTheme.shapes.medium)
				.clip(MaterialTheme.shapes.medium)
				.clickable { onClick(SortBy.Ascending) },
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
					painter = painterResource(id = R.drawable.ic_fa_sort_bars),
					contentDescription = stringResource(id = R.string.ascending),
					tint = ascendingContentColor,
					modifier = Modifier
						.size(16.dp)
						.graphicsLayer { scaleY = -1f }
				)
				Spacer(modifier = Modifier.width(12.dp))
				Text(
					text = stringResource(id = R.string.ascending),
					color = ascendingContentColor,
					style = MaterialTheme.typography.bodyMedium,
					modifier = Modifier,
				)
			}
		}
		Spacer(modifier = Modifier.width(8.dp))
		FilterButton(
			text = stringResource(id = R.string.descending),
			icon = R.drawable.ic_fa_sort_bars,
			highlight = sortBy == SortBy.Descending,
			modifier = Modifier.weight(1f),
		) { onClick(SortBy.Descending) }
	}
}

@Preview
@Composable
private fun ViewTypeView(
	viewType: ViewType = ViewType.List,
	onClick: (ViewType) -> Unit = {},
) {
	BottomSheetKeyText(text = stringResource(id = R.string.view_type))
	Spacer(modifier = Modifier.height(8.dp))
	Row(
		modifier = Modifier.fillMaxWidth()
	) {
		FilterButton(
			text = stringResource(id = R.string.list),
			icon = R.drawable.ic_fa_view_list,
			highlight = viewType == ViewType.List,
			modifier = Modifier.weight(1f),
		) { onClick(ViewType.List) }
		Spacer(modifier = Modifier.width(8.dp))
		FilterButton(
			text = stringResource(id = R.string.grid),
			icon = R.drawable.ic_fa_view_grid,
			highlight = viewType == ViewType.Grid,
			modifier = Modifier.weight(1f),
		) { onClick(ViewType.Grid) }
	}
}

@Composable
private fun FilterButton(
	modifier: Modifier = Modifier,
	text: String = "Filter",
	icon: Int = R.drawable.ic_filter,
	highlight: Boolean = false,
	onClick: () -> Unit = {},
) {
	val containerColor by animateColorAsState(
		targetValue = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
		label = "containerColor_animation"
	)
	val contentColor by animateColorAsState(
		targetValue = if (highlight) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
		label = "contentColor_animation"
	)

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
				modifier = Modifier.size(16.dp),
			)
			Spacer(modifier = Modifier.width(12.dp))
			Text(
				text = text,
				color = contentColor,
				style = MaterialTheme.typography.bodyMedium,
				modifier = Modifier,
			)
		}
	}
}

@Preview
@Composable
private fun ComponentHeight(
	componentHeight: Int = 144,
	onIncreaseSize: () -> Unit = {},
	onDecreaseSize: () -> Unit = {},
) {
	Row(
		verticalAlignment = Alignment.Top,
		modifier = Modifier.fillMaxWidth()
	) {
		Column {
			Text(
				text = stringResource(id = R.string.component_height),
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.Bold,
			)
			Spacer(modifier = Modifier.height(4.dp))
			Row(
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_fa_warning),
					contentDescription = stringResource(id = R.string.experimental),
					tint = MaterialTheme.colorScheme.errorContainer,
					modifier = Modifier.requiredSize(14.dp)
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(
					text = stringResource(id = R.string.experimental),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface,
					fontWeight = FontWeight.Bold,
				)
			}
		}

		Spacer(modifier = Modifier.weight(1f))

		Row(
			verticalAlignment = Alignment.CenterVertically
		) {
			MinusButton(onClick = onDecreaseSize)
			Spacer(modifier = Modifier.width(4.dp))
			Text(
				text = "$componentHeight",
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Bold,
				modifier = Modifier
					.background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
					.padding(12.dp, 8.dp)
			)
			Spacer(modifier = Modifier.width(4.dp))
			PlusButton(onClick = onIncreaseSize)
		}
	}
}

@Preview
@Composable
fun ComponentColumnCount(
	componentColumnCount: Int = 2,
	onIncreaseColumnCount: () -> Unit = {},
	onDecreaseCount: () -> Unit = {},
) {
	Row(
		verticalAlignment = Alignment.Top,
		modifier = Modifier.fillMaxWidth()
	) {
		Column {
			Text(
				text = stringResource(id = R.string.component_column_count),
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.Bold,
			)
			Spacer(modifier = Modifier.height(4.dp))
			Row(
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_fa_warning),
					contentDescription = stringResource(id = R.string.experimental),
					tint = MaterialTheme.colorScheme.errorContainer,
					modifier = Modifier.requiredSize(14.dp)
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(
					text = stringResource(id = R.string.experimental),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface,
					fontWeight = FontWeight.Bold,
				)
			}
		}

		Spacer(modifier = Modifier.weight(1f))

		Row(
			verticalAlignment = Alignment.CenterVertically
		) {
			MinusButton(onClick = onDecreaseCount)
			Spacer(modifier = Modifier.width(4.dp))
			Text(
				text = "$componentColumnCount",
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Bold,
				modifier = Modifier
					.background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
					.padding(12.dp, 8.dp)
			)
			Spacer(modifier = Modifier.width(4.dp))
			PlusButton(onClick = onIncreaseColumnCount)
		}
	}
}

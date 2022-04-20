package com.syncodec.momento.mainComponent.miscellaneous

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncodec.momento.MainActivity
import com.syncodec.momento.R
import com.syncodec.momento.custom.ChipData
import com.syncodec.momento.custom.ChipView
import com.syncodec.momento.custom.button.StateButton
import com.syncodec.momento.custom.button.StateData
import com.syncodec.momento.database.bucketItem.BucketItemType
import com.syncodec.momento.konstant.ResourceMap
import com.syncodec.momento.mainComponent.modalBottomSheet.BottomSheetType


@OptIn(
	ExperimentalMaterialApi::class, ExperimentalAnimationApi::class,
	ExperimentalMaterial3Api::class
)
@Composable
fun TopBar(
	isSelected: Boolean,
	selectedItemSize: Int,
	componentType: MainActivity.ComponentType,
	currentRoute: String?,
	showFavorite: Boolean,
	showArchived: Boolean,
	showLocked: Boolean,
	bucketFilter: List<BucketItemType>,
	onAction: (MainActivity.Action, Any?) -> Unit,
) {
	val containerColor by animateColorAsState(
		targetValue = when (currentRoute) {
			"momento" -> MaterialTheme.colorScheme.background
			"bucket" -> MaterialTheme.colorScheme.background
			"calendar" -> MaterialTheme.colorScheme.surface
			"atlas" -> MaterialTheme.colorScheme.surface
			else -> MaterialTheme.colorScheme.surface
		}
	)

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(containerColor)
	) {
		Bar(
			isSelected = isSelected,
			selectedItemSize = selectedItemSize,
			currentRoute = currentRoute,
			onAction = onAction
		)

		ComponentType(
			showComponentChooser = currentRoute == "momento",
			componentType = componentType,
			onAction = onAction
		)

		BucketFilter(
			showBucketFilter = currentRoute == "bucket",
			bucketFilter = bucketFilter,
			onAction = onAction
		)

		Filter(
			showFavorite = showFavorite,
			showArchived = showArchived,
			showLocked = showLocked,
			onAction = onAction
		)
	}
}

@Composable
private fun Bar(
	isSelected: Boolean,
	selectedItemSize: Int,
	currentRoute: String?,
	onAction: (MainActivity.Action, Any?) -> Unit
) {
	val containerColor by animateColorAsState(
		targetValue = when (currentRoute) {
			"momento" -> MaterialTheme.colorScheme.background
			"bucket" -> MaterialTheme.colorScheme.background
			"calendar" -> MaterialTheme.colorScheme.surface
			"atlas" -> MaterialTheme.colorScheme.surface
			else -> MaterialTheme.colorScheme.surface
		}
	)

	Crossfade(targetState = isSelected) {
		if (it) {
			SmallTopAppBar(
				title = {
					Text(
						text = if (selectedItemSize == 0) "Select items to delete" else if (selectedItemSize == 1) "1 item selected" else "$selectedItemSize items selected",
						modifier = Modifier,
						style = MaterialTheme.typography.titleMedium,
						color = MaterialTheme.colorScheme.onSurface
					)
				},
				actions = {
					IconButton(onClick = { onAction(MainActivity.Action.SHOW_DELETE, null) }) {
						Icon(
							painter = painterResource(id = R.drawable.ic_trash),
							contentDescription = "Delete items",
							tint = Color(0xFFF05945),
							modifier = Modifier
								.requiredSize(32.dp)
								.padding(4.dp)
						)
					}
				},
				colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = containerColor)
			)
		} else {
			CenterAlignedTopAppBar(
				navigationIcon = {
					IconButton(onClick = {
						onAction(
							MainActivity.Action.OPEN_BOTTOM_SHEET,
							BottomSheetType.MenuBottomSheet
						)
					}) {
						Icon(
							painter = painterResource(id = R.drawable.ic_icon),
							contentDescription = null,
							tint = Color.Unspecified,
							modifier = Modifier
								.requiredSize(32.dp)
								.padding(4.dp)
						)
					}
				},
				title = {
					Text(
						text = "MOMENTO",
						modifier = Modifier,
						fontFamily = FontFamily(
							Font(R.font.graduate_regular, FontWeight.Normal)
						),
						fontWeight = FontWeight.Bold,
						fontSize = 24.sp,
						lineHeight = 28.sp,
						letterSpacing = 2.sp,
						color = MaterialTheme.colorScheme.primary
					)
				},
				actions = {
					IconButton(onClick = { onAction(MainActivity.Action.SEARCH, null) }) {
						Icon(
							painter = painterResource(id = R.drawable.ic_search),
							contentDescription = "Search",
							tint = Color(0xFF2978B5),
							modifier = Modifier
								.requiredSize(32.dp)
								.padding(4.dp)
						)
					}
				},
				colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = containerColor)
			)
		}
	}
}

@Composable
private fun ComponentType(
	showComponentChooser: Boolean,
	componentType: MainActivity.ComponentType,
	onAction: (MainActivity.Action, Any?) -> Unit
) {
	AnimatedVisibility(
		visible = showComponentChooser,
		enter = expandVertically(tween(600)) + fadeIn(tween(300)),
		exit = shrinkVertically(tween(600)) + fadeOut(tween(300)),
		modifier = Modifier
			.fillMaxWidth()
			.padding(12.dp, 0.dp),
	) {
		Column(
			modifier = Modifier.fillMaxWidth()
		) {
			StateButton(
				stateList = listOf(
					StateData(
						title = "Diary",
						icon = R.drawable.ic_write,
						stateTint = MaterialTheme.colorScheme.primary
					),
					StateData(
						title = "Notebook",
						icon = R.drawable.ic_notebook,
						stateTint = MaterialTheme.colorScheme.primary
					),
				),
				currentState = componentType.ordinal,
				modifier = Modifier.height(32.dp)
			) { onAction(MainActivity.Action.CHANGE_COMPONENT, it) }
			Spacer(modifier = Modifier.height(6.dp))
		}
	}
}

@Composable
private fun BucketFilter(
	showBucketFilter: Boolean,
	bucketFilter: List<BucketItemType>,
	onAction: (MainActivity.Action, Any?) -> Unit
) {
	val chipDataList: MutableList<ChipData> = mutableListOf()

	BucketItemType.values().forEach {
		ChipData(
			title = ResourceMap.BucketItemNameMap[it]!!,
			icon = ResourceMap.bucketTypeToIcon[it]!!,
			isSelected = it in bucketFilter
		) { onAction(MainActivity.Action.BUCKET_FILTER_CHIP, it) }.apply { chipDataList.add(this) }
	}

	AnimatedVisibility(
		visible = showBucketFilter,
		enter = expandVertically(tween(600)) + fadeIn(tween(300)),
		exit = shrinkVertically(tween(600)) + fadeOut(tween(300)),
		modifier = Modifier.fillMaxWidth()
	) {
		Column(
			modifier = Modifier.fillMaxWidth()
		) {
			ChipView(chipDataList = chipDataList)
			Spacer(modifier = Modifier.height(6.dp))
		}
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun Filter(
	showFavorite: Boolean,
	showArchived: Boolean,
	showLocked: Boolean,
	onAction: (MainActivity.Action, Any?) -> Unit,
) {
	val chipDataList: List<ChipData> = listOf(
		ChipData(
			title = "Favourite",
			icon = R.drawable.ic_favourite,
			isSelected = showFavorite
		) { onAction(MainActivity.Action.TOGGLE_FAVOURITE, null) },
		ChipData(
			title = "Archive",
			icon = R.drawable.ic_archive,
			isSelected = showArchived
		) { onAction(MainActivity.Action.TOGGLE_ARCHIVED, null) },
		ChipData(
			title = "Locked",
			icon = R.drawable.ic_lock_open,
			isSelected = showLocked
		) { onAction(MainActivity.Action.LOCKED, null) }
	)

	AnimatedVisibility(
		visible = showFavorite || showArchived || showLocked,
		enter = expandVertically(tween(600)) + fadeIn(tween(300)),
		exit = shrinkVertically(tween(600)) + fadeOut(tween(300)),
		modifier = Modifier.fillMaxWidth()
	) {
		Column(
			modifier = Modifier.fillMaxWidth()
		) {
			ChipView(chipDataList = chipDataList)
			Spacer(modifier = Modifier.height(6.dp))
		}
	}
}

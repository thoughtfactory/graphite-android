package com.syncodec.graphite.presentation.bucket.composable.bar

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import com.syncodec.graphite.presentation.common.button.stateButton.StateButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateData


@Preview
@Composable
fun TopBar(
	title : String? = null,
	isLocked : Boolean = false,
	isFavourite : Boolean = false,
	bucketType : BucketType = BucketType.UNKNOWN,
	viewState : Int = 0,
	isSelecting : Boolean = false,
	selectedItemSize : Int = 0,
	onCancelSelection : () -> Unit = {},
	onClickFavourite : () -> Unit = {},
	onClickLock : () -> Unit = {},
	onStateChange : (Int) -> Unit = {},
	onShare : () -> Unit = {},
	onDelete : () -> Unit = {},
) {

	val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.background)
	) {
		Crossfade(
			targetState = isSelecting,
			animationSpec = tween(durationMillis = 300)
		) {
			if (it) {
				SelectionBar(
					selectedItemSize = selectedItemSize,
					onCancelSelection = onCancelSelection,
					onShare = onShare,
					onDelete = onDelete,
				)
			} else {
				Bar(
					title = title,
					isLocked = isLocked,
					isFavourite = isFavourite,
					onClickFavourite = onClickFavourite,
					onClickLock = onClickLock,
					onClickBack = { backPressedDispatcher?.onBackPressed() }
				)
			}
		}

		AnimatedVisibility(
			visible = ! isSelecting && bucketType != BucketType.LINK,
			enter = expandVertically(tween(300)),
			exit = shrinkVertically(tween(300))
		) {
			StateView(
				bucketType = bucketType,
				currentState = viewState,
				onStateChange = onStateChange
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Bar(
	title : String?,
	isFavourite : Boolean = false,
	isLocked : Boolean = false,
	onClickFavourite : () -> Unit = {},
	onClickLock : () -> Unit = {},
	onClickBack : () -> Unit = {},
) {
	TopAppBar(
		navigationIcon = {
			MenuButton(
				icon = R.drawable.ic_back,
				tooltip = "Back",
				onClick = onClickBack
			)
		},
		title = {
			Crossfade(
				targetState = title,
				animationSpec = tween(300)
			) {
				Text(
					text = it ?: "Untitled",
					color = MaterialTheme.colorScheme.onBackground,
					fontStyle = if (it == null) FontStyle.Italic else FontStyle.Normal
				)
			}
		},
		actions = {
			MenuButton(
				icon = if (isLocked) R.drawable.ic_lock_close else R.drawable.ic_lock_open,
				tooltip = if (isLocked) "Locked" else "Not locked",
				checked = isLocked,
				onClick = onClickLock
			)

			MenuButton(
				icon = R.drawable.ic_favourite,
				tooltip = "Favourite",
				checked = isFavourite,
				onClick = onClickFavourite,
			)
		},
		colors = TopAppBarDefaults.topAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
			titleContentColor = MaterialTheme.colorScheme.onSurface,
			actionIconContentColor = MaterialTheme.colorScheme.onSurface,
		)
	)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
private fun SelectionBar(
	selectedItemSize : Int = 0,
	onCancelSelection : () -> Unit = {},
	onShare : () -> Unit = {},
	onDelete : () -> Unit = {},
) {
	TopAppBar(
		navigationIcon = {
			MenuButton(
				icon = R.drawable.ic_close,
				tooltip = "Cancel selection",
				onClick = onCancelSelection,
			)
		},
		title = {
			AnimatedText(
				text = if (selectedItemSize == 0) "No items selected" else if (selectedItemSize == 1) "1 item selected" else "${selectedItemSize} items selected",
				color = MaterialTheme.colorScheme.onBackground,
				transitionSpec = { fadeIn(tween(300)) with fadeOut(tween(300)) }
			)
		},
		actions = {
			MenuButton(
				icon = R.drawable.ic_share,
				tooltip = "Share items",
				onClick = onShare
			)

			MenuButton(
				icon = R.drawable.ic_delete,
				tooltip = "Delete items",
				colors = MenuButtonDefaults.deleteButtonColors(),
				onClick = onDelete
			)
		},
		colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
	)
}

@Composable
private fun StateView(
	bucketType : BucketType,
	currentState : Int,
	onStateChange : (Int) -> Unit
) {
	val stateNameList : List<String> = when (bucketType) {
		BucketType.TODO -> listOf("All", "To Do", "Doing", "Done")
		BucketType.BOOK -> listOf("All", "To Read", "Reading", "Read")
		BucketType.SHOW -> listOf("All", "To Watch", "Watching", "Watched")
		BucketType.LINK -> listOf("All", "To Visit", "Opened", "Done")
		BucketType.UNKNOWN -> listOf("All", "To Do", "Doing", "Done")
	}
	val stateIconList : List<Int> = when (bucketType) {
		BucketType.TODO -> listOf(
			R.drawable.ic_state,
			R.drawable.ic_clock,
			R.drawable.ic_todo,
			R.drawable.ic_done
		)

		BucketType.BOOK -> listOf(
			R.drawable.ic_state,
			R.drawable.ic_clock,
			R.drawable.ic_book,
			R.drawable.ic_done
		)

		BucketType.SHOW -> listOf(
			R.drawable.ic_state,
			R.drawable.ic_clock,
			R.drawable.ic_show,
			R.drawable.ic_done
		)

		BucketType.LINK -> listOf(
			R.drawable.ic_state,
			R.drawable.ic_clock,
			R.drawable.ic_show,
			R.drawable.ic_done
		)

		BucketType.UNKNOWN -> listOf(
			R.drawable.ic_state,
			R.drawable.ic_clock,
			R.drawable.ic_todo,
			R.drawable.ic_done
		)
	}

	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		StateButton(
			stateList = listOf(
				StateData(
					title = stateNameList[0],
					icon = stateIconList[0],
					stateTint = MaterialTheme.colorScheme.primary
				),
				StateData(
					title = stateNameList[1],
					icon = stateIconList[1],
					stateTint = MaterialTheme.colorScheme.primary
				),
				StateData(
					title = stateNameList[2],
					icon = stateIconList[2],
					stateTint = MaterialTheme.colorScheme.primary
				),
				StateData(
					title = stateNameList[3],
					icon = stateIconList[3],
					stateTint = MaterialTheme.colorScheme.primary
				)
			),
			currentState = currentState,
			modifier = Modifier
				.height(36.dp)
				.padding(12.dp, 0.dp),
			onChangeState = onStateChange
		)
		Spacer(modifier = Modifier.height(8.dp))
	}
}

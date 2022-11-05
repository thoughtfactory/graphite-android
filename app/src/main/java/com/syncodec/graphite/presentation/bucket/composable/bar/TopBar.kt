package com.syncodec.graphite.presentation.bucket.composable.bar

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.BucketActivity
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionBucketObject
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnClickFavourite
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnClickLock
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnPagerStateChange
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionPagerState
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionSelectedObjectIdList
import com.syncodec.graphite.presentation.bucket.composable.dialog.BucketDialogType
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateData
import com.syncodec.graphite.presentation.ui.DeleteContainer


@Preview
@Composable
fun TopBar() {
	val bucketObject = LocalCompositionBucketObject.current
	val viewState = LocalCompositionPagerState.current

	val onClickFavourite = LocalCompositionOnClickFavourite.current
	val onClickLock = LocalCompositionOnClickLock.current
	val onStateChange = LocalCompositionOnPagerStateChange.current

	val bucketType = try {
		BucketType.valueOf(bucketObject?.bucketType ?: "")
	} catch (e : Exception) {
		BucketType.UNKNOWN
	}

	val isSelected = LocalCompositionIsSelected.current

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.background)
	) {
		Crossfade(
			targetState = isSelected,
			animationSpec = tween(durationMillis = 300)
		) {
			if (it) {
				SelectionBar()
			} else {
				Bar(
					title = bucketObject?.title,
					isLocked = bucketObject?.isLocked ?: false,
					isFavourite = bucketObject?.isFavourite ?: false,
					onClickFavourite = onClickFavourite,
					onClickLock = onClickLock
				)
			}
		}

		AnimatedVisibility(
			visible = ! isSelected && bucketType != BucketType.LINK,
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
	isFavourite : Boolean,
	isLocked : Boolean,
	onClickFavourite : () -> Unit,
	onClickLock : () -> Unit
) {
	val activity : BucketActivity = LocalContext.current as BucketActivity

	TopAppBar(
		navigationIcon = {
			MenuButton(
				icon = R.drawable.ic_back,
				contentDescription = "Back",
				tint = MaterialTheme.colorScheme.onBackground
			) { activity.onBackPressed() }
		},
		title = {
			Crossfade(
				targetState = title,
				animationSpec = tween(300)
			) {
				Text(
					text = it ?: "Untitled",
					color = MaterialTheme.colorScheme.onBackground,
					fontWeight = FontWeight.Bold,
					fontStyle = if (it == null) FontStyle.Italic else FontStyle.Normal
				)
			}
		},
		actions = {
			MenuButton(
				icon = if (isLocked) R.drawable.ic_lock_close else R.drawable.ic_lock_open,
				contentDescription = if (isLocked) "Locked" else "Not locked",
				tint = MaterialTheme.colorScheme.onBackground,
				isChecked = isLocked,
				isEnabled = true,
				onClick = onClickLock
			)

			MenuButton(
				icon = R.drawable.ic_favourite,
				contentDescription = "Favourite",
				tint = MaterialTheme.colorScheme.onBackground,
				isChecked = isFavourite,
				isEnabled = true,
				onClick = onClickFavourite,
			)
		},
		colors = TopAppBarDefaults.smallTopAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
			titleContentColor = MaterialTheme.colorScheme.onSurface,
			actionIconContentColor = MaterialTheme.colorScheme.onSurface,
		)
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionBar() {

	val selectedObjectIdList = LocalCompositionSelectedObjectIdList.current

	val openDialog = LocalCompositionOpenDialog.current

	TopAppBar(
		title = {
			Text(
				text = if (selectedObjectIdList.size == 0) "Select items to delete" else if (selectedObjectIdList.size == 1) "1 item selected" else "${selectedObjectIdList.size} items selected",
				color = MaterialTheme.colorScheme.onBackground
			)
		},
		actions = {
			IconButton(
				onClick = { openDialog(BucketDialogType.DELETE) }
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_delete),
					contentDescription = "Delete items",
					tint = Color.DeleteContainer
				)
			}
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
			modifier = Modifier.height(36.dp),
			onStateChange = onStateChange
		)
		Spacer(modifier = Modifier.height(8.dp))
	}
}

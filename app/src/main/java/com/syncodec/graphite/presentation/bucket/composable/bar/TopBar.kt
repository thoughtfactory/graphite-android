package com.syncodec.graphite.presentation.bucket.composable.bar

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.BucketActivity
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateData


@Composable
fun TopBar(
	title: String,
	bucketType: BucketType,
	isFavourite: Boolean,
	isLocked: Boolean,
	viewState: Int,
	onClickFavourite: () -> Unit,
	onClickLock: () -> Unit,
	onStateChange: (Int) -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.background)
	) {
		Bar(
			title = title,
			isLocked = isLocked,
			isFavourite = isFavourite,
			onClickFavourite = onClickFavourite,
			onClickLock = onClickLock
		)

		StateSelector(
			showStateSelector = true,
			bucketType = bucketType,
			currentState = viewState,
			onStateChange = onStateChange
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Bar(
	title: String,
	isFavourite: Boolean,
	isLocked: Boolean,
	onClickFavourite: () -> Unit,
	onClickLock: () -> Unit
) {
	val activity: BucketActivity = LocalContext.current as BucketActivity

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
					text = it,
					color = MaterialTheme.colorScheme.onBackground,
					fontWeight = FontWeight.Bold
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

@Composable
private fun StateSelector(
	showStateSelector: Boolean,
	bucketType: BucketType,
	currentState: Int,
	onStateChange: (Int) -> Unit
) {
	val stateNameList: List<String> = when (bucketType) {
		BucketType.TODO -> listOf("All", "To Do", "Doing", "Done")
		BucketType.BOOK -> listOf("All", "To Read", "Reading", "Read")
		BucketType.SHOW -> listOf("All", "To Watch", "Watching", "Watched")
		BucketType.LINK -> listOf("All", "To Visit", "Opened", "Done")
		BucketType.UNKNOWN -> listOf("All", "To Do", "Doing", "Done")
	}
	val stateIconList: List<Int> = when (bucketType) {
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

	AnimatedVisibility(
		visible = showStateSelector && bucketType != BucketType.LINK,
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
}

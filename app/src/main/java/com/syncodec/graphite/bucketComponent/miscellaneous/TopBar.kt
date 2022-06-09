package com.syncodec.graphite.bucketComponent.miscellaneous

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.bucketComponent.BucketActivity
import com.syncodec.graphite.custom.button.StateButton
import com.syncodec.graphite.custom.button.StateData
import com.syncodec.graphite.database.bucketItem.BucketItemType


@Composable
fun TopBar(
	title: String,
	bucketItemType: BucketItemType,
	showStateSelector: Boolean,
	currentState: Int,
	onAction: (BucketActivity.Action, Any?) -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface)
	) {
		Bar(
			title = title,
			onAction = onAction
		)

		StateSelector(
			showStateSelector = showStateSelector,
			currentState = currentState,
			bucketItemType = bucketItemType,
			onAction = onAction
		)
	}
}

@Composable
private fun Bar(
	title: String,
	onAction: (BucketActivity.Action, Any?) -> Unit
) {
	SmallTopAppBar(
		navigationIcon = {
			IconButton(onClick = { onAction(BucketActivity.Action.BACK, null) }) {
				Icon(
					painter = painterResource(id = R.drawable.ic_back),
					contentDescription = "Back",
					tint = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier
						.requiredSize(32.dp)
						.padding(4.dp)
				)
			}
		},
		title = {
			Text(
				text = title,
				color = MaterialTheme.colorScheme.onSurface,
				style = MaterialTheme.typography.titleMedium
			)
		},
		actions = {
			IconButton(onClick = { onAction(BucketActivity.Action.MENU, null) }) {
				Icon(
					painter = painterResource(id = R.drawable.ic_menu),
					contentDescription = "Menu",
					tint = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier
						.requiredSize(32.dp)
						.padding(4.dp)
				)
			}
		},
		colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
	)
}

@Composable
private fun StateSelector(
	showStateSelector: Boolean,
	currentState: Int,
	bucketItemType: BucketItemType,
	onAction: (BucketActivity.Action, Any?) -> Unit
) {
	val stateNameList: List<String> = when (bucketItemType) {
		BucketItemType.TODO -> listOf("All", "To Do", "Doing", "Done")
		BucketItemType.BOOKS -> listOf("All", "To Read", "Reading", "Read")
		BucketItemType.SHOWS -> listOf("All", "To Watch", "Watching", "Watched")
	}
	val stateIconList: List<Int> = when (bucketItemType) {
		BucketItemType.TODO -> listOf(
			R.drawable.ic_state,
			R.drawable.ic_clock,
			R.drawable.ic_todo,
			R.drawable.ic_done
		)
		BucketItemType.BOOKS -> listOf(
			R.drawable.ic_state,
			R.drawable.ic_clock,
			R.drawable.ic_book,
			R.drawable.ic_done
		)
		BucketItemType.SHOWS -> listOf(
			R.drawable.ic_state,
			R.drawable.ic_clock,
			R.drawable.ic_show,
			R.drawable.ic_done
		)
	}

	AnimatedVisibility(
		visible = showStateSelector,
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
				modifier = Modifier.height(32.dp)
			) { onAction(BucketActivity.Action.CHANGE_STATE, it) }
			Spacer(modifier = Modifier.height(8.dp))
		}
	}
}

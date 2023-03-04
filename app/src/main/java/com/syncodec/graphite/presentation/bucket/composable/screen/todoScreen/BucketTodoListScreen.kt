package com.syncodec.graphite.presentation.bucket.composable.screen.todoScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun BucketTodoListScreen(
	bucketItemList : List<BucketItemObject> = listOf(),
	isSelecting : Boolean = false,
	selectedIdList : List<RealmUUID> = listOf(),
	onSelect : (RealmUUID) -> Unit = {},
	onClickBucketItem : (RealmUUID) -> Unit = {},
	onClickFavourite : (BucketItemObject) -> Unit = {},
	onClickLock : (BucketItemObject) -> Unit = {},
	onCheckedChange : (BucketItemObject) -> Unit = {},
) {
	val hapticFeedback = LocalHapticFeedback.current

	if (bucketItemList.isEmpty()) {
		EmptyView(bucketType = BucketType.TODO)
	} else {
		LazyColumn(
			modifier = Modifier.fillMaxSize()
		) {
			bucketItemList.sortedBy { it.state }.forEach { bucketItemObject ->
				item(
					key = bucketItemObject.id.toString(),
				) {
					Box(
						modifier = Modifier.animateItemPlacement()
					) {
						TodoItem(
							title = bucketItemObject.title,
							state = ((BucketItemState.values().find { it.name == bucketItemObject.state }?.ordinal ?: 0) + 1) % 3,
							isLocked = bucketItemObject.isLocked,
							isFavourite = bucketItemObject.isFavourite,
							isSelected = bucketItemObject.id in selectedIdList,
							onClick = { if (isSelecting) onSelect(bucketItemObject.id) else onClickBucketItem(bucketItemObject.id) },
							onLongClick = {
								hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
								onSelect(bucketItemObject.id)
							},
							onCheckedChange = { onCheckedChange(bucketItemObject) },
							onToggleLock = { onClickLock(bucketItemObject) },
							onToggleFavourite = { onClickFavourite(bucketItemObject) },
						)
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Preview
@Composable
private fun TodoItem(
	title : String? = "title",
	state : Int = 0,
	isLocked : Boolean = false,
	isFavourite : Boolean = false,
	isSelected : Boolean = false,
	onClick : () -> Unit = {},
	onLongClick : () -> Unit = {},
	onCheckedChange : () -> Unit = {},
	onToggleLock : () -> Unit = {},
	onToggleFavourite : () -> Unit = {},
) {

	val containerColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.47f) else Color.Transparent,
		animationSpec = tween(300)
	)
	val contentColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onBackground,
		animationSpec = tween(300)
	)

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.background(containerColor)
			.combinedClickable(
				enabled = true,
				onClick = onClick,
				onLongClick = onLongClick
			)
	) {
		Row(
			modifier = Modifier
				.fillMaxSize()
				.padding(16.dp, 4.dp),
			verticalAlignment = Alignment.CenterVertically
		) {

			TriStateCheckbox(
				state = ToggleableState.values().getOrElse(state) { ToggleableState.Off },
				onClick = onCheckedChange
			)

			Spacer(modifier = Modifier.width(12.dp))

			AnimatedContent(
				targetState = state,
				transitionSpec = { fadeIn(tween(300)) with fadeOut(tween(300)) },
				modifier = Modifier.weight(1f)
			) {
				if (state == 0) {
					Text(
						text = if (title.isNullOrEmpty()) "Untitled" else title,
						color = contentColor,
						style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough),
						maxLines = 1,
						fontStyle = if (title.isNullOrBlank()) FontStyle.Italic else FontStyle.Normal,
						modifier = Modifier.weight(1f)
					)
				} else {
					Text(
						text = if (title.isNullOrEmpty()) "Untitled" else title,
						color = contentColor,
						style = MaterialTheme.typography.bodyMedium,
						maxLines = 1,
						fontStyle = if (title.isNullOrBlank()) FontStyle.Italic else FontStyle.Normal,
						modifier = Modifier.weight(1f)
					)
				}
			}

			MenuButton(
				icon = if (isLocked) R.drawable.ic_lock_close else R.drawable.ic_lock_open,
				tooltip = if (isLocked) "Locked" else "Not locked",
				checked = isLocked,
				colors = MenuButtonDefaults.menuButtonColors(
					containerColor = Color.Transparent,
					iconColor = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onBackground,
					checkedContainerColor = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
					checkedIconColor = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface,
				),
				onClick = onToggleLock,
			)

			MenuButton(
				icon = R.drawable.ic_favourite,
				tooltip = "Favourite",
				checked = isFavourite,
				shape = MaterialTheme.shapes.medium,
				colors = MenuButtonDefaults.menuButtonColors(
					containerColor = Color.Transparent,
					iconColor = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onBackground,
					checkedContainerColor = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
					checkedIconColor = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface,
				),
				onClick = onToggleFavourite,
			)
		}
	}
}

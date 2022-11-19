package com.syncodec.graphite.presentation.bucket.composable.screen

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionBucketObject
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnClickBucketItemFavourite
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnClickBucketItemLock
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnDelete
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnPutTodo
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOpenBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionSetBucketItemObject
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.BucketBottomSheetType
import com.syncodec.graphite.presentation.bucket.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.LocalCompositionOnSelect
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedRealmUUIDList
import com.syncodec.graphite.presentation.common.button.MenuButton


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun TodoScreen(
	bucketItemList : List<BucketItemObject> = listOf(),
) {
	val isSelected = LocalCompositionIsSelected.current
	val onSelected = LocalCompositionOnSelect.current
	val selectedRealmUUIDList = LocalCompositionSelectedRealmUUIDList.current

	val setBucketItemObject = LocalCompositionSetBucketItemObject.current
	val openSheet = LocalCompositionOpenBottomSheet.current

	val onAddTodo = LocalCompositionOnPutTodo.current
	val onClickLock = LocalCompositionOnClickBucketItemLock.current
	val onClickFavourite = LocalCompositionOnClickBucketItemFavourite.current

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
							isSelected = bucketItemObject.id in selectedRealmUUIDList,
							onClick = {
								if (isSelected) {
									if (bucketItemObject.id in selectedRealmUUIDList) selectedRealmUUIDList.remove(bucketItemObject.id)
									else selectedRealmUUIDList.add(bucketItemObject.id)
								} else {
									setBucketItemObject(bucketItemObject)
									openSheet(BucketBottomSheetType.ADD_TODO)
								}
							},
							onLongClick = {
								if (bucketItemObject.id in selectedRealmUUIDList) selectedRealmUUIDList.remove(bucketItemObject.id)
								else selectedRealmUUIDList.add(bucketItemObject.id)
								onSelected(true)
							},
							onCheckedChange = {
								onAddTodo(
									bucketItemObject.id,
									bucketItemObject.title ?: "",
									BucketItemState.values()[((BucketItemState.values().find { it.name == bucketItemObject.state }
										?: BucketItemState.ALPHA).ordinal + 1) % 3]
								)
							},
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
@Composable
private fun TodoItem(
	title : String?,
	state : Int,
	isLocked : Boolean,
	isFavourite : Boolean,
	isSelected : Boolean,
	onClick : () -> Unit,
	onLongClick : () -> Unit,
	onCheckedChange : () -> Unit,
	onToggleLock : () -> Unit,
	onToggleFavourite : () -> Unit,
) {

	val containerColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
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
						style = MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.LineThrough),
						maxLines = 1,
						fontStyle = if (title.isNullOrBlank()) FontStyle.Italic else FontStyle.Normal,
						modifier = Modifier.weight(1f)
					)
				} else {
					Text(
						text = if (title.isNullOrEmpty()) "Untitled" else title,
						color = contentColor,
						style = MaterialTheme.typography.bodyLarge,
						maxLines = 1,
						fontStyle = if (title.isNullOrBlank()) FontStyle.Italic else FontStyle.Normal,
						modifier = Modifier.weight(1f)
					)
				}
			}

			MenuButton(
				icon = if (isLocked) R.drawable.ic_lock_close else R.drawable.ic_lock_open,
				contentDescription = if (isLocked) "Locked" else "Not locked",
				tint = MaterialTheme.colorScheme.onBackground,
				isChecked = isLocked,
				isEnabled = true,
				onClick = onToggleLock
			)

			MenuButton(
				icon = R.drawable.ic_favourite,
				contentDescription = "Favourite",
				tint = MaterialTheme.colorScheme.onBackground,
				isChecked = isFavourite,
				isEnabled = true,
				onClick = onToggleFavourite
			)
		}
	}
}

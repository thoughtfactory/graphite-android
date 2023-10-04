package com.syncodec.graphite.presentation.bucket.composable.screen.todoScreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.common.reorderable.ReorderableItem
import com.syncodec.graphite.presentation.common.reorderable.SpringDragCancelledAnimation
import com.syncodec.graphite.presentation.common.reorderable.detectReorder
import com.syncodec.graphite.presentation.common.reorderable.lazyState.rememberReorderableLazyListState
import com.syncodec.graphite.presentation.common.reorderable.reorderable
import com.syncodec.graphite.presentation.common.selectable.SelectableContainer
import com.syncodec.graphite.presentation.base.FavouriteContainer
import com.syncodec.graphite.presentation.base.LockClosedContainer
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun BucketTodoListScreen(
	bucketItemList: List<BucketItemObject> = listOf(),
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	onSelect: (RealmUUID) -> Unit = {},
	onClickBucketItem: (BucketItemObject) -> Unit = {},
	onCheckedChange: (RealmUUID) -> Unit = {},
	onReorderBucketItemList: (List<RealmUUID>) -> Unit = {},
) {
	var bucketItemListOrdered by remember { mutableStateOf<List<BucketItemObject>>(listOf()) }
	LaunchedEffect(bucketItemList) { bucketItemListOrdered = bucketItemList.toList() }
	val state = rememberReorderableLazyListState(
		dragCancelledAnimation = SpringDragCancelledAnimation(),
		onMove = { from, to ->
			bucketItemListOrdered.toMutableList().apply {
				add(to.index, removeAt(from.index))
				bucketItemListOrdered = toList()
			}
		},
		onDragEnd = { from, to -> onReorderBucketItemList(bucketItemListOrdered.map { it.id }) }
	)

	if (bucketItemList.isEmpty()) EmptyView(bucketType = BucketType.TODO)
	else LazyColumn(
		state = state.listState,
		modifier = Modifier
			.fillMaxSize()
			.reorderable(state)
	) {
		items(
			items = bucketItemListOrdered,
			key = { it.id.toString() },
			contentType = { 0 }
		) { bucketItemObject ->
			ReorderableItem(
				reorderableState = state,
				key = bucketItemObject.id.toString(),
				modifier = Modifier.animateItemPlacement()
			) { isDragging ->
				TodoItem(
					title = bucketItemObject.title,
					state = bucketItemObject.getState(),
					dragHandle = {
						Icon(
							painter = painterResource(id = R.drawable.ic_fa_grip),
							contentDescription = stringResource(id = R.string.reorder_grip),
							tint = MaterialTheme.colorScheme.onSurface,
							modifier = Modifier
								.size(16.dp)
								.detectReorder(state)
						)
					},
					isLocked = bucketItemObject.isLocked,
					isFavourite = bucketItemObject.isFavourite,
					isSelected = isDragging or (bucketItemObject.id in selectedIdList),
					onClick = { if (isSelecting) onSelect(bucketItemObject.id) else onClickBucketItem(bucketItemObject) },
					onLongClick = { onSelect(bucketItemObject.id) },
					onCheckedChange = { onCheckedChange(bucketItemObject.id) },
				)
			}
		}
		item { Spacer(modifier = Modifier.height(128.dp)) }
	}
}

@Preview
@Composable
private fun TodoItem(
	title: String? = null,
	state: Int = 0,
	dragHandle: @Composable () -> Unit = {},
	isLocked: Boolean = false,
	isFavourite: Boolean = false,
	isSelected: Boolean = false,
	onClick: () -> Unit = {},
	onLongClick: () -> Unit = {},
	onCheckedChange: () -> Unit = {},
) {
	SelectableContainer(
		selected = isSelected,
		onClick = onClick,
		onLongClick = onLongClick,
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp)
		) {
			dragHandle()

			Spacer(modifier = Modifier.width(4.dp))

			TriStateCheckbox(
				state = ToggleableState.entries.getOrElse((state + 1) % 3) { ToggleableState.Off },
				onClick = onCheckedChange
			)

			Spacer(modifier = Modifier.width(12.dp))

//			AnimatedContent(
//				targetState = state,
//				transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
//				modifier = Modifier.weight(1f),
//				label = ""
//			) {
			if (state == 2) {
				Text(
					text = if (title.isNullOrEmpty()) stringResource(id = R.string.untitled) else title,
					style = MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.LineThrough),
					maxLines = 1,
					fontStyle = if (title.isNullOrBlank()) FontStyle.Italic else FontStyle.Normal,
					modifier = Modifier.weight(1f)
				)
			} else {
				Text(
					text = if (title.isNullOrEmpty()) stringResource(id = R.string.untitled) else title,
					style = MaterialTheme.typography.bodyLarge,
					maxLines = 1,
					fontStyle = if (title.isNullOrBlank()) FontStyle.Italic else FontStyle.Normal,
					modifier = Modifier.weight(1f)
				)
			}
//			}

			Spacer(modifier = Modifier.width(12.dp))

			if (isFavourite or isLocked) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier
						.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.47f), MaterialTheme.shapes.small)
						.padding(8.dp, 4.dp)
				) {
					if (isLocked) {
						Icon(
							painter = painterResource(id = R.drawable.ic_fa_lock_close_solid),
							contentDescription = stringResource(id = R.string.locked),
							tint = Color.LockClosedContainer,
							modifier = Modifier.requiredSize(12.dp)
						)
					}
					if (isFavourite and isLocked) {
						Text(
							text = "·",
							style = MaterialTheme.typography.bodySmall,
							fontWeight = FontWeight.Bold,
							maxLines = 1,
							modifier = Modifier.padding(horizontal = 2.dp)
						)
					}
					if (isFavourite) {
						Icon(
							painter = painterResource(id = R.drawable.ic_fa_heart_solid),
							contentDescription = stringResource(id = R.string.favourite),
							tint = Color.FavouriteContainer,
							modifier = Modifier.requiredSize(12.dp)
						)
					}
				}
			}
		}
	}
}

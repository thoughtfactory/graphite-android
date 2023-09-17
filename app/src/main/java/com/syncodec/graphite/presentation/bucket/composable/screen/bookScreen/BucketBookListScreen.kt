package com.syncodec.graphite.presentation.bucket.composable.screen.bookScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.composable.buildingBlock.BucketListItem
import com.syncodec.graphite.presentation.bucket.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.common.reorderable.ReorderableItem
import com.syncodec.graphite.presentation.common.reorderable.SpringDragCancelledAnimation
import com.syncodec.graphite.presentation.common.reorderable.detectReorder
import com.syncodec.graphite.presentation.common.reorderable.lazyState.rememberReorderableLazyListState
import com.syncodec.graphite.presentation.common.reorderable.reorderable
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Preview
@Composable
fun BucketBookListScreen(
	bucketId: RealmUUID? = null,
	bucketItemList: List<BucketItemObject> = listOf(),
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	onClickBucketItem: (RealmUUID) -> Unit = {},
	onSelect: (RealmUUID) -> Unit = {},
	onReorderBucketItemList: (List<RealmUUID>) -> Unit = {},
) {
	val scope = rememberCoroutineScope()

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
		onDragEnd = { from, to ->
			scope.launch(Dispatchers.Default) { onReorderBucketItemList(bucketItemListOrdered.map { it.id }) }
		}
	)

	if (bucketItemList.isEmpty()) {
		EmptyView(bucketType = BucketType.BOOK)
	} else {
		LazyColumn(
			state = state.listState,
			modifier = Modifier
				.fillMaxSize()
				.reorderable(state)
		) {
			items(
				items = bucketItemList,
				key = { it.id.toString() }
			) { bucketItemObject ->
				ReorderableItem(
					reorderableState = state,
					key = bucketItemObject.id.toString()
				) { isDragging ->
					BucketListItem(
						title = bucketItemObject.title,
						description = bucketItemObject.getBookDescription(),
						thumbnail = bucketItemObject.thumbnail,
						dragHandle = {
							Box(
								contentAlignment = Alignment.Center,
								modifier = Modifier
									.requiredSize(24.dp)
									.background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.small)
									.detectReorder(state)
									.padding(2.dp)
							) {
								Icon(
									painter = painterResource(id = R.drawable.ic_fa_grip),
									contentDescription = stringResource(id = R.string.reorder_grip),
									tint = MaterialTheme.colorScheme.onBackground,
									modifier = Modifier.requiredSize(16.dp)
								)
							}
						},
						isLocked = bucketItemObject.isLocked,
						isFavourite = bucketItemObject.isFavourite,
						isSelected = isDragging or (bucketItemObject.id in selectedIdList),
						onClick = { if (isSelecting) onSelect(bucketItemObject.id) else onClickBucketItem(bucketItemObject.id) },
						onLongClick = { onSelect(bucketItemObject.id) },
					)
				}
			}
		}
	}
}

package com.syncodec.graphite.presentation.bucket.composable.screen.bookScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.local.BucketItemObject
import com.syncodec.graphite.di.model.local.BucketItemState
import com.syncodec.graphite.di.model.local.BucketType
import com.syncodec.graphite.presentation.bucket.composable.buildingBlock.BucketGridItem
import com.syncodec.graphite.presentation.bucket.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.common.reorderable.SpringDragCancelledAnimation
import com.syncodec.graphite.presentation.common.reorderable.lazyState.rememberReorderableLazyGridState
import com.syncodec.graphite.presentation.common.reorderable.reorderable
import com.syncodec.graphite.utils.isTablet
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Preview
@Composable
fun BucketBookGridScreen(
	bucketItemList: List<BucketItemObject> = listOf(),
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	onReorderBucketItemList: (List<RealmUUID>) -> Unit = {},
	onSelect: (RealmUUID) -> Unit = {},
	onClickBucketItem: (RealmUUID) -> Unit = {},
) {
	val scope = rememberCoroutineScope()

	var bucketItemListOrdered by remember { mutableStateOf<List<BucketItemObject>>(listOf()) }
	LaunchedEffect(bucketItemList) { bucketItemListOrdered = bucketItemList.toList() }
	val reorderableLazyGridState = rememberReorderableLazyGridState(
		dragCancelledAnimation = SpringDragCancelledAnimation(),
		onMove = { from, to ->
			bucketItemListOrdered.toMutableList().apply {
				add(to.index, removeAt(from.index))
				bucketItemListOrdered = toList()
			}
		},
		onDragEnd = { from, to -> scope.launch(Dispatchers.Default) { onReorderBucketItemList(bucketItemListOrdered.map { it.id }) } }
	)

	if (bucketItemListOrdered.isEmpty()) EmptyView(bucketType = BucketType.BOOK)
	else LazyVerticalGrid(
		columns = GridCells.Adaptive(if (isTablet()) 144.dp else 96.dp),
		horizontalArrangement = Arrangement.Center,
		contentPadding = PaddingValues(8.dp, 0.dp),
		modifier = Modifier
			.fillMaxSize()
			.reorderable(state = reorderableLazyGridState)
	) {
		items(
			items = bucketItemListOrdered,
			key = { it.id.toString() }
		) { bucketItemObject ->
			BucketGridItem(
				id = bucketItemObject.id,
				bucketType = BucketType.BOOK,
				title = bucketItemObject.title,
				thumbnail = bucketItemObject.thumbnail,
				isLocked = bucketItemObject.isLocked,
				isFavourite = bucketItemObject.isFavourite,
				bucketItemState = BucketItemState.entries.find { it.name == bucketItemObject.state },
				isSelected = bucketItemObject.id in selectedIdList,
				onLongClick = { onSelect(bucketItemObject.id) },
				onClick = { if (isSelecting) onSelect(bucketItemObject.id) else onClickBucketItem(bucketItemObject.id) },
			)
		}
	}
}

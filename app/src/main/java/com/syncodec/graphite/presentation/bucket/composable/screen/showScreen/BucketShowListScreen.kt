package com.syncodec.graphite.presentation.bucket.composable.screen.showScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.BucketItemData
import com.syncodec.graphite.di.model.local.BucketItemObject
import com.syncodec.graphite.di.model.local.BucketItemState
import com.syncodec.graphite.di.model.local.BucketType
import com.syncodec.graphite.presentation.base.AttachmentContainer
import com.syncodec.graphite.presentation.base.LocalIsDarkTheme
import com.syncodec.graphite.presentation.base.LocationContainer
import com.syncodec.graphite.presentation.base.LockClosedContainer
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
fun BucketShowListScreen(
	bucketItemList: List<BucketItemObject> = listOf(),
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	onReorderBucketItemList: (List<RealmUUID>) -> Unit = {},
	onSelect: (RealmUUID) -> Unit = {},
	onClickBucketItem: (RealmUUID) -> Unit = {},
) {
	val scope = rememberCoroutineScope()
	val isDarkTheme = LocalIsDarkTheme.current

	var bucketItemListOrdered by remember { mutableStateOf<List<BucketItemObject>>(listOf()) }
	LaunchedEffect(bucketItemList) { bucketItemListOrdered = bucketItemList.toList() }
	val reorderableLazyListState = rememberReorderableLazyListState(
		dragCancelledAnimation = SpringDragCancelledAnimation(),
		onMove = { from, to ->
			bucketItemListOrdered.toMutableList().apply {
				add(to.index, removeAt(from.index))
				bucketItemListOrdered = toList()
			}
		},
		onDragEnd = { from, to -> scope.launch(Dispatchers.Default) { onReorderBucketItemList(bucketItemListOrdered.map { it.id }) } }
	)

	if (bucketItemList.isEmpty()) EmptyView(bucketType = BucketType.SHOW)
	else LazyColumn(
		state = reorderableLazyListState.listState,
		modifier = Modifier
			.fillMaxSize()
			.reorderable(reorderableLazyListState)
	) {
		items(
			items = bucketItemListOrdered,
			key = { it.id.toString() }
		) { bucketItemObject ->
			ReorderableItem(
				reorderableState = reorderableLazyListState,
				key = bucketItemObject.id.toString()
			) { isDragging ->
				BucketListItem(
					id = bucketItemObject.id,
					title = bucketItemObject.title,
					bucketType = BucketType.SHOW,
					description = bucketItemObject.getBucketItemData<BucketItemData>()?.description,
					thumbnail = bucketItemObject.thumbnail,
					dragHandle = {
						Box(
							modifier = Modifier
								.width(24.dp)
								.height(108.dp)
								.background(
									when (bucketItemObject.state) {
										BucketItemState.ALPHA.name -> Color.LocationContainer.copy(alpha = if (isDarkTheme) 0.31f else 0.17f)
										BucketItemState.BETA.name -> Color.AttachmentContainer.copy(alpha = if (isDarkTheme) 0.31f else 0.17f)
										BucketItemState.GAMMA.name -> Color.LockClosedContainer.copy(alpha = if (isDarkTheme) 0.31f else 0.17f)
										else -> Color.Transparent
									},
									MaterialTheme.shapes.small
								),
							contentAlignment = Alignment.Center
						) {
							Box(
								contentAlignment = Alignment.Center,
								modifier = Modifier
									.requiredSize(24.dp)
									.detectReorder(reorderableLazyListState)
									.padding(2.dp)
							) {
								Icon(
									painter = painterResource(id = R.drawable.ic_fa_grip),
									contentDescription = stringResource(id = R.string.reorder_grip),
									tint = when (bucketItemObject.state) {
										BucketItemState.ALPHA.name -> Color.LocationContainer
										BucketItemState.BETA.name -> Color.AttachmentContainer
										BucketItemState.GAMMA.name -> Color.LockClosedContainer
										else -> MaterialTheme.colorScheme.onBackground
									},
									modifier = Modifier.requiredSize(16.dp)
								)
							}
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

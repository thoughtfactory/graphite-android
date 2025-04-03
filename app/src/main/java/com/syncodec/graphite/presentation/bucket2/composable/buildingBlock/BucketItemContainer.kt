package com.syncodec.graphite.presentation.bucket2.composable.buildingBlock

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyGridState
import sh.calvin.reorderable.ReorderableLazyListState


@Composable
fun BucketItemListContainer(
    lazyListState: LazyListState,
    reorderableLazyListState: ReorderableLazyListState,
    bucketItemBoxIdListOrdered: List<Long>,
    content: @Composable ReorderableCollectionItemScope.(isDragging: Boolean, index: Int, id: Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState,
    ) {

        item(key = "top_spacer", contentType = { 0 }) { Spacer(modifier = Modifier.height(height = 8.dp)) }

        itemsIndexed(
            items = bucketItemBoxIdListOrdered,
            key = { _, bucketItemBoxId -> bucketItemBoxId },
            contentType = { _, _ -> 1 }
        ) { index, bucketItemBoxId ->
            ReorderableItem(
                state = reorderableLazyListState,
                key = bucketItemBoxId,
            ) { isDragging ->
                content(isDragging, index, bucketItemBoxId)
            }
        }

        item(key = "bottom_spacer", contentType = { 0 }) { Spacer(modifier = Modifier.height(height = 256.dp)) }
    }
}

@Composable
fun BucketItemGridContainer(
    lazyGridState: LazyGridState,
    reorderableGridState: ReorderableLazyGridState,
    bucketItemBoxIdListOrdered: List<Long>,
    content: @Composable ReorderableCollectionItemScope.(isDragging: Boolean, index: Int, id: Long) -> Unit,
) {
    LazyVerticalGrid(
        state = lazyGridState,
        columns = GridCells.Adaptive(minSize = 128.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(
            items = bucketItemBoxIdListOrdered,
            key = { _, bucketItemBoxId -> bucketItemBoxId },
            contentType = { _, _ -> 1 }
        ) { index, bucketItemBoxId ->
            ReorderableItem(
                state = reorderableGridState,
                key = bucketItemBoxId,
            ) { isDragging ->
                content(isDragging, index, bucketItemBoxId)
            }
        }
    }
}


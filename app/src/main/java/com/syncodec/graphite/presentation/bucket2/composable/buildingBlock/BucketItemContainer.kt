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
import com.syncodec.graphite.di.modelObjectBox.BucketItemBox
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyGridState
import sh.calvin.reorderable.ReorderableLazyListState


@Composable
fun BucketItemListContainer(
    lazyListState: LazyListState,
    reorderableLazyListState: ReorderableLazyListState,
    bucketItemBoxListOrdered: List<BucketItemBox>,
    content: @Composable ReorderableCollectionItemScope.(isDragging: Boolean, index: Int,  data: BucketItemBox) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState,
    ) {

        item(key = "top_spacer", contentType = { 0 }) { Spacer(modifier = Modifier.height(height = 8.dp)) }

        itemsIndexed(
            items = bucketItemBoxListOrdered,
            key = { _, bucketItemBox -> bucketItemBox.id },
            contentType = { _, _ -> 1 }
        ) { index, bucketItemBox ->
            ReorderableItem(
                state = reorderableLazyListState,
                key = bucketItemBox.id,
            ) { isDragging ->
                content(isDragging, index, bucketItemBox)
            }
        }

        item(key = "bottom_spacer", contentType = { 0 }) { Spacer(modifier = Modifier.height(height = 256.dp)) }
    }
}

@Composable
fun BucketItemGridContainer(
    lazyGridState: LazyGridState,
    reorderableGridState: ReorderableLazyGridState,
    bucketItemBoxListOrdered: List<BucketItemBox>,
    content: @Composable ReorderableCollectionItemScope.(isDragging: Boolean, index: Int,  data: BucketItemBox) -> Unit,
) {
    LazyVerticalGrid(
        state = lazyGridState,
        columns = GridCells.Adaptive(minSize = 108.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(
            items = bucketItemBoxListOrdered,
            key = { _, bucketItemBox -> bucketItemBox.id },
            contentType = { _, _ -> 1 }
        ) { index, bucketItemBox ->
            ReorderableItem(
                state = reorderableGridState,
                key = bucketItemBox.id,
            ) { isDragging ->
                content(isDragging, index, bucketItemBox)
            }
        }
    }
}


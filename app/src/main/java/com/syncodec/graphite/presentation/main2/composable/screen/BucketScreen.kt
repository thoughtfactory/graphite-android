package com.syncodec.graphite.presentation.main2.composable.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.modelObjectBox.BucketBox
import com.syncodec.graphite.presentation.common.v2.selectable2.LocalSelectionContainerActor
import com.syncodec.graphite.presentation.main2.composable.buildingBlock.bucket.BucketCard
import com.syncodec.graphite.utils.IntentUtil
import com.syncodec.graphite.utils.alice2.Alice2
import kotlinx.coroutines.flow.Flow
import org.koin.compose.koinInject


@Composable
fun BucketScreen(
    allBucketBoxListFlow: Flow<List<BucketBox>>
) {
    val scope = rememberCoroutineScope()
    val alice2: Alice2 = koinInject()
    val context = LocalContext.current

    val allBucketBoxList by allBucketBoxListFlow.collectAsState(initial = listOf())
    val lazyListState = rememberLazyListState()

    val selectionContainerActor = LocalSelectionContainerActor.current
    val isSelecting by selectionContainerActor.isSelectingFlow.collectAsState()
    val selectedItemIdList by selectionContainerActor.selectedItemIdListFlow.collectAsState()
    BackHandler(enabled = isSelecting) { selectionContainerActor.unselect() }

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Adaptive(minSize = 144.dp),
        contentPadding = PaddingValues(all = 8.dp)
    ) {
        items(
            items = allBucketBoxList,
            contentType = { 0 },
            key = { it.id },
        ) { bucketBox ->
            BucketCard(
                titleText = bucketBox.title?.decrypt(alice2),
                descriptionText = bucketBox.description?.decrypt(alice2),
                bucketType = bucketBox.bucketType?.decrypt(alice2) ?: BucketBox.BucketType.Unknown,
                bucketSize = 0,
                selected = bucketBox.id in selectedItemIdList,
                onLongClick = { selectionContainerActor.selectItem(objectBoxId = bucketBox.id) },
                onClick = {
                    if (isSelecting) selectionContainerActor.selectItem(objectBoxId = bucketBox.id)
                    else IntentUtil.launchBucketActivity(context = context, bucketId = bucketBox.id)
                }
            )
        }
    }
}

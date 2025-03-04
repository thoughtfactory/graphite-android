package com.syncodec.graphite.presentation.bucket2.composable.screen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketBox
import com.syncodec.graphite.di.modelObjectBox.BucketItemBox
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemData
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemTodo
import com.syncodec.graphite.presentation.bucket2.BucketViewModel2
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.todoCard.TodoCard
import com.syncodec.graphite.presentation.common.v2.selectable2.LocalSelectionContainerActor
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import com.syncodec.graphite.presentation.ui.ICON_SIZE
import com.syncodec.graphite.utils.DataLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState


@Composable
fun TodoScreen(
    pagerState: PagerState = rememberPagerState { 4 },
    bucketBoxDataFlow: StateFlow<DataLoader<BucketBox>> = MutableStateFlow(value = DataLoader.Init),
    bucketItemBoxGroupDataFlow: StateFlow<DataLoader<BucketViewModel2.BucketItemBoxGroup>>,
    onUpdateBucketItemBoxState: (BucketItemBox, BucketItemData.State) -> Unit = { _, _ -> },
    onUpdateBucketItemOrder: (BucketBox, List<Long>) -> Unit = { _, _ -> },
) {

    val view = LocalView.current
    val bucketItemBoxListData by bucketItemBoxGroupDataFlow.collectAsState()

    val selectionContainerActor = LocalSelectionContainerActor.current
    val selectedItemIdList by selectionContainerActor.selectedItemIdListFlow.collectAsState()
    val isSelecting by selectionContainerActor.isSelectingFlow.collectAsState()
    BackHandler(enabled = isSelecting) { selectionContainerActor.unselect() }

    AnimatedContent(
        targetState = bucketItemBoxListData::class.simpleName,
        transitionSpec = { AnimationDefaults.Fade }
    ) { bucketItemBoxListData1 ->
        when (bucketItemBoxListData1) {
            DataLoader.Init::class.simpleName -> Unit
            DataLoader.Loading::class.simpleName -> Unit
            DataLoader.NoData::class.simpleName -> Unit
            DataLoader.Error::class.simpleName -> Unit
            DataLoader.Loaded::class.simpleName -> HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 4,
                userScrollEnabled = !isSelecting,
                modifier = Modifier.fillMaxSize()
            ) { pageNumber ->

                val bucketItemBoxListData1 = bucketItemBoxListData as? DataLoader.Loaded ?: return@HorizontalPager

                val bucketItemBoxListUnOrdered = when (pageNumber) {
                    0 -> bucketItemBoxListData1.data.allOrdered
                    1 -> bucketItemBoxListData1.data.alpha
                    2 -> bucketItemBoxListData1.data.beta
                    3 -> bucketItemBoxListData1.data.gamma
                    else -> bucketItemBoxListData1.data.all
                }
                var bucketItemBoxListOrdered: List<BucketItemBox> by remember(key1 = bucketItemBoxListUnOrdered.map { it.id }.sorted()) { mutableStateOf(value = bucketItemBoxListUnOrdered) }
//                var bucketItemBoxListOrdered: List<BucketItemBox> by remember { mutableStateOf(value = bucketItemBoxListUnOrdered) }
//                LaunchedEffect(key1 = bucketItemBoxListUnOrdered.map { it.id }.sorted()) {
                LaunchedEffect(key1 = null) {
                    Log.d("npr71", "update : page : $pageNumber : ${bucketItemBoxListUnOrdered.map { it.id }.sorted()}")
                }

                val lazyListState = rememberLazyListState()
                val reorderableLazyListState = rememberReorderableLazyListState(lazyListState = lazyListState) { from, to ->
                    bucketItemBoxListOrdered = bucketItemBoxListOrdered.toMutableList().apply { add(index = to.index - 1, element = removeAt(index = from.index - 1)) }
                    ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK)
                }

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
                        val data = bucketItemBox.bucketItemData as? BucketItemTodo
                        data ?: return@itemsIndexed

                        ReorderableItem(reorderableLazyListState, key = bucketItemBox.id) { isDragging ->

                            TodoCard(
                                text = data.title,
                                state = data.state,
                                selected = bucketItemBox.id in selectedItemIdList,
                                isSelecting = isSelecting,
                                isReorderable = pageNumber == 0,
                                isLast = index == bucketItemBoxListOrdered.lastIndex,
                                onClickTriStateButton = { if (isSelecting) selectionContainerActor.selectItem(objectBoxId = bucketItemBox.id) else onUpdateBucketItemBoxState(bucketItemBox, data.nextState()) },
                                onClick = { if (isSelecting) selectionContainerActor.selectItem(objectBoxId = bucketItemBox.id) },
                                onLongClick = { selectionContainerActor.selectItem(objectBoxId = bucketItemBox.id) },
                                dragHandle = {
                                    IconButton(
                                        modifier = Modifier.draggableHandle(
                                            onDragStarted = { ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.GESTURE_START) },
                                            onDragStopped = {
                                                ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.GESTURE_END)
                                                val bucketBox = (bucketBoxDataFlow.value as? DataLoader.Loaded)?.data
                                                bucketBox ?: return@draggableHandle
                                                onUpdateBucketItemOrder(bucketBox, bucketItemBoxListOrdered.map { it.id })
                                            },
                                        ),
                                        onClick = {},
                                    ) {
                                        Icon(painter = painterResource(id = R.drawable.ic_fa_drag_handle), contentDescription = "Reorder", modifier = Modifier.requiredSize(size = ICON_SIZE))
                                    }
                                }
                            )
                        }
                    }

                    item(key = "bottom_spacer", contentType = { 0 }) { Spacer(modifier = Modifier.height(height = 256.dp)) }

                }
            }
        }
    }
}

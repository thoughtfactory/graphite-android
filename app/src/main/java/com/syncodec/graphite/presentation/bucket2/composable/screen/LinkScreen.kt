package com.syncodec.graphite.presentation.bucket2.composable.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import com.syncodec.graphite.di.modelObjectBox.BucketBox
import com.syncodec.graphite.di.modelObjectBox.BucketItemBox
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemBook
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemData
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemLink
import com.syncodec.graphite.presentation.bucket2.BucketViewModel2
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.BucketItemGridContainer
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.DragHandle
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.linkCard.LinkGridCard
import com.syncodec.graphite.presentation.common.v2.selectable2.LocalSelectionContainerActor
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import com.syncodec.graphite.utils.DataLoader
import com.syncodec.graphite.utils.alice2.Alice2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import sh.calvin.reorderable.rememberReorderableLazyGridState
import sh.calvin.reorderable.rememberReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyStaggeredGridState


@Composable
fun LinkScreen(
    pagerState: PagerState = rememberPagerState { 4 },
    bucketBoxDataFlow: StateFlow<DataLoader<BucketBox>> = MutableStateFlow(value = DataLoader.Init),
    bucketItemBoxGroupDataFlow: StateFlow<DataLoader<BucketViewModel2.BucketItemBoxGroup>>,
    onUpdateBucketItemBoxState: (BucketItemBox, BucketItemData.State) -> Unit = { _, _ -> },
    onUpdateBucketItemOrder: (BucketBox, List<Long>) -> Unit = { _, _ -> },
    onClickBucketItem: (BucketItemBox) -> Unit = {},
) {
    val alice2: Alice2 = koinInject()

    val view = LocalView.current
    val bucketItemBoxListData by bucketItemBoxGroupDataFlow.collectAsState()

    val selectionContainerActor = LocalSelectionContainerActor.current
    val isSelecting by selectionContainerActor.isSelectingFlow.collectAsState()
    BackHandler(enabled = isSelecting) { selectionContainerActor.unselect() }

    AnimatedContent(
        targetState = bucketItemBoxListData::class.simpleName,
        transitionSpec = { AnimationDefaults.Fade }
    ) { bucketItemBoxListDataString1 ->
        when (bucketItemBoxListDataString1) {
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
                var bucketItemBoxListOrdered: List<BucketItemBox> by remember(key1 = Unit) { mutableStateOf(value = bucketItemBoxListUnOrdered) }
                LaunchedEffect(key1 = bucketItemBoxListUnOrdered) { if (bucketItemBoxListOrdered.map { it.id } == bucketItemBoxListUnOrdered.map { it.id }) bucketItemBoxListOrdered = bucketItemBoxListUnOrdered }
                val lazyListState = rememberLazyListState()
                val lazyGridState = rememberLazyGridState()

                val reorderableLazyListState = rememberReorderableLazyListState(lazyListState = lazyListState) { from, to ->
                    bucketItemBoxListOrdered = bucketItemBoxListOrdered.toMutableList().apply { add(index = to.index - 1, element = removeAt(index = from.index - 1)) }
                    ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK)
                }
                val reorderableGridState = rememberReorderableLazyGridState(lazyGridState = lazyGridState) { from, to ->
                    bucketItemBoxListOrdered = bucketItemBoxListOrdered.toMutableList().apply { add(index = to.index, element = removeAt(index = from.index)) }
                    ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK)
                }

//                BucketItemListContainer(
//                    lazyListState = lazyListState,
//                    bucketItemBoxListOrdered = bucketItemBoxListOrdered,
//                    reorderableLazyListState = reorderableLazyListState,
//                ) { isDragging, index, bucketItemBox ->
//
//                    val data = bucketItemBox.bucketItemData as? BucketItemData.Link ?: return@BucketItemListContainer
//
//                    LinkListCard(
//                        bucketItemBox = bucketItemBox,
//                        linkData = data.linkData,
//                        state = data.state,
//                        isReorderable = pageNumber == 0,
//                        isLast = index == bucketItemBoxListOrdered.lastIndex,
//                        onClickTriStateButton = { if (isSelecting) selectionContainerActor.selectItem(objectBoxId = bucketItemBox.id) else onUpdateBucketItemBoxState(bucketItemBox, data.nextState()) },
//                        onClick = { if (isSelecting) selectionContainerActor.selectItem(objectBoxId = bucketItemBox.id) },
//                        onLongClick = { selectionContainerActor.selectItem(objectBoxId = bucketItemBox.id) },
//                        dragHandle = {
//                            DragHandle {
//                                val bucketBox = (bucketBoxDataFlow.value as? DataLoader.Loaded)?.data ?: return@DragHandle
//                                onUpdateBucketItemOrder(bucketBox, bucketItemBoxListOrdered.map { it.id })
//                            }
//                        }
//                    )
//                }

//                BucketItemGridContainer(
//                    lazyGridState = lazyGridState,
//                    bucketItemBoxListOrdered = bucketItemBoxListOrdered,
//                    reorderableGridState = reorderableGridState,
//                ) { isDragging, index, bucketItemBox ->
//
//                    var bucketItemLink: BucketItemLink? by remember { mutableStateOf(null) }
//                    LaunchedEffect(key1 = bucketItemBox.bucketItemData) { withContext(Dispatchers.Default) { bucketItemLink = bucketItemBox.bucketItemData?.decrypt(alice2 = alice2) as? BucketItemLink } }
//
//                    bucketItemLink?.let { bucketItemLink1 ->
//                        LinkGridCard(
//                            bucketItemBox = bucketItemBox,
//                            linkData = bucketItemLink1.linkData,
//                            state = bucketItemLink1.state,
//                            isReorderable = pageNumber == 0,
//                            isLast = index == bucketItemBoxListOrdered.lastIndex,
//                            onClickTriStateButton = { if (isSelecting) selectionContainerActor.selectItem(objectBoxId = bucketItemBox.id) else onUpdateBucketItemBoxState(bucketItemBox, bucketItemLink1.nextState()) },
//                            onClick = { if (isSelecting) selectionContainerActor.selectItem(objectBoxId = bucketItemBox.id) else onClickBucketItem(bucketItemBox) },
//                            onLongClick = { selectionContainerActor.selectItem(objectBoxId = bucketItemBox.id) },
//                            dragHandle = {
//                                DragHandle {
//                                    val bucketBox = (bucketBoxDataFlow.value as? DataLoader.Loaded)?.data ?: return@DragHandle
//                                    onUpdateBucketItemOrder(bucketBox, bucketItemBoxListOrdered.map { it.id })
//                                }
//                            }
//                        )
//                    }
//                }
            }
        }
    }
}

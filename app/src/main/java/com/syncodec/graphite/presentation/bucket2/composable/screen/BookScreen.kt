package com.syncodec.graphite.presentation.bucket2.composable.screen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketBox
import com.syncodec.graphite.di.modelObjectBox.BucketItemBox
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxPlain
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemBook
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemData
import com.syncodec.graphite.presentation.bucket2.BucketViewModel2
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.BucketItemGridContainer
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.DragHandle
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.bookCard.BookGridCard
import com.syncodec.graphite.presentation.common.v2.selectable2.LocalSelectionContainerActor
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import com.syncodec.graphite.utils.DataLoader
import com.syncodec.graphite.utils.alice2.Alice2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import sh.calvin.reorderable.ReorderableLazyGridState
import sh.calvin.reorderable.rememberReorderableLazyGridState
import sh.calvin.reorderable.rememberReorderableLazyListState


@Composable
fun BookScreen(
    pagerState: PagerState = rememberPagerState { 4 },
    bucketBoxDataFlow: StateFlow<DataLoader<BucketBox>> = MutableStateFlow(value = DataLoader.Init),
    bucketItemBoxGroupDataFlow: StateFlow<DataLoader<BucketViewModel2.BucketItemBoxGroup>>,
    bucketItemBoxOrderedDataFlow: StateFlow<DataLoader<BucketViewModel2.BucketItemBoxOrderedData>>,
    onUpdateBucketItemBoxState: (BucketItemBoxPlain, BucketItemData.State) -> Unit = { _, _ -> },
    onUpdateBucketItemOrder: (BucketBox, List<Long>) -> Unit = { _, _ -> },
    onClickBucketItem: (BucketItemBoxPlain) -> Unit = {},
) {

    val view = LocalView.current
//    val bucketItemBoxListData by bucketItemBoxGroupDataFlow.collectAsState()
    val bucketItemBoxOrderedData by bucketItemBoxOrderedDataFlow.collectAsState()

    val selectionContainerActor = LocalSelectionContainerActor.current
    val isSelecting by selectionContainerActor.isSelectingFlow.collectAsState()
    BackHandler(enabled = isSelecting) { selectionContainerActor.unselect() }

    val isGrid by remember { mutableStateOf(true) }

    AnimatedContent(
        targetState = bucketItemBoxOrderedData::class.simpleName,
        transitionSpec = { AnimationDefaults.Fade }
    ) { bucketItemBoxOrderedDataString1 ->
        when (bucketItemBoxOrderedDataString1) {
            DataLoader.Init::class.simpleName -> Unit
            DataLoader.Loading::class.simpleName -> Unit
            DataLoader.NoData::class.simpleName -> NoDataView()
            DataLoader.Error::class.simpleName -> Unit
            DataLoader.Loaded::class.simpleName -> HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 4,
                userScrollEnabled = !isSelecting,
                modifier = Modifier.fillMaxSize()
            ) { pageNumber ->

                val bucketItemBoxOrdered = (bucketItemBoxOrderedData as? DataLoader.Loaded)?.data ?: return@HorizontalPager

                if (pageNumber == 0) AllDataView(
                    bucketItemBoxOrdered = bucketItemBoxOrdered,
                    bucketBoxDataFlow = bucketBoxDataFlow,
                    onUpdateBucketItemOrder = onUpdateBucketItemOrder,
                    onUpdateBucketItemBoxState = onUpdateBucketItemBoxState,
                    onClickBucketItem = onClickBucketItem,
                )
                else FilteredDataView(
                    bucketItemBoxOrdered = bucketItemBoxOrdered,
                    bucketBoxDataFlow = bucketBoxDataFlow,
                    onUpdateBucketItemOrder = onUpdateBucketItemOrder,
                    onUpdateBucketItemBoxState = onUpdateBucketItemBoxState,
                    onClickBucketItem = onClickBucketItem,
                )

//                var bucketItemBoxListOrdered: List<BucketItemBox> by remember(key1 = Unit) { mutableStateOf(value = bucketItemBoxListUnOrdered) }
//
////                *
//                LaunchedEffect(key1 = bucketItemBoxListUnOrdered) { if (bucketItemBoxListOrdered.map { it.id } == bucketItemBoxListUnOrdered.map { it.id }) bucketItemBoxListOrdered = bucketItemBoxListUnOrdered }
//
//                val lazyListState = rememberLazyListState()
//                val lazyGridState = rememberLazyGridState()
//
//                val reorderableLazyListState = rememberReorderableLazyListState(lazyListState = lazyListState) { from, to ->
//                    bucketItemBoxListOrdered = bucketItemBoxListOrdered.toMutableList().apply { add(index = to.index - 1, element = removeAt(index = from.index - 1)) }
//                    ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK)
//                }
//                val reorderableGridState = rememberReorderableLazyGridState(lazyGridState = lazyGridState) { from, to ->
//                    bucketItemBoxListOrdered = bucketItemBoxListOrdered.toMutableList().apply { add(index = to.index, element = removeAt(index = from.index)) }
//                    ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK)
//                }
//
//
//                if (isGrid) GridView(
//                    lazyGridState = lazyGridState,
//                    bucketBoxDataFlow = bucketBoxDataFlow,
//                    bucketItemBoxListOrdered = bucketItemBoxListOrdered,
//                    reorderableGridState = reorderableGridState,
//                    pageNumber = pageNumber,
//                    onUpdateBucketItemOrder = onUpdateBucketItemOrder,
//                    onUpdateBucketItemBoxState = onUpdateBucketItemBoxState,
//                    onClickBucketItem = onClickBucketItem,
//                )
            }
        }
    }


//    AnimatedContent(
//        targetState = bucketItemBoxListData::class.simpleName,
//        transitionSpec = { AnimationDefaults.Fade }
//    ) { bucketItemBoxListDataString1 ->
//        when (bucketItemBoxListDataString1) {
//            DataLoader.Init::class.simpleName -> Unit
//            DataLoader.Loading::class.simpleName -> Unit
//            DataLoader.NoData::class.simpleName -> NoDataView()
//            DataLoader.Error::class.simpleName -> Unit
//            DataLoader.Loaded::class.simpleName -> HorizontalPager(
//                state = pagerState,
//                beyondViewportPageCount = 4,
//                userScrollEnabled = !isSelecting,
//                modifier = Modifier.fillMaxSize()
//            ) { pageNumber ->
//
//                val bucketItemBoxListData1 = bucketItemBoxListData as? DataLoader.Loaded ?: return@HorizontalPager
//
//                val bucketItemBoxListUnOrdered = when (pageNumber) {
//                    0 -> bucketItemBoxListData1.data.allOrdered
//                    1 -> bucketItemBoxListData1.data.alpha
//                    2 -> bucketItemBoxListData1.data.beta
//                    3 -> bucketItemBoxListData1.data.gamma
//                    else -> bucketItemBoxListData1.data.all
//                }
//                var bucketItemBoxListOrdered: List<BucketItemBox> by remember(key1 = Unit) { mutableStateOf(value = bucketItemBoxListUnOrdered) }
//
////                *
//                LaunchedEffect(key1 = bucketItemBoxListUnOrdered) { if (bucketItemBoxListOrdered.map { it.id } == bucketItemBoxListUnOrdered.map { it.id }) bucketItemBoxListOrdered = bucketItemBoxListUnOrdered }
//
//                val lazyListState = rememberLazyListState()
//                val lazyGridState = rememberLazyGridState()
//
//                val reorderableLazyListState = rememberReorderableLazyListState(lazyListState = lazyListState) { from, to ->
//                    bucketItemBoxListOrdered = bucketItemBoxListOrdered.toMutableList().apply { add(index = to.index - 1, element = removeAt(index = from.index - 1)) }
//                    ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK)
//                }
//                val reorderableGridState = rememberReorderableLazyGridState(lazyGridState = lazyGridState) { from, to ->
//                    bucketItemBoxListOrdered = bucketItemBoxListOrdered.toMutableList().apply { add(index = to.index, element = removeAt(index = from.index)) }
//                    ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK)
//                }
//
////                BucketItemListContainer(
////                    lazyListState = lazyListState,
////                    bucketItemBoxListOrdered = bucketItemBoxListOrdered,
////                    reorderableLazyListState = reorderableLazyListState,
////                ) { isDragging, index, bucketItemBox ->
////
////                    val data = bucketItemBox.bucketItemData as? BucketItemData.Link ?: return@BucketItemListContainer
////
////                    LinkListCard(
////                        bucketItemBox = bucketItemBox,
////                        linkData = data.linkData,
////                        state = data.state,
////                        isReorderable = pageNumber == 0,
////                        isLast = index == bucketItemBoxListOrdered.lastIndex,
////                        onClickTriStateButton = { if (isSelecting) selectionContainerActor.selectItem(objectBoxId = bucketItemBox.id) else onUpdateBucketItemBoxState(bucketItemBox, data.nextState()) },
////                        onClick = { if (isSelecting) selectionContainerActor.selectItem(objectBoxId = bucketItemBox.id) },
////                        onLongClick = { selectionContainerActor.selectItem(objectBoxId = bucketItemBox.id) },
////                        dragHandle = {
////                            DragHandle {
////                                val bucketBox = (bucketBoxDataFlow.value as? DataLoader.Loaded)?.data ?: return@DragHandle
////                                onUpdateBucketItemOrder(bucketBox, bucketItemBoxListOrdered.map { it.id })
////                            }
////                        }
////                    )
////                }
//
//                if (isGrid) GridView(
//                    lazyGridState = lazyGridState,
//                    bucketBoxDataFlow = bucketBoxDataFlow,
//                    bucketItemBoxListOrdered = bucketItemBoxListOrdered,
//                    reorderableGridState = reorderableGridState,
//                    pageNumber = pageNumber,
//                    onUpdateBucketItemOrder = onUpdateBucketItemOrder,
//                    onUpdateBucketItemBoxState = onUpdateBucketItemBoxState,
//                    onClickBucketItem = onClickBucketItem,
//                )
//            }
//        }
//    }
}

@Composable
private fun AllDataView(
    bucketItemBoxOrdered: BucketViewModel2.BucketItemBoxOrderedData,
    bucketBoxDataFlow: StateFlow<DataLoader<BucketBox>> = MutableStateFlow(value = DataLoader.Init),
    onUpdateBucketItemOrder: (BucketBox, List<Long>) -> Unit = { _, _ -> },
    onUpdateBucketItemBoxState: (BucketItemBoxPlain, BucketItemData.State) -> Unit = { _, _ -> },
    onClickBucketItem: (BucketItemBoxPlain) -> Unit = {},
) {
    val view = LocalView.current
    val isGrid by remember { mutableStateOf(true) }

    var bucketItemBoxList: List<BucketItemBoxPlain> by remember(key1 = Unit) { mutableStateOf(value = listOf()) }
    LaunchedEffect(key1 = Unit) { withContext(Dispatchers.Default) { bucketItemBoxList = bucketItemBoxOrdered.orderedIdList.mapNotNull { bucketItemBoxOrdered.bucketItemBoxMap[it] } } }

    val lazyListState = rememberLazyListState()
    val lazyGridState = rememberLazyGridState()

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState = lazyListState) { from, to ->
        bucketItemBoxList = bucketItemBoxList.toMutableList().apply { add(index = to.index - 1, element = removeAt(index = from.index - 1)) }
        ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK)
    }
    val reorderableGridState = rememberReorderableLazyGridState(lazyGridState = lazyGridState) { from, to ->
        bucketItemBoxList = bucketItemBoxList.toMutableList().apply { add(index = to.index, element = removeAt(index = from.index)) }
        ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK)
    }


    if (isGrid) GridView(
        lazyGridState = lazyGridState,
        bucketBoxDataFlow = bucketBoxDataFlow,
        bucketItemBoxList = bucketItemBoxList,
        reorderableGridState = reorderableGridState,
        pageNumber = 0,
        onUpdateBucketItemOrder = onUpdateBucketItemOrder,
        onUpdateBucketItemBoxState = onUpdateBucketItemBoxState,
        onClickBucketItem = onClickBucketItem,
    )
}

@Composable
private fun FilteredDataView(
    bucketItemBoxOrdered: BucketViewModel2.BucketItemBoxOrderedData,
    bucketBoxDataFlow: StateFlow<DataLoader<BucketBox>> = MutableStateFlow(value = DataLoader.Init),
    onUpdateBucketItemOrder: (BucketBox, List<Long>) -> Unit = { _, _ -> },
    onUpdateBucketItemBoxState: (BucketItemBoxPlain, BucketItemData.State) -> Unit = { _, _ -> },
    onClickBucketItem: (BucketItemBoxPlain) -> Unit = {},
) {
    val view = LocalView.current
    val isGrid by remember { mutableStateOf(true) }

    var bucketItemBoxList = bucketItemBoxOrdered.orderedIdList.mapNotNull { bucketItemBoxOrdered.bucketItemBoxMap[it] }

    val lazyListState = rememberLazyListState()
    val lazyGridState = rememberLazyGridState()

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState = lazyListState) { from, to ->
        bucketItemBoxList = bucketItemBoxList.toMutableList().apply { add(index = to.index - 1, element = removeAt(index = from.index - 1)) }
        ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK)
    }
    val reorderableGridState = rememberReorderableLazyGridState(lazyGridState = lazyGridState) { from, to ->
        bucketItemBoxList = bucketItemBoxList.toMutableList().apply { add(index = to.index, element = removeAt(index = from.index)) }
        ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK)
    }


    if (isGrid) GridView(
        lazyGridState = lazyGridState,
        bucketBoxDataFlow = bucketBoxDataFlow,
        bucketItemBoxList = bucketItemBoxList,
        reorderableGridState = reorderableGridState,
        pageNumber = 1,
        onUpdateBucketItemOrder = onUpdateBucketItemOrder,
        onUpdateBucketItemBoxState = onUpdateBucketItemBoxState,
        onClickBucketItem = onClickBucketItem,
    )
}

@Composable
private fun GridView(
    lazyGridState: LazyGridState,
    bucketBoxDataFlow: StateFlow<DataLoader<BucketBox>> = MutableStateFlow(value = DataLoader.Init),
    bucketItemBoxList: List<BucketItemBoxPlain>,
    reorderableGridState: ReorderableLazyGridState,
    pageNumber: Int,
    onUpdateBucketItemOrder: (BucketBox, List<Long>) -> Unit = { _, _ -> },
    onUpdateBucketItemBoxState: (BucketItemBoxPlain, BucketItemData.State) -> Unit = { _, _ -> },
    onClickBucketItem: (BucketItemBoxPlain) -> Unit = {},
) {
    val alice2: Alice2 = koinInject()

    val selectionContainerActor = LocalSelectionContainerActor.current
    val isSelecting by selectionContainerActor.isSelectingFlow.collectAsState()

    BucketItemGridContainer(
        lazyGridState = lazyGridState,
        bucketItemBoxListOrdered = bucketItemBoxList,
        reorderableGridState = reorderableGridState,
    ) { isDragging, index, bucketItemBox ->

        val bucketItemBook by remember(key1 = bucketItemBox.bucketItemData) { derivedStateOf { bucketItemBox.bucketItemData as? BucketItemBook } }

        BookGridCard(
            bucketItemBox = bucketItemBox,
            bucketItemBook = bucketItemBox.bucketItemData as? BucketItemBook,
            state = bucketItemBook?.state ?: BucketItemData.State.Alpha,
            isReorderable = pageNumber == 0,
            isLast = index == bucketItemBoxList.lastIndex,
            onClickTriStateButton = { if (isSelecting) selectionContainerActor.selectItem(objectBoxId = bucketItemBox.id) else onUpdateBucketItemBoxState(bucketItemBox, bucketItemBook?.nextState() ?: BucketItemData.State.Alpha) },
            onClick = { if (isSelecting) selectionContainerActor.selectItem(objectBoxId = bucketItemBox.id) else onClickBucketItem(bucketItemBox) },
            onLongClick = { selectionContainerActor.selectItem(objectBoxId = bucketItemBox.id) },
            dragHandle = {
                DragHandle {
                    val bucketBox = (bucketBoxDataFlow.value as? DataLoader.Loaded)?.data ?: return@DragHandle
                    onUpdateBucketItemOrder(bucketBox, bucketItemBoxList.map { it.id })
                }
            }
        )
    }
}

@Composable
private fun NoDataView() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.il_mj_book),
            contentDescription = null,
            modifier = Modifier.requiredSize(360.dp)
        )
    }
}

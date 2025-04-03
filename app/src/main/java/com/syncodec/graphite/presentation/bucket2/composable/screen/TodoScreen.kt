package com.syncodec.graphite.presentation.bucket2.composable.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import com.syncodec.graphite.di.modelObjectBox.BucketBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemTodo
import com.syncodec.graphite.presentation.bucket2.BucketViewModel2
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.BucketItemGridContainer
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.BucketItemListContainer
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.DragHandle
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.todoCard.TodoGridCard
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.todoCard.TodoListCard
import com.syncodec.graphite.presentation.common.v2.selectable2.LocalSelectionContainerActor
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import com.syncodec.graphite.utils.DataLoader
import com.syncodec.graphite.utils.SharedPref
import com.syncodec.graphite.utils.alice2.Alice2
import ir.amirreza.composepreferences.state.rememberPreferenceStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.compose.koinInject
import sh.calvin.reorderable.rememberReorderableLazyGridState
import sh.calvin.reorderable.rememberReorderableLazyListState


@Composable
fun TodoScreen(
    pagerState: PagerState = rememberPagerState { 4 },
    bucketBoxDataFlow: StateFlow<DataLoader<BucketBoxDecrypted>> = MutableStateFlow(value = DataLoader.Init()),
    bucketItemBoxOrderedDataFlow: StateFlow<DataLoader<BucketViewModel2.BucketItemBoxOrderedData>>,
    onUpdateBucketItemOrder: (BucketBoxDecrypted, List<Long>) -> Unit = { _, _ -> },
    onUpdateBucketItemBoxState: (BucketItemBoxDecrypted, BucketItemBoxDecrypted.State) -> Unit = { _, _ -> },
    onClickBucketItem: (BucketItemBoxDecrypted) -> Unit = {},
) {

    val bucketItemBoxOrderedData by bucketItemBoxOrderedDataFlow.collectAsState()

    val selectionContainerActor = LocalSelectionContainerActor.current
    val isSelecting by selectionContainerActor.isSelectingFlow.collectAsState()
    BackHandler(enabled = isSelecting) { selectionContainerActor.unselect() }

    var viewType by rememberPreferenceStateOf(key = SharedPref.Key.ViewType.name, defaultValue = SharedPref.ViewType.List.name)

    AnimatedContent(
        targetState = bucketItemBoxOrderedData::class.simpleName,
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

                val bucketItemBoxOrdered = (bucketItemBoxOrderedData as? DataLoader.Loaded)?.data ?: return@HorizontalPager

                DataListView(
                    bucketItemBoxOrdered = bucketItemBoxOrdered,
                    bucketBoxDataFlow = bucketBoxDataFlow,
                    pageNumber = pageNumber,
                    onUpdateBucketItemBoxState = onUpdateBucketItemBoxState,
                    onUpdateBucketItemOrder = onUpdateBucketItemOrder,
                    onClickBucketItem = onClickBucketItem,
                )
            }
        }
    }
}

@Composable
private fun DataGridView(
    bucketItemBoxOrdered: BucketViewModel2.BucketItemBoxOrderedData,
    bucketBoxDataFlow: StateFlow<DataLoader<BucketBoxDecrypted>> = MutableStateFlow(value = DataLoader.Init()),
    pageNumber: Int = 0,
    onUpdateBucketItemBoxState: (BucketItemBoxDecrypted, BucketItemBoxDecrypted.State) -> Unit = { _, _ -> },
    onUpdateBucketItemOrder: (BucketBoxDecrypted, List<Long>) -> Unit = { _, _ -> },
    onClickBucketItem: (BucketItemBoxDecrypted) -> Unit = {},
) {
    val view = LocalView.current
    val alice2: Alice2 = koinInject()

    val selectionContainerActor = LocalSelectionContainerActor.current
    val isSelecting by selectionContainerActor.isSelectingFlow.collectAsState()

    val idList = when (pageNumber) {
        0 -> bucketItemBoxOrdered.allOrderedIdList
        1 -> bucketItemBoxOrdered.alphaOrderedIdList
        2 -> bucketItemBoxOrdered.betaOrderedIdList
        3 -> bucketItemBoxOrdered.gammaOrderedIdList
        else -> bucketItemBoxOrdered.allOrderedIdList
    }

    var orderedIdList by remember { mutableStateOf(value = idList) }
    LaunchedEffect(key1 = idList.size, key2 = if (pageNumber == 0) orderedIdList.toSet() != idList.toSet() else idList) { orderedIdList = idList }

    val lazyGridState = rememberLazyGridState()

    val reorderableGridState = rememberReorderableLazyGridState(lazyGridState = lazyGridState) { from, to ->
        orderedIdList = orderedIdList.toMutableList().apply { add(index = to.index, element = removeAt(index = from.index)) }
        ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK)
    }

    BucketItemGridContainer(
        lazyGridState = lazyGridState,
        bucketItemBoxIdListOrdered = orderedIdList,
        reorderableGridState = reorderableGridState,
    ) { isDragging, index, bucketItemBoxId ->

        val bucketItemBox = bucketItemBoxOrdered.bucketItemBoxMap[bucketItemBoxId]?.decryptBlocking(alice2)
        val bucketItemTodo by remember(key1 = bucketItemBox?.bucketItemData) { derivedStateOf { bucketItemBox?.bucketItemData as? BucketItemTodo } }

        TodoGridCard(
            bucketItemBox = bucketItemBox,
            bucketItemTodo = bucketItemTodo,
            state = bucketItemBox?.state ?: BucketItemBoxDecrypted.State.Alpha,
            isReorderable = pageNumber == 0,
            isLast = index == orderedIdList.lastIndex,
            onClickTriStateButton = { bucketItemBox ?: return@TodoGridCard; if (isSelecting) selectionContainerActor.selectItem(objectBoxId = bucketItemBox.id, allItemIdList = orderedIdList) else onUpdateBucketItemBoxState(bucketItemBox, bucketItemBox.nextState()) },
            onClick = { bucketItemBox ?: return@TodoGridCard; if (isSelecting) selectionContainerActor.selectItem(objectBoxId = bucketItemBox.id, allItemIdList = orderedIdList) else onClickBucketItem(bucketItemBox) },
            onLongClick = { bucketItemBox ?: return@TodoGridCard; selectionContainerActor.selectItem(objectBoxId = bucketItemBox.id, allItemIdList = orderedIdList) },
            dragHandle = {
                DragHandle {
                    val bucketBox = (bucketBoxDataFlow.value as? DataLoader.Loaded)?.data ?: return@DragHandle
                    onUpdateBucketItemOrder(bucketBox, orderedIdList)
                }
            }
        )
    }
}

@Composable
private fun DataListView(
    bucketItemBoxOrdered: BucketViewModel2.BucketItemBoxOrderedData,
    bucketBoxDataFlow: StateFlow<DataLoader<BucketBoxDecrypted>> = MutableStateFlow(value = DataLoader.Init()),
    pageNumber: Int = 0,
    onUpdateBucketItemBoxState: (BucketItemBoxDecrypted, BucketItemBoxDecrypted.State) -> Unit = { _, _ -> },
    onUpdateBucketItemOrder: (BucketBoxDecrypted, List<Long>) -> Unit = { _, _ -> },
    onClickBucketItem: (BucketItemBoxDecrypted) -> Unit = {},
) {
    val view = LocalView.current
    val alice2: Alice2 = koinInject()

    val selectionContainerActor = LocalSelectionContainerActor.current
    val isSelecting by selectionContainerActor.isSelectingFlow.collectAsState()

    val idList = when (pageNumber) {
        0 -> bucketItemBoxOrdered.allOrderedIdList
        1 -> bucketItemBoxOrdered.alphaOrderedIdList
        2 -> bucketItemBoxOrdered.betaOrderedIdList
        3 -> bucketItemBoxOrdered.gammaOrderedIdList
        else -> bucketItemBoxOrdered.allOrderedIdList
    }

    var orderedIdList by remember { mutableStateOf(value = idList) }
    LaunchedEffect(key1 = idList.size, key2 = if (pageNumber == 0) orderedIdList.toSet() != idList.toSet() else idList) { orderedIdList = idList }

    val lazyListState = rememberLazyListState()

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState = lazyListState) { from, to ->
        orderedIdList = orderedIdList.toMutableList().apply { add(index = to.index - 1, element = removeAt(index = from.index - 1)) }
        ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK)
    }

    BucketItemListContainer(
        lazyListState = lazyListState,
        bucketItemBoxIdListOrdered = orderedIdList,
        reorderableLazyListState = reorderableLazyListState,
    ) { isDragging, index, bucketItemBoxId ->

        val bucketItemBox = bucketItemBoxOrdered.bucketItemBoxMap[bucketItemBoxId]?.decryptBlocking(alice2)
        val bucketItemTodo by remember(key1 = bucketItemBox?.bucketItemData) { derivedStateOf { bucketItemBox?.bucketItemData as? BucketItemTodo } }

        TodoListCard(
            bucketItemBox = bucketItemBox,
            bucketItemTodo = bucketItemTodo,
            state = bucketItemBox?.state ?: BucketItemBoxDecrypted.State.Alpha,
            isReorderable = pageNumber == 0,
            isLast = index == orderedIdList.lastIndex,
            onClickTriStateButton = { bucketItemBox ?: return@TodoListCard; if (isSelecting) selectionContainerActor.selectItem(objectBoxId = bucketItemBox.id, allItemIdList = orderedIdList) else onUpdateBucketItemBoxState(bucketItemBox, bucketItemBox.nextState()) },
            onClick = { bucketItemBox ?: return@TodoListCard; if (isSelecting) selectionContainerActor.selectItem(objectBoxId = bucketItemBox.id, allItemIdList = orderedIdList) else onClickBucketItem(bucketItemBox) },
            onLongClick = { bucketItemBox ?: return@TodoListCard; selectionContainerActor.selectItem(objectBoxId = bucketItemBox.id, allItemIdList = orderedIdList) },
            dragHandle = {
                DragHandle {
                    val bucketBox = (bucketBoxDataFlow.value as? DataLoader.Loaded)?.data ?: return@DragHandle
                    onUpdateBucketItemOrder(bucketBox, orderedIdList)
                }
            }
        )
    }
}

package com.syncodec.graphite.presentation.bucket2.composable.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketBox
import com.syncodec.graphite.di.modelObjectBox.BucketItemBox
import com.syncodec.graphite.di.network.openLibrary.OLBookSearchResult
import com.syncodec.graphite.di.network.trakt.TraktShowSearchResult
import com.syncodec.graphite.presentation.bucket2.BucketViewModel2
import com.syncodec.graphite.presentation.bucket2.composable.bar.BottomBar
import com.syncodec.graphite.presentation.bucket2.composable.bar.TopBar
import com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.AddBookBottomSheet
import com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.AddLinkBottomSheet
import com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.AddShowBottomSheet
import com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.AddTodoBottomSheet
import com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.EditBottomSheet
import com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.LinkBottomSheet
import com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.MetadataBottomSheet
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.BucketFloatingButton
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.v2.scaffold2.GenericScaffold2
import com.syncodec.graphite.presentation.common.v2.selectable2.LocalSelectionContainerActor
import com.syncodec.graphite.presentation.common.v2.selectable2.SelectionActionCard
import com.syncodec.graphite.presentation.common.v2.selectable2.SelectionContainerActor
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import com.syncodec.graphite.presentation.ui.ICON_SIZE
import com.syncodec.graphite.utils.DataLoader
import com.syncodec.graphite.utils.IntentUtil
import com.syncodec.graphite.utils.IntentUtil.BucketItemActivityData
import com.syncodec.graphite.utils.alice2.Alice2
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BucketScreen(
    viewModel: BucketViewModel2 = koinViewModel()
) {
    val context = LocalContext.current
    val alice2: Alice2 = koinInject()
    val scope = rememberCoroutineScope()

    val bucketBoxData by viewModel.bucketBoxDataFlow.collectAsState()
    val bucketItemBoxGroupDataFlow = viewModel.bucketItemBoxGroupDataFlow
    val bucketItemBoxOrderedDataFlow = viewModel.bucketItemBoxOrderedDataFlow

    val selectionContainerActor = remember { SelectionContainerActor() }
    val isSelecting by selectionContainerActor.isSelectingFlow.collectAsState()

    val hazeState = remember { HazeState() }

    val pagerState = rememberPagerState { 4 }

    val metadataBottomSheetState = GenericBottomSheet2State.rememberGenericBottomSheet2State()
    val editBottomSheetState = GenericBottomSheet2State.rememberGenericBottomSheet2State()

    val addTodoBottomSheetState = GenericBottomSheet2State.rememberGenericBottomSheet2State(skipPartiallyExpanded = true)
    val addBookBottomSheetState = GenericBottomSheet2State.rememberGenericBottomSheet2State(skipPartiallyExpanded = true)
    val addShowBottomSheetState = GenericBottomSheet2State.rememberGenericBottomSheet2State(skipPartiallyExpanded = true)
    val addLinkBottomSheetState = GenericBottomSheet2State.rememberGenericBottomSheet2State(skipPartiallyExpanded = true)
    val addLocationBottomSheetState = GenericBottomSheet2State.rememberGenericBottomSheet2State(skipPartiallyExpanded = true)

    val viewLinkBottomSheet = GenericBottomSheet2State.rememberGenericBottomSheet2StateT<BucketItemBox>(skipPartiallyExpanded = true)

    val bucketBox by remember(key1 = bucketBoxData) { derivedStateOf { (bucketBoxData as? DataLoader.Loaded)?.data } }
    val bucketBoxId by remember(key1 = bucketBox) { derivedStateOf { bucketBox?.id } }
    val title by remember(key1 = bucketBox) { derivedStateOf { bucketBox?.title?.decrypt(alice2) } }
    val bucketType by remember(key1 = bucketBox) { derivedStateOf { bucketBox?.bucketType?.decrypt(alice2) } }


    GenericScaffold2(
        topBar = {
            TopBar(
                title = title,
                bucketType = bucketType ?: BucketBox.BucketType.Unknown,
                stateFilterInt = pagerState.currentPage,
                isLocked = bucketBox?.isLocked?.decrypt(alice2, false) == true,
                isFavourite = bucketBox?.isFavourite?.decrypt(alice2, false) == true,
                onUpdateFilterInt = { scope.launch { pagerState.animateScrollToPage(page = it) } },
                onClickLock = viewModel::onToggleLock,
                onClickFavourite = viewModel::onToggleFavourite
            )
        },
        bottomBar = {
            BottomBar(
                isSelecting = isSelecting,
                onClickMetadata = { metadataBottomSheetState.openSheet() }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                AddTestDataFab { viewModel.addTestData(bucketType = bucketType, bucketBox = bucketBox ?: return@AddTestDataFab) }
                Spacer(modifier = Modifier.height(height = 8.dp))
                BucketFloatingButton(
                    bucketType = bucketType ?: BucketBox.BucketType.Unknown,
                    isSelecting = isSelecting,
                    onClickTodo = { addTodoBottomSheetState.openSheet() },
                    onClickBook = { addBookBottomSheetState.openSheet() },
                    onClickShow = { addShowBottomSheetState.openSheet() },
                    onClickLink = { addLinkBottomSheetState.openSheet() },
                    onClickLocation = {},
                )
            }
        },
        bottomSheetContent = {
            BucketAddDataBottomSheet(
                addTodoBottomSheetState = addTodoBottomSheetState,
                addBookBottomSheetState = addBookBottomSheetState,
                addShowBottomSheetState = addShowBottomSheetState,
                addLinkBottomSheetState = addLinkBottomSheetState,
                addLocationBottomSheetState = addLocationBottomSheetState,
                outerHazeState = hazeState,
                bucketBox = bucketBox,
                bucketBoxId = bucketBoxId,
                bucketType = bucketType ?: BucketBox.BucketType.Unknown,
                onAddBucketItemBox = { viewModel.putBucketItemBox(bucketItemBox = it, parent = bucketBox ?: return@BucketAddDataBottomSheet) },
                onAddBook = { IntentUtil.launchBucketItemActivity(context = context, bucketItemActivityData = BucketItemActivityData.NewItem.BookItem(parentId = bucketBox?.id ?: return@BucketAddDataBottomSheet, olBookSearchResult = it)) },
                onClickShow = { IntentUtil.launchBucketItemActivity(context = context, bucketItemActivityData = BucketItemActivityData.NewItem.ShowItem(parentId = bucketBox?.id ?: return@BucketAddDataBottomSheet, traktShowSearchResult = it)) }
            )
            BucketViewDataBottomSheet(
                viewLinkBottomSheet = viewLinkBottomSheet,
                outerHazeState = hazeState,
                onUpdateBucketItemBox = {}
            )
            MetadataBottomSheet(
                bottomSheet2State = metadataBottomSheetState,
                outerHazeState = hazeState,
                bucketBox = bucketBox,
                onClickEdit = { editBottomSheetState.openSheet() }
            )
            EditBottomSheet(
                bottomSheet2State = editBottomSheetState,
                bucketBox = bucketBox,
                onUpdateBucket = viewModel::updateBucketBox
            )
        },
        hazeState = hazeState,
        compositionLocalValues = listOf(
            LocalSelectionContainerActor provides selectionContainerActor
        ),
        overlayContent = {
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 24.dp)
            ) {
                SelectionActionCard.TodoBucketScreenSelectionActionCard(
                    visible = isSelecting,
                    areAllSelectedFavourite = false,
                    areAllSelectedLocked = false,
                    onClickSelectAll = {},
                    onClickMove = {},
                    onClickFavourite = {},
                    onClickLock = {},
                    onClickShare = {},
                    onClickSetState = {},
                    onClickDelete = {},
                    onClickCancel = { selectionContainerActor.unselect() },
                )
            }
        }
    ) {
        AnimatedContent(
            targetState = bucketBoxData::class.simpleName,
            transitionSpec = { AnimationDefaults.Fade }
        ) { bucketBoxData1 ->
            when (bucketBoxData1) {
                DataLoader.Init::class.simpleName -> DataLoaderLoadingView()
                DataLoader.Loading::class.simpleName -> DataLoaderLoadingView()
                DataLoader.NoData::class.simpleName -> Unit
                DataLoader.Error::class.simpleName -> Unit
                DataLoader.Loaded::class.simpleName -> when (bucketType) {
                    BucketBox.BucketType.Todo -> TodoScreen(
                        bucketBoxDataFlow = viewModel.bucketBoxDataFlow,
                        pagerState = pagerState,
                        bucketItemBoxGroupDataFlow = bucketItemBoxGroupDataFlow,
//                        onUpdateBucketItemBoxState = viewModel::updateBucketItemBoxState,
                        onUpdateBucketItemOrder = viewModel::onUpdateBucketItemOrder
                    )

                    BucketBox.BucketType.Book -> BookScreen(
                        bucketBoxDataFlow = viewModel.bucketBoxDataFlow,
                        pagerState = pagerState,
                        bucketItemBoxGroupDataFlow = bucketItemBoxGroupDataFlow,
                        bucketItemBoxOrderedDataFlow = bucketItemBoxOrderedDataFlow,
                        onUpdateBucketItemBoxState = viewModel::updateBucketItemBoxState,
                        onUpdateBucketItemOrder = viewModel::onUpdateBucketItemOrder,
                        onClickBucketItem = { IntentUtil.launchBucketItemActivity(context = context, bucketItemActivityData = BucketItemActivityData.LocalItem(parentId = bucketBox?.id ?: return@BookScreen, bucketItemId = it.id)) }
                    )

                    BucketBox.BucketType.Show -> ShowScreen(
                        bucketBoxDataFlow = viewModel.bucketBoxDataFlow,
                        pagerState = pagerState,
                        bucketItemBoxGroupDataFlow = bucketItemBoxGroupDataFlow,
//                        onUpdateBucketItemBoxState = viewModel::updateBucketItemBoxState,
                        onUpdateBucketItemOrder = viewModel::onUpdateBucketItemOrder,
                        onClickBucketItem = { IntentUtil.launchBucketItemActivity(context = context, bucketItemActivityData = BucketItemActivityData.LocalItem(parentId = bucketBox?.id ?: return@ShowScreen, bucketItemId = it.id)) }
                    )

                    BucketBox.BucketType.Link -> LinkScreen(
                        bucketBoxDataFlow = viewModel.bucketBoxDataFlow,
                        pagerState = pagerState,
                        bucketItemBoxGroupDataFlow = bucketItemBoxGroupDataFlow,
//                        onUpdateBucketItemBoxState = viewModel::updateBucketItemBoxState,
                        onUpdateBucketItemOrder = viewModel::onUpdateBucketItemOrder,
                        onClickBucketItem = { viewLinkBottomSheet.openSheet(data = it) }
                    )

                    BucketBox.BucketType.Location -> Unit
                    BucketBox.BucketType.Unknown -> Unit
                    null -> Unit
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BucketAddDataBottomSheet(
    addTodoBottomSheetState: GenericBottomSheet2State<Nothing> = GenericBottomSheet2State.rememberGenericBottomSheet2State(),
    addBookBottomSheetState: GenericBottomSheet2State<Nothing> = GenericBottomSheet2State.rememberGenericBottomSheet2State(),
    addShowBottomSheetState: GenericBottomSheet2State<Nothing> = GenericBottomSheet2State.rememberGenericBottomSheet2State(),
    addLinkBottomSheetState: GenericBottomSheet2State<Nothing> = GenericBottomSheet2State.rememberGenericBottomSheet2State(),
    addLocationBottomSheetState: GenericBottomSheet2State<Nothing> = GenericBottomSheet2State.rememberGenericBottomSheet2State(),
    outerHazeState: HazeState = remember { HazeState() },
    bucketBox: BucketBox? = null,
    bucketBoxId: Long? = null,
    bucketType: BucketBox.BucketType = BucketBox.BucketType.Unknown,
    onAddBucketItemBox: (bucketItemBox: BucketItemBox) -> Unit = {},
    onAddBook: (OLBookSearchResult) -> Unit = {},
    onClickShow: (TraktShowSearchResult) -> Unit = {}
) {
    AddTodoBottomSheet(
        bottomSheet2State = addTodoBottomSheetState,
        outerHazeState = outerHazeState,
        onAddTodo = onAddBucketItemBox
    )
    AddBookBottomSheet(
        bottomSheet2State = addBookBottomSheetState,
        outerHazeState = outerHazeState,
        onAddBook = onAddBook
    )
    AddShowBottomSheet(
        bottomSheet2State = addShowBottomSheetState,
        outerHazeState = outerHazeState,
        onClickShow = onClickShow,
    )
    AddLinkBottomSheet(
        bottomSheet2State = addLinkBottomSheetState,
        outerHazeState = outerHazeState,
        onAddLink = onAddBucketItemBox,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BucketViewDataBottomSheet(
    viewLinkBottomSheet: GenericBottomSheet2State<BucketItemBox> = GenericBottomSheet2State.rememberGenericBottomSheet2StateT(),
    outerHazeState: HazeState = remember { HazeState() },
    onUpdateBucketItemBox: (BucketItemBox) -> Unit = {}
) {
    LinkBottomSheet(
        bottomSheet2State = viewLinkBottomSheet,
        outerHazeState = outerHazeState,
        onUpdateBucketItemBox = onUpdateBucketItemBox
    )
}

@Composable
private fun DataLoaderLoadingView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(height = 4.dp))
        Text(text = stringResource(id = R.string.loading))
    }
}

@Composable
private fun AddTestDataFab(
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_fa_bug),
            contentDescription = null,
            modifier = Modifier.size(size = ICON_SIZE)
        )
    }
}

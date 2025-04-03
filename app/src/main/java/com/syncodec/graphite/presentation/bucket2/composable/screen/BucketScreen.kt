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
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.BucketBoxEncrypted
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxEncrypted
import com.syncodec.graphite.di.network.openLibrary.OLBookSearchResult
import com.syncodec.graphite.di.network.trakt.TraktShowSearchResult
import com.syncodec.graphite.di.secureRepository.BoxRepository
import com.syncodec.graphite.presentation.bucket2.BucketViewModel2
import com.syncodec.graphite.presentation.bucket2.composable.bar.BottomBar
import com.syncodec.graphite.presentation.bucket2.composable.bar.TopBar
import com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.AddBookBottomSheet
import com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.AddLinkBottomSheet
import com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.AddShowBottomSheet
import com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.AddTodoBottomSheet
import com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.EditBottomSheet
import com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.EditTodoBottomSheet
import com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.LinkBottomSheet
import com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.MetadataBottomSheet
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.BucketFloatingButton
import com.syncodec.graphite.presentation.bucket2.moveBucketItemScaffold.MoveBucketItemData
import com.syncodec.graphite.presentation.bucket2.moveBucketItemScaffold.MoveBucketItemScaffold
import com.syncodec.graphite.presentation.bucketItem2.utils.BucketItemUtils
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.graBottomSheet.FilterAndSortBottomSheet
import com.syncodec.graphite.presentation.common.v2.dialog2.GenericDialog2
import com.syncodec.graphite.presentation.common.v2.overlayScaffold.OverlayScaffold
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val bucketItemBoxOrderedDataFlow = viewModel.bucketItemBoxOrderedDataFlow

    val selectionContainerActor = remember { SelectionContainerActor() }
    val isSelecting by selectionContainerActor.isSelectingFlow.collectAsState()
    val selectedItemIdList by selectionContainerActor.selectedItemIdListFlow.collectAsState()

    val pagerState = rememberPagerState { 4 }

    val metadataBottomSheetState = GenericBottomSheet2State.rememberGenericBottomSheet2State()
    val filterAndSortBottomSheetState = GenericBottomSheet2State.rememberGenericBottomSheet2State()
    val editBottomSheetState = GenericBottomSheet2State.rememberGenericBottomSheet2State()

    val addTodoBottomSheetState = GenericBottomSheet2State.rememberGenericBottomSheet2State(skipPartiallyExpanded = true)
    val addBookBottomSheetState = GenericBottomSheet2State.rememberGenericBottomSheet2State(skipPartiallyExpanded = true)
    val addShowBottomSheetState = GenericBottomSheet2State.rememberGenericBottomSheet2State(skipPartiallyExpanded = true)
    val addLinkBottomSheetState = GenericBottomSheet2State.rememberGenericBottomSheet2State(skipPartiallyExpanded = true)
    val addLocationBottomSheetState = GenericBottomSheet2State.rememberGenericBottomSheet2State(skipPartiallyExpanded = true)

    val editTodoBottomSheetState = GenericBottomSheet2State.rememberGenericBottomSheet2StateT<BucketItemBoxDecrypted>(skipPartiallyExpanded = true)
    val viewLinkBottomSheet = GenericBottomSheet2State.rememberGenericBottomSheet2StateT<BucketItemBoxDecrypted>(skipPartiallyExpanded = true)

    val moveBucketItemState = OverlayScaffold.State.rememberOverlayStateT<MoveBucketItemData>()

    val deleteDialogState: GenericDialog2.State<List<Long>> = GenericDialog2.State.rememberDialogStateT()

    val bucketBox by remember(key1 = bucketBoxData) { derivedStateOf { (bucketBoxData as? DataLoader.Loaded)?.data } }
    val bucketBoxId by remember(key1 = bucketBox) { derivedStateOf { bucketBox?.id } }
    val title by remember(key1 = bucketBox) { derivedStateOf { bucketBox?.title } }
    val bucketType by remember(key1 = bucketBox) { derivedStateOf { bucketBox?.bucketType } }

    GenericScaffold2(
        topBar = {
            TopBar(
                title = title,
                bucketType = bucketType ?: BucketBoxEncrypted.BucketType.Unknown,
                stateFilterInt = pagerState.currentPage,
                isLocked = bucketBox?.isLocked != false,
                isFavourite = bucketBox?.isFavourite != false,
                onUpdateFilterInt = { scope.launch { pagerState.animateScrollToPage(page = it) } },
                onClickLock = viewModel::onToggleLockBucketBox,
                onClickFavourite = viewModel::onToggleFavouriteBucketBox
            )
        },
        bottomBar = {
            BottomBar(
                isSelecting = isSelecting,
                onClickFilterAndSortButton = { filterAndSortBottomSheetState.openSheet() },
                onClickMetadata = { metadataBottomSheetState.openSheet() }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                AddTestDataFab { viewModel.addTestData(bucketType = bucketType, bucketBox = bucketBox ?: return@AddTestDataFab) }
                Spacer(modifier = Modifier.height(height = 8.dp))
                BucketFloatingButton(
                    bucketType = bucketType ?: BucketBoxEncrypted.BucketType.Unknown,
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
                bucketBox = bucketBox,
                bucketBoxId = bucketBoxId,
                bucketType = bucketType ?: BucketBoxEncrypted.BucketType.Unknown,
                onAddBucketItemBox = { viewModel.putBucketItemBox(bucketItemBox = it, parent = bucketBox ?: return@BucketAddDataBottomSheet) },
                onAddBook = { IntentUtil.launchBucketItemActivity(context = context, bucketItemActivityData = BucketItemActivityData.NewItem.BookItem(parentId = bucketBox?.id ?: return@BucketAddDataBottomSheet, olBookSearchResult = it)) },
                onClickShow = { IntentUtil.launchBucketItemActivity(context = context, bucketItemActivityData = BucketItemActivityData.NewItem.ShowItem(parentId = bucketBox?.id ?: return@BucketAddDataBottomSheet, traktShowSearchResult = it)) }
            )
            BucketViewDataBottomSheet(
                editTodoBottomSheetState = editTodoBottomSheetState,
                viewLinkBottomSheet = viewLinkBottomSheet,
                onUpdateBucketItemBox = viewModel::updateBucketItemBox
            )
            MetadataBottomSheet(
                bottomSheet2State = metadataBottomSheetState,
                bucketBox = bucketBox,
                onClickEdit = { editBottomSheetState.openSheet() }
            )
            EditBottomSheet(
                bottomSheet2State = editBottomSheetState,
                bucketBox = bucketBox,
                onUpdateBucket = viewModel::updateBucketBox
            )

            FilterAndSortBottomSheet(bottomSheet2State = filterAndSortBottomSheetState)
        },
        compositionLocalValues = listOf(LocalSelectionContainerActor provides selectionContainerActor),
        overlayContent = {
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 24.dp)
            ) {
                val bucketItemBoxOrderedData by bucketItemBoxOrderedDataFlow.collectAsState()
                var filteredBucketItemBoxList: List<BoxRepository.Companion.CacheValue<BucketItemBoxEncrypted, BucketItemBoxDecrypted>> by remember { mutableStateOf(value = listOf()) }
                var areAllSelectedFavourite by remember { mutableStateOf(value = false) }
                var areAllSelectedLocked by remember { mutableStateOf(value = false) }
                LaunchedEffect(key1 = bucketItemBoxOrderedData.rand, key2 = selectedItemIdList, key3 = isSelecting) {
                    if (!isSelecting) return@LaunchedEffect
                    withContext(context = Dispatchers.Default) {
                        filteredBucketItemBoxList = (bucketItemBoxOrderedData as? DataLoader.Loaded)
                            ?.data
                            ?.bucketItemBoxMap
                            ?.filterKeys { it in selectedItemIdList }
                            ?.values?.toList() ?: listOf()
                        areAllSelectedFavourite = filteredBucketItemBoxList.all { it.enc.isFavourite?.decrypt(alice2) == true } && filteredBucketItemBoxList.isNotEmpty()
                        areAllSelectedLocked = filteredBucketItemBoxList.all { it.enc.isLocked?.decrypt(alice2) == true } && filteredBucketItemBoxList.isNotEmpty()
                    }
                }
                SelectionActionCard.TodoBucketScreenSelectionActionCard(
                    visible = isSelecting,
                    areAllSelectedFavourite = areAllSelectedFavourite,
                    areAllSelectedLocked = areAllSelectedLocked,
                    onClickSelectAll = { selectionContainerActor.selectAll() },
                    onClickMove = { moveBucketItemState.openOverlay(data = MoveBucketItemData(bucketType = bucketType ?: return@TodoBucketScreenSelectionActionCard, selectedItemIdList = selectedItemIdList.toList())); selectionContainerActor.unselect() },
                    onClickFavourite = { viewModel.onToggleFavouriteBucketItemBox(itemIdList = selectedItemIdList.toList()) },
                    onClickLock = { viewModel.onToggleLockBucketItemBox(itemIdList = selectedItemIdList.toList()) },
                    onClickShare = {
                        scope.launch(context = Dispatchers.Default) {
                            val sharableText = BucketItemUtils.toSharableString(bucketItemBoxDecryptedList = filteredBucketItemBoxList.mapNotNull { it.decryptBlocking(alice2) })
                            IntentUtil.shareText(context = context, text = sharableText)
                        }
                    },
                    onClickSetState = { viewModel.updateBucketItemBoxState(itemIdList = selectedItemIdList.toList(), newState = it) },
                    onClickDelete = { deleteDialogState.openDialog(data = selectedItemIdList.toList()) },
                    onClickCancel = { selectionContainerActor.unselect() },
                )
            }

            GenericDialog2.DeleteDialog(
                state = deleteDialogState,
                onClickDelete = { deleteDialogState.closeDialog(data = null); selectionContainerActor.unselect(); viewModel.deleteBucketItemBox(idList = it) },
                onClickCancel = { deleteDialogState.closeDialog(data = null) }
            )
        },
        overlayScaffold = {
            MoveBucketItemScaffold(state = moveBucketItemState)
        },
    ) {
        BucketScreenContent(
            bucketBoxData = bucketBoxData,
            bucketType = bucketType,
            viewModel = viewModel,
            pagerState = pagerState,
            bucketItemBoxOrderedDataFlow = bucketItemBoxOrderedDataFlow,
            editTodoBottomSheetState = editTodoBottomSheetState,
            viewLinkBottomSheet = viewLinkBottomSheet
        )
    }
}

@Composable
private fun BucketScreenContent(
    bucketBoxData: DataLoader<BucketBoxDecrypted>,
    bucketType: BucketBoxEncrypted.BucketType?,
    viewModel: BucketViewModel2,
    pagerState: PagerState,
    bucketItemBoxOrderedDataFlow: StateFlow<DataLoader<BucketViewModel2.BucketItemBoxOrderedData>>,
    editTodoBottomSheetState: GenericBottomSheet2State<BucketItemBoxDecrypted>,
    viewLinkBottomSheet: GenericBottomSheet2State<BucketItemBoxDecrypted>,
) {
    val context = LocalContext.current

    val bucketBox by remember(key1 = bucketBoxData) { derivedStateOf { (bucketBoxData as? DataLoader.Loaded)?.data } }

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
                BucketBoxEncrypted.BucketType.Todo -> TodoScreen(
                    bucketBoxDataFlow = viewModel.bucketBoxDataFlow,
                    pagerState = pagerState,
                    bucketItemBoxOrderedDataFlow = bucketItemBoxOrderedDataFlow,
                    onUpdateBucketItemOrder = viewModel::onUpdateBucketItemOrder,
                    onUpdateBucketItemBoxState = viewModel::updateBucketItemBoxState,
                    onClickBucketItem = { editTodoBottomSheetState.openSheet(data = it) }
                )

                BucketBoxEncrypted.BucketType.Book -> BookScreen(
                    bucketBoxDataFlow = viewModel.bucketBoxDataFlow,
                    pagerState = pagerState,
                    bucketItemBoxOrderedDataFlow = bucketItemBoxOrderedDataFlow,
                    onUpdateBucketItemOrder = viewModel::onUpdateBucketItemOrder,
                    onClickBucketItem = { IntentUtil.launchBucketItemActivity(context = context, bucketItemActivityData = BucketItemActivityData.LocalItem(parentId = bucketBox?.id ?: return@BookScreen, bucketItemId = it.id)) }
                )

                BucketBoxEncrypted.BucketType.Show -> ShowScreen(
                    bucketBoxDataFlow = viewModel.bucketBoxDataFlow,
                    pagerState = pagerState,
                    bucketItemBoxOrderedDataFlow = bucketItemBoxOrderedDataFlow,
                    onUpdateBucketItemOrder = viewModel::onUpdateBucketItemOrder,
                    onClickBucketItem = { IntentUtil.launchBucketItemActivity(context = context, bucketItemActivityData = BucketItemActivityData.LocalItem(parentId = bucketBox?.id ?: return@ShowScreen, bucketItemId = it.id)) }
                )
//
                BucketBoxEncrypted.BucketType.Link -> LinkScreen(
                    bucketBoxDataFlow = viewModel.bucketBoxDataFlow,
                    pagerState = pagerState,
                    bucketItemBoxOrderedDataFlow = bucketItemBoxOrderedDataFlow,
                    onUpdateBucketItemOrder = viewModel::onUpdateBucketItemOrder,
                    onClickBucketItem = { viewLinkBottomSheet.openSheet(data = it) }
                )

                BucketBoxEncrypted.BucketType.Location -> Unit
                BucketBoxEncrypted.BucketType.Unknown -> Unit
                null -> Unit
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
    bucketBox: BucketBoxDecrypted? = null,
    bucketBoxId: Long? = null,
    bucketType: BucketBoxEncrypted.BucketType = BucketBoxEncrypted.BucketType.Unknown,
    onAddBucketItemBox: (bucketItemBox: BucketItemBoxDecrypted) -> Unit = {},
    onAddBook: (OLBookSearchResult) -> Unit = {},
    onClickShow: (TraktShowSearchResult) -> Unit = {}
) {
    AddTodoBottomSheet(
        bottomSheet2State = addTodoBottomSheetState,
        onAddTodo = onAddBucketItemBox
    )
    AddBookBottomSheet(
        bottomSheet2State = addBookBottomSheetState,
        onAddBook = onAddBook
    )
    AddShowBottomSheet(
        bottomSheet2State = addShowBottomSheetState,
        onClickShow = onClickShow,
    )
    AddLinkBottomSheet(
        bottomSheet2State = addLinkBottomSheetState,
        onAddLink = onAddBucketItemBox,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BucketViewDataBottomSheet(
    editTodoBottomSheetState: GenericBottomSheet2State<BucketItemBoxDecrypted> = GenericBottomSheet2State.rememberGenericBottomSheet2StateT(),
    viewLinkBottomSheet: GenericBottomSheet2State<BucketItemBoxDecrypted> = GenericBottomSheet2State.rememberGenericBottomSheet2StateT(),
    onUpdateBucketItemBox: (BucketItemBoxDecrypted) -> Unit = {}
) {
    EditTodoBottomSheet(
        bottomSheet2State = editTodoBottomSheetState,
        onUpdateBucketItemBox = onUpdateBucketItemBox
    )
    LinkBottomSheet(
        bottomSheet2State = viewLinkBottomSheet,
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

package com.syncodec.graphite.presentation.bucket.composable.screen

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.BucketViewModel
import com.syncodec.graphite.presentation.bucket.composable.bar.BottomBar
import com.syncodec.graphite.presentation.bucket.composable.bar.TopBar
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.MetadataBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.screen.bookScreen.BucketBookScreen
import com.syncodec.graphite.presentation.bucket.composable.screen.linkScreen.BucketLinkScreen
import com.syncodec.graphite.presentation.bucket.composable.screen.showScreen.BucketShowScreen
import com.syncodec.graphite.presentation.bucket.composable.screen.todoScreen.BucketTodoScreen
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.dialog.dialog2.DeleteDialog
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.common.selectionAction.BucketSelectionActionView
import com.syncodec.graphite.utils.LocalIsAuthenticated
import com.syncodec.graphite.utils.enumValueOf
import com.syncodec.graphite.utils.xor
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Preview
@Composable
fun BucketScreen(
	afterDeleteBucket: () -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	val isAuthenticated = LocalIsAuthenticated.current

	val viewModel: BucketViewModel = koinViewModel()
	val bucketScreenCommonViewModel: BucketScreenCommonViewModel = koinViewModel()

	val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

	val pagerState = rememberPagerState(
		initialPage = 0,
		initialPageOffsetFraction = 0f,
		pageCount = { 4 }
	)

	var isSelecting by remember { mutableStateOf(false) }
	var selectedIdList: Set<RealmUUID> by remember { mutableStateOf(setOf()) }
	var toSelectIdList: Set<RealmUUID> by remember { mutableStateOf(setOf()) }
	fun onSelect(id: RealmUUID, idList: Set<RealmUUID>) {
		if (!isSelecting) isSelecting = true
		selectedIdList.toMutableSet().apply {
			xor(id)
			selectedIdList = toSet()
		}
		toSelectIdList = idList
	}

	var isSearching by remember { mutableStateOf(false) }

	val searchQueryList by bucketScreenCommonViewModel.searchQueryList.collectAsState()

	var showEditBucketDialog by remember { mutableStateOf(false) }
	var showDeleteBucketItemsDialog by remember { mutableStateOf(false) }
	var showDeleteBucketDialog by remember { mutableStateOf(false) }

	val bucketObject by viewModel.bucketObject.collectAsState()
	val title by viewModel.title.collectAsState()
	val bucketType by viewModel.bucketType.collectAsState()

	val bottomSheetState = rememberModalBottomSheetState()
	var isMenuBottomSheetVisible by remember { mutableStateOf(false) }
	var isMetadataBottomSheetVisible by remember { mutableStateOf(false) }

	var isDeleteDialogVisible by remember { mutableStateOf(false) }

	BackHandler(enabled = isSelecting) { isSelecting = false; selectedIdList = setOf() }
	BackHandler(enabled = isSearching) { isSearching = false; bucketScreenCommonViewModel.clearSearchFilter() }

	fun share(shareAll: Boolean = false) {
		viewModel.shareBucketItems(shareAll = shareAll, realmUUIDList = selectedIdList.toList()) {
			withContext(Dispatchers.Main) {
				Intent(Intent.ACTION_SEND).apply {
					type = "text/html"
					putExtra(Intent.EXTRA_SUBJECT, title ?: bucketType)
					putExtra(Intent.EXTRA_TEXT, it)

					if (resolveActivity(context.packageManager) != null) context.startActivity(Intent.createChooser(this, "Share using"))
					else Toast.makeText(context, "No app found on your device which can perform this action", Toast.LENGTH_SHORT).show()
				}
			}
		}
		isSelecting = false
		selectedIdList = setOf()
	}

	bucketObject?.let { bucketObject1 ->
		GenericScaffold2(
			topBar = {
				TopBar(
					title = bucketObject1.title,
					isLocked = bucketObject1.isLocked,
					isFavourite = bucketObject1.isFavourite,
					bucketType = enumValueOf(bucketObject1.bucketType, BucketType.UNKNOWN),
					pagerState = pagerState,
					isSearching = isSearching,
					isSelecting = isSelecting,
					searchQueryList = searchQueryList,
					selectedItemSize = selectedIdList.size,
					onCancelSelection = { isSelecting = false; selectedIdList = setOf() },
					onClickFavourite = viewModel::toggleFavourite,
					onClickLock = { viewModel.toggleLock() },
					onClickSearch = { isSearching = true },
					onShare = { share(false) },
					onDelete = {},
					onClickBack = { backPressedDispatcher?.onBackPressed() },
					addSearchQuery = { bucketScreenCommonViewModel.filterBySearchAdd(it) },
					removeSearchQuery = { bucketScreenCommonViewModel.filterBySearchRemove(it) },
				)
			},
			bottomBar = {
				BottomBar(
					onClickMetadata = { isMetadataBottomSheetVisible = true },
				)
			},
			isBottomBarVisible = !isSelecting,
			dialogContent = {
//				BucketDialog(
//					bucketObject = bucketObject1,
//					showEditBucketDialog = showEditBucketDialog,
//					showDeleteBucketItemsDialog = showDeleteBucketItemsDialog,
//					showDeleteBucketDialog = showDeleteBucketDialog,
//					onUpdateBucket = viewModel::updateBucket,
//					onDelete = {
//						viewModel.deleteBucketItem(selectedIdList.toList())
//						isSelecting = false
//						selectedIdList = setOf()
//					},
//					onDeleteBucket = {
//						viewModel.deleteBucket(bucketObject1.id) {
//							closeDialog(BucketDialogType.DeleteBucket)
//							withContext(Dispatchers.Main) { Toast.makeText(context, "Bucket Deleted", Toast.LENGTH_SHORT).show() }
//							afterDeleteBucket()
//						}
//					},
//					closeDialog = ::closeDialog,
//				)
			},
			isButtonVisible = !isSelecting
		) {
			when (bucketObject1.bucketType) {
				BucketType.TODO.name -> BucketTodoScreen(
					pagerState = pagerState,
					isSelecting = isSelecting,
					selectedIdList = selectedIdList,
					onSelect = ::onSelect,
				)

				BucketType.BOOK.name -> BucketBookScreen(
					pagerState = pagerState,
					isSelecting = isSelecting,
					selectedIdList = selectedIdList,
					onSelect = ::onSelect
				)

				BucketType.SHOW.name -> BucketShowScreen(
					pagerState = pagerState,
					isSelecting = isSelecting,
					selectedIdList = selectedIdList,
					onSelect = ::onSelect
				)

				BucketType.LINK.name -> BucketLinkScreen(
					viewModel = bucketScreenCommonViewModel,
					isSelecting = isSelecting,
					selectedIdList = selectedIdList,
					onSelect = ::onSelect
				)

				BucketType.UNKNOWN.name -> null
				else -> null
			}

			BucketSelectionActionView(
				modifier = Modifier
					.padding(start = 24.dp, top = 0.dp, end = 24.dp, bottom = 32.dp)
					.align(Alignment.BottomCenter),
				isSelecting = isSelecting,
				selectedItemCount = selectedIdList.size,
				onClickShare = {},
				onClickSelectAll = { selectedIdList.toMutableSet().apply { addAll(toSelectIdList); selectedIdList = toSet() } },
				onClickDelete = { isDeleteDialogVisible = true },
				onClickCancel = { backPressedDispatcher?.onBackPressed() },
				onClickMove = {},
				onClickFavourite = { bucketScreenCommonViewModel.toggleFavourite(selectedIdList) },
				onClickLock = {
					if (isAuthenticated) bucketScreenCommonViewModel.toggleLock(selectedIdList)
					else Toast.makeText(context, context.getText(R.string.toast_not_authenticated), Toast.LENGTH_SHORT).show()
				},
			) {}
		}
	} ?: LoadingView()

	MetadataBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isMetadataBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isMetadataBottomSheetVisible = false } },
		id = bucketObject?.id,
		createdTimestamp = bucketObject?.createdTimestamp,
		modifiedTimestamp = bucketObject?.modifiedTimestamp,
	)

	DeleteDialog(
		isDialogVisible = isDeleteDialogVisible,
		onDismissRequest = { isDeleteDialogVisible = false },
		title = stringResource(id = R.string.delete_items_multiple),
		contentText = stringResource(id = R.string.are_you_sure_delete_multiple),
		onConfirmDelete = {
			bucketScreenCommonViewModel.deleteMultiple(idList = selectedIdList.toSet())
			isSelecting = false; selectedIdList = setOf()
			isDeleteDialogVisible = false
		},
	)
}

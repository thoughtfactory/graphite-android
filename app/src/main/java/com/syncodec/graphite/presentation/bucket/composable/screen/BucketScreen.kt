package com.syncodec.graphite.presentation.bucket.composable.screen

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.EditBucketBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.MenuBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.screen.bookScreen.BucketBookScreen
import com.syncodec.graphite.presentation.bucket.composable.screen.linkScreen.BucketLinkScreen
import com.syncodec.graphite.presentation.bucket.composable.screen.showScreen.BucketShowScreen
import com.syncodec.graphite.presentation.bucket.composable.screen.todoScreen.BucketTodoScreen
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetInfo2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.sheets.MetadataBottomSheet
import com.syncodec.graphite.presentation.common.dialog.dialog2.DeleteDialog
import com.syncodec.graphite.presentation.common.dialog.where.whereBucketDialog.WhereBucketDialog2
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.common.selectionAction.BucketSelectionActionView
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

	val viewModel: BucketViewModel = koinViewModel()
	val bucketScreenCommonViewModel: BucketScreenCommonViewModel = koinViewModel()

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

	val bucketObject by viewModel.bucketObject.collectAsState()

	val bottomSheetState = rememberModalBottomSheetState()
	var isMenuBottomSheetVisible by remember { mutableStateOf(false) }
	var isMetadataBottomSheetVisible by remember { mutableStateOf(false) }
	var isEditBucketBottomSheetVisible by remember { mutableStateOf(false) }

	var isWhereBucketDialogVisible by remember { mutableStateOf(false) }
	var isDeleteBucketDialogVisible by remember { mutableStateOf(false) }
	var isDeleteSelectedItemDialogVisible by remember { mutableStateOf(false) }

	BackHandler(enabled = isSelecting) { isSelecting = false; selectedIdList = setOf() }
	BackHandler(enabled = isSearching) { isSearching = false; bucketScreenCommonViewModel.clearSearchFilter() }

	suspend fun share(shareText: String) {
		withContext(Dispatchers.Main) {
			Intent(Intent.ACTION_SEND).apply {
				type = "text/html"
				putExtra(Intent.EXTRA_SUBJECT, "My book list")
				putExtra(Intent.EXTRA_TEXT, shareText)
				addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

				if (resolveActivity(context.packageManager) != null) context.startActivity(Intent.createChooser(this, "Share using"))
				else Toast.makeText(context, "No app found on your device which can perform this action", Toast.LENGTH_SHORT).show()
			}
		}
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
					onClickFavourite = { viewModel.toggleFavourite(bucketObject1.id) },
					onClickLock = { viewModel.toggleLock(bucketObject1.id) },
					onClickSearch = { isSearching = true },
					onClickMenuButton = { isMenuBottomSheetVisible = true },
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
				DeleteDialog(
					isDialogVisible = isDeleteSelectedItemDialogVisible,
					onDismissRequest = { isDeleteSelectedItemDialogVisible = false },
					title = stringResource(id = R.string.delete_items_multiple),
					contentText = stringResource(id = R.string.are_you_sure_delete_multiple),
					onConfirmDelete = {
						bucketScreenCommonViewModel.deleteMultiple(idList = selectedIdList.toSet())
						isSelecting = false; selectedIdList = setOf()
						isDeleteSelectedItemDialogVisible = false
					},
				)

				DeleteDialog(
					isDialogVisible = isDeleteBucketDialogVisible,
					onDismissRequest = { isDeleteBucketDialogVisible = false },
					title = stringResource(id = R.string.delete_item),
					contentText = stringResource(id = R.string.are_you_sure_delete_bucket),
					onConfirmDelete = {},
				)
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

				else -> Unit
			}

			BucketSelectionActionView(
				modifier = Modifier
					.padding(start = 24.dp, top = 0.dp, end = 24.dp, bottom = 32.dp)
					.align(Alignment.BottomCenter),
				bucketType = BucketType.entries.find { it.name == bucketObject?.bucketType },
				isSelecting = isSelecting,
				selectedItemCount = selectedIdList.size,
				onClickShare = { bucketScreenCommonViewModel.shareBucketItems(idList = selectedIdList, callback = ::share) },
				onClickSelectAll = { selectedIdList.toMutableSet().apply { addAll(toSelectIdList); selectedIdList = toSet() } },
				onClickDelete = { isDeleteSelectedItemDialogVisible = true },
				onClickMove = { isWhereBucketDialogVisible = true },
				onClickFavourite = { bucketScreenCommonViewModel.toggleFavourite(selectedIdList) },
				onClickLock = { bucketScreenCommonViewModel.toggleLock(selectedIdList) },
				onClickSetAs = { bucketScreenCommonViewModel.updateBucketItemState(selectedIdList, it) }
			)
		}
	} ?: LoadingView()

	MetadataBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isMetadataBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isMetadataBottomSheetVisible = false } },
		id = bucketObject?.id,
		createdTimestamp = bucketObject?.createdTimestamp,
		modifiedTimestamp = bucketObject?.modifiedTimestamp,
		extraContent = {
			GenericBottomSheetInfo2(
				key = stringResource(id = R.string.description),
				value = bucketObject?.description ?: stringResource(id = R.string.no_description),
			)
		}

	)

	MenuBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isMenuBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isMenuBottomSheetVisible = false } },
		onClickShareAll = {},
		onClickEdit = { scope.launch { bottomSheetState.hide(); isMenuBottomSheetVisible = false; isEditBucketBottomSheetVisible = true } },
		onClickDelete = { isDeleteBucketDialogVisible = true },
	)

	EditBucketBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isEditBucketBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isEditBucketBottomSheetVisible = false } },
		bucketObject = bucketObject,
		onClickUpdate = { newTitle, newDescription -> viewModel.updateTitleDescription(id = bucketObject?.id, title = newTitle, description = newDescription) },
	)

	WhereBucketDialog2(
		isDialogVisible = isWhereBucketDialogVisible,
		onDismissRequest = { isWhereBucketDialogVisible = false },
		currentSelectedBucket = bucketObject?.id,
		currentBucketType = bucketObject?.bucketType,
		onSelectBucket = { bucketScreenCommonViewModel.moveBucketItem(idList = selectedIdList, newParentId = it); selectedIdList = setOf() },
	)
}

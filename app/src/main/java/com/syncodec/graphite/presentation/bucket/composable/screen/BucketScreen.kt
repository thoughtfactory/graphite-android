package com.syncodec.graphite.presentation.bucket.composable.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.composable.bar.BottomBar
import com.syncodec.graphite.presentation.bucket.composable.bar.TopBar
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.EditBucketBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.MenuBottomSheet
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetInfo2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.sheets.MetadataBottomSheet
import com.syncodec.graphite.presentation.common.dialog.dialog2.DeleteDialog
import com.syncodec.graphite.presentation.common.dialog.where.whereBucketDialog.WhereBucketDialog2
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.common.selectionAction.BucketSelectionActionView
import com.syncodec.graphite.utils.enumValueOf
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Preview
@Composable
fun BucketScreen(
	pagerState: PagerState = rememberPagerState { 4 },
	bucketObject: BucketObject? = null,
	bucketItemListMap: Map<BucketItemState?, List<BucketItemObject>> = mapOf(),
	searchQueryList: Set<String> = setOf(),
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	toggleFavourite: (RealmUUID) -> Unit = {},
	toggleLock: (RealmUUID) -> Unit = {},
	filterBySearchAdd: (String) -> Unit = {},
	filterBySearchRemove: (String) -> Unit = {},
	updateTitleDescription: (id: RealmUUID, title: String?, description: String?) -> Unit = { _, _, _ -> },
	moveBucketItem: (idList: Set<RealmUUID>, newParentId: RealmUUID) -> Unit = { _, _ -> },
	toggleMultiFavorite: (idList: Set<RealmUUID>) -> Unit = {},
	toggleMultiLock: (idList: Set<RealmUUID>) -> Unit = {},
	updateMultiState: (idList: Set<RealmUUID>, newState: Int) -> Unit = { _, _ -> },
	deleteMulti: (idList: Set<RealmUUID>) -> Unit = {},
	clearSearchFilter: () -> Unit = {},
	onSelectAll: () -> Unit = {},
	onUnselectAll: () -> Unit = {},
	shareBucketItems: (idList: Set<RealmUUID>) -> Unit = {},
	content: @Composable BoxScope.() -> Unit = {},
) {
	val scope = rememberCoroutineScope()

	var isSearching by remember { mutableStateOf(false) }


	val bottomSheetState = rememberModalBottomSheetState()
	var isMenuBottomSheetVisible by remember { mutableStateOf(false) }
	var isMetadataBottomSheetVisible by remember { mutableStateOf(false) }
	var isEditBucketBottomSheetVisible by remember { mutableStateOf(false) }

	var isWhereBucketDialogVisible by remember { mutableStateOf(false) }
	var isDeleteBucketDialogVisible by remember { mutableStateOf(false) }
	var isDeleteSelectedItemDialogVisible by remember { mutableStateOf(false) }

	BackHandler(enabled = isSearching) { isSearching = false; clearSearchFilter() }

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
					onClickFavourite = { toggleFavourite(bucketObject1.id) },
					onClickLock = { toggleLock(bucketObject1.id) },
					onClickSearch = { isSearching = true },
					onClickMenuButton = { isMenuBottomSheetVisible = true },
					addSearchQuery = filterBySearchAdd,
					removeSearchQuery = filterBySearchRemove,
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
						deleteMulti(selectedIdList.toSet())
						onUnselectAll()
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
			content()
			BucketSelectionActionView(
				modifier = Modifier
					.padding(start = 24.dp, top = 0.dp, end = 24.dp, bottom = 32.dp)
					.align(Alignment.BottomCenter),
				bucketType = BucketType.entries.find { it.name == bucketObject.bucketType },
				isSelecting = isSelecting,
				selectedItemCount = selectedIdList.size,
				isAllItemFavourite = selectedIdList.isNotEmpty() && bucketItemListMap.flatMap { it.value }.filter { it.id in selectedIdList }.all { it.isFavourite },
				isAllItemLocked = selectedIdList.isNotEmpty() && bucketItemListMap.flatMap { it.value }.filter { it.id in selectedIdList }.all { it.isLocked },
				onClickShare = { shareBucketItems(selectedIdList) },
				onClickSelectAll = onSelectAll,
				onClickDelete = { isDeleteSelectedItemDialogVisible = true },
				onClickMove = { isWhereBucketDialogVisible = true },
				onClickFavourite = { toggleMultiFavorite(selectedIdList) },
				onClickLock = { toggleMultiLock(selectedIdList) },
				onClickSetAs = { updateMultiState(selectedIdList, it) }
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
		onClickUpdate = { newTitle, newDescription -> bucketObject?.id?.let { updateTitleDescription(it, newTitle, newDescription) } },
	)

	WhereBucketDialog2(
		isDialogVisible = isWhereBucketDialogVisible,
		onDismissRequest = { isWhereBucketDialogVisible = false },
		currentSelectedBucket = bucketObject?.id,
		currentBucketType = bucketObject?.bucketType,
		onSelectBucket = { moveBucketItem(selectedIdList, it); onUnselectAll() },
	)
}

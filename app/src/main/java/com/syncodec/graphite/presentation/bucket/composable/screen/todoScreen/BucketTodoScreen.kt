package com.syncodec.graphite.presentation.bucket.composable.screen.todoScreen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.model.local.BucketItemObject
import com.syncodec.graphite.di.model.local.BucketItemState
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.AddTodoBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.EditTodoBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.buildingBlock.TodoFloatingActionButton
import com.syncodec.graphite.presentation.bucket.composable.buildingBlock.TodoFloatingActionButtonDebug
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Preview
@Composable
fun BucketTodoScreen(
	pagerState: PagerState = rememberPagerState(initialPage = 0, initialPageOffsetFraction = 0f, pageCount = { 0 }),
	bucketItemList: List<BucketItemObject> = listOf(),
	previewBucketItemObject: BucketItemObject? = null,
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	onSelect: (RealmUUID, Set<RealmUUID>) -> Unit = { _, _ -> },
	selectBucketItemObjectForPreview: (RealmUUID) -> Unit = {},
	onUpdateTitle: (BucketItemObject, String) -> Unit = { _, _ -> },
	toggleFavourite: (RealmUUID) -> Unit = {},
	toggleLock: (RealmUUID) -> Unit = {},
	toggleBucketItemState: (RealmUUID) -> Unit = {},
	onReorderBucketItemList: (List<RealmUUID>) -> Unit = {},
	updateBucketItemTitle: (RealmUUID, String) -> Unit = { _, _ -> },
	updateBucketItemState: (RealmUUID, Int) -> Unit = { _, _ -> },
	putTodo: (realmUUID: RealmUUID?, title: String, state: BucketItemState) -> Unit = { _, _, _ -> },
	addTodoDebugData: () -> Unit = {}
) {
	val scope = rememberCoroutineScope()

	val bottomSheetState = rememberModalBottomSheetState()
	var isAddTodoSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isEditTodoSheetVisible by remember { mutableStateOf(false) }

	GenericScaffold2(
		floatingActionButton = {
			if (BuildConfig.DEBUG) TodoFloatingActionButtonDebug(
				visible = !isSelecting,
				onClick = { isAddTodoSheetVisible = true },
				onClickDebug = addTodoDebugData,
			) else TodoFloatingActionButton(
				visible = !isSelecting,
				onClick = { isAddTodoSheetVisible = true },
			)
		}
	) {
		HorizontalPager(
			state = pagerState,
			userScrollEnabled = !isSelecting,
		) { page ->
			val filteredBucketItemList = when (page) {
				0 -> bucketItemList
				1 -> bucketItemList.filter { it.state == BucketItemState.ALPHA.name }
				2 -> bucketItemList.filter { it.state == BucketItemState.BETA.name }
				3 -> bucketItemList.filter { it.state == BucketItemState.GAMMA.name }
				else -> bucketItemList
			}

			BucketTodoListScreen(
				bucketItemList = filteredBucketItemList,
				isSelecting = isSelecting,
				selectedIdList = selectedIdList,
				onSelect = { onSelect(it, bucketItemList.map { it.id }.toSet()) },
				onClickBucketItem = {
					selectBucketItemObjectForPreview(it)
					isEditTodoSheetVisible = true
				},
				onCheckedChange = toggleBucketItemState,
				onReorderBucketItemList = onReorderBucketItemList
			)
		}
	}

	AddTodoBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isAddTodoSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isAddTodoSheetVisible = false } },
		onAddTodo = { todoTitle, state -> putTodo(null, todoTitle, BucketItemState.entries.getOrElse(state) { BucketItemState.ALPHA }) }
	)

	EditTodoBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isEditTodoSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isEditTodoSheetVisible = false } },
		bucketItemObject = previewBucketItemObject,
		onUpdateTitle = { bucketItemObject, title -> updateBucketItemTitle(bucketItemObject.id, title) },
		onUpdateState = { bucketItemObject, state -> updateBucketItemState(bucketItemObject.id, state) },
		onToggleFavourite = { toggleFavourite(it.id) },
		onToggleLock = { toggleLock(it.id) },
	)
}

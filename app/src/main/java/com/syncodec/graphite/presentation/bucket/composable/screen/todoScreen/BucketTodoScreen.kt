package com.syncodec.graphite.presentation.bucket.composable.screen.todoScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
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
	bucketItemListMap: Map<BucketItemState?, List<BucketItemObject>> = mapOf(),
	previewBucketItemObject: BucketItemObject? = null,
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	onSelect: (RealmUUID, Set<RealmUUID>) -> Unit = { _, _ -> },
	selectBucketItemObjectForPreview: (RealmUUID) -> Unit = {},
	onUpdateTitle: (BucketItemObject, String) -> Unit = { _, _ -> },
	onUpdateState: (BucketItemObject, Int) -> Unit = { _, _ -> },
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
			val bucketItemList = when (page) {
				0 -> mutableListOf<BucketItemObject>().apply {
					addAll(bucketItemListMap[BucketItemState.ALPHA] ?: listOf())
					addAll(bucketItemListMap[BucketItemState.BETA] ?: listOf())
					addAll(bucketItemListMap[BucketItemState.GAMMA] ?: listOf())
				}

				1 -> bucketItemListMap[BucketItemState.ALPHA]
				2 -> bucketItemListMap[BucketItemState.BETA]
				3 -> bucketItemListMap[BucketItemState.GAMMA]
				else -> bucketItemListMap.flatMap { it.value }
			} ?: listOf()

			BucketTodoListScreen(
				bucketItemList = bucketItemList,
				isSelecting = isSelecting,
				selectedIdList = selectedIdList,
				onSelect = { onSelect(it, bucketItemList.map { it.id }.toSet()) },
				onClickBucketItem = {
					selectBucketItemObjectForPreview(it.id)
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

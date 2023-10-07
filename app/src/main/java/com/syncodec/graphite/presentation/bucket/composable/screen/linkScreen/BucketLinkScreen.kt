package com.syncodec.graphite.presentation.bucket.composable.screen.linkScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.base.LocalAppDataStore
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.AddLinkBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.PreviewLinkBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.buildingBlock.LinkFloatingActionButton
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.utils.ViewType
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BucketLinkScreen(
	bucketItemList: List<BucketItemObject> = listOf(),
	previewBucketItemObject: BucketItemObject? = null,
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	onSelect: (RealmUUID, Set<RealmUUID>) -> Unit = { _, _ -> },
	selectBucketItemObjectForPreview: (RealmUUID) -> Unit = {},
	toggleFavourite: (RealmUUID) -> Unit = {},
	toggleLock: (RealmUUID) -> Unit = {},
	onReorderBucketItemList: (List<RealmUUID>) -> Unit = {},
	putLink: (url: String, state: BucketItemState) -> Unit = { _, _ -> },
) {
	val scope = rememberCoroutineScope()

	val appDataStore = LocalAppDataStore.current
	val viewType by appDataStore.getViewType.collectAsState(initial = null)

	val bottomSheetState = rememberModalBottomSheetState()
	var isAddLinkBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isPreviewLinkBottomSheetVisible by rememberSaveable { mutableStateOf(false) }

	GenericScaffold2(
		floatingActionButton = {
			LinkFloatingActionButton(
				visible = !isSelecting,
				onClick = { isAddLinkBottomSheetVisible = true }
			)
		}
	) {
		AnimatedContent(
			targetState = viewType,
			transitionSpec = { fadeIn(tween(ANIMATION_DURATION_MILLIS)) + scaleIn(tween(ANIMATION_DURATION_MILLIS), 0.71f) togetherWith fadeOut(tween(470)) + scaleOut(tween(470), 0.71f) },
			label = "viewType_animation"
		) {
			when (it) {
				ViewType.List -> BucketLinkListScreen(
					bucketItemList = bucketItemList,
					isSelecting = isSelecting,
					selectedIdList = selectedIdList,
					onSelect = { onSelect(it, bucketItemList.map { it.id }.toSet()) },
					onClickBucketItem = {
						selectBucketItemObjectForPreview(it)
						isPreviewLinkBottomSheetVisible = true
					},
					onReorderBucketItemList = onReorderBucketItemList,
				)

				ViewType.Grid -> BucketLinkGridScreen(
					bucketItemList = bucketItemList,
					isSelecting = isSelecting,
					selectedIdList = selectedIdList,
					onSelect = { onSelect(it, bucketItemList.map { it.id }.toSet()) },
					onClickBucketItem = {
						selectBucketItemObjectForPreview(it)
						isPreviewLinkBottomSheetVisible = true
					},
					onReorderBucketItemList = onReorderBucketItemList,
				)

				null -> LoadingView()
			}
		}
	}

	AddLinkBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isAddLinkBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isAddLinkBottomSheetVisible = false } },
		onAddLink = { url, state -> putLink(url, BucketItemState.ALPHA) }
	)

	PreviewLinkBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isPreviewLinkBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isPreviewLinkBottomSheetVisible = false } },
		bucketItemObject = previewBucketItemObject,
		onToggleFavourite = { toggleFavourite(it.id) },
		onToggleLock = { toggleLock(it.id) },
	)
}

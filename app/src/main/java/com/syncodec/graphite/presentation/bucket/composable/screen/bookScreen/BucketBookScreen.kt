package com.syncodec.graphite.presentation.bucket.composable.screen.bookScreen

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.base.LocalAppDataStore
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.AddBookBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.buildingBlock.BookFloatingActionButton
import com.syncodec.graphite.presentation.bucketItem.activity.BookBucketItemActivity
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.ViewType
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.launch


@Preview
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BucketBookScreen(
	pagerState: PagerState = rememberPagerState(initialPage = 0, initialPageOffsetFraction = 0f, pageCount = { 0 }),
	bucketId: RealmUUID? = null,
	bucketItemList: List<BucketItemObject> = listOf(),
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	onSelect: (RealmUUID, Set<RealmUUID>) -> Unit = { _, _ -> },
	onReorderBucketItemList: (List<RealmUUID>) -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	val appDataStore = LocalAppDataStore.current
	val viewType by appDataStore.getViewType.collectAsState(initial = null)

	val bottomSheetState = rememberModalBottomSheetState()
	var isAddBookBottomSheetVisible by rememberSaveable { mutableStateOf(false) }

	fun onClickBucketItem(id: RealmUUID) {
		Intent(context, BookBucketItemActivity::class.java).apply {
			putExtra(Extra.Companion.Extra.IsNew.name, false)
			putExtra(Extra.Companion.Extra.BUCKET_ID.name, bucketId?.bytes)
			putExtra(Extra.Companion.Extra.BUCKET_TYPE.name, BucketType.BOOK.name)
			putExtra(Extra.Companion.Extra.BUCKET_ITEM_ID.name, id.bytes)

			context.startActivity(this)
		}
	}

	GenericScaffold2(
		floatingActionButton = {
			BookFloatingActionButton (
				visible = !isSelecting,
				onClick = { isAddBookBottomSheetVisible = true }
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
			AnimatedContent(
				targetState = viewType,
				transitionSpec = { fadeIn(tween(ANIMATION_DURATION_MILLIS)) + scaleIn(tween(ANIMATION_DURATION_MILLIS), 0.71f) togetherWith fadeOut(tween(470)) + scaleOut(tween(470), 0.71f) },
				label = "viewType_animation"
			) {
				when (it) {
					ViewType.List -> BucketBookListScreen(
						bucketItemList = filteredBucketItemList,
						isSelecting = isSelecting,
						selectedIdList = selectedIdList,
						onReorderBucketItemList = onReorderBucketItemList,
						onSelect = { id -> onSelect(id, filteredBucketItemList.map { it.id }.toSet()) },
						onClickBucketItem = ::onClickBucketItem,
					)

					ViewType.Grid -> BucketBookGridScreen(
						bucketItemList = filteredBucketItemList,
						isSelecting = isSelecting,
						selectedIdList = selectedIdList,
						onReorderBucketItemList = onReorderBucketItemList,
						onSelect = { id -> onSelect(id, filteredBucketItemList.map { it.id }.toSet()) },
						onClickBucketItem = ::onClickBucketItem,
					)

					null -> LoadingView()
				}
			}
		}
	}

	AddBookBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isAddBookBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isAddBookBottomSheetVisible = false } },
		parentId = bucketId,
	)
}

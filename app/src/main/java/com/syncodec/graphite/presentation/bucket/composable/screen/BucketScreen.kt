package com.syncodec.graphite.presentation.bucket.composable.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionBucketObject
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnPagerStateChange
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnReorderBucketItem
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOpenBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionPagerState
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionSetBucketItemObject
import com.syncodec.graphite.presentation.bucket.composable.bar.BottomBar
import com.syncodec.graphite.presentation.bucket.composable.bar.TopBar
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.BucketBottomSheetType
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.bucket.composable.dialog.BucketDialog
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.button.PrimaryButton
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.utils.LocalVaultIsOpened
import kotlinx.coroutines.launch


@OptIn(
	ExperimentalMaterialApi::class,
	ExperimentalPagerApi::class
)
@Composable
fun BucketScreen(
	modalBottomSheetState : ModalBottomSheetState,
	bottomSheetType : BucketBottomSheetType,
) {
	val scope = rememberCoroutineScope()

	val bucketObject = LocalCompositionBucketObject.current
	val onReorderBucketItem = LocalCompositionOnReorderBucketItem.current

	val isSelected = LocalCompositionIsSelected.current

	val isVaultOpened = LocalVaultIsOpened.current

	var bottomBarSpacingPx by remember { mutableStateOf<Int?>(null) }

	val pagerState = rememberPagerState(0)

	val openSheet = LocalCompositionOpenBottomSheet.current

	val setBucketItemObject = LocalCompositionSetBucketItemObject.current

	CompositionLocalProvider(
		LocalCompositionPagerState provides pagerState.currentPage,
		LocalCompositionOnPagerStateChange provides { scope.launch { pagerState.animateScrollToPage(it) } },
	) {
		GenericScaffold(
			modalBottomSheetState = modalBottomSheetState,
			sheetContent = { SheetLayout(bottomSheetType = bottomSheetType) },
			topBar = { TopBar() },
			dialogContent = { BucketDialog() }
		) {
			bucketObject?.let {
				Column(
					modifier = Modifier.fillMaxSize()
				) {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.weight(1f)
					) {
						HorizontalPager(
							state = pagerState,
							count = 4,
							userScrollEnabled = ! isSelected && it.bucketType != BucketType.LINK.name,
							modifier = Modifier.fillMaxSize(),
						) { currentPage ->
							val filteredList = it.bucketItemList.filter {
								if (currentPage == 0) true
								else try {
									BucketItemState.valueOf(it.state).ordinal == currentPage - 1
								} catch (e : Exception) {
									false
								}
							}.filter {
								if (it.isLocked) isVaultOpened
								else true
							}

							when (it.bucketType) {
								BucketType.TODO.name -> TodoScreen(bucketItemList = filteredList)
								BucketType.BOOK.name -> BookGridScreen(bucketItemList = filteredList)
								BucketType.SHOW.name -> ShowGridScreen(bucketItemList = filteredList)
								BucketType.LINK.name -> LinkListScreen(bucketItemList = filteredList, onReorderBucketItemList = onReorderBucketItem)
								else -> null
							}
						}
					}

					BottomBar(modifier = Modifier.onGloballyPositioned {
						bottomBarSpacingPx = it.positionInParent().y.toInt()
					})
				}

				AnimatedVisibility(
					visible = ! isSelected,
					enter = fadeIn(tween(300)),
					exit = fadeOut(tween(300)),
					modifier = Modifier
				) {
					PrimaryButton(
						primaryText = when (it.bucketType) {
							BucketType.TODO.name -> "Add Todo"
							BucketType.BOOK.name -> "Add Book"
							BucketType.SHOW.name -> "Add Show"
							BucketType.LINK.name -> "Add Link"
							else -> "ERROR"
						},
						primaryIcon = when (it.bucketType) {
							BucketType.TODO.name -> R.drawable.ic_todo
							BucketType.BOOK.name -> R.drawable.ic_book_shelf
							BucketType.SHOW.name -> R.drawable.ic_show
							BucketType.LINK.name -> R.drawable.ic_link
							else -> R.drawable.ic_warning
						},
						primaryDescription = when (it.bucketType) {
							BucketType.TODO.name -> "Add a new todo to your bucket"
							BucketType.BOOK.name -> "Add a new book to your bucket"
							BucketType.SHOW.name -> "Add a new show to your bucket"
							BucketType.LINK.name -> "Add a new link to your bucket"
							else -> "ERROR"
						},
						bottomBarSpacingPx = bottomBarSpacingPx,
						onClickPrimary = {
							when (it.bucketType) {
								BucketType.TODO.name -> BucketBottomSheetType.ADD_TODO
								BucketType.BOOK.name -> BucketBottomSheetType.ADD_BOOK
								BucketType.SHOW.name -> BucketBottomSheetType.ADD_SHOW
								BucketType.LINK.name -> BucketBottomSheetType.ADD_LINK
								else -> BucketBottomSheetType.MENU
							}.let {
								if (it == BucketBottomSheetType.ADD_TODO) setBucketItemObject(null)
								openSheet(it)
							}
						}
					)
				}
			} ?: LoadingView()
		}
	}
}

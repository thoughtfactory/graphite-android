package com.syncodec.graphite.presentation.bucket.composable.screen

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.BucketViewModel
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOpenBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionSetBucketItemObject
import com.syncodec.graphite.presentation.bucket.composable.bar.BottomBar
import com.syncodec.graphite.presentation.bucket.composable.bar.TopBar
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.BucketBottomSheetType
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.bucket.composable.dialog.BucketDialog
import com.syncodec.graphite.presentation.bucket.composable.screen.bookScreen.BucketBookScreen
import com.syncodec.graphite.presentation.bucket.composable.screen.linkScreen.BucketLinkScreen
import com.syncodec.graphite.presentation.bucket.composable.screen.showScreen.BucketShowScreen
import com.syncodec.graphite.presentation.bucket.composable.screen.todoScreen.BucketTodoScreen
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.scaffold.GenericButton
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.utils.enumValueOf
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@OptIn(
	ExperimentalMaterialApi::class,
	ExperimentalPagerApi::class
)
@Composable
fun BucketScreen(
	bucketObject : BucketObject?,
	modalBottomSheetState : ModalBottomSheetState,
	bottomSheetType : BucketBottomSheetType,
	onClickBack : () -> Unit = {},
) {
	val scope = rememberCoroutineScope()
	val viewModel : BucketViewModel = koinViewModel()

	val isSelected = LocalCompositionIsSelected.current

	val pagerState = rememberPagerState(0)
	var currentPage = pagerState.currentPage
	LaunchedEffect(key1 = pagerState.currentPage) {
		currentPage = pagerState.currentPage
	}

	val openSheet = LocalCompositionOpenBottomSheet.current

	val setBucketItemObject = LocalCompositionSetBucketItemObject.current

	bucketObject?.let { bucketObject ->
		GenericScaffold(
			modalBottomSheetState = modalBottomSheetState,
			sheetContent = { SheetLayout(bottomSheetType = bottomSheetType) },
			topBar = {
				TopBar(
					title = bucketObject.title,
					isLocked = bucketObject.isLocked,
					isFavourite = bucketObject.isFavourite,
					bucketType = enumValueOf(bucketObject.bucketType, BucketType.UNKNOWN),
					viewState = currentPage,
					isSelected = isSelected,
					onClickFavourite = viewModel::toggleFavourite,
					onClickLock = viewModel::toggleLock,
					onStateChange = { scope.launch { pagerState.animateScrollToPage(it) } },
					onClickBack = onClickBack,
				)
			},
			bottomBar = { BottomBar() },
			isBottomBarVisible = ! isSelected,
			dialogContent = { BucketDialog(bucketObject = bucketObject) },
			isButtonVisible = ! isSelected,
			primaryButton = GenericButton(
				text = when (bucketObject.bucketType) {
					BucketType.TODO.name -> "Add Todo"
					BucketType.BOOK.name -> "Add Book"
					BucketType.SHOW.name -> "Add Show"
					BucketType.LINK.name -> "Add Link"
					BucketType.UNKNOWN.name -> "ERROR"
					else -> "ERROR"
				},
				icon = when (bucketObject.bucketType) {
					BucketType.TODO.name -> R.drawable.ic_todo
					BucketType.BOOK.name -> R.drawable.ic_book_shelf
					BucketType.SHOW.name -> R.drawable.ic_show
					BucketType.LINK.name -> R.drawable.ic_link
					BucketType.UNKNOWN.name -> R.drawable.ic_warning
					else -> R.drawable.ic_warning
				},
				onClick = {
					when (bucketObject.bucketType) {
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
		) {
			when (bucketObject.bucketType) {
				BucketType.TODO.name -> BucketTodoScreen(pagerState = pagerState)
				BucketType.BOOK.name -> BucketBookScreen(pagerState = pagerState)
				BucketType.SHOW.name -> BucketShowScreen(pagerState = pagerState)
				BucketType.LINK.name -> BucketLinkScreen()
				BucketType.UNKNOWN.name -> null
				else -> null
			}
		}
	} ?: LoadingView()
}

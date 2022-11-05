package com.syncodec.graphite.presentation.bucket.composable.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionBucketObject
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionCloseBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnPagerStateChange
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOpenBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionPagerState
import com.syncodec.graphite.presentation.bucket.composable.bar.BottomBar
import com.syncodec.graphite.presentation.bucket.composable.bar.TopBar
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.BucketBottomSheetType
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.bucket.composable.dialog.BucketDialog
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.button.PrimaryButton
import com.syncodec.graphite.utils.LocalVaultIsOpened
import kotlinx.coroutines.launch


@OptIn(
	ExperimentalMaterialApi::class,
	ExperimentalComposeUiApi::class,
	ExperimentalMaterial3Api::class,
	ExperimentalPagerApi::class
)
@Preview
@Composable
fun BucketScreen() {
	val scope = rememberCoroutineScope()

	val bucketObject = LocalCompositionBucketObject.current

	val keyboardController = LocalSoftwareKeyboardController.current

	val isSelected = LocalCompositionIsSelected.current

	val isVaultOpened = LocalVaultIsOpened.current

	var bottomSheetType : BucketBottomSheetType by rememberSaveable { mutableStateOf(BucketBottomSheetType.MENU) }
	val modalBottomSheetState = rememberModalBottomSheetState(
		initialValue = ModalBottomSheetValue.Hidden,
		confirmStateChange = { keyboardController?.hide(); true }
	)

	fun closeSheet() {
		scope.launch { modalBottomSheetState.hide() }
	}

	fun openSheet(_bottomSheetType : BucketBottomSheetType) {
		scope.launch { bottomSheetType = _bottomSheetType; modalBottomSheetState.show() }
	}

	var bottomBarSpacingPx by remember { mutableStateOf(0) }

	val pagerState = rememberPagerState(0)

	CompositionLocalProvider(
		LocalCompositionPagerState provides pagerState.currentPage,
		LocalCompositionOnPagerStateChange provides { scope.launch { pagerState.scrollToPage(it) } },
		LocalCompositionOpenBottomSheet provides ::openSheet,
		LocalCompositionCloseBottomSheet provides ::closeSheet,
	) {
		Crossfade(targetState = bucketObject) { bucket ->
			if (bucket == null) {
				LoadingView()
			} else {
				ModalBottomSheetLayout(
					modifier = Modifier.fillMaxSize(),
					sheetState = modalBottomSheetState,
					sheetElevation = 0.dp,
					sheetBackgroundColor = Color.Transparent,
					sheetContent = {
						SheetLayout(bottomSheetType = bottomSheetType)
					}
				) {
					Scaffold(
						modifier = Modifier.fillMaxSize(),
						topBar = { TopBar() },
					) {
						Box(
							modifier = Modifier
								.fillMaxSize()
								.padding(it)
						) {
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
										userScrollEnabled = ! isSelected && bucketObject?.bucketType != BucketType.LINK.name,
										modifier = Modifier.fillMaxSize(),
									) { currentPage ->
										val filteredList = bucket.bucketItemList.filter {
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

										when (bucket.bucketType) {
											BucketType.TODO.name -> null
											BucketType.BOOK.name -> BookGridScreen(bucketItemList = filteredList)
											BucketType.SHOW.name -> ShowGridScreen(bucketItemList = filteredList)
											BucketType.LINK.name -> LinkListScreen(bucketItemList = filteredList)
											else -> null
										}
									}
								}

								BottomBar(modifier = Modifier.onGloballyPositioned { bottomBarSpacingPx = it.positionInParent().y.toInt() })
							}

							AnimatedVisibility(
								visible = ! isSelected,
								enter = fadeIn(tween(300)),
								exit = fadeOut(tween(300))
							) {
								PrimaryButton(
									primaryText = when (bucket.bucketType) {
										BucketType.TODO.name -> "Add Todo"
										BucketType.BOOK.name -> "Add Book"
										BucketType.SHOW.name -> "Add Show"
										BucketType.LINK.name -> "Add Link"
										else -> "ERROR"
									},
									primaryIcon = when (bucket.bucketType) {
										BucketType.TODO.name -> R.drawable.ic_todo
										BucketType.BOOK.name -> R.drawable.ic_book
										BucketType.SHOW.name -> R.drawable.ic_show
										BucketType.LINK.name -> R.drawable.ic_link
										else -> R.drawable.ic_warning
									},
									primaryDescription = when (bucket.bucketType) {
										BucketType.TODO.name -> "Add a new todo to your bucket"
										BucketType.BOOK.name -> "Add a new book to your bucket"
										BucketType.SHOW.name -> "Add a new show to your bucket"
										BucketType.LINK.name -> "Add a new link to your bucket"
										else -> "ERROR"
									},
									bottomBarSpacingPx = bottomBarSpacingPx,
									onClickPrimary = {
										bottomSheetType = when (bucket.bucketType) {
											BucketType.TODO.name -> BucketBottomSheetType.ADD_TODO
											BucketType.BOOK.name -> BucketBottomSheetType.ADD_BOOK
											BucketType.SHOW.name -> BucketBottomSheetType.ADD_SHOW
											BucketType.LINK.name -> BucketBottomSheetType.ADD_LINK
											else -> BucketBottomSheetType.MENU
										}

										openSheet(bottomSheetType)
									}
								)
							}
						}

						BucketDialog()
					}
				}
			}
		}
	}
}

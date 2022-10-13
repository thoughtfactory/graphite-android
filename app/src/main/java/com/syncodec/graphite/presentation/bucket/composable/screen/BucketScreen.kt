package com.syncodec.graphite.presentation.bucket.composable.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.BucketViewModel
import com.syncodec.graphite.presentation.bucket.composable.bar.BottomBar
import com.syncodec.graphite.presentation.bucket.composable.bar.TopBar
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.BucketBottomSheetType
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.button.PrimaryButton
import kotlinx.coroutines.launch
import com.syncodec.graphite.R


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class, ExperimentalPagerApi::class)
@Composable
fun BucketScreen() {
	val scope = rememberCoroutineScope()

	val viewModel: BucketViewModel = viewModel()

	val _bucketObject by viewModel.bucketObject

	val keyboardController = LocalSoftwareKeyboardController.current

	var bottomSheetType: BucketBottomSheetType by rememberSaveable { mutableStateOf(BucketBottomSheetType.MENU) }
	val modalBottomSheetState = rememberModalBottomSheetState(
		initialValue = ModalBottomSheetValue.Hidden,
		confirmStateChange = { keyboardController?.hide(); true }
	)
	val closeSheet = { scope.launch { modalBottomSheetState.hide() } }
	val openSheet = { scope.launch { modalBottomSheetState.show() } }

	var viewState by rememberSaveable { mutableStateOf(0) }

	var bottomBarSpacingPx by remember { mutableStateOf(0) }

	Crossfade(targetState = _bucketObject) { bucketObject ->
		if (bucketObject == null) {
			LoadingView()
		} else {
			ModalBottomSheetLayout(
				modifier = Modifier.fillMaxSize(),
				sheetState = modalBottomSheetState,
				sheetElevation = 0.dp,
				sheetBackgroundColor = Color.Transparent,
				sheetContent = { SheetLayout(bottomSheetType = bottomSheetType) { closeSheet() } }
			) {
				Scaffold(
					modifier = Modifier.fillMaxSize(),
					topBar = {
						TopBar(
							title = bucketObject.title,
							bucketType = BucketType.BOOK,
							isFavourite = bucketObject.isFavourite,
							isLocked = bucketObject.isLocked,
							viewState = viewState,
							onClickFavourite = { viewModel.toggleFavourite() },
							onClickLock = { viewModel.toggleLock() },
							onStateChange = { viewState = it }
						)
					},
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
									count = 4,
									modifier = Modifier.fillMaxSize(),
								) {
									when (bucketObject.bucketType) {
										BucketType.TODO.name -> null
										BucketType.BOOK.name -> BookGridScreen()
										BucketType.SHOW.name -> ShowGridScreen(it)
										BucketType.LINK.name -> LinkListScreen()
									}
								}
							}

							BottomBar(
								modifier = Modifier
									.onGloballyPositioned {
										bottomBarSpacingPx = it.positionInParent().y.toInt()
									}
							) {
								bottomSheetType = it
								openSheet()
							}
						}

						PrimaryButton(
							primaryText = when (bucketObject.bucketType) {
								BucketType.TODO.name -> "Add Todo"
								BucketType.BOOK.name -> "Add Book"
								BucketType.SHOW.name -> "Add Show"
								BucketType.LINK.name -> "Add Link"
								else -> "ERROR"
							},
							primaryIcon = when (bucketObject.bucketType) {
								BucketType.TODO.name -> R.drawable.ic_todo
								BucketType.BOOK.name -> R.drawable.ic_book
								BucketType.SHOW.name -> R.drawable.ic_show
								BucketType.LINK.name -> R.drawable.ic_link
								else -> R.drawable.ic_warning
							},
							primaryDescription = when (bucketObject.bucketType) {
								BucketType.TODO.name -> "Add a new todo to your bucket"
								BucketType.BOOK.name -> "Add a new book to your bucket"
								BucketType.SHOW.name -> "Add a new show to your bucket"
								BucketType.LINK.name -> "Add a new link to your bucket"
								else -> "ERROR"
							},
							bottomBarSpacingPx = bottomBarSpacingPx,
							onClickPrimary = {
								bottomSheetType = when (bucketObject.bucketType) {
									BucketType.TODO.name -> BucketBottomSheetType.ADD_TODO
									BucketType.BOOK.name -> BucketBottomSheetType.ADD_BOOK
									BucketType.SHOW.name -> BucketBottomSheetType.ADD_SHOW
									BucketType.LINK.name -> BucketBottomSheetType.ADD_LINK
									else -> BucketBottomSheetType.MENU
								}
								openSheet()
							}
						)
					}
				}
			}
		}
	}
}

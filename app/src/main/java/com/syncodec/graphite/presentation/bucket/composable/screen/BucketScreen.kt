package com.syncodec.graphite.presentation.bucket.composable.screen

import androidx.compose.animation.Crossfade
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
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.composable.bar.BottomBar
import com.syncodec.graphite.presentation.bucket.composable.bar.TopBar
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.BucketBottomSheetType
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.button.PrimaryButton
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class, ExperimentalPagerApi::class)
@Composable
fun BucketScreen(
	bucketObject: BucketObject?
) {
	val scope = rememberCoroutineScope()


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
					SheetLayout(
						bucketObject = bucket,
						bottomSheetType = bottomSheetType
					) { closeSheet() }
				}
			) {
				Scaffold(
					modifier = Modifier.fillMaxSize(),
					topBar = {
						TopBar(
							title = bucket.title,
							bucketType = BucketType.BOOK,
							isFavourite = bucket.isFavourite,
							isLocked = bucket.isLocked,
							viewState = viewState,
							onClickFavourite = {  },
							onClickLock = {  },
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
									when (bucket.bucketType) {
										BucketType.TODO.name -> null
										BucketType.BOOK.name -> BookGridScreen(bucketObject = bucket)
										BucketType.SHOW.name -> ShowGridScreen(bucketObject = bucket, viewState = viewState)
										BucketType.LINK.name -> LinkListScreen(bucketObject = bucket)
										else -> null
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
								openSheet()
							}
						)
					}
				}
			}
		}
	}
}

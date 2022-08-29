package com.syncodec.graphite.presentation.bucket

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.composable.bar.BottomBar
import com.syncodec.graphite.presentation.bucket.composable.bar.TopBar
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.BucketBottomSheetType
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.bucket.composable.screen.BookListScreen
import com.syncodec.graphite.presentation.bucket.composable.screen.ShowListScreen
import com.syncodec.graphite.presentation.custom.LoadingView
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BucketScreen() {
	val scope = rememberCoroutineScope()

	val viewModel: BucketViewModel = viewModel()

	val _bucketObject by viewModel.bucketObject

	val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
	var bottomSheetType: BucketBottomSheetType by rememberSaveable { mutableStateOf(BucketBottomSheetType.MENU) }

	val closeSheet = { scope.launch { modalBottomSheetState.hide() } }

	val openSheet = { scope.launch { modalBottomSheetState.show() } }

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
					topBar = { TopBar(title = bucketObject.title, bucketType = BucketType.BOOK) },
					bottomBar = {
						BottomBar {
							when (bucketObject.bucketType) {
								BucketType.TODO.name -> null
								BucketType.BOOK.name -> bottomSheetType = BucketBottomSheetType.ADD_BOOK
								BucketType.SHOW.name -> bottomSheetType = BucketBottomSheetType.ADD_SHOW
								BucketType.LINK.name -> null
							}
							openSheet()
						}
					}
				) {
					Box(modifier = Modifier.padding(it)) {
						when(bucketObject.bucketType) {
							BucketType.TODO.name -> null
							BucketType.BOOK.name -> BookListScreen()
							BucketType.SHOW.name -> ShowListScreen()
							BucketType.LINK.name -> null
						}
					}
				}
			}
		}
	}
}

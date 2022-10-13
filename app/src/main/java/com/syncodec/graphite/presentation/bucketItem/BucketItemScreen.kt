package com.syncodec.graphite.presentation.bucketItem

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucketItem.composable.bar.BottomBar
import com.syncodec.graphite.presentation.bucketItem.composable.bar.TopBar
import com.syncodec.graphite.presentation.bucketItem.composable.dialog.DeleteDialog
import com.syncodec.graphite.presentation.bucketItem.composable.screen.BookScreen
import com.syncodec.graphite.presentation.bucketItem.composable.screen.showScreen.ShowScreen
import com.syncodec.graphite.presentation.common.ErrorView
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.utils.Status
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BucketItemScreen() {
	val activity : BucketItemActivity = LocalContext.current as BucketItemActivity
	val scope = rememberCoroutineScope()

	val viewModel : BucketItemViewModel = viewModel()

	val bucketType by viewModel.bucketType
	val isNew by viewModel.isNew

	val movieId by viewModel.movieId
	val tvId by viewModel.tvId
	val currentState by viewModel.state

	val status by viewModel.status

	val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
	val softwareKeyboardController = LocalSoftwareKeyboardController.current

	val openSheet = { scope.launch { softwareKeyboardController?.hide();modalBottomSheetState.show() } }
	val closeSheet = { scope.launch { modalBottomSheetState.hide() } }

	val bucketItem by viewModel.bucketItemObject
	val isFavourite by viewModel.isFavourite
	val isLocked by viewModel.isLocked

	var showDeleteDialog by remember { mutableStateOf(false) }

	Scaffold(
		modifier = Modifier.fillMaxSize(),
		topBar = {
			TopBar(
				title = bucketItem?.title,
				isNew = isNew ?: true,
				isLocked = isLocked ?: false,
				isFavourite = isFavourite ?: false,
				onClickSave = viewModel::putBucketItem,
				onClickLock = viewModel::onClickLock,
				onClickFavourite = viewModel::onClickFavourite,
			) { activity.onBackPressed() }
		},
		bottomBar = {
			BottomBar(
				onClickShare = {},
				onClickDelete = { showDeleteDialog = true },
				onClickMove = {}
			)
		}
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(it)
		) {
			Crossfade(
				targetState = status,
				animationSpec = tween(300)
			) {
				when(it){
					Status.INIT -> LoadingView()
					Status.LOADING -> LoadingView()
					Status.LOADED -> {
						when (bucketType) {
							BucketType.TODO -> null
							BucketType.BOOK -> BookScreen(
								currentState = currentState?.ordinal ?: 0,
								onChangeState = viewModel::onChangeState,
							)
							BucketType.SHOW -> ShowScreen(
								movieId = movieId,
								tvId = tvId,
								currentState = currentState?.ordinal ?: 0,
								onChangeState = viewModel::onChangeState,
							)
							BucketType.LINK -> null
							BucketType.UNKNOWN -> null
							else -> null
						}
					}
					Status.ERROR -> ErrorView()
				}
			}

			DeleteDialog(
				showDeleteDialog = showDeleteDialog,
				onDismiss = { showDeleteDialog = false },
				onDelete = {  }
			)
		}
	}
}

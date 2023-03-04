package com.syncodec.graphite.presentation.bucketItem.composable.screen

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.ShowType
import com.syncodec.graphite.presentation.bucketItem.composable.bar.BottomBar
import com.syncodec.graphite.presentation.bucketItem.composable.bar.TopBar
import com.syncodec.graphite.presentation.bucketItem.composable.dialog.BucketItemDialog
import com.syncodec.graphite.presentation.bucketItem.composable.dialog.BucketItemDialogType
import com.syncodec.graphite.presentation.bucketItem.composable.screen.bookScreen.BookScreen
import com.syncodec.graphite.presentation.bucketItem.composable.screen.movieScreen.MovieScreen
import com.syncodec.graphite.presentation.bucketItem.composable.screen.tvScreen.TvScreen
import com.syncodec.graphite.presentation.common.ErrorView
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.utils.AuthenticatorScreen
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalIsAuthenticated


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BucketItemScreen(
	bucketItemObject : BucketItemObject? = null,
	bucketType : BucketType? = null,
	showType : ShowType? = null,
	isSaved : Boolean? = false,
	isFavourite : Boolean = false,
	isLocked : Boolean = false,
	onClickSave : () -> Unit = {},
	onClickFavourite : () -> Unit = {},
	onClickLock : () -> Unit = {},
	onChangeState : (Int) -> Unit = {},
	onShare : () -> Unit = {},
	onDelete : () -> Unit = {},
	onClickBack : () -> Unit = {},
) {
	val context = LocalContext.current
	val isAuthenticated = LocalIsAuthenticated.current
	val onAuthenticationAction = LocalAuthenticatorAction.current

	val currentState = bucketItemObject?.state.let { state -> BucketItemState.values().find { it.name == state }?.ordinal ?: 0 }

	var showDeleteDialog by remember { mutableStateOf(false) }

	fun openDialog(dialogType : BucketItemDialogType) = when (dialogType) {
		BucketItemDialogType.DELETE -> showDeleteDialog = true
	}

	fun closeDialog(dialogType : BucketItemDialogType) = when (dialogType) {
		BucketItemDialogType.DELETE -> showDeleteDialog = false
	}

	Crossfade(targetState = isSaved) {
		if (it == null) {
			LoadingView()
		} else {
			GenericScaffold(
				topBar = {
					TopBar(
						isSaved = it,
						isFavourite = isFavourite,
						isLocked = isLocked,
						onClickSave = onClickSave,
						onClickFavourite = onClickFavourite,
						onClickLock = { if (isAuthenticated) onClickLock() else onAuthenticationAction(AuthenticatorScreen.Authenticate) },
						onClickBack = onClickBack,
					)
				},
				bottomBar = {
					BottomBar(
						isSaved = it,
						onClickShare = onShare,
						onClickDelete = { openDialog(BucketItemDialogType.DELETE) },
						onClickAddReminder = {
							Toast.makeText(context, "Add reminder and due dates are under development. Stay tuned...", Toast.LENGTH_SHORT).show()
						}
					)
				},
				dialogContent = {
					BucketItemDialog(
						showDeleteDialog = showDeleteDialog,
						onDelete = onDelete,
						closeDialog = ::closeDialog,
					)
				}
			) {
				Crossfade(
					targetState = bucketType,
					animationSpec = tween(durationMillis = 300)
				) {
					when (it) {
						null -> LoadingView()
						BucketType.BOOK -> BookScreen(
							currentState = currentState,
							onChangeState = onChangeState,
						)

						BucketType.SHOW -> when (showType) {
							ShowType.MOVIE -> MovieScreen(
								currentState = currentState,
								onChangeState = onChangeState,
							)

							ShowType.TV -> TvScreen(
								currentState = currentState,
								onChangeState = onChangeState,
							)

							else -> ErrorView()
						}

						else -> ErrorView()
					}
				}
			}
		}
	}
}

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
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucketItem.composable.bar.BottomBar
import com.syncodec.graphite.presentation.bucketItem.composable.bar.TopBar
import com.syncodec.graphite.presentation.bucketItem.composable.dialog.DeleteDialog
import com.syncodec.graphite.presentation.bucketItem.composable.screen.BookScreen
import com.syncodec.graphite.presentation.bucketItem.composable.screen.MovieScreen
import com.syncodec.graphite.presentation.bucketItem.composable.screen.TvScreen
import com.syncodec.graphite.presentation.common.ErrorView
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.utils.Status
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BucketItemScreen(
	viewModel : BucketItemViewModel
) {
	val activity : BucketItemActivity = LocalContext.current as BucketItemActivity
	val scope = rememberCoroutineScope()

	val bucketType by viewModel.bucketType
	val isNew by viewModel.isNew

	val currentState by viewModel.state

	val status by viewModel.status

	val bookKey by viewModel.bookKey
	val bookTitle by viewModel.bookTitle
	val bookAuthors = viewModel.bookAuthorList
	val bookDescription by viewModel.bookDescription
	val bookPageCount by viewModel.bookPageCount
	val bookPublishedDate by viewModel.bookFirstPublishYear

	val tvId by viewModel.tvId
	val tvGenres = viewModel.tvGenres
	val tvHomepage by viewModel.tvHomepage
	val tvName by viewModel.tvName
	val tvNumberOfSeasons by viewModel.tvNumberOfSeasons
	val tvNumberOfEpisodes by viewModel.tvNumberOfEpisodes
	val tvOverview by viewModel.tvOverview
	val tvFirstAirDate by viewModel.tvFirstAirDate
	val tvTagline by viewModel.tvTagline

	val movieId by viewModel.movieId
	val movieGenres = viewModel.movieGenres
	val movieImdbId by viewModel.movieImdbId
	val movieOriginalTitle by viewModel.movieOriginalTitle
	val movieOverview by viewModel.movieOverview
	val movieReleaseDate by viewModel.movieReleaseDate
	val movieRuntime by viewModel.movieRuntime
	val movieTagline by viewModel.movieTagline
	val movieTitle by viewModel.movieTitle

	val thumbnail by viewModel.thumbnail


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
				when (it) {
					Status.INIT -> LoadingView()
					Status.LOADING -> LoadingView()
					Status.LOADED -> {
						when (bucketType) {
							BucketType.TODO -> null
							BucketType.BOOK -> BookScreen(
								bookKey = bookKey,
								bookTitle = bookTitle,
								bookAuthors = bookAuthors.filterNotNull(),
								bookDescription = bookDescription,
								bookPageCount = bookPageCount,
								bookPublishedDate = bookPublishedDate,
								thumbnail = thumbnail,
								currentState = currentState?.ordinal ?: 0,
								onChangeState = viewModel::onChangeState,
							)

							BucketType.SHOW -> when {
								movieId != null -> MovieScreen(
									movieId = movieId,
									movieGenres = movieGenres.filterNotNull(),
									movieImdbId = movieImdbId,
									movieOriginalTitle = movieOriginalTitle,
									movieOverview = movieOverview,
									movieReleaseDate = movieReleaseDate,
									movieRuntime = movieRuntime,
									movieTagline = movieTagline,
									movieTitle = movieTitle,
									thumbnail = thumbnail,
									currentState = currentState?.ordinal ?: 0,
									onChangeState = viewModel::onChangeState
								)

								tvId != null -> TvScreen(
									tvId = tvId,
									tvGenres = tvGenres.filterNotNull(),
									tvName = tvName,
									tvNumberOfSeasons = tvNumberOfSeasons,
									tvNumberOfEpisodes = tvNumberOfEpisodes,
									tvOverview = tvOverview,
									tvFirstAirDate = tvFirstAirDate,
									tvTagline = tvTagline,
									thumbnail = thumbnail,
									currentState = currentState?.ordinal ?: 0,
									onChangeState = viewModel::onChangeState
								)

								else -> ErrorView()
							}

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
				onDelete = { }
			)
		}
	}
}

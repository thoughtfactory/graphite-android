package com.syncodec.graphite.presentation.bucketItem

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.ShowType
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionBookAuthorList
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionBookCoverI
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionBookDescription
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionBookFirstPublishYear
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionBookKey
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionBookPageCount
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionBookTitle
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionBucketType
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionIsFavourite
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionIsLocked
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionIsNew
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieAdult
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieGenres
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieHomepage
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieId
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieImdbId
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieOriginalLanguage
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieOriginalTitle
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieOverview
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMoviePosterPath
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieReleaseDate
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieRuntime
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieTagline
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieTitle
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionOnChangeState
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionOnClickFavourite
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionOnClickLock
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionOnClickNavigationIcon
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionOnClickSave
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionOnDelete
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionOnShare
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionShowBookInfoDialog
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionShowDeleteDialog
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionShowShowInfoDialog
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionShowType
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionState
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionStatus
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionThumbnail
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionTitle
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionTvAdult
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionTvFirstAirDate
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionTvGenres
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionTvHomepage
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionTvId
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionTvName
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionTvNumberOfEpisodes
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionTvNumberOfSeasons
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionTvOriginalLanguage
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionTvOriginalName
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionTvOverview
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionTvPosterPath
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionTvTagline
import com.syncodec.graphite.presentation.bucketItem.composable.screen.BucketItemScreen
import com.syncodec.graphite.presentation.common.ErrorView
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.LocalCompositionCloseDialog
import com.syncodec.graphite.presentation.common.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.common.dialog.DialogType
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Authenticator
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalVaultIsOpened
import com.syncodec.graphite.utils.Status
import com.syncodec.graphite.utils.tone
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.types.RealmUUID


@AndroidEntryPoint
class BucketItemActivity : ComponentActivity() {

	val viewModel by viewModels<BucketItemViewModel>()

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		val hasIsNew = intent.hasExtra(Extra.Companion.Constant.IS_NEW.name)
		val hasBucketId = intent.hasExtra(Extra.Companion.Constant.BUCKET_ID.name)
		val hasBucketType = intent.hasExtra(Extra.Companion.Constant.BUCKET_TYPE.name)

		if (hasIsNew && hasBucketId && hasBucketType) {
			val isNew = intent.getBooleanExtra(Extra.Companion.Constant.IS_NEW.name, false)
			val bucketId = intent.getByteArrayExtra(Extra.Companion.Constant.BUCKET_ID.name)?.let { RealmUUID.from(it) }
			val bucketItemId = intent.getByteArrayExtra(Extra.Companion.Constant.BUCKET_ITEM_ID.name)?.let { RealmUUID.from(it) }
			val _bucketType = intent.getStringExtra(Extra.Companion.Constant.BUCKET_TYPE.name)

			if (bucketId == null || _bucketType == null) {
				finish()
			} else {
				try {
					val bucketType = BucketType.valueOf(_bucketType)
					if (bucketType == BucketType.UNKNOWN) {
						finish()
					} else {
						if (isNew) {
							viewModel.initData(bucketId, bucketType, intent)
						} else {
							if (bucketItemId == null || bucketId == null) {
								finish()
							} else {
								viewModel.loadData(bucketItemId, bucketId)
							}
						}
					}
				} catch (e : Exception) {
//					e.printStackTrace()
					finish()
				}
			}

		} else {
			finish()
		}

		setContent {
			BaseContent {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)
				systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1))

				val isVaultOpened = LocalVaultIsOpened.current
				val authenticator = LocalAuthenticatorAction.current

				val status by viewModel.status

				val isNew by viewModel.isNew

				val objectId by viewModel.objectId
				val bucketType by viewModel.bucketType
				val title by viewModel.title
				val state by viewModel.state
				val thumbnail by viewModel.thumbnail
				val isLocked by viewModel.isLocked
				val isFavourite by viewModel.isFavourite

				val bookKey by viewModel.bookKey
				val bookTitle by viewModel.bookTitle
				val bookCoverI by viewModel.bookCoverI
				val bookAuthorList = viewModel.bookAuthorList
				val bookDescription by viewModel.bookDescription
				val bookPageCount by viewModel.bookPageCount
				val bookFirstPublishYear by viewModel.bookFirstPublishYear

				val showType by viewModel.showType

				val movieId by viewModel.movieId
				val movieAdult by viewModel.movieAdult
				val movieGenres = viewModel.movieGenres
				val movieHomepage by viewModel.movieHomepage
				val movieImdbId by viewModel.movieImdbId
				val movieOriginalLanguage by viewModel.movieOriginalLanguage
				val movieOriginalTitle by viewModel.movieOriginalTitle
				val movieOverview by viewModel.movieOverview
				val moviePosterPath by viewModel.moviePosterPath
				val movieReleaseDate by viewModel.movieReleaseDate
				val movieRuntime by viewModel.movieRuntime
				val movieTitle by viewModel.movieTitle
				val movieTagline by viewModel.movieTagline

				val tvId by viewModel.tvId
				val tvAdult by viewModel.tvAdult
				val tvFirstAirDate by viewModel.tvFirstAirDate
				val tvGenres = viewModel.tvGenres
				val tvHomepage by viewModel.tvHomepage
				val tvNumberOfSeasons by viewModel.tvNumberOfSeasons
				val tvNumberOfEpisodes by viewModel.tvNumberOfEpisodes
				val tvOriginalLanguage by viewModel.tvOriginalLanguage
				val tvName by viewModel.tvName
				val tvOriginalName by viewModel.tvOriginalName
				val tvOverview by viewModel.tvOverview
				val tvPosterPath by viewModel.tvPosterPath
				val tvTagline by viewModel.tvTagline

				var showShowInfoDialog by remember { mutableStateOf(false) }
				var showBookInfoDialog by remember { mutableStateOf(false) }
				var showDeleteDialog by remember { mutableStateOf(false) }

				fun openDialog(dialogType : DialogType) {
					when (dialogType) {
						DialogType.SHOW_INFO -> showShowInfoDialog = true
						DialogType.BOOK_INFO -> showBookInfoDialog = true
						DialogType.DELETE -> showDeleteDialog = true
						else -> null
					}
				}

				fun closeDialog(dialogType : DialogType) {
					when (dialogType) {
						DialogType.SHOW_INFO -> showShowInfoDialog = false
						DialogType.BOOK_INFO -> showBookInfoDialog = false
						DialogType.DELETE -> showDeleteDialog = false
						else -> null
					}
				}

				CompositionLocalProvider(
					LocalCompositionStatus provides status,
					LocalCompositionIsNew provides isNew,
					LocalCompositionBucketType provides bucketType,
					LocalCompositionTitle provides title,
					LocalCompositionState provides state?.ordinal,
					LocalCompositionThumbnail provides thumbnail,
					LocalCompositionIsLocked provides isLocked,
					LocalCompositionIsFavourite provides isFavourite,
					LocalCompositionOnClickFavourite provides viewModel::onToggleFavourite,
					LocalCompositionOnClickLock provides { if (isVaultOpened) viewModel.onToggleLocked() else authenticator(Authenticator.AUTHENTICATE) },
					LocalCompositionOnClickNavigationIcon provides { this.finish() },
					LocalCompositionOnChangeState provides viewModel::onToggleState,
					LocalCompositionOnClickSave provides viewModel::putBucketItem,
					LocalCompositionBookKey provides bookKey,
					LocalCompositionBookTitle provides bookTitle,
					LocalCompositionBookCoverI provides bookCoverI,
					LocalCompositionBookAuthorList provides bookAuthorList,
					LocalCompositionBookDescription provides bookDescription,
					LocalCompositionBookPageCount provides bookPageCount,
					LocalCompositionBookFirstPublishYear provides bookFirstPublishYear,
					LocalCompositionShowType provides showType,
					LocalCompositionMovieId provides movieId,
					LocalCompositionMovieAdult provides movieAdult,
					LocalCompositionMovieGenres provides movieGenres,
					LocalCompositionMovieHomepage provides movieHomepage,
					LocalCompositionMovieImdbId provides movieImdbId,
					LocalCompositionMovieOriginalLanguage provides movieOriginalLanguage,
					LocalCompositionMovieOriginalTitle provides movieOriginalTitle,
					LocalCompositionMovieOverview provides movieOverview,
					LocalCompositionMoviePosterPath provides moviePosterPath,
					LocalCompositionMovieReleaseDate provides movieReleaseDate,
					LocalCompositionMovieRuntime provides movieRuntime,
					LocalCompositionMovieTitle provides movieTitle,
					LocalCompositionMovieTagline provides movieTagline,
					LocalCompositionTvId provides tvId,
					LocalCompositionTvAdult provides tvAdult,
					LocalCompositionTvFirstAirDate provides tvFirstAirDate,
					LocalCompositionTvGenres provides tvGenres,
					LocalCompositionTvHomepage provides tvHomepage,
					LocalCompositionTvNumberOfSeasons provides tvNumberOfSeasons,
					LocalCompositionTvNumberOfEpisodes provides tvNumberOfEpisodes,
					LocalCompositionTvOriginalLanguage provides tvOriginalLanguage,
					LocalCompositionTvName provides tvName,
					LocalCompositionTvOriginalName provides tvOriginalName,
					LocalCompositionTvOverview provides tvOverview,
					LocalCompositionTvPosterPath provides tvPosterPath,
					LocalCompositionTvTagline provides tvTagline,
					LocalCompositionShowShowInfoDialog provides showShowInfoDialog,
					LocalCompositionShowBookInfoDialog provides showBookInfoDialog,
					LocalCompositionShowDeleteDialog provides showDeleteDialog,
					LocalCompositionOpenDialog provides ::openDialog,
					LocalCompositionCloseDialog provides ::closeDialog,
					LocalCompositionOnDelete provides {
						Intent().apply {
							putExtra(Extra.Companion.Constant.INTENT_ACTION.name, Extra.Companion.IntentAction.DELETE.name)
							putExtra(Extra.Companion.Constant.OBJECT_ID.name, objectId?.bytes)
							setResult(Activity.RESULT_OK, this)
							this@BucketItemActivity.finish()
						}
					},
					LocalCompositionOnShare provides this::onShare
				) {
					Crossfade(
						targetState = status,
						animationSpec = tween(durationMillis = 300)
					) {
						when (it) {
							Status.INIT -> LoadingView()
							Status.LOADING -> LoadingView()
							Status.LOADED -> BucketItemScreen()
							Status.ERROR -> ErrorView()
						}
					}
				}
			}
		}
	}

	private fun onShare() {
		var shareText = when(viewModel.bucketType.value) {
			BucketType.BOOK -> "I'm reading ${viewModel.bookTitle.value} by ${viewModel.bookAuthorList.joinToString(", ")}. Find it on https://openlibrary.org${viewModel.bookKey.value}"
			BucketType.SHOW -> when(viewModel.showType.value) {
				ShowType.MOVIE -> "I'm watching ${viewModel.movieTitle.value}. Find it on https://www.themoviedb.org/movie/${viewModel.movieId.value}"
				ShowType.TV -> "I'm watching ${viewModel.tvName.value}. Find it on https://www.themoviedb.org/tv/${viewModel.tvId.value}"
				else -> ""
			}
			else -> ""
		}

		Intent(Intent.ACTION_SEND).apply {
			type = "text/html"
			putExtra(Intent.EXTRA_SUBJECT, shareText)
//			putExtra(Intent.EXTRA_TEXT, Html.fromHtml(shareText, Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST))
			putExtra(Intent.EXTRA_TEXT, shareText)

			if (resolveActivity(this@BucketItemActivity.packageManager) != null) startActivity(Intent.createChooser(this, "Share using"))
			else Toast.makeText(this@BucketItemActivity, "No app found on your phone which can perform this action", Toast.LENGTH_SHORT).show()
		}
	}
}

package com.syncodec.graphite.presentation.bucketItem2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.ShowType
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionBookAuthorList
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionBookCoverI
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionBookDescription
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionBookFirstPublishYear
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionBookKey
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionBookPageCount
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionBookTitle
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionBucketType
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionIsFavourite
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionIsLocked
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionIsNew
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionMovieAdult
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionMovieGenres
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionMovieHomepage
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionMovieId
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionMovieImdbId
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionMovieOriginalLanguage
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionMovieOriginalTitle
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionMovieOverview
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionMoviePosterPath
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionMovieReleaseDate
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionMovieRuntime
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionMovieTagline
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionMovieTitle
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionOnChangeState
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionOnClickFavourite
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionOnClickLock
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionOnClickNavigationIcon
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionOnClickSave
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionShowType
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionState
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionStatus
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionThumbnail
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionTitle
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionTvAdult
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionTvFirstAirDate
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionTvGenres
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionTvHomepage
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionTvId
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionTvName
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionTvNumberOfEpisodes
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionTvNumberOfSeasons
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionTvOriginalLanguage
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionTvOriginalName
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionTvOverview
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionTvPosterPath
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionTvTagline
import com.syncodec.graphite.presentation.bucketItem2.composable.screen.BucketItemScreen
import com.syncodec.graphite.presentation.common.ErrorView
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.types.ObjectId


@AndroidEntryPoint
class BucketItemActivity2 : ComponentActivity() {

	val viewModel by viewModels<BucketItemViewModel>()

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		val hasIsNew = intent.hasExtra(Extra.Companion.Constant.IS_NEW.name)
		val hasBucketId = intent.hasExtra(Extra.Companion.Constant.BUCKET_ID.name)
		val hasBucketType = intent.hasExtra(Extra.Companion.Constant.BUCKET_TYPE.name)

		if (hasIsNew && hasBucketId && hasBucketType) {
			val isNew = intent.getBooleanExtra(Extra.Companion.Constant.IS_NEW.name, false)
			val bucketId = intent.getStringExtra(Extra.Companion.Constant.BUCKET_ID.name)?.let { ObjectId.from(it) }
			val bucketItemId = intent.getStringExtra(Extra.Companion.Constant.BUCKET_ITEM_ID.name)?.let { ObjectId.from(it) }
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
					e.printStackTrace()
					finish()
				}
			}

		} else {
			finish()
		}

		setContent {
			BaseContent {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)
				systemUiController.setNavigationBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)

				val status by viewModel.status

				val isNew by viewModel.isNew

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
					LocalCompositionOnClickLock provides viewModel::onToggleLocked,
					LocalCompositionOnClickNavigationIcon provides { this.onBackPressed() },
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
}

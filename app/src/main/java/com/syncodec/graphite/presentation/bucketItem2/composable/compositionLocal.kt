package com.syncodec.graphite.presentation.bucketItem2.composable

import android.graphics.Bitmap
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.Genre
import com.syncodec.graphite.di.network.ShowType
import com.syncodec.graphite.utils.Status


val LocalCompositionStatus = compositionLocalOf { Status.INIT }

val LocalCompositionIsNew = compositionLocalOf<Boolean?> { null }

val LocalCompositionBucketType = compositionLocalOf<BucketType?> { null }
val LocalCompositionTitle = compositionLocalOf<String?> { null }
val LocalCompositionState = compositionLocalOf<Int?> { null }
val LocalCompositionThumbnail = compositionLocalOf<Bitmap?> { null }
val LocalCompositionIsFavourite = compositionLocalOf<Boolean?> { null }
val LocalCompositionIsLocked = compositionLocalOf<Boolean?> { null }

val LocalCompositionOnClickFavourite = compositionLocalOf { {} }
val LocalCompositionOnClickLock = compositionLocalOf { {} }
val LocalCompositionOnClickNavigationIcon = compositionLocalOf { {} }
val LocalCompositionOnChangeState = compositionLocalOf<(Int) -> Unit> { {} }
val LocalCompositionOnClickSave = compositionLocalOf { {} }

val LocalCompositionBookKey = compositionLocalOf<String?> { null }
val LocalCompositionBookTitle = compositionLocalOf<String?> { null }
val LocalCompositionBookCoverI = compositionLocalOf<String?> { null }
val LocalCompositionBookAuthorList = compositionLocalOf<SnapshotStateList<String>> { mutableStateListOf() }
val LocalCompositionBookDescription = compositionLocalOf<String?> { null }
val LocalCompositionBookPageCount = compositionLocalOf<Int?> { null }
val LocalCompositionBookFirstPublishYear = compositionLocalOf<String?> { null }

val LocalCompositionShowType = compositionLocalOf<ShowType?> { null }

val LocalCompositionMovieId = compositionLocalOf<String?> { null }
val LocalCompositionMovieAdult = compositionLocalOf<Boolean?> { null }
val LocalCompositionMovieGenres = compositionLocalOf<SnapshotStateList<Genre>> { mutableStateListOf() }
val LocalCompositionMovieHomepage = compositionLocalOf<String?> { null }
val LocalCompositionMovieImdbId = compositionLocalOf<String?> { null }
val LocalCompositionMovieOriginalLanguage = compositionLocalOf<String?> { null }
val LocalCompositionMovieOriginalTitle = compositionLocalOf<String?> { null }
val LocalCompositionMovieOverview = compositionLocalOf<String?> { null }
val LocalCompositionMoviePosterPath = compositionLocalOf<String?> { null }
val LocalCompositionMovieReleaseDate = compositionLocalOf<String?> { null }
val LocalCompositionMovieRuntime = compositionLocalOf<Int?> { null }
val LocalCompositionMovieTitle = compositionLocalOf<String?> { null }
val LocalCompositionMovieTagline = compositionLocalOf<String?> { null }

val LocalCompositionTvId = compositionLocalOf<String?> { null }
val LocalCompositionTvAdult = compositionLocalOf<Boolean?> { null }
val LocalCompositionTvFirstAirDate = compositionLocalOf<String?> { null  }
val LocalCompositionTvGenres = compositionLocalOf<SnapshotStateList<Genre>> { mutableStateListOf() }
val LocalCompositionTvHomepage = compositionLocalOf<String?> { null }
val LocalCompositionTvNumberOfSeasons = compositionLocalOf<Int?> { null }
val LocalCompositionTvNumberOfEpisodes = compositionLocalOf<Int?> { null }
val LocalCompositionTvOriginalLanguage = compositionLocalOf<String?> { null }
val LocalCompositionTvName = compositionLocalOf<String?> { null }
val LocalCompositionTvOriginalName = compositionLocalOf<String?> { null }
val LocalCompositionTvOverview = compositionLocalOf<String?> { null }
val LocalCompositionTvPosterPath = compositionLocalOf<String?> { null }
val LocalCompositionTvTagline = compositionLocalOf<String?> { null }

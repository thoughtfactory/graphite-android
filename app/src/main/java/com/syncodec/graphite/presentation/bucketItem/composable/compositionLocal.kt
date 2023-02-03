package com.syncodec.graphite.presentation.bucketItem.composable

import android.graphics.Bitmap
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.syncodec.graphite.di.network.Genre


val LocalCompositionState = compositionLocalOf<Int?> { null }
val LocalCompositionThumbnail = compositionLocalOf<Bitmap?> { null }

val LocalCompositionOnChangeState = compositionLocalOf<(Int) -> Unit> { {} }

val LocalCompositionBookKey = compositionLocalOf<String?> { null }

val LocalCompositionMovieId = compositionLocalOf<String?> { null }
val LocalCompositionMovieImdbId = compositionLocalOf<String?> { null }
val LocalCompositionMovieOriginalTitle = compositionLocalOf<String?> { null }
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

val LocalCompositionShowShowInfoDialog = compositionLocalOf { false }
val LocalCompositionShowBookInfoDialog = compositionLocalOf { false }
val LocalCompositionShowDeleteDialog = compositionLocalOf { false }

val LocalCompositionOnDelete = compositionLocalOf { {} }
val LocalCompositionOnShare = compositionLocalOf { {} }

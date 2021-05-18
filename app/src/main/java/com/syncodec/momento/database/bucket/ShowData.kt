package com.syncodec.momento.database.bucket

import com.syncodec.momento.bucketComponent.modalBottomSheet.ShowType


data class CreatorData(
	val id: String,
	val creditId: String,
	val name: String
)

data class SeasonData(
	val airDate: String,
	val noEpisode: Int?,
	val id: String,
	val name: String,
	val overview: String?,
	val posterPath: String?,
	val seasonNo: Int
)

data class TvData(
	val adult: Boolean,
	val backdropPath: String?,
	val creatorDataList: List<CreatorData>,
	val episodeRunTime: List<Int>,
	val firstAirDate: String?,
	val genreIds: List<Int>,
	val homepage: String?,
	val id: String,
	val inProduction: Boolean?,
	val name: String,
	val noEpisode: Int?,
	val noSeason: Int?,
	val originalLanguage: String?,
	val overview: String?,
	val popularity: Double?,
	val posterPath: String?,
//	val seasonDataList: List<SeasonData>,
	val showType: ShowType,
	val status: String?,
	val tagline: String?,
	val type: String?,
	val voteAverage: Double,
	val voteCount: Int
)

data class MovieData(
	val adult: Boolean,
	val backdropPath: String?,
	val genreIds: List<Int>,
	val homepage: String?,
	val id: String,
	val imdbId: String?,
	val originalLanguage: String?,
	val originalTitle: String?,
	val overview: String?,
	val popularity: Int?,
	val posterPath: String,
	val releaseDate: String?,
	val runtime: Int?,
	val status: String?,
	val tagline: String?,
	val title: String,
	val voteAverage: Float,
	val voteCount: Int
)

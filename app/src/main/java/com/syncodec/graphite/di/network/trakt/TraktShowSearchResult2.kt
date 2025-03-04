package com.syncodec.graphite.di.network.trakt

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Keep
@Serializable
sealed class TraktShowSearchResult {
    abstract val showType: String?
    abstract val score: Float?
    abstract val showSearchResult: ShowSearchResult?

    @Keep
    @Serializable
    data class TraktMovieSearchResult(
        @SerialName(value = "type") override val showType: String? = null,
        @SerialName(value = "score") override val score: Float? = null,
        @SerialName(value = "movie") override val showSearchResult: ShowSearchResult? = null,
    ): TraktShowSearchResult()

    @Keep
    @Serializable
    data class TraktSeriesSearchResult(
        @SerialName(value = "type") override val showType: String? = null,
        @SerialName(value = "score") override val score: Float? = null,
        @SerialName(value = "show") override val showSearchResult: ShowSearchResult? = null,
    ): TraktShowSearchResult()
}

@Keep
@Serializable
data class ShowSearchResult(
    @SerialName(value = "title") val title: String? = null,
    @SerialName(value = "year") val year: Int? = null,
    @SerialName(value = "ids") val ids: TraktIDs? = null,
    @SerialName(value = "images") val traktImages: TraktImages? = null
)

package com.syncodec.graphite.di.network.trakt

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Keep
@Serializable
data class TraktIDs(
    @SerialName(value = "trakt") val trakt: Int? = null,
    @SerialName(value = "slug") val slug: String? = null,
    @SerialName(value = "imdb") val imdb: String? = null,
    @SerialName(value = "tmdb") val tmdb: Int? = null,
    @SerialName(value = "tvdb") val tvdb: Int? = null,
)

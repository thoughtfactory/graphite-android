package com.syncodec.graphite.di.network.trakt

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Keep
@Serializable
data class TraktMovieData(
    @SerialName(value = "title") val title: String? = null,
    @SerialName(value = "year") val year: Int? = null,
    @SerialName(value = "ids") val ids: TraktIDs? = null,
    @SerialName(value = "tagline") val tagline: String? = null,
    @SerialName(value = "overview") val overview: String? = null,
    @SerialName(value = "released") val released: String? = null,
    @SerialName(value = "runtime") val runtime: Int? = null,
    @SerialName(value = "country") val country: String? = null,
    @SerialName(value = "trailer") val trailer: String? = null,
    @SerialName(value = "homepage") val homepage: String? = null,
    @SerialName(value = "genres") val genres: List<String> = listOf(),
)

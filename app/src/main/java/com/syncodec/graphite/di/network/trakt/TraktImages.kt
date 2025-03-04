package com.syncodec.graphite.di.network.trakt

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Keep
@Serializable
data class TraktImages(
    @SerialName(value = "fanart") val fanArt: List<String> = listOf(),
    @SerialName(value = "poster") val poster: List<String> = listOf(),
    @SerialName(value = "logo") val logo: List<String> = listOf(),
    @SerialName(value = "clearart") val clearArt: List<String> = listOf(),
    @SerialName(value = "banner") val banner: List<String> = listOf(),
    @SerialName(value = "thumb") val thumb: List<String> = listOf(),
)

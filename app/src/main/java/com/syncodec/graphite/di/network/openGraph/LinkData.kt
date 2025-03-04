package com.syncodec.graphite.di.network.openGraph

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Keep
@Serializable
data class LinkData(
    @SerialName(value = "title") val title: String? = null,
    @SerialName(value = "description") val description: String? = null,
    @SerialName(value = "url") val url: String? = null,
    @SerialName(value = "imagePath") val imagePath: String? = null,
    @SerialName(value = "imageBase64") val imageBase64: String? = null,
    @SerialName(value = "siteName") val siteName: String? = null,
    @SerialName(value = "type") val type: String? = null,
)

package com.syncodec.graphite.di.network.openLibrary

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class OLBookData(
    @SerialName(value = "description") val description: String? = null,
    @SerialName(value = "title") val title: String? = null,
    @SerialName(value = "key") val key: String? = null,
)

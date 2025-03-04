package com.syncodec.graphite.di.network.openLibrary

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Keep
@Serializable
data class OpenLibraryTitleSearchResult2(
    @SerialName(value = "numFound") val numFound: Int?,
    @SerialName(value = "start") val start: Int?,
    @SerialName(value = "docs") val docs: List<OLBookSearchResult> = listOf(),
)

@Keep
@Serializable
data class OLBookSearchResult(
    @SerialName(value = "author_name") val authorName: List<String> = listOf(),
    @SerialName(value = "cover_i") val coverI: Int? = null,
    @SerialName(value = "first_publish_year") val firstPublishedYear: Int? = null,
    @SerialName(value = "key") val key: String? = null,
    @SerialName(value = "number_of_pages_median") val numberOfPages: Int? = null,
    @SerialName(value = "title") val title: String? = null,
)

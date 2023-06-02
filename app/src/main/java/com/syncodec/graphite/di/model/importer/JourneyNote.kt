package com.syncodec.graphite.di.model.importer

import androidx.annotation.Keep
import kotlinx.serialization.Serializable


@Keep
@Serializable
data class JourneyNote(
	val text: String? = null,
	val dateModified: Long? = null,
	val dateJournal: Long? = null,
	val id: String? = null,
	val previewText: String? = null,
	val address: String? = null,
	val lat: Double? = null,
	val lon: Double? = null,
	val favourite: Boolean? = null,
	val photos: List<String?>? = null,
	val tags: List<String?>? = null,
)

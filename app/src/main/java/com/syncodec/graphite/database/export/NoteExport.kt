package com.syncodec.graphite.database.export

import androidx.annotation.Keep
import com.google.android.gms.maps.model.LatLng

@Keep
data class NoteExport(
	val key: String,
	val createdTimestamp: Long,
	val modifiedTimestamp: Long,
	val userTimestamp: Long,
	val timezone: String,
	val chapterPath: List<String>,
	val notebookKey: String,
	val title: String?,
	val content: String?,
	val latLng: LatLng?,
	val address: String?,
	val attachmentKeyList: List<String>,
	val tagList: List<String>,
	val version: Int = 1
)

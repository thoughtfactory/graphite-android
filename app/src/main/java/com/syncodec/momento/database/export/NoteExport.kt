package com.syncodec.momento.database.export

import com.syncodec.momento.database.note.LocationData

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
	val location: LocationData?,
	val address: String?,
	val attachmentKey: List<String>
)

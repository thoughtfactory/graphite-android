package com.syncodec.momento.database.note

import androidx.room.ColumnInfo

data class NoteTimelineData(
	@ColumnInfo(name = "key")
	val key: String,
	@ColumnInfo(name = "user_timestamp")
	val timestamp: Long
)

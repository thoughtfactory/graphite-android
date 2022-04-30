package com.syncodec.graphite.database.tag

import androidx.room.ColumnInfo
import androidx.room.Entity


@Entity(tableName = "tag_key_table", primaryKeys = ["tag", "key"])
data class TagKeyDbEntry(
	@ColumnInfo(name = "tag")
	val tag: String,

	@ColumnInfo(name = "key")
	val key: String
)

package com.syncodec.graphite.database.tag

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tag_table")
data class TagDbEntry(
	@PrimaryKey(autoGenerate = false)
	@ColumnInfo(name = "tag")
	val tag: String,
)

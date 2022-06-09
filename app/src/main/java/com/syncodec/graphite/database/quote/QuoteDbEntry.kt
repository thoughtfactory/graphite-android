package com.syncodec.graphite.database.quote

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quote_table")
data class QuoteDbEntry(
	@PrimaryKey(autoGenerate = false)
	@ColumnInfo(name = "date")
	val date: String,

	@ColumnInfo(name = "quote")
	val quote: String,

	@ColumnInfo(name = "author")
	val author: String?,

	@ColumnInfo(name = "author_link")
	val authorLink: String?,

	@ColumnInfo(name = "bg_link")
	val bgLink: String?,

	@ColumnInfo(name = "bg_cred")
	val bgCred: String?,

	@ColumnInfo(name = "bg_cred_link")
	val bgCredLink: String?,

	@ColumnInfo(name = "bg_provider")
	val bgProvider: String?,

	@ColumnInfo(name = "bg_provider_link")
	val bgProviderLink: String?,

	@ColumnInfo(name = "special")
	val special: String?,

	@ColumnInfo(name = "is_favourite")
	var isFavourite: Boolean,
)

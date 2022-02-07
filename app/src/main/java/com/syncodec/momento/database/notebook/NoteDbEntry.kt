package com.syncodec.momento.database.notebook

import kotlin.properties.Delegates

data class NoteDbEntry(
	val primaryKey: String,
	val timezoneOffset: Int
) {
	var createdTimestamp by Delegates.notNull<Long>()
	var modifiedTimestamp by Delegates.notNull<Long>()
	var userTimestamp by Delegates.notNull<Long>()
	var contentThumbnail: String? = null
	var title: String? = null
	var isFavourite: Boolean = false
	var isArchived: Boolean = false
	var isLocked: Boolean = false
}

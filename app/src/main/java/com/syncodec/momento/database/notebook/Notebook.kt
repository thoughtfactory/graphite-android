package com.syncodec.momento.database.notebook

import com.syncodec.momento.database.diary.Note
import kotlin.properties.Delegates

data class Notebook(
	var primaryKey: String,
) {
	var createdTimestamp by Delegates.notNull<Long>()
	var modifiedTimestamp by Delegates.notNull<Long>()
	var contentThumbnail: String? = null
	lateinit var title: String
	var description: String? = null
	var color: Int? = null
	var chapterMap: MutableMap<String, Chapter> = mutableMapOf()
	var noteMap: MutableMap<String, Note> = mutableMapOf()
	var isFavourite: Boolean = false
	var isArchived: Boolean = false
	var isLocked: Boolean = false
}

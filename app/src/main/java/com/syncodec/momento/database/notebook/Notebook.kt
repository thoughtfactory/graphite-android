package com.syncodec.momento.database.notebook

import kotlin.properties.Delegates

data class Notebook(
	var primaryKey: String,
) {
	var createdTimestamp by Delegates.notNull<Long>()
	var modifiedTimestamp by Delegates.notNull<Long>()
	var contentThumbnail: String? = null
	lateinit var title: String
	var description: String? = null
	var color: Long? = null
	var chapterMap: MutableMap<String, Chapter> = mutableMapOf()
	var noteList: MutableList<NoteDbEntry> = mutableListOf()
	var isFavourite: Boolean = false
	var isArchived: Boolean = false
	var isLocked: Boolean = false
}

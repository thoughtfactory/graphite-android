package com.syncodec.momento.database.notebook

import kotlin.properties.Delegates

data class Chapter(
	val primaryKey: String,
	val notebookKey: String,
	val notebookRoute: MutableList<String>
) {
	var createdTimestamp by Delegates.notNull<Long>()
	var modifiedTimestamp by Delegates.notNull<Long>()
	lateinit var title: String
	var description: String? = null
}

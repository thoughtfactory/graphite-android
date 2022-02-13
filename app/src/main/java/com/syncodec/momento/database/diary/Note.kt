package com.syncodec.momento.database.diary

import android.location.Location
import kotlin.properties.Delegates

data class Note(
	val primaryKey: String,
	val timezoneOffset: Int
) {
	var createdTimestamp by Delegates.notNull<Long>()
	var modifiedTimestamp by Delegates.notNull<Long>()
	var userTimestamp by Delegates.notNull<Long>()
	var contentThumbnail: String? = null
	var mood: Int = 0
	var title: String? = null
	var content: String? = null
	var location: Location? = null
	var address: String? = null
	var weatherData: WeatherData? = null
	var attachmentKeyList: MutableList<String> = mutableListOf()
	var notebookKey: String? = null
	var notebookRoute: MutableList<String>? = null
	var isFavourite: Boolean = false
	var isArchived: Boolean = false
	var isLocked: Boolean = false
}

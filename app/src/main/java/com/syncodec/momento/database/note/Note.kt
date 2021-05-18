package com.syncodec.momento.database.note

import kotlin.properties.Delegates

data class Note(
	val primaryKey: String,
	val timezoneOffset: Int
) {
	var createdTimestamp by Delegates.notNull<Long>()
	var modifiedTimestamp by Delegates.notNull<Long>()
	var userTimestamp by Delegates.notNull<Long>()
	var title: String? = null
	var contentThumbnail: String? = null
	var content: String? = null
	lateinit var notebookKey: String
	var chapterPath: MutableList<String> = mutableListOf()
	var attachmentKeyList: MutableList<String> = mutableListOf()
	var attachmentThumbnail: String? = null
	var attachmentCount: Int = 0
	var location: LocationData? = null
	var address: String? = null
	var weatherData: WeatherData? = null
	var mood: Int = 0
	var isFavourite: Boolean = false
	var isArchived: Boolean = false
	var isLocked: Boolean = false
	var deletedTimestamp: Long = -1
}

data class LocationData(
	val latitude: Double? = null,
	val longitude: Double? = null,
	val bearing: Float? = null,
	val altitude: Double? = null,
	val speed: Float? = null
)

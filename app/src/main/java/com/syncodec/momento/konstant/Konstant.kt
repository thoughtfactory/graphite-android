package com.syncodec.momento.konstant

class Konstant {
	companion object {

		const val ATTACHMENT_PRIMARY_KEY_LENGTH = 8

		enum class Konstant {
			PRIMARY_KEY,
			BUCKET_KEY,
			BUCKET_ITEM_KEY,
			BUCKET_TYPE,
			BUCKET_ITEM_DATA,
			NOTEBOOK_KEY,
			CHAPTER_KEY,
			COMPONENT_TYPE,
			TITLE,
			IS_VIEWER,
			DIARY_KEY
		}

		val monthName: List<String> = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
		val weekName: List<String> = listOf("S", "M", "T", "W", "T", "F", "S")
	}
}

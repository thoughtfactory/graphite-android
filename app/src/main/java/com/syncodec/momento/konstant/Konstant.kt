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
			TITLE,
			IS_VIEWER,
			NOTE_KEY,
			IS_NEW,
			DO_DELETE
		}

		val monthName: List<String> = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
		val weekName: List<String> = listOf("S", "M", "T", "W", "T", "F", "S")

		val genreIdMap: Map<Int, String> = mapOf(
			28 to "action",
			12 to "adventure",
			16 to "animation",
			35 to "comedy",
			80 to "crime",
			99 to "documentary",
			18 to "drama",
			10751 to "family",
			14 to "fantasy",
			36 to "history",
			27 to "horror",
			10402 to "music",
			9648 to "mystery",
			10749 to "romance",
			878 to "scifi",
			10770 to "tvmovie",
			53 to "thriller",
			10752 to "war",
			37 to "western"
		)
	}
}

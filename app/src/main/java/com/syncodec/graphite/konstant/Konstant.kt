package com.syncodec.graphite.konstant

import com.google.android.gms.maps.model.LatLng
import com.syncodec.graphite.R

class Konstant {
	companion object {

		enum class Konstant {
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
			IS_NOTE,
			SHOW_ARCHIVED,
			SHOW_LOCKED,
			DO_DELETE
		}

		val monthName: List<String> = listOf(
			"January",
			"February",
			"March",
			"April",
			"May",
			"June",
			"July",
			"August",
			"September",
			"October",
			"November",
			"December"
		)

		val monthNameShort: List<String> = listOf(
			"Jan",
			"Feb",
			"Mar",
			"Apr",
			"May",
			"Jun",
			"Jul",
			"Aug",
			"Sep",
			"Oct",
			"Nov",
			"Dec"
		)

		val weekNameInitial: List<String> = listOf("S", "M", "T", "W", "T", "F", "S")

		val weekNameShort: List<String> = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")


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

		val ilMap: Map<LatLng, Int> = mapOf(
			LatLng(48.864716, 2.349014) to R.drawable.il_map_1,
			LatLng(53.726669, -127.647621) to R.drawable.il_map_6,
			LatLng(27.173891, 78.042068) to R.drawable.il_map_2,
			LatLng(-19.002846, 46.460938) to R.drawable.il_map_4,
			LatLng(-46.2418834,-77.7089894) to R.drawable.il_map_5,
			LatLng(-18.156290, 147.485962) to R.drawable.il_map_3,
		)
	}
}

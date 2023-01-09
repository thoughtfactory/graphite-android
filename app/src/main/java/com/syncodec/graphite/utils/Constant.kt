package com.syncodec.graphite.utils

import androidx.compose.ui.graphics.Color
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketType


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

val bucketTypeToIcon: Map<BucketType, Int> = mapOf(
	BucketType.TODO to R.drawable.ic_todo,
	BucketType.BOOK to R.drawable.ic_book_shelf,
	BucketType.SHOW to R.drawable.ic_show,
	BucketType.LINK to R.drawable.ic_link,
)

val bucketItemNameMap: Map<BucketType, String> = mapOf(
	BucketType.TODO to "Todo",
	BucketType.BOOK to "Books",
	BucketType.SHOW to "Movies / Series",
	BucketType.LINK to "Link",
)

enum class SortOn {
	TITLE,
	TIMESTAMP,
	MODIFIED,
	PRIORITY,
	COMPLETED,
	DUE,
	CREATED,
	DONE,
	CUSTOM
}

enum class SortBy {
	ASCENDING,
	DESCENDING,
}

enum class ViewType {
	LIST,
	GRID,
}

enum class LocationState {
	INIT,
	NO_PERMISSION,
	LOADING,
	NOT_PRO,
	DISABLED,
	LATLNG,
	ONLY_LATLNG,
	ONLY_ADDRESS,
	SUCCESS,
	KNOWN_ERROR,
	REMOVED,
	UNKNOW_ERROR,
}

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

val colorList: List<Color> = listOf(
	Color(0xFFBB6464),
	Color(0xFF9ADCFF),
	Color(0xFFFFB2A6),
	Color(0xFFB4CFB0),
	Color(0xFF655D8A),
	Color(0xFFD885A3),
	Color(0xFF694E4E),
)

val imageList: List<Int> = listOf(
	R.drawable.img_1,
	R.drawable.img_2,
	R.drawable.img_3,
	R.drawable.img_4,
	R.drawable.img_5,
	R.drawable.img_6,
	R.drawable.img_7,
)

val mimeTypeIconMap: Map<String, Int> = mapOf(
	"image" to R.drawable.ic_gallery,
	"video" to R.drawable.ic_file,
	"audio" to R.drawable.ic_file_audio,
	"text" to R.drawable.ic_file,
	"application" to R.drawable.ic_file,
)

val mimeSubTypeIconMap: Map<String, Int> = mapOf(
	"image/jpeg" to R.drawable.ic_file,
	"image/png" to R.drawable.ic_file,
	"image/gif" to R.drawable.ic_file,
	"image/webp" to R.drawable.ic_file,
	"image/heic" to R.drawable.ic_file,
	"video/mp4" to R.drawable.ic_file,
	"video/webm" to R.drawable.ic_file,
	"video/ogg" to R.drawable.ic_file,
	"audio/aac" to R.drawable.ic_file,
	"audio/mp3" to R.drawable.ic_file,
	"audio/wav" to R.drawable.ic_file,
	"audio/flac" to R.drawable.ic_file,
	"audio/aac" to R.drawable.ic_file,
	"audio/ogg" to R.drawable.ic_file,
	"audio/midi" to R.drawable.ic_file,
	"audio/x-midi" to R.drawable.ic_file,

	"text/plain" to R.drawable.ic_file,
	"text/html" to R.drawable.ic_file,
	"text/css" to R.drawable.ic_file,
	"text/javascript" to R.drawable.ic_file,
	"text/csv" to R.drawable.ic_file,

	"application/zip" to R.drawable.ic_file,
	"application/gz" to R.drawable.ic_file,
	"application/x-rar-compressed" to R.drawable.ic_file,
	"application/x-7z-compressed" to R.drawable.ic_file,
	"application/x-bzip2" to R.drawable.ic_file,
	"application/x-gzip" to R.drawable.ic_file,
	"application/x-tar" to R.drawable.ic_file,
	"application/x-rar" to R.drawable.ic_file,

	"application/vnd.amazon.ebook" to R.drawable.ic_file,
	"application/epub+zip" to R.drawable.ic_file,
	"application/epub" to R.drawable.ic_file,

	"application/pdf" to R.drawable.ic_file,
	"application/msword" to R.drawable.ic_file,
	"application/vnd.openxmlformats-officedocument.wordprocessingml.document" to R.drawable.ic_file,

	"application/octet-stream" to R.drawable.ic_file,
)

class Extra {
	companion object {
		enum class Constant {
			INTENT_ACTION,
			OBJECT_ID,
			OBJECT_TYPE,
			IS_NEW,
			CHAPTER_ID,
			NOTE_ID,
			SHOW_ALL,
			BUCKET_ID,
			BUCKET_ITEM_ID,
			BUCKET_TYPE,
			BUCKET_EXTRA_DATA,
			BOOK_ID,
			MOVIE_ID,
			TV_ID,
			FILTER
		}

		enum class ObjectType {
			ATTAHCMENT,
			BUCKET_ITEM,
			BUCKET,
			CHAPTER,
			NOTE,
			TAG
		}

		enum class Filter {
			SINGLE_READ,
			READ_CHAPTER
		}

		enum class IntentAction {
			DELETE
		}
	}
}

enum class Status {
	INIT,
	LOADING,
	LOADED,
	ERROR
}

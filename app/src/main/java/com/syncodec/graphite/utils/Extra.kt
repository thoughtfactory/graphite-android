package com.syncodec.graphite.utils

import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.model.LatLng


val monthName : List<String> = listOf(
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

val monthNameShort : List<String> = listOf(
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

val weekNameInitial : List<String> = listOf("S", "M", "T", "W", "T", "F", "S")

val weekNameShort : List<String> = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

val bucketTypeToIcon : Map<BucketType, Int> = mapOf(
	BucketType.TODO to R.drawable.ic_todo,
	BucketType.BOOK to R.drawable.ic_book_shelf,
	BucketType.SHOW to R.drawable.ic_show,
	BucketType.LINK to R.drawable.ic_link,
)

val bucketItemNameMap : Map<BucketType, String> = mapOf(
	BucketType.TODO to "Todo",
	BucketType.BOOK to "Books",
	BucketType.SHOW to "Movies / Series",
	BucketType.LINK to "Link",
)

enum class SortOn {
	Title,
	Timestamp,
	Modified,
	PRIORITY,
	COMPLETED,
	DUE,
	CREATED,
	DONE,
	Custom
}

enum class SortBy {
	Ascending,
	Descending,
}

enum class ViewType {
	List,
	Grid,
}

sealed class LocationData {
	object Init : LocationData()
	object Loading : LocationData()
	data class SuccessOnlyLatLng(val latLng : LatLng) : LocationData()
	data class SuccessOnlyAddress(val address : String) : LocationData()
	data class Success(val latLng : LatLng, val address : String) : LocationData()
	object SuccessNoData : LocationData()
	object NoPermission : LocationData()
	data class Error(val message : String) : LocationData()
}

val genreIdMap : Map<Int, String> = mapOf(
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

val imageList : List<Int> = listOf(
	R.drawable.img_1,
	R.drawable.img_2,
	R.drawable.img_3,
	R.drawable.img_4,
	R.drawable.img_5,
	R.drawable.img_6,
	R.drawable.img_7,
)

val mimeTypeIconMap : Map<String, Int> = mapOf(
	"image" to R.drawable.ic_gallery,
	"video" to R.drawable.ic_file,
	"audio" to R.drawable.ic_file_audio,
	"text" to R.drawable.ic_file,
	"application" to R.drawable.ic_file,
)

enum class MimeType {
	IMAGE,
	VIDEO,
	AUDIO,
	TEXT,
	APPLICATION,
}

val mimeSubTypeIconMap : Map<String, Int> = mapOf(
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
		enum class Extra {
			INTENT_ACTION,
			OBJECT_ID,
			OBJECT_TYPE,
			IsNew,
			ParentId,
			ChapterId,
			NoteId,
			TagId,
			ShowAll,
			BUCKET_ID,
			BUCKET_ITEM_ID,
			BUCKET_TYPE,
			BUCKET_EXTRA_DATA,
			SHOW_TYPE,
			BOOK_ID,
			MOVIE_ID,
			TV_ID,
			Filter,
			ExplorerType
		}

		enum class ExplorerType {
			Atlas,
			Attachment,
			Calendar,
			Search,
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
			SingleRead,
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

enum class LoaderStatus {
	Init,
	Error,
	Loading,
	LoadedEmpty,
	Loaded,
}

sealed class ContentStatus<out T> {
	object Init : ContentStatus<Nothing>()
	object Loading : ContentStatus<Nothing>()
	object LoadedEmpty : ContentStatus<Nothing>()
	class Loaded<T>(val data : T) : ContentStatus<T>()
	data class Error(val message : String) : ContentStatus<Nothing>()

	val dataOrNull : T?
		get() = if (this is Loaded) this.data else null

	override fun equals(other : Any?) : Boolean {
		if (this === other) return true
		if (this is Init && other !is Init) return false
		if (this is Loading && other !is Loading) return false
		if (this is LoadedEmpty && other !is LoadedEmpty) return false
		if (this is Error && other !is Error) return false
		if (this is Loaded && other !is Loaded<*>) return false
		if (other !is ContentStatus<*>) return false

		if (dataOrNull != other.dataOrNull) return false

		if (this is Error && other is Error) {
			if (message != other.message) return false
		}

		if (this is Loaded && other is Loaded<*>) {
			if (data != other.data) return false
		}


		return true
	}

	override fun hashCode() : Int {
		return dataOrNull?.hashCode() ?: 0
	}
}

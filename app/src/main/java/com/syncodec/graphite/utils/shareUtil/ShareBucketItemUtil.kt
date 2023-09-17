package com.syncodec.graphite.utils.shareUtil

import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.network.MovieData
import com.syncodec.graphite.di.network.ShowType
import com.syncodec.graphite.di.network.TvData


object ShareBucketItemUtil {

	private const val BOOK_BASE_URL = "https://openlibrary.org"
	private const val MOVIE_BASE_URL = "https://www.themoviedb.org/movie"
	private const val TV_BASE_URL = "https://www.themoviedb.org/tv"

	fun getTodoItemShareText(bucketItemList: List<BucketItemObject>): String {
		return bucketItemList
			.map { "${getCheckString(it.state)} ${it.title}" }
			.fold("") { acc, s -> "$acc$s\n" }
	}

	fun getBookItemShareText(bucketItemList: List<BucketItemObject>): String {
		return bucketItemList
			.map { "${it.title} - $BOOK_BASE_URL${it.getBookData()?.key}" }           //  key is an OpenLibrary Id. It already contains /  -> /works/OL5819456W
			.fold("") { acc, s -> "$acc$s\n" }
	}

	fun getShowItemShareText(bucketItemList: List<BucketItemObject>): String {
		return bucketItemList
			.map { "${it.title} - ${getShowItemShareText(it.getShowData())}" }
			.fold("") { acc, s -> "$acc$s\n" }
	}

	private fun getShowItemShareText(showData: BucketItemObject.Companion.BucketItemData.ShowData?): String {
		return when (showData?.type) {
			ShowType.TV -> getTvItemShareText(showData.tvData)
			ShowType.MOVIE -> getMovieItemShareText(showData.movieData)
			else -> ""
		}
	}

	private fun getMovieItemShareText(movieData: MovieData?): String {
		return movieData?.id?.let { "$MOVIE_BASE_URL/${it}" } ?: ""
	}

	private fun getTvItemShareText(tvData: TvData?): String {
		return tvData?.id?.let { "$TV_BASE_URL/${it}" } ?: ""
	}

	fun getLinkItemShareText(bucketItemList: List<BucketItemObject>): String {
		return bucketItemList
			.map { "${it.title} - ${it.getOpenGraphResult()?.url ?: it.key}}" }           //  key is an OpenLibrary Id. It already contains /  ->
			.fold("") { acc, s -> "$acc$s\n" }
	}

	private fun getCheckString(state: String): String {
		return when (state) {
			BucketItemState.ALPHA.name -> "[]"
			BucketItemState.BETA.name -> "[-]"
			BucketItemState.GAMMA.name -> "[x]"
			else -> "[]"
		}
	}
}

package com.syncodec.graphite.presentation.bucketItem2

import android.util.Log
import androidx.annotation.WorkerThread
import com.syncodec.graphite.di.model.importer.ThumbnailData
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemBook
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemShow
import com.syncodec.graphite.di.network.NetworkResponse
import com.syncodec.graphite.di.network.openLibrary.OLBookSearchResult
import com.syncodec.graphite.di.network.openLibrary.OpenLibraryApi2
import com.syncodec.graphite.di.network.trakt.TraktApi
import com.syncodec.graphite.di.network.trakt.TraktShowSearchResult
import com.syncodec.graphite.utils.DataLoader
import kotlin.uuid.ExperimentalUuidApi


class BookFetcher(
    private val openLibraryApi2: OpenLibraryApi2
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend fun fetchBookData(olBookSearchResult: OLBookSearchResult, callback: (DataLoader<BucketItemBoxDecrypted>) -> Unit) {
        Log.d(TAG, "fetchBookData")

        if (olBookSearchResult.key == null) {
            callback(DataLoader.NoData())
            return
        } else {
            openLibraryApi2.getBookData(bookKey = olBookSearchResult.key) { networkResponse ->
                when (networkResponse) {
                    is NetworkResponse.Init -> callback(DataLoader.Init())
                    is NetworkResponse.Loading -> callback(DataLoader.Loading())
                    is NetworkResponse.Error -> callback(DataLoader.Error(exception = networkResponse.exception, message = networkResponse.message))
                    is NetworkResponse.Success -> {
                        val olBookData = networkResponse.data
                        val bucketItemData = BucketItemBook.OpenLibrary(
                            key = olBookData.key,
                            title = olBookData.title,
                            description = olBookData.description,
                            coverI = olBookSearchResult.coverI,
                            authorList = olBookSearchResult.authorName,
                            firstPublishYear = olBookSearchResult.firstPublishedYear,
                            numberOfPages = olBookSearchResult.numberOfPages,
                        )
                        val bucketItemBox = BucketItemBoxDecrypted.newInstance.copy(bucketItemData = bucketItemData)
                        callback(DataLoader.Loaded(data = bucketItemBox))
                    }
                }
            }
        }
    }

    suspend fun fetchBookCover(olBookSearchResult: OLBookSearchResult, callback: (DataLoader<ThumbnailData.Bitmap>) -> Unit) {
        val coverI = olBookSearchResult.coverI
        if (coverI == null) callback(DataLoader.NoData())
        else openLibraryApi2.getBookCoverImage(coverI = olBookSearchResult.coverI) { networkResponse ->
            when (networkResponse) {
                is NetworkResponse.Init -> callback(DataLoader.Init())
                is NetworkResponse.Loading -> callback(DataLoader.Loading())
                is NetworkResponse.Error -> callback(DataLoader.Error(exception = networkResponse.exception, message = networkResponse.message))
                is NetworkResponse.Success -> callback(DataLoader.Loaded(data = ThumbnailData.Bitmap(data = networkResponse.data)))
            }
        }
    }

    companion object {
        const val TAG = "BookFetcher"
    }
}

class ShowFetcher(
    private val traktApi: TraktApi,
) {
    suspend fun fetchShowData(traktShowSearchResult: TraktShowSearchResult, callback: (DataLoader<BucketItemBoxDecrypted>) -> Unit) {
        Log.d(TAG, "fetchShowData")

        val traktId = traktShowSearchResult.showSearchResult?.ids?.trakt
        if (traktId == null) {
            callback(DataLoader.NoData())
        } else when (traktShowSearchResult) {
            is TraktShowSearchResult.TraktMovieSearchResult -> fetchMovieSummary(traktId = traktId, callback = callback)
            is TraktShowSearchResult.TraktSeriesSearchResult -> fetchSeriesSummary(traktId = traktId, callback = callback)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @WorkerThread
    private suspend fun fetchMovieSummary(traktId: Int, callback: (DataLoader<BucketItemBoxDecrypted>) -> Unit) {
        traktApi.getMovieSummary(traktId = traktId) { networkResponse ->
            when (networkResponse) {
                is NetworkResponse.Init -> callback(DataLoader.Init())
                is NetworkResponse.Loading -> callback(DataLoader.Loading())
                is NetworkResponse.Error -> callback(DataLoader.Error(exception = networkResponse.exception, message = networkResponse.message))
                is NetworkResponse.Success -> {
                    val traktMovieData = networkResponse.data
                    val bucketItemData = BucketItemShow.TraktMovie(
                        title = traktMovieData.title,
                        year = traktMovieData.year,
                        ids = traktMovieData.ids,
                        tagline = traktMovieData.tagline,
                        overview = traktMovieData.overview,
                        released = traktMovieData.released,
                        runtime = traktMovieData.runtime,
                        country = traktMovieData.country,
                        trailer = traktMovieData.trailer,
                        homepage = traktMovieData.homepage,
                        genres = traktMovieData.genres,
                    )
                    val bucketItemBox = BucketItemBoxDecrypted.newInstance.copy(bucketItemData = bucketItemData)
                    callback(DataLoader.Loaded(data = bucketItemBox))
                }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @WorkerThread
    private suspend fun fetchSeriesSummary(traktId: Int, callback: (DataLoader<BucketItemBoxDecrypted>) -> Unit) {
        traktApi.getSeriesSummary(traktId = traktId) { networkResponse ->
            when (networkResponse) {
                is NetworkResponse.Init -> callback(DataLoader.Init())
                is NetworkResponse.Loading -> callback(DataLoader.Loading())
                is NetworkResponse.Error -> callback(DataLoader.Error(exception = networkResponse.exception, message = networkResponse.message))
                is NetworkResponse.Success -> {
                    val traktSeriesData = networkResponse.data
                    val bucketItemData = BucketItemShow.TraktSeries(
                        title = traktSeriesData.title,
                        year = traktSeriesData.year,
                        ids = traktSeriesData.ids,
                        tagline = traktSeriesData.tagline,
                        overview = traktSeriesData.overview,
                        firstAired = traktSeriesData.firstAired,
                        runtime = traktSeriesData.runtime,
                        certification = traktSeriesData.certification,
                        country = traktSeriesData.country,
                        trailer = traktSeriesData.trailer,
                        homepage = traktSeriesData.homepage,
                        genres = traktSeriesData.genres,
                    )
                    val bucketItemBox = BucketItemBoxDecrypted.newInstance.copy(bucketItemData = bucketItemData)
                    callback(DataLoader.Loaded(data = bucketItemBox))
                }
            }
        }
    }

    suspend fun fetchShowPoster(traktShowSearchResult: TraktShowSearchResult, callback: (DataLoader<ThumbnailData.Bitmap>) -> Unit) {
        val posterPath = traktShowSearchResult.showSearchResult?.traktImages?.poster?.firstOrNull()
        if (posterPath == null) callback(DataLoader.NoData())
        else traktApi.getShowCoverImage(posterPath = posterPath) { networkResponse ->
            when (networkResponse) {
                is NetworkResponse.Init -> callback(DataLoader.Init())
                is NetworkResponse.Loading -> callback(DataLoader.Loading())
                is NetworkResponse.Error -> callback(DataLoader.Error(exception = networkResponse.exception, message = networkResponse.message))
                is NetworkResponse.Success -> callback(DataLoader.Loaded(data = ThumbnailData.Bitmap(data = networkResponse.data)))
            }
        }
    }

    companion object {
        const val TAG = "ShowFetcher"
    }
}

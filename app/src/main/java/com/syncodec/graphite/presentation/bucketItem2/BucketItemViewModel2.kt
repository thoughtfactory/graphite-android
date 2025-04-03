package com.syncodec.graphite.presentation.bucketItem2

import android.net.Uri
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.di.model.importer.ThumbnailData
import com.syncodec.graphite.di.modelObjectBox.BucketBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.BucketBoxEncrypted
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemBook
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemData
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemShow
import com.syncodec.graphite.di.network.NetworkResponse
import com.syncodec.graphite.di.network.openLibrary.OLBookSearchResult
import com.syncodec.graphite.di.network.openLibrary.OpenLibraryApi2
import com.syncodec.graphite.di.network.trakt.TraktApi
import com.syncodec.graphite.di.network.trakt.TraktShowSearchResult
import com.syncodec.graphite.di.secureRepository.BoxRepository
import com.syncodec.graphite.utils.DataLoader
import com.syncodec.graphite.utils.FileUtil.toByteArray
import com.syncodec.graphite.utils.alice2.Alice2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import org.koin.android.annotation.KoinViewModel
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalUuidApi::class)
@KoinViewModel
class BucketItemViewModel2(
    private val baseApplication: BaseApplication,
    private val boxRepository: BoxRepository,
    private val alice2: Alice2,
    private val openLibraryApi2: OpenLibraryApi2,
    private val traktApi: TraktApi,
) : ViewModel() {

    private var _isDataFetched = false

    private val _isDataSaved: MutableStateFlow<Boolean?> = MutableStateFlow(value = null)
    val isDataSaved: StateFlow<Boolean?> = _isDataSaved.asStateFlow()

    private val _parent: MutableStateFlow<DataLoader<BucketBoxDecrypted>> = MutableStateFlow(value = DataLoader.Init())
    val parent: StateFlow<DataLoader<BucketBoxDecrypted>> = _parent.asStateFlow()

    private val _bucketType: MutableStateFlow<BucketBoxEncrypted.BucketType?> = MutableStateFlow(value = null)
    val bucketType: StateFlow<BucketBoxEncrypted.BucketType?> = _bucketType.asStateFlow()

    private val _bucketItemBoxDataFlow: MutableStateFlow<DataLoader<BucketItemBoxDecrypted>> = MutableStateFlow(value = DataLoader.Init())
    val bucketItemBoxDataFlow: StateFlow<DataLoader<BucketItemBoxDecrypted>> = this._bucketItemBoxDataFlow.asStateFlow()

    private val _thumbnailDataFlow: MutableStateFlow<DataLoader<ThumbnailData>> = MutableStateFlow(value = DataLoader.Init())
    val thumbnailDataFlow: StateFlow<DataLoader<ThumbnailData>> = this._thumbnailDataFlow.asStateFlow()

    fun loadData(bucketItemId: Long, parentId: Long) {
        loadParent(parentId = parentId)
        this._isDataSaved.tryEmit(value = true)

        viewModelScope.launch(context = Dispatchers.Default) {
            try {
                boxRepository.getBucketItemBoxAsFlow(id = bucketItemId).collectLatest { bucketItemBoxCache ->
                    val bucketItemBox = bucketItemBoxCache?.decrypt(alice2 = alice2)
                    if (bucketItemBox == null) this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.NoData())
                    else {
                        this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Loaded(data = bucketItemBox))

                        val thumbnail = bucketItemBox.bucketItemData?.thumbnail(context = baseApplication) as? BucketItemData.Companion.Thumbnail.File
                        if (thumbnail == null) this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.NoData())
                        else this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.Loaded(data = ThumbnailData.EncryptedFile(file = thumbnail)))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Error(message = "Error loading data", exception = e))
            }
        }
    }

    private fun loadParent(parentId: Long) {
        viewModelScope.launch(context = Dispatchers.Default) {
            boxRepository.getBucketBoxAsFlow(id = parentId).collectLatest { bucketBoxCache ->
                val bucketBox = bucketBoxCache?.decrypt(alice2 = alice2)
                if (bucketBox == null) this@BucketItemViewModel2._parent.tryEmit(value = DataLoader.NoData())
                else this@BucketItemViewModel2._parent.tryEmit(value = DataLoader.Loaded(data = bucketBox))

                this@BucketItemViewModel2._bucketType.tryEmit(value = bucketBox?.bucketType)
            }
        }
    }

    fun fetchBookData(parentId: Long, olBookSearchResult: OLBookSearchResult) {
        Log.d(TAG, "fetchBookData")

        if (!_isDataFetched) {
            loadParent(parentId = parentId)

            viewModelScope.launch(context = Dispatchers.IO) {
                if (olBookSearchResult.key == null) this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.NoData())
                else {
                    openLibraryApi2.getBookData(bookKey = olBookSearchResult.key) { networkResponse ->
                        this@BucketItemViewModel2._isDataFetched = true
                        when (networkResponse) {
                            is NetworkResponse.Init -> this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Init())
                            is NetworkResponse.Loading -> this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Loading())
                            is NetworkResponse.Error -> this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Error(exception = networkResponse.exception, message = networkResponse.message))
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
                                this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Loaded(data = bucketItemBox))
                                this@BucketItemViewModel2._isDataSaved.tryEmit(value = false)
                            }
                        }
                    }
                }
            }

            fetchBookCover(olBookSearchResult)
        }
    }

    private fun fetchBookCover(olBookSearchResult: OLBookSearchResult) {
        viewModelScope.launch(context = Dispatchers.IO) {
            val coverI = olBookSearchResult.coverI
            if (coverI == null) this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.NoData())
            else openLibraryApi2.getBookCoverImage(coverI = olBookSearchResult.coverI) { networkResponse ->
                when (networkResponse) {
                    is NetworkResponse.Init -> this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.Init())
                    is NetworkResponse.Loading -> this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.Loading())
                    is NetworkResponse.Error -> this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.Error(exception = networkResponse.exception, message = networkResponse.message))
                    is NetworkResponse.Success -> this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.Loaded(data = ThumbnailData.Bitmap(data = networkResponse.data)))
                }
            }
        }
    }

    fun fetchShowData(parentId: Long, traktShowSearchResult: TraktShowSearchResult) {
        Log.d(TAG, "fetchShowData")

        if (!_isDataFetched) {
            loadParent(parentId = parentId)

            viewModelScope.launch(context = Dispatchers.IO) {
                val traktId = traktShowSearchResult.showSearchResult?.ids?.trakt
                if (traktId == null) {
                    this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.NoData())
                    this@BucketItemViewModel2._isDataFetched = true
                } else when (traktShowSearchResult) {
                    is TraktShowSearchResult.TraktMovieSearchResult -> fetchMovieSummary(traktId)
                    is TraktShowSearchResult.TraktSeriesSearchResult -> fetchSeriesSummary(traktId)
                }
            }

            fetchShowPoster(traktShowSearchResult)
        }
    }

    @WorkerThread
    private suspend fun fetchMovieSummary(traktId: Int) {
        traktApi.getMovieSummary(traktId = traktId) { networkResponse ->
            this@BucketItemViewModel2._isDataFetched = true
            when (networkResponse) {
                is NetworkResponse.Init -> this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Init())
                is NetworkResponse.Loading -> this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Loading())
                is NetworkResponse.Error -> this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Error(exception = networkResponse.exception, message = networkResponse.message))
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
                    this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Loaded(data = bucketItemBox))
                    this@BucketItemViewModel2._isDataSaved.tryEmit(value = false)
                }
            }
        }
    }

    @WorkerThread
    private suspend fun fetchSeriesSummary(traktId: Int) {
        traktApi.getSeriesSummary(traktId = traktId) { networkResponse ->
            this@BucketItemViewModel2._isDataFetched = true
            when (networkResponse) {
                is NetworkResponse.Init -> this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Init())
                is NetworkResponse.Loading -> this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Loading())
                is NetworkResponse.Error -> this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Error(exception = networkResponse.exception, message = networkResponse.message))
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
                    this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Loaded(data = bucketItemBox))
                    this@BucketItemViewModel2._isDataSaved.tryEmit(value = false)
                }
            }
        }

    }

    @OptIn(InternalSerializationApi::class)
    fun putBucketItemBox() {
        val parent = (this.parent.value as? DataLoader.Loaded)?.data ?: return
        val bucketItemBox = (this._bucketItemBoxDataFlow.value as? DataLoader.Loaded)?.data ?: return

        val thumbnailPlainByteArray = (this.thumbnailDataFlow.value as? DataLoader.Loaded)?.data?.getAsByteArray()
        val thumbnailFile = if (thumbnailPlainByteArray != null) BucketItemData.Companion.Thumbnail.File.PersistentFile() else null

        val updatedBucketItemData = when (bucketItemBox.bucketItemData) {
            is BucketItemBook.OpenLibrary -> bucketItemBox.bucketItemData.copy(thumbnail = thumbnailFile)
            is BucketItemBook.Custom -> bucketItemBox.bucketItemData.copy(thumbnail = thumbnailFile)
            is BucketItemShow.TraktMovie -> bucketItemBox.bucketItemData.copy(thumbnail = thumbnailFile)
            is BucketItemShow.TraktSeries -> bucketItemBox.bucketItemData.copy(thumbnail = thumbnailFile)
            else -> bucketItemBox.bucketItemData
        }

        val updatedBucketItemBox = bucketItemBox.copy(bucketItemData = updatedBucketItemData, parent = parent)

        boxRepository.putBucketItemBox(bucketItemBox = updatedBucketItemBox) { bucketItemBoxId ->
            if (bucketItemBoxId != null && thumbnailPlainByteArray != null) boxRepository.putBucketItemBoxThumbnail(thumbnailFile = thumbnailFile, thumbnailPlainByteArray = thumbnailPlainByteArray)
            this@BucketItemViewModel2._isDataSaved.tryEmit(value = true)
        }
    }

    private fun fetchShowPoster(traktShowSearchResult: TraktShowSearchResult) {
        viewModelScope.launch(context = Dispatchers.IO) {
            val posterPath = traktShowSearchResult.showSearchResult?.traktImages?.poster?.firstOrNull()
            if (posterPath == null) this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.NoData())
            else traktApi.getShowCoverImage(posterPath = posterPath) { networkResponse ->
                when (networkResponse) {
                    is NetworkResponse.Init -> this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.Init())
                    is NetworkResponse.Loading -> this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.Loading())
                    is NetworkResponse.Error -> this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.Error(exception = networkResponse.exception, message = networkResponse.message))
                    is NetworkResponse.Success -> this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.Loaded(data = ThumbnailData.Bitmap(data = networkResponse.data)))
                }
            }
        }
    }


    fun updateBucketItemBox(bucketItemBox: BucketItemBoxDecrypted) {
        boxRepository.putBucketItemBox(bucketItemBox = bucketItemBox) {
            this@BucketItemViewModel2._isDataSaved.tryEmit(value = true)
        }
    }

    fun updateBucketItemData(bucketItemData: BucketItemData) {
        val bucketItemBox = (this.bucketItemBoxDataFlow.value as? DataLoader.Loaded)?.data?.copy(bucketItemData = bucketItemData) ?: return
        boxRepository.putBucketItemBox(bucketItemBox = bucketItemBox) {
            this@BucketItemViewModel2._isDataSaved.tryEmit(value = true)
        }
    }

    fun onToggleBucketItemState(newState: BucketItemBoxDecrypted.State) {
        val bucketItemBox = (this._bucketItemBoxDataFlow.value as? DataLoader.Loaded)?.data
        val newBucketItemBox = bucketItemBox?.copy(state = newState)
        val newData = DataLoader.Loaded(data = newBucketItemBox ?: return)

        if (isDataSaved.value == false) this._bucketItemBoxDataFlow.tryEmit(value = newData)
        else updateBucketItemBox(bucketItemBox = newBucketItemBox)
    }

    fun onToggleLock(newState: Boolean) {
        val bucketItemBox = (this._bucketItemBoxDataFlow.value as? DataLoader.Loaded)?.data
        val newBucketItemBox = bucketItemBox?.copy(isLocked = newState)
        val newData = DataLoader.Loaded(data = newBucketItemBox ?: return)

        if (isDataSaved.value == false) this._bucketItemBoxDataFlow.tryEmit(value = newData)
        else updateBucketItemBox(bucketItemBox = newBucketItemBox)
    }

    fun onToggleFavourite(newState: Boolean) {
        val bucketItemBox = (this._bucketItemBoxDataFlow.value as? DataLoader.Loaded)?.data
        val newBucketItemBox = bucketItemBox?.copy(isFavourite = newState)
        val newData = DataLoader.Loaded(data = newBucketItemBox ?: return)

        if (isDataSaved.value == false) this._bucketItemBoxDataFlow.tryEmit(value = newData)
        else updateBucketItemBox(bucketItemBox = newBucketItemBox)
    }

    fun onUpdateThumbnail(uri: Uri) {
        viewModelScope.launch(context = Dispatchers.Default) {
            val bucketItemBox = (this@BucketItemViewModel2._bucketItemBoxDataFlow.value as? DataLoader.Loaded)?.data ?: return@launch

            val thumbnailPlainByteArray = uri.toByteArray(context = baseApplication) ?: return@launch
            val thumbnailFile = BucketItemData.Companion.Thumbnail.File.PersistentFile()
            (bucketItemBox.bucketItemData?.thumbnail(baseApplication) as? BucketItemData.Companion.Thumbnail.File)?.getFile(baseApplication)?.delete()

            val updatedBucketItemData = when (bucketItemBox.bucketItemData) {
                is BucketItemBook.OpenLibrary -> bucketItemBox.bucketItemData.copy(thumbnail = thumbnailFile)
                is BucketItemBook.Custom -> bucketItemBox.bucketItemData.copy(thumbnail = thumbnailFile)
                is BucketItemShow.TraktMovie -> bucketItemBox.bucketItemData.copy(thumbnail = thumbnailFile)
                is BucketItemShow.TraktSeries -> bucketItemBox.bucketItemData.copy(thumbnail = thumbnailFile)
                else -> bucketItemBox.bucketItemData
            }

            val updatedBucketItemBox = bucketItemBox.copy(bucketItemData = updatedBucketItemData)

            boxRepository.putBucketItemBox(bucketItemBox = updatedBucketItemBox) { bucketItemBoxId ->
                if (bucketItemBoxId != null) boxRepository.putBucketItemBoxThumbnail(thumbnailFile = thumbnailFile, thumbnailPlainByteArray = thumbnailPlainByteArray)
                this@BucketItemViewModel2._isDataSaved.tryEmit(value = true)
            }
        }
    }

    companion object {
        val TAG = BucketItemViewModel2::class.simpleName!!
    }
}

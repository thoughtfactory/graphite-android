package com.syncodec.graphite.presentation.bucketItem2

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.di.model.importer.ThumbnailData
import com.syncodec.graphite.di.modelObjectBox.BucketBox
import com.syncodec.graphite.di.modelObjectBox.BucketItemBox
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemBook
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemData
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemLink
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemLocation
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemShow
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemTodo
import com.syncodec.graphite.di.network.NetworkResponse
import com.syncodec.graphite.di.network.openLibrary.OLBookSearchResult
import com.syncodec.graphite.di.network.openLibrary.OpenLibraryApi2
import com.syncodec.graphite.di.network.trakt.TraktApi
import com.syncodec.graphite.di.network.trakt.TraktShowSearchResult
import com.syncodec.graphite.di.secureRepository.BoxRepository
import com.syncodec.graphite.utils.BitmapUtil.compress
import com.syncodec.graphite.utils.DataLoader
import com.syncodec.graphite.utils.FileUtil.toBitmap
import com.syncodec.graphite.utils.FileUtil.toByteArray
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class BucketItemViewModel2(
    private val baseApplication: BaseApplication,
    private val boxRepository: BoxRepository,
    private val openLibraryApi2: OpenLibraryApi2,
    private val traktApi: TraktApi,
) : ViewModel() {

    private var _isDataFetched = false

    private val _isDataSaved: MutableStateFlow<Boolean?> = MutableStateFlow(value = null)
    val isDataSaved: StateFlow<Boolean?> = _isDataSaved.asStateFlow()

    private val _parent: MutableStateFlow<DataLoader<BucketBox>> = MutableStateFlow(value = DataLoader.Init)
    val parent: StateFlow<DataLoader<BucketBox>> = _parent.asStateFlow()

    private val _bucketType: MutableStateFlow<BucketBox.BucketType?> = MutableStateFlow(value = null)
    val bucketType: StateFlow<BucketBox.BucketType?> = _bucketType.asStateFlow()

    private val _bucketItemBoxDataFlow: MutableStateFlow<DataLoader<BucketItemBox>> = MutableStateFlow(value = DataLoader.Init)
    val bucketItemBoxDataFlow: StateFlow<DataLoader<BucketItemBox>> = this._bucketItemBoxDataFlow.asStateFlow()

    private val _thumbnailDataFlow: MutableStateFlow<DataLoader<ThumbnailData>> = MutableStateFlow(value = DataLoader.Init)
    val thumbnailDataFlow: StateFlow<DataLoader<ThumbnailData>> = this._thumbnailDataFlow.asStateFlow()

    fun loadData(bucketItemId: Long, parentId: Long) {
        loadParent(parentId = parentId)
        this._isDataSaved.tryEmit(value = true)

        viewModelScope.launch(context = Dispatchers.Default) {
            try {
                boxRepository.getBucketItemBoxAsFlow(id = bucketItemId).collectLatest { bucketItemBox ->
                    if (bucketItemBox == null) this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.NoData)
                    else {
                        this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Loaded(data = bucketItemBox))

                        val thumbnail = bucketItemBox.bucketItemData?.thumbnail(context = baseApplication)
                        val a = when (thumbnail) {
                            is BucketItemData.Companion.Thumbnail.Base64 -> thumbnail.data.decodeBase64ToBitmap()
                            is BucketItemData.Companion.Thumbnail.File -> boxRepository.getBucketItemBoxThumbnail(bucketItemBoxId = bucketItemBox.id)
                            else -> null
                        }



                        when (bucketItemBox.bucketItemData) {
                            is BucketItemTodo -> Unit
                            is BucketItemBook -> {
                                val thumbnail = bucketItemBox.bucketItemData?.thumbnail(context = baseApplication)
                                val thumbnailData = when (thumbnail) {
                                    is BucketItemData.Companion.Thumbnail.Base64 -> thumbnail.data.decodeBase64ToBitmap()?.let { ThumbnailData.Bitmap(data = it) }
                                    is BucketItemData.Companion.Thumbnail.File -> boxRepository.getBucketItemBoxThumbnail(bucketItemBoxId = bucketItemBox.id)?.let { ThumbnailData.File(data = it) }
                                    else -> null
                                }
                                if (thumbnailData == null) this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.NoData)
                                else this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.Loaded(data = thumbnailData))
                            }

                            is BucketItemShow -> {
                                val thumbnailFile = boxRepository.getBucketItemBoxThumbnail(bucketItemBoxId = bucketItemBox.id)
                                if (thumbnailFile == null) this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.NoData)
                                else this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.Loaded(data = ThumbnailData.File(data = thumbnailFile)))
                            }

                            is BucketItemLink -> Unit
                            is BucketItemLocation -> Unit
                            else -> Unit
                        }
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
            boxRepository.getBucketBoxAsFlow(id = parentId).collectLatest {
                if (it == null) this@BucketItemViewModel2._parent.tryEmit(value = DataLoader.NoData)
                else this@BucketItemViewModel2._parent.tryEmit(value = DataLoader.Loaded(data = it))

                this@BucketItemViewModel2._bucketType.tryEmit(value = it?.bucketType)
            }
        }
    }

    fun fetchBookData(parentId: Long, olBookSearchResult: OLBookSearchResult) {
        Log.d(TAG, "fetchBookData")

        if (!_isDataFetched) {
            loadParent(parentId = parentId)

            viewModelScope.launch(context = Dispatchers.IO) {
                if (olBookSearchResult.key == null) this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.NoData)
                else {
                    openLibraryApi2.getBookData(bookKey = olBookSearchResult.key) { networkResponse ->
                        this@BucketItemViewModel2._isDataFetched = true
                        when (networkResponse) {
                            is NetworkResponse.Init -> this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Init)
                            is NetworkResponse.Loading -> this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Loading)
                            is NetworkResponse.Error -> this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Error(exception = networkResponse.exception, message = networkResponse.message))
                            is NetworkResponse.Success -> {
                                val olBookData = networkResponse.data
                                val bucketItemData = BucketItemBook.OpenLibrary(
                                    state = BucketItemData.State.Alpha,
                                    key = olBookData.key,
                                    title = olBookData.title,
                                    description = olBookData.description,
                                    coverI = olBookSearchResult.coverI,
                                    authorList = olBookSearchResult.authorName,
                                    firstPublishYear = olBookSearchResult.firstPublishedYear,
                                    numberOfPages = olBookSearchResult.numberOfPages,
                                )
                                val bucketItemBox = BucketItemBox(bucketItemData = bucketItemData)
                                this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Loaded(data = bucketItemBox))
                                this@BucketItemViewModel2._isDataSaved.tryEmit(value = false)
                            }
                        }
                    }
                }
            }

            viewModelScope.launch(context = Dispatchers.IO) {
                val coverI = olBookSearchResult.coverI
                if (coverI == null) this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.NoData)
                else openLibraryApi2.getBookCoverImage(coverI = olBookSearchResult.coverI) { networkResponse ->
                    when (networkResponse) {
                        is NetworkResponse.Init -> this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.Init)
                        is NetworkResponse.Loading -> this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.Loading)
                        is NetworkResponse.Error -> this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.Error(exception = networkResponse.exception, message = networkResponse.message))
                        is NetworkResponse.Success -> this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.Loaded(data = ThumbnailData.Bitmap(data = networkResponse.data)))
                    }
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
                    this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.NoData)
                    this@BucketItemViewModel2._isDataFetched = true
                } else when (traktShowSearchResult) {
                    is TraktShowSearchResult.TraktMovieSearchResult -> traktApi.getMovieSummary(traktId = traktId) { networkResponse ->
                        this@BucketItemViewModel2._isDataFetched = true
                        when (networkResponse) {
                            is NetworkResponse.Init -> this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Init)
                            is NetworkResponse.Loading -> this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Loading)
                            is NetworkResponse.Error -> this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Error(exception = networkResponse.exception, message = networkResponse.message))
                            is NetworkResponse.Success -> {
                                val traktMovieData = networkResponse.data
                                val bucketItemData = BucketItemShow.TraktMovie(
                                    state = BucketItemData.State.Alpha,
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
                                val bucketItemBox = BucketItemBox(bucketItemData = bucketItemData)
                                this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Loaded(data = bucketItemBox))
                                this@BucketItemViewModel2._isDataSaved.tryEmit(value = false)
                            }
                        }
                    }

                    is TraktShowSearchResult.TraktSeriesSearchResult -> traktApi.getSeriesSummary(traktId = traktId) { networkResponse ->
                        this@BucketItemViewModel2._isDataFetched = true
                        when (networkResponse) {
                            is NetworkResponse.Init -> this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Init)
                            is NetworkResponse.Loading -> this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Loading)
                            is NetworkResponse.Error -> this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Error(exception = networkResponse.exception, message = networkResponse.message))
                            is NetworkResponse.Success -> {
                                val traktSeriesData = networkResponse.data
                                val bucketItemData = BucketItemShow.TraktSeries(
                                    state = BucketItemData.State.Alpha,
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
                                val bucketItemBox = BucketItemBox(bucketItemData = bucketItemData)
                                this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Loaded(data = bucketItemBox))
                                this@BucketItemViewModel2._isDataSaved.tryEmit(value = false)
                            }
                        }
                    }
                }
            }

            viewModelScope.launch(context = Dispatchers.IO) {
                val posterPath = traktShowSearchResult.showSearchResult?.traktImages?.poster?.firstOrNull()
                if (posterPath == null) this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.NoData)
                else traktApi.getShowCoverImage(posterPath = posterPath) { networkResponse ->
                    when (networkResponse) {
                        is NetworkResponse.Init -> this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.Init)
                        is NetworkResponse.Loading -> this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.Loading)
                        is NetworkResponse.Error -> this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.Error(exception = networkResponse.exception, message = networkResponse.message))
                        is NetworkResponse.Success -> this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = DataLoader.Loaded(data = ThumbnailData.Bitmap(data = networkResponse.data)))
                    }
                }
            }
        }
    }

    @OptIn(InternalSerializationApi::class)
    fun putBucketItemBox() {
        val parent = (this.parent.value as? DataLoader.Loaded)?.data ?: return
        val thumbnailBase64 = (this.thumbnailDataFlow.value as? DataLoader.Loaded)?.data?.getAsBase64()?.let { BucketItemData.Companion.Thumbnail.Base64(data = it) }
        val bucketItemBox = (this._bucketItemBoxDataFlow.value as? DataLoader.Loaded)?.data ?: return

        bucketItemBox.bucketItemData = when (bucketItemBox.bucketItemData) {
            is BucketItemBook.OpenLibrary -> (bucketItemBox.bucketItemData as? BucketItemBook.OpenLibrary)?.copy(thumbnail = thumbnailBase64)
            is BucketItemShow.TraktMovie -> (bucketItemBox.bucketItemData as? BucketItemShow.TraktMovie)?.copy(thumbnail = if (thumbnailBase64 == null) null else BucketItemData.Companion.Thumbnail.File)
            else -> bucketItemBox.bucketItemData
        }

        boxRepository.putBucketItemBox(bucketItemBox = bucketItemBox, parent = parent) { bucketItemBoxId ->
            if (bucketItemBoxId != null && thumbnailBase64 != null) boxRepository.putBucketItemBoxThumbnail(bucketItemBoxId = bucketItemBoxId, thumbnail = (this@BucketItemViewModel2.thumbnailDataFlow.value as? DataLoader.Loaded)?.data?.getAsBitmap())
            this@BucketItemViewModel2._isDataSaved.tryEmit(value = true)
        }
    }

    fun updateBucketItemBox(bucketItemBox: BucketItemBox) {
        val parent = (this.parent.value as? DataLoader.Loaded)?.data ?: return
        boxRepository.putBucketItemBox(bucketItemBox = bucketItemBox, parent = parent) {
            this@BucketItemViewModel2._isDataSaved.tryEmit(value = true)
        }
    }

    fun updateBucketItemData(bucketItemData: BucketItemData) {
        val parent = (this.parent.value as? DataLoader.Loaded)?.data ?: return
        val bucketItemBox = (this.bucketItemBoxDataFlow.value as? DataLoader.Loaded)?.data?.copy(bucketItemData = bucketItemData) ?: return
        boxRepository.putBucketItemBox(bucketItemBox = bucketItemBox, parent = parent) {
            this@BucketItemViewModel2._isDataSaved.tryEmit(value = true)
        }
    }

    fun onToggleBucketItemState(newState: BucketItemData.State) {
        val bucketItemBox = (this._bucketItemBoxDataFlow.value as? DataLoader.Loaded)?.data
        val newBucketItemBox = bucketItemBox?.copy(bucketItemData = bucketItemBox.bucketItemData?.copyWithState(newState = newState))
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
            boxRepository.putBucketItemBoxThumbnail(bucketItemBoxId = bucketItemBox.id) {
                uri
                    .toByteArray(context = baseApplication)
                    ?.toBitmap(context = baseApplication)
                    ?.compress(maxSize = 1024 * 1024 * 12, outputStream = it)
            }
        }
    }

    companion object {
        val TAG = BucketItemViewModel2::class.simpleName!!
    }
}

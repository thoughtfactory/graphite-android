package com.syncodec.graphite.presentation.bucketItem2

import android.net.Uri
import android.util.Log
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

    private val bookFetcher = BookFetcher(openLibraryApi2 = openLibraryApi2)
    private val showFetcher = ShowFetcher(traktApi = traktApi)

    private var _isDataFetched = false

    private val _isDataSaved: MutableStateFlow<Boolean?> = MutableStateFlow(value = null)
    val isDataSaved: StateFlow<Boolean?> = _isDataSaved.asStateFlow()

    private val _parentIdDataFlow: MutableStateFlow<DataLoader<Long>> = MutableStateFlow(value = DataLoader.Init())
    val parentIdDataFlow: StateFlow<DataLoader<Long>> = this._parentIdDataFlow.asStateFlow()

    private val _parentDataFlow: MutableStateFlow<DataLoader<BucketBoxDecrypted>> = MutableStateFlow(value = DataLoader.Init())
    val parentDataFlow: StateFlow<DataLoader<BucketBoxDecrypted>> = this._parentDataFlow.asStateFlow()

    private val _bucketType: MutableStateFlow<BucketBoxEncrypted.BucketType?> = MutableStateFlow(value = null)
    val bucketType: StateFlow<BucketBoxEncrypted.BucketType?> = this._bucketType.asStateFlow()

    private val _bucketItemBoxDataFlow: MutableStateFlow<DataLoader<BucketItemBoxDecrypted>> = MutableStateFlow(value = DataLoader.Init())
    val bucketItemBoxDataFlow: StateFlow<DataLoader<BucketItemBoxDecrypted>> = this._bucketItemBoxDataFlow.asStateFlow()

    private val _thumbnailDataFlow: MutableStateFlow<DataLoader<ThumbnailData>> = MutableStateFlow(value = DataLoader.Init())
    val thumbnailDataFlow: StateFlow<DataLoader<ThumbnailData>> = this._thumbnailDataFlow.asStateFlow()

    init {
        viewModelScope.launch(context = Dispatchers.Default) {
            this@BucketItemViewModel2.parentIdDataFlow.collectLatest { parentIdData ->
                if (parentIdData !is DataLoader.Loaded) this@BucketItemViewModel2._parentDataFlow.tryEmit(value = DataLoader.Loading())
                else {
                    boxRepository.bucketBoxRepository.getBucketBoxAsFlow(id = parentIdData.data).collectLatest { bucketBoxCache ->
                        val bucketBox = bucketBoxCache?.decrypt(alice2 = alice2)
                        if (bucketBox == null) this@BucketItemViewModel2._parentDataFlow.tryEmit(value = DataLoader.NoData())
                        else this@BucketItemViewModel2._parentDataFlow.tryEmit(value = DataLoader.Loaded(data = bucketBox))

                        this@BucketItemViewModel2._bucketType.tryEmit(value = bucketBox?.bucketType)
                    }
                }
            }
        }
    }

    fun loadData(bucketItemId: Long, parentId: Long) {
        this._isDataSaved.tryEmit(value = true)

        viewModelScope.launch(context = Dispatchers.Default) {
            try {
                boxRepository.getBucketItemBoxAsFlow(id = bucketItemId).collectLatest { bucketItemBoxCache ->
                    val bucketItemBox = bucketItemBoxCache?.decrypt(alice2 = alice2)
                    if (bucketItemBox == null) this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = DataLoader.NoData())
                    else {
                        bucketItemBox.parent?.id?.let { loadParentId(parentId = it) }

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

    fun loadParentId(parentId: Long) {
        Log.d(TAG, "parentId : $parentId")
        this._parentIdDataFlow.tryEmit(value = DataLoader.Loaded(data = parentId))
    }

    fun fetchBookData(parentId: Long, olBookSearchResult: OLBookSearchResult) {
        Log.d(TAG, "bookData")

        if (!_isDataFetched) {
            loadParentId(parentId = parentId)

            viewModelScope.launch(context = Dispatchers.IO) {
                bookFetcher.fetchBookData(olBookSearchResult = olBookSearchResult) {
                    this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = it)
                    if (it is DataLoader.Loaded) this@BucketItemViewModel2._isDataSaved.tryEmit(value = false)
                }
            }

            viewModelScope.launch(context = Dispatchers.IO) {
                bookFetcher.fetchBookCover(olBookSearchResult = olBookSearchResult) { this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = it) }
            }
        }
    }

    fun fetchShowData(parentId: Long, traktShowSearchResult: TraktShowSearchResult) {
        Log.d(TAG, "fetchShowData")

        if (!_isDataFetched) {
            loadParentId(parentId = parentId)

            viewModelScope.launch(context = Dispatchers.IO) {
                showFetcher.fetchShowData(traktShowSearchResult = traktShowSearchResult) {
                    this@BucketItemViewModel2._bucketItemBoxDataFlow.tryEmit(value = it)
                    if (it is DataLoader.Loaded) this@BucketItemViewModel2._isDataSaved.tryEmit(value = false)
                }
            }

            viewModelScope.launch(context = Dispatchers.IO) {
                showFetcher.fetchShowPoster(traktShowSearchResult = traktShowSearchResult) { this@BucketItemViewModel2._thumbnailDataFlow.tryEmit(value = it) }
            }
        }
    }

    @OptIn(InternalSerializationApi::class)
    fun putBucketItemBox() {
        val parent = (this.parentDataFlow.value as? DataLoader.Loaded)?.data ?: return
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

    fun onUpdateBucketItemBox(bucketItemBoxDecrypted: BucketItemBoxDecrypted) {
        if (isDataSaved.value == false) this._bucketItemBoxDataFlow.tryEmit(value = DataLoader.Loaded(data = bucketItemBoxDecrypted))
        else updateBucketItemBox(bucketItemBox = bucketItemBoxDecrypted)
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

    fun deleteBucketItemBox(id: Long) = boxRepository.deleteBucketItemBox(idList = listOf(id))

    companion object {
        val TAG = BucketItemViewModel2::class.simpleName!!
    }
}

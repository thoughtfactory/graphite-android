package com.syncodec.graphite.presentation.bucketItem.composable.screen.bookScreen

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.network.OpenLibraryApi
import com.syncodec.graphite.presentation.bucketItem.composable.screen.AbstractBucketScreenViewModel
import com.syncodec.graphite.utils.ContentStatus
import com.syncodec.graphite.utils.Quadruple
import com.syncodec.graphite.utils.Status
import com.syncodec.graphite.utils.encodeBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch


class BookScreenViewModel : AbstractBucketScreenViewModel() {

	val status : MutableState<Status> = mutableStateOf(Status.INIT)

	val bookKey : MutableStateFlow<String?> = MutableStateFlow(null)
	val bookTitle : MutableStateFlow<String?> = MutableStateFlow(null)
	val bookCoverI : MutableStateFlow<String?> = MutableStateFlow(null)
	val bookAuthorList : MutableStateFlow<List<String>> = MutableStateFlow(listOf())
	val bookDescription : MutableStateFlow<String?> = MutableStateFlow(null)
	val bookPageCount : MutableStateFlow<Int?> = MutableStateFlow(null)
	val bookFirstPublishYear : MutableStateFlow<String?> = MutableStateFlow(null)

	val thumbnailContentStatus : MutableStateFlow<ContentStatus<Bitmap?>> = MutableStateFlow(ContentStatus.Init)

	val bookData : MutableStateFlow<BucketItemObject.Companion.BucketItemData.BookData?> = MutableStateFlow(null)

	init {
		observeData()
	}

	override fun observeData() {
		viewModelScope.launch(Dispatchers.Default) {
			combine(
				bookKey,
				bookTitle,
				bookCoverI,
				bookAuthorList,
				bookDescription,
				bookPageCount,
				bookFirstPublishYear
			) { flowResult ->
				try {
					val key = flowResult[0] as String?
					val title = flowResult[1] as String?
					val coverI = flowResult[2] as String?
					val authorList = flowResult[3] as List<*>
					val description = flowResult[4] as String?
					val pageCount = flowResult[5] as Int?
					val firstPublishYear = flowResult[6] as String?

					BucketItemObject.Companion.BucketItemData.BookData(
						key = key,
						title = title,
						coverI = coverI,
						authorList = authorList.map { it as String },
						description = description,
						numberOfPages = pageCount,
						firstPublishYear = firstPublishYear,
					)
				} catch (e : Exception) {
					null
				}
			}.collect {
				bookData.tryEmit(it)
			}
		}
	}

	override fun initData(id : String, data : String?) {
		viewModelScope.launch(Dispatchers.IO) {
			BucketItemObject.Companion.BucketItemData.BookData(jsonString = data).let { bookData ->
				loadData(data)
				OpenLibraryApi.retrieveDescriptionFromKey(key = id) { description ->
					viewModelScope.launch(Dispatchers.Main) { bookDescription.value = description }
				}
				retrieveThumbnail(data = bookData.coverI) { loadThumbnail(it) }
			}
		}
	}

	override fun loadData(data : String?) {
		viewModelScope.launch(Dispatchers.Main) {
			BucketItemObject.Companion.BucketItemData.BookData(jsonString = data).let { bookData ->
				this@BookScreenViewModel.bookKey.tryEmit(bookData.key)
				this@BookScreenViewModel.bookTitle.tryEmit(bookData.title)
				this@BookScreenViewModel.bookCoverI.tryEmit(bookData.coverI)
				this@BookScreenViewModel.bookAuthorList.tryEmit(bookData.authorList?.filterNotNull() ?: listOf())
				this@BookScreenViewModel.bookDescription.tryEmit(bookData.description)
				this@BookScreenViewModel.bookPageCount.tryEmit(bookData.numberOfPages)
				this@BookScreenViewModel.bookFirstPublishYear.tryEmit(bookData.firstPublishYear)

				status.value = Status.LOADED
			}
		}
	}

	override fun loadThumbnail(thumbnail : Bitmap?) {
		thumbnail?.let {
			this@BookScreenViewModel.thumbnailContentStatus.tryEmit(ContentStatus.Loaded(it))
		} ?: this@BookScreenViewModel.thumbnailContentStatus.tryEmit(ContentStatus.LoadedEmpty)
	}

	override fun getData() : Quadruple<String?, String?, String?, String?> {
		return Quadruple(
			BucketItemObject.Companion.BucketItemData.BookData(
				key = bookKey.value,
				title = bookTitle.value,
				coverI = bookCoverI.value,
				authorList = bookAuthorList.value,
				firstPublishYear = bookFirstPublishYear.value,
				numberOfPages = bookPageCount.value,
				description = bookDescription.value
			).toJsonString(),
			bookKey.value,
			thumbnailContentStatus.value.dataOrNull?.encodeBase64(),
			bookTitle.value
		)
	}

	override fun retrieveThumbnail(data : String?, onSuccess : (Bitmap) -> Unit) {
		try {
			viewModelScope.launch(Dispatchers.IO) {
				OpenLibraryApi.retrieveBookCover(coverI = data) { it?.let(onSuccess) }
			}
		} catch (e : Exception) {
//			TODO Show error
		}
	}
}

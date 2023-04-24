package com.syncodec.graphite.presentation.notebook.screen

import android.graphics.Bitmap
import androidx.annotation.WorkerThread
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.di.repository.koinRepository.KoinRepository
import com.syncodec.graphite.utils.LoaderStatus
import com.syncodec.graphite.utils.encodeBase64
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class NotebookScreenViewModel(private val repository : KoinRepository) : ViewModel() {

	val repositoryState = repository.repositoryState
	val chapterId = MutableStateFlow<RealmUUID?>(null)

	/** Show loading animation and seize new events when set. */
	val isOperationPending = MutableStateFlow(false)

	/** Cancelled and reassigned when [loadData] is called. This is scoped to the ViewModel.*/
	private var chapterReaderCoroutine : CoroutineScope? = null
	private var chapterCounterCoroutine : CoroutineScope? = null
	private var noteCounterCoroutine : CoroutineScope? = null

	val defaultChapterId = MutableStateFlow<RealmUUID?>(null)

	val chapterObject = MutableStateFlow<ChapterObject?>(null)

	val createdTimestamp = MutableStateFlow<Long?>(null)
	val modifiedTimestamp = MutableStateFlow<Long?>(null)
	val title = MutableStateFlow<String?>(null)
	val description = MutableStateFlow<String?>(null)
	val color = MutableStateFlow<Int?>(null)
	val thumbnail = MutableStateFlow<String?>(null)
	val isFavourite = MutableStateFlow<Boolean?>(null)
	val isLocked = MutableStateFlow<Boolean?>(null)

	private val _chapterList = MutableStateFlow<List<ChapterObject>>(listOf())
	private val _noteList = MutableStateFlow<List<NoteObjectLite>>(listOf())
	val chapterList : StateFlow<List<ChapterObject>> = _chapterList
	val noteList : StateFlow<List<NoteObjectLite>> = _noteList

	private val _chapterChapterItemCount : MutableStateFlow<Map<RealmUUID?, Int>> = MutableStateFlow(mapOf())
	val chapterChapterItemCount : StateFlow<Map<RealmUUID?, Int>> = _chapterChapterItemCount
	private val _chapterNoteItemCount : MutableStateFlow<Map<RealmUUID?, Int>> = MutableStateFlow(mapOf())
	val chapterNoteItemCount : StateFlow<Map<RealmUUID?, Int>> = _chapterNoteItemCount

	val parentId = MutableStateFlow<ByteArray?>(null)

	val chapterPath = MutableStateFlow<List<ChapterObjectLite>>(listOf())

	val isChapterRefreshing : MutableStateFlow<Boolean> = MutableStateFlow(false)
	val loaderStatus = MutableStateFlow(LoaderStatus.Init)

	val tagList = MutableStateFlow<List<TagObject>>(listOf())

	private var noteLoaderCoroutine : CoroutineScope? = null
	private var chapterLoaderCoroutine : CoroutineScope? = null

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryState.collect {
				if (it == RepositoryState.Success) repository.getAllTagAsFlow()
					.collect { tagObjectList -> this@NotebookScreenViewModel.tagList.tryEmit(tagObjectList) }
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			combine(repositoryState, chapterId) { repositoryState, chapterId ->
				repositoryState to chapterId
			}.collect { (repositoryState, chapterId) ->
				if (repositoryState == RepositoryState.Success) chapterId?.let { loadData(it) }
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			repository.getDefaultChapterIdAsFlow().collect { this@NotebookScreenViewModel.defaultChapterId.tryEmit(it) }
		}
	}

	fun loadChapter(chapterId : RealmUUID?) = this.chapterId.tryEmit(chapterId)

	private fun loadData(chapterId : RealmUUID) {
		loaderStatus.tryEmit(LoaderStatus.Loading)
		viewModelScope.launch(Dispatchers.Default) {
			chapterReaderCoroutine?.cancel()
			chapterReaderCoroutine = this
			repository.getChapterFromIdAsFlow(id = chapterId).collect { chapterObject ->
				chapterObject?.let { chapterObject1 ->
					this@NotebookScreenViewModel.chapterObject.tryEmit(chapterObject1)

					this@NotebookScreenViewModel.createdTimestamp.tryEmit(chapterObject1.createdTimestamp)
					this@NotebookScreenViewModel.modifiedTimestamp.tryEmit(chapterObject1.modifiedTimestamp)
					this@NotebookScreenViewModel.title.tryEmit(chapterObject1.title)
					this@NotebookScreenViewModel.description.tryEmit(chapterObject1.description)
					this@NotebookScreenViewModel.color.tryEmit(chapterObject1.color)
					this@NotebookScreenViewModel.thumbnail.tryEmit(chapterObject1.thumbnail)
					this@NotebookScreenViewModel.isFavourite.tryEmit(chapterObject1.isFavourite)
					this@NotebookScreenViewModel.isLocked.tryEmit(chapterObject1.isLocked)

					this@NotebookScreenViewModel.parentId.tryEmit(chapterObject1.parentId?.bytes)

					loadNoteList(parentId = chapterObject1.id)
					loadChapterList(parentId = chapterObject1.id)
					loadChapterPath(chapterId = chapterObject1.id).let {
						this@NotebookScreenViewModel.chapterPath.tryEmit(it)
						loaderStatus.tryEmit(LoaderStatus.Loaded)
					}
				}
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			chapterCounterCoroutine?.cancel()
			chapterCounterCoroutine = this
			repository.getAllChapterAsFlow().cancellable().collect {
				it.groupingBy { it.parentId }.eachCount().let { _chapterChapterItemCount.tryEmit(it) }
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			noteCounterCoroutine?.cancel()
			noteCounterCoroutine = this
			repository.getAllNoteAsFlow().cancellable().collect {
				it.groupingBy { it.parentId }.eachCount().let { _chapterNoteItemCount.tryEmit(it) }
			}
		}
	}

	private fun loadNoteList(parentId : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			noteLoaderCoroutine?.cancel()
			noteLoaderCoroutine = this
			repository.getNoteWithParentIdAsFlow(parentId = parentId).collect { noteList ->
				this@NotebookScreenViewModel._noteList.tryEmit(noteList.list.map { it.toLite() })
			}
		}
	}

	private fun loadChapterList(parentId : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			chapterLoaderCoroutine?.cancel()
			chapterLoaderCoroutine = this
			repository.getChapterWithParentIdAsFlow(parentId = parentId).collect { chapterList ->
				this@NotebookScreenViewModel._chapterList.tryEmit(chapterList.list)
			}
		}
	}

	@WorkerThread
	private fun loadChapterPath(chapterId : RealmUUID) = repository.getChapterPath(id = chapterId, includeEdge = true)

	fun putInnerChapter(
		parentId : RealmUUID?,
		title : String?,
		description : String?,
		color : Color?,
		thumbnail : Bitmap?,
		callback : (String?) -> Unit
	) {
		val chapterObject = ChapterObject().apply {
			this.title = title
			this.description = description
			this.color = color?.toArgb()
			this.thumbnail = thumbnail?.encodeBase64()

			this.parentId = parentId
		}

		if (BaseApplication.isPro.value) repository.putChapterSuspended(chapterObject)
		else callback("Join Graphite Pro to create chapters")
	}

	fun updateChapter(chapterId : RealmUUID, title : String?, description : String?, color : Color?, thumbnail : Bitmap?) {
		ChapterObject().apply {
			this.id = chapterId
			this.createdTimestamp = this@NotebookScreenViewModel.createdTimestamp.value ?: System.currentTimeMillis()
			this.title = title
			this.description = description
			this.color = color?.toArgb()
			this.thumbnail = thumbnail?.encodeBase64()
			this.isFavourite = this@NotebookScreenViewModel.isFavourite.value ?: false
			this.isLocked = this@NotebookScreenViewModel.isLocked.value ?: false

			this.parentId = this@NotebookScreenViewModel.parentId.value?.let { RealmUUID.Companion.from(it) }
			repository.putChapterSuspended(this)
		}
	}

	fun refresh(chapterId : RealmUUID) = loadChapter(chapterId = chapterId)

	fun toggleFavourite(chapterId : RealmUUID) {
		ChapterObject().apply {
			this.id = chapterId
			this.createdTimestamp = this@NotebookScreenViewModel.createdTimestamp.value ?: System.currentTimeMillis()
			this.title = this@NotebookScreenViewModel.title.value
			this.description = this@NotebookScreenViewModel.description.value
			this.color = this@NotebookScreenViewModel.color.value
			this.thumbnail = this@NotebookScreenViewModel.thumbnail.value
			this.isFavourite = (this@NotebookScreenViewModel.isFavourite.value ?: false).not()
			this.isLocked = this@NotebookScreenViewModel.isLocked.value ?: false

			this.parentId = this@NotebookScreenViewModel.parentId.value?.let { RealmUUID.Companion.from(it) }
			repository.putChapterSuspended(this)
		}
	}

	fun toggleLock(chapterId : RealmUUID) {
		ChapterObject().apply {
			this.id = chapterId
			this.createdTimestamp = this@NotebookScreenViewModel.createdTimestamp.value ?: System.currentTimeMillis()
			this.title = this@NotebookScreenViewModel.title.value
			this.description = this@NotebookScreenViewModel.description.value
			this.color = this@NotebookScreenViewModel.color.value
			this.thumbnail = this@NotebookScreenViewModel.thumbnail.value
			this.isFavourite = this@NotebookScreenViewModel.isFavourite.value ?: false
			this.isLocked = (this@NotebookScreenViewModel.isLocked.value ?: false).not()

			this.parentId = this@NotebookScreenViewModel.parentId.value?.let { RealmUUID.Companion.from(it) }
			repository.putChapterSuspended(this)
		}
	}

	fun toggleLocalOnly(chapterId : RealmUUID) {
		ChapterObject().apply {
			this.id = chapterId
			this.createdTimestamp = this@NotebookScreenViewModel.createdTimestamp.value ?: System.currentTimeMillis()
			this.title = this@NotebookScreenViewModel.title.value
			this.description = this@NotebookScreenViewModel.description.value
			this.color = this@NotebookScreenViewModel.color.value
			this.thumbnail = this@NotebookScreenViewModel.thumbnail.value
			this.isFavourite = this@NotebookScreenViewModel.isFavourite.value ?: false
			this.isLocked = this@NotebookScreenViewModel.isLocked.value ?: false

			this.parentId = this@NotebookScreenViewModel.parentId.value?.let { RealmUUID.Companion.from(it) }
			repository.putChapterSuspended(this)
		}
	}

	fun setDefaultChapter(chapterId : RealmUUID) {
		CoroutineScope(Dispatchers.Default).launch {
			repository.putDefaultChapterId(id = chapterId)
		}
	}

	fun delete(idList : List<RealmUUID>) {
		repository.deleteSuspended(idList = idList)
	}

	fun deleteCurrentChapter(id : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			parentId.value?.let { loadChapter(chapterId = RealmUUID.Companion.from(it)) }
			repository.deleteSuspended(id)
		}
	}
}

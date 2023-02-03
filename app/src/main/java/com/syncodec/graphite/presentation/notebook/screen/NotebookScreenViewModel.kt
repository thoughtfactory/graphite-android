package com.syncodec.graphite.presentation.notebook.screen

import android.graphics.Bitmap
import androidx.annotation.WorkerThread
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.KoinRepository
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.utils.ContentStatus
import com.syncodec.graphite.utils.encodeBase64
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
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
	val chapterList = MutableStateFlow<List<ChapterObject>>(listOf())
	val noteList = MutableStateFlow<List<NoteObjectLite>>(listOf())
	val parentId = MutableStateFlow<ByteArray?>(null)

	val chapterPath = MutableStateFlow<List<ChapterObjectLite>>(listOf())

	val isChapterRefreshing : MutableStateFlow<Boolean> = MutableStateFlow(false)
	val contentStatus = MutableStateFlow(ContentStatus.Init)

	val tagList = MutableStateFlow<List<TagObject>>(listOf())

	private var noteLoaderCoroutine : CoroutineScope? = null
	private var chapterLoaderCoroutine : CoroutineScope? = null

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryState.collect {
				if (it == RepositoryState.SUCCESS) repository.getAllTagAsFlow()
					.collect { tagObjectList -> this@NotebookScreenViewModel.tagList.tryEmit(tagObjectList) }
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			combine(repositoryState, chapterId) { repositoryState, chapterId ->
				repositoryState to chapterId
			}.collect { (repositoryState, chapterId) ->
				if (repositoryState == RepositoryState.SUCCESS) chapterId?.let { loadData(it) }
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			repository.getDefaultChapterIdAsFlow().collect { this@NotebookScreenViewModel.defaultChapterId.tryEmit(it) }
		}
	}

	fun loadChapter(chapterId : RealmUUID?) = this.chapterId.tryEmit(chapterId)

	private fun loadData(chapterId : RealmUUID) {
		contentStatus.tryEmit(ContentStatus.Loading)
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
						if (chapterObject1.noteList.isEmpty() && chapterObject1.chapterList.isEmpty()) contentStatus.tryEmit(ContentStatus.LoadedEmpty)
						else contentStatus.tryEmit(ContentStatus.Loaded)
					}
				}
			}
		}
	}

	private fun loadNoteList(parentId : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			noteLoaderCoroutine?.cancel()
			noteLoaderCoroutine = this
			repository.getNoteWithParentIdAsFlow(parentId = parentId).collect { noteList ->
				this@NotebookScreenViewModel.noteList.tryEmit(noteList.list.map { it.toLite() })
			}
		}
	}

	private fun loadChapterList(parentId : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			chapterLoaderCoroutine?.cancel()
			chapterLoaderCoroutine = this
			repository.getChapterWithParentIdAsFlow(parentId = parentId).collect { chapterList ->
				this@NotebookScreenViewModel.chapterList.tryEmit(chapterList.list)
			}
		}
	}

	@WorkerThread
	private fun loadChapterPath(chapterId : RealmUUID) = repository.getChapterPath(id = chapterId, includeEdge = true)

	fun putInnerChapter(parentId : RealmUUID?, title : String?, description : String?, color : Color?, thumbnail : Bitmap?) {
		ChapterObject().apply {
			this.title = title
			this.description = description
			this.color = color?.toArgb()
			this.thumbnail = thumbnail?.encodeBase64()

			this.parentId = parentId

			repository.putChapter(parentId = parentId, this) { _, _ -> }
		}
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
			repository.putChapter(parentId = this@NotebookScreenViewModel.parentId.value?.let { RealmUUID.Companion.from(it) }, this) { _, _ -> }
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
			repository.putChapter(parentId = this@NotebookScreenViewModel.parentId.value?.let { RealmUUID.Companion.from(it) }, this) { _, _ -> }
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
			repository.putChapter(parentId = this@NotebookScreenViewModel.parentId.value?.let { RealmUUID.Companion.from(it) }, this) { _, _ -> }
		}
	}

	fun setDefaultChapter(chapterId : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			repository.putDefaultChapterId(id = chapterId) { _ -> }
		}
	}

	fun delete(idList : List<RealmUUID>) = viewModelScope.launch(Dispatchers.Default) { repository.delete(idList) }

	fun deleteCurrentChapter(id : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			parentId.value?.let { loadChapter(chapterId = RealmUUID.Companion.from(it)) }
			repository.delete(listOf(id))
		}
	}
}

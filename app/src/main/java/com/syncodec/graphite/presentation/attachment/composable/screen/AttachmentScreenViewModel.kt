package com.syncodec.graphite.presentation.attachment.composable.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.repository.AttachmentRepository
import com.syncodec.graphite.di.repository.koinRepository.KoinRepository
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.utils.ContentStatus
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.io.File


@KoinViewModel
class AttachmentScreenViewModel(private val repository : KoinRepository, private val attachmentRepository : AttachmentRepository) : ViewModel() {

	val repositoryState = repository.repositoryState

	private val loadAll : MutableStateFlow<Boolean> = MutableStateFlow(false)
	private val noteId : MutableStateFlow<String?> = MutableStateFlow(null)
	private val chapterId : MutableStateFlow<String?> = MutableStateFlow(null)

	private var loadAllCoroutine : CoroutineScope? = null
	private var loadNoteCoroutine : CoroutineScope? = null
	private var loadChapterCoroutine : CoroutineScope? = null

	val contentStatus : MutableStateFlow<ContentStatus> = MutableStateFlow(ContentStatus.Init)
	val noteAttachmentListMap : MutableStateFlow<Map<NoteObjectLite, List<File>>> = MutableStateFlow(mapOf())

	val enableNoteNavigation : MutableStateFlow<Boolean> = MutableStateFlow(false)

	init {
		viewModelScope.launch(Dispatchers.Default) {
			combine(loadAll, repositoryState) { loadAll, repositoryState ->
				loadAll to repositoryState
			}.collect { (loadAll, repositoryState) ->
				if (loadAll && repositoryState == RepositoryState.SUCCESS) loadAllData()
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			combine(noteId, repositoryState) { noteId, repositoryState ->
				noteId to repositoryState
			}.collect { (noteId, repositoryState) ->
				if (! noteId.isNullOrEmpty() && repositoryState == RepositoryState.SUCCESS) loadNoteData(noteId)
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			combine(chapterId, repositoryState) { chapterId, repositoryState ->
				chapterId to repositoryState
			}.collect { (chapterId, repositoryState) ->
				if (! chapterId.isNullOrEmpty() && repositoryState == RepositoryState.SUCCESS) loadChapterData(chapterId)
			}
		}
	}

	private fun loadAllData() {
		viewModelScope.launch(Dispatchers.Default) {
			loadAllCoroutine?.cancel()
			loadAllCoroutine = this
			repository.getAllNoteLiteAsFlow().cancellable().collect {
				contentStatus.tryEmit(ContentStatus.Loading)
				val noteAttachmentListMap = mutableMapOf<NoteObjectLite, List<File>>()
				it.forEach { note ->
					attachmentRepository.getAttachmentFromNote(note.id).let { attachmentList ->
						if (attachmentList.isNotEmpty()) noteAttachmentListMap[note] = attachmentList
					}
				}
				this@AttachmentScreenViewModel.noteAttachmentListMap.tryEmit(noteAttachmentListMap)
				if (noteAttachmentListMap.isEmpty()) contentStatus.tryEmit(ContentStatus.LoadedEmpty) else contentStatus.tryEmit(ContentStatus.Loaded)
			}
		}
	}

	private fun loadNoteData(id : String) {
		contentStatus.tryEmit(ContentStatus.Loading)
		val noteId = try {
			RealmUUID.from(id)
		} catch (e : Exception) {
			contentStatus.tryEmit(ContentStatus.Error)
			null
		}
		noteId?.let {
			viewModelScope.launch(Dispatchers.Default) {
				loadNoteCoroutine?.cancel()
				loadNoteCoroutine = this
				repository.getNoteFromIdAsFlow(id = it).cancellable().collect {
					it?.let { noteObject ->
						attachmentRepository.getAttachmentFromNote(noteId = noteObject.id).let { attachmentList ->
							noteAttachmentListMap.tryEmit(mapOf(noteObject.toLite() to attachmentList))
							contentStatus.tryEmit(ContentStatus.Loaded)
						}
					} ?: contentStatus.tryEmit(ContentStatus.Error)
				}
			}
		}
	}

	private fun loadChapterData(id : String) {
		contentStatus.tryEmit(ContentStatus.Loading)
		val chapterId = try {
			RealmUUID.from(id)
		} catch (e : Exception) {
			contentStatus.tryEmit(ContentStatus.Error)
			null
		}
		chapterId?.let {
			viewModelScope.launch(Dispatchers.Default) {
				loadChapterCoroutine?.cancel()
				loadChapterCoroutine = this
				repository.getNoteWithParentIdAsFlow(parentId = it).cancellable().collect {
					val noteAttachmentListMap = mutableMapOf<NoteObjectLite, List<File>>()
					it.list.forEach { note ->
						attachmentRepository.getAttachmentFromNote(note.id).let { attachmentList ->
							if (attachmentList.isNotEmpty()) noteAttachmentListMap[note.toLite()] = attachmentList
						}
					}
					this@AttachmentScreenViewModel.noteAttachmentListMap.tryEmit(noteAttachmentListMap)
					if (noteAttachmentListMap.isEmpty()) contentStatus.tryEmit(ContentStatus.LoadedEmpty) else contentStatus.tryEmit(ContentStatus.Loaded)
				}
			}
		}
	}

	fun loadAll() {
		this.enableNoteNavigation.tryEmit(true)
		this.loadAll.tryEmit(true)
	}

	fun loadNote(noteId : RealmUUID) {
		this.enableNoteNavigation.tryEmit(false)
		this.noteId.tryEmit(noteId.toString())
	}

	fun loadChapter(chapterId : RealmUUID) {
		this.enableNoteNavigation.tryEmit(true)
		this.chapterId.tryEmit(chapterId.toString())
	}

	fun deleteAttachment(attachmentList : List<File>) {
		contentStatus.tryEmit(ContentStatus.Loading)

		viewModelScope.launch(Dispatchers.Default) {
			val currentNoteAttachmentListMap = noteAttachmentListMap.value
			val newNoteAttachmentListMap = mutableMapOf<NoteObjectLite, List<File>>()

			currentNoteAttachmentListMap.forEach { (note, currentAttachmentList) ->
				val newAttachmentList = currentAttachmentList.toMutableList()
				newAttachmentList.removeAll(attachmentList)
				if (newAttachmentList.isNotEmpty()) newNoteAttachmentListMap[note] = newAttachmentList
			}

			noteAttachmentListMap.tryEmit(newNoteAttachmentListMap)
			if (newNoteAttachmentListMap.isEmpty()) contentStatus.tryEmit(ContentStatus.LoadedEmpty) else contentStatus.tryEmit(ContentStatus.Loaded)
		}

		attachmentRepository.delete(attachmentList)
	}
}

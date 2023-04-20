package com.syncodec.graphite.presentation.attachment.composable.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.di.repository.koinRepository.KoinRepository
import com.syncodec.graphite.utils.LoaderStatus
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
class AttachmentScreenViewModel(private val repository : KoinRepository) : ViewModel() {

	val repositoryState = repository.repositoryState

	private val loadAll : MutableStateFlow<Boolean> = MutableStateFlow(false)
	private val noteId : MutableStateFlow<String?> = MutableStateFlow(null)
	private val chapterId : MutableStateFlow<String?> = MutableStateFlow(null)

	private var loadAllCoroutine : CoroutineScope? = null
	private var loadNoteCoroutine : CoroutineScope? = null
	private var loadChapterCoroutine : CoroutineScope? = null

	val loaderStatus : MutableStateFlow<LoaderStatus> = MutableStateFlow(LoaderStatus.Init)
	val noteAttachmentListMap : MutableStateFlow<Map<NoteObjectLite, List<File>>> = MutableStateFlow(mapOf())

	val enableNoteNavigation : MutableStateFlow<Boolean> = MutableStateFlow(false)

	init {
		viewModelScope.launch(Dispatchers.Default) {
			combine(loadAll, repositoryState) { loadAll, repositoryState ->
				loadAll to repositoryState
			}.collect { (loadAll, repositoryState) ->
				if (loadAll && repositoryState == RepositoryState.Success) loadAllData()
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			combine(noteId, repositoryState) { noteId, repositoryState ->
				noteId to repositoryState
			}.collect { (noteId, repositoryState) ->
				if (! noteId.isNullOrEmpty() && repositoryState == RepositoryState.Success) loadNoteData(noteId)
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			combine(chapterId, repositoryState) { chapterId, repositoryState ->
				chapterId to repositoryState
			}.collect { (chapterId, repositoryState) ->
				if (! chapterId.isNullOrEmpty() && repositoryState == RepositoryState.Success) loadChapterData(chapterId)
			}
		}
	}

	private fun loadAllData() {
		viewModelScope.launch(Dispatchers.Default) {
			loadAllCoroutine?.cancel()
			loadAllCoroutine = this
			repository.getAllNoteLiteAsFlow().cancellable().collect {
				loaderStatus.tryEmit(LoaderStatus.Loading)
				val noteAttachmentListMap = mutableMapOf<NoteObjectLite, List<File>>()
				it.forEach { note ->
					repository.attachmentRepository.getAttachmentFromNote(note.id).let { attachmentList ->
						if (attachmentList.isNotEmpty()) noteAttachmentListMap[note] = attachmentList
					}
				}
				this@AttachmentScreenViewModel.noteAttachmentListMap.tryEmit(noteAttachmentListMap)
				if (noteAttachmentListMap.isEmpty()) loaderStatus.tryEmit(LoaderStatus.LoadedEmpty) else loaderStatus.tryEmit(LoaderStatus.Loaded)
			}
		}
	}

	private fun loadNoteData(id : String) {
		loaderStatus.tryEmit(LoaderStatus.Loading)
		val noteId = try {
			RealmUUID.from(id)
		} catch (e : Exception) {
			loaderStatus.tryEmit(LoaderStatus.Error)
			null
		}
		noteId?.let {
			viewModelScope.launch(Dispatchers.Default) {
				loadNoteCoroutine?.cancel()
				loadNoteCoroutine = this
				repository.getNoteFromIdAsFlow(id = it).cancellable().collect {
					it?.let { noteObject ->
						repository.attachmentRepository.getAttachmentFromNote(parentId = noteObject.id).let { attachmentList ->
							noteAttachmentListMap.tryEmit(mapOf(noteObject.toLite() to attachmentList))
							loaderStatus.tryEmit(LoaderStatus.Loaded)
						}
					} ?: loaderStatus.tryEmit(LoaderStatus.Error)
				}
			}
		}
	}

	private fun loadChapterData(id : String) {
		loaderStatus.tryEmit(LoaderStatus.Loading)
		val chapterId = try {
			RealmUUID.from(id)
		} catch (e : Exception) {
			loaderStatus.tryEmit(LoaderStatus.Error)
			null
		}
		chapterId?.let {
			viewModelScope.launch(Dispatchers.Default) {
				loadChapterCoroutine?.cancel()
				loadChapterCoroutine = this
				repository.getNoteWithParentIdAsFlow(parentId = it).cancellable().collect {
					val noteAttachmentListMap = mutableMapOf<NoteObjectLite, List<File>>()
					it.list.forEach { note ->
						repository.attachmentRepository.getAttachmentFromNote(note.id).let { attachmentList ->
							if (attachmentList.isNotEmpty()) noteAttachmentListMap[note.toLite()] = attachmentList
						}
					}
					this@AttachmentScreenViewModel.noteAttachmentListMap.tryEmit(noteAttachmentListMap)
					if (noteAttachmentListMap.isEmpty()) loaderStatus.tryEmit(LoaderStatus.LoadedEmpty) else loaderStatus.tryEmit(LoaderStatus.Loaded)
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
		loaderStatus.tryEmit(LoaderStatus.Loading)

		viewModelScope.launch(Dispatchers.Default) {
			val currentNoteAttachmentListMap = noteAttachmentListMap.value
			val newNoteAttachmentListMap = mutableMapOf<NoteObjectLite, List<File>>()

			currentNoteAttachmentListMap.forEach { (note, currentAttachmentList) ->
				val newAttachmentList = currentAttachmentList.toMutableList()
				newAttachmentList.removeAll(attachmentList)
				if (newAttachmentList.isNotEmpty()) newNoteAttachmentListMap[note] = newAttachmentList
			}

			noteAttachmentListMap.tryEmit(newNoteAttachmentListMap)
			if (newNoteAttachmentListMap.isEmpty()) loaderStatus.tryEmit(LoaderStatus.LoadedEmpty) else loaderStatus.tryEmit(LoaderStatus.Loaded)
		}

		repository.deleteAttachment(attachmentList, )
	}
}

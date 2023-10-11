package com.syncodec.graphite.presentation.attachment.composable.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.repository.LockableRepo
import com.syncodec.graphite.di.repository.Repository
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.io.File


@KoinViewModel
class AttachmentScreenViewModel(private val lockableRepo: LockableRepo) : ViewModel() {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val loadAll: MutableStateFlow<Boolean> = MutableStateFlow(false)
	private val noteId: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)
	private val chapterId: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)

	private var loadAllCoroutine: CoroutineScope? = null
	private var loadNoteCoroutine: CoroutineScope? = null
	private var loadChapterCoroutine: CoroutineScope? = null

	val noteAttachmentListMap: MutableStateFlow<Map<NoteObjectLite, List<File>>> = MutableStateFlow(mapOf())

	val enableNoteNavigation: MutableStateFlow<Boolean> = MutableStateFlow(false)

	init {
		viewModelScope.launch(Dispatchers.Default) {
			lockableRepo.repositoryStatusFlow.collectLatest { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) _repository.tryEmit(repositoryStatus.repository)
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			this@AttachmentScreenViewModel._repository.collectLatest { repository1 ->
				if (repository1 != null) {
					launch { this@AttachmentScreenViewModel.loadAll.collectLatest { if (it) loadAllData(repository = repository1) } }
					launch { this@AttachmentScreenViewModel.noteId.collectLatest { if (it != null) loadNoteData(repository = repository1, id = it) } }
					launch { this@AttachmentScreenViewModel.chapterId.collectLatest { if (it != null) loadChapterData(repository = repository1, id = it) } }
				}
			}
		}
	}

	private fun loadAllData(repository: Repository) {
		Log.d("npr71", "loadAllData")
		viewModelScope.launch(Dispatchers.Default) {
			loadAllCoroutine?.cancel()
			loadAllCoroutine = this
			repository.getAllNoteLiteAsFlow().collect {
				val noteAttachmentListMap = mutableMapOf<NoteObjectLite, List<File>>()
				it.forEach { note ->
					repository.attachmentRepository.getAttachmentFromNote(note.id).let { attachmentList ->
						if (attachmentList.isNotEmpty()) noteAttachmentListMap[note] = attachmentList
					}
				}
				this@AttachmentScreenViewModel.noteAttachmentListMap.tryEmit(noteAttachmentListMap)
			}
		}
	}

	private fun loadNoteData(repository: Repository, id: RealmUUID) {
		Log.d("npr71", "loadNoteData")
		viewModelScope.launch(Dispatchers.Default) {
			loadNoteCoroutine?.cancel()
			loadNoteCoroutine = this
			repository.getNoteFromIdAsFlow(id = id).collect {
				it?.let { noteObject ->
					repository.attachmentRepository.getAttachmentFromNote(parentId = noteObject.id).let { attachmentList ->
						noteAttachmentListMap.tryEmit(mapOf(noteObject.toLite() to attachmentList))
					}
				}
			}
		}
	}

	private fun loadChapterData(repository: Repository, id: RealmUUID) {
		Log.d("npr71", "loadChapterData")
		viewModelScope.launch(Dispatchers.Default) {
			loadChapterCoroutine?.cancel()
			loadChapterCoroutine = this
			repository.getNoteWithParentIdAsFlow(parentId = id).collect {
				val noteAttachmentListMap = mutableMapOf<NoteObjectLite, List<File>>()
				it.forEach { note ->
					repository.attachmentRepository.getAttachmentFromNote(note.id).let { attachmentList ->
						if (attachmentList.isNotEmpty()) noteAttachmentListMap[note] = attachmentList
					}
				}
				this@AttachmentScreenViewModel.noteAttachmentListMap.tryEmit(noteAttachmentListMap)
			}
		}
	}

	fun loadAll() {
		this.enableNoteNavigation.tryEmit(true)
		this.loadAll.tryEmit(true)
	}

	fun loadNote(noteId: RealmUUID) {
		this.enableNoteNavigation.tryEmit(false)
		this.noteId.tryEmit(noteId)
	}

	fun loadChapter(chapterId: RealmUUID) {
		this.enableNoteNavigation.tryEmit(true)
		this.chapterId.tryEmit(chapterId)
	}

	fun deleteAttachment(attachmentList: List<File>) {
		viewModelScope.launch(Dispatchers.Default) {
			val currentNoteAttachmentListMap = noteAttachmentListMap.value
			val newNoteAttachmentListMap = mutableMapOf<NoteObjectLite, List<File>>()

			currentNoteAttachmentListMap.forEach { (note, currentAttachmentList) ->
				val newAttachmentList = currentAttachmentList.toMutableList()
				newAttachmentList.removeAll(attachmentList)
				if (newAttachmentList.isNotEmpty()) newNoteAttachmentListMap[note] = newAttachmentList
			}

			noteAttachmentListMap.tryEmit(newNoteAttachmentListMap)
		}
	}
}

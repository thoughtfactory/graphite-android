package com.syncodec.graphite.presentation.attachment

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.AttachmentObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.repository.RealmNotInitializedException
import com.syncodec.graphite.di.repository.Repository2
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.utils.Quadruple
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject


@HiltViewModel
class AttachmentViewModel @Inject constructor(private val repository2 : Repository2) : ViewModel() {

	val repositoryState = repository2.repositoryState

	val noteObject : MutableState<NoteObject?> = mutableStateOf(null)
	val chapterObject : MutableState<ChapterObject?> = mutableStateOf(null)

	val attachmentList : SnapshotStateList<Quadruple<AttachmentObject, File?, Uri?, RealmUUID>> = mutableStateListOf()

	val isSelected = mutableStateOf(false)
	val selectedAttachmentList = mutableStateListOf<Quadruple<AttachmentObject, File?, Uri?, RealmUUID>>()

	fun loadAllData() {
		viewModelScope.launch(Dispatchers.IO) {
			repositoryState.collect {
				when (it) {
					RepositoryState.INIT -> Log.d("AttachmentViewModel", "Init")
					RepositoryState.LOCKED -> null
					RepositoryState.LOADING -> Log.d("AttachmentViewModel", "Loading")
					RepositoryState.SUCCESS -> {
						viewModelScope.launch {
							if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
							try {
								repository2.getAllNote().forEach { note -> loadDataFromNote(note.id) }
							} catch (e : RealmNotInitializedException) {
							} catch (e : Exception) {
							}
						}
					}

					RepositoryState.ERROR -> Log.d("AttachmentViewModel", "Error")
				}
			}
		}
	}

	fun loadDataFromNote(noteId : RealmUUID) {
		viewModelScope.launch(Dispatchers.IO) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()

			try {
				repository2.getNoteFromIdAsFlow(noteId).collect {
					it?.let {_noteObject ->
						_noteObject.attachmentList.forEach {
							val attachmentObject = AttachmentObject.deserialize(it)
							if (attachmentObject != null) {
								val file = repository2.readAttachmentFile(_noteObject.id, attachmentObject.name)
								val uri = file?.let { it1 ->
									FileProvider.getUriForFile(repository2.context, "${repository2.context.packageName}.fileprovider", it1)
								}
								Quadruple(attachmentObject, file, uri, _noteObject.id).let { quadruple ->
									if (quadruple !in attachmentList) attachmentList.add(quadruple)
								}
							}
						}
					}
				}

			} catch (e : RealmNotInitializedException) {
			} catch (e : Exception) {
			}
		}
	}

	fun loadDataFromChapter(chapterId : RealmUUID) {
		viewModelScope.launch(Dispatchers.IO) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()

			repository2.getChapterFromIdAsFlow(chapterId).collect {
				withContext(Dispatchers.Main) {
					chapterObject.value = it

					it?.chapterList?.forEach { loadDataFromChapter(it.id) }
					it?.noteList?.forEach { loadDataFromNote(it.id) }
				}
			}
		}
	}

	fun deleteAttachment(callback: () -> Unit) {
		val _attachmentList = selectedAttachmentList.toList()
		viewModelScope.launch(Dispatchers.IO) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()

			val attachmentListIterator = _attachmentList.iterator()
			while(attachmentListIterator.hasNext()) {
				val attachment = attachmentListIterator.next()
				deleteAttachment(
					attachment.first,
					attachment.second,
					attachment.fourth,
					10
				)
			}

			callback()
		}
	}

	private suspend fun deleteAttachment(attachmentObject: AttachmentObject, file : File?, noteId: RealmUUID, retry: Int) {
		try {
			repository2.deleteAttachment(attachmentObject, file, noteId)
		} catch (e : Exception) {
			if (retry > 0) {
				delay(1000)
				deleteAttachment(attachmentObject, file, noteId, retry - 1)
			}
		}
	}
}

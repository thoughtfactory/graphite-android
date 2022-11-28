package com.syncodec.graphite.presentation.attachment

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.repository.RealmNotInitializedException
import com.syncodec.graphite.di.repository.Repository2
import com.syncodec.graphite.di.repository.RepositoryState
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

	val attachmentList : SnapshotStateList<Triple<RealmUUID, File?, Uri?>> = mutableStateListOf()

	val isSelected = mutableStateOf(false)
	val selectedAttachmentList = mutableStateListOf<Triple<RealmUUID, File?, Uri?>>()

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
								val _attachmentList : MutableList<Triple<RealmUUID, File?, Uri?>> = mutableListOf()
								repository2.getAllNote().forEach { note -> loadDataFromNote(note.id) { _attachmentList.addAll(it) } }
//								withContext(Dispatchers.Main) {
//									attachmentList.clear()
//									attachmentList.addAll(_attachmentList.toList())
//								}
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

	fun loadDataFromNote(noteId : RealmUUID, getAttachmentList : (List<Triple<RealmUUID, File?, Uri?>>) -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()

			try {
				val _attachmentList : MutableList<Triple<RealmUUID, File?, Uri?>> = mutableListOf()
				repository2.getNoteFromId(noteId).let {
					it?.let { _noteObject ->
						repository2.readAttachmentFromNoteId(_noteObject.id).forEach { attachment ->
//							_attachmentList.add(Triple(_noteObject.id, attachment.first, attachment.second))
							attachmentList.add(Triple(_noteObject.id, attachment.first, attachment.second))
						}
					}
				}

				getAttachmentList(_attachmentList)

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

					val _attachmentList : MutableList<Triple<RealmUUID, File?, Uri?>> = mutableListOf()
					it?.chapterList?.forEach { loadDataFromChapter(it.id) }
					it?.noteList?.forEach { loadDataFromNote(it.id) { _attachmentList.addAll(it) } }
//					withContext(Dispatchers.Main) {
//						attachmentList.clear()
//						attachmentList.addAll(_attachmentList.toList())
//					}
				}
			}
		}
	}

	fun deleteAttachment(callback : () -> Unit) {
		val _attachmentList = selectedAttachmentList.toList()
		viewModelScope.launch(Dispatchers.IO) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()

			val attachmentListIterator = _attachmentList.iterator()
			while (attachmentListIterator.hasNext()) {
				val attachment = attachmentListIterator.next()
				deleteAttachment(attachment.first, attachment.second, 10)
			}

			callback()
		}
	}

	private suspend fun deleteAttachment(noteId : RealmUUID, file : File?, retry : Int) {
		try {
			repository2.deleteAttachment(file)
		} catch (e : Exception) {
			if (retry > 0) {
				delay(1000)
				deleteAttachment(noteId, file, retry - 1)
			}
		}
	}
}

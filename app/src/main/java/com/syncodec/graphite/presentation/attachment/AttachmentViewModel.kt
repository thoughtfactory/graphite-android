package com.syncodec.graphite.presentation.attachment

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.AttachmentObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.repository.RealmNotInitializedException
import com.syncodec.graphite.di.repository.Repository2
import com.syncodec.graphite.di.repository.RepositoryState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject


@HiltViewModel
class AttachmentViewModel @Inject constructor(private val repository2 : Repository2) : ViewModel() {

	val repositoryState = repository2.repositoryState

	val noteObject : MutableState<NoteObject?> = mutableStateOf(null)
	val chapterObject : MutableState<ChapterObject?> = mutableStateOf(null)

	val attachmentList : SnapshotStateMap<ObjectId, Triple<AttachmentObject, File?, Uri?>> = mutableStateMapOf()

	fun loadAllAttachments() {
		viewModelScope.launch(Dispatchers.IO) {
			repositoryState.collect {
				when (it) {
					RepositoryState.INIT -> Log.d("AttachmentViewModel", "Init")
					RepositoryState.LOADING -> Log.d("AttachmentViewModel", "Loading")
					RepositoryState.SUCCESS -> {
						viewModelScope.launch {
							if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
							try {
								repository2.getAllAttachmentWithFileAsFlow().collect {
									withContext(Dispatchers.Main) {
										attachmentList.clear()
										attachmentList.putAll(it.associate { (first, second, third) -> first.id to Triple(first, second, third) })
									}
								}
							} catch (e: RealmNotInitializedException) {
							} catch (e: Exception) {
							}
						}
					}

					RepositoryState.ERROR -> Log.d("AttachmentViewModel", "Error")
				}
			}
		}
	}

	fun loadAndViewFromNoteData(noteId : ObjectId) {
		viewModelScope.launch(Dispatchers.IO) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()

			try {
				repository2.getAttachmentFromNote(noteId).collect {
					withContext(Dispatchers.Main) {
						attachmentList.clear()
//						attachmentList.putAll(it?.associate { (first, second, third) -> first.id to Triple(first, second, third) } ?: emptyMap())
					}
				}
			} catch (e: RealmNotInitializedException) {
			} catch (e: Exception) {
			}
		}
	}

	fun loadAndViewFromChapterData(chapterId : ObjectId) {
		viewModelScope.launch(Dispatchers.IO) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()

			repository2.getChapterFromIdAsFlow(chapterId).collect {
				withContext(Dispatchers.Main) {
					chapterObject.value = it

					it?.chapterList?.forEach { loadAndViewFromChapterData(it.id) }
					it?.noteList?.forEach { loadAndViewFromNoteData(it.id) }
				}
			}
		}
	}
}

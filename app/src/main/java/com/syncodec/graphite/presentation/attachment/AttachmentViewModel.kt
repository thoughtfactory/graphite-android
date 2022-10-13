package com.syncodec.graphite.presentation.attachment

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.di.Repository
import com.syncodec.graphite.di.Repository.getAttachmentFile
import com.syncodec.graphite.di.model.AttachmentObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObject
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class AttachmentViewModel(application: Application): AndroidViewModel(application) {

	val noteObject: MutableState<NoteObject?> = mutableStateOf(null)
	val chapterObject: MutableState<ChapterObject?> = mutableStateOf(null)

	val attachmentList: SnapshotStateMap<ObjectId, Triple<Uri, File, AttachmentObject>> = mutableStateMapOf()

	fun loadAllAttachments() {
		viewModelScope.launch {
			Repository.getAllAttachmentAsFlow().collect {
				attachmentList.clear()
				it.forEach { attachmentObject ->
					val file = getApplication<BaseApplication>().getAttachmentFile(attachmentObject.id, attachmentObject.extension)

					file?.let { it1 ->
						val uri = FileProvider.getUriForFile(getApplication<BaseApplication>(), "com.syncodec.fileprovider", it1)
						attachmentList[attachmentObject.id] = Triple(uri, file, attachmentObject.clone().apply { this.isSaved = true })
					}
				}
			}
		}
	}

	fun loadAndViewFromNoteData(noteId: ObjectId) {
		viewModelScope.launch(Dispatchers.IO) {
			Repository.getNote(noteId)?.let { note ->
				noteObject.value = note
				note.attachmentList.forEach { attachmentObject ->
					val file = getApplication<BaseApplication>().getAttachmentFile(attachmentObject.id, attachmentObject.extension)

					file?.let { it1 ->
						val uri = FileProvider.getUriForFile(getApplication<BaseApplication>(), "com.syncodec.fileprovider", it1)
						attachmentList[attachmentObject.id] = Triple(uri, file, attachmentObject.clone().apply { this.isSaved = true })
					}
				}
			}
		}
	}

	fun loadAndViewFromChapterData(chapterId: ObjectId) {
		viewModelScope.launch(Dispatchers.IO) {
			Repository.getChapter(chapterId)?.let { chapter ->
				chapterObject.value = chapter

				chapter.chapterList.forEach {
					loadAndViewFromChapterData(it.id)
				}

				chapter.noteList.forEach {
					loadAndViewFromNoteData(it.id)
				}
			}
		}
	}
}

package com.syncodec.momento.mainComponent

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.*
import com.syncodec.momento.MainActivity
import com.syncodec.momento.Momento
import com.syncodec.momento.database.bucketItem.BucketItemType
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.database.notebook.NotebookDbEntry
import com.syncodec.momento.miscellaneous.DataStore
import com.syncodec.momento.repository.BucketRepository
import com.syncodec.momento.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

	val dataStore = DataStore(this.getApplication())
	private val noteRepository: NoteRepository = NoteRepository.getInstance(momento = application as Momento)
	private val bucketRepository: BucketRepository = BucketRepository.getInstance(momento = application as Momento)

	lateinit var activityState: MainActivity.ActivityState

	var defaultNotebookKey: String? = null
	var defaultNoteList: SnapshotStateList<NoteDbEntry> = mutableStateListOf()
	val notebookList: SnapshotStateList<NotebookDbEntry> = mutableStateListOf()
	val bucketMap = bucketRepository.bucketMap

	fun initData() {
		viewModelScope.launch(Dispatchers.IO) {
			dataStore.getDefaultNotebookKey.collect {
				defaultNotebookKey = it
				if (it != null) {
					noteRepository.getNoteAsFlow(it).collect {
						defaultNoteList.removeAll { true }
						defaultNoteList.addAll(it   )
					}
				} else {
					val key = noteRepository.putNotebook(
						title = "Diary",
						description = "Default diary",
						color = Color(0xFF52616B).toArgb(),
						image = null
					)
					dataStore.putDefaultNotebookKey(key)
				}
			}
		}
		viewModelScope.launch(Dispatchers.IO) {
			noteRepository.notebookDbEntryListFlow.collect{
				notebookList.removeAll { true }
				notebookList.addAll(it)
			}
		}
	}

	fun deleteNote(key: String, notebookKey: String) {
		viewModelScope.launch {
			noteRepository.deleteNote(key = key)
		}
	}

	fun insertNotebook(
		title: String,
		description: String?,
		color: Int?,
		image: Bitmap?
	) {
		viewModelScope.launch {
			noteRepository.putNotebook(
				title = title,
				description = description,
				color = color,
				image = image
			)
		}
	}

	fun createNewBucket(
		bucketType: BucketItemType,
		title: String
	) {
		viewModelScope.launch {
			bucketRepository.putNewBucket(
				bucketType = bucketType,
				title = title
			)
		}
	}

	fun getNotebookImage(
		notebookKey: String
	): ImageBitmap? {
		return null
	}
}

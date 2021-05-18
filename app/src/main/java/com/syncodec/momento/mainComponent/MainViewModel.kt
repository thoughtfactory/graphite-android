package com.syncodec.momento.mainComponent

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.*
import com.syncodec.momento.MainActivity
import com.syncodec.momento.Momento
import com.syncodec.momento.database.bucket.BucketDbEntry
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.miscellaneous.DataStore
import com.syncodec.momento.repository.BucketRepository
import com.syncodec.momento.repository.NoteRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

	val dataStore = DataStore(this.getApplication())
	val noteRepository: NoteRepository = NoteRepository.getInstance(momento = application as Momento)
	val bucketRepository: BucketRepository = BucketRepository.getInstance(momento = application as Momento)

	lateinit var activityState: MainActivity.ActivityState

	var defaultNotebookKey: String? = null
	var defaultNoteList: LiveData<List<NoteDbEntry>> = MutableLiveData()

	suspend fun initDefaultNoteList() {
		dataStore.getDefaultNotebookKey.collect {
			defaultNotebookKey = it
			if (it != null) {
				defaultNoteList = noteRepository.getNoteAsLiveData(it)
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

	fun getBucketList(): LiveData<List<BucketDbEntry>> {
		return bucketRepository.getBucketListAsLiveData()
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
		bucketType: BucketItemType.Type,
		title: String
	) {
		viewModelScope.launch {
			bucketRepository.putBucket(
				bucketType = bucketType,
				title = title
			)
		}
	}

	fun getNotebookImage(
		notebookKey: String
	): ImageBitmap? {
		return getApplication<Momento>().getNotebookImage(notebookKey = notebookKey)
	}
}

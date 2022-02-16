package com.syncodec.momento.mainComponent

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.datastore.core.DataStore
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.momento.MainActivity
import com.syncodec.momento.Momento
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.database.diary.DiaryDbEntry
import com.syncodec.momento.repository.BucketRepository
import com.syncodec.momento.repository.DiaryRepository
import com.syncodec.momento.repository.NotebookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

	val diaryRepository: DiaryRepository = DiaryRepository(application)
	val notebookRepository: NotebookRepository = NotebookRepository(application)
	val bucketRepository: BucketRepository = BucketRepository(application)

	lateinit var mainActivityState: MainActivity.MainActivityState

	fun insertDiary(diaryDbEntry: DiaryDbEntry) {
		viewModelScope.launch {
			diaryRepository.insert(diaryDbEntry)
		}
	}

	fun moveDiaryToTrash(primaryKey: String) {
		viewModelScope.launch {
			diaryRepository.delete(primaryKey = primaryKey)
		}
//		viewModelScope.launch {
//			diaryRepository.moveToTrash(primaryKey)
//		}
	}

	fun deleteAllDiary() {
		viewModelScope.launch {
			withContext(Dispatchers.IO) {
				diaryRepository.deleteAll()
			}
		}
	}

	fun deleteAllBucket() {
		viewModelScope.launch {
			withContext(Dispatchers.IO) {
				bucketRepository.deleteAll()
				File("${(getApplication<Application>() as Momento).DATA}/").deleteRecursively()
			}
		}
	}

	fun deleteAllNotebook() {
		viewModelScope.launch {
			withContext(Dispatchers.IO) {
				notebookRepository.deleteAll()
				File("${(getApplication<Application>() as Momento).DATA}/").deleteRecursively()
			}
		}
	}

	fun insertNotebook(
		title: String,
		description: String?,
		color: Int?,
		image: Bitmap?
	) {
		viewModelScope.launch {
			notebookRepository.createNewNotebook(
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
			bucketRepository.createNewBucket(
				title = title,
				bucketType = bucketType
			)
		}
	}

	fun getNotebookImage(
		notebookKey: String
	): ImageBitmap? {
		return getApplication<Momento>().getNotebookImage(notebookKey = notebookKey)
	}
}

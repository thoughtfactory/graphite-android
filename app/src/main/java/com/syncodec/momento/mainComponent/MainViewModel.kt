package com.syncodec.momento.mainComponent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.momento.MainActivity
import com.syncodec.momento.Momento
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.database.diary.DiaryDbEntry
import com.syncodec.momento.database.notebook.NotebookDbEntry
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import com.syncodec.momento.repository.BucketRepository
import com.syncodec.momento.repository.DiaryRepository
import com.syncodec.momento.repository.NotebookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

	val diaryRepository: DiaryRepository = DiaryRepository(application)
	val notebookRepository: NotebookRepository = NotebookRepository(application)
	val bucketRepository: BucketRepository = BucketRepository(application)

	lateinit var mainActivityState: MainActivity.MainActivityState

	fun insertDiary(diaryDbEntry: DiaryDbEntry) {
		viewModelScope.launch {
			withContext(Dispatchers.IO) {
				diaryRepository.insert(diaryDbEntry)
			}
		}
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
		color: Long?
	) {
		viewModelScope.launch {
			notebookRepository.createNewNotebook(
				title = title,
				description = description,
				color = color
			)
		}
	}

	fun createNewBucket(
		bucketType: BucketItemType,
		title: String
	) {
		viewModelScope.launch {
			bucketRepository.createNewBucket(
				title = title,
				bucketType = bucketType
			)
		}
	}
}

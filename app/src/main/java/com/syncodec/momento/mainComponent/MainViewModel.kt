package com.syncodec.momento.mainComponent

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.syncodec.momento.MainActivity
import com.syncodec.momento.Momento
import com.syncodec.momento.database.bucket.BucketDbEntry
import com.syncodec.momento.database.bucket.BucketItemDbEntry
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

	val diaryRepository: DiaryRepository = DiaryRepository(momento = application as Momento)
	val notebookRepository: NotebookRepository = NotebookRepository(momento = application as Momento)
	val bucketRepository: BucketRepository = BucketRepository(momento = application as Momento)

	lateinit var activityState: MainActivity.ActivityState

	fun insertDiary(diaryDbEntry: DiaryDbEntry) {
		viewModelScope.launch {
			diaryRepository.insert(diaryDbEntry)
		}
	}

	fun getBucketList(): LiveData<List<BucketDbEntry>> {
		return bucketRepository.getBucketList()
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
//		viewModelScope.launch {
//			withContext(Dispatchers.IO) {
//				bucketRepository.deleteAll()
//				File("${(getApplication<Application>() as Momento).DATA}/").deleteRecursively()
//			}
//		}
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

package com.syncodec.momento.searchComponent

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.momento.Momento
import com.syncodec.momento.database.diary.Note
import com.syncodec.momento.repository.BucketRepository
import com.syncodec.momento.repository.DiaryRepository
import com.syncodec.momento.repository.NotebookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

	val diaryRepository: DiaryRepository = DiaryRepository.getInstance(momento = application as Momento)
	val notebookRepository: NotebookRepository = NotebookRepository(momento = application as Momento)
	val bucketRepository: BucketRepository = BucketRepository.getInstance(momento = application as Momento)

	lateinit var activityState: SearchActivity.ActivityState

	var noteKeyList: List<String> = listOf()
	val noteMap: SnapshotStateMap<String, Pair<Note, Boolean>> = mutableStateMapOf()

	fun readData() {
		viewModelScope.launch(Dispatchers.IO) {
			noteKeyList = diaryRepository.getAllKey()

			noteKeyList.forEach {
				noteMap[it] = Pair(diaryRepository.loadDiary(it), true)
			}
		}
	}
}

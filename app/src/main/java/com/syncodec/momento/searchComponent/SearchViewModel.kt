package com.syncodec.momento.searchComponent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.momento.Momento
import com.syncodec.momento.repository.BucketRepository
import com.syncodec.momento.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

	private val noteRepository: NoteRepository = NoteRepository.getInstance(momento = application as Momento)
	val bucketRepository: BucketRepository = BucketRepository.getInstance(momento = application as Momento)

	lateinit var activityState: SearchActivity.ActivityState

	var noteKeyList = noteRepository.getAllKey()
}

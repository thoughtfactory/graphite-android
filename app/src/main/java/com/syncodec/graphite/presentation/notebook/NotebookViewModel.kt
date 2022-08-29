package com.syncodec.graphite.presentation.notebook

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.Repository
import com.syncodec.graphite.di.model.ChapterObject
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class NotebookViewModel : ViewModel() {

	var notebook: MutableState<ChapterObject?> = mutableStateOf(null)

	fun initNotebook(chapterId: ObjectId) {
		viewModelScope.launch(Dispatchers.IO) {
			Repository.getChapterAsFlow(chapterId).collect {
				notebook.value = it
			}
		}
	}
}

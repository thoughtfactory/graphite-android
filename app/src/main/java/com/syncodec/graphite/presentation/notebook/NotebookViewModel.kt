package com.syncodec.graphite.presentation.notebook

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.Repository
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.utils.getRandomColor
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NotebookViewModel : ViewModel() {

	var notebook: MutableStateFlow<ChapterObject?> = MutableStateFlow(null)
	var currentChapterObject: MutableState<ChapterObject?> = mutableStateOf(null)

	val showEditChapterDialog: MutableState<Boolean> = mutableStateOf(false)

	fun initNotebook(chapterId: ObjectId) {
		viewModelScope.launch(Dispatchers.IO) {
			Repository.getChapterAsFlow(chapterId).collect {
				withContext(Dispatchers.Main) {
					notebook.value = it
					currentChapterObject.value = it
//					if (currentChapterObject.value == null) {
//						currentChapterObject.value = it
//					}
				}
			}
		}
	}

	fun putChapter(title: String, description: String, color: Color?, bitmap: Bitmap?) {
		if (currentChapterObject.value != null) {
			CoroutineScope(Dispatchers.IO).launch {
				try {
					ChapterObject().apply {
						this.title = title
						this.description = description
						this.color = color?.toArgb() ?: getRandomColor().toArgb()
						this.parentChapterId = currentChapterObject.value?.id

						try {
							Repository.putChapter(currentChapterObject.value!!.id, this)
						} catch (e: Exception) {
//							TODO Show error
							e.printStackTrace()
						}
					}
				} catch (e: Exception) {
//	    		    TODO Show error message
					e.printStackTrace()
				}
			}
		} else {
//		    TODO Show error message
		}
	}
}

package com.syncodec.graphite.presentation.common.dialog.whereDialog

import android.graphics.Bitmap
import androidx.annotation.WorkerThread
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.repository.koinRepository.KoinRepository
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.utils.encodeBase64
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class WhereDialogViewModel(val repository : KoinRepository) : ViewModel() {

	val repositoryState = repository.repositoryState

	val parentChapter : MutableStateFlow<ChapterObject?> = MutableStateFlow(null)
	private val allChapterList : MutableStateFlow<List<ChapterObject>> = MutableStateFlow(listOf())
	val visibleChapterList : MutableStateFlow<List<ChapterObject>> = MutableStateFlow(listOf())
	val chapterPath = MutableStateFlow<List<ChapterObjectLite>>(listOf())

	init {
		observeExplorer()

		viewModelScope.launch(Dispatchers.Default) {
			repositoryState.collect {
				if (it == RepositoryState.SUCCESS) observeChapter()
			}
		}
	}

	private suspend fun observeChapter() {
		repository.getAllChapterAsFlow().collect { allChapterList.tryEmit(it) }
	}

	private fun observeExplorer() {
		viewModelScope.launch(Dispatchers.Default) {
			combine(
				parentChapter,
				allChapterList
			) { parentChapter, allChapterList ->
				allChapterList.filter { it.parentId == parentChapter?.id }.let { this@WhereDialogViewModel.visibleChapterList.tryEmit(it) }
			}.collect()
		}
	}

	fun exploreChapter(chapterId : RealmUUID?) {
		viewModelScope.launch(Dispatchers.Default) {
			allChapterList.value.find { it.id == chapterId }.let { parentChapter.tryEmit(it) }
			loadChapterPath(chapterId = chapterId)
		}
	}

	@WorkerThread
	private fun loadChapterPath(chapterId : RealmUUID?) = repository.getChapterPath(id = chapterId, includeEdge = true).let { this@WhereDialogViewModel.chapterPath.tryEmit(it) }

	fun putChapter(parentChapterId : RealmUUID?, title : String?, description : String?, color : Color?, bitmap : Bitmap?) {
		if (BaseApplication.isPro.value) {
			try {
				ChapterObject().apply {
					this.modifiedTimestamp = System.currentTimeMillis()

					this.title = title
					this.description = description
					this.color = color?.toArgb()
					this.thumbnail = bitmap?.encodeBase64()

					this.parentId = parentChapterId

					repository.putChapterSuspended(this)
				}
			} catch (e : Exception) {
//	    		    TODO Show error message
//						e.printStackTrace()
			}
		} else {
//			viewModelScope.launch(Dispatchers.Main) {
//				Toast.makeText(repository.context, "Join Graphite Pro to add chapters in notebook", Toast.LENGTH_SHORT).show()
//			}
		}
	}
}

package com.syncodec.graphite.presentation.common.dialog.whereDialog

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.repository.Repository2
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.utils.encodeBase64
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class WhereDialogViewModel @Inject constructor(val repository2 : Repository2) : ViewModel() {

	val repositoryState = repository2.repositoryState

	val parentChapter : MutableStateFlow<ChapterObjectLite?> = MutableStateFlow(null)
	private val allChapterList : SnapshotStateList<ChapterObjectLite> = mutableStateListOf()
	val visibleChapterList : SnapshotStateList<ChapterObjectLite> = mutableStateListOf()
	val chapterPath = mutableStateListOf<ChapterObjectLite>()

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryState.collect {
				when (it) {
					RepositoryState.INIT -> null
					RepositoryState.LOCKED -> null
					RepositoryState.LOADING -> null
					RepositoryState.SUCCESS -> onRepositoryStateSuccess()
					RepositoryState.ERROR -> null
				}
			}
		}
	}

	private suspend fun onRepositoryStateSuccess() {
		repository2.getAllChapterAsFlow().map { it.map { it.toLite() } }.collect { chapterList ->
			withContext(Dispatchers.Main) {
				allChapterList.clear()
				allChapterList.addAll(chapterList)
				onExploreChapter(parentChapter.value?.id)
			}
		}
	}


//	private suspend fun onRepositoryStateSuccess() {
//		parentChapter.collect {
//
//			viewModelScope.launch(Dispatchers.Default) {
//				repository2.getChapterWithParentIdAsFlow(it?.id).collect { childChapterList ->
//
//				}
//			}
//
//			viewModelScope.launch(Dispatchers.Default) {
//				repository2.getChapterPath(it?.id).collect { chapterPath ->
//				}
//			}
//
//			repository2.getChapterWithParentId(it?.id).let {
//				repository2.getChapterPath(id = it.first?.id, includeEdge = true) { _chapterPath, _ ->
//					chapterPath.clear()
//					chapterPath.addAll(_chapterPath ?: listOf())
//				}
//
//				it.second.map { it.toLite() }.let {
//					withContext(Dispatchers.Main) {
//						childChapterList.clear()
//						childChapterList.addAll(it)
//					}
//				}
//			}
//		}
//	}

	fun onExploreChapter(chapterId : RealmUUID?) {
		viewModelScope.launch(Dispatchers.Main) {
			visibleChapterList.clear()
			visibleChapterList.addAll(allChapterList.filter { it.parentId == chapterId })
			parentChapter.tryEmit(allChapterList.find { it.id == chapterId })
		}
		viewModelScope.launch(Dispatchers.Default) {
			repository2.getChapterPath(id = chapterId, includeEdge = true) { _chapterPath, _ ->
				chapterPath.clear()
				chapterPath.addAll(_chapterPath ?: listOf())
			}
		}
	}

	fun putChapter(parentChapterId : RealmUUID?, title : String?, description : String?, color : Color?, bitmap : Bitmap?) {
		if (BaseApplication.isPro.value) {
			CoroutineScope(Dispatchers.Default).launch {
				try {
					ChapterObject().apply {
						this.modifiedTimestamp = System.currentTimeMillis()

						this.title = title
						this.description = description
						this.color = color?.toArgb()
						this.thumbnail = bitmap?.encodeBase64()

						this.parentId = parentChapterId

						repository2.putChapter(this.parentId, this) { _, _ -> }
					}
				} catch (e : Exception) {
//	    		    TODO Show error message
//						e.printStackTrace()
				}
			}
		} else {
			viewModelScope.launch(Dispatchers.Main) {
				Toast.makeText(repository2.context, "Join Graphite Pro to add chapters in notebook", Toast.LENGTH_SHORT).show()
			}
		}
	}

}

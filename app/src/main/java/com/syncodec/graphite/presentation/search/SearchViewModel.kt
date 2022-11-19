package com.syncodec.graphite.presentation.search

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.Repository2
import com.syncodec.graphite.di.repository.RepositoryState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class SearchViewModel @Inject constructor(private val repository2 : Repository2) : ViewModel() {

	val repositoryState = repository2.repositoryState

	private val noteList : SnapshotStateList<NoteObject> = mutableStateListOf()
	val tagList : SnapshotStateList<TagObject> = mutableStateListOf()

	val visibleNoteList : SnapshotStateList<NoteObject> = mutableStateListOf()

	var showResultScreen : MutableState<Boolean> = mutableStateOf(false)
	var parentChapterId : MutableStateFlow<RealmUUID?> = MutableStateFlow(null)
	var showFavourite : MutableStateFlow<Boolean> = MutableStateFlow(false)
	var showWithAttachments : MutableStateFlow<Boolean> = MutableStateFlow(false)
	var showTag : MutableStateFlow<TagObject?> = MutableStateFlow(null)
	var searchQuery : MutableStateFlow<String?> = MutableStateFlow(null)

	val parentChapter : MutableState<ChapterObjectLite?> = mutableStateOf(null)
	val chapterList : SnapshotStateList<ChapterObjectLite> = mutableStateListOf()
	val chapterPath : SnapshotStateList<ChapterObjectLite> = mutableStateListOf()

	val isSelected: MutableState<Boolean> = mutableStateOf(false)
	val selectedRealmUUIDList: SnapshotStateList<RealmUUID> = mutableStateListOf()

	init {
		onWhere(null)
	}

	init {
		viewModelScope.launch(Dispatchers.IO) {
			when (repositoryState.value) {
				RepositoryState.INIT -> Log.d("SearchViewModel", "Init")
				RepositoryState.LOCKED -> null
				RepositoryState.LOADING -> Log.d("SearchViewModel", "Loading")
				RepositoryState.SUCCESS -> onRepositoryStateSuccess()
				RepositoryState.ERROR -> Log.d("SearchViewModel", "Error")
			}
		}
	}

	private fun onRepositoryStateSuccess() {
		viewModelScope.launch(Dispatchers.IO) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
			repository2.getAllNoteAsFlow().collect {
				withContext(Dispatchers.Main) {
					noteList.clear()
					noteList.addAll(it)
				}
			}
		}

		viewModelScope.launch(Dispatchers.IO) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
			repository2.getAllTagAsFlow().collect {
				withContext(Dispatchers.Main) {
					tagList.clear()
					tagList.addAll(it)
				}
			}
		}

		viewModelScope.launch(Dispatchers.Main) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
			combine(
				parentChapterId,
				showFavourite,
				showWithAttachments,
				showTag,
				searchQuery
			) { parentChapterId, favourite, attachment, tag, query ->
				noteList.filter { note ->
					(note.parentChapterId == parentChapterId || parentChapterId == null) &&
							(! favourite || note.isFavourite) &&
							(! attachment || note.attachmentList.isNotEmpty()) &&
							(tag == null || tag.RealmUUIDList.contains(note.id)) &&
							(query == null || note.title?.contains(query, true) == true || note.content?.contains(query, true) == true)
				}
			}.cancellable().collect {
				withContext(Dispatchers.Main) {
					visibleNoteList.clear()
					visibleNoteList.addAll(it)
				}
			}
		}
	}

	fun showFavourite() {
		showResultScreen.value = true
		showFavourite.tryEmit(true)
		showWithAttachments.tryEmit(false)
		showTag.tryEmit(null)
		searchQuery.tryEmit(null)
	}

	fun showWithAttachment() {
		showResultScreen.value = true
		showFavourite.tryEmit(false)
		showWithAttachments.tryEmit(true)
		showTag.tryEmit(null)
		searchQuery.tryEmit(null)
	}

	fun showTag(tag : TagObject?) {
		showResultScreen.value = true
		showFavourite.tryEmit(false)
		showWithAttachments.tryEmit(false)
		showTag.tryEmit(tag)
		searchQuery.tryEmit(null)
	}

	fun searchInNotes(query : String) {
		showResultScreen.value = true
		showFavourite.tryEmit(false)
		showWithAttachments.tryEmit(false)
		showTag.tryEmit(null)
		searchQuery.tryEmit(query)
	}

	fun onWhere(chapterId: RealmUUID?) {
		viewModelScope.launch(Dispatchers.IO) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
			repository2.getChapterWithParentId(chapterId).let { (_chapterObject, _chapterList) ->
				withContext(Dispatchers.Main) {
					parentChapter.value = _chapterObject?.toLite()
					chapterList.clear()
					chapterList.addAll(_chapterList.map { it.toLite() })
					repository2.getParentChapterList(id = _chapterObject?.id, includeEdge = true) {_chapterPath, _ ->
						chapterPath.clear()
						chapterPath.addAll(_chapterPath ?: listOf())
					}
				}
			}
		}
	}

	fun setOnWhere(chapterObjectLite : ChapterObjectLite?) {
		parentChapterId.tryEmit(chapterObjectLite?.id)
		parentChapter.value = chapterObjectLite
	}
}

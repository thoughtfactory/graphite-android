package com.syncodec.graphite.presentation.atlas

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.repository.RealmNotInitializedException
import com.syncodec.graphite.di.repository.Repository2
import com.syncodec.graphite.di.repository.RepositoryState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class AtlasViewModel @Inject constructor(private val repository2 : Repository2) : ViewModel() {

	val repositoryState = repository2.repositoryState

	val chapterObject : MutableState<ChapterObjectLite?> = mutableStateOf(null)
	val noteList : SnapshotStateList<NoteObjectLite> = mutableStateListOf()

	val chapterList : SnapshotStateList<ChapterObjectLite> = mutableStateListOf()
	val chapterPath : SnapshotStateList<ChapterObjectLite> = mutableStateListOf()

	val isSelected : MutableState<Boolean> = mutableStateOf(false)
	val selectedRealmUUIDList : SnapshotStateList<RealmUUID> = mutableStateListOf()

	init {
		viewModelScope.launch(Dispatchers.IO) {
			repositoryState.collect {
				when (it) {
					RepositoryState.INIT -> null
					RepositoryState.LOCKED -> null
					RepositoryState.LOADING -> null
					RepositoryState.SUCCESS -> {
						viewModelScope.launch {
							if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
							try {
								repository2.getAllNoteLiteAsFlow().collect {
									withContext(Dispatchers.Main) {
										noteList.clear()
										noteList.addAll(it)
									}
								}
							} catch (e : RealmNotInitializedException) {
							} catch (e : Exception) {
							}
						}
					}

					RepositoryState.ERROR -> null
				}
			}
		}
	}

	fun onWhere(chapterId: RealmUUID?) {
		viewModelScope.launch(Dispatchers.IO) {
			if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
			repository2.getChapterWithParentId(chapterId).let { (_chapterObject, _chapterList) ->
				withContext(Dispatchers.Main) {
					chapterObject.value = _chapterObject?.toLite()
					chapterList.clear()
					chapterList.addAll(_chapterList.map { it.toLite() })
					repository2.getChapterPath(id = _chapterObject?.id, includeEdge = true) { _chapterPath, _ ->
						chapterPath.clear()
						chapterPath.addAll(_chapterPath ?: listOf())
					}
				}
			}
		}
	}

	fun setOnWhere(chapterObjectLite : ChapterObjectLite?) {
		chapterObject.value = chapterObjectLite
	}

	fun delete() {
		try {
			val toDeleteRealmUUIDList = selectedRealmUUIDList.toList()
			repository2.delete(toDeleteRealmUUIDList)
			selectedRealmUUIDList.clear()
			isSelected.value = false
		} catch (e : Exception) {

		}
	}
}

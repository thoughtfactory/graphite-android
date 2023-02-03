package com.syncodec.graphite.presentation.main.composable.screen.noteScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.KoinRepository
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.utils.ContentStatus
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class NoteScreenViewModel(private val repository : KoinRepository) : ViewModel() {

	val repositoryState = repository.repositoryState

	private var noteObserverCoroutine : CoroutineScope? = null
	private var defaultChapterObserverCoroutine : CoroutineScope? = null

	val defaultChapterId : MutableStateFlow<RealmUUID?> = MutableStateFlow(null)
	val noteList : MutableStateFlow<List<NoteObjectLite>> = MutableStateFlow(listOf())

	val isNoteRefreshing : MutableStateFlow<Boolean> = MutableStateFlow(false)
	val contentStatus : MutableStateFlow<ContentStatus> = MutableStateFlow(ContentStatus.Init)

	val tagList : MutableStateFlow<List<TagObject>> = MutableStateFlow(listOf())

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryState.collect {
				when (it) {
					RepositoryState.SUCCESS -> {
						observeNotes()
						observeTags()
					}
					else -> null
				}
			}
		}
	}

	private fun observeNotes() {
		viewModelScope.launch(Dispatchers.Default) {
			contentStatus.tryEmit(ContentStatus.Loading)
			isNoteRefreshing.tryEmit(true)
			noteObserverCoroutine?.cancel()
			noteObserverCoroutine = this
			repository.getDefaultChapterIdAsFlow().collect { defaultChapterId ->
				defaultChapterObserverCoroutine?.cancel()
				defaultChapterObserverCoroutine = this
				this@NoteScreenViewModel.defaultChapterId.tryEmit(defaultChapterId)
				defaultChapterId?.let {
					repository.getNoteWithParentIdAsFlow(parentId = it).cancellable().collect { noteObjectResultsChange ->
						noteObjectResultsChange.list.let { noteObjectList ->
							this@NoteScreenViewModel.noteList.tryEmit(noteObjectList.map { it.toLite() })
							if (noteObjectList.isEmpty()) contentStatus.tryEmit(ContentStatus.LoadedEmpty) else contentStatus.tryEmit(ContentStatus.Loaded)
							isNoteRefreshing.tryEmit(false)
						}
					}
				}
			}
		}
	}

	private fun observeTags() {
		viewModelScope.launch(Dispatchers.Default) {
			repository.getAllTagAsFlow().collect {
				tagList.tryEmit(it)
			}
		}
	}

	fun refresh() = observeNotes()

	fun delete(idList : List<RealmUUID>) = viewModelScope.launch(Dispatchers.Default) { repository.delete(idList) }
}

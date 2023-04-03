package com.syncodec.graphite.presentation.main.composable.screen.noteScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.fido.fido2.api.common.RSAAlgorithm
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.di.repository.koinRepository.KoinRepository
import com.syncodec.graphite.utils.ContentStatus
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import kotlin.random.Random


@KoinViewModel
class NoteScreenViewModel(private val repository : KoinRepository) : ViewModel() {

	val repositoryState = repository.repositoryState

	private var noteObserverCoroutine : CoroutineScope? = null
	private var defaultChapterObserverCoroutine : CoroutineScope? = null
	private var tagObserverCoroutine : CoroutineScope? = null
	private var observeAllCoroutine : CoroutineScope? = null

	val defaultChapterId : MutableStateFlow<ByteArray?> = MutableStateFlow(null)

	val contentStatus : MutableStateFlow<ContentStatus<List<NoteObjectLite>>> = MutableStateFlow(ContentStatus.Init)

	private val noteList : MutableStateFlow<List<NoteObjectLite>> = MutableStateFlow(listOf())
	val tagList : MutableStateFlow<List<TagObject>> = MutableStateFlow(listOf())

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryState.collect {
				when (it) {
					RepositoryState.SUCCESS -> refresh()

					else -> null
				}
			}
		}
	}

	private fun observeDefaultChapter() {
		viewModelScope.launch(Dispatchers.Default) {
			defaultChapterObserverCoroutine?.cancel()
			defaultChapterObserverCoroutine = this
			repository.getDefaultChapterIdAsFlow().cancellable().collect { defaultChapterId.tryEmit(it?.bytes) }
		}
	}

	private fun observeNotes() {
		viewModelScope.launch(Dispatchers.Default) {
			noteObserverCoroutine?.cancel()
			noteObserverCoroutine = this
			repository.getAllNoteLiteAsFlow().cancellable().collect { noteList.tryEmit(it) }
		}
	}

	private fun observeAll() {
		viewModelScope.launch(Dispatchers.Default) {
			contentStatus.tryEmit(ContentStatus.Loading)
			observeAllCoroutine?.cancel()
			observeAllCoroutine = this
			combine(
				defaultChapterId,
				noteList,
			) { defaultChapterId, noteList ->
				noteList.filter { it.parentId?.bytes.contentEquals(defaultChapterId) }
			}.cancellable().collect {
				if (it.isEmpty()) contentStatus.tryEmit(ContentStatus.LoadedEmpty)
				else contentStatus.tryEmit(ContentStatus.Loaded(it))
			}
		}
	}


	private fun observeTags() {
		viewModelScope.launch(Dispatchers.Default) {
			tagObserverCoroutine?.cancel()
			tagObserverCoroutine = this
			repository.getAllTagAsFlow().cancellable().collect { tagList.tryEmit(it) }
		}
	}

	fun refresh() {
		observeAll()
		observeDefaultChapter()
		observeNotes()
		observeTags()
	}

	fun delete(idList : List<RealmUUID>) {
		repository.deleteSuspended(idList)
	}

	fun addDebugData() {
		CoroutineScope(Dispatchers.Default).launch {
			for (i in 0 .. 10) {
				NoteObject.getRandomInstance().apply {
					this.id = RealmUUID.random()
					this.title = "Note ${Random.nextInt()}}"
					this.content =
						"{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":\"left\"},\"content\":[{\"type\":\"text\",\"text\":\"${
							"Content ${this.id}"
						}\"}]}]}"
					this.parentId = defaultChapterId.value?.let { RealmUUID.from(it) }
					repository.putNote(this)
				}
			}
		}
	}
}

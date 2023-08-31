package com.syncodec.graphite.presentation.notebook.composable

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import com.syncodec.graphite.utils.encodeBase64
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class NotebookScreenViewModel2(
	repositoryStateFlow: MutableStateFlow<Repository.Companion.RepositoryStatus>,
	dataStoreInstance: DataStoreInstance
) : ViewModel() {

	private val repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _currentChapterId: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)

	private val _defaultChapterId: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)
	val defaultChapterId: StateFlow<RealmUUID?> = _defaultChapterId

	private val _currentChapter: MutableStateFlow<ChapterObject?> = MutableStateFlow(null)
	val currentChapter: StateFlow<ChapterObject?> = _currentChapter

	private val _currentChapterPath: MutableStateFlow<List<ChapterObjectLite>> = MutableStateFlow(listOf())
	val currentChapterPath: StateFlow<List<ChapterObjectLite>> = _currentChapterPath

	private val _unorderedChapterList: MutableStateFlow<List<ChapterObject>> = MutableStateFlow(listOf())
	private val _chapterList: MutableStateFlow<List<ChapterObject>> = MutableStateFlow(listOf())
	val chapterList: StateFlow<List<ChapterObject>> = _chapterList

	private val _unorderedNoteList: MutableStateFlow<List<NoteObjectLite>> = MutableStateFlow(listOf())
	private val _noteList: MutableStateFlow<List<NoteObjectLite>> = MutableStateFlow(listOf())
	val noteList: StateFlow<List<NoteObjectLite>> = _noteList

	private var observeCurrentChapterJob: Job? = null
	private var observeChildChapterJob: Job? = null
	private var observeChildNoteJob: Job? = null

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryStateFlow.collect { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) repository.tryEmit(repositoryStatus.repository)
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			combine(repository, _currentChapterId) { repository1, currentChapterId1 -> Pair(repository1, currentChapterId1) }.collectLatest { (repository1, currentChapterId1) ->
				currentChapterId1?.let {
					observeCurrentChapter(chapterId = it)
					observeChild(parentId = it)
				}
			}
		}

//		Sorts child chapter list
		viewModelScope.launch(Dispatchers.Default) {
			combine(
				_unorderedChapterList,
				_currentChapter,
				dataStoreInstance.getSortBy,
				dataStoreInstance.getSortOn,
			) { unorderedObjectList, chapterObject, sortBy, sortOn ->
				when (sortOn) {
					SortOn.Title -> if (sortBy == SortBy.Ascending) unorderedObjectList.sortedBy { it.title } else unorderedObjectList.sortedByDescending { it.title }
					SortOn.Timestamp -> if (sortBy == SortBy.Ascending) unorderedObjectList.sortedBy { it.createdTimestamp } else unorderedObjectList.sortedByDescending { it.createdTimestamp }
					SortOn.Modified -> if (sortBy == SortBy.Ascending) unorderedObjectList.sortedBy { it.modifiedTimestamp } else unorderedObjectList.sortedByDescending { it.modifiedTimestamp }
//					SortOn.Custom -> unorderedObjectList.sortedBy { idOrderList.indexOf(it.id) }
					else -> if (sortBy == SortBy.Ascending) unorderedObjectList.sortedBy { it.title } else unorderedObjectList.sortedByDescending { it.title }
				}
			}.collectLatest {
				this@NotebookScreenViewModel2._chapterList.tryEmit(it)
			}
		}

//		Sorts child note list
		viewModelScope.launch(Dispatchers.Default) {
			combine(
				_unorderedNoteList,
				_currentChapter,
				dataStoreInstance.getSortBy,
				dataStoreInstance.getSortOn,
			) { unorderedObjectList, chapterObject, sortBy, sortOn ->
				when (sortOn) {
					SortOn.Title -> if (sortBy == SortBy.Ascending) unorderedObjectList.sortedBy { it.title } else unorderedObjectList.sortedByDescending { it.title }
					SortOn.Timestamp -> if (sortBy == SortBy.Ascending) unorderedObjectList.sortedBy { it.createdTimestamp } else unorderedObjectList.sortedByDescending { it.createdTimestamp }
					SortOn.Modified -> if (sortBy == SortBy.Ascending) unorderedObjectList.sortedBy { it.modifiedTimestamp } else unorderedObjectList.sortedByDescending { it.modifiedTimestamp }
//					SortOn.Custom -> unorderedObjectList.sortedBy { idOrderList.indexOf(it.id) }
					else -> if (sortBy == SortBy.Ascending) unorderedObjectList.sortedBy { it.title } else unorderedObjectList.sortedByDescending { it.title }
				}
			}.collectLatest {
				this@NotebookScreenViewModel2._noteList.tryEmit(it)
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			repository.collectLatest { repository1 ->
				repository1?.getDefaultChapterIdAsFlow()?.collectLatest {
					this@NotebookScreenViewModel2._defaultChapterId.tryEmit(it)
				}
			}
		}
	}

	fun loadChapter(chapterId: RealmUUID) {
		this._currentChapterId.tryEmit(chapterId)
	}

	private fun observeCurrentChapter(chapterId: RealmUUID) {
		observeCurrentChapterJob?.cancel()
		observeCurrentChapterJob = viewModelScope.launch(Dispatchers.Default) {
			repository.value?.getChapterFromIdAsFlow(id = chapterId)?.cancellable()?.collectLatest {
				this@NotebookScreenViewModel2._currentChapter.tryEmit(it)
				this@NotebookScreenViewModel2._currentChapterPath.tryEmit(repository.value?.getChapterPath(id = it?.id, includeEdge = true) ?: listOf())
			}
		}
	}

	private fun observeChild(parentId: RealmUUID) {
		observeChildChapterJob?.cancel()
		observeChildChapterJob = viewModelScope.launch(Dispatchers.Default) {
			repository.value?.getChapterWithParentIdAsFlow(parentId = parentId)?.cancellable()?.collect {
				this@NotebookScreenViewModel2._unorderedChapterList.tryEmit(it.list)
			}
		}

		observeChildNoteJob?.cancel()
		observeChildChapterJob = viewModelScope.launch(Dispatchers.Default) {
			repository.value?.getNoteWithParentIdAsFlow(parentId = parentId)?.cancellable()?.collect {
				this@NotebookScreenViewModel2._unorderedNoteList.tryEmit(it.list.map { it.toLite() })
			}
		}
	}

	fun putChapter(
		chapterId: RealmUUID?,
		title: String,
		description: String,
		color: Color?,
		bitmap: Bitmap?,
	) {
		viewModelScope.launch(Dispatchers.Default) {
//			Save new chapter
			if (chapterId == null) {
				ChapterObject().apply {
					apply {
						this.title = title
						this.description = description
						this.color = color?.toArgb()
						this.thumbnail = bitmap?.encodeBase64()

						this.parentId = this@NotebookScreenViewModel2.currentChapter.value?.id

						repository.value?.putChapterSuspended(this)
					}
				}
			}
			else {
//				Edit chapter
				repository.value?.getChapterFromId(id = chapterId)?.clone()?.apply {
					apply {
						this.title = title
						this.description = description
						if ((color != null) xor (bitmap != null)) {
							this.color = color?.toArgb()
							this.thumbnail = bitmap?.encodeBase64()
						}

						repository.value?.putChapterSuspended(this)
					}
				}
			}
		}
	}

	fun setDefaultChapter(id : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			repository.value?.putDefaultChapterId(id = id)
		}
	}

	fun toggleFavourite(chapterObject: ChapterObject) {
		chapterObject.clone().apply {
			this.isFavourite = !this.isFavourite
			repository.value?.putChapterSuspended(chapterObject = this)
		}
	}

	fun toggleLock(chapterObject: ChapterObject) {
		chapterObject.clone().apply {
			this.isLocked = !this.isLocked
			repository.value?.putChapterSuspended(chapterObject = this)
		}
	}

	fun toggleFavourite(idList: Set<RealmUUID>) {
		viewModelScope.launch(Dispatchers.Default) {
			val areAllFavourite = noteList.value.filter { it.id in idList }.all { it.isFavourite } && chapterList.value.filter { it.id in idList }.all { it.isFavourite }
			repository.value?.let { repo ->
				idList.forEach {
					repo.getNoteFromId(it)?.clone()?.apply {
						this.isFavourite = !areAllFavourite
						repo.putNoteSuspended(noteObject = this)
					}
				}
				idList.forEach {
					repo.getChapterFromId(it)?.clone()?.apply {
						this.isFavourite = !areAllFavourite
						repo.putChapterSuspended(chapterObject = this)
					}
				}
			}
		}
	}

	fun toggleLock(idList: Set<RealmUUID>) {
		viewModelScope.launch(Dispatchers.Default) {
			val areAllLocked = noteList.value.filter { it.id in idList }.all { it.isLocked } && chapterList.value.filter { it.id in idList }.all { it.isLocked }
			repository.value?.let { repo ->
				idList.forEach {
					repo.getNoteFromId(it)?.clone()?.apply {
						this.isLocked = !areAllLocked
						repo.putNoteSuspended(noteObject = this)
					}
				}
				idList.forEach {
					repo.getChapterFromId(it)?.clone()?.apply {
						this.isLocked = !areAllLocked
						repo.putChapterSuspended(chapterObject = this)
					}
				}
			}
		}
	}
}

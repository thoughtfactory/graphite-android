package com.syncodec.graphite.presentation.notebook.composable

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.LockableRepo
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.utils.encodeBase64
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class NotebookScreenViewModel2(
	lockableRepo: LockableRepo
) : ViewModel() {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _currentChapterId: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)

	private val _defaultChapterId: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)
	val defaultChapterId: StateFlow<RealmUUID?> = _defaultChapterId

	private val _currentChapter: MutableStateFlow<ChapterObject?> = MutableStateFlow(null)
	val currentChapter: StateFlow<ChapterObject?> = _currentChapter

	private val _currentChapterPath: MutableStateFlow<List<ChapterObjectLite>> = MutableStateFlow(listOf())
	val currentChapterPath: StateFlow<List<ChapterObjectLite>> = _currentChapterPath

	private val _chapterList: MutableStateFlow<List<ChapterObject>> = MutableStateFlow(listOf())
	val chapterList: StateFlow<List<ChapterObject>> = _chapterList

	private val _noteList: MutableStateFlow<List<NoteObjectLite>> = MutableStateFlow(listOf())
	val noteList: StateFlow<List<NoteObjectLite>> = _noteList

	private val _tagList: MutableStateFlow<List<TagObject>> = MutableStateFlow(listOf())
	val tagList: StateFlow<List<TagObject>> = _tagList

	private val _chapterNoteItemCount: MutableStateFlow<Map<RealmUUID?, Int>> = MutableStateFlow(mapOf())
	val chapterNoteItemCount: StateFlow<Map<RealmUUID?, Int>> = _chapterNoteItemCount

	private val _chapterChapterItemCount: MutableStateFlow<Map<RealmUUID?, Int>> = MutableStateFlow(mapOf())
	val chapterChapterItemCount: StateFlow<Map<RealmUUID?, Int>> = _chapterChapterItemCount

	private var observeChildChapterJob: Job? = null
	private var observeChildNoteJob: Job? = null

	init {
		viewModelScope.launch(Dispatchers.Default) {
			lockableRepo.repositoryStatusFlow.collect { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) _repository.tryEmit(repositoryStatus.repository)
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			combine(_repository, _currentChapterId) { repository1, currentChapterId1 -> Pair(repository1, currentChapterId1) }.collectLatest { (repository1, currentChapterId1) ->
				Log.d("npr71", "currentChapterId1 : $currentChapterId1 ${repository1!=null}")
				if (repository1 != null && currentChapterId1!=null) {
					launch { observeDefaultChapter(repository = repository1) }
					launch { observeTags(repository = repository1) }
					launch { observeNoteCount(repository = repository1) }
					launch { observeChapterCount(repository = repository1) }
					launch { observeChapter(repository = repository1, currentChapterId = currentChapterId1) }
					launch { observeChildChapters(repository = repository1, currentChapterId = currentChapterId1) }
					launch { observeChildNotes(repository = repository1, currentChapterId = currentChapterId1) }
				}
			}
		}
	}

	fun loadChapter(chapterId: RealmUUID) {
		this._currentChapterId.tryEmit(chapterId)
	}

	private suspend fun observeDefaultChapter(repository: Repository) {
		repository.getDefaultChapterIdAsFlow().collectLatest {
			this@NotebookScreenViewModel2._defaultChapterId.tryEmit(it)
		}
	}

	private suspend fun observeChapter(repository: Repository, currentChapterId: RealmUUID) {
		repository.getChapterFromIdAsFlow(id = currentChapterId).collectLatest { chapterObject1 ->
			Log.d("npr71", "chapterObject1 : ${chapterObject1?.title}")
			this@NotebookScreenViewModel2._currentChapter.tryEmit(chapterObject1)
			this@NotebookScreenViewModel2._currentChapterPath.tryEmit(_repository.value?.getChapterPath(id = chapterObject1?.id, includeEdge = true) ?: listOf())
		}
	}

	private fun observeChildChapters(repository: Repository, currentChapterId: RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			observeChildChapterJob?.cancel()
			Job().let { job ->
				observeChildChapterJob = job
				launch(Dispatchers.Default + job) {
					repository.getChapterWithParentIdAsFlow(parentId = currentChapterId).collectLatest { chapterList ->
						Log.d("npr71", "found ${chapterList.size} chapters")
						this@NotebookScreenViewModel2._chapterList.tryEmit(chapterList)
					}
				}
			}
		}
	}

	private fun observeChildNotes(repository: Repository, currentChapterId: RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			observeChildNoteJob?.cancel()
			Job().let { job ->
				observeChildNoteJob = job
				launch(Dispatchers.Default + job) {
					repository.getNoteWithParentIdAsFlow(parentId = currentChapterId).collectLatest { noteList ->
						Log.d("npr71", "found ${noteList.size} notes")
						this@NotebookScreenViewModel2._noteList.tryEmit(noteList)
					}
				}
			}
		}
	}

	private suspend fun observeTags(repository: Repository) {
		repository.getAllTagAsFlow().collectLatest {
			this._tagList.tryEmit(it)
		}
	}

	private suspend fun observeNoteCount(repository: Repository) {
		repository.getAllNoteAsFlow().collectLatest { noteObjectList ->
			noteObjectList.groupingBy { it.parentId }.eachCount().let { _chapterNoteItemCount.tryEmit(it) }
		}
	}

	private suspend fun observeChapterCount(repository: Repository) {
		repository.getAllChapterAsFlow().collectLatest {
			it.groupingBy { it.parentId }.eachCount().let { _chapterChapterItemCount.tryEmit(it) }
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

						_repository.value?.putChapterSuspended(this)
					}
				}
			} else {
//				Edit chapter
				_repository.value?.getChapterFromId(id = chapterId)?.clone()?.apply {
					apply {
						this.title = title
						this.description = description
						if ((color != null) xor (bitmap != null)) {
							this.color = color?.toArgb()
							this.thumbnail = bitmap?.encodeBase64()
						}

						_repository.value?.putChapterSuspended(this)
					}
				}
			}
		}
	}

	fun setDefaultChapter(id: RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			_repository.value?.putDefaultChapterId(id = id)
		}
	}

	fun toggleFavourite(chapterObject: ChapterObject) {
		_repository.value?.setObjectFromIdSuspended<ChapterObject>(id = chapterObject.id) {
			this.updateModifyTimestamp()
			this.isFavourite = this.isFavourite.not()
		}
	}

	fun toggleLock(chapterObject: ChapterObject) {
		_repository.value?.setObjectFromIdSuspended<ChapterObject>(id = chapterObject.id) {
			this.updateModifyTimestamp()
			this.isLocked = this.isLocked.not()
		}
	}

	fun toggleFavourite(idList: Set<RealmUUID>) {
		val areAllFavourite = noteList.value.filter { it.id in idList }.all { it.isFavourite } && chapterList.value.filter { it.id in idList }.all { it.isFavourite }
		_repository.value?.setMultiObjectFromIdSuspended<ChapterObject>(idList = idList) {
			this.updateModifyTimestamp()
			this.isFavourite = !areAllFavourite
		}
		_repository.value?.setMultiObjectFromIdSuspended<NoteObject>(idList = idList) {
			this.updateModifyTimestamp()
			this.isFavourite = !areAllFavourite
		}
	}

	fun toggleLock(idList: Set<RealmUUID>) {
		val areAllLocked = noteList.value.filter { it.id in idList }.all { it.isLocked } && chapterList.value.filter { it.id in idList }.all { it.isLocked }
		_repository.value?.setMultiObjectFromIdSuspended<ChapterObject>(idList = idList) {
			this.updateModifyTimestamp()
			this.isLocked = !areAllLocked
		}
		_repository.value?.setMultiObjectFromIdSuspended<NoteObject>(idList = idList) {
			this.updateModifyTimestamp()
			this.isLocked = !areAllLocked
		}
	}

	fun delete(idList: Set<RealmUUID>) {
		_repository.value?.deleteSuspended(idList = idList)
	}
}

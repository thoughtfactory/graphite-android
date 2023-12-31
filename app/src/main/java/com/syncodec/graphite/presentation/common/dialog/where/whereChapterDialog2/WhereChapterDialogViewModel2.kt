package com.syncodec.graphite.presentation.common.dialog.where.whereChapterDialog2

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.local.ChapterObject
import com.syncodec.graphite.di.model.local.ChapterObjectLite
import com.syncodec.graphite.di.repository.LockableRepo
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.utils.encodeBase64
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class WhereChapterDialogViewModel2(lockableRepo: LockableRepo) : ViewModel() {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _currentChapterId: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)

	private val _allChapterList: MutableStateFlow<List<ChapterObject>> = MutableStateFlow(listOf())

	private val _currentChapterPath: MutableStateFlow<List<ChapterObjectLite>> = MutableStateFlow(listOf())
	val currentChapterPath: StateFlow<List<ChapterObjectLite>> = _currentChapterPath

	private val _childChapterList: MutableStateFlow<List<ChapterObject>> = MutableStateFlow(listOf())
	val childChapterObject: StateFlow<List<ChapterObject>> = _childChapterList

	private val _childChapterCountMap: MutableStateFlow<Map<RealmUUID?, Int>> = MutableStateFlow(mapOf())
	val childChapterCountMap: StateFlow<Map<RealmUUID?, Int>> = _childChapterCountMap

	private val _childNoteCountMap: MutableStateFlow<Map<RealmUUID?, Int>> = MutableStateFlow(mapOf())
	val childNoteCountMap: StateFlow<Map<RealmUUID?, Int>> = _childNoteCountMap

	init {
		viewModelScope.launch(Dispatchers.Default) {
			lockableRepo.repositoryStatusFlow.collect { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) _repository.tryEmit(repositoryStatus.repository)
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			_repository.collectLatest { repository1 ->
				repository1?.getAllChapterAsFlow()?.collectLatest { chapterObjectList ->
					_allChapterList.tryEmit(chapterObjectList)
					_childChapterCountMap.tryEmit(chapterObjectList.groupingBy { it.parentId }.eachCount())
				}
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			_repository.collectLatest { repository1 ->
				repository1?.getAllNoteLiteAsFlow2()?.collectLatest { noteObjectLiteList ->
					_childChapterCountMap.tryEmit(noteObjectLiteList.groupingBy { it.parentId }.eachCount())
				}
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			combine(_allChapterList, _currentChapterId) { allChapterList1, parentChapterId1 -> Pair(allChapterList1, parentChapterId1) }.collect { (allChapterList1, parentChapterId1) ->
				_childChapterList.tryEmit(allChapterList1.filter { it.parentId == parentChapterId1 })
				_currentChapterPath.tryEmit(_repository.value?.getChapterPath(id = parentChapterId1, includeEdge = true) ?: listOf())
			}
		}
	}

	fun exploreChapter(chapterId: RealmUUID?) {
		this._currentChapterId.tryEmit(chapterId)
	}

	fun putNotebook(
		title: String,
		description: String,
		color: Color?,
		bitmap: Bitmap?,
	) {
		val chapterObject = ChapterObject().apply {
			this.title = title
			this.description = description
			this.color = color?.toArgb()
			this.thumbnail = bitmap?.encodeBase64()

			this.parentId = this@WhereChapterDialogViewModel2._currentChapterId.value
		}
		viewModelScope.launch(Dispatchers.Default) {
			_repository.value?.getObjectWithParentId<ChapterObject>(parentId = this@WhereChapterDialogViewModel2._currentChapterId.value, includeLocked = true)?.let {
				if (it.size < 4) _repository.value?.putChapterSuspended(chapterObject)
			}
		}
	}
}


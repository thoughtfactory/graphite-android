package com.syncodec.graphite.presentation.main.composable.screen.notebookScreen

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.local.ChapterObject
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.utils.encodeBase64
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class NotebookScreenViewModel(
	repositoryStateFlow: MutableStateFlow<Repository.Companion.RepositoryStatus>,
) : ViewModel() {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _defaultChapterId: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)

	private val _notebookList: MutableStateFlow<List<ChapterObject>?> = MutableStateFlow(null)
	val notebookList: StateFlow<List<ChapterObject>?> = _notebookList

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryStateFlow.collectLatest { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) _repository.tryEmit(repositoryStatus.repository)
			}
		}
		viewModelScope.launch(Dispatchers.Default) {
			_repository.collectLatest { repository1 ->
				launch { observeDefaultChapter(repository = repository1) }
				launch { observeNotebook(repository = repository1) }
			}
		}
	}

	private suspend fun observeDefaultChapter(repository: Repository?) {
		repository?.getDefaultChapterIdAsFlow()?.collectLatest { defaultChapterId1 ->
			this@NotebookScreenViewModel._defaultChapterId.tryEmit(defaultChapterId1)
		}
	}

	private suspend fun observeNotebook(repository: Repository?) {
		repository?.getNotebookAsFlow()?.collectLatest {
			this@NotebookScreenViewModel._notebookList.tryEmit(it)
		}
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
		}
		viewModelScope.launch(Dispatchers.Default) {
			_repository.value?.getObjectWithParentId<ChapterObject>(parentId = null, includeLocked = true)?.let {
				if (it.size < 4) _repository.value?.putChapterSuspended(chapterObject)
			}
		}
	}

	fun onReorderNotebookList(idList: List<RealmUUID>) {
		viewModelScope.launch(Dispatchers.Default) { _repository.value?.reorderNotebookList(idList) }
	}

	fun onClickMultiFavourite(idList: Set<RealmUUID>, isAllFavourite: Boolean) {
		viewModelScope.launch(Dispatchers.Default) {
			_repository.value?.setMultiObjectFromIdSuspended<ChapterObject>(idList = idList) {
				this.isFavourite = !isAllFavourite
			}
		}
	}

	fun onClickMultiLock(idList: Set<RealmUUID>, isAllLocked: Boolean) {
		viewModelScope.launch(Dispatchers.Default) {
			_repository.value?.setMultiObjectFromIdSuspended<ChapterObject>(idList = idList.filter { it != _defaultChapterId.value }.toSet()) {
				this.isLocked = !isAllLocked
			}
		}
	}

	fun delete(idList: Set<RealmUUID>) {
		_repository.value?.deleteSuspended(idList.filter { it != _defaultChapterId.value })
	}
}

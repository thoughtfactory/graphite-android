package com.syncodec.graphite.presentation.tags

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.local.NoteObjectLite
import com.syncodec.graphite.di.model.local.TagObject
import com.syncodec.graphite.di.repository.LockableRepo
import com.syncodec.graphite.di.repository.Repository
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class TagsViewModel(lockableRepo: LockableRepo) : ViewModel() {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _tagList: MutableStateFlow<List<TagObject>> = MutableStateFlow(listOf())
	val tagList: StateFlow<List<TagObject>> = _tagList

	private val _noteList: MutableStateFlow<List<NoteObjectLite>> = MutableStateFlow(listOf())
	val noteList: StateFlow<List<NoteObjectLite>> = _noteList

	init {
		viewModelScope.launch(Dispatchers.Default) {
			lockableRepo.repositoryStatusFlow.collectLatest { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) _repository.tryEmit(repositoryStatus.repository)
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			_repository.collectLatest { repository1 ->
				launch {
					repository1?.getAllTagAsFlow()?.collectLatest { tagList1 ->
						this@TagsViewModel._tagList.tryEmit(tagList1)
					}
				}
				launch {
					repository1?.getAllNoteLiteAsFlow2()?.collectLatest { noteList1 ->
						this@TagsViewModel._noteList.tryEmit(noteList1)
					}
				}
			}
		}
	}

	fun putTag(tag: String, color: Color): Boolean {
		return if (tagList.value.find { it.tag == tag } != null) {
			false
		}
		else {
			viewModelScope.launch(Dispatchers.Default) {
				TagObject().apply {
					this.tag = tag
					this.color = color.toArgb()

					_repository.value?.putTag(this)
				}
			}
			true
		}
	}

	fun updateTag(id: RealmUUID, tag: String, color: Color) {
		viewModelScope.launch(Dispatchers.Default) {
			_repository.value?.setObjectFromIdSuspended<TagObject>(id = id) {
				this.tag = tag
				this.color = color.toArgb()
			}
		}
	}

	fun deleteTag(id: RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			_repository.value?.deleteSuspended(id = id)
		}
	}
}

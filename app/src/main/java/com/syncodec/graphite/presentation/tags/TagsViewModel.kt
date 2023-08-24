package com.syncodec.graphite.presentation.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.Repository
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class TagsViewModel(private val repository : Repository) : ViewModel() {

	val repositoryState = repository.repositoryState

	private val _tagList : MutableStateFlow<List<TagObject>> = MutableStateFlow(listOf())
	private val _recalculatedTagList : MutableStateFlow<List<TagObject>> = MutableStateFlow(listOf())
	val tagList : StateFlow<List<TagObject>> = _recalculatedTagList
	private val _noteIdList : MutableStateFlow<List<RealmUUID>> = MutableStateFlow(listOf())

	init {
		viewModelScope.launch(Dispatchers.Default) {
			when (repositoryState.value) {
				Repository.Companion.RepositoryState.Init -> null
				Repository.Companion.RepositoryState.Loading -> null
				Repository.Companion.RepositoryState.Locked -> null
				Repository.Companion.RepositoryState.Success -> {
					viewModelScope.launch(Dispatchers.Default) {
						if (repositoryState.value != Repository.Companion.RepositoryState.Success) this.cancel()
						repository.getAllTagAsFlow().collect { _tagList.tryEmit(it) }
					}
					viewModelScope.launch(Dispatchers.Default) {
						if (repositoryState.value != Repository.Companion.RepositoryState.Success) this.cancel()
						repository.getAllNoteAsFlow().collect { _noteIdList.tryEmit(it.map { it.id }) }
					}
				}

				Repository.Companion.RepositoryState.Error -> null
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			combine(
				_tagList,
				_noteIdList
			) { tagList, noteIdList ->
				tagList.map { tagObject ->
					tagObject.clone().apply {
						objectIdList = objectIdList.filter { it in noteIdList }.toRealmList()
					}
				}
			}.collect { _recalculatedTagList.tryEmit(it) }
		}
	}

	fun putTag(tagObject : TagObject, callback : suspend (String) -> Unit) {
		viewModelScope.launch(Dispatchers.Default) {
			try {
				tagList.value.find { it.tag == tagObject.tag && it.id != tagObject.id }?.let {
					callback("Tag already exists")
					return@launch
				}

				if (repository.getAllTag().size < 8) repository.putTag(tagObject)
				else callback("Join Graphite Pro to add more tags")
			} catch (_ : Exception) {
			}
		}
	}

	fun deleteTag(tagObject : TagObject) {
		repository.deleteSuspended(tagObject.id)
	}
}

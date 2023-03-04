package com.syncodec.graphite.presentation.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.RealmNotInitializedException
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.di.repository.koinRepository.KoinRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class TagsViewModel(private val repository : KoinRepository) : ViewModel() {

	val repositoryState = repository.repositoryState

	val tagList : MutableStateFlow<List<TagObject>> = MutableStateFlow(listOf())

	init {
		viewModelScope.launch(Dispatchers.Default) {
			when (repositoryState.value) {
				RepositoryState.INIT -> null
				RepositoryState.LOADING -> null
				RepositoryState.LOCKED -> null
				RepositoryState.SUCCESS -> {
					viewModelScope.launch(Dispatchers.Default) {
						if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
						repository.getAllTagAsFlow().collect { tagList.tryEmit(it) }
					}
				}

				RepositoryState.ERROR -> null
			}
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
			} catch (e : RealmNotInitializedException) {
			} catch (e : Exception) {
			}
		}
	}

	fun deleteTag(tagObject : TagObject) {
		repository.deleteSuspended(tagObject.id)
	}
}

package com.syncodec.graphite.presentation.tags

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.KoinRepository
import com.syncodec.graphite.di.repository.RealmNotInitializedException
import com.syncodec.graphite.di.repository.RepositoryState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class TagsViewModel(private val repository : KoinRepository) : ViewModel() {

	val repositoryState = repository.repositoryState

	val tagList : SnapshotStateList<TagObject> = mutableStateListOf()

	init {
		viewModelScope.launch(Dispatchers.IO) {
			when (repositoryState.value) {
				RepositoryState.INIT -> null
				RepositoryState.LOADING -> null
				RepositoryState.LOCKED -> null
				RepositoryState.SUCCESS -> {
					viewModelScope.launch(Dispatchers.IO) {
						if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
						repository.getAllTagAsFlow().collect {
							withContext(Dispatchers.Main) {
								tagList.clear()
								tagList.addAll(it)
							}
						}
					}
				}

				RepositoryState.ERROR -> null
			}
		}
	}

	fun putTag(tagObject : TagObject) {
		viewModelScope.launch(Dispatchers.IO) {
			try {
				repository.putTag(tagObject) { _, _ -> }
			} catch (e : RealmNotInitializedException) {
			} catch (e : Exception) {
			}
		}
	}

	fun deleteTag(tagObject : TagObject?) {
		viewModelScope.launch(Dispatchers.IO) {
			try {
				repository.deleteTag(tagObject?.id)
			} catch (e : RealmNotInitializedException) {
			} catch (e : Exception) {
			}
		}
	}
}

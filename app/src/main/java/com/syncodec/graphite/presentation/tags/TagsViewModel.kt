package com.syncodec.graphite.presentation.tags

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.RealmNotInitializedException
import com.syncodec.graphite.di.repository.Repository2
import com.syncodec.graphite.di.repository.RepositoryState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class TagsViewModel @Inject constructor(private val repository2 : Repository2) : ViewModel() {

	val repositoryState = repository2.repositoryState

	val tagList:SnapshotStateList<TagObject> = mutableStateListOf()

	init {
		viewModelScope.launch(Dispatchers.IO) {
			when (repositoryState.value) {
				RepositoryState.INIT -> null
				RepositoryState.LOADING -> null
				RepositoryState.SUCCESS -> {
					viewModelScope.launch(Dispatchers.IO) {
						if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
						repository2.getAllTagAsFlow().collect {
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
				repository2.putTag(tagObject)
			} catch (e : RealmNotInitializedException) {
			} catch (e : Exception) {
			}
		}
	}

	fun deleteTag(tagObject : TagObject?) {
		viewModelScope.launch(Dispatchers.IO) {
			try {
				repository2.deleteTag(tagObject?.id)
			} catch (e : RealmNotInitializedException) {
			} catch (e : Exception) {
			}
		}
	}
}

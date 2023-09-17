package com.syncodec.graphite.presentation.bucket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.repository.Repository
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class BucketViewModel(repositoryStateFlow: MutableStateFlow<Repository.Companion.RepositoryStatus>) : ViewModel() {

	private val _repository : MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _id : MutableStateFlow<RealmUUID?> = MutableStateFlow(null)

	private val _bucketObject : MutableStateFlow<BucketObject?> = MutableStateFlow(null)
	val bucketObject : StateFlow<BucketObject?> = _bucketObject

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryStateFlow.collect { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) _repository.tryEmit(repositoryStatus.repository)
			}
		}
		viewModelScope.launch(Dispatchers.Default) {
			combine(_repository, _id) { repository1, id1 -> Pair(repository1, id1) }.collect { (repository1, id1) ->
				id1?.let {
					this.launch { repository1?.getBucketAsFlow(id = it)?.collect { this@BucketViewModel._bucketObject.tryEmit(it) } }
				}
			}
		}
	}

	fun initBucket(realmUUID: RealmUUID) {
		this@BucketViewModel._id.tryEmit(realmUUID)
	}

	fun toggleFavourite(id : RealmUUID?) {
		_repository.value?.setObjectFromIdSuspended<BucketObject>(id = id) {
			this.updateModifyTimestamp()
			this.isFavourite = this.isFavourite.not()
		}
	}

	fun toggleLock(id : RealmUUID?) {
		_repository.value?.setObjectFromIdSuspended<BucketObject>(id = id) {
			this.updateModifyTimestamp()
			this.isLocked = this.isLocked.not()
		}
	}

	fun updateTitleDescription(id : RealmUUID?, title: String?, description: String?) {
		_repository.value?.setObjectFromIdSuspended<BucketObject>(id = id) {
			this.updateModifyTimestamp()
			this.title = title
			this.description = description
		}
	}
}

package com.syncodec.graphite.presentation.main.composable.screen.bucketScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketObjectLite
import com.syncodec.graphite.di.model.BucketType
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
class BucketScreenViewModel(
	lockableRepo: LockableRepo,
) : ViewModel() {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _bucketList: MutableStateFlow<List<BucketObjectLite>?> = MutableStateFlow(listOf())
	val bucketList : StateFlow<List<BucketObjectLite>?> = _bucketList

	init {
		viewModelScope.launch(Dispatchers.Default) {
			lockableRepo.repositoryStatusFlow.collectLatest { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) _repository.tryEmit(repositoryStatus.repository)
			}
		}
		viewModelScope.launch(Dispatchers.Default) {
			_repository.collectLatest { repository1 ->
				launch { observeBucket(repository = repository1) }
			}
		}
	}

	private suspend fun observeBucket(repository: Repository?) {
		repository?.getAllBucketLiteAsFlow()?.collectLatest { bucketList1 ->
			this@BucketScreenViewModel._bucketList.tryEmit(bucketList1)
		}
	}

	fun putBucket(
		title: String?,
		description: String?,
		bucketType: BucketType,
		callback: suspend (String) -> Unit,
	) {
		val bucketObject = BucketObject().apply {
			this.title = title
			this.description = description
			this.bucketType = bucketType.name
		}

		if (BaseApplication.isPro.value || bucketType == BucketType.TODO) _repository.value?.putBucketSuspended(bucketObject)
		else viewModelScope.launch(Dispatchers.Default) {
			_repository.value?.getAllBucket()?.let {
				if (it.count { it.bucketType == bucketType.name } < 1) _repository.value?.putBucketSuspended(bucketObject) else callback("Join Graphite Pro to create more ${bucketType.name} buckets")
			}
		}
	}

	fun onReorderBucketList(idList : List<RealmUUID>) {
		viewModelScope.launch(Dispatchers.Default) { _repository.value?.reorderBucketList(idOrderList = idList) }
	}

	fun onClickMultiFavourite(idList: Set<RealmUUID>, isAllFavourite: Boolean) {
		_repository.value?.setMultiObjectFromIdSuspended<BucketObject>(idList = idList) {
			this.isFavourite = !isAllFavourite
		}
	}

	fun onClickMultiLock(idList: Set<RealmUUID>, isAllLocked: Boolean) {
		_repository.value?.setMultiObjectFromIdSuspended<BucketObject>(idList = idList) {
			this.isFavourite = !isAllLocked
		}
	}

	fun delete(idList: Set<RealmUUID>) {
		_repository.value?.deleteSuspended(idList)
	}
}

package com.syncodec.graphite.presentation.bucket

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.repository.koinRepository.KoinRepository
import com.syncodec.graphite.di.repository.RepositoryState
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class BucketViewModel(private val repository : KoinRepository) : ViewModel() {

	val repositoryState = repository.repositoryState

	val bucketObject : MutableState<BucketObject?> = mutableStateOf(null)

	val id : MutableState<RealmUUID?> = mutableStateOf(null)
	val title : MutableState<String?> = mutableStateOf(null)
	val description : MutableState<String?> = mutableStateOf(null)
	val bucketType : MutableState<String?> = mutableStateOf(null)
	val isFavourite : MutableState<Boolean?> = mutableStateOf(null)
	val isLocked : MutableState<Boolean?> = mutableStateOf(null)

	val bucketItemObject : MutableState<BucketItemObject?> = mutableStateOf(null)

	private var refreshCoroutine : CoroutineScope? = null

	fun loadAndViewData(realmUUID : RealmUUID) {
		viewModelScope.launch(Dispatchers.IO) {
			this@BucketViewModel.refreshCoroutine?.cancel()
			this@BucketViewModel.refreshCoroutine = this
			repositoryState.collect {
				when (it) {
					RepositoryState.INIT -> null
					RepositoryState.LOCKED -> null
					RepositoryState.LOADING -> null
					RepositoryState.SUCCESS -> {
						if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
						onRepositorySuccess(realmUUID = realmUUID)
					}

					RepositoryState.ERROR -> null
				}
			}
		}
	}

	private suspend fun onRepositorySuccess(realmUUID : RealmUUID) {
		repository.getBucketAsFlow(realmUUID).collect {
			bucketObject.value = it

			this@BucketViewModel.id.value = it?.id
			this@BucketViewModel.title.value = it?.title
			this@BucketViewModel.description.value = it?.description
			this@BucketViewModel.bucketType.value = it?.bucketType
			this@BucketViewModel.isFavourite.value = it?.isFavourite
			this@BucketViewModel.isLocked.value = it?.isLocked
		}
	}

	private fun putBucket() {
		CoroutineScope(Dispatchers.IO).launch {
			BucketObject().apply {
				if (this@BucketViewModel.id.value != null) this.id = this@BucketViewModel.id.value !!
				this.title = this@BucketViewModel.title.value
				this.description = this@BucketViewModel.description.value
				this.bucketType = this@BucketViewModel.bucketType.value ?: BucketType.UNKNOWN.name
				this.isFavourite = this@BucketViewModel.isFavourite.value ?: false
				this.isLocked = this@BucketViewModel.isLocked.value ?: false

				repository.putBucket(this) { _, _ ->
					loadAndViewData(this.id)
				}
			}
		}
	}


	fun toggleLock() {
		this.isLocked.value = this.isLocked.value?.not()
		putBucket()
	}

	fun toggleFavourite() {
		this.isFavourite.value = this.isFavourite.value?.not()
		putBucket()
	}

	fun updateBucket(title : String?, description : String?) {
		this.title.value = title
		this.description.value = description
		putBucket()
	}

	fun deleteBucketItem(realmUUIDList : List<RealmUUID>) {
		CoroutineScope(Dispatchers.Default).launch {
			repository.delete(realmUUIDList)
		}
	}
}

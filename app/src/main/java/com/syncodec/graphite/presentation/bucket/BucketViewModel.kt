package com.syncodec.graphite.presentation.bucket

import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kedia.ogparser.OpenGraphCallback
import com.kedia.ogparser.OpenGraphParser
import com.kedia.ogparser.OpenGraphResult
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.Network
import com.syncodec.graphite.di.repository.Repository2
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.utils.encodeBase64
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class BucketViewModel @Inject constructor(private val repository2 : Repository2) : ViewModel() {

	val repositoryState = repository2.repositoryState

	val bucketObject : MutableState<BucketObject?> = mutableStateOf(null)

	val id : MutableState<RealmUUID?> = mutableStateOf(null)
	val title : MutableState<String?> = mutableStateOf(null)
	val description : MutableState<String?> = mutableStateOf(null)
	val bucketType : MutableState<String?> = mutableStateOf(null)
	val isFavourite : MutableState<Boolean?> = mutableStateOf(null)
	val isLocked : MutableState<Boolean?> = mutableStateOf(null)

	val isSelected : MutableState<Boolean> = mutableStateOf(false)
	val selectedRealmUUIDList : SnapshotStateList<RealmUUID> = mutableStateListOf()

	val bucketItemRealmUUID : MutableState<RealmUUID?> = mutableStateOf(null)
	val bucketItemObject : MutableState<BucketItemObject?> = mutableStateOf(null)

	private var refreshCoroutine : CoroutineScope? = null
	private var bucketItemObjectCoroutine : CoroutineScope? = null


	fun refresh() {
		bucketObject.value?.id?.let {
			loadAndViewData(id = it)
		}
	}

	fun loadAndViewData(id : RealmUUID) {
		viewModelScope.launch(Dispatchers.IO) {
			this@BucketViewModel.refreshCoroutine?.cancel()
			this@BucketViewModel.refreshCoroutine = this
			repositoryState.collect {
				when (it) {
					RepositoryState.INIT -> Log.d("BucketViewModel", "Init")
					RepositoryState.LOCKED -> null
					RepositoryState.LOADING -> Log.d("BucketViewModel", "Loading")
					RepositoryState.SUCCESS -> {
						if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
						repository2.getBucketAsFlow(id).collect {
							bucketObject.value = it

							this@BucketViewModel.id.value = it?.id
							this@BucketViewModel.title.value = it?.title
							this@BucketViewModel.description.value = it?.description
							this@BucketViewModel.bucketType.value = it?.bucketType
							this@BucketViewModel.isFavourite.value = it?.isFavourite
							this@BucketViewModel.isLocked.value = it?.isLocked
						}
					}

					RepositoryState.ERROR -> Log.d("BucketViewModel", "Error")
				}
			}
		}
	}

	fun putLink(url : String) {
		CoroutineScope(Dispatchers.IO).launch {
			val isUrlValid = URLUtil.isValidUrl(url)
			if (isUrlValid) {
				val bucketItemObject = BucketItemObject().apply {
					this.bucketType = BucketType.LINK.name
					this.parentId = this@BucketViewModel.bucketObject.value?.id
					this.key = url
					this@BucketViewModel.id.value?.let {
						repository2.putBucketItem(it, this) { _, _ -> }
					}
				}

				val openGraphParser = OpenGraphParser(
					listener = object : OpenGraphCallback {
						override fun onError(error : String) {

						}

						override fun onPostResponse(openGraphResult : OpenGraphResult) {
							CoroutineScope(Dispatchers.IO).launch {
								Network.retrieveImage(openGraphResult.image) { bitmap ->
									bucketItemObject.title = openGraphResult.title
									bucketItemObject.thumbnail = bitmap?.encodeBase64()
									bucketItemObject.putOpenGraphResult(openGraphResult)

									CoroutineScope(Dispatchers.Default).launch {
										this@BucketViewModel.id.value?.let {
											repository2.putBucketItem(it, bucketItemObject) { _, _ ->
												refresh()
											}
										}
									}
								}
							}
						}
					}
				)

				openGraphParser.parse(url)
			} else {
				withContext(Dispatchers.Main) { Toast.makeText(repository2.context, "Invalid URL", Toast.LENGTH_SHORT).show() }
			}
		}
	}

	fun putTodo(
		realmUUID : RealmUUID?,
		todo : String,
		state : BucketItemState
	) {
		CoroutineScope(Dispatchers.Default).launch {
			BucketItemObject().apply {
				if (realmUUID != null) this.id = realmUUID
				this.bucketType = BucketType.TODO.name
				this.title = todo
				this.state = state.name
				this.parentId = this@BucketViewModel.bucketObject.value?.id
				this.key = todo

				this@BucketViewModel.id.value?.let {
					repository2.putBucketItem(it, this) { _, _ ->
						refresh()
					}
				}
			}
		}
	}

	fun putBucket() {
		CoroutineScope(Dispatchers.IO).launch {
			BucketObject().apply {
				if (this@BucketViewModel.id.value != null) this.id = this@BucketViewModel.id.value !!
				this.modifiedTimestamp = System.currentTimeMillis()
				this.title = this@BucketViewModel.title.value
				this.description = this@BucketViewModel.description.value
				this.bucketType = this@BucketViewModel.bucketType.value ?: BucketType.UNKNOWN.name
				this.isFavourite = this@BucketViewModel.isFavourite.value ?: false
				this.isLocked = this@BucketViewModel.isLocked.value ?: false

				repository2.putBucket(this) { _, _ ->
					loadAndViewData(this.id)
				}
			}
		}
	}

	fun getBucketItem(id : RealmUUID?) {
		viewModelScope.launch(Dispatchers.IO) {
			if (id != bucketItemObject.value?.id) bucketItemObjectCoroutine?.cancel()
			bucketItemObjectCoroutine = this
			if (id != null) {
				repository2.getBucketItemAsFlow(id).collect {
					this@BucketViewModel.bucketItemObject.value = it
					this@BucketViewModel.bucketItemRealmUUID.value = it?.id
				}
			} else {
				this@BucketViewModel.bucketItemObject.value = null
				this@BucketViewModel.bucketItemRealmUUID.value = null
			}
		}
	}

	fun deleteBucketItem() {
		try {
			val toDeleteRealmUUIDList = selectedRealmUUIDList.toList()
			repository2.delete(toDeleteRealmUUIDList)
			selectedRealmUUIDList.clear()
			isSelected.value = false
		} catch (e : Exception) {

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

	fun toggleBucketItemLock(bucketItemObject : BucketItemObject) {
		CoroutineScope(Dispatchers.IO).launch {
			bucketItemObject.clone().apply {
				this.isLocked = this.isLocked.not()
				this@BucketViewModel.id.value?.let {
					repository2.putBucketItem(it, this) { _, e ->
						refresh()
					}
				}
			}
		}
	}

	fun toggleBucketItemFavourite(bucketItemObject : BucketItemObject) {
		CoroutineScope(Dispatchers.IO).launch {
			bucketItemObject.clone().apply {
				this.isFavourite = this.isFavourite.not()
				this@BucketViewModel.id.value?.let {
					repository2.putBucketItem(it, this) { _, e ->
						refresh()
					}
				}
			}
		}
	}

	fun deleteBucketItem(bucketItemObject : BucketItemObject) {
		CoroutineScope(Dispatchers.IO).launch {
			repository2.delete(listOf(bucketItemObject.id))
		}
	}

	fun updateBucket(title : String?, description : String?) {
		this.title.value = title
		this.description.value = description
		putBucket()
	}
}

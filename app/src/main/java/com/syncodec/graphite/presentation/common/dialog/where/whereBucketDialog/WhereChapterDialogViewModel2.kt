package com.syncodec.graphite.presentation.common.dialog.where.whereBucketDialog

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketObjectLite
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.utils.encodeBase64
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class WhereBucketDialogViewModel2(
	repositoryStateFlow: MutableStateFlow<Repository.Companion.RepositoryStatus>,
	private val isAuthenticated: StateFlow<Boolean>,
) : ViewModel() {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _currentBucketId: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)

	private val _bucketList: MutableStateFlow<List<BucketObjectLite>> = MutableStateFlow(listOf())
	val bucketList: StateFlow<List<BucketObjectLite>> = _bucketList

	private val _bucketItemList : MutableStateFlow<List<BucketItemObject>?> = MutableStateFlow(null)
	val bucketItemList : StateFlow<List<BucketItemObject>?> = _bucketItemList

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryStateFlow.collect { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) this@WhereBucketDialogViewModel2._repository.tryEmit(repositoryStatus.repository)
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			_repository.collectLatest { repository1 ->
				repository1?.let {
					launch { observeAllBucket(repository = it) }
				}
			}
		}
	}

	private suspend fun observeAllBucket(repository: Repository) {
		combine(repository.getAllBucketAsFlow(), repository.getAllBucketSizeAsFlow()) { bucketList1, bucketSize1 ->
			bucketList1.map { it.toLite().copy(bucketItemCount = bucketSize1[it.id] ?: 0) }
		}.collectLatest {bucketList1 ->
			this@WhereBucketDialogViewModel2._bucketList.tryEmit(bucketList1)
		}
	}

	fun putNotebook(
		title: String,
		description: String,
		color: Color?,
		bitmap: Bitmap?,
	) {
	}
}


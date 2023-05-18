package com.syncodec.graphite.presentation.main

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.repository.repository.Repository
import com.syncodec.graphite.di.cloud.dropbox.DBox
import com.syncodec.graphite.di.cloud.googleDrive.GDrive
import com.syncodec.graphite.utils.dataStore.SyncDataStoreInstance
import com.syncodec.graphite.utils.encodeBase64
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class MainViewModel(private val repository: Repository, private val dBox: DBox, private val gDrive: GDrive) : ViewModel() {

	val repositoryState = repository.repositoryState

	val defaultChapterId = MutableStateFlow(null as RealmUUID?)

	private val _testConnectionResponse = MutableStateFlow<DBox.Companion.TestConnectionResponse?>(DBox.Companion.TestConnectionResponse.Error(Exception("Test Connection Error"), ""))
	val testConnectionResponse: StateFlow<DBox.Companion.TestConnectionResponse?> = _testConnectionResponse

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryState.collect { repositoryState1 ->
				when (repositoryState1) {
					Repository.Companion.RepositoryState.Success -> {
						repository.getDefaultChapterIdAsFlow().collect {
//							TODO : Remove null check after after realm update #1289
							it?.let { it1 -> defaultChapterId.tryEmit(it1) }
						}
					}

					else -> null
				}
			}
		}
	}

	fun putNotebook(
		title: String,
		description: String,
		color: Color?,
		bitmap: Bitmap?,
		callback: suspend (String) -> Unit,
	) {
		val chapterObject = ChapterObject().apply {
			this.title = title
			this.description = description
			this.color = color?.toArgb()
			this.thumbnail = bitmap?.encodeBase64()
		}

		if (BaseApplication.isPro.value) repository.putChapterSuspended(chapterObject)
		else viewModelScope.launch(Dispatchers.Default) {
			repository.getChapterWithParentId(parentChapterId = null).let {
				if (it.size < 4) repository.putChapterSuspended(chapterObject) else callback("Join Graphite Pro to create more notebooks")
			}
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

		if (BaseApplication.isPro.value || bucketType == BucketType.TODO) repository.putBucketSuspended(bucketObject)
		else viewModelScope.launch(Dispatchers.Default) {
			repository.getAllBucket().let {
				if (it.count { it.bucketType == bucketType.name } < 1) repository.putBucketSuspended(bucketObject) else callback("Join Graphite Pro to create more ${bucketType.name} buckets")
			}
		}
	}

	fun delete(idList: List<RealmUUID>) {
		repository.deleteSuspended(idList)
	}

	fun setRepositoryState(repositoryState: Repository.Companion.RepositoryState) {
		repository.repositoryState.tryEmit(repositoryState)
	}

	fun onAuthenticate(context: Context) {
		repository.initRepository(context)
		repository.isAuthenticated.tryEmit(true)
	}

	fun onAuthFailure() {
		repository.isAuthenticated.tryEmit(false)
	}

	fun onDeauthenticate() {
		repository.isAuthenticated.tryEmit(false)
	}

	fun testRemoteConnection(syncProvider: SyncDataStoreInstance.Companion.SyncProvider?) {
		Log.d("npr71", "testRemoteConnection: $syncProvider")
		viewModelScope.launch(Dispatchers.IO) {
			when (syncProvider) {
				SyncDataStoreInstance.Companion.SyncProvider.Dropbox -> dBox.testConnection { _testConnectionResponse.tryEmit(it) }
				SyncDataStoreInstance.Companion.SyncProvider.GoogleDrive -> gDrive.testConnection { _testConnectionResponse.tryEmit(it) }

				SyncDataStoreInstance.Companion.SyncProvider.NotConfigured -> _testConnectionResponse.tryEmit(DBox.Companion.TestConnectionResponse.NotLoggedIn)
				SyncDataStoreInstance.Companion.SyncProvider.Unknown -> _testConnectionResponse.tryEmit(DBox.Companion.TestConnectionResponse.NotLoggedIn)
				else -> null
			}
		}
	}
}

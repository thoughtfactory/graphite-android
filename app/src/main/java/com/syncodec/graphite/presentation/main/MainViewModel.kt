package com.syncodec.graphite.presentation.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.cloud.dropbox.DBox
import com.syncodec.graphite.di.repository.LockableRepo
import com.syncodec.graphite.utils.dataStore.SyncDataStoreInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class MainViewModel(
	val lockableRepo: LockableRepo,
	private val dBox: DBox,
) : ViewModel() {

	private val _testConnectionResponse = MutableStateFlow<DBox.Companion.TestConnectionResponse?>(DBox.Companion.TestConnectionResponse.Error(Exception("Test Connection Error"), ""))
	val testConnectionResponse: StateFlow<DBox.Companion.TestConnectionResponse?> = _testConnectionResponse

//	fun testRemoteConnection(syncProvider: SyncDataStoreInstance.Companion.SyncProvider?) {
//		Log.d("npr71", "testRemoteConnection: $syncProvider")
//		viewModelScope.launch(Dispatchers.IO) {
//			when (syncProvider) {
//				SyncDataStoreInstance.Companion.SyncProvider.Dropbox -> dBox.testConnection { _testConnectionResponse.tryEmit(it) }
//				SyncDataStoreInstance.Companion.SyncProvider.GoogleDrive -> gDrive.testConnection { _testConnectionResponse.tryEmit(it) }
//
//				SyncDataStoreInstance.Companion.SyncProvider.NotConfigured -> _testConnectionResponse.tryEmit(DBox.Companion.TestConnectionResponse.NotLoggedIn)
//				SyncDataStoreInstance.Companion.SyncProvider.Unknown -> _testConnectionResponse.tryEmit(DBox.Companion.TestConnectionResponse.NotLoggedIn)
//				else -> null
//			}
//		}
//	}

	fun onAuthenticate() {
		lockableRepo.decryptRepository()
	}
}

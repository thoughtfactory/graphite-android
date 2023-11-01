package com.syncodec.graphite.presentation.sync.dropbox2

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.cloud.dropbox.DropboxApi
import com.syncodec.graphite.di.cloud.dropbox.DropboxConnector
import com.syncodec.graphite.di.network.NetworkRequest
import com.syncodec.graphite.di.repository.LockableRepo
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.di.snapshot.SnapshotInator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class DropboxViewModel(private val lockableRepo: LockableRepo, private val dropboxConnector: DropboxConnector) : ViewModel() {

	private var _repository: Repository? = null
	private var dropboxApi: DropboxApi? = null

	private val _dropBoxConnection: MutableStateFlow<DropboxConnector.Companion.DropBoxConnection> = MutableStateFlow(DropboxConnector.Companion.DropBoxConnection.Init)
	val dropBoxConnection: StateFlow<DropboxConnector.Companion.DropBoxConnection> = _dropBoxConnection

	private val _dropboxAccountInfo: MutableStateFlow<NetworkRequest<DropboxApi.Companion.DropboxAccountInfo>> = MutableStateFlow(NetworkRequest.Init)
	val dropboxAccountInfo: StateFlow<NetworkRequest<DropboxApi.Companion.DropboxAccountInfo>> = _dropboxAccountInfo

	private val _remoteSnapshotList: MutableStateFlow<List<DropboxApi.Companion.RemoteSnapshot>> = MutableStateFlow(listOf())
	val remoteSnapshotList: StateFlow<List<DropboxApi.Companion.RemoteSnapshot>> = _remoteSnapshotList

	init {
		viewModelScope.launch(Dispatchers.Default) {
			lockableRepo.repositoryStatusFlow.collectLatest { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) this@DropboxViewModel._repository = repositoryStatus.repository
			}
		}
	}

	fun exchangeCode(code: String) {
		Log.d("npr71", "DropboxViewModel.exchangeCode: $code")
		viewModelScope.launch(Dispatchers.IO) {
			_dropBoxConnection.tryEmit(DropboxConnector.Companion.DropBoxConnection.Connecting)
			_dropboxAccountInfo.tryEmit(NetworkRequest.Loading)

			when (val exchangeCodeForTokenResponse = dropboxConnector.exchangeCodeForToken(code = code)) {
				is DropboxConnector.Companion.ExchangeCodeForTokenResponse.Success -> {
					dropboxApi = DropboxApi(context = lockableRepo.context, accessToken = exchangeCodeForTokenResponse.accessToken, expireAt = System.currentTimeMillis() + exchangeCodeForTokenResponse.expireAt * 1000)

					_dropBoxConnection.tryEmit(DropboxConnector.Companion.DropBoxConnection.Connected)

					_remoteSnapshotList.tryEmit(dropboxApi?.scanSnapshot() ?: listOf())
					dropboxApi?.getAccountInfo()?.let { _dropboxAccountInfo.tryEmit(it) }
				}

				is DropboxConnector.Companion.ExchangeCodeForTokenResponse.Error -> {
					_dropBoxConnection.tryEmit(DropboxConnector.Companion.DropBoxConnection.NotConnected)
					_dropboxAccountInfo.tryEmit(NetworkRequest.Init)
				}
			}
		}
	}

	fun refreshConnection() {
		Log.d("npr71", "DropboxViewModel.refreshConnection")
		viewModelScope.launch(Dispatchers.IO) {
			_dropBoxConnection.tryEmit(DropboxConnector.Companion.DropBoxConnection.Connecting)
			_dropboxAccountInfo.tryEmit(NetworkRequest.Loading)

			dropboxApi = dropboxConnector.refreshConnection()
			if (dropboxApi == null) _dropBoxConnection.tryEmit(DropboxConnector.Companion.DropBoxConnection.NotConnected)
			else _dropBoxConnection.tryEmit(DropboxConnector.Companion.DropBoxConnection.Connected)

			_remoteSnapshotList.tryEmit(dropboxApi?.scanSnapshot() ?: listOf())
			dropboxApi?.getAccountInfo()?.let { _dropboxAccountInfo.tryEmit(it) } ?: _dropboxAccountInfo.tryEmit(NetworkRequest.Init)
		}
	}

	fun disconnect() {
		Log.d("npr71", "DropboxViewModel.disconnect")
		viewModelScope.launch(Dispatchers.IO) {
			dropboxApi?.disconnect()
			dropboxConnector.disconnect()
			refreshConnection()
		}
	}

	fun takeSnapshot() {
		viewModelScope.launch(Dispatchers.IO) {
			this@DropboxViewModel._repository?.let { repository ->
				val snapshotFile = repository.snapshotInator.generateSnapshotFile()
				dropboxApi?.uploadSnapshot(snapshotFile)

				dropboxApi?.getAccountInfo()?.let { _dropboxAccountInfo.tryEmit(it) }
				_remoteSnapshotList.tryEmit(dropboxApi?.scanSnapshot() ?: listOf())
			}
		}
	}

	suspend fun downloadSnapshot(fileId: String): Pair<ByteArray, SnapshotInator.Companion.SnapshotMetadata?>? {
		val snapshotByteArray = dropboxApi?.downloadSnapshot(fileId)
		return if (snapshotByteArray == null) null
		else Pair(snapshotByteArray, _repository?.snapshotInator?.getByteArrayInfo(snapshotByteArray))
	}

	suspend fun restoreSnapshot(snapshotByteArray: ByteArray, is7z: Boolean): Boolean {
		return _repository?.snapshotInator?.restoreSnapshot(snapshotByteArray = snapshotByteArray, is7z = is7z) ?: false
	}
}
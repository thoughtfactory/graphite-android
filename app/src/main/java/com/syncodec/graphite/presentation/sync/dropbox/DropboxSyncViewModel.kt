package com.syncodec.graphite.presentation.sync.dropbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dropbox.core.v2.files.Metadata
import com.syncodec.graphite.di.repository.repository.Repository
import com.syncodec.graphite.di.sync.dropbox.DBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.io.InputStream


@KoinViewModel
class DropboxSyncViewModel(private val repository : Repository, private val dBox : DBox) : ViewModel() {

	val _snapshotList : MutableStateFlow<List<Metadata>?> = MutableStateFlow(null)
	val snapshotList : StateFlow<List<Metadata>?> = _snapshotList

	init {
		refreshSnapshot()
	}

	fun exchangeCodeForToken(code : String, callback : (DBox.Companion.ExchangeCodeForTokenResponse) -> Unit) {
		callback(DBox.Companion.ExchangeCodeForTokenResponse.Loading)
		viewModelScope.launch(Dispatchers.IO) { dBox.exchangeCodeForToken(code, callback) }
	}

	fun testConnection(callback : (DBox.Companion.TestConnectionResponse) -> Unit) {
		callback(DBox.Companion.TestConnectionResponse.Loading)
		viewModelScope.launch(Dispatchers.IO) { dBox.testConnection(callback) }
	}

	fun disconnect(callback : () -> Unit) {
		viewModelScope.launch(Dispatchers.IO) { dBox.disconnect(callback) }
	}

	fun refreshSnapshot() {
		_snapshotList.tryEmit(listOf())
		viewModelScope.launch(Dispatchers.IO) {
			dBox.getSnapshot {
				when(it) {
					is DBox.Companion.GetSnapshotResponse.Success -> {
						_snapshotList.tryEmit(it.snapshotList)
					}
					is DBox.Companion.GetSnapshotResponse.SuccessContinue -> {
						_snapshotList.value?.toMutableList()?.apply {
							addAll(it.snapshotList)
						}.let { list ->
							_snapshotList.tryEmit(list)
						}
					}
					is DBox.Companion.GetSnapshotResponse.SuccessEmpty -> {
						_snapshotList.tryEmit(null)
					}
					else -> null
				}
			}
		}
	}

	fun generateSnapshot(
		callback : (Boolean) -> Unit = {}
	) {
		callback(true)
		dBox.snapshot.observeBackupFolder {
			refreshSnapshot()
			callback(false)
		}
		viewModelScope.launch(Dispatchers.IO) {
			repository.snapshot.generate { dBox.snapshot.uploadSnapshot(it) }
		}
	}

	fun downloadSnapshot(
		metadata : Metadata,
		callback : (DBox.Companion.DownloadSnapshotResponse) -> Unit
	) {
		viewModelScope.launch(Dispatchers.IO) {
			dBox.snapshot.downloadSnapshot(metadata, callback)
		}
	}

	fun restore(inputStream : InputStream, callback : (Boolean) -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			repository.snapshot.restore(inputStream = inputStream, callback = callback)
		}
	}
}

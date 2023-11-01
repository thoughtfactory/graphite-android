package com.syncodec.graphite.presentation.sync.googleDrive

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.syncodec.graphite.di.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.io.InputStream

@KoinViewModel
class GoogleDriveSyncViewModel(private val repository: Repository) : ViewModel() {

//	val _snapshotList: MutableStateFlow<GDriveSyncInatorService.Companion.ListFiles?> = MutableStateFlow(null)
//	val snapshotList: MutableStateFlow<GDriveSyncInatorService.Companion.ListFiles?> = _snapshotList
//
//	fun refreshSnapshot(drive: Drive) {
//		_snapshotList.tryEmit(null)
//		_snapshotList.tryEmit(gDrive.refreshSnapshot(drive = drive))
//	}
//
//	fun generateSnapshot(drive: Drive, callback: (Boolean) -> Unit) {
//		callback(true)
////		repository.snapshot.generate {
////			gDrive.snapshot.uploadSnapshot(drive = drive, byteArray = it.readBytes()).let {
////				Log.d("npr71", "generateSnapshot: $it")
////			}
////			refreshSnapshot(drive = drive)
////			callback(false)
////		}
//	}
//
//	fun downloadSnapshot(
//		drive: Drive,
//		file: File,
//		callback : (GDriveSyncInatorService.Companion.DownloadResult) -> Unit
//	) {
//		viewModelScope.launch(Dispatchers.IO) {
//			callback(gDrive.downloadData(drive = drive, fileId = file.id))
//		}
//	}
//
//	fun restore(inputStream : InputStream, callback : (Boolean) -> Unit) {
//		viewModelScope.launch(Dispatchers.IO) {
////			repository.snapshot.restore(inputStream = inputStream, callback = callback)
//		}
//	}
//
//	fun deleteSnapshot(drive: Drive, file: File, callback: (GDriveSyncInatorService.Companion.DeleteResult) -> Unit) {
//		viewModelScope.launch(Dispatchers.IO) {
//			callback(gDrive.deleteFile(drive = drive, fileId = file.id))
//		}
//	}
}

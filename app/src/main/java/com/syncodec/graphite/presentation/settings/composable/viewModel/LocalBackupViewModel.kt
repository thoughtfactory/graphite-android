package com.syncodec.graphite.presentation.settings.composable.viewModel

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.repository.LockableRepo
import com.syncodec.graphite.di.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.internal.closeQuietly
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel
import org.koin.android.annotation.KoinViewModel
import java.time.Instant


@KoinViewModel
class LocalBackupViewModel(private val lockableRepo: LockableRepo) : ViewModel() {

	private val json = Json

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	init {
		viewModelScope.launch(Dispatchers.Default) {
			lockableRepo.repositoryStatusFlow.collectLatest { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) _repository.tryEmit(repositoryStatus.repository)
			}
		}
	}

	fun takeSnapshot(callback: () -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			_repository.value?.let { repository1 ->
				repository1.snapshot.generate { zipFile ->
					val uri = lockableRepo.context.contentResolver.persistedUriPermissions.firstOrNull()?.uri
					if (uri != null) {
						val snapshotFile = DocumentFile.fromTreeUri(lockableRepo.context, uri)?.createFile("application/zip", zipFile.name)
						if (snapshotFile != null) {
							val inputStream = zipFile.inputStream()
							val outputStream = lockableRepo.context.contentResolver.openOutputStream(snapshotFile.uri)
							outputStream?.let(inputStream::copyTo)
							inputStream.closeQuietly()
							outputStream?.closeQuietly()
						}
					}
					callback()
				}
			}
		}
	}

	@WorkerThread
	fun readBackupFolder(): Map<DocumentFile, SnapshotMetadata?> {
		return _repository.value?.let { repository1 ->
			val uri = lockableRepo.context.contentResolver.persistedUriPermissions.firstOrNull()?.uri
			if (uri != null) {
				val documentTree = DocumentFile.fromTreeUri(lockableRepo.context, uri)
				documentTree
					?.listFiles()
					?.filter { it.name?.endsWith(".zip") == true || it.name?.endsWith(".7z") == true }
					?.associateWith { it.getFileInfo(context = lockableRepo.context) }
			} else mapOf()
		} ?: mapOf()
	}

	private fun DocumentFile.getFileInfo(context: Context): SnapshotMetadata? {
		return when {
			name?.endsWith(".zip") == true -> getZipFileInfo(context = context)
			name?.endsWith(".7z") == true -> null
			else -> null
		}
	}

	@OptIn(ExperimentalSerializationApi::class)
	private fun DocumentFile.getZipFileInfo(context: Context): SnapshotMetadata? {
		try {
			return context.contentResolver.openInputStream(uri)?.use { inputStream ->
				val inMemoryByteChannel = SeekableInMemoryByteChannel(inputStream.readBytes())

				val zipFile = ZipFile(inMemoryByteChannel)
				zipFile.entries.toList().find { it.name == "metadata.json" }?.let { zipArchiveEntry ->
					val metadataInputStream = zipFile.getInputStream(zipArchiveEntry)
					val snapshotMetadata = json.decodeFromStream<SnapshotMetadata>(metadataInputStream)
					metadataInputStream.closeQuietly()
					snapshotMetadata
				}
			}
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			return null
		}
	}

	@WorkerThread
	fun restoreSnapshot(documentFile: DocumentFile, callback: (Boolean) -> Unit) {
		_repository.value?.let { repository1 ->
			lockableRepo.context.contentResolver.openInputStream(documentFile.uri)?.use {
				repository1.snapshot.restore(inputStream = it, is7z = documentFile.name?.endsWith(".7z") == true, callback = callback)
			}
		}
	}

	companion object {
		@Serializable
		data class SnapshotMetadata(
			val timestamp: Long = Instant.now().toEpochMilli(),
			val noteCount: Int = 0,
			val chapterCount: Int = 0,
			val bucketItemCount: Int = 0,
			val bucketCount: Int = 0,
			val tagCount: Int = 0,
			val attachmentCount: Int = 0,
		)
	}
}

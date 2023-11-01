package com.syncodec.graphite.presentation.settings.composable.viewModel

import androidx.annotation.WorkerThread
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.repository.LockableRepo
import com.syncodec.graphite.di.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class LocalBackupViewModel(private val lockableRepo: LockableRepo) : ViewModel() {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	init {
		viewModelScope.launch(Dispatchers.Default) {
			lockableRepo.repositoryStatusFlow.collectLatest { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) _repository.tryEmit(repositoryStatus.repository)
			}
		}
	}

	@WorkerThread
	fun takeSnapshot() {
		_repository.value?.let {repository ->
			val context = lockableRepo.context
			val zipFile = repository.snapshotInator.generateSnapshotFile()
			val uri = context.contentResolver.persistedUriPermissions.firstOrNull()?.uri
			if (uri != null) {
				val snapshotFile = DocumentFile.fromTreeUri(context, uri)?.createFile("application/zip", zipFile.name)
				if (snapshotFile != null) {
					zipFile.inputStream().use { inputStream ->
						context.contentResolver.openOutputStream(snapshotFile.uri)?.use { outputStream ->
							inputStream.copyTo(outputStream)
						}
					}
					repository.snapshotInator.clearSnapshotDir()
				}
			}
		}
	}

	@WorkerThread
	fun restoreSnapshot(snapshotFile: DocumentFile) = _repository.value?.snapshotInator?.restoreSnapshot(snapshotFile) ?: false

	@WorkerThread
	fun readBackupFolder() = _repository.value?.snapshotInator?.readBackupFolder() ?: mapOf()
}

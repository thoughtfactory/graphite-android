package com.syncodec.graphite.presentation.settings.composable.viewModel

import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.local.BucketItemObject
import com.syncodec.graphite.di.model.local.BucketObject
import com.syncodec.graphite.di.model.local.ChapterObject
import com.syncodec.graphite.di.model.local.NoteObject
import com.syncodec.graphite.di.model.dataExchanger.Exportable.ExportBaseObject
import com.syncodec.graphite.di.model.dataExchanger.Exportable.ExportBucketItemObject
import com.syncodec.graphite.di.model.dataExchanger.Exportable.ExportBucketObject
import com.syncodec.graphite.di.model.dataExchanger.Exportable.ExportChapterObject
import com.syncodec.graphite.di.model.dataExchanger.Exportable.ExportNoteObject
import com.syncodec.graphite.di.model.dataExchanger.Exportable.ExportTagObject
import com.syncodec.graphite.di.model.local.TagObject
import com.syncodec.graphite.di.repository.LockableRepo
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.utils.archiveUtil.CompressUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.annotation.KoinViewModel
import java.io.File
import java.time.Instant


@KoinViewModel
class ExportDataViewModel(private val lockableRepo: LockableRepo) : ViewModel() {

	private val json = Json

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	init {
		viewModelScope.launch(Dispatchers.Default) {
			lockableRepo.repositoryStatusFlow.collectLatest { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) _repository.tryEmit(repositoryStatus.repository)
			}
		}
	}

	@WorkerThread
	fun exportData(
		isNotesSelected: Boolean = false,
		isBucketsSelected: Boolean = false,
		isTagsSelected: Boolean = false,
		isAttachmentsSelected: Boolean = false,
		includeLockedItems: Boolean = false,
	): File? {
		_repository.value?.let { repository1 ->

			val timestamp = Instant.now().toEpochMilli()

			val exportDir = File(lockableRepo.context.cacheDir, "export_$timestamp")
			exportDir.mkdirs()

			if (isNotesSelected) {
				val parentDir = File(exportDir, "note")
				parentDir.mkdirs()
				repository1
					.getAllObjectOfType<NoteObject>(includeLocked = includeLockedItems)
					.map { ExportNoteObject.fromObject(inputObject = it) }
					.forEach { writeFile(parentDir = parentDir, fileName = it.id.toString(), data = json.encodeToString(it)) }
		}

			if (isNotesSelected) {
				val parentDir = File(exportDir, "chapter")
				parentDir.mkdirs()
				repository1
					.getAllObjectOfType<ChapterObject>(includeLocked = includeLockedItems)
					.map { ExportChapterObject.fromObject(inputObject = it) }
					.forEach { writeFile(parentDir = parentDir, fileName = it.id.toString(), data = json.encodeToString(it)) }
		}

			if (isBucketsSelected) {
				val parentDir = File(exportDir, "bucketItem")
				parentDir.mkdirs()
				repository1
					.getAllObjectOfType<BucketItemObject>(includeLocked = includeLockedItems)
					.map { ExportBucketItemObject.fromObject(inputObject = it) }
					.forEach { writeFile(parentDir = parentDir, fileName = it.id.toString(), data = json.encodeToString(it)) }
			}

			if (isBucketsSelected) {
				val parentDir = File(exportDir, "bucket")
				parentDir.mkdirs()
				repository1
					.getAllObjectOfType<BucketObject>(includeLocked = includeLockedItems)
					.map { ExportBucketObject.fromObject(inputObject = it) }
					.forEach { writeFile(parentDir = parentDir, fileName = it.id.toString(), data = json.encodeToString(it)) }
			}

			if (isTagsSelected) {
				val parentDir = File(exportDir, "tag")
				parentDir.mkdirs()
				repository1
					.getAllObjectOfType<TagObject>(includeLocked = true)
					.map { ExportTagObject.fromObject(inputObject = it) }
					.forEach { writeFile(parentDir = parentDir, fileName = it.id.toString(), data = json.encodeToString(it)) }
			}

			if (isAttachmentsSelected) {
				repository1
					.attachmentRepository
					.getAttachmentDir()
					.let {
						File(exportDir, "attachment").let { attachmentFile ->
							attachmentFile.mkdirs()
							it.copyRecursively(attachmentFile)
						}
					}
			}

			repository1
				.getBaseObject()
				?.let { ExportBaseObject.fromObject(inputObject = it) }
				?.let { writeFile(parentDir = exportDir, fileName = "metadata", data = json.encodeToString(it)) }

			val exportZippedFile = File(lockableRepo.context.cacheDir, "graphite_export_$timestamp.zip")
			CompressUtil.Zip.createZipFile(inputFile = exportDir, outputFile = exportZippedFile)

			return exportZippedFile
		} ?: return null
	}

	private fun writeFile(parentDir: File, fileName: String, data: String) {
		val file = File(parentDir, "$fileName.json")
		file.createNewFile()
		file.writeText(data)
	}
}

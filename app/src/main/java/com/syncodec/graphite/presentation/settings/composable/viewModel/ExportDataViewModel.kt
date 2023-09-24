package com.syncodec.graphite.presentation.settings.composable.viewModel

import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.exporter.schema3.ExportBucketItemObject
import com.syncodec.graphite.di.model.exporter.schema3.ExportBucketObject
import com.syncodec.graphite.di.model.exporter.schema3.ExportChapterObject
import com.syncodec.graphite.di.model.exporter.schema3.ExportNoteObject
import com.syncodec.graphite.di.model.exporter.schema3.ExportTagObject
import com.syncodec.graphite.di.repository.LockableRepo
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.utils.archiveUtil.CompressUtil
import io.realm.kotlin.types.RealmUUID
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
					.map { Triple(parentDir, it.id, json.encodeToString(it)) }
					.forEach(::writeFile)
			}

			if (isNotesSelected) {
				val parentDir = File(exportDir, "chapter")
				parentDir.mkdirs()
				repository1
					.getAllObjectOfType<ChapterObject>(includeLocked = includeLockedItems)
					.map { ExportChapterObject.fromObject(inputObject = it) }
					.map { Triple(parentDir, it.id, json.encodeToString(it)) }
					.forEach(::writeFile)
			}

			if (isBucketsSelected) {
				val parentDir = File(exportDir, "bucketItem")
				parentDir.mkdirs()
				repository1
					.getAllObjectOfType<BucketItemObject>(includeLocked = includeLockedItems)
					.map { ExportBucketItemObject.fromObject(inputObject = it) }
					.map { Triple(parentDir, it.id, json.encodeToString(it)) }
					.forEach(::writeFile)
			}

			if (isBucketsSelected) {
				val parentDir = File(exportDir, "bucket")
				parentDir.mkdirs()
				repository1
					.getAllObjectOfType<BucketObject>(includeLocked = includeLockedItems)
					.map { ExportBucketObject.fromObject(inputObject = it) }
					.map { Triple(parentDir, it.id, json.encodeToString(it)) }
					.forEach(::writeFile)
			}

			if (isTagsSelected) {
				val parentDir = File(exportDir, "tag")
				parentDir.mkdirs()
				repository1
					.getAllTag()
					.map { ExportTagObject.fromObject(inputObject = it) }
					.map { Triple(parentDir, it.id, json.encodeToString(it)) }
					.forEach(::writeFile)
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

			val exportZippedFile = File(lockableRepo.context.cacheDir, "graphite_export_$timestamp.zip")
			CompressUtil.Zip.createZipFile(inputFile = exportDir, outputFile = exportZippedFile)

			return exportZippedFile
		} ?: return null
	}

	private fun writeFile(data: Triple<File, RealmUUID, String>) {
		val file = File(data.first, "${data.second}.json")
		file.createNewFile()
		file.writeText(data.third)
	}
}

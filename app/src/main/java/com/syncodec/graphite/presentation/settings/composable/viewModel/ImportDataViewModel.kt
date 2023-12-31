package com.syncodec.graphite.presentation.settings.composable.viewModel

import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.model.local.TagObject
import com.syncodec.graphite.di.model.dataExchanger.Exportable
import com.syncodec.graphite.di.model.dataExchanger.Importable
import com.syncodec.graphite.di.repository.LockableRepo
import com.syncodec.graphite.di.repository.Repository
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class ImportDataViewModel(private val lockableRepo: LockableRepo) : ViewModel() {

	private val json = Json { ignoreUnknownKeys = true }

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	init {
		viewModelScope.launch(Dispatchers.Default) {
			lockableRepo.repositoryStatusFlow.collectLatest { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) _repository.tryEmit(repositoryStatus.repository)
			}
		}
	}

	@WorkerThread
	suspend fun importFromGraphite(inputUri: Uri, callback: suspend (Int, Int) -> Unit) {
		_repository.value?.let { repository1 ->
			lockableRepo.context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
				val inMemoryByteChannel = SeekableInMemoryByteChannel(inputStream.readBytes())
				val zipFile = ZipFile(inMemoryByteChannel)
				val zipArchiveEntryList = zipFile.entries.toList()

				var currentItemIndex = 0

				callback(zipArchiveEntryList.size, currentItemIndex)

				zipArchiveEntryList.filter { it.name.matches(chapterFileRegex) }.forEach { zipArchiveEntry ->
					decodeObject<Exportable.ExportChapterObject>(zipFile = zipFile, zipArchiveEntry = zipArchiveEntry)?.toObject()?.let(repository1::putChapterSuspended)
					callback(zipArchiveEntryList.size, ++currentItemIndex)
				}
				zipArchiveEntryList.filter { it.name.matches(noteFileRegex) }.forEach { zipArchiveEntry ->
					decodeObject<Exportable.ExportNoteObject>(zipFile = zipFile, zipArchiveEntry = zipArchiveEntry)?.toObject()?.let { noteObject ->
						repository1.putNote(noteObject = noteObject)
						importGraphiteAttachment(
							zipFile = zipFile,
							attachmentZipArchiveEntryList = zipArchiveEntryList.filter { it.name.matches(noteAttachmentRegex(id = noteObject.id)) },
							parentId = noteObject.id,
							repository1 = repository1
						) {
							callback(zipArchiveEntryList.size, ++currentItemIndex)
						}
					}
					callback(zipArchiveEntryList.size, ++currentItemIndex)
				}
				zipArchiveEntryList.filter { it.name.matches(bucketFileRegex) }.forEach { zipArchiveEntry ->
					decodeObject<Exportable.ExportBucketObject>(zipFile = zipFile, zipArchiveEntry = zipArchiveEntry)?.toObject()?.let(repository1::putBucket)
					callback(zipArchiveEntryList.size, ++currentItemIndex)
				}
				zipArchiveEntryList.filter { it.name.matches(bucketItemFileRegex) }.forEach { zipArchiveEntry ->
					decodeObject<Exportable.ExportBucketItemObject>(zipFile = zipFile, zipArchiveEntry = zipArchiveEntry)?.toObject()?.let(repository1::putBucketItem)
					callback(zipArchiveEntryList.size, ++currentItemIndex)
				}
				zipArchiveEntryList.filter { it.name.matches(tagFileRegex) }.forEach { zipArchiveEntry ->
					decodeObject<Exportable.ExportTagObject>(zipFile = zipFile, zipArchiveEntry = zipArchiveEntry)?.toObject()?.let(repository1::putTag)
					callback(zipArchiveEntryList.size, ++currentItemIndex)
				}
			}
		}
	}

	@OptIn(ExperimentalSerializationApi::class)
	@WorkerThread
	suspend fun importFromJourney(inputUri: Uri, callback: suspend (Int, Int) -> Unit) {
		_repository.value?.let { repository1 ->

			val defaultChapterId = repository1.getDefaultChapterId()
			val tagList = repository1.getAllObjectOfType<TagObject>(includeLocked = true)

			lockableRepo.context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
				val inMemoryByteChannel = SeekableInMemoryByteChannel(inputStream.readBytes())
				val zipFile = ZipFile(inMemoryByteChannel)

				val zipArchiveEntryList = zipFile.entries.toList()

				val archivedJourneyList = zipArchiveEntryList.filter { it.name.endsWith(".json") }

				archivedJourneyList
					.forEachIndexed { index, zipArchiveEntry ->
						try {
							val journeyNote = json.decodeFromStream<Importable.JourneyNote>(zipFile.getInputStream(zipArchiveEntry))

							val noteObject = journeyNote.toObject(parentId = defaultChapterId)
							repository1.putNote(noteObject = noteObject)

							val toAddTagList = journeyNote.tags?.mapNotNull { tag ->
								if (tag == null) return@mapNotNull null
								tagList.find { tagObject -> tagObject.tag == tag } ?: TagObject().apply {
									this.tag = tag
									repository1.putTag(tagObject = this)
								}
							}
							if (toAddTagList != null) repository1.updateTagConnections(objectId = noteObject.id, tagListToAdd = toAddTagList.map { it.id }, tagListToRemove = listOf())

							journeyNote.photos
								?.filterNotNull()
								?.mapNotNull { attachmentFileName -> zipArchiveEntryList.find { it.name == attachmentFileName } }
								?.forEach { attachmentZipArchiveEntry ->
									zipFile.getInputStream(attachmentZipArchiveEntry).use {
										repository1.attachmentRepository.putAttachment(parentId = noteObject.id, fileName = attachmentZipArchiveEntry.name, byteArray = it.readBytes())
									}
								}

							callback(archivedJourneyList.size, index)
						} catch (e: Exception) {
							if (BuildConfig.DEBUG) e.printStackTrace()
						}
					}
			}
		}
	}

	@OptIn(ExperimentalSerializationApi::class)
	@WorkerThread
	suspend fun importFromGoogleKeep(inputUri: Uri, callback: suspend (Int, Int) -> Unit) {
		_repository.value?.let { repository1 ->

			val defaultChapterId = repository1.getDefaultChapterId()
			val tagList = repository1.getAllObjectOfType<TagObject>(includeLocked = true)

			lockableRepo.context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
				val inMemoryByteChannel = SeekableInMemoryByteChannel(inputStream.readBytes())
				val zipFile = ZipFile(inMemoryByteChannel)

				val zipArchiveEntryList = zipFile.entries.toList()

				zipArchiveEntryList.filter { it.name.endsWith(".json") }
					.forEachIndexed { index, zipArchiveEntry ->
						try {
							val googleKeepNote = json.decodeFromStream<Importable.GoogleKeepNote>(zipFile.getInputStream(zipArchiveEntry))

							val noteObject = googleKeepNote.toObject(parentId = defaultChapterId)
							repository1.putNote(noteObject = noteObject)

							googleKeepNote.attachments
								?.mapNotNull { attachment -> zipArchiveEntryList.find { it.name == attachment.filePath } }
								?.forEach { attachmentZipArchiveEntry ->
									zipFile.getInputStream(attachmentZipArchiveEntry).use {
										repository1.attachmentRepository.putAttachment(parentId = noteObject.id, fileName = attachmentZipArchiveEntry.name, byteArray = it.readBytes())
									}
								}

							val toAddTagList = googleKeepNote.labels?.map { label ->
								tagList.find { tagObject -> tagObject.tag == label } ?: TagObject().apply {
									this.tag = label
									repository1.putTag(tagObject = this)
								}
							}
							if (toAddTagList != null) repository1.updateTagConnections(objectId = noteObject.id, tagListToAdd = toAddTagList.map { it.id }, tagListToRemove = listOf())

							callback(zipArchiveEntryList.size, index)
						} catch (e: Exception) {
							if (BuildConfig.DEBUG) e.printStackTrace()
						}
					}

			}
		}
	}

	private inline fun <reified T : Exportable> decodeObject(zipFile: ZipFile, zipArchiveEntry: ZipArchiveEntry): T? {
		return try {
			zipFile.getInputStream(zipArchiveEntry).use {
				json.decodeFromString<T>(it.readBytes().decodeToString())
			}
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			null
		}
	}

	private suspend fun importGraphiteAttachment(zipFile: ZipFile, attachmentZipArchiveEntryList: List<ZipArchiveEntry>, parentId: RealmUUID, repository1: Repository, callback: suspend () -> Unit) {
		attachmentZipArchiveEntryList.forEach { attachmentZipArchiveEntry ->
			zipFile.getInputStream(attachmentZipArchiveEntry).use { inputStream1 ->
				try {
					val fileName = attachmentZipArchiveEntry.name.split("$attachmentFileRegexBase$parentId/".toRegex())[1]
					repository1.attachmentRepository.putAttachment(parentId = parentId, fileName = fileName, byteArray = inputStream1.readBytes())
					callback()
				} catch (e: Exception) {
					if (BuildConfig.DEBUG) e.printStackTrace()
				}
			}
		}
	}


	companion object {

		private const val uuidRegexString = "[0-9a-z]{8}\\b-[0-9a-z]{4}\\b-[0-9a-z]{4}\\b-[0-9a-z]{4}\\b-[0-9a-z]{12}"
		private val chapterFileRegex = """graphite_export_[0-9]+/chapter/$uuidRegexString.json""".toRegex()
		private val noteFileRegex = """graphite_export_[0-9]+/note/$uuidRegexString.json""".toRegex()
		private val bucketFileRegex = """graphite_export_[0-9]+/bucket/$uuidRegexString.json""".toRegex()
		private val bucketItemFileRegex = """graphite_export_[0-9]+/bucketItem/$uuidRegexString.json""".toRegex()
		private val tagFileRegex = """graphite_export_[0-9]+/tag/$uuidRegexString.json""".toRegex()
		private val attachmentFileRegexBase = """graphite_export_[0-9]+/attachment/""".toRegex()
		private val attachmentFileRegex = """$attachmentFileRegexBase$uuidRegexString.*""".toRegex()

		fun noteAttachmentRegex(id: RealmUUID): Regex {
			return """$attachmentFileRegexBase$id/.+""".toRegex()
		}

		data class ImportProcess(
			val totalItem: Int,
			val currentItem: Int,
		) {
			fun percent() = currentItem.toFloat().div(totalItem)
		}
	}
}

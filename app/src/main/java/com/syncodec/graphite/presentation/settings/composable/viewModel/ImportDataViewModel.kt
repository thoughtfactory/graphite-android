package com.syncodec.graphite.presentation.settings.composable.viewModel

import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.importer.JourneyNote
import com.syncodec.graphite.di.repository.LockableRepo
import com.syncodec.graphite.di.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
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

	@OptIn(ExperimentalSerializationApi::class)
	@WorkerThread
	suspend fun importFromJourney(inputUri: Uri, callback: suspend (Int, Int) -> Unit) {
		_repository.value?.let { repository1 ->

			val defaultChapterId = repository1.getDefaultChapterId()

			val inputStream = lockableRepo.context.contentResolver.openInputStream(inputUri)
			if (inputStream != null) {
				val inMemoryByteChannel = SeekableInMemoryByteChannel(inputStream.readBytes())
				val zipFile = ZipFile(inMemoryByteChannel)

				val zipArchiveEntryList = zipFile.entries.toList()

				val archivedJourneyList = zipArchiveEntryList.filter { it.name.endsWith(".json") }

				archivedJourneyList
					.forEachIndexed { index, zipArchiveEntry ->
						try {
							val journeyNote = json.decodeFromStream<JourneyNote>(zipFile.getInputStream(zipArchiveEntry))

							val noteObject = NoteObject().apply {
								journeyNote.dateJournal?.let { this.createdTimestamp = it; this.userTimestamp = it }
								journeyNote.dateModified?.let { this.modifiedTimestamp = it }
								if (journeyNote.lat != null && journeyNote.lon != null) this.setLatLng(LatLng(journeyNote.lat, journeyNote.lon))
								this.address = journeyNote.address
								this.content2 = journeyNote.text
								this.isFavourite = journeyNote.favourite ?: false
								this.parentId = defaultChapterId
							}

							repository1.putNote(noteObject = noteObject)

							journeyNote.photos
								?.filterNotNull()
								?.mapNotNull { attachmentFileName -> zipArchiveEntryList.find { it.name == attachmentFileName } }
								?.forEach { attachmentZipArchiveEntry ->
									zipFile.getInputStream(attachmentZipArchiveEntry).use {
										repository1.attachmentRepository.putAttachment(parentId = noteObject.id, fileName = attachmentZipArchiveEntry.name, byteArray = it.readBytes())
									}
								}

							callback(archivedJourneyList.size, index)
						} catch (e : Exception) {
							if (BuildConfig.DEBUG) e.printStackTrace()
						}
					}
			}
			inputStream?.close()
		}
	}
}

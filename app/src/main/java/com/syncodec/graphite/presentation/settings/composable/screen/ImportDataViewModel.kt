package com.syncodec.graphite.presentation.settings.composable.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.importer.GoogleKeepNote
import com.syncodec.graphite.di.model.importer.JourneyNote
import com.syncodec.graphite.di.model.serializer.NoteObjectSerializer
import com.syncodec.graphite.di.repository.Repository
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.zip.ZipFile
import org.json.JSONObject
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class ImportDataViewModel(private val repository: Repository) : ViewModel() {
	private val json = Json {
		ignoreUnknownKeys = true
		coerceInputValues = true
	}

	val repositoryState = repository.repositoryState
	private var defaultChapterId: RealmUUID? = null

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryState.collect {
				if (it == Repository.Companion.RepositoryState.Success) defaultChapterId = repository.getDefaultChapterId()
			}
		}
	}

	@OptIn(ExperimentalSerializationApi::class)
	fun importGraphiteData(seven7File: SevenZFile, callback: suspend (ImportDataCallback) -> Unit) {
		viewModelScope.launch(Dispatchers.Default) {
			val totalFile = seven7File.entries.count()
			callback(ImportDataCallback.Importing(0, totalFile))

			seven7File.entries.forEachIndexed { index, sevenZArchiveEntry ->
				val dataType = sevenZArchiveEntry.name.split("/")[1]
				val inputStream = seven7File.getInputStream(sevenZArchiveEntry)

				when (dataType) {
					"chapter" -> repository.putChapter(chapterObject = json.decodeFromStream(inputStream))
					"note" -> repository.putNote(noteObject = json.decodeFromStream(NoteObjectSerializer, inputStream))
				}
				callback(ImportDataCallback.Importing(index + 1, totalFile))
			}

			callback(ImportDataCallback.Success)
		}
	}

	@OptIn(ExperimentalSerializationApi::class)
	fun importJourneyData(zipFile: ZipFile, transformData: suspend (Int, RealmUUID, String) -> Unit, callback: suspend (ImportDataCallback) -> Unit) {
		viewModelScope.launch(Dispatchers.Default) {
			if (defaultChapterId == null) {
				callback(ImportDataCallback.Error)
				return@launch
			}

			val entries = zipFile
				.entries
				.toList()
				.filter { it.name.endsWith(".json") }

			callback(ImportDataCallback.Importing(0, entries.size))

			entries.forEachIndexed { index, zipArchiveEntry ->
				val inputStream = zipFile.getInputStream(zipArchiveEntry)
				val journeyNote = json.decodeFromStream<JourneyNote>(inputStream)
				val noteId = RealmUUID.random()
				journeyNote.photos?.map { zipFile.getEntry(it) }?.let {
					repository.attachmentRepository.putAttachment(parentId = noteId, zipFile = zipFile, attachmentList = it)
				}

				inputStream.bufferedReader().use { reader -> transformData(index, noteId, reader.readText()) }
				inputStream.close()
			}
		}
	}

	@Synchronized
	fun storeJourneyData(noteId: RealmUUID, journeyData: JSONObject, dataText: String?, dataJson: JSONObject?) {
		NoteObject().apply {
			this.id = noteId
			this.createdTimestamp = journeyData.optLong("date_journal").let { if (it == 0L) System.currentTimeMillis() else it }
			this.modifiedTimestamp = journeyData.optLong("date_modified").let { if (it == 0L) System.currentTimeMillis() else it }
			this.userTimestamp = this.createdTimestamp
			if (journeyData.has("lat") && journeyData.has("lon")) {
				val lat = journeyData.optDouble("lat")
				val lng = journeyData.optDouble("lon")
				this.setLatLng(LatLng(lat, lng))
			}
			this.address = journeyData.optString("address")
			this.content = dataJson.toString()
			this.contentThumbnail = dataText?.take(256)
			this.isFavourite = journeyData.optBoolean("favorite")
			this.parentId = defaultChapterId

			repository.putNote(this)
			Log.d("npr71", "storeJourneyData: ${this.id}")
		}
	}

	@OptIn(ExperimentalSerializationApi::class)
	fun importGoogleKeepData(zipFile: ZipFile, callback: suspend (ImportDataCallback) -> Unit) {
		viewModelScope.launch(Dispatchers.Default) {
			if (defaultChapterId == null) {
				callback(ImportDataCallback.Error)
				return@launch
			}
			val entries = zipFile
				.entries
				.toList()
				.filter { it.name.let { it.startsWith("Takeout/Keep/") && it.endsWith(".json") } }

			callback(ImportDataCallback.Importing(0, entries.size))
			entries.forEachIndexed { index, zipArchiveEntry ->
				val inputStream = zipFile.getInputStream(zipArchiveEntry)
				val googleKeepNote = json.decodeFromStream<GoogleKeepNote>(inputStream)
				NoteObject().apply {
					googleKeepNote.creationTimestampUsec?.let { this.createdTimestamp = it / 1000 }
					googleKeepNote.userEditedTimestampUsec?.let { this.modifiedTimestamp = it / 1000 }
					googleKeepNote.userEditedTimestampUsec?.let { this.userTimestamp = it / 1000 }
					this.title = googleKeepNote.title
					this.content = googleKeepNote.encodeToTipTapFormat()
					this.contentThumbnail = googleKeepNote.textContent?.let { it.substring(0, minOf(256, it.length)) }
					this.isFavourite = googleKeepNote.isPinned ?: false
					this.parentId = defaultChapterId

					val attachmentList =
						googleKeepNote
							.attachments
							?.map { zipFile.getEntries("Takeout/Keep/${it.filePath}").toList() }
							?.flatten() ?: listOf()

					googleKeepNote.attachments?.forEach { zipFile.getEntries("Takeout/Keep/${it.filePath}") }

					repository.putNote(noteObject = this)
					repository.attachmentRepository.putAttachment(parentId = this.id, zipFile = zipFile, attachmentList = attachmentList)
				}
				callback(ImportDataCallback.Importing(index + 1, entries.size))
			}
			callback(ImportDataCallback.Success)
		}
	}

	companion object {
		sealed class ImportDataCallback {
			data class Importing(val progress: Int, val total: Int) : ImportDataCallback()
			object Success : ImportDataCallback()
			object Error : ImportDataCallback()
		}
	}
}

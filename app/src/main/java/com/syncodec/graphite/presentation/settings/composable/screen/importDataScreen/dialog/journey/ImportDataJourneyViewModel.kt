package com.syncodec.graphite.presentation.settings.composable.screen.importDataScreen.dialog.journey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.repository.repository.Repository
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.apache.commons.compress.archivers.zip.ZipFile
import org.json.JSONObject
import org.koin.android.annotation.KoinViewModel
import java.io.File


@KoinViewModel
class ImportDataJourneyViewModel(private val repository : Repository) : ViewModel() {

	val objectMapper : ObjectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	val repositoryState = repository.repositoryState

	private val defaultChapterId : MutableStateFlow<RealmUUID?> = MutableStateFlow(null)

	val noteConcurrentQueue = Channel<Pair<NoteObject, Boolean>>(capacity = Channel.Factory.UNLIMITED)
	val attachmentConcurrentQueue = Channel<Pair<RealmUUID, File>>(capacity = Channel.Factory.UNLIMITED)

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryState.collect {
				if (it == Repository.Companion.RepositoryState.Success) repository.getDefaultChapterId().let { chapterId -> defaultChapterId.tryEmit(chapterId) }
			}
		}
		viewModelScope.launch(Dispatchers.Default) {
			noteConcurrentQueue.receiveAsFlow().collect { (noteObject, isLast) ->
				noteObject.parentId = defaultChapterId.value
				repository.putNote(noteObject)
			}
		}
		viewModelScope.launch(Dispatchers.Default) {
			attachmentConcurrentQueue.receiveAsFlow().collect { (noteId, file) ->
				repository.attachmentRepository.putAttachment(noteId, file)
			}
		}
	}

	fun checkFileIntegrity(
		zipFile : ZipFile,
		integrityCallback : (Boolean, Exception?) -> Unit,
		onSuccessListener : () -> Unit,
		onFailureListener : () -> Unit
	) {
		try {
			zipFile.entries.toList().filter { it.isDirectory }.let {
				integrityCallback(true, DirectoryDetectedException("Directory detected in zip file. It will be ignored."))
			}
			zipFile.entries.toList().filter { ! it.isDirectory && it.name.endsWith(".json") }.forEach { zipArchiveEntry ->
				try {
					zipFile.getInputStream(zipArchiveEntry).bufferedReader().use { reader ->
						reader.readText()
					}.let { jsonString ->
						val jsonObject = JSONObject(jsonString)
						jsonObject.keys().asSequence().toList().filter { ! JourneyDataKeyList.contains(it) }.let {
							integrityCallback(true, FileReadException("Unknown key detected in some files. It will be ignored."))
						}
					}
				} catch (e : Exception) {
					integrityCallback(true, FileReadException("File ${zipArchiveEntry.name} cannot be read. It will be ignored."))
				}
			}
			onSuccessListener()
		} catch (e : Exception) {
			integrityCallback(false, ImportFromJourneyException("Unknown error. Aborting import."))
			onFailureListener()
		}
	}

	companion object {
		open class ImportFromJourneyException(message : String) : Exception(message)
		class DirectoryDetectedException(message : String) : ImportFromJourneyException(message)
		class FileReadException(message : String) : ImportFromJourneyException(message)

		val JourneyDataKeyList = listOf(
			"text",
			"date_modified",
			"date_journal",
			"id",
			"preview_text",
			"address",
			"music_artist",
			"music_title",
			"lat",
			"lon",
			"mood",
			"label",
			"folder",
			"sentiment",
			"timezone",
			"favourite",
			"type",
			"linked_account_id",
			"weather",
			"photos",
			"tags",
		)
	}
}

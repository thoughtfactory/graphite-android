package com.syncodec.graphite.presentation.settings.composable.screen.importDataScreen.dialog.googleKeep

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
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class ImportDataGoogleKeepViewModel(private val repository : Repository) : ViewModel() {

	val objectMapper : ObjectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	val repositoryState = repository.repositoryState

	private val defaultChapterId : MutableStateFlow<RealmUUID?> = MutableStateFlow(null)

	val concurrentQueue = Channel<Triple<NoteObject, ZipFile, List<ZipArchiveEntry>>>(capacity = Channel.Factory.UNLIMITED)

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryState.collect {
				if (it == Repository.Companion.RepositoryState.Success) repository.getDefaultChapterId().let { chapterId -> defaultChapterId.tryEmit(chapterId) }
			}
		}
		viewModelScope.launch(Dispatchers.Default) {
			concurrentQueue.receiveAsFlow().collect { (noteObject, zipFile, attachmentList) ->
				noteObject.parentId = defaultChapterId.value
				repository.putNote(noteObject)
				repository.attachmentRepository.putAttachment(parentId = noteObject.id, zipFile, attachmentList)
			}
		}
	}

	companion object {
		open class ImportFromGoogleKeepException(message : String) : Exception(message)
		class FileReadException(message : String) : ImportFromGoogleKeepException(message)

		val GoogleKeepDataKeyList = listOf(
			"attachments",
			"color",
			"isTrashed",
			"isPinned",
			"isArchived",
			"textContent",
			"title",
			"userEditedTimestampUsec",
			"creationTimestampUsec",
			"labels",
			"listContent",
			"reminder",
			"annotations",
		)
	}
}

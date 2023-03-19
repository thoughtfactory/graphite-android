package com.syncodec.graphite.presentation.settings.composable.screen.importDataScreen.dialog.graphite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.di.repository.koinRepository.KoinRepository
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.android.annotation.KoinViewModel
import java.io.File


@KoinViewModel
class ImportDataGraphiteViewModel(private val repository : KoinRepository) : ViewModel() {

	val objectMapper : ObjectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	val repositoryState = repository.repositoryState

	private val defaultChapterId : MutableStateFlow<RealmUUID?> = MutableStateFlow(null)

	val noteConcurrentQueue = Channel<Pair<NoteObject, Boolean>>(capacity = Channel.Factory.UNLIMITED)
	val attachmentConcurrentQueue = Channel<Pair<RealmUUID, File>>(capacity = Channel.Factory.UNLIMITED)

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryState.collect {
				if (it == RepositoryState.SUCCESS) repository.getDefaultChapterId().let { chapterId -> defaultChapterId.tryEmit(chapterId) }
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

	fun importData(file : File, callback : suspend (Boolean) -> Unit, progress : suspend (Int, Int) -> Unit) {
		viewModelScope.launch(Dispatchers.Default) {
			try {
				val bucketDir = File(file, "bucket")
				val bucketItemDir = File(file, "bucketItem")
				val chapterDir = File(file, "chapter")
				val noteDir = File(file, "note")
				val tagDir = File(file, "tag")
				val attachmentDir = File(file, "attachment")

				val total = (bucketDir.listFiles()?.size ?: 0) +
						(bucketItemDir.listFiles()?.size ?: 0) +
						(chapterDir.listFiles()?.size ?: 0) +
						(noteDir.listFiles()?.size ?: 0) +
						(tagDir.listFiles()?.size ?: 0) +
						(attachmentDir.listFiles()?.size ?: 0)
				progress(0, total)
				var progressCount = 0
				if (bucketDir.isDirectory) bucketDir.listFiles()
					?.forEach { BucketObject(JSONObject(it.readText())).let { repository.putBucket(it); progress(progressCount ++, total) } }
				if (bucketItemDir.isDirectory) bucketItemDir.listFiles()
					?.forEach { BucketItemObject(JSONObject(it.readText())).let { repository.putBucketItem(it); progress(progressCount ++, total) } }
				if (chapterDir.isDirectory) chapterDir.listFiles()
					?.forEach { ChapterObject(JSONObject(it.readText())).let { repository.putChapter(it); progress(progressCount ++, total) } }
				if (noteDir.isDirectory) noteDir.listFiles()
					?.forEach {
						NoteObject(JSONObject(it.readText())).let {
							if (it.parentId == null) it.parentId = defaultChapterId.value
							repository.putNote(it); progress(progressCount ++, total)
						}
					}
				if (tagDir.isDirectory) tagDir.listFiles()
					?.forEach { TagObject(JSONObject(it.readText())).let { repository.putTag(it); progress(progressCount ++, total) } }

				repository.attachmentRepository.importAttachmentFromGraphite(attachmentDir)

				callback(true)
			} catch (e : Exception) {
				callback(false)
			}
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

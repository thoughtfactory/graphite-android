package com.syncodec.graphite.settingsComponent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.android.gms.maps.model.LatLng
import com.syncodec.graphite.Graphite
import com.syncodec.graphite.database.attachment.AttachmentDbEntry
import com.syncodec.graphite.database.export.NoteExport
import com.syncodec.graphite.database.note.NoteDbEntry
import com.syncodec.graphite.miscellaneous.CollectionUtils.Companion.listOfField
import com.syncodec.graphite.miscellaneous.FileUtils
import com.syncodec.graphite.miscellaneous.generatePrimaryKey
import com.syncodec.graphite.repository.AttachmentRepository
import com.syncodec.graphite.repository.NoteRepository
import com.syncodec.graphite.repository.TagRepository
import org.json.JSONObject
import java.io.File


class SettingsViewModel(application: Application) : AndroidViewModel(application) {

	private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

	private val noteRepository: NoteRepository =
		NoteRepository.getInstance(graphite = application as Graphite)
	private val attachmentRepository: AttachmentRepository =
		AttachmentRepository.getInstance(graphite = application as Graphite)
	private val tagRepository: TagRepository =
		TagRepository.getInstance(graphite = application as Graphite)

	lateinit var activityState: SettingsActivity.ActivityState

	val notebookList = noteRepository.notebookListFlow

	suspend fun insertNote(notebookKey: String, data: String) {
		val dataObject = JSONObject(data)
		val dataJson = dataObject.optJSONObject("dataJson")
		val dataText = dataObject.optString("dataText")
		val importData = dataObject.getJSONObject("importData")

		val noteDbEntry = NoteDbEntry(
			key = generatePrimaryKey(),
			timezone = importData.optString("timezone"),
			chapterPath = mutableListOf(),
			notebookKey = notebookKey
		).apply {
			this.createdTimestamp = System.currentTimeMillis()
			this.modifiedTimestamp = this.createdTimestamp
			importData.optLong("date_journal").also {
				this.userTimestamp = if (it != 0L) it else this.createdTimestamp
			}
			this.title = null
			this.contentThumbnail = dataText.substring(0, minOf(128, dataText.length))
			this.attachmentThumbnail = null
//			this.content = JSONObject(dataJson)
			importData.optDouble("lat").also { lat ->
				if (!lat.isNaN()) {
					importData.optDouble("lon").also { lon ->
						if (!lon.isNaN()) this.latLng = LatLng(lat, lon)
					}
				}
			}
			this.address = importData.optString("address")
			this.mood = importData.optInt("sentiment")
		}

		noteRepository.putNote(noteDbEntry = noteDbEntry, noteContent = dataObject)
	}

	suspend fun exportNotes(notebookKey: String): String? {
		val cacheDir: File = getApplication<Graphite>().cacheDir
		val exportDir = File("${cacheDir.path}/export/export_notes_${System.currentTimeMillis()}")
		exportDir.mkdirs()

		val noteKeyList = noteRepository.openNotebook(notebookKey)
		noteKeyList.forEach { key ->
			try {
				noteRepository.getNote(key = key).apply {
					val noteDbEntry = first
					val noteContent = second

					if (noteDbEntry != null) {
						val attachmentDataList =
							attachmentRepository.getAttachment(noteKey = noteDbEntry.key)
						val attachmentList = attachmentRepository.getAttachmentUri(
							keyList = attachmentDataList.listOfField(AttachmentDbEntry::key)
						)

						NoteExport(
							key = noteDbEntry.key,
							createdTimestamp = noteDbEntry.createdTimestamp,
							modifiedTimestamp = noteDbEntry.modifiedTimestamp,
							userTimestamp = noteDbEntry.userTimestamp,
							timezone = noteDbEntry.timezone,
							chapterPath = noteDbEntry.chapterPath,
							notebookKey = noteDbEntry.notebookKey,
							title = noteDbEntry.title,
							content = noteContent?.toString(),
							latLng = noteDbEntry.latLng,
							address = noteDbEntry.address,
							attachmentKey = attachmentList.keys.toList()
						).apply {
							File("${exportDir.path}/${noteDbEntry.key}.json")
								.writeText(objectMapper.writeValueAsString(this))
						}
					}
				}
			} catch (exception: Exception) {
			}
		}

		activityState.dataExchange.value = SettingsActivity.DataExchange.NONE
		FileUtils.zipFolder(exportDir.path, "${exportDir.path}.zip")

		return if (exportDir.exists()) "${exportDir.path}.zip" else null
	}
}

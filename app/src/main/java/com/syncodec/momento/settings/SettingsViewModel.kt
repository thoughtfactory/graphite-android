package com.syncodec.momento.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.syncodec.momento.Momento
import com.syncodec.momento.database.note.LocationData
import com.syncodec.momento.database.note.Note
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import com.syncodec.momento.repository.AttachmentRepository
import com.syncodec.momento.repository.NoteRepository
import com.syncodec.momento.repository.TagRepository
import org.json.JSONObject
import kotlin.math.min

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

	private val noteRepository: NoteRepository = NoteRepository.getInstance(momento = application as Momento)
	private val attachmentRepository: AttachmentRepository = AttachmentRepository(momento = application as Momento)
	private val tagRepository: TagRepository = TagRepository.getInstance(momento = application as Momento)

	lateinit var activityState: SettingsActivity.ActivityState

	suspend fun insertNote(notebookKey: String, data: String) {
		val dataObject = JSONObject(data)
		val dataJson = dataObject.getString("dataJson")
		val dataText = dataObject.getString("dataText")
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
			importData.optDouble("lat").also {lat ->
				if (!lat.isNaN()) {
					importData.optDouble("lon").also { lon ->
						if (!lon.isNaN()) {
							this.location = LocationData(latitude = lat, longitude = lon)
						}
					}
				}
			}
			this.address = importData.optString("address")
			this.mood = importData.optInt("sentiment")
		}

		val note = Note(
			key = noteDbEntry.key
		).apply {
			this.content = dataJson
		}

		noteRepository.putNote(noteDbEntry = noteDbEntry, note = note)
	}
}

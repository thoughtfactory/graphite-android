package com.syncodec.graphite.settingsComponent

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.syncodec.graphite.Graphite
import com.syncodec.graphite.database.attachment.AttachmentDbEntry
import com.syncodec.graphite.database.export.NoteExport
import com.syncodec.graphite.database.note.NoteDbEntry
import com.syncodec.graphite.miscellaneous.CollectionUtils.Companion.listOfField
import com.syncodec.graphite.miscellaneous.FileUtils
import com.syncodec.graphite.miscellaneous.FileUtils.Companion.copyInputStreamToOutputStream
import com.syncodec.graphite.miscellaneous.FileUtils.Companion.getFileExtension
import com.syncodec.graphite.miscellaneous.FileUtils.Companion.getFileFromUri
import com.syncodec.graphite.miscellaneous.StringUtils.Companion.encrypt
import com.syncodec.graphite.miscellaneous.generatePrimaryKey
import com.syncodec.graphite.repository.AttachmentRepository
import com.syncodec.graphite.repository.NoteRepository
import com.syncodec.graphite.repository.TagRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipFile


class SettingsViewModel(application: Application) : AndroidViewModel(application) {

	private val objectMapper: ObjectMapper = jsonMapper { addModule(kotlinModule()) }

	private val noteRepository: NoteRepository =
		NoteRepository.getInstance(graphite = application as Graphite)
	private val attachmentRepository: AttachmentRepository =
		AttachmentRepository.getInstance(graphite = application as Graphite)
	private val tagRepository: TagRepository =
		TagRepository.getInstance(graphite = application as Graphite)

	val firebaseAuth = Firebase.auth
	val email: MutableState<String?> = mutableStateOf(null)


	lateinit var activityState: SettingsActivity.ActivityState

	val notebookList = noteRepository.notebookListFlow

	suspend fun insertNote(notebookKey: String, data: String) {
		val dataObject = JSONObject(data)
		val dataJson = dataObject.optJSONObject("dataJson")
		val dataText = dataObject.optString("dataText")
		val importData = dataObject.getJSONObject("importData")
		val cachePath = dataObject.getString("cachePath")

		when (dataObject.getString("importer")) {
			"graphite" -> {
				NoteDbEntry(
					key = generatePrimaryKey(),
					timezone = importData.optString("timezone"),
					chapterPath = mutableListOf(),
					notebookKey = notebookKey
				).apply {
					this.createdTimestamp =
						if (importData.optLong("createdTimestamp") != 0L) importData.optLong("createdTimestamp") else System.currentTimeMillis()
					this.modifiedTimestamp =
						if (importData.optLong("modifiedTimestamp") != 0L) importData.optLong("modifiedTimestamp") else System.currentTimeMillis()
					this.userTimestamp =
						if (importData.optLong("userTimestamp") != 0L) importData.optLong("userTimestamp") else System.currentTimeMillis()
					this.title = importData.optString("title")
					this.contentThumbnail = dataText.substring(0, minOf(256, dataText.length)).encrypt()
					importData.optDouble("lat").also { lat ->
						if (!lat.isNaN()) {
							importData.optDouble("lon").also { lon ->
								if (!lon.isNaN()) this.latLng = LatLng(lat, lon)
							}
						}
					}

					importData.optJSONObject("latLng")?.also {
						this.latLng = LatLng(it.optDouble("latitude"), it.optDouble("longitude"))
					}

					this.address = importData.optString("address")
					this.mood = importData.optInt("mood")

					importData.optJSONArray("tagList")?.also {
						val tagList: MutableList<String> = mutableListOf()
						for (i in 0 until it.length()) {
							it.optString(i)?.also {
								tagRepository.putTag(it)
								tagList.add(it)
							}
						}
						tagRepository.connectTag(this.key, tagList)
					}

					val cacheFile = File(cachePath)
					importData.optJSONArray("attachmentKey")?.also {
						val attachmentMap: MutableMap<AttachmentDbEntry, Uri> = mutableMapOf()
						for (i in 0 until it.length()) {
							it.optString(i)?.also {
								try {
									val file = cacheFile.listFiles { dir, name -> name.contains(it) }
										?.firstOrNull()
									if (file!=null) {
										val uri = file.toUri()
										val mimeType =
											getApplication<Graphite>().contentResolver.getType(uri)
										val extension = getApplication<Graphite>().getFileExtension(uri)

										AttachmentDbEntry(
											key = generatePrimaryKey(),
											createdTimestamp = this.createdTimestamp,
											mimeType = mimeType,
											extension = extension,
											noteKey = this.key,
											chapterPath = this.chapterPath,
											notebookKey = this.notebookKey
										).apply { attachmentMap[this] = uri }
									}
								} catch (exception: Exception) {

								}
							}
						}
						this.attachmentKeyList =
							attachmentMap.keys.listOfField(AttachmentDbEntry::key)

						var bitmap: Bitmap? = null
						attachmentMap.forEach { (data, uri) ->
							try {
								BitmapFactory.decodeStream(
									getApplication<Graphite>().contentResolver.openInputStream(uri)
								)?.also {
									bitmap = it
									return@forEach
								}
							} catch (exception: Exception) {

							}
						}
						this.attachmentThumbnail = bitmap
						attachmentRepository.putAttachment(attachmentMap.toList(), this.key)
					}

					noteRepository.putNote(noteDbEntry = this, noteContent = dataJson)
				}
			}
			"journey" -> {
				NoteDbEntry(
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
					this.contentThumbnail = dataText.substring(0, minOf(256, dataText.length)).encrypt()
					this.attachmentThumbnail = null
					importData.optDouble("lat").also { lat ->
						if (!lat.isNaN()) {
							importData.optDouble("lon").also { lon ->
								if (!lon.isNaN()) this.latLng = LatLng(lat, lon)
							}
						}
					}
					this.address = importData.optString("address")
					this.mood = importData.optInt("sentiment")

					importData.optJSONArray("tags")?.also {
						val tagList: MutableList<String> = mutableListOf()
						for (i in 0 until it.length()) {
							it.optString(i)?.also {
								tagRepository.putTag(it)
								tagList.add(it)
							}
						}
						tagRepository.connectTag(this.key, tagList)
					}

					importData.optJSONArray("photos")?.also {
						val attachmentMap: MutableMap<AttachmentDbEntry, Uri> = mutableMapOf()
						for (i in 0 until it.length()) {
							try {
								it.optString(i)?.also {
									val file = File("$cachePath/$it")
									val uri = file.toUri()
									val mimeType =
										getApplication<Graphite>().contentResolver.getType(uri)
									val extension = getApplication<Graphite>().getFileExtension(uri)

									AttachmentDbEntry(
										key = generatePrimaryKey(),
										createdTimestamp = this.createdTimestamp,
										mimeType = mimeType,
										extension = extension,
										noteKey = this.key,
										chapterPath = this.chapterPath,
										notebookKey = this.notebookKey
									).apply { attachmentMap[this] = uri }
								}
							} catch (exception: Exception) {

							}
						}
						this.attachmentKeyList =
							attachmentMap.keys.listOfField(AttachmentDbEntry::key)

						var bitmap: Bitmap? = null
						attachmentMap.forEach { (data, uri) ->
							try {
								BitmapFactory.decodeStream(
									getApplication<Graphite>().contentResolver.openInputStream(uri)
								)?.also {
									bitmap = it
									return@forEach
								}
							} catch (exception: Exception) {

							}
						}
						this.attachmentThumbnail = bitmap
						attachmentRepository.putAttachment(attachmentMap.toList(), this.key)
					}

					noteRepository.putNote(noteDbEntry = this, noteContent = dataJson)
				}
			}
		}
	}

	suspend fun exportNotes(notebookKey: String): String? {
		val cacheDir: File = getApplication<Graphite>().cacheDir
		val exportDir = File("${cacheDir.path}/export/graphite_${System.currentTimeMillis()}")
		exportDir.mkdirs()

		val noteKeyList = noteRepository.openNotebook(notebookKey)
		activityState.exchangeDataSize.value = noteKeyList.size

		noteKeyList.forEachIndexed { index, key ->
			activityState.currentImportFileIndex.value = index
			activityState.currentImportFileName.value = key

			try {
				noteRepository.getNote(key = key).apply {
					val noteDbEntry = first
					val noteContent = second

					if (noteDbEntry != null) {
						val attachmentDataList =
							attachmentRepository.getAttachment(noteKey = noteDbEntry.key)
						val attachmentList = attachmentRepository.getAttachmentUri(
							keyList = attachmentDataList.listOfField(AttachmentDbEntry::key),
							extensionList = attachmentDataList.listOfField(AttachmentDbEntry::extension)
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
							attachmentKey = attachmentList.keys.toList(),
							tagList = listOf()
						).apply {
							File("${exportDir.path}/${noteDbEntry.key}.json")
								.writeText(objectMapper.writeValueAsString(this))
						}
					}
				}
			} catch (exception: Exception) {
			}

			attachmentRepository
				.getAttachmentForNotebook(notebookKey = notebookKey)
				.onEach { (key, data) ->
					try {
						val extension =
							data.second?.let { getApplication<Graphite>().getFileExtension(uri = it) }
						val file =
							File("${exportDir.path}/${key}${if (extension != null) ".$extension" else ""}")
						val outputStream = file.outputStream()
						val inputStream =
							data.second?.let {
								getApplication<Graphite>().contentResolver.openInputStream(it)
							}
						if (inputStream != null) {
							copyInputStreamToOutputStream(inputStream, outputStream)
						}
					} catch (exception: Exception) {

					}
				}
		}

		activityState.dataExchange.value = SettingsActivity.DataExchange.NONE
		FileUtils.zipFolder(exportDir.path, "${exportDir.path}.zip")

		return if (exportDir.exists()) "${exportDir.path}.zip" else null
	}

	fun importFromGraphite(uri: Uri?) {
		viewModelScope.launch(Dispatchers.IO) {
			val file = getApplication<Graphite>().getFileFromUri(uri = uri)
			if (file != null) {
				try {
					val zipFile = ZipFile(file)

					val destFile =
						File("${getApplication<Graphite>().cacheDir.path}/${System.currentTimeMillis()}")
					destFile.mkdirs()
					zipFile.entries().toList().filter { it.name.split(".").lastOrNull() != "json" }
						.forEach {
							val inputStream = zipFile.getInputStream(it)
							val outputStream = File("${destFile.path}/${it.name}").outputStream()
							copyInputStreamToOutputStream(inputStream, outputStream)
						}

					val fileList = zipFile.entries().toList()
						.filter { it.name.split(".").lastOrNull() == "json" }
					activityState.exchangeDataSize.value = fileList.size

					var currentIndex = 0

					while (currentIndex < fileList.size) {
						if (activityState.isDataSaving.value) {
							delay(100)
						} else {
							val jsonString = String(
								zipFile.getInputStream(fileList[currentIndex]).readBytes()
							)
							activityState.richTextEditor.exec("editor.importData($jsonString, \"graphite\", \"${destFile.path}\");")
							activityState.isDataSaving.value = true
							activityState.currentImportFileIndex.value = currentIndex
							activityState.currentImportFileName.value = fileList[currentIndex].name
							currentIndex++
						}
					}

					activityState.dataExchange.value = SettingsActivity.DataExchange.NONE
					activityState.exchangeDataSize.value = 0
					activityState.currentImportFileIndex.value = 1
					CoroutineScope(Dispatchers.Main).launch {
						Toast.makeText(
							getApplication(),
							"${fileList.size} entries imported",
							Toast.LENGTH_LONG
						).show()
					}
				} catch (exception: Exception) {
					activityState.dataExchange.value = SettingsActivity.DataExchange.NONE
					activityState.exchangeDataSize.value = 0
					activityState.currentImportFileIndex.value = 1

					exception.printStackTrace()

					CoroutineScope(Dispatchers.Main).launch {
						Toast.makeText(
							getApplication(),
							"Sorry, can't process selected file",
							Toast.LENGTH_LONG
						).show()
					}
				}
			} else {
				activityState.dataExchange.value = SettingsActivity.DataExchange.NONE
				activityState.exchangeDataSize.value = 0
				activityState.currentImportFileIndex.value = 1
			}
		}
	}

	fun importFromJourney(uri: Uri?) {
		viewModelScope.launch(Dispatchers.IO) {
			val file = getApplication<Graphite>().getFileFromUri(uri = uri)
			if (file != null) {
				try {
					val zipFile = ZipFile(file)

					val destFile =
						File("${getApplication<Graphite>().cacheDir.path}/${System.currentTimeMillis()}")
					destFile.mkdirs()
					zipFile.entries().toList().filter { it.name.split(".").lastOrNull() != "json" }
						.forEach {
							val inputStream = zipFile.getInputStream(it)
							val outputStream = File("${destFile.path}/${it.name}").outputStream()
							copyInputStreamToOutputStream(inputStream, outputStream)
						}

					val fileList = zipFile.entries().toList()
						.filter { it.name.split(".").lastOrNull() == "json" }
					activityState.exchangeDataSize.value = fileList.size

					var currentIndex = 0

					while (currentIndex < fileList.size) {
						if (activityState.isDataSaving.value) {
							delay(100)
						} else {
							val jsonString = String(
								zipFile.getInputStream(fileList[currentIndex]).readBytes()
							)
							activityState.richTextEditor.exec("editor.importData($jsonString, \"journey\", \"${destFile.path}\");")
							activityState.isDataSaving.value = true
							activityState.currentImportFileIndex.value = currentIndex
							activityState.currentImportFileName.value = fileList[currentIndex].name
							currentIndex++
						}
					}

					activityState.dataExchange.value = SettingsActivity.DataExchange.NONE
					activityState.exchangeDataSize.value = 0
					activityState.currentImportFileIndex.value = 1
					CoroutineScope(Dispatchers.Main).launch {
						Toast.makeText(
							getApplication(),
							"${fileList.size} entries imported",
							Toast.LENGTH_LONG
						).show()
					}
				} catch (exception: Exception) {
					activityState.dataExchange.value = SettingsActivity.DataExchange.NONE
					activityState.exchangeDataSize.value = 0
					activityState.currentImportFileIndex.value = 1

					exception.printStackTrace()

					CoroutineScope(Dispatchers.Main).launch {
						Toast.makeText(
							getApplication(),
							"Sorry, can't process selected file",
							Toast.LENGTH_LONG
						).show()
					}
				}
			} else {
				activityState.dataExchange.value = SettingsActivity.DataExchange.NONE
				activityState.exchangeDataSize.value = 0
				activityState.currentImportFileIndex.value = 1
			}
		}
	}
}

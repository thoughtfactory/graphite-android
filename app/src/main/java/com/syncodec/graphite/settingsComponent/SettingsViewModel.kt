package com.syncodec.graphite.settingsComponent

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.syncodec.graphite.Graphite
import com.syncodec.graphite.database.UserDatabase
import com.syncodec.graphite.database.attachment.AttachmentDbEntry
import com.syncodec.graphite.database.attachment.AttachmentTableDao
import com.syncodec.graphite.database.bucket.BucketDbEntry
import com.syncodec.graphite.database.bucket.BucketDbTableDao
import com.syncodec.graphite.database.bucketItem.BucketItemDbEntry
import com.syncodec.graphite.database.bucketItem.BucketItemDbTableDao
import com.syncodec.graphite.database.chapter.ChapterDbEntry
import com.syncodec.graphite.database.chapter.ChapterTableDao
import com.syncodec.graphite.database.export.Metadata
import com.syncodec.graphite.database.export.NoteBackup
import com.syncodec.graphite.database.export.NoteExport
import com.syncodec.graphite.database.note.NoteDbEntry
import com.syncodec.graphite.database.note.NoteTableDao
import com.syncodec.graphite.database.notebook.NotebookDbEntry
import com.syncodec.graphite.database.notebook.NotebookTableDao
import com.syncodec.graphite.database.snapshot.Snapshot
import com.syncodec.graphite.database.tag.TagDbEntry
import com.syncodec.graphite.database.tag.TagDbTableDao
import com.syncodec.graphite.database.tag.TagKeyDbEntry
import com.syncodec.graphite.database.tag.TagKeyDbTableDao
import com.syncodec.graphite.miscellaneous.CollectionUtils.Companion.listOfField
import com.syncodec.graphite.miscellaneous.DataStoreInstance
import com.syncodec.graphite.miscellaneous.FileUtils
import com.syncodec.graphite.miscellaneous.FileUtils.Companion.copyInputStreamToOutputStream
import com.syncodec.graphite.miscellaneous.FileUtils.Companion.getFileExtension
import com.syncodec.graphite.miscellaneous.FileUtils.Companion.getFileFromUri
import com.syncodec.graphite.miscellaneous.FileUtils.Companion.readAsObject
import com.syncodec.graphite.miscellaneous.FileUtils.Companion.readAsString
import com.syncodec.graphite.miscellaneous.StringUtils.Companion.encrypt
import com.syncodec.graphite.miscellaneous.TimeUtils.Companion.timeStampToPrettyFullStop
import com.syncodec.graphite.miscellaneous.generatePrimaryKey
import com.syncodec.graphite.repository.AttachmentRepository
import com.syncodec.graphite.repository.NoteRepository
import com.syncodec.graphite.repository.TagRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.zip.ZipFile


class SettingsViewModel(application: Application) : AndroidViewModel(application) {

	val dataStoreInstance = DataStoreInstance(this.getApplication())
	private val objectMapper: ObjectMapper = jsonMapper { addModule(kotlinModule()) }

	private var notebookTableDao: NotebookTableDao =
		UserDatabase.getInstance(application).notebookTableDao
	private var chapterTableDao: ChapterTableDao =
		UserDatabase.getInstance(application).chapterTableDao
	private var noteTableDao: NoteTableDao = UserDatabase.getInstance(application).noteTableDao
	private var attachmentTableDao: AttachmentTableDao =
		UserDatabase.getInstance(application).attachmentTableDao
	private var bucketDbTableDao: BucketDbTableDao =
		UserDatabase.getInstance(application).bucketDbTableDao
	private var bucketItemDbTableDao: BucketItemDbTableDao =
		UserDatabase.getInstance(application).bucketItemDbTableDao
	private var tagDbTableDao: TagDbTableDao = UserDatabase.getInstance(application).tagDbTableDao
	private var tagKeyDbTableDao: TagKeyDbTableDao =
		UserDatabase.getInstance(application).tagKeyDbTableDao

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

	val snapshotList: SnapshotStateList<Snapshot> = mutableStateListOf()
	val currentSnapshot: MutableState<Snapshot?> = mutableStateOf(null)
	val currentSnapshotFile: MutableState<DocumentFile?> = mutableStateOf(null)
	var isSnapshotRefreshing: MutableState<Boolean> = mutableStateOf(false)

	suspend fun importNotes(notebookKey: String, data: String) {
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
					this.contentThumbnail =
						dataText.substring(0, minOf(256, dataText.length)).encrypt()
					importData.optDouble("latitude").also { lat ->
						if (!lat.isNaN()) {
							importData.optDouble("longitude").also { lon ->
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
									val file =
										cacheFile.listFiles { dir, name -> name.contains(it) }
											?.firstOrNull()
									if (file != null) {
										val uri = file.toUri()
										val mimeType =
											getApplication<Graphite>().contentResolver.getType(uri)
										val extension =
											getApplication<Graphite>().getFileExtension(uri)

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
					this.contentThumbnail =
						dataText.substring(0, minOf(256, dataText.length)).encrypt()
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
							attachmentKeyList = attachmentList.keys.toList(),
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

	fun takeSnapshot(uri: Uri) {
		val documentTree = DocumentFile.fromTreeUri(getApplication(), uri)

		if (documentTree == null) {
			Toast.makeText(
				getApplication(),
				"Error generating snapshot. Try setting up backup folder again",
				Toast.LENGTH_SHORT
			).show()
		} else {
			try {
				if (documentTree.canWrite()) {
					val snapshotDir = documentTree.createDirectory(
						System.currentTimeMillis().timeStampToPrettyFullStop()
					)

					val metadataFile = snapshotDir?.createFile("application/json", "metadata")
					val notebookDir = snapshotDir?.createDirectory("Notebooks")
					val chapterDir = snapshotDir?.createDirectory("Chapters")
					val noteDir = snapshotDir?.createDirectory("Notes")
					val attachmentDir = snapshotDir?.createDirectory("Attachments")
					val bucketDir = snapshotDir?.createDirectory("Buckets")
					val bucketItemDir = snapshotDir?.createDirectory("Bucket Items")
					val tagFile = snapshotDir?.createFile("application/json", "tags")
					val connectionFile = snapshotDir?.createFile("application/json", "connections")

					activityState.isRestoring.value = false
					activityState.isSnapshotting.value = true

					viewModelScope.launch(Dispatchers.IO) {
						DataStoreInstance(getApplication()).getDefaultNotebookKey.collectLatest {
							if (it != null) {
								val metadata = Metadata(defaultNotebookKey = it)

								if (metadataFile != null) {
									val outputStream =
										getApplication<Graphite>()
											.contentResolver
											.openOutputStream(metadataFile.uri)
									outputStream?.write(objectMapper.writeValueAsBytes(metadata))
								}

								this.cancel()
							}
						}
					}

//			        Export notebooks
					viewModelScope.launch(Dispatchers.IO) {
						val keyList = notebookTableDao.getAllKeys()
						activityState.totalNotebook.value = keyList.size
						activityState.processedNotebook.value = 0

						keyList.forEach { key ->
							try {
								val notebook = notebookTableDao.get(key)
								if (notebook != null) {
									notebookDir?.createFile("application/json", notebook.key)
										?.apply {
											val outputStream =
												getApplication<Graphite>().contentResolver.openOutputStream(
													this.uri
												)
											outputStream?.write(
												objectMapper.writeValueAsBytes(
													notebook
												)
											)
										}
								}
							} catch (exception: Exception) {
							}
							activityState.processedNotebook.value += 1
						}
					}

//			        Export chapters
					viewModelScope.launch(Dispatchers.IO) {
						val keyList = chapterTableDao.getAllKeys()
						activityState.totalChapter.value = keyList.size
						activityState.processedChapter.value = 0

						keyList.forEach { key ->
							try {
								val chapter = chapterTableDao.get(key)
								if (chapter != null) {
									chapterDir?.createFile("application/json", chapter.key)?.apply {
										val outputStream =
											getApplication<Graphite>().contentResolver.openOutputStream(
												this.uri
											)
										outputStream?.write(objectMapper.writeValueAsBytes(chapter))
									}
								}
							} catch (exception: Exception) {
							}
							activityState.processedChapter.value += 1
						}
					}

//			        Export notes
					viewModelScope.launch(Dispatchers.IO) {
						val keyList = noteTableDao.getAllKeys()
						activityState.totalNote.value = keyList.size
						activityState.processedNote.value = 0

						keyList.forEach { key ->
							try {
								val noteDbEntry = noteTableDao.get(key)
								if (noteDbEntry != null) {
									val noteContent = getApplication<Graphite>().getNote(key)

									noteDir?.createFile("application/json", noteDbEntry.key)
										?.apply {
											val outputStream =
												getApplication<Graphite>().contentResolver.openOutputStream(
													this.uri
												)
											NoteBackup(
												key = noteDbEntry.key,
												timezone = noteDbEntry.timezone,
												chapterPath = noteDbEntry.chapterPath,
												notebookKey = noteDbEntry.notebookKey,
												createdTimestamp = noteDbEntry.createdTimestamp,
												modifiedTimestamp = noteDbEntry.modifiedTimestamp,
												userTimestamp = noteDbEntry.userTimestamp,
												title = noteDbEntry.title,
												contentThumbnail = noteDbEntry.contentThumbnail,
												attachmentThumbnail = noteDbEntry.attachmentThumbnail,
												attachmentKeyList = noteDbEntry.attachmentKeyList,
												latLng = noteDbEntry.latLng,
												address = noteDbEntry.address,
												mood = noteDbEntry.mood,
												content = noteContent?.toString(),
												isFavourite = noteDbEntry.isFavourite,
												isArchived = noteDbEntry.isArchived,
												isLocked = noteDbEntry.isLocked,
												version = 1
											).apply {
												outputStream?.write(
													objectMapper.writeValueAsBytes(
														this
													)
												)
											}
										}
								}
							} catch (exception: Exception) {
								exception.printStackTrace()
							}
							activityState.processedNote.value += 1
						}
					}

//			        Export attachments
					viewModelScope.launch(Dispatchers.IO) {
						val keyList = attachmentTableDao.getAllKeys()
						activityState.totalAttachment.value = keyList.size
						activityState.processedAttachment.value = 0

						keyList.forEach { key ->
							try {
								val attachmentDbEntry = attachmentTableDao.get(key = key)
								val attachment = getApplication<Graphite>().getAttachment(key = key)
								if (attachmentDbEntry != null && attachment != null) {

									attachmentDir
										?.createFile(
											"application/json",
											"${attachmentDbEntry.key}_m"
										)
										?.apply {
											val outputStream =
												getApplication<Graphite>().contentResolver.openOutputStream(
													this.uri
												)
											outputStream?.write(
												objectMapper.writeValueAsBytes(attachmentDbEntry)
											)
										}
									attachmentDir
										?.createFile(
											attachmentDbEntry.mimeType
												?: "application/octet-stream",
											"${attachmentDbEntry.key}_f"
										)
										?.apply {
											val inputStream =
												getApplication<Graphite>().contentResolver.openInputStream(
													attachment
												)
											val outputStream =
												getApplication<Graphite>().contentResolver.openOutputStream(
													this.uri
												)
											if (inputStream != null) {
												if (outputStream != null) {
													copyInputStreamToOutputStream(
														inputStream, outputStream
													)
												}
											}
										}
								}
							} catch (exception: Exception) {
							}

							activityState.processedAttachment.value += 1
						}
					}

//			        Export buckets
					viewModelScope.launch(Dispatchers.IO) {
						val keyList = bucketDbTableDao.getAllKeys()
						activityState.totalBucket.value = keyList.size
						activityState.processedBucket.value = 0

						keyList.forEach { key ->
							try {
								val bucket = bucketDbTableDao.get(key = key)
								if (bucket != null) {
									bucketDir?.createFile("application/json", bucket.key)?.apply {
										val outputStream =
											getApplication<Graphite>().contentResolver.openOutputStream(
												this.uri
											)
										outputStream?.write(objectMapper.writeValueAsBytes(bucket))
									}
								}
							} catch (exception: Exception) {

							}

							activityState.processedBucket.value += 1
						}
					}

//			        Export bucket items
					viewModelScope.launch(Dispatchers.IO) {
						val keyList = bucketItemDbTableDao.getAllKeys()
						activityState.totalBucketItem.value = keyList.size
						activityState.processedBucketItem.value = 0

						keyList.forEach { key ->
							try {
								val bucketItem = bucketItemDbTableDao.get(key = key)
								if (bucketItem != null) {
									bucketItemDir?.createFile("application/json", bucketItem.key)
										?.apply {
											val outputStream =
												getApplication<Graphite>().contentResolver.openOutputStream(
													this.uri
												)
											outputStream?.write(
												objectMapper.writeValueAsBytes(
													bucketItem
												)
											)
										}
								}
							} catch (exception: Exception) {
								exception.printStackTrace()
							}

							activityState.processedBucketItem.value += 1
						}
					}

//			        Export tags
					viewModelScope.launch(Dispatchers.IO) {
						val keyList = tagDbTableDao.getAllKeys()
						activityState.totalTag.value = keyList.size
						activityState.processedTag.value = 0

						try {
							if (tagFile != null) {
								val outputStream =
									getApplication<Graphite>().contentResolver.openOutputStream(
										tagFile.uri
									)
								outputStream?.write(objectMapper.writeValueAsBytes(keyList))
							}

						} catch (exception: Exception) {

						}
						activityState.processedTag.value = keyList.size
					}

//			        Export tag connections
					viewModelScope.launch(Dispatchers.IO) {
						val tagKeyList = tagKeyDbTableDao.getAll()

						try {
							if (connectionFile != null) {
								val outputStream =
									getApplication<Graphite>().contentResolver.openOutputStream(
										connectionFile.uri
									)
								outputStream?.write(objectMapper.writeValueAsBytes(tagKeyList))
							}

						} catch (exception: Exception) {

						}
					}
				} else {
					CoroutineScope(Dispatchers.Main).launch {
						Toast.makeText(
							getApplication(),
							"Error creating backup file. Try setuping backup folder again (use same folder if possible).",
							Toast.LENGTH_LONG
						).show()
					}

					resetSnapshotState()
				}
			} catch (exception: SecurityException) {
				CoroutineScope(Dispatchers.Main).launch {
					Toast.makeText(
						getApplication(),
						"Error creating backup file. Try setuping backup folder again (use same folder if possible).",
						Toast.LENGTH_LONG
					).show()
				}

				resetSnapshotState()
			} catch (exception: Exception) {
				CoroutineScope(Dispatchers.Main).launch {
					Toast.makeText(
						getApplication(),
						"Error creating backup file. Try setuping backup folder again (use same folder if possible).",
						Toast.LENGTH_SHORT
					).show()
				}

				resetSnapshotState()
			}
		}
	}

	fun restoreSnapshot() {
		if (currentSnapshot.value != null) {
			viewModelScope.launch(Dispatchers.IO) {

				val metadataFile = currentSnapshot.value!!.documentFile.findFile("metadata.json")
				val notebookDir = currentSnapshot.value!!.documentFile.findFile("Notebooks")
				val chapterDir = currentSnapshot.value!!.documentFile.findFile("Chapters")
				val noteDir = currentSnapshot.value!!.documentFile.findFile("Notes")
				val attachmentDir = currentSnapshot.value!!.documentFile.findFile("Attachments")
				val bucketDir = currentSnapshot.value!!.documentFile.findFile("Buckets")
				val bucketItemDir = currentSnapshot.value!!.documentFile.findFile("Bucket Items")
				val tagFile = currentSnapshot.value!!.documentFile.findFile("tags.json")
				val connectionFile =
					currentSnapshot.value!!.documentFile.findFile("connections.json")

//				Restore metadata
				if (metadataFile != null) {
					val metadata = metadataFile.readAsObject<Metadata>(getApplication())
					DataStoreInstance(getApplication()).putDefaultNotebookKey(metadata.defaultNotebookKey)
				}

				activityState.isSnapshotting.value = false
				activityState.isRestoring.value = true

				activityState.totalNotebook.value = notebookDir?.listFiles()?.size ?: 0
				activityState.processedNotebook.value = 0
				activityState.totalChapter.value = chapterDir?.listFiles()?.size ?: 0
				activityState.processedChapter.value = 0
				activityState.totalNote.value = noteDir?.listFiles()?.size ?: 0
				activityState.processedNote.value = 0
				activityState.totalAttachment.value = (attachmentDir?.listFiles()?.size ?: 0) / 2
				activityState.processedAttachment.value = 0
				activityState.totalBucket.value = bucketDir?.listFiles()?.size ?: 0
				activityState.processedBucket.value = 0
				activityState.totalBucketItem.value = bucketItemDir?.listFiles()?.size ?: 0
				activityState.processedBucketItem.value = 0
				activityState.totalTag.value = 0
				activityState.processedTag.value = 0

//				Restore Notes
				noteTableDao.deleteAll()
				getApplication<Graphite>().deleteAllNote()
				noteDir
					?.listFiles()
					?.forEach { documentFile ->
						try {
							val noteBackup = documentFile.readAsObject<NoteBackup>(getApplication())

							NoteDbEntry(
								key = noteBackup.key,
								timezone = noteBackup.timezone,
								chapterPath = noteBackup.chapterPath.toMutableList(),
								notebookKey = noteBackup.notebookKey
							).apply {
								this.createdTimestamp = noteBackup.createdTimestamp
								this.modifiedTimestamp = noteBackup.modifiedTimestamp
								this.userTimestamp = noteBackup.userTimestamp
								this.title = noteBackup.title
								this.contentThumbnail = noteBackup.contentThumbnail
								this.attachmentThumbnail = noteBackup.attachmentThumbnail
								this.attachmentKeyList =
									noteBackup.attachmentKeyList.toMutableList()
								this.latLng = noteBackup.latLng
								this.address = noteBackup.address
								this.mood = noteBackup.mood
								this.isFavourite = noteBackup.isFavourite
								this.isArchived = noteBackup.isArchived
								this.isLocked = noteBackup.isLocked
								this.deletedTimestamp = -1


								noteTableDao.insert(this)
								getApplication<Graphite>().putNote(
									noteBackup.key,
									noteBackup.content?.let { JSONObject(it) }
								)
							}

						} catch (exception: Exception) {
							exception.printStackTrace()
						}

						activityState.processedNote.value += 1
					}

//			    Restore Chapters
				chapterTableDao.deleteAll()
				notebookTableDao.deleteAll()

//				Restore Notebooks
				notebookDir
					?.listFiles()
					?.forEach { documentFile ->
						try {
							val inputStream =
								getApplication<Graphite>().contentResolver.openInputStream(
									documentFile.uri
								)

							val r = BufferedReader(InputStreamReader(inputStream))
							val data: StringBuilder = StringBuilder()
							var line: String?
							while (r.readLine().also { line = it } != null) {
								data.append(line).append('\n')
							}

							val notebookDbEntry =
								objectMapper.readValue<NotebookDbEntry>(data.toString())
							notebookTableDao.insert(notebookDbEntry)
						} catch (exception: Exception) {

						}

						activityState.processedNotebook.value += 1
					}

				chapterDir
					?.listFiles()
					?.forEach { documentFile ->
						try {
							val chapterDbEntry =
								documentFile.readAsObject<ChapterDbEntry>(getApplication())
							chapterTableDao.insert(chapterDbEntry)
						} catch (exception: Exception) {
							exception.printStackTrace()
						}

						activityState.processedChapter.value += 1
					}

//				Restore Attachments
				attachmentTableDao.deleteAll()
				getApplication<Graphite>().deleteAllAttachment()
				viewModelScope.launch(Dispatchers.IO) {
					val attachmentFiles = attachmentDir?.listFiles()
					val metadataFiles =
						attachmentFiles?.filter { it?.name?.contains("_m") ?: false }
					val contentFiles = attachmentFiles?.filter { it?.name?.contains("_f") ?: false }

					metadataFiles?.forEach { documentFile ->
						try {
							if (documentFile.name != null &&
								attachmentFiles.filter {
									it.name?.substring(0, 36) == documentFile.name?.substring(0, 36)
								}.size == 2
							) {

								val data =
									documentFile.readAsObject<AttachmentDbEntry>(getApplication())
								attachmentTableDao.insert(data)
							}
						} catch (exception: Exception) {
							exception.printStackTrace()
						}
					}
					contentFiles?.forEach { documentFile ->
						try {
							if (documentFile.name != null) {
								val key = documentFile.name!!.substring(0, 36)
								val extension = documentFile.name!!.substring(39)
								getApplication<Graphite>().putAttachment(
									key = key,
									uri = documentFile.uri,
									extension = extension
								)
							}
						} catch (exception: Exception) {

						}

						activityState.processedAttachment.value += 1
					}
				}

//				Restore Buckets
				bucketDbTableDao.deleteAll()
				viewModelScope.launch(Dispatchers.IO) {
					bucketDir
						?.listFiles()
						?.forEach { documentFile ->
							try {
								val data = documentFile.readAsString(getApplication())

								val bucketDbEntry = objectMapper.readValue<BucketDbEntry>(data)
								bucketDbTableDao.insert(bucketDbEntry)
							} catch (exception: Exception) {

							}

							activityState.processedBucket.value += 1
						}
				}

				bucketItemDbTableDao.deleteAll()
//				Restore Bucket Items
				viewModelScope.launch(Dispatchers.IO) {
					bucketItemDir
						?.listFiles()
						?.forEach { documentFile ->
							try {
								val data =
									documentFile.readAsObject<BucketItemDbEntry>(getApplication())
								bucketItemDbTableDao.insert(data)
							} catch (exception: Exception) {

							}

							activityState.processedBucketItem.value += 1
						}
				}

//				Restore Tags
				tagDbTableDao.deleteAll()
				viewModelScope.launch(Dispatchers.IO) {
					try {
						tagFile?.let {
							val tagList = it.readAsObject<List<String>>(getApplication())
							tagList.forEach { tagDbTableDao.insert(TagDbEntry(it)) }
						}
					} catch (exception: Exception) {
						exception.printStackTrace()
					}
				}

//				Restore Connections
				tagKeyDbTableDao.deleteAll()
				viewModelScope.launch(Dispatchers.IO) {
					try {
						connectionFile?.let {
							val tagConnectionList = objectMapper.readValue<List<TagKeyDbEntry>>(
								it.readAsString(getApplication())
							)
							tagConnectionList.forEach {
								tagKeyDbTableDao.insert(it)
							}
						}
					} catch (exception: Exception) {
						exception.printStackTrace()
					}
				}
			}
		} else {
			resetSnapshotState()
		}
	}

	fun refreshSnapshot(uri: Uri) {
		viewModelScope.launch(Dispatchers.IO) {
			if (!isSnapshotRefreshing.value) {
				isSnapshotRefreshing.value = true
				CoroutineScope(Dispatchers.Main).launch { snapshotList.clear() }

				val documentTree = DocumentFile.fromTreeUri(getApplication(), uri)

				documentTree?.listFiles()?.forEach { documentFile ->
					try {
						val notebookDir = documentFile.findFile("Notebooks")
						val chapterDir = documentFile.findFile("Chapters")
						val noteDir = documentFile.findFile("Notes")
						val attachmentDir = documentFile.findFile("Attachments")
						val bucketDir = documentFile.findFile("Buckets")
						val bucketItemDir = documentFile.findFile("Bucket Items")
						val tagFile = documentTree.findFile("tags.json")
						val connectionFile = documentTree.findFile("connections.json")

						Snapshot(
							title = documentFile.name ?: "Untitled",
							notebookCount = notebookDir?.listFiles()?.size ?: 0,
							chapterCount = chapterDir?.listFiles()?.size ?: 0,
							noteCount = noteDir?.listFiles()?.size ?: 0,
							attachmentCount = attachmentDir?.listFiles()?.size ?: 0,
							bucketCount = bucketDir?.listFiles()?.size ?: 0,
							bucketItemCount = bucketItemDir?.listFiles()?.size ?: 0,
							tagCount = -1,
							connectionCount = -1,
							documentFile = documentFile
						).apply {
							CoroutineScope(Dispatchers.Main).launch {
								snapshotList.add(this@apply)
								snapshotList.sortBy { it.documentFile.lastModified() }
								snapshotList.reverse()
							}
						}
					} catch (exception: Exception) {

					}
				}
				isSnapshotRefreshing.value = false
			}
		}
	}

	private fun resetSnapshotState() {
		activityState.totalNotebook.value = 0
		activityState.processedNotebook.value = 0
		activityState.totalChapter.value = 0
		activityState.processedChapter.value = 0
		activityState.totalNote.value = 0
		activityState.processedNote.value = 0
		activityState.totalAttachment.value = 0
		activityState.processedAttachment.value = 0
		activityState.totalBucket.value = 0
		activityState.processedBucket.value = 0
		activityState.totalBucketItem.value = 0
		activityState.processedBucketItem.value = 0
		activityState.totalTag.value = 0
		activityState.processedTag.value = 0

		activityState.isSnapshotting.value = false
		activityState.isRestoring.value = false
	}
}

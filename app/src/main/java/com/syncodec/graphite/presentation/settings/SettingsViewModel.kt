package com.syncodec.graphite.presentation.settings

import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.syncodec.graphite.di.model.BucketItemSnapshot
import com.syncodec.graphite.di.model.BucketSnapshot
import com.syncodec.graphite.di.model.ChapterSnapshot
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.NoteSnapshot
import com.syncodec.graphite.di.model.TagSnapshot
import com.syncodec.graphite.di.model.importer.JourneyNote
import com.syncodec.graphite.di.repository.RealmUUIDDeserializer
import com.syncodec.graphite.di.repository.Repository2
import com.syncodec.graphite.presentation.common.richText.RichTextEditor
import com.syncodec.graphite.utils.copyInDirectory
import com.syncodec.graphite.utils.extractZipFile
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipFile
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository2 : Repository2) : ViewModel() {

	private val objectMapper = jsonMapper {
		addModule(
			kotlinModule().addDeserializer(
				RealmUUID::class.java,
				RealmUUIDDeserializer()
			)
		)
	}.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	val attachmentCount = mutableStateOf(0)
	val attachmentProcessed = mutableStateOf(0)
	val bucketItemCount = mutableStateOf(0)
	val bucketItemProcessed = mutableStateOf(0)
	val bucketCount = mutableStateOf(0)
	val bucketProcessed = mutableStateOf(0)
	val chapterCount = mutableStateOf(0)
	val chapterProcessed = mutableStateOf(0)
	val noteCount = mutableStateOf(0)
	val noteProcessed = mutableStateOf(0)
	val tagCount = mutableStateOf(0)
	val tagProcessed = mutableStateOf(0)
	val packageCount = mutableStateOf(0)
	val packageProcessed = mutableStateOf(0)

	val importDataCount = mutableStateOf(0)
	val importDataProcessed = mutableStateOf(0)

	val snapshotList : SnapshotStateList<DocumentFile> = mutableStateListOf()

	var defaultChapterId: RealmUUID? = null

	init {
		val documentUri = repository2.context.contentResolver.persistedUriPermissions.firstOrNull()?.uri
		if (documentUri != null) getSnapshot(documentUri)

		viewModelScope.launch(Dispatchers.Default) {
			repository2.getDefaultChapterId().collect {
				defaultChapterId = it
			}
		}
	}

	fun takeSnapshot(uri : Uri, callback : (Boolean) -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			val documentTree = DocumentFile.fromTreeUri(repository2.context, uri)

			when {
				documentTree == null -> viewModelScope.launch(Dispatchers.Main) {
					Toast.makeText(repository2.context, "Error generating snapshot. Try setting backup folder again.", Toast.LENGTH_SHORT).show()
					callback(false)
				}

				documentTree.canWrite() -> {
					generateSnapshot { isSuccess, file ->
						if (isSuccess && file != null) {
							documentTree.createFile("application/x-7z-compressed", file.name)?.let { documentFile ->
								val fileInputStream = file.inputStream()
								val outputStream = repository2.context.contentResolver.openOutputStream(documentFile.uri)
								fileInputStream.copyTo(outputStream !!)
								fileInputStream.close()
								outputStream.close()
							}
							callback(true)
						} else {
							viewModelScope.launch(Dispatchers.Main) {
								Toast.makeText(repository2.context, "Error generating snapshot. Try setting backup folder again.", Toast.LENGTH_SHORT).show()
								callback(false)
							}
						}
					}
				}

				else -> viewModelScope.launch(Dispatchers.Main) {
					Toast.makeText(repository2.context, "Error generating snapshot. Try setting backup folder again.", Toast.LENGTH_SHORT).show()
					callback(false)
				}
			}
		}
	}

	fun generateSnapshot(callback : (Boolean, File?) -> Unit) {
		try {
			val snapshotFolder = File(repository2.context.cacheDir, "snapshot").apply { mkdirs() }
			val currentSnapshotFolder = File(snapshotFolder, "snapshot_${System.currentTimeMillis()}")
			currentSnapshotFolder.mkdirs()

			val baseFile = File(currentSnapshotFolder.path, "base.json")
			val attachmentFolder = File(currentSnapshotFolder, "attachment").apply { mkdirs() }
			val bucketItemFolder = File(currentSnapshotFolder, "bucketItem").apply { mkdirs() }
			val bucketFolder = File(currentSnapshotFolder, "bucket").apply { mkdirs() }
			val chapterFolder = File(currentSnapshotFolder, "chapter").apply { mkdirs() }
			val noteFolder = File(currentSnapshotFolder, "note").apply { mkdirs() }
			val tagFolder = File(currentSnapshotFolder, "tag").apply { mkdirs() }

//			** Base
			repository2
				.getBaseObject()
				?.let {
					val jsonObject = JSONObject()
					jsonObject.put("version", "1")
					jsonObject.put("default_chapter_id", it.defaultChapterId.toString())
					baseFile.writeText(jsonObject.toString())
				}


//			** Attachment
			File(repository2.attachmentDirPath).let { attachmentDir ->
				copyInDirectory(attachmentDir, attachmentFolder)
			}


//			** Bucket
			repository2.getAllBucket()
				.let { bucketList ->
					viewModelScope.launch(Dispatchers.Main) {
						bucketCount.value = bucketList.size
						bucketProcessed.value = 0
					}
					bucketList.forEachIndexed { index, bucketObject ->
						val snapshotString = objectMapper.writeValueAsString(bucketObject.toSnapshot())
						val snapshotFile = File(bucketFolder.path, "${bucketObject.id}.json")
						snapshotFile.writeText(snapshotString)
						viewModelScope.launch(Dispatchers.Main) { bucketProcessed.value = index + 1 }
					}
				}

//			** BucketItem
			repository2.getAllBucketItem()
				.let { bucketItemList ->
					viewModelScope.launch(Dispatchers.Main) {
						bucketItemCount.value = bucketItemList.size
						bucketItemProcessed.value = 0
					}
					bucketItemList.forEachIndexed { index, bucketItemObject ->
						val snapshotString = objectMapper.writeValueAsString(bucketItemObject.toSnapshot())
						val snapshotFile = File(bucketItemFolder.path, "${bucketItemObject.id}.json")
						snapshotFile.writeText(snapshotString)
						viewModelScope.launch(Dispatchers.Main) { bucketItemProcessed.value = index + 1 }
					}
				}

//			** Chapter
			repository2.getAllChapter()
				.let { chapterList ->
					viewModelScope.launch(Dispatchers.Main) {
						chapterCount.value = chapterList.size
						chapterProcessed.value = 0
					}
					chapterList.forEachIndexed { index, chapterObject ->
						val snapshotString = objectMapper.writeValueAsString(chapterObject.toSnapshot())
						val snapshotFile = File(chapterFolder.path, "${chapterObject.id}.json")
						snapshotFile.writeText(snapshotString)
						viewModelScope.launch(Dispatchers.Main) { chapterProcessed.value = index + 1 }
					}
				}

//			** Note
			repository2.getAllNote()
				.let { noteList ->
					viewModelScope.launch(Dispatchers.Main) {
						noteCount.value = noteList.size
						noteProcessed.value = 0
					}
					noteList.forEachIndexed { index, note ->
						val snapshotString = objectMapper.writeValueAsString(note.toSnapshot())
						val snapshotFile = File(noteFolder.path, "${note.id}.json")
						snapshotFile.writeText(snapshotString)
						viewModelScope.launch(Dispatchers.Main) { noteProcessed.value = index + 1 }
					}
				}

//			** Tag
			repository2.getAllTag()
				.let { tagList ->
					viewModelScope.launch(Dispatchers.Main) {
						tagCount.value = tagList.size
						tagProcessed.value = 0
					}
					tagList.forEachIndexed { index, tag ->
						val snapshotString = objectMapper.writeValueAsString(tag.toSnapshot())
						val snapshotFile = File(tagFolder.path, "${tag.id}.json")
						snapshotFile.writeText(snapshotString)
						viewModelScope.launch(Dispatchers.Main) { tagProcessed.value = index + 1 }
					}
				}

			val sevenZOutput = SevenZOutputFile(File(snapshotFolder, "${currentSnapshotFolder.name}.7z"))
			compressFile(currentSnapshotFolder, sevenZOutput)

			File(snapshotFolder, "${currentSnapshotFolder.name}.7z").apply {
				callback(true, this)
			}

			viewModelScope.launch(Dispatchers.Main) {
				noteCount.value = 0
				noteProcessed.value = 0
			}
		} catch (e : Exception) {
			e.printStackTrace()
			callback(false, null)
		}
	}

	fun getSnapshot(uri : Uri) {
		viewModelScope.launch(Dispatchers.IO) {
			val documentTree = DocumentFile.fromTreeUri(repository2.context, uri)
			withContext(Dispatchers.Main) { snapshotList.clear() }
			if (documentTree == null) {

			} else {
				documentTree.listFiles().forEach {
					it.name?.let { name ->
						if (name.endsWith(".7z")) withContext(Dispatchers.Main) { snapshotList.add(it) }
					}
				}
			}
		}
	}

	fun restoreSnapshot(inputStream : InputStream, clearAll : Boolean, callback : (Boolean) -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {

			val tmp7zFile = File(File(repository2.context.cacheDir, "restore"), "graphite.7z")
			tmp7zFile.parentFile?.mkdirs()
			tmp7zFile.delete()
			tmp7zFile.createNewFile()

			inputStream.copyTo(tmp7zFile.outputStream())

			val restoreFolder = File(File(repository2.context.cacheDir, "restore"), "graphite")
			restoreFolder.deleteRecursively()
			restoreFolder.mkdirs()

			try {
				if (clearAll) {
					repository2.clearRealm { isCleared, e ->
						if (! isCleared) {
							Toast.makeText(repository2.context, "Failed to clear database", Toast.LENGTH_SHORT).show()
							callback(false)
						}
					}
				}
			} catch (e : Exception) {
				e.printStackTrace()
				callback(false)
				return@launch
			}

			try {
				val sevenZFile = SevenZFile(tmp7zFile)

				sevenZFile.entries.forEach {
					val entry = it
					val entryName = entry.name
					val entryFile = File(restoreFolder, entryName)
					entryFile.parentFile?.mkdirs()
					entryFile.delete()
					entryFile.createNewFile()
					val entryInputStream = sevenZFile.getInputStream(entry)
					entryInputStream.copyTo(entryFile.outputStream())
					entryInputStream.close()
				}

			} catch (e : Exception) {
				e.printStackTrace()
				callback(false)
				return@launch
			}

			val baseFile = File(restoreFolder, "base.json")
			val attachmentFolder = File(restoreFolder, "attachment")
			val bucketItemFile = File(restoreFolder, "bucketItem").listFiles()
			val bucketFileList = File(restoreFolder, "bucket").listFiles()
			val chapterFileList = File(restoreFolder, "chapter").listFiles()
			val noteFileList = File(restoreFolder, "note").listFiles()
			val tagFolder = File(restoreFolder, "tag").listFiles()

//			** Base
			try {
				val baseJsonObject = JSONObject(baseFile.readText())
				val defaultChapterIdString = baseJsonObject.optString("default_chapter_id")
				if (defaultChapterIdString.isNotBlank()) {
					val defaultChapterId = defaultChapterIdString.let { RealmUUID.from(it) }
					repository2.putDefaultChapterId(defaultChapterId) {}
				}
			} catch (e : Exception) {
				e.printStackTrace()
			}

//			** Attachment
			try {
				copyInDirectory(attachmentFolder, File(repository2.attachmentDirPath))
			} catch (e : Exception) {
				e.printStackTrace()
			}

//			** Bucket
			viewModelScope.launch(Dispatchers.Main) {
				bucketCount.value = bucketFileList?.size ?: 0
				bucketProcessed.value = 0
			}
			bucketFileList?.forEach {
				try {
					val bucketSnapshot = objectMapper.readValue(it.readBytes(), BucketSnapshot::class.java)
					bucketSnapshot.toObject().let { bucketObject ->
						repository2.putBucket(bucketObject) { _, e -> }
					}
				} catch (e : Exception) {
					e.printStackTrace()
				}
				viewModelScope.launch(Dispatchers.Main) { bucketProcessed.value = bucketProcessed.value + 1 }
				delay(100)
			}

//			** BucketItem
			viewModelScope.launch(Dispatchers.Main) {
				bucketItemCount.value = bucketItemFile?.size ?: 0
				bucketItemProcessed.value = 0
			}
			bucketItemFile?.forEach {
				try {
					val bucketItemSnapshot = objectMapper.readValue(it.readBytes(), BucketItemSnapshot::class.java)
					bucketItemSnapshot.toObject().let { bucketItemObject ->
						bucketItemObject.parentId?.let { repository2.putBucketItem(it, bucketItemObject) { _, e -> } }
					}
				} catch (e : Exception) {
					e.printStackTrace()
				}
				viewModelScope.launch(Dispatchers.Main) { bucketItemProcessed.value = bucketItemProcessed.value + 1 }
				delay(100)
			}

//			** Chapter
			viewModelScope.launch(Dispatchers.Main) {
				chapterCount.value = chapterFileList?.size ?: 0
				chapterProcessed.value = 0
			}
			chapterFileList?.forEach {
				try {
					val chapterSnapshot = objectMapper.readValue(it.readBytes(), ChapterSnapshot::class.java)
					chapterSnapshot.toObject().let { chapterObject ->
						repository2.putChapter(chapterObject.parentId, chapterObject) { _, e -> }
					}
				} catch (e : Exception) {
					e.printStackTrace()
				}
				viewModelScope.launch(Dispatchers.Main) { chapterProcessed.value = chapterProcessed.value + 1 }
				delay(100)
			}

//			** Note
			viewModelScope.launch(Dispatchers.Main) {
				noteCount.value = noteFileList?.size ?: 0
				noteProcessed.value = 0
			}
			noteFileList?.forEach {
				try {
					val noteSnapshot = objectMapper.readValue(it.readBytes(), NoteSnapshot::class.java)
					noteSnapshot.toObject().let { noteObject ->
						repository2.putNote(noteObject) { _, e -> }
					}
				} catch (e : Exception) {
					e.printStackTrace()
				}
				viewModelScope.launch(Dispatchers.Main) { noteProcessed.value = noteProcessed.value + 1 }
				delay(100)
			}

//			** Tag
			viewModelScope.launch(Dispatchers.Main) {
				tagCount.value = tagFolder?.size ?: 0
				tagProcessed.value = 0
			}
			tagFolder?.forEach {
				try {
					val tagSnapshot = objectMapper.readValue(it.readBytes(), TagSnapshot::class.java)
					tagSnapshot.toObject().let { tagObject ->
						repository2.putTag(tagObject) { _, e -> }
					}
				} catch (e : Exception) {
					e.printStackTrace()
				}
				viewModelScope.launch(Dispatchers.Main) { tagProcessed.value = tagProcessed.value + 1 }
				delay(100)
			}

			callback(true)
		}
	}

	var lock = false

	fun importFromJourney(inputStream : InputStream, richTextEditor : RichTextEditor, callback : (Boolean) -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			val importDir = File(repository2.context.cacheDir, "import").apply {
				if (exists()) deleteRecursively()
				mkdirs()
			}
			val journey7z = File(importDir, "journey.7z").apply {
				if (exists()) delete()
				createNewFile()
			}

			inputStream.copyTo(journey7z.outputStream())

			val journeyFolder = File(File(repository2.context.cacheDir, "import"), "journey")
			journeyFolder.deleteRecursively()
			journeyFolder.mkdirs()

			try {
				val zipFile = ZipFile(journey7z)

				extractZipFile(zipFile, journeyFolder).let {
					if (true) {
						val files = journeyFolder.listFiles()
						viewModelScope.launch(Dispatchers.Main) {
							importDataCount.value = files?.size ?: 0
							importDataProcessed.value = 0
						}

						val attachmentDir = File(repository2.attachmentDirPath)
						val fileIterator = files?.iterator()
						viewModelScope.launch(Dispatchers.Default) {
							while (true) {
								if (lock) {
									delay(100)
								} else {
									if (fileIterator?.hasNext() == true) {
										lock = true
										try {
											val journeyFile = fileIterator?.next()
											if (journeyFile?.extension == "json") {
												val journeyJson = journeyFile?.readText()
												val journeyNote = objectMapper.readValue(journeyJson, JourneyNote::class.java)
												val noteId = RealmUUID.random()
												journeyNote.photos?.forEach {
													val photoFile = it?.let { it1 -> File(journeyFolder, it1) }
													val noteAttachmentDir = File(attachmentDir, noteId.toString()).apply { mkdirs() }
													val attachmentFile = File(noteAttachmentDir, photoFile?.name ?: RealmUUID.random().toString()).apply { createNewFile() }
													photoFile?.inputStream()?.copyTo(attachmentFile.outputStream())
												}
												richTextEditor.exec("editor.importData(\"$noteId\", $journeyJson, \"journey\");")
											} else {
												lock = false
											}
										} catch (e : Exception) {
											lock = false
											e.printStackTrace()
										}
										viewModelScope.launch(Dispatchers.Main) {
											importDataProcessed.value = importDataProcessed.value + 1
										}
									} else {
										break
									}
								}
							}
							callback(true)
						}
					} else {
						callback(false)
					}
				}
			} catch (e : Exception) {
				e.printStackTrace()
				callback(false)
				return@launch
			}
		}
	}

	fun putNote(noteObject : NoteObject) {
		viewModelScope.launch(Dispatchers.Default) {
			repository2.putNote(noteObject) { _, e -> lock = false }
		}
	}

	private fun compressFile(fileToCompress : File, outputFile : SevenZOutputFile) {
		outputFile.use { sevenZOutput ->
			val archivePackage = fileToCompress.walk()
			viewModelScope.launch(Dispatchers.Main) {
				packageCount.value = archivePackage.count()
				packageProcessed.value = 0
			}
			archivePackage.forEachIndexed { index, file ->
				if (file.isFile) {
					try {
						val entry : SevenZArchiveEntry = sevenZOutput.createArchiveEntry(file, file.path.replace(fileToCompress.path, ""))
						sevenZOutput.putArchiveEntry(entry)
						sevenZOutput.write(file.readBytes())
						sevenZOutput.closeArchiveEntry()
					} catch (e : IOException) {
					}
					viewModelScope.launch(Dispatchers.Main) { packageProcessed.value = index + 1 }
				}
			}
			sevenZOutput.finish()
		}
	}

	fun deleteAccount() {
	}
}

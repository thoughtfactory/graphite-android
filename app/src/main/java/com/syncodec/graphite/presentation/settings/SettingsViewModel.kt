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
import com.syncodec.graphite.di.model.ChapterSnapshot
import com.syncodec.graphite.di.model.NoteSnapshot
import com.syncodec.graphite.di.repository.Repository2
import com.syncodec.graphite.utils.copyInputStreamToOutputStream
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository2 : Repository2) : ViewModel() {

	private val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

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

	val snapshotList : SnapshotStateList<DocumentFile> = mutableStateListOf()

	init {
		val documentUri = repository2.context.contentResolver.persistedUriPermissions.firstOrNull()?.uri
		if (documentUri != null) getSnapshot(documentUri)
	}

	fun takeSnapshot(uri : Uri, onResult : (Boolean) -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			val documentTree = DocumentFile.fromTreeUri(repository2.context, uri)

			when {
				documentTree == null -> viewModelScope.launch(Dispatchers.Main) {
					Toast.makeText(repository2.context, "Error generating snapshot. Try setting backup folder again.", Toast.LENGTH_SHORT).show()
					onResult(false)
				}

				documentTree.canWrite() -> takeSnapshot(documentTree = documentTree, onResult = onResult)

				else -> viewModelScope.launch(Dispatchers.Main) {
					Toast.makeText(repository2.context, "Error generating snapshot. Try setting backup folder again.", Toast.LENGTH_SHORT).show()
					onResult(false)
				}
			}
		}
	}

	private fun takeSnapshot(documentTree : DocumentFile, onResult : (Boolean) -> Unit) {
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

			repository2
				.getBaseObject()
				?.let {
					val jsonObject = JSONObject()
					jsonObject.put("version", "1")
					jsonObject.put("default_chapter_id", it.defaultChapterId.toString())
					baseFile.writeText(jsonObject.toString())
				}

			repository2.getAllAttachmentWithFile()
				.let { attachmentList ->
					viewModelScope.launch(Dispatchers.Main) {
						attachmentCount.value = attachmentList.size
						attachmentProcessed.value = 0
					}
					attachmentList.forEachIndexed { index, (attachmentObject, file, uri) ->
						val snapshot = attachmentObject.toSnapshot().apply { data = file?.readBytes() }
						val snapshotString = objectMapper.writeValueAsString(snapshot)
						val snapshotFile = File(attachmentFolder.path, "${snapshot.id}.json")
						snapshotFile.writeText(snapshotString)
						viewModelScope.launch(Dispatchers.Main) { attachmentProcessed.value = index + 1 }
					}
				}

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

			documentTree.createFile("application/x-7z-compressed", "${currentSnapshotFolder.name}.7z")?.let { documentFile ->
				val fileInputStream = FileInputStream(File(snapshotFolder, "${currentSnapshotFolder.name}.7z"))
				val outputStream = repository2.context.contentResolver.openOutputStream(documentFile.uri)
				fileInputStream.copyTo(outputStream !!)
				fileInputStream.close()
				outputStream.close()
			}

			onResult(true)
			viewModelScope.launch(Dispatchers.Main) {
				noteCount.value = 0
				noteProcessed.value = 0
			}
		} catch (e : Exception) {
			e.printStackTrace()
			onResult(false)
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

	fun restoreSnapshot(documentFile : DocumentFile) {
		viewModelScope.launch(Dispatchers.IO) {

			val snapshotFolder = File(repository2.context.cacheDir, "snapshot").apply { mkdirs() }
			val restoreSnapshotFolder = File(File(snapshotFolder, "restore"), documentFile.name?.dropLast(3)).apply { mkdirs() }

			val restoreSnapshotFile = File(restoreSnapshotFolder, documentFile.name !!)

			val inputStream = repository2.context.contentResolver.openInputStream(documentFile.uri)
			val outputStream = FileOutputStream(restoreSnapshotFile)

			if (inputStream != null) {
				copyInputStreamToOutputStream(inputStream, outputStream)

				try {
					repository2.clearRealm { isCleared, e ->
						if (isCleared) {
							SevenZFile(restoreSnapshotFile).use { sevenZFile ->
								var entry : SevenZArchiveEntry
								try {
									while (sevenZFile.nextEntry.also { entry = it } != null) {
										val file = File(restoreSnapshotFolder, entry.name).apply { parentFile?.mkdirs(); createNewFile() }
										val content = ByteArray(entry.size.toInt())
										sevenZFile.read(content)
										file.writeBytes(content)
									}
								} catch (e : Exception) {
								}

								val baseFile = File(restoreSnapshotFolder, "base.json")
								val attachmentFolderIterator = File(restoreSnapshotFolder, "attachment").listFiles()?.iterator()
								val bucketItemFolderIterator = File(restoreSnapshotFolder, "bucketItem").listFiles()?.iterator()
								val bucketFileListIterator = File(restoreSnapshotFolder, "bucket").listFiles()?.iterator()
								val chapterFileListIterator = File(restoreSnapshotFolder, "chapter").listFiles()?.iterator()
								val noteFileListIterator = File(restoreSnapshotFolder, "note").listFiles()?.iterator()
								val tagFolderIterator = File(restoreSnapshotFolder, "tag").listFiles()?.iterator()

								var lock = false

								try {
									val baseJsonObject = JSONObject(baseFile.readText())
									val defaultChapterIdString = baseJsonObject.optString("default_chapter_id")
									if (defaultChapterIdString.isNotBlank()) {
										val defaultChapterId = defaultChapterIdString.let { RealmUUID.from(it) }
										repository2.putDefaultChapterId(defaultChapterId) {}
									}
								} catch (e : Exception) {
								}

								while (true) {
									if(!lock) {
										if (chapterFileListIterator != null) {
											if (chapterFileListIterator.hasNext()) {
												try {
													lock = true
													val chapterSnapshot = objectMapper.readValue(chapterFileListIterator.next().readBytes(), ChapterSnapshot::class.java)
													val chapterObject = chapterSnapshot.toObject()
													repository2.putChapter(chapterObject.parentChapterId, chapterObject) { _,  e ->
														lock = false
													}
												} catch (e : Exception) {
													e.printStackTrace()
													lock = false
												}
											} else {
												break
											}
										} else {
											break
										}
									}
								}
								while (true) {
									if(!lock) {
										if (noteFileListIterator != null) {
											if (noteFileListIterator.hasNext()) {
												lock = true
												val noteSnapshot = objectMapper.readValue(noteFileListIterator.next().readText(), NoteSnapshot::class.java)
												repository2.putNote(noteSnapshot.toObject()) { _,  _ -> lock = false }
											} else {
												break
											}
										} else {
											break
										}
									}
								}
							}
						} else {

						}
					}
				} catch (e : IOException) {
					e.printStackTrace()
				} catch (e : Exception) {
					e.printStackTrace()
				}
			}

			inputStream?.close()
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

	private fun decompressFile(fileToDecompress : File) {

	}
}

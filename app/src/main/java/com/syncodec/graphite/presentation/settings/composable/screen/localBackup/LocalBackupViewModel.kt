package com.syncodec.graphite.presentation.settings.composable.screen.localBackup

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.repository.KoinRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class LocalBackupViewModel(private val repository : KoinRepository) : ViewModel() {

	fun getRealmSnapshot(name:String, path: String) {
		repository.getRealmSnapshot(name, path)
	}

//	fun takeSnapshot(uri : Uri, callback : (Boolean) -> Unit) {
//		viewModelScope.launch(Dispatchers.IO) {
//			val documentTree = DocumentFile.fromTreeUri(repository2.context, uri)
//
//			when {
//				documentTree == null -> viewModelScope.launch(Dispatchers.Main) {
//					Toast.makeText(repository2.context, "Error generating snapshot. Try setting backup folder again.", Toast.LENGTH_SHORT).show()
//					callback(false)
//				}
//
//				documentTree.canWrite() -> {
//					generateSnapshot { isSuccess, file ->
//						if (isSuccess && file != null) {
//							documentTree.createFile("application/x-7z-compressed", file.name)?.let { documentFile ->
//								val fileInputStream = file.inputStream()
//								val outputStream = repository2.context.contentResolver.openOutputStream(documentFile.uri)
//								fileInputStream.copyTo(outputStream !!)
//								fileInputStream.close()
//								outputStream.close()
//							}
//							callback(true)
//						} else {
//							viewModelScope.launch(Dispatchers.Main) {
//								Toast.makeText(repository2.context, "Error generating snapshot. Try setting backup folder again.", Toast.LENGTH_SHORT).show()
//								callback(false)
//							}
//						}
//					}
//				}
//
//				else -> viewModelScope.launch(Dispatchers.Main) {
//					Toast.makeText(repository2.context, "Error generating snapshot. Try setting backup folder again.", Toast.LENGTH_SHORT).show()
//					callback(false)
//				}
//			}
//		}
//	}

//	fun generateSnapshot(callback : (Boolean, File?) -> Unit) {
//		try {
//			val snapshotFolder = File(repository2.context.cacheDir, "snapshot").apply { mkdirs() }
//			val currentSnapshotFolder = File(snapshotFolder, "snapshot_${System.currentTimeMillis()}")
//			currentSnapshotFolder.mkdirs()
//
//			val baseFile = File(currentSnapshotFolder.path, "base.json")
//			val attachmentFolder = File(currentSnapshotFolder, "attachment").apply { mkdirs() }
//			val bucketItemFolder = File(currentSnapshotFolder, "bucketItem").apply { mkdirs() }
//			val bucketFolder = File(currentSnapshotFolder, "bucket").apply { mkdirs() }
//			val chapterFolder = File(currentSnapshotFolder, "chapter").apply { mkdirs() }
//			val noteFolder = File(currentSnapshotFolder, "note").apply { mkdirs() }
//			val tagFolder = File(currentSnapshotFolder, "tag").apply { mkdirs() }
//
////			** Base
//			repository
//				.getBaseObject()
//				?.let {
//					val jsonObject = JSONObject()
//					jsonObject.put("version", "1")
//					jsonObject.put("default_chapter_id", it.defaultChapterId.toString())
//					baseFile.writeText(jsonObject.toString())
//				}
//
//
////			** Attachment
//			File(repository2.attachmentDirPath).let { attachmentDir ->
//				copyInDirectory(attachmentDir, attachmentFolder)
//			}
//
//
////			** Bucket
//			repository.getAllBucket()
//				.let { bucketList ->
//					viewModelScope.launch(Dispatchers.Main) {
//						bucketCount.value = bucketList.size
//						bucketProcessed.value = 0
//					}
//					bucketList.forEachIndexed { index, bucketObject ->
//						val snapshotString = objectMapper.writeValueAsString(bucketObject.toSnapshot())
//						val snapshotFile = File(bucketFolder.path, "${bucketObject.id}.json")
//						snapshotFile.writeText(snapshotString)
//						viewModelScope.launch(Dispatchers.Main) { bucketProcessed.value = index + 1 }
//					}
//				}
//
////			** BucketItem
//			repository.getAllBucketItem()
//				.let { bucketItemList ->
//					viewModelScope.launch(Dispatchers.Main) {
//						bucketItemCount.value = bucketItemList.size
//						bucketItemProcessed.value = 0
//					}
//					bucketItemList.forEachIndexed { index, bucketItemObject ->
//						val snapshotString = objectMapper.writeValueAsString(bucketItemObject.toSnapshot())
//						val snapshotFile = File(bucketItemFolder.path, "${bucketItemObject.id}.json")
//						snapshotFile.writeText(snapshotString)
//						viewModelScope.launch(Dispatchers.Main) { bucketItemProcessed.value = index + 1 }
//					}
//				}
//
////			** Chapter
//			repository.getAllChapter()
//				.let { chapterList ->
//					viewModelScope.launch(Dispatchers.Main) {
//						chapterCount.value = chapterList.size
//						chapterProcessed.value = 0
//					}
//					chapterList.forEachIndexed { index, chapterObject ->
//						val snapshotString = objectMapper.writeValueAsString(chapterObject.toSnapshot())
//						val snapshotFile = File(chapterFolder.path, "${chapterObject.id}.json")
//						snapshotFile.writeText(snapshotString)
//						viewModelScope.launch(Dispatchers.Main) { chapterProcessed.value = index + 1 }
//					}
//				}
//
////			** Note
//			repository.getAllNote()
//				.let { noteList ->
//					viewModelScope.launch(Dispatchers.Main) {
//						noteCount.value = noteList.size
//						noteProcessed.value = 0
//					}
//					noteList.forEachIndexed { index, note ->
//						val snapshotString = objectMapper.writeValueAsString(note.toSnapshot())
//						val snapshotFile = File(noteFolder.path, "${note.id}.json")
//						snapshotFile.writeText(snapshotString)
//						viewModelScope.launch(Dispatchers.Main) { noteProcessed.value = index + 1 }
//					}
//				}
//
////			** Tag
//			repository.getAllTag()
//				.let { tagList ->
//					viewModelScope.launch(Dispatchers.Main) {
//						tagCount.value = tagList.size
//						tagProcessed.value = 0
//					}
//					tagList.forEachIndexed { index, tag ->
//						val snapshotString = objectMapper.writeValueAsString(tag.toSnapshot())
//						val snapshotFile = File(tagFolder.path, "${tag.id}.json")
//						snapshotFile.writeText(snapshotString)
//						viewModelScope.launch(Dispatchers.Main) { tagProcessed.value = index + 1 }
//					}
//				}
//
//			val sevenZOutput = SevenZOutputFile(File(snapshotFolder, "${currentSnapshotFolder.name}.7z"))
//			compressFile(currentSnapshotFolder, sevenZOutput)
//
//			File(snapshotFolder, "${currentSnapshotFolder.name}.7z").apply {
//				callback(true, this)
//			}
//
//			viewModelScope.launch(Dispatchers.Main) {
//				noteCount.value = 0
//				noteProcessed.value = 0
//			}
//		} catch (e : Exception) {
//			e.printStackTrace()
//			callback(false, null)
//		}
//	}
}

package com.syncodec.graphite.presentation.settings.composable.dialog.importData

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.syncodec.graphite.di.model.BucketItemSnapshot
import com.syncodec.graphite.di.model.BucketSnapshot
import com.syncodec.graphite.di.model.ChapterSnapshot
import com.syncodec.graphite.di.model.NoteSnapshot
import com.syncodec.graphite.di.model.TagSnapshot
import com.syncodec.graphite.di.repository.KoinRepository
import com.syncodec.graphite.di.repository.RealmUUIDDeserializer
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.json.JSONObject
import org.koin.android.annotation.KoinViewModel
import java.io.File


@KoinViewModel
class ImportDataViewModel(private val repository : KoinRepository) : ViewModel() {

	private val objectMapper = jsonMapper {
		addModule(
			kotlinModule().addDeserializer(
				RealmUUID::class.java,
				RealmUUIDDeserializer()
			)
		)
	}.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	fun importData(restoreFolder : File, sevenZFile : SevenZFile, progress : (Int, Int) -> Unit, onComplete: () -> Unit) {
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
		val baseFile = File(restoreFolder, "base.json")
		val bucketItemFile = File(restoreFolder, "bucketItem").listFiles()
		val bucketFileList = File(restoreFolder, "bucket").listFiles()
		val chapterFileList = File(restoreFolder, "chapter").listFiles()
		val noteFileList = File(restoreFolder, "note").listFiles()
		val tagFolder = File(restoreFolder, "tag").listFiles()

		viewModelScope.launch(Dispatchers.Default) {
//		    ** Base
			try {
				val baseJsonObject = JSONObject(baseFile.readText())
				val defaultChapterIdString = baseJsonObject.optString("default_chapter_id")
				if (defaultChapterIdString.isNotBlank()) {
					val defaultChapterId = defaultChapterIdString.let { RealmUUID.from(it) }
					repository.putDefaultChapterId(defaultChapterId) {}
				}
			} catch (e : Exception) {
				e.printStackTrace()
			}

			val totalSize = (bucketItemFile?.size ?: 0) +
					(bucketFileList?.size ?: 0) +
					(chapterFileList?.size ?: 0) +
					(noteFileList?.size ?: 0) +
					(tagFolder?.size ?: 0)
			var processesSize = 0

//		    ** Bucket
			bucketFileList?.forEach {
				try {
					val bucketSnapshot = objectMapper.readValue(it.readBytes(), BucketSnapshot::class.java)
					bucketSnapshot.toObject().let { bucketObject ->
						repository.putBucket(bucketObject) { _, e -> }
						progress(++ processesSize, totalSize)
						delay(100)
					}
				} catch (e : Exception) {
					e.printStackTrace()
				}
			}

//		    ** BucketItem
			bucketItemFile?.forEach {
				try {
					val bucketItemSnapshot = objectMapper.readValue(it.readBytes(), BucketItemSnapshot::class.java)
					bucketItemSnapshot.toObject().let { bucketItemObject ->
						bucketItemObject.parentId?.let { repository.putBucketItem(it, bucketItemObject) { _, e -> } }
						progress(++ processesSize, totalSize)
						delay(100)
					}
				} catch (e : Exception) {
					e.printStackTrace()
				}
			}

//		    ** Chapter
			chapterFileList?.forEach {
				try {
					val chapterSnapshot = objectMapper.readValue(it.readBytes(), ChapterSnapshot::class.java)
					chapterSnapshot.toObject().let { chapterObject ->
						repository.putChapter(chapterObject.parentId, chapterObject) { _, e -> }
						progress(++ processesSize, totalSize)
						delay(100)
					}
				} catch (e : Exception) {
					e.printStackTrace()
				}
			}

//		    ** Note
			noteFileList?.forEach {
				try {
					val noteSnapshot = objectMapper.readValue(it.readBytes(), NoteSnapshot::class.java)
					noteSnapshot.toObject().let { noteObject ->
						repository.putNote(noteObject) { _, e -> }
						progress(++ processesSize, totalSize)
						delay(100)
					}
				} catch (e : Exception) {
					e.printStackTrace()
				}
			}

//	        ** Tag
			tagFolder?.forEach {
				try {
					val tagSnapshot = objectMapper.readValue(it.readBytes(), TagSnapshot::class.java)
					tagSnapshot.toObject().let { tagObject ->
						repository.putTag(tagObject) { _, e -> }
						progress(++ processesSize, totalSize)
						delay(100)
					}
				} catch (e : Exception) {
					e.printStackTrace()
				}
			}

			onComplete()
		}
	}
}

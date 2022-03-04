package com.syncodec.momento

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.syncodec.momento.database.diary.Note
import com.syncodec.momento.database.notebook.Chapter
import com.syncodec.momento.database.notebook.Notebook
import java.io.*
import java.net.URL
import java.net.URLConnection


class Momento : Application() {

	private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

	var vaultState = mutableStateOf(VaultState.NOT_OPENED)

	lateinit var ROOT: String

	var DATA: String = "data"
		get() = "$ROOT/$field"

	var DIARY_DIR = "diary"
		get() = "$DATA/$field"

	var BUCKET_DIR = "bucket"
		get() = "$DATA/$field"

	var NOTEBOOK_DIR = "notebook"
		get() = "$DATA/$field"

	override fun onCreate() {
		super.onCreate()

		ROOT = applicationContext.applicationInfo.dataDir
	}

	fun getBucketDirPath(bucketKey: String): String {
		return "$BUCKET_DIR/bucket_$bucketKey"
	}

	fun getBucketDataPath(bucketKey: String): String {
		File("$BUCKET_DIR/bucket_${bucketKey}").mkdirs()
		return "$BUCKET_DIR/bucket_$bucketKey/bucket_$bucketKey.json"
	}

	fun getBucketItemDirPath(bucketKey: String, bucketItemKey: String): String {
		File("$BUCKET_DIR/bucket_$bucketKey/bucket_item_$bucketItemKey").mkdirs()
		return "$BUCKET_DIR/bucket_$bucketKey/bucket_item_$bucketItemKey"
	}

	fun getBucketItemDataPath(bucketKey: String, bucketItemKey: String): String {
		File("$BUCKET_DIR/bucket_$bucketKey/bucket_item_$bucketItemKey").mkdirs()
		return "$BUCKET_DIR/bucket_$bucketKey/bucket_item_$bucketItemKey/data.json"
	}

	fun getBucketItemThoughtPath(bucketKey: String, bucketItemKey: String): String {
		File("$BUCKET_DIR/bucket_$bucketKey/bucket_item_$bucketItemKey").mkdirs()
		return "$BUCKET_DIR/bucket_$bucketKey/bucket_item_$bucketItemKey/thought.json"
	}

	fun getBucketItemThumbnailPath(bucketKey: String, bucketItemKey: String): String {
		File("$BUCKET_DIR/bucket_$bucketKey").mkdirs()
		return "$BUCKET_DIR/bucket_$bucketKey/bucket_item_$bucketItemKey/bucket_item_thumbnail_$bucketItemKey.png"
	}

	fun getNotebookDirPath(notebookKey: String): String {
		return "$NOTEBOOK_DIR/notebook_$notebookKey"
	}

	fun getNotebookImagePath(notebookKey: String): String {
		File(getNotebookDirPath(notebookKey = notebookKey)).mkdirs()
		return "${getNotebookDirPath(notebookKey = notebookKey)}/notebook_image_$notebookKey.png"
	}

	fun getNotebookDataPath(notebookKey: String): String {
		File("$NOTEBOOK_DIR/notebook_${notebookKey}").mkdirs()
		return "${getNotebookDirPath(notebookKey = notebookKey)}/notebook_$notebookKey.json"
	}

	fun getNoteDataPath(notebookKey: String, noteKey: String): String {
		File("$NOTEBOOK_DIR/notebook_${notebookKey}").mkdirs()
		return "$NOTEBOOK_DIR/notebook_$notebookKey/note_$noteKey.json"
	}

	fun getDiaryDirPath(diaryKey: String): String {
		return "$DIARY_DIR/diary_$diaryKey"
	}

	fun getDiaryDataPath(diaryKey: String): String {
		File("$DIARY_DIR/diary_${diaryKey}").mkdirs()
		return "${getDiaryDirPath(diaryKey = diaryKey)}/diary_$diaryKey.json"
	}

	fun putDiary(
		note: Note
	): Boolean {
		val file = File(getDiaryDataPath(diaryKey = note.primaryKey))
		return if (file.exists()) {
			objectMapper.writeValue(file, note)
			true
		} else {
			File("$DIARY_DIR/diary_${note.primaryKey}").mkdirs()
			objectMapper.writeValue(file, note)
			false
		}
	}

	fun getDiary(
		primaryKey: String
	): Note {
		val file = File(getDiaryDataPath(diaryKey = primaryKey))
		return objectMapper.readValue(file.readBytes())
	}

	fun putBucketItemData(
		bucketKey: String,
		bucketItemKey: String,
		jsonString: String,
		thumbnail: Bitmap?
	) {
		val bucketItemDataFile = File(getBucketItemDataPath(bucketKey = bucketKey, bucketItemKey = bucketItemKey))
		bucketItemDataFile.writeText(jsonString)

		if (thumbnail != null) {
			val bucketItemThumbnailFile = File(getBucketItemThumbnailPath(bucketKey = bucketKey, bucketItemKey = bucketItemKey))
			thumbnail.compress(Bitmap.CompressFormat.PNG, 100, bucketItemThumbnailFile.outputStream())
		}
	}

	fun getBucketItemData(
		bucketKey: String,
		bucketItemKey: String,
	): String {
		val file = File(getBucketItemDataPath(bucketKey = bucketKey, bucketItemKey = bucketItemKey))
		return file.readText()
	}

	fun putThought(
		bucketKey: String,
		bucketItemKey: String,
		thoughtList: List<String>
	) {
		val file = File(getBucketItemThoughtPath(bucketKey = bucketKey, bucketItemKey = bucketItemKey))
		objectMapper.writeValue(file, thoughtList)
	}

	fun getThought(
		bucketKey: String,
		bucketItemKey: String
	): List<String> {
		return try {
			val file = File(getBucketItemThoughtPath(bucketKey = bucketKey, bucketItemKey = bucketItemKey))
			objectMapper.readValue(file)
		} catch (exception: Exception) {
			emptyList()
		}
	}

	fun getBucketItemThumbnail(
		bucketKey: String,
		bucketItemKey: String
	): String? {
		val file = File(getBucketItemThumbnailPath(bucketKey = bucketKey, bucketItemKey = bucketItemKey))
		return if (file.exists()) getBucketItemThumbnailPath(bucketKey = bucketKey, bucketItemKey = bucketItemKey) else null
	}

	fun deleteBucketItem(
		bucketKey: String,
		bucketItemKey: String
	) {
		File(
			getBucketItemDirPath(
				bucketKey = bucketKey,
				bucketItemKey = bucketItemKey
			)
		).deleteRecursively()
	}

	fun downloadMovieData(
		requestUrl: String
	): String {
		val url = URL(requestUrl)
		val connection: URLConnection = url.openConnection()
		connection.connect()

		val input: InputStream = BufferedInputStream(
			url.openStream(),
			8192
		)

		val output = ByteArrayOutputStream()

		val data = ByteArray(1024)

		var total: Long = 0
		var count = 0

		while (input.read(data).also { count = it } != -1) {
			total += count
			output.write(data, 0, count)
		}
		output.flush()
		output.close()
		input.close()

		return output.toString()
	}

	//	Notebook

	fun putNotebook(
		notebook: Notebook
	): Boolean {
		val file = File(getNotebookDataPath(notebookKey = notebook.primaryKey))
		return if (file.exists()) {
			objectMapper.writeValue(file, notebook)
			true
		} else {
			objectMapper.writeValue(file, notebook)
			false
		}
	}

	fun getNotebook(
		primaryKey: String
	): Notebook {
		val file = File(getNotebookDataPath(notebookKey = primaryKey))
		if (file.exists() && file.isFile) {
			return objectMapper.readValue(file)
		} else {
			throw FileNotFoundException()
		}
	}

	fun putNotebookImage(notebookKey: String, image: Bitmap) {
		val imageFile = File(getNotebookImagePath(notebookKey = notebookKey))
		val os: OutputStream = BufferedOutputStream(FileOutputStream(imageFile))
		image.compress(Bitmap.CompressFormat.JPEG, 100, os)
		os.close()
	}

	fun getNotebookImage(notebookKey: String): ImageBitmap? {
		val imageFile = File(getNotebookImagePath(notebookKey = notebookKey))
		return if (imageFile.exists()) {
			val imageBytes = imageFile.readBytes()
			BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size).asImageBitmap()
		} else {
			null
		}
	}

	fun putChapter(
		chapter: Chapter,
	) {
		this.getNotebook(primaryKey = chapter.notebookKey).apply {
			this.chapterMap[chapter.primaryKey] = chapter
			putNotebook(this)
		}
	}

	fun getNote(
		notebookKey: String,
		noteKey: String
	): Note {
		val file = File(getNoteDataPath(notebookKey = notebookKey, noteKey = noteKey))
		if (file.exists() && file.isFile) {
			return objectMapper.readValue(file)
		} else {
			throw FileNotFoundException()
		}
	}

	fun putNote(
		note: Note
	) {
		val file = File(getNoteDataPath(notebookKey = note.notebookKey!!, noteKey = note.primaryKey))
		objectMapper.writeValue(file, note)

		this.getNotebook(primaryKey = note.notebookKey!!).apply {
			note.content = null
			this.noteMap[note.primaryKey] = note
			putNotebook(this)
		}
	}

	companion object {
		enum class ComponentType {
			DIARY,
			NOTE,
			CHAPTER,
			NOTEBOOK,
			BUCKET,
			BUCKET_ITEM
		}

		enum class VaultState {
			NOT_OPENED,
			TRY_OPEN,
			SETUP,
			OPENED,
			CLOSED,
			ERROR
		}
	}
}

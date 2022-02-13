package com.syncodec.momento

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.syncodec.momento.database.bucket.Bucket
import com.syncodec.momento.database.bucket.BucketItem
import com.syncodec.momento.database.diary.Note
import com.syncodec.momento.database.notebook.Chapter
import com.syncodec.momento.database.notebook.Notebook
import java.io.*
import java.net.URL
import java.net.URLConnection


class Momento : Application() {

	private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())


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

	fun getBucketItemPath(bucketKey: String, bucketItemKey: String): String {
		File("$BUCKET_DIR/bucket_$bucketKey").mkdirs()
		return "$BUCKET_DIR/bucket_$bucketKey/bucket_item_$bucketItemKey.json"
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

	fun getChapterPath(notebookKey: String, chapterKey: String): String {
		File("$NOTEBOOK_DIR/notebook_${notebookKey}").mkdirs()
		return "$NOTEBOOK_DIR/notebook_$notebookKey/chapter_$chapterKey.json"
	}

	fun getDiaryDirPath(diaryKey: String): String {
		return "$DIARY_DIR/diary_$diaryKey"
	}

	fun getDiaryDataPath(diaryKey: String):String {
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

	fun getBucketFull(
		primaryKey: String
	): FileOutputStream {
		val bucketFile = File(getBucketDirPath(bucketKey = primaryKey))
		if (bucketFile.exists() && bucketFile.isDirectory) {
			return bucketFile.outputStream()
		} else {
			throw FileNotFoundException()
		}
	}

	fun getBucket(
		primaryKey: String
	): Bucket {
		val file = File(getBucketDataPath(bucketKey = primaryKey))
		if (file.exists() && file.isFile) {
			return objectMapper.readValue(file)
		} else {
			throw FileNotFoundException()
		}
	}

	fun putBucket(
		bucket: Bucket
	): Boolean {
		val file = File(getBucketDataPath(bucketKey = bucket.primaryKey))
		return if (file.exists()) {
			objectMapper.writeValue(file, bucket)
			true
		} else {
			objectMapper.writeValue(file, bucket)
			false
		}
	}

	fun getBucketItem(
		bucketKey: String,
		bucketItemKey: String
	): BucketItem {
		val file = File(getBucketItemPath(bucketKey = bucketKey, bucketItemKey = bucketItemKey))
		if (file.exists() && file.isFile) {
			return objectMapper.readValue(file)
		} else {
			throw FileNotFoundException()
		}
	}

	fun putBucketItem(
		bucketItem: BucketItem,
	): Boolean {
		val file = File(getBucketItemPath(bucketKey = bucketItem.bucketKey, bucketItemKey = bucketItem.primaryKey))
		return if (file.exists()) {
			file.writeBytes(objectMapper.writeValueAsBytes(bucketItem))
			true
		} else {
			file.writeBytes(objectMapper.writeValueAsBytes(bucketItem))
			false
		}
	}

	fun getAllBucketItems(
		bucketKey: String,
	): MutableList<BucketItem> {
		val bucketDirFile = File(getBucketDirPath(bucketKey = bucketKey))

		val bucketItemList: MutableList<BucketItem> = mutableListOf()

		bucketDirFile.listFiles { file, name ->
			name.startsWith("bucket_item")
		}?.forEach {
			try {
				val bucketItem: BucketItem = objectMapper.readValue(it)
				bucketItemList.add(bucketItem)
			} catch (exception: FileNotFoundException) {

			} catch (exception: Exception) {

			}
		}

		return bucketItemList
	}

	fun deleteBucketItem(
		bucketKey: String,
		bucketItemKey: String
	) {
		File(
			getBucketItemPath(
				bucketKey = bucketKey,
				bucketItemKey = bucketItemKey
			)
		).delete()
	}

	fun getBucketSize(
		bucketKey: String
	): Int {
		val bucketDirFile = File(getBucketDirPath(bucketKey = bucketKey))
		return bucketDirFile.listFiles { file, name ->
			name.startsWith("bucket_item")
		}?.size ?: 0
	}

	fun downloadBucketItemThumbnail(
		thumbnailUrl: String,
	): ByteArray {
		val url = URL(thumbnailUrl)
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

		return output.toByteArray()
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
		}else {
			null
		}
	}

	fun openNotebook(
		primaryKey: String
	): Notebook {
		val file = File(getNotebookDataPath(notebookKey = primaryKey))
		if (file.exists() && file.isFile) {
			return objectMapper.readValue(file)
		} else {
			throw FileNotFoundException()
		}
	}

	fun putChapter(
		chapter: Chapter,
	): Boolean {
		val file = File(
			getChapterPath(
				notebookKey = chapter.notebookKey,
				chapterKey = chapter.primaryKey
			)
		)
		getNotebook(primaryKey = chapter.notebookKey).apply {
			this.chapterMap[chapter.primaryKey] = chapter
			putNotebook(this)
		}
		return if (file.exists()) {
			objectMapper.writeValue(file, chapter)
			true
		} else {
			objectMapper.writeValue(file, chapter)
			false
		}
	}

//	fun getNote(
//		notebookKey: String,
//		currentPath: List<String>,
//		noteKey: String
//	): Note {
//		var chapterPath = getNotebookDirPath(notebookKey = notebookKey)
//		currentPath.forEach { chapterPath = "$chapterPath/$it" }
//		chapterPath = "$chapterPath/chapter_$chapterKey/chapter_$chapterKey.json"
//
//		val file = File(chapterPath)
//		if (file.exists() && file.isFile) {
//			return objectMapper.readValue(file)
//		} else {
//			throw FileNotFoundException()
//
//		}
//	}

}

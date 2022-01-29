package com.syncodec.momento

import android.app.Application
import android.os.Environment
import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.syncodec.momento.database.bucket.Bucket
import com.syncodec.momento.database.bucket.BucketItem
import com.syncodec.momento.database.diary.Diary
import okhttp3.OkHttp
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

	override fun onCreate() {
		super.onCreate()

		ROOT = applicationContext.applicationInfo.dataDir
	}

	suspend fun putDiary(diary: Diary): Boolean {
		val file = File("$DIARY_DIR/diary_${diary.primaryKey}/diary_${diary.primaryKey}.json")
		return if (file.exists()) {
			objectMapper.writeValue(file, diary)
			true
		} else {
			File("$DIARY_DIR/diary_${diary.primaryKey}").mkdirs()
			objectMapper.writeValue(file, diary)
			false
		}
	}

	suspend fun getBucketFull(primaryKey: String): FileOutputStream {
		val bucketFile = File("$BUCKET_DIR/bucket_$primaryKey")
		if (bucketFile.exists() && bucketFile.isDirectory) {
			return bucketFile.outputStream()
		} else {
			throw FileNotFoundException()
		}
	}

	suspend fun getBucket(primaryKey: String): Bucket {
		val file = File("$BUCKET_DIR/bucket_$primaryKey/bucket_$primaryKey.json")
		if (file.exists() && file.isFile) {
			return objectMapper.readValue(file)
		} else {
			throw FileNotFoundException()
		}
	}

	suspend fun putBucket(bucket: Bucket): Boolean {
		val file = File("$BUCKET_DIR/bucket_${bucket.primaryKey}/bucket_${bucket.primaryKey}.json")
		return if (file.exists()) {
			objectMapper.writeValue(file, bucket)
			true
		} else {
			File("$BUCKET_DIR/bucket_${bucket.primaryKey}").mkdirs()
			objectMapper.writeValue(file, bucket)
			false
		}
	}

	suspend fun getBucketItem(bucketKey: String, bucketItemKey: String): BucketItem {
		val file = File("$BUCKET_DIR/bucket_$bucketKey/bucket_item_$bucketItemKey.json")
		if (file.exists() && file.isFile) {
			return objectMapper.readValue(file)
		} else {
			throw FileNotFoundException()
		}
	}

	suspend fun putBucketItem(
		bucketItem: BucketItem,
		isNewItem: Boolean = false
	): Boolean {
		if (isNewItem) {
			getBucket(bucketItem.bucketKey).apply {
				bucketItemKeyList.add(bucketItem.primaryKey)
				putBucket(this)
			}
		}
		val file = File("$BUCKET_DIR/bucket_${bucketItem.bucketKey}/bucket_item_${bucketItem.primaryKey}.json")
		return if (file.exists()) {
			file.writeBytes(objectMapper.writeValueAsBytes(bucketItem))
			getBucket(bucketItem.bucketKey).apply {
				bucketItemKeyList.add(bucketItem.primaryKey)
				containerSize = bucketItemKeyList.size
				putBucket(this)
			}
			true
		} else {
			File("$BUCKET_DIR/bucket_${bucketItem.bucketKey}").mkdirs()
			file.writeBytes(objectMapper.writeValueAsBytes(bucketItem))
			false
		}
	}

	suspend fun getAllBucketItems(
		bucketKey: String,
		bucketItemKeyList: Set<String>
	): MutableList<BucketItem> {
		val bucketItemList: MutableList<BucketItem> = mutableListOf()
		bucketItemKeyList.forEach { bucketItemKey ->
			try {
				val bucketItem = getBucketItem(bucketKey = bucketKey, bucketItemKey = bucketItemKey)
				bucketItemList.add(bucketItem)
			} catch (exception: FileNotFoundException) {

			} catch (exception: Exception) {

			}
		}

		return bucketItemList
	}

	fun downloadBucketItemThumbnail(
		thumbnailUrl: String,
		bucketKey: String,
		bucketItemKey: String
	): String {
		val url = URL(thumbnailUrl)
		val connection: URLConnection = url.openConnection()
		connection.connect()

		val thumbnailPath = "$BUCKET_DIR/bucket_${bucketKey}/bucket_item_thumbnail_${bucketItemKey}.jpg"

		val input: InputStream = BufferedInputStream(
			url.openStream(),
			8192
		)

		val output: OutputStream = FileOutputStream(
			File(thumbnailPath)
		)
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

		return thumbnailPath
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
}

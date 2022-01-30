package com.syncodec.momento

import android.app.Application
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.syncodec.momento.database.bucket.Bucket
import com.syncodec.momento.database.bucket.BucketItem
import com.syncodec.momento.database.diary.Diary
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

	fun getBucketDirPath(bucketKey: String): String {
		return "$BUCKET_DIR/bucket_$bucketKey"
	}

	fun getBucketDataPath(primaryKey: String): String {
		File("$BUCKET_DIR/bucket_${primaryKey}").mkdirs()
		return "$BUCKET_DIR/bucket_$primaryKey/bucket_$primaryKey.json"
	}

	fun getBucketItemPath(bucketKey: String, bucketItemKey: String): String {
		File("$BUCKET_DIR/bucket_$bucketKey").mkdirs()
		return "$BUCKET_DIR/bucket_$bucketKey/bucket_item_$bucketItemKey.json"
	}

	fun putDiary(diary: Diary): Boolean {
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

	fun getBucketFull(primaryKey: String): FileOutputStream {
		val bucketFile = File(getBucketDirPath(bucketKey = primaryKey))
		if (bucketFile.exists() && bucketFile.isDirectory) {
			return bucketFile.outputStream()
		} else {
			throw FileNotFoundException()
		}
	}

	fun getBucket(primaryKey: String): Bucket {
		val file = File(getBucketDataPath(primaryKey = primaryKey))
		if (file.exists() && file.isFile) {
			return objectMapper.readValue(file)
		} else {
			throw FileNotFoundException()
		}
	}

	fun putBucket(bucket: Bucket): Boolean {
		val file = File(getBucketDataPath(primaryKey = bucket.primaryKey))
		return if (file.exists()) {
			objectMapper.writeValue(file, bucket)
			true
		} else {
			objectMapper.writeValue(file, bucket)
			false
		}
	}

	fun getBucketItem(bucketKey: String, bucketItemKey: String): BucketItem {
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
}

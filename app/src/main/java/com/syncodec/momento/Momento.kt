package com.syncodec.momento

import android.app.Application
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.syncodec.momento.database.bucket.Bucket
import com.syncodec.momento.database.bucket.BucketItem
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

class Momento : Application() {

	private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())


	lateinit var ROOT: String

	var DATA: String = "data"
		get() = "$ROOT/$field"

	var BUCKET_DIR = "bucket"
		get() = "$DATA/$field"

	override fun onCreate() {
		super.onCreate()

		ROOT = applicationContext.applicationInfo.dataDir
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
			file.writeBytes(objectMapper.writeValueAsBytes(bucket))
			true
		} else {
			File("$BUCKET_DIR/bucket_${bucket.primaryKey}").mkdirs()
			file.writeBytes(objectMapper.writeValueAsBytes(bucket))
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

	suspend fun putBucketItem(bucketItem: BucketItem): Boolean {
		val file = File("$BUCKET_DIR/bucket_${bucketItem.bucketKey}/bucket_${bucketItem.primaryKey}.json")
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

	suspend fun getAllBucketItems(bucketKey: String, bucketItemKeyList: List<String>): MutableList<BucketItem> {
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
}

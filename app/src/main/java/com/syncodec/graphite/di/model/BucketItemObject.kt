package com.syncodec.graphite.di.model

import androidx.room.PrimaryKey
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.kedia.ogparser.OpenGraphResult
import com.syncodec.graphite.di.network.BookData
import com.syncodec.graphite.di.network.ShowData
import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmObject


enum class BucketItemState {
	ALPHA,
	BETA,
	GAMMA
}

class BucketItemObject: RealmObject {
	@PrimaryKey var id: ObjectId = ObjectId.create()

	var createdTimestamp: Long = System.currentTimeMillis()
	var modifiedTimestamp: Long = System.currentTimeMillis()
	var bucketType: String = BucketType.UNKNOWN.name
	var title: String? = null
	var state: String = BucketItemState.ALPHA.name
	var thumbnail: String? = null
	var isFavourite: Boolean = false
	var isLocked: Boolean = false

	var data: String? = null

	fun toBookData(): BookData? {
		return try {
			val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			objectMapper.readValue(data, BookData::class.java)
		} catch (e: Exception) {
			e.printStackTrace()
			null
		}
	}

	fun toShowData(): ShowData? {
		return try {
			val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			objectMapper.readValue(data, ShowData::class.java)
		} catch (e: Exception) {
			e.printStackTrace()
			null
		}
	}

	fun toOpenGraphResult(): OpenGraphResult? {
		return try {
			val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			objectMapper.readValue(data, OpenGraphResult::class.java)
		} catch (e: Exception) {
			e.printStackTrace()
			null
		}
	}

	fun setOpenGraphResult(openGraphResult: OpenGraphResult) {
		this.data = jsonMapper { addModule(kotlinModule()) }.writeValueAsString(openGraphResult)
	}

	override fun hashCode(): Int {
		var result = id.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		result = 31 * result + bucketType.hashCode()
		result = 31 * result + title.hashCode()
		result = 31 * result + state.hashCode()
		result = 31 * result + isFavourite.hashCode()
		result = 31 * result + isLocked.hashCode()
		result = 31 * result + (data?.hashCode() ?: 0)
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is BucketItemObject) return false

		if (id != other.id) return false
		if (createdTimestamp != other.createdTimestamp) return false
		if (modifiedTimestamp != other.modifiedTimestamp) return false
		if (bucketType != other.bucketType) return false
		if (title != other.title) return false
		if (state != other.state) return false
		if (isFavourite != other.isFavourite) return false
		if (isLocked != other.isLocked) return false
		if (data != other.data) return false

		return true
	}
}



data class BucketItemPreview(
	val id: String,
	val createdTimestamp: String,
	val modifiedTimestamp: String,
	val bucketId: String,
	val bucketType: String,
	val title: String,
	val state: String,
	val thumbnail: String,
	val isFavourite: Boolean,
	val isLocked: Boolean
)

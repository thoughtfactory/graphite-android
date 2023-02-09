package com.syncodec.graphite.di.model

import androidx.annotation.Keep
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.kedia.ogparser.OpenGraphResult
import com.syncodec.graphite.di.network.BookData
import com.syncodec.graphite.di.network.ShowData
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey


enum class BucketItemState {
	ALPHA,
	BETA,
	GAMMA
}

@Keep
class BucketItemObject : RealmObject {
	@PrimaryKey
	var id : RealmUUID = RealmUUID.random()

	var createdTimestamp : Long = System.currentTimeMillis()
	var modifiedTimestamp : Long = System.currentTimeMillis()
	var bucketType : String = BucketType.UNKNOWN.name
	var title : String? = null
	var state : String = BucketItemState.ALPHA.name
	var thumbnail : String? = null
	var isFavourite : Boolean = false
	var isLocked : Boolean = false
	var parentId : RealmUUID? = null

	var key : String? = null
	var data : String? = null

	fun getBookData() : BookData? {
		return try {
			val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			objectMapper.readValue(data, BookData::class.java)
		} catch (e : Exception) {
//			e.printStackTrace()
			null
		}
	}

	fun getShowData() : ShowData? {
		return try {
			val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			objectMapper.readValue(data, ShowData::class.java)
		} catch (e : Exception) {
//			e.printStackTrace()
			null
		}
	}

	fun getOpenGraphResult() : OpenGraphResult? {
		return try {
			val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			objectMapper.readValue(data, OpenGraphResult::class.java)
		} catch (e : Exception) {
//			e.printStackTrace()
			null
		}
	}

	fun putOpenGraphResult(openGraphResult : OpenGraphResult) {
		this.data = jsonMapper { addModule(kotlinModule()) }.writeValueAsString(openGraphResult)
	}

	fun clone() : BucketItemObject {
		return BucketItemObject().apply {
			this.id = this@BucketItemObject.id
			this.createdTimestamp = this@BucketItemObject.createdTimestamp
			this.modifiedTimestamp = this@BucketItemObject.modifiedTimestamp
			this.bucketType = this@BucketItemObject.bucketType
			this.title = this@BucketItemObject.title
			this.state = this@BucketItemObject.state
			this.thumbnail = this@BucketItemObject.thumbnail
			this.isFavourite = this@BucketItemObject.isFavourite
			this.isLocked = this@BucketItemObject.isLocked
			this.parentId = this@BucketItemObject.parentId
			this.key = this@BucketItemObject.key
			this.data = this@BucketItemObject.data
		}
	}

	override fun hashCode() : Int {
		var result = id.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		result = 31 * result + bucketType.hashCode()
		result = 31 * result + (title?.hashCode() ?: 0)
		result = 31 * result + state.hashCode()
		result = 31 * result + (thumbnail?.hashCode() ?: 0)
		result = 31 * result + isFavourite.hashCode()
		result = 31 * result + isLocked.hashCode()
		result = 31 * result + (parentId?.hashCode() ?: 0)
		result = 31 * result + (key?.hashCode() ?: 0)
		result = 31 * result + (data?.hashCode() ?: 0)
		return result
	}

	override fun equals(other : Any?) : Boolean {
		if (this === other) return true
		if (other !is BucketItemObject) return false

		if (id != other.id) return false
		if (createdTimestamp != other.createdTimestamp) return false
		if (modifiedTimestamp != other.modifiedTimestamp) return false
		if (bucketType != other.bucketType) return false
		if (title != other.title) return false
		if (state != other.state) return false
		if (thumbnail != other.thumbnail) return false
		if (isFavourite != other.isFavourite) return false
		if (isLocked != other.isLocked) return false
		if (parentId != other.parentId) return false
		if (key != other.key) return false
		if (data != other.data) return false

		return true
	}
}

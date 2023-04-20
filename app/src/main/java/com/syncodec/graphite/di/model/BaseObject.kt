package com.syncodec.graphite.di.model

import androidx.annotation.Keep
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.realmSetOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.ext.toRealmSet
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmSet
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey
import org.json.JSONObject


@Keep
class BaseObject : RealmObject {
	@PrimaryKey
	var id : RealmUUID = RealmUUID.random()
	var defaultChapterId : RealmUUID? = null

	var notebookIdOrderList : RealmList<RealmUUID> = realmListOf()
	var bucketIdOrderList : RealmList<RealmUUID> = realmListOf()

	var deletedObjectSet : RealmSet<DeletedObject> = realmSetOf()
	var deletedAttachmentSet : RealmSet<DeletedAttachment> = realmSetOf()

	var lastSyncedTimestamp : Long = 0

	var modifiedTimestamp : Long = System.currentTimeMillis()

	fun clone() : BaseObject = BaseObject().apply {
		this.id = this@BaseObject.id
		this.defaultChapterId = this@BaseObject.defaultChapterId
		this.lastSyncedTimestamp = this@BaseObject.lastSyncedTimestamp
		this.notebookIdOrderList = this@BaseObject.notebookIdOrderList.toRealmList()
		this.bucketIdOrderList = this@BaseObject.bucketIdOrderList.toRealmList()
		this.deletedObjectSet = this@BaseObject.deletedObjectSet.toRealmSet()
		this.modifiedTimestamp = this@BaseObject.modifiedTimestamp
	}

	fun toCloudSnapshot() : String {
		val jsonObject = JSONObject()
		jsonObject.put("defaultChapterId", defaultChapterId?.toString() ?: "")
		jsonObject.put("notebookIdOrderList", notebookIdOrderList.map { it.toString() })
		jsonObject.put("bucketIdOrderList", bucketIdOrderList.map { it.toString() })
		jsonObject.put("modifiedTimestamp", modifiedTimestamp)
		return jsonObject.toString()
	}

	override fun hashCode() : Int {
		var result = id.hashCode()
		result = 31 * result + (defaultChapterId?.hashCode() ?: 0)
		result = 31 * result + lastSyncedTimestamp.hashCode()
		result = 31 * result + bucketIdOrderList.hashCode()
		result = 31 * result + deletedObjectSet.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		return result
	}

	override fun equals(other : Any?) : Boolean {
		if (this === other) return true
		if (other !is BaseObject) return false

		if (id != other.id) return false
		if (defaultChapterId != other.defaultChapterId) return false
		if (lastSyncedTimestamp != other.lastSyncedTimestamp) return false
		if (bucketIdOrderList != other.bucketIdOrderList) return false
		if (deletedObjectSet != other.deletedObjectSet) return false
		if (modifiedTimestamp != other.modifiedTimestamp) return false

		return true
	}

	companion object {
		fun fromCloudSnapshot(snapshot : ByteArray) : BaseObject? {
			try {
				val jsonObject = JSONObject(String(snapshot, Charsets.UTF_8))
				val baseObject = BaseObject()
				jsonObject.getString("defaultChapterId").let {
					if (it.isNotEmpty()) try {
						baseObject.defaultChapterId = RealmUUID.from(it)
					} catch (_ : Exception) {
					}
				}
				jsonObject.getJSONArray("notebookIdOrderList").let {
					val size = it.length()
					for (i in 0 until size) {
						baseObject.notebookIdOrderList.add(RealmUUID.from(it.getString(i)))
					}
				}
				jsonObject.getJSONArray("bucketIdOrderList").let {
					val size = it.length()
					for (i in 0 until size) {
						baseObject.bucketIdOrderList.add(RealmUUID.from(it.getString(i)))
					}
				}
				baseObject.modifiedTimestamp = jsonObject.getLong("modifiedTimestamp")
				return baseObject
			} catch (e : Exception) {
				return null
			}
		}
	}
}

@Keep
class DeletedObject : RealmObject {
	@PrimaryKey
	var id : RealmUUID = RealmUUID.random()
	var deletedTimestamp : Long = 0
	var objectType : String? = ""

	override fun hashCode() : Int {
		var result = id.hashCode()
		result = 31 * result + deletedTimestamp.hashCode()
		result = 31 * result + objectType.hashCode()
		return result
	}

	override fun equals(other : Any?) : Boolean {
		if (this === other) return true
		if (other !is DeletedObject) return false

		if (id != other.id) return false
		if (deletedTimestamp != other.deletedTimestamp) return false
		if (objectType != other.objectType) return false

		return true
	}
}

@Keep
class DeletedAttachment : RealmObject {
	var parentId : RealmUUID = RealmUUID.random()
	var fileName : String = ""

	override fun hashCode() : Int {
		var result = parentId.hashCode()
		result = 31 * result + fileName.hashCode()
		return result
	}

	override fun equals(other : Any?) : Boolean {
		if (this === other) return true
		if (other !is DeletedAttachment) return false

		if (parentId != other.parentId) return false
		if (fileName != other.fileName) return false

		return true
	}

	override fun toString() : String {
		return "$parentId/$fileName"
	}
}

package com.syncodec.graphite.di.model.local

import androidx.annotation.Keep
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.realmSetOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmSet
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey
import org.json.JSONObject
import java.time.Instant


@Keep
class BaseObject : RealmObject {

	@PrimaryKey
	var id: RealmUUID = RealmUUID.random()

	/**
	 * The [RealmUUID] of the default chapter. Should never be null.
	 */
	var defaultChapterId: RealmUUID? = null

	/**
	 * Stores the order of the [ChapterObject]s to render in NotebookScreen.
	 */
	var notebookIdOrderList: RealmList<RealmUUID> = realmListOf()

	/**
	 * Stores the order of the [BucketObject]s to render in ListScreen.
	 */
	var bucketIdOrderList: RealmList<RealmUUID> = realmListOf()

	/**
	 * It keeps track of deleted objects so that they can be deleted from the cloud.
	 */
	var deletedObjectSet: RealmSet<ObjectIdentity> = realmSetOf()

	/**
	 * It keeps track of deleted attachments so that they can be deleted from the cloud.
	 */
	var deletedAttachmentSet: RealmSet<AttachmentIdentity> = realmSetOf()

	var modifiedTimestamp: Long = Instant.now().toEpochMilli()

	fun clone(): BaseObject = BaseObject().apply {
		this.id = this@BaseObject.id
		this.defaultChapterId = this@BaseObject.defaultChapterId
		this.notebookIdOrderList = this@BaseObject.notebookIdOrderList.toRealmList()
		this.bucketIdOrderList = this@BaseObject.bucketIdOrderList.toRealmList()
		this.deletedObjectSet = this@BaseObject.deletedObjectSet
		this.modifiedTimestamp = this@BaseObject.modifiedTimestamp
	}

	fun toCloudSnapshot(): String {
		val jsonObject = JSONObject()
		jsonObject.put("defaultChapterId", defaultChapterId?.toString() ?: "")
		jsonObject.put("notebookIdOrderList", notebookIdOrderList.map { it.toString() })
		jsonObject.put("bucketIdOrderList", bucketIdOrderList.map { it.toString() })
		jsonObject.put("modifiedTimestamp", modifiedTimestamp)
		return jsonObject.toString()
	}

	override fun hashCode(): Int {
		var result = id.hashCode()
		result = 31 * result + (defaultChapterId?.hashCode() ?: 0)
		result = 31 * result + notebookIdOrderList.hashCode()
		result = 31 * result + bucketIdOrderList.hashCode()
		result = 31 * result + deletedObjectSet.hashCode()
		result = 31 * result + deletedAttachmentSet.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as BaseObject

		if (id != other.id) return false
		if (defaultChapterId != other.defaultChapterId) return false
		if (notebookIdOrderList != other.notebookIdOrderList) return false
		if (bucketIdOrderList != other.bucketIdOrderList) return false
		if (deletedObjectSet != other.deletedObjectSet) return false
		if (deletedAttachmentSet != other.deletedAttachmentSet) return false
		if (modifiedTimestamp != other.modifiedTimestamp) return false

		return true
	}
}

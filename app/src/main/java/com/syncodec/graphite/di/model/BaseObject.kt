package com.syncodec.graphite.di.model

import androidx.annotation.Keep
import com.syncodec.graphite.di.model.serializer.RealmUUIDNullableSerializer
import com.syncodec.graphite.di.model.serializer.RealmUUIDSerializer
import com.syncodec.graphite.service.syncInator.SyncInatorService
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.realmSetOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.ext.toRealmSet
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmSet
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.serialization.Serializable
import org.json.JSONObject
import java.time.Instant


@Keep
class BaseObject : RealmObject {
	@Serializable(with = RealmUUIDSerializer::class)
	@PrimaryKey
	var id: RealmUUID = RealmUUID.random()

	/**
	 * The [RealmUUID] of the default chapter. Should never be null.
	 */
	@Serializable(with = RealmUUIDNullableSerializer::class)
	var defaultChapterId: RealmUUID? = null

	/**
	 * Stores the order of the [ChapterObject]s to render in NotebookScreen.
	 */
	var notebookIdOrderList: RealmList<@Serializable(with = RealmUUIDSerializer::class)RealmUUID> = realmListOf()

	/**
	 * Stores the order of the [BucketObject]s to render in ListScreen.
	 */
	var bucketIdOrderList: RealmList<@Serializable(with = RealmUUIDSerializer::class)RealmUUID> = realmListOf()

	/**
	 * It keeps track of deleted objects so that they can be deleted from the cloud.
	 */
	var deletedObjectSet: RealmSet<DeletedObject> = realmSetOf()

	/**
	 * It keeps track of deleted attachments so that they can be deleted from the cloud.
	 */
	var deletedAttachmentSet: RealmSet<DeletedAttachment> = realmSetOf()

	var modifiedTimestamp: Long = Instant.now().toEpochMilli()

	fun clone(): BaseObject = BaseObject().apply {
		this.id = this@BaseObject.id
		this.defaultChapterId = this@BaseObject.defaultChapterId
		this.notebookIdOrderList = this@BaseObject.notebookIdOrderList.toRealmList()
		this.bucketIdOrderList = this@BaseObject.bucketIdOrderList.toRealmList()
		this.deletedObjectSet = this@BaseObject.deletedObjectSet.toRealmSet()
		this.modifiedTimestamp = this@BaseObject.modifiedTimestamp
	}

	/**
	 * It converts the object to a JSON string to be stored in the cloud.
	 * All properties of the object are not needed to be stored in the cloud.
	 */
	fun toCloudSnapshot(): String {
		val jsonObject = JSONObject()
		jsonObject.put("defaultChapterId", defaultChapterId?.toString() ?: "")
		jsonObject.put("notebookIdOrderList", notebookIdOrderList.map { it.toString() })
		jsonObject.put("bucketIdOrderList", bucketIdOrderList.map { it.toString() })
		jsonObject.put("modifiedTimestamp", modifiedTimestamp)
		return jsonObject.toString()
	}

//	override fun hashCode(): Int {
//		var result = id.hashCode()
//		result = 31 * result + (defaultChapterId?.hashCode() ?: 0)
//		result = 31 * result + bucketIdOrderList.hashCode()
//		result = 31 * result + deletedObjectSet.hashCode()
//		result = 31 * result + modifiedTimestamp.hashCode()
//		return result
//	}
//
//	override fun equals(other: Any?): Boolean {
//		if (this === other) return true
//		if (other !is BaseObject) return false
//
//		if (id != other.id) return false
//		if (defaultChapterId != other.defaultChapterId) return false
//		if (bucketIdOrderList != other.bucketIdOrderList) return false
//		if (deletedObjectSet != other.deletedObjectSet) return false
//		if (modifiedTimestamp != other.modifiedTimestamp) return false
//
//		return true
//	}

	companion object {
		fun fromCloudSnapshot(snapshot: ByteArray): BaseObject? {
			try {
				val jsonObject = JSONObject(String(snapshot, Charsets.UTF_8))
				val baseObject = BaseObject()
				jsonObject.getString("defaultChapterId").let {
					if (it.isNotEmpty()) try {
						baseObject.defaultChapterId = RealmUUID.from(it)
					} catch (_: Exception) {
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
			} catch (e: Exception) {
				return null
			}
		}
	}
}

/**
 * It is used to keep track of deleted objects so that they can be deleted from the cloud.
 * @author pushpull
 * @since 2.3.0
 */
@Keep
@Serializable
class DeletedObject : RealmObject {
	/**
	 * The [RealmUUID] of the deleted object.
	 */
	@PrimaryKey
	@Serializable(with = RealmUUIDSerializer::class)
	var id: RealmUUID = RealmUUID.random()

	/**
	 * Exact file id of the object in Google Drive.
	 */
	var googleDriveId: String? = null

	/**
	 * The timestamp of when the object was deleted.
	 * If cloud modified timestamp is less than this, then the object should be deleted from the cloud.
	 */
	var deletedTimestamp: Long = 0

	/**
	 * The type of the deleted object. String representation of T::class.simpleName
	 * It is used to determine the type of the object when it is deleted from the cloud.
	 */
	var objectType: String? = ""

	override fun hashCode(): Int {
		var result = id.hashCode()
		result = 31 * result + deletedTimestamp.hashCode()
		result = 31 * result + objectType.hashCode()
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is DeletedObject) return false

		if (id != other.id) return false
		if (deletedTimestamp != other.deletedTimestamp) return false
		if (objectType != other.objectType) return false

		return true
	}
}

/**
 * It is used to keep track of deleted attachments so that they can be deleted from the cloud.
 * This is a fuck up.
 * It is supposed to be [com.syncodec.graphite.service.syncInator.SyncInatorService.Companion.AttachmentMetadata.Companion.AttachmentIdentity].
 *
 * @author pushpull
 * @since 2.3.0
 */
@Deprecated(message = "Use AttachmentIdentity instead", replaceWith = ReplaceWith("AttachmentIdentity"))
@Keep
@Serializable
class DeletedAttachment : RealmObject {
	/**
	 * The [RealmUUID] of the parent [NoteObject].
	 */
	@Serializable(with = RealmUUIDSerializer::class)
	var parentId: RealmUUID = RealmUUID.random()

	/**
	 * The name of the deleted attachment.
	 */
	var fileName: String = ""

	fun toAttachmentIdentity(): SyncInatorService.Companion.AttachmentMetadata.Companion.AttachmentIdentity =
		SyncInatorService.Companion.AttachmentMetadata.Companion.AttachmentIdentity(fileName, parentId)

	override fun hashCode(): Int {
		var result = parentId.hashCode()
		result = 31 * result + fileName.hashCode()
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is DeletedAttachment) return false

		if (parentId != other.parentId) return false
		if (fileName != other.fileName) return false

		return true
	}

	override fun toString(): String {
		return "$parentId/$fileName"
	}
}

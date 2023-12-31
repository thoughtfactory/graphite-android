package com.syncodec.graphite.di.model.local

import androidx.annotation.Keep
import com.syncodec.graphite.di.cloud.dropbox.DropboxObjectMetadata
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import kotlinx.serialization.Serializable


/**
 * It is used to keep track of deleted objects so that they can be deleted from the cloud.
 * @author pushpull
 * @since 2.3.0
 * @property deletedTimestamp The timestamp of when the object was deleted.
 * @property objectType The type of the deleted object. String representation of T::class.simpleName [NoteObject::class.simpleName]
 */
@Keep
@Serializable
class ObjectIdentity() : RealmObject {

	constructor(id : RealmUUID, deletedTimestamp : Long, objectType : String?) : this() {
		this.id = id
		this.deletedTimestamp = deletedTimestamp
		this.objectType = objectType
	}

	var id : RealmUUID? = null

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

	fun toObjectMetadata() : DropboxObjectMetadata = DropboxObjectMetadata(
		modifiedTimestamp = deletedTimestamp,
		hash = "",
		isDeleted = true
	)

	override fun hashCode(): Int {
		var result = id?.hashCode() ?: 0
		result = 31 * result + deletedTimestamp.hashCode()
		result = 31 * result + (objectType?.hashCode() ?: 0)
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as ObjectIdentity

		return id == other.id
	}
}

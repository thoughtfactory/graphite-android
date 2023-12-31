package com.syncodec.graphite.di.model.local

import androidx.annotation.Keep
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import kotlinx.serialization.Serializable


@Keep
@Serializable
class AttachmentIdentity() : RealmObject {
	constructor(parentId: RealmUUID, fileName: String) : this() {
		this.parentId = parentId
		this.fileName = fileName
	}

	var parentId: RealmUUID? = null
	var fileName: String? = null

	override fun hashCode(): Int {
		var result = parentId.hashCode()
		result = 31 * result + fileName.hashCode()
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as AttachmentIdentity

		if (parentId != other.parentId) return false
		if (fileName != other.fileName) return false

		return true
	}
}

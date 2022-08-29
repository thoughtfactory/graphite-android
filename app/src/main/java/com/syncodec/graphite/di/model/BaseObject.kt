package com.syncodec.graphite.di.model

import androidx.room.PrimaryKey
import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmObject


class BaseObject: RealmObject {
	@PrimaryKey var id: ObjectId = ObjectId.create()
	var defaultChapterId: ObjectId? = null

	override fun hashCode(): Int {
		var result = id.hashCode()
		result = 31 * result + defaultChapterId.hashCode()
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is BaseObject) return false

		if (id != other.id) return false
		if (defaultChapterId != other.defaultChapterId) return false

		return true
	}
}

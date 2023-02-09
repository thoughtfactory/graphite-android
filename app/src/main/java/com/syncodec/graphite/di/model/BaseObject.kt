package com.syncodec.graphite.di.model

import androidx.annotation.Keep
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey


@Keep
class BaseObject : RealmObject {
	@PrimaryKey
	var id : RealmUUID = RealmUUID.random()
	var defaultChapterId : RealmUUID? = null

	var lastSyncedTimestamp : Long = 0

	var notebookIdOrderList : RealmList<RealmUUID> = realmListOf()
	var bucketIdOrderList : RealmList<RealmUUID> = realmListOf()

	var deletedIdList : RealmList<RealmUUID> = realmListOf()

	fun clone() : BaseObject = BaseObject().apply {
		this.id = this@BaseObject.id
		this.defaultChapterId = this@BaseObject.defaultChapterId
		this.lastSyncedTimestamp = this@BaseObject.lastSyncedTimestamp
		this.notebookIdOrderList = this@BaseObject.notebookIdOrderList
		this.bucketIdOrderList = this@BaseObject.bucketIdOrderList
		this.deletedIdList = this@BaseObject.deletedIdList
	}

	override fun hashCode() : Int {
		var result = id.hashCode()
		result = 31 * result + (defaultChapterId?.hashCode() ?: 0)
		result = 31 * result + lastSyncedTimestamp.hashCode()
		result = 31 * result + bucketIdOrderList.hashCode()
		return result
	}

	override fun equals(other : Any?) : Boolean {
		if (this === other) return true
		if (other !is BaseObject) return false

		if (id != other.id) return false
		if (defaultChapterId != other.defaultChapterId) return false
		if (lastSyncedTimestamp != other.lastSyncedTimestamp) return false
		if (bucketIdOrderList != other.bucketIdOrderList) return false

		return true
	}
}

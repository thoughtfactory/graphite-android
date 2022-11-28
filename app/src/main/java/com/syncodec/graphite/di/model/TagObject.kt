package com.syncodec.graphite.di.model

import androidx.compose.ui.graphics.toArgb
import com.syncodec.graphite.utils.getRandomColor
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey


class TagObject : RealmObject {
	@PrimaryKey var id: RealmUUID = RealmUUID.random()

	var tag: String = ""
	var color: Int = getRandomColor().toArgb()

	var objectIdList: RealmList<RealmUUID> = realmListOf()

	fun toLite(): TagObjectLite {
		return TagObjectLite(
			id = id,
			tag = tag,
			color = color
		)
	}

	fun toSnapshot() = TagSnapshot(
		id = this.id.toString(),
		tag = this.tag,
		color = this.color,
		objectIdList = this.objectIdList.map { it.toString() }
	)


	override fun hashCode() : Int {
		var result = id.hashCode()
		result = 31 * result + tag.hashCode()
		result = 31 * result + color
		result = 31 * result + objectIdList.hashCode()
		return result
	}

	override fun equals(other : Any?) : Boolean {
		if (this === other) return true
		if (other !is TagObject) return false

		if (id != other.id) return false
		if (tag != other.tag) return false
		if (color != other.color) return false
		if (objectIdList != other.objectIdList) return false

		return true
	}
}

data class TagObjectLite(
	val id: RealmUUID,
	val tag: String,
	val color: Int
)

data class TagSnapshot(
	val id: String,
	val tag: String,
	val color: Int,
	val objectIdList: List<String>
) {
	fun toObject() = TagObject().apply {
		this.id = RealmUUID.from(this@TagSnapshot.id)
		this.tag = this@TagSnapshot.tag
		this.color = this@TagSnapshot.color
		this.objectIdList.addAll(this@TagSnapshot.objectIdList.map { RealmUUID.from(it) })
	}
}

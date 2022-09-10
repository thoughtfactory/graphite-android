package com.syncodec.graphite.di.model

import androidx.compose.ui.graphics.toArgb
import com.syncodec.graphite.utils.getRandomColor
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject


class TagObject : RealmObject {
	var id: ObjectId = ObjectId.create()

	var tag: String = ""
	var color: Int = getRandomColor().toArgb()

	var noteList: RealmList<NoteObject> = realmListOf()

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is TagObject) return false

		if (id != other.id) return false
		if (tag != other.tag) return false
		if (color != other.color) return false

		return true
	}

	override fun hashCode(): Int {
		var result = id.hashCode()
		result = 31 * result + tag.hashCode()
		result = 31 * result + (color ?: 0)
		return result
	}
}

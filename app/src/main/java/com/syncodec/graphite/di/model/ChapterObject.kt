package com.syncodec.graphite.di.model

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject


class ChapterObject : RealmObject {
	var id: ObjectId = ObjectId.create()

	var createdTimestamp: Long = 0
	var modifiedTimestamp: Long = 0
	var title: String = ""
	var description: String? = null
	var color: Int? = null

	//	var thumbnail: ByteArray? = null
	var isFavourite: Boolean = false
	var isLocked: Boolean = false

	var chapterList: RealmList<ChapterObject> = realmListOf()
	var noteList: RealmList<NoteObject> = realmListOf()

	var parentChapterId: ObjectId? = null

	override fun hashCode(): Int {
		var result = id.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		result = 31 * result + title.hashCode()
		result = 31 * result + (description?.hashCode() ?: 0)
		result = 31 * result + (color ?: 0)
		result = 31 * result + isFavourite.hashCode()
		result = 31 * result + isLocked.hashCode()
		result = 31 * result + chapterList.hashCode()
		result = 31 * result + noteList.hashCode()
		result = 31 * result + parentChapterId.hashCode()
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is ChapterObject) return false

		if (id != other.id) return false
		if (createdTimestamp != other.createdTimestamp) return false
		if (modifiedTimestamp != other.modifiedTimestamp) return false
		if (title != other.title) return false
		if (description != other.description) return false
		if (color != other.color) return false
		if (isFavourite != other.isFavourite) return false
		if (isLocked != other.isLocked) return false
		if (chapterList != other.chapterList) return false
		if (noteList != other.noteList) return false
		if (parentChapterId != other.parentChapterId) return false

		return true
	}
}

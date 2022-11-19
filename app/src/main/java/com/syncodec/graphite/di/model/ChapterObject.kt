package com.syncodec.graphite.di.model

import androidx.compose.ui.graphics.toArgb
import com.syncodec.graphite.utils.getRandomColor
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey


class ChapterObject : RealmObject {
	@PrimaryKey var id : RealmUUID = RealmUUID.random()

	var createdTimestamp : Long = System.currentTimeMillis()
	var modifiedTimestamp : Long = System.currentTimeMillis()
	var title : String? = null
	var description : String? = null
	var color : Int? = getRandomColor().toArgb()
	var thumbnail : String? = null
	var isFavourite : Boolean = false
	var isLocked : Boolean = false

	var chapterList : RealmList<ChapterObject> = realmListOf()
	var noteList : RealmList<NoteObject> = realmListOf()

	var parentChapterId : RealmUUID? = null

	fun toLite() : ChapterObjectLite {
		return ChapterObjectLite(
			id = this.id,
			createdTimestamp = this.createdTimestamp,
			modifiedTimestamp = this.modifiedTimestamp,
			title = this.title,
			description = this.description,
			color = this.color,
			isFavourite = this.isFavourite,
			isLocked = this.isLocked,
			totalChapterDirect = this.chapterList.size,
			totalNoteDirect = this.noteList.size,
			totalChapter = this.countTotalChapter(),
			totalNote = this.countTotalNote(),
			parentChapterId = this.parentChapterId
		)
	}

	fun countTotalChapter() : Int {
		var total = 0
		for (chapter in chapterList) {
			total += chapter.countTotalChapter()
		}
		return total + chapterList.size
	}

	fun countTotalNote() : Int {
		var total = 0
		for (chapter in chapterList) {
			total += chapter.countTotalNote()
		}
		return total + noteList.size
	}

	fun toSnapshot() = ChapterSnapshot(
		id = this.id.toString(),
		createdTimestamp = this.createdTimestamp,
		modifiedTimestamp = this.modifiedTimestamp,
		title = this.title,
		description = this.description,
		color = this.color,
		thumbnail = this.thumbnail,
		isFavourite = this.isFavourite,
		isLocked = this.isLocked,
		chapterList = this.chapterList.map { it.id.toString() },
		noteList = this.noteList.map { it.id.toString() },
		parentChapterId = this.parentChapterId?.toString()
	)

	override fun hashCode() : Int {
		var result = id.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		result = 31 * result + title.hashCode()
		result = 31 * result + (description?.hashCode() ?: 0)
		result = 31 * result + (color ?: 0)
		result = 31 * result + (thumbnail?.hashCode() ?: 0)
		result = 31 * result + isFavourite.hashCode()
		result = 31 * result + isLocked.hashCode()
		result = 31 * result + chapterList.hashCode()
		result = 31 * result + noteList.hashCode()
		result = 31 * result + (parentChapterId?.hashCode() ?: 0)
		return result
	}

	override fun equals(other : Any?) : Boolean {
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

data class ChapterObjectLite(
	val id : RealmUUID,
	val createdTimestamp : Long,
	val modifiedTimestamp : Long,
	val title : String?,
	val description : String?,
	val color : Int?,
	val isFavourite : Boolean,
	val isLocked : Boolean,
	val totalChapterDirect : Int,
	val totalNoteDirect : Int,
	val totalChapter : Int,
	val totalNote : Int,
	val parentChapterId : RealmUUID?
) {
	override fun hashCode() : Int {
		var result = id.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		result = 31 * result + title.hashCode()
		result = 31 * result + (description?.hashCode() ?: 0)
		result = 31 * result + (color ?: 0)
		result = 31 * result + isFavourite.hashCode()
		result = 31 * result + isLocked.hashCode()
		result = 31 * result + (parentChapterId?.hashCode() ?: 0)
		return result
	}

	override fun equals(other : Any?) : Boolean {
		if (this === other) return true
		if (other !is ChapterObjectLite) return false

		if (id != other.id) return false
		if (createdTimestamp != other.createdTimestamp) return false
		if (modifiedTimestamp != other.modifiedTimestamp) return false
		if (title != other.title) return false
		if (description != other.description) return false
		if (color != other.color) return false
		if (isFavourite != other.isFavourite) return false
		if (isLocked != other.isLocked) return false
		if (parentChapterId != other.parentChapterId) return false

		return true
	}
}

data class ChapterSnapshot(
	val id : String,
	val createdTimestamp : Long,
	val modifiedTimestamp : Long,
	val title : String?,
	val description : String?,
	val color : Int?,
	val thumbnail : String?,
	val isFavourite : Boolean,
	val isLocked : Boolean,
	val chapterList : List<String>,
	val noteList : List<String>,
	val parentChapterId : String?
) {
	fun toObject() : ChapterObject = ChapterObject().apply {
		this.id = RealmUUID.from(this@ChapterSnapshot.id)
		this.createdTimestamp = this@ChapterSnapshot.createdTimestamp
		this.modifiedTimestamp = this@ChapterSnapshot.modifiedTimestamp
		this.title = this@ChapterSnapshot.title
		this.description = this@ChapterSnapshot.description
		this.color = this@ChapterSnapshot.color
		this.thumbnail = this@ChapterSnapshot.thumbnail
		this.isFavourite = this@ChapterSnapshot.isFavourite
		this.isLocked = this@ChapterSnapshot.isLocked
		this.parentChapterId = this@ChapterSnapshot.parentChapterId?.let { RealmUUID.from(this@ChapterSnapshot.id) }
	}
}

package com.syncodec.graphite.di.model

import androidx.compose.ui.graphics.toArgb
import com.syncodec.graphite.di.Repository
import com.syncodec.graphite.utils.getRandomColor
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey


class ChapterObject : RealmObject {
	@PrimaryKey var id: ObjectId = ObjectId.create()

	var createdTimestamp: Long = System.currentTimeMillis()
	var modifiedTimestamp: Long = System.currentTimeMillis()
	var title: String = ""
	var description: String? = null
	var color: Int = getRandomColor().toArgb()

	//	var thumbnail: ByteArray? = null
	var isFavourite: Boolean = false
	var isLocked: Boolean = false

	var chapterList: RealmList<ChapterObject> = realmListOf()
	var noteList: RealmList<NoteObject> = realmListOf()

	var parentChapterId: ObjectId? = null

	fun toLite():ChapterObjectLite {
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
	fun getPath(): List<ChapterObjectLite> {
		val path = mutableListOf<ChapterObjectLite>()
		var chapter: ChapterObject? = this
		while (chapter?.parentChapterId != null) {
			path.add(0, chapter.toLite())
			chapter = chapter.parentChapterId?.let { Repository.getChapter(it) }
		}
		return path
	}

	fun countTotalChapter(): Int {
		var total = 0
		for (chapter in chapterList) {
			total += chapter.countTotalChapter()
		}
		return total + chapterList.size
	}

	fun countTotalNote(): Int {
		var total = 0
		for (chapter in chapterList) {
			total += chapter.countTotalNote()
		}
		return total + noteList.size
	}

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

data class ChapterObjectLite(
	val id: ObjectId,
	val createdTimestamp: Long,
	val modifiedTimestamp: Long,
	val title: String,
	val description: String?,
	val color: Int,
	val isFavourite: Boolean,
	val isLocked: Boolean,
	val totalChapterDirect: Int,
	val totalNoteDirect: Int,
	val totalChapter: Int,
	val totalNote: Int,
	val parentChapterId: ObjectId?
) {
	override fun hashCode(): Int {
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

	override fun equals(other: Any?): Boolean {
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

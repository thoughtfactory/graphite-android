package com.syncodec.graphite.database.snapshot

import androidx.documentfile.provider.DocumentFile

data class Snapshot(
	val title: String,
	val notebookCount: Int,
	val chapterCount: Int,
	val noteCount: Int,
	val attachmentCount: Int,
	val bucketCount: Int,
	val bucketItemCount: Int,
	val tagCount: Int,
	val connectionCount: Int,
	val documentFile: DocumentFile
) {
	override fun hashCode(): Int {
		var result = title.hashCode()
		result = 31 * result + notebookCount
		result = 31 * result + chapterCount
		result = 31 * result + noteCount
		result = 31 * result + attachmentCount
		result = 31 * result + bucketCount
		result = 31 * result + bucketItemCount
		result = 31 * result + tagCount
		result = 31 * result + connectionCount
		result = 31 * result + documentFile.hashCode()
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Snapshot

		if (title != other.title) return false
		if (notebookCount != other.notebookCount) return false
		if (chapterCount != other.chapterCount) return false
		if (noteCount != other.noteCount) return false
		if (attachmentCount != other.attachmentCount) return false
		if (bucketCount != other.bucketCount) return false
		if (bucketItemCount != other.bucketItemCount) return false
		if (tagCount != other.tagCount) return false
		if (connectionCount != other.connectionCount) return false
		if (documentFile != other.documentFile) return false

		return true
	}
}

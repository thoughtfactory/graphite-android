package com.syncodec.graphite.di.model.dataExchanger

import androidx.annotation.Keep
import com.syncodec.graphite.di.model.local.LatLng
import com.syncodec.graphite.di.model.local.NoteObject
import io.realm.kotlin.types.RealmUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Keep
@Serializable
sealed class Importable {

	abstract fun toObject(parentId: RealmUUID?): NoteObject

	@Keep
	@Serializable
	data class JourneyNote(
		@SerialName("text") val text: String? = null,
		@SerialName("date_modified") val dateModified: Long? = null,
		@SerialName("date_journal") val dateJournal: Long? = null,
		@SerialName("id") val id: String? = null,
		@SerialName("preview_text") val previewText: String? = null,
		@SerialName("address") val address: String? = null,
		@SerialName("lat") val latitude: Double? = null,
		@SerialName("lon") val longitude: Double? = null,
		@SerialName("favourite") val favourite: Boolean? = null,
		@SerialName("photos") val photos: List<String?>? = null,
		@SerialName("tags") val tags: List<String?>? = null,
	) : Importable() {
		override fun toObject(parentId: RealmUUID?): NoteObject = NoteObject().apply {
			this@JourneyNote.dateJournal?.let { this.createdTimestamp = it; this.userTimestamp = it }
			if (this@JourneyNote.latitude != null && this@JourneyNote.longitude != null) this.setLatLng(LatLng(this@JourneyNote.latitude, this@JourneyNote.longitude))
			this.address = this@JourneyNote.address
			this.content = this@JourneyNote.text
			this.isFavourite = this@JourneyNote.favourite ?: false
			this.parentId = parentId
		}
	}


	@Keep
	@Serializable
	data class GoogleKeepNote(
		@SerialName("color") val color: String? = null,
		@SerialName("isTrashed") val isTrashed: Boolean? = null,
		@SerialName("isPinned") val isPinned: Boolean? = null,
		@SerialName("isArchived") val isArchived: Boolean? = null,
		@SerialName("annotations") val annotations: List<GoogleKeepAnnotation>? = null,
		@SerialName("textContent") val textContent: String? = null,
		@SerialName("listContent") val listContent: List<GoogleKeepListContent>? = null,
		@SerialName("title") val title: String? = null,
		@SerialName("labels") val labels: List<String>? = null,
		@SerialName("attachments") val attachments: List<GoogleKeepAttachment>? = null,
		@SerialName("userEditedTimestampUsec") val userEditedTimestamp: Long? = null,
		@SerialName("creationTimestampUsec") val creationTimestamp: Long? = null,
	) : Importable() {
		private fun encodeToHtml(): String {
			return when {
				this.textContent != null -> "<p class=\"kitkat-paragraph-style\">${this.textContent.replace("\n", "<br />")}</p>"
				this.listContent != null -> {
					val taskList = this.listContent.map { it.toHtmlTask() }
					"<ul class=\"kitkat-task-list-style\" data-type=\"taskList\">\n${taskList.joinToString("\n")}\n</ul>"
				}

				else -> ""
			}
		}

		override fun toObject(parentId: RealmUUID?): NoteObject = NoteObject().apply {
			this@GoogleKeepNote.creationTimestamp?.let { this.createdTimestamp = it; this.userTimestamp = it }
			this.title = this@GoogleKeepNote.title
			this.content = this@GoogleKeepNote.encodeToHtml()
			this.isFavourite = this@GoogleKeepNote.isPinned ?: false
			this.parentId = parentId
		}

		companion object {

			@Keep
			@Serializable
			data class GoogleKeepAnnotation(
				val description: String? = null,
				val source: String? = null,
				val title: String? = null,
				val url: String? = null,
			)

			@Keep
			@Serializable
			data class GoogleKeepAttachment(
				val filePath: String? = null,
				val mimeType: String? = null,
			)

			@Keep
			@Serializable
			data class GoogleKeepListContent(
				val text: String? = null,
				val isChecked: Boolean? = null,
			) {
				fun toHtmlTask(): String {
					return if (this.isChecked == true) {
						"""<li class="kitkat-task-item-style" data-checked="false" data-type="taskItem">
						|<label><input type="checkbox" /><span></span></label>
						|<div><p class="kitkat-paragraph-style">${this.text}</p></div>
						|""".trimMargin()
					} else {
						"""<li class="kitkat-task-item-style" data-checked="true" data-type="taskItem">
						|<label><input type="checkbox" /><span></span></label>
						|<div><p class="kitkat-paragraph-style">${this.text}</p></div>
						""".trimMargin()
					}
				}
			}
		}
	}
}

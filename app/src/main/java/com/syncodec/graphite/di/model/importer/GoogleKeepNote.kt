package com.syncodec.graphite.di.model.importer

import androidx.annotation.Keep
import com.syncodec.graphite.di.model.Attrs
import com.syncodec.graphite.di.model.Content
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class GoogleKeepNote(
	val color : String? = null,
	val isTrashed : Boolean? = null,
	val isPinned : Boolean? = null,
	val isArchived : Boolean? = null,
	val annotations : List<GoogleKeepAnnotation>? = null,
	val textContent : String? = null,
	val listContent : List<GoogleKeepListContent>? = null,
	val title : String? = null,
	val labels : List<String>? = null,
	val attachments : List<GoogleKeepAttachment>? = null,
	val userEditedTimestampUsec : Long? = null,
	val creationTimestampUsec : Long? = null,
) {
	fun encodeToTipTapFormat() : String? {
		if (this.textContent != null) {
			return Content(
				type = "doc",
				content = listOf(
					Content(
						type = "paragraph",
						attrs = Attrs(
							textAlign = Attrs.Companion.TextAlignType.Left.value,
						),
						content = listOf(
							Content(
								type = "text",
								text = this.textContent,
							)
						)
					)
				)
			).toJsonString()
		} else if (this.listContent != null) {
			val taskList = this.listContent.map { it.toTipTapTask() }
			return Content(
				type = "doc",
				content = listOf(
					Content(
						type = "taskList",
						content = taskList,
					)
				)
			).toJsonString()
		} else {
			return Content(
				type = "doc",
				content = listOf()
			).toJsonString()
		}
	}
}

@Keep
@Serializable
data class GoogleKeepAnnotation(
	val description : String? = null,
	val source : String? = null,
	val title : String? = null,
	val url : String? = null,
)

@Keep
@Serializable
data class GoogleKeepAttachment(
	val filePath : String? = null,
	val mimeType : String? = null,
)

@Keep
@Serializable
data class GoogleKeepListContent(
	val text : String? = null,
	val isChecked : Boolean? = null,
) {
	fun toTipTapTask() : Content {
		return Content(
			type = "taskItem",
			attrs = Attrs(
				checked = this.isChecked ?: false,
			),
			content = listOf(
				Content(
					type = "paragraph",
					attrs = Attrs(
						textAlign = Attrs.Companion.TextAlignType.Left.value,
					),
					content = listOf(
						Content(
							type = "text",
							text = this.text,
						)
					)
				)
			)
		)
	}
}

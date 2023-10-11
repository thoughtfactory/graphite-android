package com.syncodec.graphite.presentation.note.kitKat

import android.text.Html
import androidx.annotation.Keep
import kotlinx.serialization.Serializable


@Keep
@Serializable
data class KitKatFormat(
	val bold: Boolean = false,
	val italic: Boolean = false,
	val underline: Boolean = false,
	val strike: Boolean = false,
	val superscript: Boolean = false,
	val subscript: Boolean = false,
	val link: String? = null,
	val code: Boolean = false,

	val alignLeft: Boolean = false,
	val alignCenter: Boolean = false,
	val alignRight: Boolean = false,
	val alignJustify: Boolean = false,

	val blockquote: Boolean = false,
	val codeBlock: Boolean = false,

	val paragraph: Boolean = false,
	val heading1: Boolean = false,
	val heading2: Boolean = false,
	val heading3: Boolean = false,
	val heading4: Boolean = false,
	val heading5: Boolean = false,
	val heading6: Boolean = false,

	val bulletList: Boolean = false,
	val orderedList: Boolean = false,
	val taskList: Boolean = false,

	val characterCount: Int = 0,
	val wordCount: Int = 0,
	val textColor: String? = null,
	val highlightColor: String? = null,

	val fontSize: String = "12px",
	val fontFamily: String = "Open Sans",

	val currentSelection: Int = 0,

	val kitKatTitle: String? = null,
	val kitKatContent: String? = null,
) {
	fun getPlainString(): String = Html.fromHtml(kitKatContent ?: "", Html.FROM_HTML_MODE_LEGACY)?.toString()?.drop(1)?.dropLast(1)?.take(512)?.replace("\n\n", "\n")?.trimEnd { it == '\n' } ?: ""
	override fun hashCode(): Int {
		var result = bold.hashCode()
		result = 31 * result + italic.hashCode()
		result = 31 * result + underline.hashCode()
		result = 31 * result + strike.hashCode()
		result = 31 * result + superscript.hashCode()
		result = 31 * result + subscript.hashCode()
		result = 31 * result + (link?.hashCode() ?: 0)
		result = 31 * result + code.hashCode()
		result = 31 * result + alignLeft.hashCode()
		result = 31 * result + alignCenter.hashCode()
		result = 31 * result + alignRight.hashCode()
		result = 31 * result + alignJustify.hashCode()
		result = 31 * result + blockquote.hashCode()
		result = 31 * result + codeBlock.hashCode()
		result = 31 * result + paragraph.hashCode()
		result = 31 * result + heading1.hashCode()
		result = 31 * result + heading2.hashCode()
		result = 31 * result + heading3.hashCode()
		result = 31 * result + heading4.hashCode()
		result = 31 * result + heading5.hashCode()
		result = 31 * result + heading6.hashCode()
		result = 31 * result + bulletList.hashCode()
		result = 31 * result + orderedList.hashCode()
		result = 31 * result + taskList.hashCode()
		result = 31 * result + characterCount
		result = 31 * result + wordCount
		result = 31 * result + (textColor?.hashCode() ?: 0)
		result = 31 * result + (highlightColor?.hashCode() ?: 0)
		result = 31 * result + fontSize.hashCode()
		result = 31 * result + fontFamily.hashCode()
		result = 31 * result + currentSelection
		result = 31 * result + (kitKatTitle?.hashCode() ?: 0)
		result = 31 * result + (kitKatContent?.hashCode() ?: 0)
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as KitKatFormat

		if (bold != other.bold) return false
		if (italic != other.italic) return false
		if (underline != other.underline) return false
		if (strike != other.strike) return false
		if (superscript != other.superscript) return false
		if (subscript != other.subscript) return false
		if (link != other.link) return false
		if (code != other.code) return false
		if (alignLeft != other.alignLeft) return false
		if (alignCenter != other.alignCenter) return false
		if (alignRight != other.alignRight) return false
		if (alignJustify != other.alignJustify) return false
		if (blockquote != other.blockquote) return false
		if (codeBlock != other.codeBlock) return false
		if (paragraph != other.paragraph) return false
		if (heading1 != other.heading1) return false
		if (heading2 != other.heading2) return false
		if (heading3 != other.heading3) return false
		if (heading4 != other.heading4) return false
		if (heading5 != other.heading5) return false
		if (heading6 != other.heading6) return false
		if (bulletList != other.bulletList) return false
		if (orderedList != other.orderedList) return false
		if (taskList != other.taskList) return false
		if (characterCount != other.characterCount) return false
		if (wordCount != other.wordCount) return false
		if (textColor != other.textColor) return false
		if (highlightColor != other.highlightColor) return false
		if (fontSize != other.fontSize) return false
		if (fontFamily != other.fontFamily) return false
		if (currentSelection != other.currentSelection) return false
		if (kitKatTitle != other.kitKatTitle) return false
		if (kitKatContent != other.kitKatContent) return false

		return true
	}
}

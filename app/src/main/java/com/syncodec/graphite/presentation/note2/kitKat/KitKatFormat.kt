package com.syncodec.graphite.presentation.note2.kitKat

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
)

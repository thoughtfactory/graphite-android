package com.syncodec.momento.noteComponent.miscellaneous

import android.webkit.JavascriptInterface
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

class TiptapInterface {
	private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	interface OnFormatUpdateListener {
		fun onFormatUpdate(newTextFormat: TextFormat)
	}

	private var onFormatUpdateListener: OnFormatUpdateListener? = null
	fun setOnFormatUpdate(listener: OnFormatUpdateListener) {
		onFormatUpdateListener = listener
	}

	interface OnSaveDataListener {
		fun onSaveData(data: String)
	}

	private var onSaveDataListener: OnSaveDataListener? = null
	fun setOnSaveData(listener: OnSaveDataListener) {
		onSaveDataListener = listener
	}

	@JavascriptInterface
	fun onCreate() {
	}

	@JavascriptInterface
	fun format(textFormatJsonString: String) {
		val newTextFormat: TextFormat = objectMapper.readValue(textFormatJsonString)
		onFormatUpdateListener?.onFormatUpdate(newTextFormat)
	}

	@JavascriptInterface
	fun getData(data: String) {
		onSaveDataListener?.onSaveData(data)
	}

	data class TextFormat(
		val bold: Boolean = false,
		val italic: Boolean = false,
		val underline: Boolean = false,
		val strike: Boolean = false,
		val superscript: Boolean = false,
		val subscript: Boolean = false,

		val textAlignLeft: Boolean = false,
		val textAlignCenter: Boolean = false,
		val textAlignRight: Boolean = false,
		val textAlignJustify: Boolean = false,

		val link: String? = null,

		val blockquote: Boolean = false,
		val code: Boolean = false,
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

		val startOffset: Int = -1,
		val endOffset: Int = -1
	)
}

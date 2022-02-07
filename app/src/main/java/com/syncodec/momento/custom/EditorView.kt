package com.syncodec.momento.custom

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.*
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.json.JSONObject

@SuppressLint("JavascriptInterface")
class EditorView(context: Context) : WebView(context) {
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

	private var isReady: Boolean = false

	init {
		isVerticalScrollBarEnabled = false
		isHorizontalScrollBarEnabled = false
		settings.javaScriptEnabled = true
		webViewClient = WebViewClient()

		webChromeClient = object : WebChromeClient() {
			override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
				Log.d(TAG, consoleMessage.message())
				return true
			}
		}

		settings.allowFileAccess = true

		setBackgroundColor(0)
		setLayerType(LAYER_TYPE_SOFTWARE, null)

		addJavascriptInterface(this, "bridge")

		loadUrl(INDEX_PATH)
		isReady = url.equals(INDEX_PATH, ignoreCase = true)
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

	private fun load(trigger: String) {
		evaluateJavascript(trigger) { result ->
			run {
				Log.i("JS", result)
			}
		}
	}

	fun exec(trigger: String) {
		Log.i(TAG, "isReady : $isReady")
		if (isReady) {
			load(trigger)
		} else {
			postDelayed({ exec(trigger) }, 100)
		}
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

	companion object {
		const val TAG = "EDITOR_VIEW"
		const val INDEX_PATH = "file:///android_asset/tiptap/index.html"
	}
}

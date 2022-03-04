package com.syncodec.momento.custom.richText

import android.content.Context
import android.util.Log
import android.webkit.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue


class RichTextEditor(context: Context) : WebView(context) {
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

	var isReady: MutableState<Boolean> = mutableStateOf(false)
	var currentSelection: Int = 0

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
	}

	private fun load(trigger: String) {
		evaluateJavascript(trigger) { result ->
			run {
				Log.i("JS", result)
			}
		}
	}

	fun exec(trigger: String, blur: Boolean = true) {
		Log.i(TAG, "isReady : ${isReady.value}")
		if (isReady.value) {
			if (blur) {
				clearFocus()
				load(trigger)
				requestFocus()
			} else {
				load(trigger)
			}
		} else {
			postDelayed({ exec(trigger) }, 100)
		}
	}

	@JavascriptInterface
	fun onCreate() {
		isReady.value = true
	}

	@JavascriptInterface
	fun format(textFormatJsonString: String) {
		val newTextFormat: TextFormat = objectMapper.readValue(textFormatJsonString)
		onFormatUpdateListener?.onFormatUpdate(newTextFormat)
		currentSelection = newTextFormat.currentSelection
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

		val alignLeft: Boolean = false,
		val alignCenter: Boolean = false,
		val alignRight: Boolean = false,
		val alignJustify: Boolean = false,

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

		val currentSelection: Int = 0
	)

	companion object {
		const val TAG = "RICH_TEXT_EDITOR"
		const val INDEX_PATH = "file:///android_asset/tiptap/index.html"
	}
}

@Composable
fun rememberRichTextEditorWithLifecycle(): RichTextEditor {
	val context = LocalContext.current
	val richTextEditor = remember { RichTextEditor(context) }

	val lifecycleObserver = rememberRichTextEditorLifecycleObserver(richTextEditor)
	val lifecycle = LocalLifecycleOwner.current.lifecycle
	DisposableEffect(lifecycle) {
		lifecycle.addObserver(lifecycleObserver)
		onDispose {
			lifecycle.removeObserver(lifecycleObserver)
		}
	}

	return richTextEditor
}

@Composable
fun rememberRichTextEditorLifecycleObserver(richTextEditor: RichTextEditor): LifecycleEventObserver =
	remember(richTextEditor) {
		LifecycleEventObserver { _, event ->
			when (event) {
				Lifecycle.Event.ON_RESUME -> richTextEditor.onResume()
				Lifecycle.Event.ON_PAUSE -> richTextEditor.onPause()
			}
		}
	}

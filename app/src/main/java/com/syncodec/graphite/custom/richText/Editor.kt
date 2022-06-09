package com.syncodec.graphite.custom.richText

import android.content.Context
import android.webkit.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.syncodec.graphite.miscellaneous.DataStoreInstance
import com.syncodec.graphite.miscellaneous.toHexString
import kotlinx.coroutines.*


class RichTextEditor(context: Context, val textColor: String, typography: Int?) : WebView(context) {
	private val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

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

	interface OnPrintDataListener {
		fun onPrintData(data: String)
	}

	private var onPrintDataListener: OnPrintDataListener? = null
	fun setOnPrintData(listener: OnPrintDataListener) {
		onPrintDataListener = listener
	}

	interface OnGetTextListener {
		fun onGetPlainText(data: String)
	}

	private var onGetPlainTextListener: OnGetTextListener? = null
	fun setOnPlainGetText(listener: OnGetTextListener) {
		onGetPlainTextListener = listener
	}

	var isReady: MutableState<Boolean> = mutableStateOf(false)
	var currentSelection: Int = 0

	init {
		isVerticalScrollBarEnabled = false
		isHorizontalScrollBarEnabled = false

		settings.javaScriptEnabled = true
		settings.domStorageEnabled = true

		webViewClient = WebViewClient()

		webChromeClient = object : WebChromeClient() {
			override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
				return true
			}
		}

		setBackgroundColor(0)
		setLayerType(LAYER_TYPE_SOFTWARE, null)

		addJavascriptInterface(this, "bridge")

		loadUrl(INDEX_PATH)
		exec("editor.setBaseFontColor('$textColor');")
		when (typography) {
			0 -> exec("editor.setBaseFontFamily(\"overlock\");")
			1 -> exec("editor.setBaseFontFamily(\"source_sans_pro\");")
			2 -> exec("editor.setBaseFontFamily(\"ubuntu\");")
			3 -> exec("editor.setBaseFontFamily('atwriter');")
			else -> exec("editor.setBaseFontFamily(\"source_sans_pro\");")
		}
	}

	private fun load(trigger: String) {
		evaluateJavascript(trigger) { result ->
			run {
			}
		}
	}

	fun exec(trigger: String) {
		CoroutineScope(Dispatchers.IO).launch {
			while (true) {
				try {
					if (isReady.value) break
				} catch (exception: Exception) {
				}
				delay(400)
			}
			withContext(Dispatchers.Main) {
				load(trigger)
			}
		}
	}

	@JavascriptInterface
	fun onCreate() {
		isReady.value = true
	}

	@JavascriptInterface
	fun format(textFormatJsonString: String) {
		try {
			val newTextFormat: TextFormat = objectMapper.readValue(textFormatJsonString)
			onFormatUpdateListener?.onFormatUpdate(newTextFormat)
			currentSelection = newTextFormat.currentSelection
		} catch (exception: Exception) {
			
		}
	}

	@JavascriptInterface
	fun saveData(data: String) {
		onSaveDataListener?.onSaveData(data)
	}

	@JavascriptInterface
	fun printData(data: String) {
		onPrintDataListener?.onPrintData(data = data)
	}

	@JavascriptInterface
	fun getPlainText(data: String) {
		onGetPlainTextListener?.onGetPlainText(data = data)
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
		const val INDEX_PATH = "file:///android_asset/dropper/index.html"
	}
}

@Composable
fun rememberRichTextEditorWithLifecycle(): RichTextEditor {
	val context = LocalContext.current
	val textColor = MaterialTheme.colorScheme.onBackground.toHexString()
	val typography by DataStoreInstance(context).getTypography.collectAsState(initial = null)

	val richTextEditor = remember { RichTextEditor(context, textColor, typography) }

	val lifecycleObserver = rememberRichTextEditorLifecycleObserver(richTextEditor)
	val lifecycle = LocalLifecycleOwner.current.lifecycle
	DisposableEffect(lifecycle) {
		lifecycle.addObserver(lifecycleObserver)
		onDispose { lifecycle.removeObserver(lifecycleObserver) }
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

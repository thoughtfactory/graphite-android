package com.syncodec.graphite.presentation.common.richText

import android.content.Context
import android.util.Log
import android.webkit.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.syncodec.graphite.utils.toHexString
import kotlinx.coroutines.*


class RichTextEditor(context : Context, val containerColor : Color, contentColor : Color, screenHeightPx : Int, typography : Int?) : WebView(context) {
	private val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	interface FormatUpdateListener {
		fun onFormatUpdate(newTextFormat : TextFormat)
	}

	interface GetTextListener {
		fun onGetData(extra : String?, data : String?)
	}

	private var formatUpdateListener : FormatUpdateListener? = null
	private var getTextListener : GetTextListener? = null

	fun setFormatUpdateListener(listener : FormatUpdateListener) {
		formatUpdateListener = listener
	}

	fun setGetTextListener(listener : GetTextListener) {
		getTextListener = listener
	}

	var isReady : MutableState<Boolean> = mutableStateOf(false)
	var currentSelection : Int = 0

	init {
		isVerticalScrollBarEnabled = false
		isHorizontalScrollBarEnabled = false

		settings.javaScriptEnabled = true
		settings.domStorageEnabled = true

		webViewClient = WebViewClient()

		webChromeClient = object : WebChromeClient() {
			override fun onConsoleMessage(consoleMessage : ConsoleMessage) : Boolean {
				Log.d("npr71 : RichTextEditor", consoleMessage.message())
				return true
			}
		}

		setWebContentsDebuggingEnabled(true)

		setBackgroundColor(0)
		setLayerType(LAYER_TYPE_SOFTWARE, null)

		addJavascriptInterface(this, "bridge")

//		loadUrl(INDEX_PATH)
//		loadData()

		context.assets.open("dropper/index.html").let {
			val buffer = ByteArray(it.available())
			it.read(buffer)
			it.close()
			val html = String(buffer)
			loadDataWithBaseURL("file:///android_asset/dropper", html, "text/html", "UTF-8", null)
		}

		exec("editor.setBaseColor('${containerColor.toHexString()}', '${contentColor.toHexString()}');")

//		exec("editor.reCalculateHeight($screenHeightPx);")

		when (typography) {
			0 -> exec("editor.setBaseFontFamily(\"overlock\");")
			1 -> exec("editor.setBaseFontFamily(\"source_sans_pro\");")
			2 -> exec("editor.setBaseFontFamily(\"ubuntu\");")
			3 -> exec("editor.setBaseFontFamily(\"atwriter\");")
			else -> exec("editor.setBaseFontFamily(\"source_sans_pro\");")
		}

	}

	private fun load(trigger : String) {
		evaluateJavascript(trigger) { result ->
			run {
			}
		}
	}

	fun exec(trigger : String) {
		CoroutineScope(Dispatchers.IO).launch {
			while (true) {
				try {
					if (isReady.value) break
				} catch (exception : Exception) {
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
	fun format(textFormatJsonString : String) {
		try {
			val newTextFormat : TextFormat = objectMapper.readValue(textFormatJsonString)
			formatUpdateListener?.onFormatUpdate(newTextFormat)
			currentSelection = newTextFormat.currentSelection
		} catch (_ : Exception) {
		}
	}

	@JavascriptInterface
	fun getData(extra : String?, data : String?) {
		try {
			getTextListener?.onGetData(extra, data)
		} catch (_ : Exception) {
		}
	}

	data class TextFormat(
		val bold : Boolean = false,
		val italic : Boolean = false,
		val underline : Boolean = false,
		val strike : Boolean = false,
		val superscript : Boolean = false,
		val subscript : Boolean = false,

		val alignLeft : Boolean = false,
		val alignCenter : Boolean = false,
		val alignRight : Boolean = false,
		val alignJustify : Boolean = false,

		val link : String? = null,

		val blockquote : Boolean = false,
		val code : Boolean = false,
		val codeBlock : Boolean = false,

		val paragraph : Boolean = false,
		val heading1 : Boolean = false,
		val heading2 : Boolean = false,
		val heading3 : Boolean = false,
		val heading4 : Boolean = false,
		val heading5 : Boolean = false,
		val heading6 : Boolean = false,

		val bulletList : Boolean = false,
		val orderedList : Boolean = false,
		val taskList : Boolean = false,

		val characterCount : Int = 0,
		val wordCount : Int = 0,
		val textColor : String? = null,
		val highlightColor : String? = null,

		val fontSize : String = "12px",
		val fontFamily : String = "Open Sans",

		val currentSelection : Int = 0
	)

	companion object {
		const val TAG = "RICH_TEXT_EDITOR"
		const val INDEX_PATH = "file:///android_asset/dropper/index.html"
	}
}

@Composable
fun rememberRichTextEditor() : RichTextEditor {
	val context = LocalContext.current
	val configuration = LocalConfiguration.current
	val screenHeight = (configuration.screenHeightDp.dp.value * 0.8).toInt()

	val density = LocalDensity.current

	val screenHeightPx = with(density) { configuration.screenHeightDp.dp.roundToPx() }

	val containerColor = MaterialTheme.colorScheme.background
	val contentColor = MaterialTheme.colorScheme.onBackground
//	val typography by DataStoreInstance(context).getTypography.collectAsState(initial = null)
	val typography = null

	val richTextEditor = remember { RichTextEditor(context, containerColor, contentColor, screenHeight, typography) }

	val lifecycleObserver = rememberRichTextEditorLifecycleObserver(richTextEditor)
	val lifecycle = LocalLifecycleOwner.current.lifecycle
	DisposableEffect(lifecycle) {
		lifecycle.addObserver(lifecycleObserver)
		onDispose { lifecycle.removeObserver(lifecycleObserver) }
	}

	return richTextEditor
}

@Composable
fun rememberRichTextEditorLifecycleObserver(richTextEditor : RichTextEditor) : LifecycleEventObserver =
	remember(richTextEditor) {
		LifecycleEventObserver { _, event ->
			when (event) {
				Lifecycle.Event.ON_RESUME -> richTextEditor.onResume()
				Lifecycle.Event.ON_PAUSE -> richTextEditor.onPause()
				else -> null
			}
		}
	}

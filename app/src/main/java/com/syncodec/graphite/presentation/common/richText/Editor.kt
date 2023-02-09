package com.syncodec.graphite.presentation.common.richText

import android.content.Context
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.Keep
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
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
import com.syncodec.graphite.utils.alice.Alice
import com.syncodec.graphite.utils.toHexString
import io.github.esentsov.FilePrivate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class RichTextEditor(context : Context, val containerColor : Color, contentColor : Color, screenHeightPx : Int, typography : Int?) : WebView(context) {
	private val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)


	/**
	 * Override [GetTextListener] to get data from the editor.
	 *
	 * @author pushpull
	 * @since 2.2.0
	 * @param requestData Can be used to identify the type of data.
	 * @param data Handle according to [requestData].
	 *
	 * @see [getData]
	 */
	interface GetTextListener {
		fun onGetData(requestData : RequestData, data : String?)
	}

	private var getTextListener : GetTextListener? = null

	fun setGetTextListener(listener : GetTextListener) {
		getTextListener = listener
	}

	/** Set by [onReady] when TipTap is ready to use. Observe this and update UI accordingly.*/
	private var _isReady : MutableStateFlow<Boolean> = MutableStateFlow(false)
	val isReady : StateFlow<Boolean> = _isReady

	val textFormat = MutableStateFlow(TextFormat())
	var currentSelection : Int = 0

	init {
		isVerticalScrollBarEnabled = false
		isHorizontalScrollBarEnabled = false

		settings.javaScriptEnabled = true
		settings.domStorageEnabled = true
		settings.setRenderPriority(WebSettings.RenderPriority.HIGH)

		webViewClient = WebViewClient()

		webChromeClient = object : WebChromeClient() {
			override fun onConsoleMessage(consoleMessage : ConsoleMessage) : Boolean {
//				Log.d("npr71 : RichTextEditor", consoleMessage.message())
				return true
			}
		}

		setBackgroundColor(0)
		setLayerType(LAYER_TYPE_SOFTWARE, null)

		addJavascriptInterface(this, "bridge")

		context.assets.open("orbit/orbital").let {
			val buffer = ByteArray(it.available())
			it.read(buffer)
			it.close()
			val encHtml = String(buffer)
			val passcode = "d5Y3f8*hN8%c%Q3%Jb9vU^8R4MV@z^9*"

			CoroutineScope(Dispatchers.IO).launch {
				try {
					Alice.decrypt(encHtml, passcode).let { html ->
						withContext(Dispatchers.Main) {
							if (html == null) Toast.makeText(context, "Error loading editor", Toast.LENGTH_LONG).show()
							else loadDataWithBaseURL("file:///android_asset/orbit", html, "text/html", "UTF-8", null)
						}
					}
				} catch (e : Exception) {
					withContext(Dispatchers.Main) {
						Toast.makeText(context, "Error loading editor", Toast.LENGTH_LONG).show()
					}
				}
			}
		}

		exec("editor.setBaseColor('${containerColor.toHexString()}', '${contentColor.toHexString()}');")

		when (typography) {
			0 -> exec("editor.setBaseFontFamily(\"overlock\");")
			1 -> exec("editor.setBaseFontFamily(\"source_sans_pro\");")
			2 -> exec("editor.setBaseFontFamily(\"ubuntu\");")
			3 -> exec("editor.setBaseFontFamily(\"atwriter\");")
			else -> exec("editor.setBaseFontFamily(\"source_sans_pro\");")
		}
	}

	private fun load(trigger : String) = evaluateJavascript(trigger) { result -> }

	fun importData(importFrom : ImportFrom, noteId : String, data : String) {
		when (importFrom) {
			ImportFrom.Journey -> exec("editor.importData(\"$noteId\", $data, \"journey\");")
		}
	}

	private fun exec(trigger : String) {
		CoroutineScope(Dispatchers.Default).launch {
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

	/**
	 * Set content in TipTap. Do not call this method before the editor [isReady].
	 * @param content Must be a JSON string in TipTap format.
	 * @author pushpull
	 * @since 2.2.0
	 * */
	fun setData(title : String?, content : String?) = exec("editor.setData(\"${title ?: ""}\", ${content});")
	fun onEditorAction(editorAction : EditorAction) = editorActionExecMap[editorAction]?.let { exec(it) }
	fun save() = exec("editor.getData(\"${RequestData.Save.name}\");")

	/**
	 * Exposed to JS for TipTap to callback when it is ready to use. This is deeply coupled to the JS code as well as how [exec] is called.
	 *
	 * This method is not supposed to be called from anywhere except JS.
	 * @author pushpull
	 * @since 2.2.0
	 * @see [setData]
	 */
	@FilePrivate
	@JavascriptInterface
	fun onReady() = _isReady.tryEmit(true)

	@FilePrivate
	@JavascriptInterface
	fun format(textFormatJsonString : String) {
		try {
			val newTextFormat : TextFormat = objectMapper.readValue(textFormatJsonString)
			textFormat.tryEmit(newTextFormat)
			currentSelection = newTextFormat.currentSelection
		} catch (_ : Exception) {
		}
	}

	/**
	 * Exposed to JS for TipTap to callback when [save] is called. This is deeply coupled to the JS code as well as how [exec] is called.
	 *
	 * This method is not supposed to be called from anywhere except JS.
	 * @author pushpull
	 * @since 2.2.0
	 * @param requestData Can be used to identify the type of data.
	 * @param data Handle according to [requestData].
	 * Can have following extra:
	 *  *   [RequestData.Save] : JSON string. Contains dataJson, dataText and title. dataJson is the JSON string of the document provided and readable by TipTap.
	 *  *   [RequestData.Share] : JSON string. Contains dataText.
	 *  *   [RequestData.ExportText] : JSON string. Contains dataText.
	 *  *   [RequestData.ExportPdf] : JSON string. Contains dataText.
	 *  *   [RequestData.ExportHtml] : JSON string. Contains dataHtml.
	 *  *   [RequestData.ExportMarkdown] : JSON string. Contains dataText.
	 * @see [GetTextListener.onGetData]
	 */
	@FilePrivate
	@JavascriptInterface
	fun getData(requestData : String?, data : String?) {
		try {
			RequestData.values().find { it.name == requestData }?.let { getTextListener?.onGetData(it, data) }
		} catch (_ : Exception) {
		}
	}

	companion object {
		@Keep
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

		enum class EditorAction {
			Undo,
			Redo,
			Bold,
			Italic,
			Underline,
			StrikeThrough,
			HardLineBreak,
			CheckList,
			BulletList,
			OrderedList,
			Paragraph,
			Heading1,
			Heading2,
			Heading3,
			Heading4,
			Heading5,
			Heading6,
			Blockquote,
			Indent,
			Outdent,
			Superscript,
			Subscript,
		}

		val editorActionExecMap : Map<EditorAction, String> = mapOf(
			EditorAction.Undo to "editor.commands.undo();",
			EditorAction.Redo to "editor.commands.redo();",
			EditorAction.Bold to "editor.chain().focus().toggleBold().run()",
			EditorAction.Italic to "editor.chain().focus().toggleItalic().run()",
			EditorAction.Underline to "editor.chain().focus().toggleUnderline().run()",
			EditorAction.StrikeThrough to "editor.chain().focus().toggleStrike().run()",
			EditorAction.HardLineBreak to "editor.chain().focus().setHardBreak().run()",
			EditorAction.CheckList to "editor.commands.toggleTaskList();",
			EditorAction.BulletList to "editor.commands.toggleBulletList();",
			EditorAction.OrderedList to "editor.commands.toggleOrderedList();",
			EditorAction.Paragraph to "editor.commands.toggleHeading({ level: 3 });",
			EditorAction.Heading1 to "editor.commands.toggleHeading({ level: 1 });",
			EditorAction.Heading2 to "editor.commands.toggleHeading({ level: 2 });",
			EditorAction.Heading3 to "editor.commands.toggleHeading({ level: 3 });",
			EditorAction.Heading4 to "editor.commands.toggleHeading({ level: 4 });",
			EditorAction.Heading5 to "editor.commands.toggleHeading({ level: 5 });",
			EditorAction.Heading6 to "editor.commands.toggleHeading({ level: 6 });",
			EditorAction.Blockquote to "editor.chain().focus().toggleBlockquote().run();",
			EditorAction.Indent to "editor.chain().focus().sinkListItem('listItem').run()",
			EditorAction.Outdent to "editor.chain().focus().liftListItem('listItem').run()",
			EditorAction.Superscript to "editor.chain().focus().toggleSuperscript().run();",
			EditorAction.Subscript to "editor.chain().focus().toggleSubscript().run();",
		)

		/**
		 * Ensure that [RequestData] is consistent with TipTap.
		 * @author pushpull
		 * @since 2.2.0
		 */
		enum class RequestData {
			Save,
			Share,
			ExportText,
			ExportPdf,
			ExportHtml,
			ExportMarkdown,
			ImportJourney
		}

		enum class ImportFrom {
			Journey
		}
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

	val richTextEditor : RichTextEditor = remember { RichTextEditor(context, containerColor, contentColor, screenHeight, typography) }

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
				Lifecycle.Event.ON_DESTROY -> {
					richTextEditor.removeAllViews()
					richTextEditor.clearHistory()
					richTextEditor.clearCache(true)
					richTextEditor.loadUrl("about:blank")
					richTextEditor.onPause()
					richTextEditor.removeAllViews()
					richTextEditor.destroyDrawingCache()
					richTextEditor.pauseTimers()
					richTextEditor.destroy()
				}

				else -> null
			}
		}
	}

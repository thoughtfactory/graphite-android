package com.syncodec.graphite.presentation.note2

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.annotation.Keep
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.utils.alice.Alice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONObject


class KitKat(
	context: Context
) : WebView(context) {

	private val json = Json {
		ignoreUnknownKeys = true
	}

	private val _isReady: MutableStateFlow<Boolean> = MutableStateFlow(false)
	val isReady: StateFlow<Boolean> = _isReady

	private val _kitKatFormat: MutableStateFlow<KitKatFormat> = MutableStateFlow(KitKatFormat())
	val kitKatFormat: StateFlow<KitKatFormat> = _kitKatFormat

	init {
		isVerticalScrollBarEnabled = false
		isHorizontalScrollBarEnabled = false


		settings.javaScriptEnabled = true
		settings.javaScriptCanOpenWindowsAutomatically = false

		settings.domStorageEnabled = true

		settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
//		setLayerType(LAYER_TYPE_HARDWARE, null)
		setBackgroundColor(0)   //  Transparent

		setWebContentsDebuggingEnabled(BuildConfig.DEBUG)

		addJavascriptInterface(this, "bridge")
	}

	fun loadKitKat(): Boolean {
		val orbitalInputStream = context.assets.open("orbit/orbital")
		val encHtml = String(orbitalInputStream.readBytes())
		orbitalInputStream.close()
		val passcode = "d5Y3f8*hN8%c%Q3%Jb9vU^8R4MV@z^9*"

		try {
			val plain = Alice.decrypt(encHtml, passcode)
			plain?.let {
				loadDataWithBaseURL("file:///orbit", it, "text/html", "UTF-8", null)
				return true
			} ?: return false
		} catch (_: Exception) {
			return false
		}
	}

	fun loadExternalEditor(): Boolean {
		context.assets.open("orbit/oneindex.html").let {
			val html = String(it.readBytes())
			it.close()
			try {
				loadDataWithBaseURL("file:///orbit", html, "text/html", "UTF-8", null)
				return true
			} catch (_: Exception) {
				return false
			}
		}
	}

	fun print(noteId: String) {
		val printManager = context.getSystemService(PrintManager::class.java)
		val printAdapter = createPrintDocumentAdapter(noteId)
		val printAttributes = PrintAttributes.Builder().build()
		printManager.print(noteId, printAdapter, printAttributes)
	}

	//	Outgoing calls
	//  Callback strings are escaped. Use StringEscapeUtils.unescapeJava later
	private fun load(trigger: String, callback: (String) -> Unit = {}) {
		evaluateJavascript(trigger, callback)
	}

	private fun execAsync(trigger: String, callback: (String) -> Unit = {}) {
		findViewTreeLifecycleOwner()?.lifecycleScope?.launch(Dispatchers.Main) {
			while (true) {
				if (isReady.value) break
				delay(400)
			}
			load(trigger, callback)
		}
	}

	private fun exec(trigger: String, callback: (String) -> Unit = {}) {
		try {
			load(trigger, callback)
		} catch (_: Exception) {
		}
	}

	fun onKitKatActionAsync(editorAction: KitKatAction) = execAsync(trigger = editorAction.action, callback = editorAction.callback)

	fun onKitKatAction(editorAction: KitKatAction) = exec(trigger = editorAction.action, callback = editorAction.callback)


//	Incoming calls

	/**
	 * Callback from kitkat when it is ready
	 */
	@JavascriptInterface
	fun onReady() {
		_isReady.tryEmit(true)
	}

	/**
	 * Continuous callback from kitkat when format updates
	 */
	@JavascriptInterface
	fun format(kitKatFormatJsonString: String) {
		try {
			_kitKatFormat.tryEmit(json.decodeFromString(kitKatFormatJsonString))
//			Log.d("npr71", _kitKatFormat.value.toString())
//			Log.d("npr71", kitKatFormatJsonString)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	@JavascriptInterface
	fun titleUpdate(title: String?) {
		_kitKatFormat.tryEmit(kitKatFormat.value.copy(kitKatTitle = title))
	}

	companion object {

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

		sealed class KitKatAction(val action: String, val callback: (String) -> Unit = {}) {
			data object Undo : KitKatAction(action = "editor.commands.undo();")
			data object Redo : KitKatAction(action = "editor.commands.redo();")
			data object Bold : KitKatAction(action = "editor.chain().focus().toggleBold().run()")
			data object Italic : KitKatAction(action = "editor.chain().focus().toggleItalic().run()")
			data object Underline : KitKatAction(action = "editor.chain().focus().toggleUnderline().run()")
			data object StrikeThrough : KitKatAction(action = "editor.chain().focus().toggleStrike().run();")
			data object Superscript : KitKatAction(action = "editor.chain().focus().toggleSuperscript().run()")
			data object Subscript : KitKatAction(action = "editor.chain().focus().toggleSubscript().run();")
			data object HardLineBreak : KitKatAction(action = "editor.chain().focus().setHardBreak().run()")
			data object HorizontalRule : KitKatAction(action = "editor.chain().focus().setHorizontalRule().run()")
			sealed class List(action2: String) : KitKatAction(action = action2) {
				data object CheckList : List(action2 = "editor.commands.toggleTaskList();")
				data object BulletList : List(action2 = "editor.commands.toggleBulletList();")
				data object OrderedList : List(action2 = "editor.commands.toggleOrderedList();")
			}

			sealed class Heading(val action2: String) : KitKatAction(action = action2) {
				data object Paragraph : Heading(action2 = "editor.commands.toggleHeading({ level: 3 });")
				data object Heading1 : Heading(action2 = "editor.commands.toggleHeading({ level: 1 });")
				data object Heading2 : Heading(action2 = "editor.commands.toggleHeading({ level: 2 });")
				data object Heading3 : Heading(action2 = "editor.commands.toggleHeading({ level: 3 });")
				data object Heading4 : Heading(action2 = "editor.commands.toggleHeading({ level: 4 });")
				data object Heading5 : Heading(action2 = "editor.commands.toggleHeading({ level: 5 });")
				data object Heading6 : Heading(action2 = "editor.commands.toggleHeading({ level: 6 });")
			}

			data object Blockquote : KitKatAction(action = "editor.chain().focus().toggleBlockquote().run();")
			data object Indent : KitKatAction(action = "editor.chain().focus().sinkListItem('listItem').run()")
			data object Outdent : KitKatAction(action = "editor.chain().focus().liftListItem('listItem').run()")

			sealed class Link(val action2: String) : KitKatAction(action = action2) {
				data class Set(val url: String) : Link(action2 = "editor.commands.setLink({ href: '$url' })")
				data object Unset : Link(action2 = "editor.commands.unsetLink()")
				data object ExtendSelection : Link(action2 = "editor.commands.extendMarkRange('link')")
			}

			sealed class Align(action2: String) : KitKatAction(action = action2) {
				data object Left : Align(action2 = "editor.commands.setTextAlign('left');")
				data object Center : Align(action2 = "editor.commands.setTextAlign('center');")
				data object Right : Align(action2 = "editor.commands.setTextAlign('right');")
				data object Justify : Align(action2 = "editor.commands.setTextAlign('justify');")
				data object Unset : Align(action2 = "editor.commands.unsetTextAlign();")
			}

			sealed class TextColor(action2: String) : KitKatAction(action = action2) {
				data class Set(val color: String) : TextColor(action2 = "editor.commands.setColor('$color');")
				data object Unset : TextColor(action2 = "editor.commands.unsetColor();")
				data object ExtendSelection : Link(action2 = "editor.commands.extendMarkRange('textStyle')")
			}

			sealed class HighlightColor(action2: String) : KitKatAction(action = action2) {
				data class Set(val color: String) : HighlightColor(action2 = "editor.commands.setHighlight({ color: '$color' });")
				data object Unset : HighlightColor(action2 = "editor.commands.unsetHighlight();")
				data object ExtendSelection : Link(action2 = "editor.commands.extendMarkRange('highlight')")
			}

			sealed class Edit(action2: String) : KitKatAction(action = action2) {
				data object Enable : Edit(action2 = "editor.enable();")
				data object Disable : Edit(action2 = "editor.disable();")
				data class SetTitle(val title: String?) : Edit(action2 = "editor.setTitle('${title ?: ""}');")
				data class SetContent(val content: String?) : Edit(action2 = "editor.setContent(${content?.let { JSONObject().apply { put("content", it) }.toString() } ?: DEFAULT_CONTENT});") {
					companion object {
						const val DEFAULT_CONTENT = ""
					}
				}
			}

			sealed class Other(action2: String) : KitKatAction(action = action2) {
				data object SetMaxHeight : Other(action2 = "document.getElementsByClassName(\"ProseMirror\")[0].style.setProperty('height', (window.innerHeight) + 'px');")
				data object EnableDarkMode : Other(action2 = "editor.enableDarkMode();")
				data object DisableDarkMode : Other(action2 = "editor.disableDarkMode();")
			}

			sealed class Export(action2: String, callback2: (String) -> Unit) : KitKatAction(action = action2, callback = callback2) {
				data class Text(val callback3: (String) -> Unit) : Export(action2 = "editor.execExportText();", callback2 = callback3)
				data class Html(val callback3: (String) -> Unit) : Export(action2 = "editor.execExportHtml();", callback2 = callback3)
				data class Json(val callback3: (String) -> Unit) : Export(action2 = "editor.execExportJson();", callback2 = callback3)
				data class Markdown(val callback3: (String) -> Unit) : Export(action2 = "editor.execExportMarkdown();", callback2 = callback3)
			}

			data class TestAction(val action2: String, val callback2: (String) -> Unit) : KitKatAction(action = action2, callback = callback2)
		}
	}
}
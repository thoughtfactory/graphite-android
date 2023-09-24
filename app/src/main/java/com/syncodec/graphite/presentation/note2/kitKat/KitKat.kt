package com.syncodec.graphite.presentation.note2.kitKat

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
}
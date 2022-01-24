package com.syncodec.momento.custom

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.*
import org.json.JSONObject

@SuppressLint("JavascriptInterface")
class EditorView(context: Context): WebView(context) {

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

		addJavascriptInterface(this, "bridge")

		loadUrl(INDEX_PATH)
		isReady = url.equals(INDEX_PATH, ignoreCase = true)
	}

	@JavascriptInterface
	fun format(obj: String){
		Log.i("BRIDGE","$obj")
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

	companion object {
		const val TAG = "EDITOR_VIEW"
		const val INDEX_PATH = "file:///android_asset/tinyEditor/index.html"
	}
}

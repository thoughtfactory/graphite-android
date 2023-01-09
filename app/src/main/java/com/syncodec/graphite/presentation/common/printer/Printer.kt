package com.syncodec.graphite.presentation.common.printer

import android.content.Context
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintAttributes.Resolution
import android.print.PrintManager
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.syncodec.graphite.presentation.common.richText.viewer.util.randomUUID
import com.syncodec.graphite.utils.alice.Alice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class Printer(context: Context) : WebView(context) {

//	fun print(printAdapter: PrintDocumentAdapter, path: File?, fileName: String?) {
//		printAdapter.onLayout(null, printAttributes, null, object : LayoutResultCallback() {
//			override fun onLayoutFinished(info: PrintDocumentInfo, changed: Boolean) {
//				printAdapter.onWrite(null, getOutputFile(path, fileName), CancellationSignal(), object : WriteResultCallback() {
//					override fun onWriteFinished(pages: Array<PageRange>) {
//						super.onWriteFinished(pages)
//					}
//				})
//			}
//		}, null)
//	}

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

		context.assets.open("orbit/orbital").let {
			val buffer = ByteArray(it.available())
			it.read(buffer)
			it.close()
			val encHtml = String(buffer)
			val passcode = "2%xY@Z5kYGu*iX!#N3m%03fC%4!070#D"

			CoroutineScope(Dispatchers.IO).launch {
				try {
					Alice.decrypt(encHtml, passcode).let { html ->
						withContext(Dispatchers.Main) {
							if (html == null) Toast.makeText(context, "Error loading printer", Toast.LENGTH_LONG).show()
							else loadDataWithBaseURL("file:///android_asset/orbit", html, "text/html", "UTF-8", null)
						}
					}
				} catch (e : Exception) {
					withContext(Dispatchers.Main) {
						Toast.makeText(context, "Error loading printer", Toast.LENGTH_LONG).show()
					}
				}
			}
		}
	}


	fun createWebPrintJob(data: String) {

		this.loadData(data, "text/html", "UTF-8")

		val jobName = randomUUID()
		val attributes = PrintAttributes.Builder()
			.setMediaSize(PrintAttributes.MediaSize.ISO_A4)
			.setResolution(Resolution("pdf", "pdf", 600, 600))
			.setMinMargins(PrintAttributes.Margins.NO_MARGINS).build()
		val path: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/PDFTest/")
//		printDoc(createPrintDocumentAdapter(jobName),attributes, path, "output_" + System.currentTimeMillis() + ".pdf")


		(context.getSystemService(Context.PRINT_SERVICE) as? PrintManager?)?.let {
			val printAdapter = createPrintDocumentAdapter(jobName)
			val printJob = it.print(
				jobName,
				printAdapter,
				PrintAttributes.Builder().build()
			)
		}
	}
}

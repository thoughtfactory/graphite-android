package com.syncodec.graphite.presentation.common.printer

import android.content.Context
import android.os.CancellationSignal
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.print.*
import android.print.PrintAttributes.Resolution
import android.print.PrintDocumentAdapter.LayoutResultCallback
import android.print.PrintDocumentAdapter.WriteResultCallback
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.syncodec.graphite.presentation.common.richText.RichTextEditor
import com.syncodec.graphite.presentation.common.richText.viewer.util.randomUUID
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

//		addJavascriptInterface(this, "bridge")

		loadUrl(RichTextEditor.INDEX_PATH)
//		exec("editor.setBaseFontColor('$textColor');")
//		when (typography) {
//			0 -> exec("editor.setBaseFontFamily(\"overlock\");")
//			1 -> exec("editor.setBaseFontFamily(\"source_sans_pro\");")
//			2 -> exec("editor.setBaseFontFamily(\"ubuntu\");")
//			3 -> exec("editor.setBaseFontFamily('atwriter');")
//			else -> exec("editor.setBaseFontFamily(\"source_sans_pro\");")
//		}
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

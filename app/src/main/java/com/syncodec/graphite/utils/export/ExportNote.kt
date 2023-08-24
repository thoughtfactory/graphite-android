package com.syncodec.graphite.utils.export

import android.content.Context
import com.syncodec.graphite.utils.share
import java.io.File


object ExportNote {
	fun exportData(context : Context, dataString: String?, noteId : String, ext : String) {
		File(File(context.cacheDir, "export"), "$noteId.$ext").let {
			it.mkdirs()
			it.delete()
			it.createNewFile()
			it.writeText(dataString ?: "")
			it.share(context = context)
		}
	}
}
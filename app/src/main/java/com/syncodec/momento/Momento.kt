package com.syncodec.momento

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.syncodec.momento.database.UserDatabase
import com.syncodec.momento.database.quote.QuoteDbEntry
import com.syncodec.momento.miscellaneous.FileUtils.Companion.copyInputStreamToOutputStream
import com.syncodec.momento.miscellaneous.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.internal.wait
import org.json.JSONObject
import java.io.File
import java.nio.charset.Charset


class Momento : Application() {

	var vaultState = mutableStateOf(VaultState.NOT_OPENED)

	private lateinit var ROOT: String

	private val DATA: String = "data"
		get() = "$ROOT/$field"

	private val BUCKET_DIR = "bucket"
		get() = "$DATA/$field"

	private val NOTE_DIR = "note"
		get() = "$DATA/$field"

	private val ATTACHMENT_DIR = "attachment"
		get() = "$DATA/$field"

	private val EXPORT_DIR = "export"
		get() = "$DATA/$field"

	private val QUOTE = "quote"
		get() = "$DATA/$field"

	override fun onCreate() {
		super.onCreate()

		ROOT = applicationContext.applicationInfo.dataDir

		File(DATA).mkdirs()
		File(BUCKET_DIR).mkdirs()
		File(NOTE_DIR).mkdirs()
		File(ATTACHMENT_DIR).mkdirs()

		downloadQuote()
	}

	fun putNote(key: String, noteContent: JSONObject?) {
		File("$NOTE_DIR/$key.json").apply {
			noteContent?.toString()?.let { writeText(it) }
		}
	}

	fun getNote(key: String): JSONObject? {
		File("$NOTE_DIR/$key.json").also {
			return if (it.exists()) {
				try {
					JSONObject(it.readText())
				} catch (exception: Exception) {
					null
				}
			} else {
				null
			}
		}
	}

	fun deleteNote(key: String) = File("$NOTE_DIR/$key.json").delete()

	fun deleteNote(keyList: List<String>) = keyList.forEach { File("$NOTE_DIR/$it.json").delete() }

	fun putAttachment(key: String, uri: Uri): Boolean {
		val inputStream = contentResolver.openInputStream(uri)
		val outputStream = File("$ATTACHMENT_DIR/$key").outputStream()
		return if (inputStream != null) {
			try {
				copyInputStreamToOutputStream(
					inputStream = inputStream,
					outputStream = outputStream
				)
				true
			} catch (exception: Exception) {
				false
			}
		} else false
	}

	fun getAttachment(key: String): Uri = Uri.fromFile(File("$ATTACHMENT_DIR/$key"))

	fun getAttachment(keyList: List<String>): Map<String, Uri> {
		val uriMap: MutableMap<String, Uri> = mutableMapOf()
		keyList.forEach {
			try {
				uriMap[it] = File("$ATTACHMENT_DIR/$it").toUri()
			} catch (exception: Exception) {
			}
		}

		return uriMap
	}

	fun deleteAttachment(keyList: List<String>) =
		keyList.forEach { File("$ATTACHMENT_DIR/$it").delete() }

	fun getQuoteBg(date: String): File? {
		val file = File("$QUOTE/${date}.jpeg")
		return if (file.exists()) file else null
	}

	fun downloadQuote() {
		CoroutineScope(Dispatchers.IO).launch {
			val storage = Firebase.storage("gs://the-life-cycle.appspot.com")
			val storageRef = storage.reference
			val quoteDirRef = storageRef.child("server/enQuote")

			val quoteTableDao = UserDatabase.getInstance(this@Momento).quoteTableDao
			val quoteKeyList = quoteTableDao.getAllKeys()

			quoteDirRef.listAll()
				.addOnSuccessListener { dateList ->
					dateList.prefixes.forEach { date ->
						if (date.name !in quoteKeyList) {
							logger("downloading")
							date.child("${date.name}.json")
								.getBytes(1024 * 1024)
								.addOnSuccessListener { byteArray ->
									File("$QUOTE/").mkdirs()
									File("$QUOTE/${date.name}.jpeg").createNewFile()
									val fileUri = Uri.fromFile(File("$QUOTE/${date.name}.jpeg"))
									date.child("${date.name}.jpg")
										.getFile(fileUri)
										.addOnSuccessListener {
											val jsonObject =
												JSONObject(byteArray.toString(Charset.defaultCharset()))
											QuoteDbEntry(
												date = date.name,
												quote = jsonObject.getString("quote"),
												author = jsonObject.getString("author"),
												special = jsonObject.getString("special"),
												isFavourite = false,
												authorLink = jsonObject.optString("authorLink"),
												bgLink = jsonObject.optString("bgLink"),
												bgCred = jsonObject.optString("bgCred"),
												bgCredLink = jsonObject.optString("bgCredLink")
											).apply {
												CoroutineScope(Dispatchers.IO).launch {
													quoteTableDao.insert(this@apply)
												}
											}
										}
										.addOnFailureListener {
											val jsonObject =
												JSONObject(byteArray.toString(Charset.defaultCharset()))
											QuoteDbEntry(
												date = date.name,
												quote = jsonObject.getString("quote"),
												author = jsonObject.getString("author"),
												special = jsonObject.getString("special"),
												isFavourite = false,
												authorLink = jsonObject.optString("authorLink"),
												bgLink = jsonObject.optString("bgLink"),
												bgCred = jsonObject.optString("bgCred"),
												bgCredLink = jsonObject.optString("bgCredLink")
											).apply {
												CoroutineScope(Dispatchers.IO).launch {
													quoteTableDao.insert(this@apply)
												}
											}
										}
								}
						}
					}
				}

		}
	}

	companion object {
		enum class VaultState {
			NOT_OPENED,
			TRY_OPEN,
			SETUP,
			OPENED,
			CLOSED,
			ERROR
		}
	}
}

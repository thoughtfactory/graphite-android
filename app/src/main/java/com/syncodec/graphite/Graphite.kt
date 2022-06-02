package com.syncodec.graphite

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.QonversionError
import com.qonversion.android.sdk.QonversionPermissionsCallback
import com.qonversion.android.sdk.dto.QPermission
import com.syncodec.graphite.alice.Alice
import com.syncodec.graphite.database.UserDatabase
import com.syncodec.graphite.database.export.NoteExport
import com.syncodec.graphite.database.note.NoteDbEntry
import com.syncodec.graphite.database.quote.QuoteDbEntry
import com.syncodec.graphite.konstant.Konstant
import com.syncodec.graphite.miscellaneous.DataStore
import com.syncodec.graphite.miscellaneous.FileUtils
import com.syncodec.graphite.miscellaneous.FileUtils.Companion.copyInputStreamToOutputStream
import com.syncodec.graphite.miscellaneous.FileUtils.Companion.getFileExtension
import com.syncodec.graphite.miscellaneous.TimeUtils
import io.github.lucasfsc.html2pdf.Html2Pdf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.nio.charset.Charset
import java.util.*


class Graphite : Application() {

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

	private val QUOTE_DIR = "quote"
		get() = "$DATA/$field"

	override fun onCreate() {
		super.onCreate()

		Qonversion.launch(this, "uxe7AOivKhdL8V-U-ZlHS1d03IGxlAhk", false)

		Qonversion.checkPermissions(object : QonversionPermissionsCallback{
			override fun onError(error: QonversionError) {

			}

			override fun onSuccess(permissions: Map<String, QPermission>) {
				var isSubscriptionActive: Boolean = false
				val dataStore = DataStore(this@Graphite)

				permissions.forEach { (key, qPermission) ->
					qPermission.expirationDate?.time.also {
						val calendar = android.icu.util.Calendar.getInstance()
						calendar.add(android.icu.util.Calendar.MONTH, 1)

						if (it == null) {
							dataStore.putExpiryTime(calendar.timeInMillis)
						} else {
							dataStore.putExpiryTime(it)
						}

						isSubscriptionActive =
							isSubscriptionActive or qPermission.isActive()
					}
				}
			}
		})

		ROOT = applicationContext.applicationInfo.dataDir

		File(DATA).mkdirs()
		File(BUCKET_DIR).mkdirs()
		File(NOTE_DIR).mkdirs()
		File(ATTACHMENT_DIR).mkdirs()
		File(QUOTE_DIR).mkdirs()

		downloadQuote()
	}

	fun putNote(key: String, noteContent: JSONObject?) {
		try {
			File("$NOTE_DIR/$key").apply {
				noteContent?.toString(0)?.let {
					Alice.encrypt(
						it,
						"m7X*fN@Rh#WNcs2Q69NyYQrQkHb@U^%*c59R7K4o2#0d92##HBojTyZ4a^5@qB&0"
					)?.also {
						writeText(it)
					}
				}
			}
		} catch (exception: Exception) {
			Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show()
		}
	}

	fun getNote(key: String): JSONObject? {
		File("$NOTE_DIR/$key").apply {
			return if (exists()) {
				try {
					Alice.decrypt(
						readText(),
						"m7X*fN@Rh#WNcs2Q69NyYQrQkHb@U^%*c59R7K4o2#0d92##HBojTyZ4a^5@qB&0"
					)?.let { JSONObject(it) }
				} catch (exception: Exception) {
					exception.printStackTrace()
					Toast.makeText(this@Graphite, "Error reading data", Toast.LENGTH_SHORT).show()
					null
				}
			} else {
				null
			}
		}
	}

	fun getNoteString(key: String): String? {
		File("$NOTE_DIR/$key").apply {
			return if (exists()) {
				try {
					Alice.decrypt(
						readText(),
						"m7X*fN@Rh#WNcs2Q69NyYQrQkHb@U^%*c59R7K4o2#0d92##HBojTyZ4a^5@qB&0"
					)
				} catch (exception: Exception) {
					exception.printStackTrace()
					Toast.makeText(this@Graphite, "Error reading data", Toast.LENGTH_SHORT).show()
					null
				}
			} else {
				null
			}
		}
	}

	fun deleteNote(key: String) = File("$NOTE_DIR/$key").delete()

	fun deleteNote(keyList: List<String>) = keyList.forEach { File("$NOTE_DIR/$it").delete() }

	fun putAttachment(key: String, uri: Uri?, extension: String?): Boolean {
		val inputStream = uri?.let { contentResolver.openInputStream(it) }

		val outputStream =
			File("$ATTACHMENT_DIR/$key${if (extension != null) ".$extension" else ""}").outputStream()
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

	fun getAttachment(key: String): Uri? {
		File(ATTACHMENT_DIR).listFiles { dir, name -> name.contains(key) }?.getOrNull(0).also {
			return if (it != null) Uri.fromFile(it) else null
		}
	}

	fun getAttachment(keyList: List<String>, extensionList: List<String?>): Map<String, Uri> {
		val uriMap: MutableMap<String, Uri> = mutableMapOf()
		keyList.forEachIndexed { index, key ->
			try {
				uriMap[key] =
					File("$ATTACHMENT_DIR/$key${if (extensionList[index] != null) ".${extensionList[index]}" else ""}").toUri()
			} catch (exception: Exception) {
			}
		}

		return uriMap
	}

	fun deleteAttachment(keyList: List<String>) =
		keyList.forEach { File("$ATTACHMENT_DIR/$it").delete() }

	fun getQuoteBg(date: String): File? {
		val file = File("$QUOTE_DIR/${date}.jpeg")
		return if (file.exists()) file else null
	}

	fun printNote(
		htmlContent: String,
		key: String?,
		timestamp: Long,
		address: String?,
		latLng: LatLng?
	) {
		val calendar = Calendar.getInstance()
		calendar.timeInMillis = timestamp

		val day = calendar.get(Calendar.DAY_OF_WEEK)
		val date = calendar.get(Calendar.DAY_OF_MONTH)
		val month = calendar.get(Calendar.MONTH)
		val year = calendar.get(Calendar.YEAR)

		val header =
			"<h2>$date<sup>${if (date % 1 == 0) "st" else if (date % 2 == 0) "nd" else if (date % 3 == 0) "rd" else "th"}</sup> ${Konstant.monthNameShort[month]}, $year ${Konstant.weekNameShort[day - 1]}</h2>\n" +
					(if (address != null) "$address\n" else if (latLng != null) "${latLng.latitude}, ${latLng.longitude}" else "") +
					"<hr />"
		File("${cacheDir.path}/export/").mkdirs()
		val exportFile = File("${cacheDir.path}/export/${key ?: System.currentTimeMillis()}.pdf")
		val converter = Html2Pdf.Companion.Builder()
			.context(this)
			.html(header + htmlContent)
			.file(exportFile)
			.build()

		converter.convertToPdf(
			object : Html2Pdf.OnCompleteConversion {
				override fun onSuccess() {
					val shareIntent = Intent(Intent.ACTION_SEND)
					shareIntent.type = "application/pdf"

					shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
					shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
					val uri = FileProvider.getUriForFile(
						this@Graphite,
						"com.syncodec.fileprovider",
						exportFile
					)

					shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
					val resInfoList = packageManager.queryIntentActivities(
						shareIntent,
						PackageManager.MATCH_DEFAULT_ONLY
					)
					for (resolveInfo in resInfoList) {
						val packageName = resolveInfo.activityInfo.packageName
						grantUriPermission(
							packageName,
							uri,
							Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
						)
					}

					Intent.createChooser(shareIntent, "Share Via").apply {
						addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
						addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
						startActivity(this)
					}
				}

				override fun onFailed() {
					Toast.makeText(this@Graphite, "Error exporting data", Toast.LENGTH_SHORT).show()
				}
			}
		)
		converter.convertToPdf()
	}

	fun exportNote(noteDbEntry: NoteDbEntry, noteContent: JSONObject?, tagList: List<String>) {
		val objectMapper: ObjectMapper = jsonMapper { addModule(kotlinModule()) }

		val exportDir = File("${cacheDir.path}/export/${noteDbEntry.key}")
		exportDir.mkdirs()

		NoteExport(
			key = noteDbEntry.key,
			createdTimestamp = noteDbEntry.createdTimestamp,
			modifiedTimestamp = noteDbEntry.modifiedTimestamp,
			userTimestamp = noteDbEntry.userTimestamp,
			timezone = noteDbEntry.timezone,
			chapterPath = noteDbEntry.chapterPath,
			notebookKey = noteDbEntry.notebookKey,
			title = noteDbEntry.title,
			content = noteContent.toString(),
			latLng = noteDbEntry.latLng,
			address = noteDbEntry.address,
			attachmentKey = noteDbEntry.attachmentKeyList,
			tagList = tagList,
		).apply {
			File("${exportDir.path}/${noteDbEntry.key}.json")
				.writeText(objectMapper.writeValueAsString(this))


			noteDbEntry.attachmentKeyList.forEach {attachmentKey ->
				getAttachment(attachmentKey)?.also {uri ->
					val extension = getFileExtension(uri = uri)
					val attachmentFile =
						File("${exportDir.path}/$attachmentKey${if (extension != null) ".$extension" else ""}")
					val inputStream = contentResolver.openInputStream(uri)
					val outputStream = attachmentFile.outputStream()
					if (inputStream != null) copyInputStreamToOutputStream(inputStream, outputStream)
				}
			}

			FileUtils.zipFolder(exportDir.path, "${exportDir.path}.zip")

			val exportFile = File("${exportDir.path}.zip")

			val shareIntent = Intent(Intent.ACTION_SEND)
			shareIntent.type = "application/zip"

			shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
			shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			val uri = FileProvider.getUriForFile(
				this@Graphite,
				"com.syncodec.fileprovider",
				exportFile
			)

			shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
			val resInfoList = packageManager.queryIntentActivities(
				shareIntent,
				PackageManager.MATCH_DEFAULT_ONLY
			)
			for (resolveInfo in resInfoList) {
				val packageName = resolveInfo.activityInfo.packageName
				grantUriPermission(
					packageName,
					uri,
					Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
				)
			}

			Intent.createChooser(shareIntent, "Share Via").apply {
				addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
				addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				startActivity(this)
			}
		}
	}

	private fun downloadQuote() {
		CoroutineScope(Dispatchers.IO).launch {
			val storage = Firebase.storage("gs://graphite-diary.appspot.com")
			val storageRef = storage.reference
			val quoteDirRef = storageRef.child("server/enQuote")

			val quoteTableDao = UserDatabase.getInstance(this@Graphite).quoteTableDao
			val quoteKeyList = quoteTableDao.getAllKeys()

			val maxDate = quoteKeyList.maxOfOrNull { TimeUtils.quoteKeyToTimestamp(it) ?: 0 }

			if ((maxDate?.minus(TimeUtils.getToday()) ?: 0) < 3 * 24 * 60 * 60 * 1000) {
				quoteDirRef.listAll()
					.addOnSuccessListener { dateList ->
						dateList.prefixes.forEach { date ->
							if (date.name !in quoteKeyList) {
								date.child("${date.name}.json")
									.getBytes(1024 * 1024)
									.addOnSuccessListener { byteArray ->
										File("$QUOTE_DIR/").mkdirs()
										File("$QUOTE_DIR/${date.name}.jpeg").createNewFile()
										val fileUri =
											Uri.fromFile(File("$QUOTE_DIR/${date.name}.jpeg"))
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
													quote = jsonObject.optString("quote"),
													author = jsonObject.optString("author"),
													special = jsonObject.optString("special"),
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
	}

	companion object {
		enum class VaultState {
			NOT_OPENED,
			TRY_OPEN,
			OPENED,
			CLOSED,
			ERROR
		}
	}
}

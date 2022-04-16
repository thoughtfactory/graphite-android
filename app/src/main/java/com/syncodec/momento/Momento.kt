package com.syncodec.momento

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.syncodec.momento.miscellaneous.FileUtils.Companion.copyInputStreamToOutputStream
import com.syncodec.momento.miscellaneous.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class Momento : Application() {

	private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

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

	fun downloadQuote() {
//		CoroutineScope(Dispatchers.IO).launch {
//			val storage = Firebase.storage("gs://the-life-cycle.appspot.com")
//
//			val storageRef = storage.reference
//			val quoteRef = storageRef.child("server/enQuote")
//
//			quoteRef.listAll()
//				.addOnSuccessListener {
//					it.prefixes.forEach {
//						logger("item : ${it.name}")
//					}
//				}
//
//			val destFile = File("$QUOTE/")
//		}
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

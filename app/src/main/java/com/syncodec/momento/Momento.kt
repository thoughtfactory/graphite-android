package com.syncodec.momento

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.syncodec.momento.database.bucketItem.BucketItem
import com.syncodec.momento.database.note.Note
import com.syncodec.momento.miscellaneous.FileUtils.Companion.copyInputStreamToOutputStream
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

	override fun onCreate() {
		super.onCreate()

		ROOT = applicationContext.applicationInfo.dataDir

		File(DATA).mkdirs()
		File(BUCKET_DIR).mkdirs()
		File(NOTE_DIR).mkdirs()
		File(ATTACHMENT_DIR).mkdirs()
	}

	fun putNote(note: Note) = objectMapper.writeValue(File("$NOTE_DIR/${note.key}.json"), note)
	fun getNote(key: String): Note = objectMapper.readValue(File("$NOTE_DIR/$key.json"))
	fun deleteNote(key: String) = File("$NOTE_DIR/$key.json").delete()

	fun putBucketItem(bucketItem: BucketItem) = objectMapper.writeValue(File("$BUCKET_DIR/${bucketItem.key}.json"), bucketItem)
	fun getBucketItem(key: String): String = File("$BUCKET_DIR/$key.json").readText()
	fun deleteBucketItem(key: String) = File("$BUCKET_DIR/$key.json").delete()

	fun putAttachment(key: String, uri: Uri): Boolean {
		val inputStream = contentResolver.openInputStream(uri)
		val outputStream = File("$ATTACHMENT_DIR/$key").outputStream()
		return if (inputStream != null) {
			try {
				copyInputStreamToOutputStream(inputStream = inputStream, outputStream = outputStream)
				true
			} catch (exception: Exception) {
				false
			}
		} else false
	}

	fun getAttachment(key: String): Uri = File("$ATTACHMENT_DIR/$key").toUri()

	companion object {
		enum class VaultState {
			NOT_OPENED,
			TRY_OPEN,
			SETUP,
			OPENED,
			CLOSED,
			ERROR
		}

		enum class Click {
			FAVOURITE,
			ARCHIVE,
			LOCK,
			SHARE,
			EXPORT,
			DELETE
		}
	}
}

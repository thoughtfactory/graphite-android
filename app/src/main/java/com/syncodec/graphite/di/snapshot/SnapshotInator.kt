package com.syncodec.graphite.di.snapshot

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.documentfile.provider.DocumentFile
import com.jakewharton.processphoenix.ProcessPhoenix
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.model.local.BucketItemObject
import com.syncodec.graphite.di.model.local.BucketObject
import com.syncodec.graphite.di.model.local.ChapterObject
import com.syncodec.graphite.di.model.local.NoteObject
import com.syncodec.graphite.di.model.local.TagObject
import com.syncodec.graphite.di.repository.AttachmentRepository.Companion.attachmentDirPath
import com.syncodec.graphite.di.repository.RealmMigrator
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.utils.CURRENT_ISO_TIMESTAMP
import com.syncodec.graphite.utils.alice.AliceRequest2
import com.syncodec.graphite.utils.alice.getSecretData2
import com.syncodec.graphite.utils.archiveUtil.CompressUtil
import com.syncodec.graphite.utils.copyInDirectory
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.annotations.Ignore
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.internal.closeQuietly
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel
import java.io.File
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SnapshotInator(private val context: Context, private val repository: Repository) {

	private val json = Json

	private fun generateRealmSnapshot(name: String, path: String) {
		val realmConfiguration = RealmConfiguration
			.Builder(Repository.REALM_BUILDER_SCHEMA)
			.schemaVersion(Repository.SCHEMA_VERSION)
			.directory(path)
			.name(name)
			.migration(RealmMigrator())
			.build()
		repository.realm.writeCopyTo(realmConfiguration)
	}

	fun generateSnapshotFile(): File {
		val snapshotDir = File(context.cacheDir, "snapshot").also {
			it.deleteRecursively()
			it.mkdirs()
		}
		val fileName = "graphite_snapshot_$CURRENT_ISO_TIMESTAMP"
		val currentSnapshotDir = File(snapshotDir, fileName).also {
			it.mkdirs()
		}

		val snapshotMetadata = SnapshotMetadata(
			noteCount = repository.getAllObjectOfType<NoteObject>(includeLocked = true).size,
			chapterCount = repository.getAllObjectOfType<ChapterObject>(includeLocked = true).size,
			bucketItemCount = repository.getAllObjectOfType<BucketItemObject>(includeLocked = true).size,
			bucketCount = repository.getAllObjectOfType<BucketObject>(includeLocked = true).size,
			tagCount = repository.getAllObjectOfType<TagObject>(includeLocked = true).size,
			attachmentCount = repository.attachmentRepository.countTotalAttachment(),
		)

		generateRealmSnapshot("$fileName.realm", currentSnapshotDir.path)
		val attachmentFolder = File(currentSnapshotDir, "attachment").also { it.mkdirs() }
		val snapshotMetadataFile = File(currentSnapshotDir, "metadata.json")
		snapshotMetadataFile.createNewFile()
		snapshotMetadataFile.writeText(snapshotMetadata.encodeToString())

		copyInDirectory(File(context.attachmentDirPath()), attachmentFolder)

		val zipFile = File(snapshotDir, "${fileName}.zip")
		CompressUtil.Zip.createZipFile(currentSnapshotDir, zipFile)

		return zipFile
	}

	private fun getTmpImportSnapshotDir() = File(context.cacheDir, "importSnapshot").also {
		it.deleteRecursively()
		it.mkdirs()
	}

	fun restore(inputStream: InputStream, is7z: Boolean = false): Boolean {
		val importSnapshotDir = getTmpImportSnapshotDir()

		val snapshotDir = if (is7z) {
			val sevenZImportFile = File(importSnapshotDir, "graphite_snapshot.7z")
			sevenZImportFile.outputStream().use { outputStream ->
				inputStream.copyTo(outputStream)
				inputStream.closeQuietly()
			}

			val snapshotDir1 = File(importSnapshotDir, "snapshot")
			CompressUtil.SevenZ.extractSevenZFile(inputFile = sevenZImportFile, outputFile = snapshotDir1)
			snapshotDir1
		} else {
			val zipImportFile = File(importSnapshotDir, "graphite_snapshot.zip")
			zipImportFile.outputStream().use { outputStream ->
				inputStream.copyTo(outputStream)
				inputStream.closeQuietly()
			}

			val snapshotDir1 = File(importSnapshotDir, "snapshot")
			CompressUtil.Zip.extractZipFile(inputFile = zipImportFile, outputFile = snapshotDir1)
			snapshotDir1
		}

		// Delete attachment folder
		repository.attachmentRepository.deleteAll()

		snapshotDir.listFiles()?.firstOrNull { it.name.endsWith(".realm") }?.let { realmFile ->
			val isSnapshotRestored = restoreRealm(realmFile.name, snapshotDir.path)
			if (isSnapshotRestored) {
				snapshotDir.listFiles()?.firstOrNull { it.name == "attachment" }?.let { attachmentDir ->
					repository.attachmentRepository.importAttachmentFromGraphite(attachmentDir)
					ProcessPhoenix.triggerRebirth(context)
				}
				ProcessPhoenix.triggerRebirth(context)
			} else {
				return false
			}
		}
		return true
	}

	fun restoreRealm(name: String, path: String): Boolean {
		when (val realmKeyAliceRequest = context.getSecretData2(key = "realmKey")) {
			is AliceRequest2.Success -> {
				try {
					val snapshotRealmConfiguration = RealmConfiguration
						.Builder(Repository.REALM_BUILDER_SCHEMA)
						.schemaVersion(Repository.SCHEMA_VERSION)
						.directory(path)
						.name(name)
						.migration(RealmMigrator())
						.build()

					val snapshotRealm = Realm.open(snapshotRealmConfiguration)
					repository.realm.close()
					File(context.filesDir, "default.realm").delete()

					val key = realmKeyAliceRequest.data
					if (BuildConfig.DEBUG) {
						key.joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }.let {
							Log.d("npr71", "Realm Key: $it")
						}
					}

					val currentRealmConfiguration = RealmConfiguration
						.Builder(Repository.REALM_BUILDER_SCHEMA)
						.encryptionKey(key)
						.schemaVersion(Repository.SCHEMA_VERSION)
						.migration(RealmMigrator())
						.build()

					snapshotRealm.writeCopyTo(currentRealmConfiguration)
					snapshotRealm.close()
					return true
				} catch (e: Exception) {
					if (BuildConfig.DEBUG) e.printStackTrace()
					return false
				}
			}
			is AliceRequest2.KeystoreUninitialized -> return false
			is AliceRequest2.KeyNotFound -> return false
			is AliceRequest2.UnknownError -> return false
		}
	}

	@WorkerThread
	fun restoreSnapshot(documentFile: DocumentFile): Boolean {
		return context.contentResolver.openInputStream(documentFile.uri)?.use {
			restore(inputStream = it, is7z = documentFile.name?.endsWith(".7z") == true)
		} ?: false
	}

	@WorkerThread
	fun restoreSnapshot(snapshotByteArray: ByteArray, is7z: Boolean = false): Boolean {
		return snapshotByteArray.inputStream().use {
			restore(inputStream = it, is7z = is7z)
		}
	}

	@WorkerThread
	fun readBackupFolder(): Map<DocumentFile, SnapshotMetadata?> {
		val uri = context.contentResolver.persistedUriPermissions.firstOrNull()?.uri
		return if (uri != null) {
			val documentTree = DocumentFile.fromTreeUri(context, uri)
			documentTree
				?.listFiles()
				?.filter { it.name?.endsWith(".zip") == true || it.name?.endsWith(".7z") == true }
				?.associateWith { it.getFileInfo(context = context) }
				?: mapOf()
		} else mapOf()
	}

	private fun DocumentFile.getFileInfo(context: Context): SnapshotMetadata? {
		return when {
			name?.endsWith(".zip") == true -> getZipFileInfo(context = context)
			name?.endsWith(".7z") == true -> null
			else -> null
		}
	}

	@OptIn(ExperimentalSerializationApi::class)
	private fun DocumentFile.getZipFileInfo(context: Context): SnapshotMetadata? {
		try {
			return context.contentResolver.openInputStream(uri)?.use { inputStream ->
				SeekableInMemoryByteChannel(inputStream.readBytes()).use { inMemoryByteChannel ->
					val zipFile = ZipFile(inMemoryByteChannel)
					zipFile.entries.toList().find { it.name == "metadata.json" }?.let { zipArchiveEntry ->
						zipFile.getInputStream(zipArchiveEntry).use { metadataInputStream ->
							val snapshotMetadata = json.decodeFromStream<SnapshotMetadata>(metadataInputStream)
							snapshotMetadata
						}
					}
				}
			}
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			return null
		}
	}

	@OptIn(ExperimentalSerializationApi::class)
	fun getByteArrayInfo(byteArray: ByteArray): SnapshotMetadata? {
		try {
			SeekableInMemoryByteChannel(byteArray).use { inMemoryByteChannel ->
				val zipFile = ZipFile(inMemoryByteChannel)
				zipFile.entries.toList().find { it.name == "metadata.json" }?.let { zipArchiveEntry ->
					zipFile.getInputStream(zipArchiveEntry).use { metadataInputStream ->
						return json.decodeFromStream<SnapshotMetadata>(metadataInputStream)
					}
				} ?: return null
			}
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			return null
		}
	}

	fun clearSnapshotDir() {
		val snapshotDir = File(context.cacheDir, "snapshot")
		snapshotDir.deleteRecursively()
	}

	companion object {
		@Serializable
		data class SnapshotMetadata(
			val timestamp: String = CURRENT_ISO_TIMESTAMP,
			val noteCount: Int = 0,
			val chapterCount: Int = 0,
			val bucketItemCount: Int = 0,
			val bucketCount: Int = 0,
			val tagCount: Int = 0,
			val attachmentCount: Int = 0,
		) {
			@Ignore
			val timeStampPretty: String?
				get() = try {
					LocalDateTime.parse(timestamp).format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
				} catch (e: Exception) {
					timestamp
				}

			fun encodeToString(): String = Json.encodeToString(this)

			override fun hashCode(): Int {
				var result = timestamp.hashCode()
				result = 31 * result + noteCount
				result = 31 * result + chapterCount
				result = 31 * result + bucketItemCount
				result = 31 * result + bucketCount
				result = 31 * result + tagCount
				result = 31 * result + attachmentCount
				return result
			}

			override fun equals(other: Any?): Boolean {
				if (this === other) return true
				if (javaClass != other?.javaClass) return false

				other as SnapshotMetadata

				if (timestamp != other.timestamp) return false
				if (noteCount != other.noteCount) return false
				if (chapterCount != other.chapterCount) return false
				if (bucketItemCount != other.bucketItemCount) return false
				if (bucketCount != other.bucketCount) return false
				if (tagCount != other.tagCount) return false
				if (attachmentCount != other.attachmentCount) return false

				return true
			}
		}
	}
}

package com.syncodec.graphite.di.cloud.googleDrive

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.syncodec.graphite.di.cloud.dropbox.DBox
import com.syncodec.graphite.service.syncInator.GDriveSyncInatorService
import java.io.IOException
import java.time.Instant

class GDrive(val context: Context) {
	var googleDriveState: Pair<GoogleDriveState, Long>? = null
	fun getDrive(): Drive? {
		return try {
			val googleAccount = GoogleSignIn.getLastSignedInAccount(context)
			val credential = GoogleAccountCredential.usingOAuth2(context, listOf(DriveScopes.DRIVE_APPDATA))
			credential.selectedAccount = googleAccount?.account
			Drive
				.Builder(NetHttpTransport(), GsonFactory(), credential)
				.setApplicationName("Graphite")
				.build()
		} catch (exception: Exception) {
			Log.e("GDrive", "getDrive: ${exception.message}")
			null
		}
	}

	fun testConnection(callback: (DBox.Companion.TestConnectionResponse) -> Unit) {
		try {
			val googleAccount = GoogleSignIn.getLastSignedInAccount(context)
			val credential = GoogleAccountCredential.usingOAuth2(context, listOf(DriveScopes.DRIVE_APPDATA))
			credential.selectedAccount = googleAccount?.account
			if (credential.selectedAccount == null) {
				callback(DBox.Companion.TestConnectionResponse.NotLoggedIn)
				return
			}
			else {
				Drive
					.Builder(NetHttpTransport(), GsonFactory(), credential)
					.setApplicationName("Graphite")
					.build()
					.about()
					.get()
					.setFields("user, storageQuota")
					.execute()
					.let {
						callback(
							DBox.Companion.TestConnectionResponse.Success(
								name = it.user.displayName,
								email = it.user.emailAddress,
								profilePictureUrl = it.user.photoLink,
								spaceUsed = it.storageQuota.usage,
								spaceTotal = it.storageQuota.limit,
							)
						)
					}
			}
		} catch (exception: Exception) {
//			exception.printStackTrace()
			callback(DBox.Companion.TestConnectionResponse.Error(exception, exception.message ?: "Unknown error"))
		}
	}

	fun refreshSnapshot(drive: Drive): GDriveSyncInatorService.Companion.ListFiles {
		val googleDriveState = getGoogleDriveState(drive = drive)
		return if (googleDriveState is GoogleDriveState.Success) {
			listFiles(
				drive = drive,
				query = "'${googleDriveState.backupFolderId}' in parents and trashed = false",
				fields = "files(id, name, mimeType, modifiedTime, appProperties)",
			)
		}
		else GDriveSyncInatorService.Companion.ListFiles.UnknownError()
	}

	fun createFile(
		drive: Drive,
		fileName: String,
		mimeType: String,
		parentId: String,
		modifiedTime: Long = Instant.now().toEpochMilli(),
		appProperties: Map<String, String> = mapOf(),
		byteArray: ByteArray? = null,
	): GDriveSyncInatorService.Companion.RetrieveFile {
//		Log.d("npr71", "GDriveInatorService.createFile : fileName = $fileName : parentId = $parentId")

		val fileMetadata = File()
		fileMetadata.name = fileName
		fileMetadata.mimeType = mimeType
		fileMetadata.parents = listOf(parentId)
		fileMetadata.modifiedTime = DateTime(modifiedTime)
		fileMetadata.appProperties = appProperties

		val data = byteArray?.let { ByteArrayContent(mimeType, it) }
		try {
			val file = data?.let {
				drive
					.files()
					.create(fileMetadata, it)
					.setFields("id")
					.execute()
					.let {
						Log.d("npr71", "GDriveInatorService.createFile : new file with data file.id = ${it.id}")
						it
					}
			} ?: drive
				.files()
				.create(fileMetadata)
				.setFields("id")
				.execute()
				.let {
					Log.d("npr71", "GDriveInatorService.createFile : new file file.id = ${it.id}")
					it
				}
			return GDriveSyncInatorService.Companion.RetrieveFile.Success(file.id)
		} catch (e: GoogleJsonResponseException) {
//			e.printStackTrace()
			return if (e.details.code == 404) GDriveSyncInatorService.Companion.RetrieveFile.ParentNotFound else GDriveSyncInatorService.Companion.RetrieveFile.UnknownError(e)
		} catch (e: Exception) {
//			e.printStackTrace()
			return GDriveSyncInatorService.Companion.RetrieveFile.UnknownError(e)
		}
	}

	fun listFiles(
		drive: Drive,
		query: String,
		fields: String,
	): GDriveSyncInatorService.Companion.ListFiles {
		val fileList: MutableList<File> = mutableListOf()
		var pageToken: String? = null
		do {
			try {
				drive
					.files()
					.list()
					.setQ(query)
					.setSpaces("appDataFolder")
					.setFields(fields)
					.execute()
					.let {
						fileList.addAll(it.files)
						pageToken = it.nextPageToken
					}
			} catch (e: GoogleJsonResponseException) {
//				e.printStackTrace()
				return if (e.details.code == 404) GDriveSyncInatorService.Companion.ListFiles.ParentNotFound else GDriveSyncInatorService.Companion.ListFiles.UnknownError(e)
			} catch (e: Exception) {
//				e.printStackTrace()
				return GDriveSyncInatorService.Companion.ListFiles.UnknownError(e)
			}
		} while (pageToken != null)
		return GDriveSyncInatorService.Companion.ListFiles.Success(fileList)
	}

	fun downloadData(
		drive: Drive,
		fileId: String,
	): GDriveSyncInatorService.Companion.DownloadResult {
		try {
			drive
				.files()
				.get(fileId)
				.executeMediaAsInputStream()
				.readBytes()
				.let {
					return GDriveSyncInatorService.Companion.DownloadResult.Success(fileId = fileId, fileContent = it)
				}
		} catch (e: Exception) {
			return GDriveSyncInatorService.Companion.DownloadResult.UnknownError(e)
		}
	}

	fun deleteFile(
		drive: Drive,
		fileId: String,
	): GDriveSyncInatorService.Companion.DeleteResult {
		try {
			drive
				.files()
				.delete(fileId)
				.execute()
				.let {
					return GDriveSyncInatorService.Companion.DeleteResult.Success
				}
		} catch (e: GoogleJsonResponseException) {
			return if (e.details.code == 404) GDriveSyncInatorService.Companion.DeleteResult.Success else GDriveSyncInatorService.Companion.DeleteResult.UnknownError(e)
		} catch (e: IOException) {
			return GDriveSyncInatorService.Companion.DeleteResult.NetworkError
		} catch (e: Exception) {
			return GDriveSyncInatorService.Companion.DeleteResult.UnknownError(e)
		}
	}

	fun getGoogleDriveState(drive: Drive): GoogleDriveState {

		if (googleDriveState != null) {
			if (googleDriveState!!.first is GoogleDriveState.Success && googleDriveState!!.second < Instant.now().toEpochMilli() + 60000) return googleDriveState!!.first
		}

		val rootChildrenQuery = "'appDataFolder' in parents "
		val fileList: MutableMap<String, File> = mutableMapOf()
		val listFiles = listFiles(drive = drive, query = rootChildrenQuery, fields = "nextPageToken, files(id, name)")
		if (listFiles is GDriveSyncInatorService.Companion.ListFiles.Success) fileList.putAll(listFiles.fileList.associateBy { it.name })
		else return GoogleDriveState.Error

		val lockFileId = fileList[LockFileName]?.id

		val chapterFolderId = fileList[ChapterFolderName]?.id ?: createFile(drive, ChapterFolderName, FolderMimeType, RootFolderId).let {
			if (it is GDriveSyncInatorService.Companion.RetrieveFile.Success) it.fileId else return GoogleDriveState.Error
		}

		val noteFolderId = fileList[NoteFolderName]?.id ?: createFile(drive, NoteFolderName, FolderMimeType, RootFolderId).let {
			if (it is GDriveSyncInatorService.Companion.RetrieveFile.Success) it.fileId else return GoogleDriveState.Error
		}

		val bucketFolderId = fileList[BucketFolderName]?.id ?: createFile(drive, BucketFolderName, FolderMimeType, RootFolderId).let {
			if (it is GDriveSyncInatorService.Companion.RetrieveFile.Success) it.fileId else return GoogleDriveState.Error
		}

		val bucketItemFolderId = fileList[BucketItemFolderName]?.id ?: createFile(drive, BucketItemFolderName, FolderMimeType, RootFolderId)
			.let {
				if (it is GDriveSyncInatorService.Companion.RetrieveFile.Success) it.fileId else return GoogleDriveState.Error
			}

		val tagFolderId = fileList[TagFolderName]?.id ?: createFile(drive, TagFolderName, FolderMimeType, RootFolderId).let {
			if (it is GDriveSyncInatorService.Companion.RetrieveFile.Success) it.fileId else return GoogleDriveState.Error
		}

		val attachmentFolderId = fileList[AttachmentFolderName]?.id ?: kotlin.run {
			when (val getFileResult =
				createFile(drive, AttachmentFolderName, FolderMimeType, RootFolderId)) {
				is GDriveSyncInatorService.Companion.RetrieveFile.Success -> getFileResult.fileId
				is GDriveSyncInatorService.Companion.RetrieveFile.TooManyRetries -> return GoogleDriveState.Error
				is GDriveSyncInatorService.Companion.RetrieveFile.ParentNotFound -> return GoogleDriveState.Error //  WTF ?? Thats not possible
				is GDriveSyncInatorService.Companion.RetrieveFile.UnknownError -> return GoogleDriveState.Error
			}
		}

		val deletedObjectFileId = fileList[DeletedObjectFileName]?.id ?: kotlin.run {
			when (val getFileResult =
				createFile(drive, DeletedObjectFileName, JsonFileMimeType, RootFolderId)) {
				is GDriveSyncInatorService.Companion.RetrieveFile.Success -> getFileResult.fileId
				is GDriveSyncInatorService.Companion.RetrieveFile.TooManyRetries -> return GoogleDriveState.Error
				is GDriveSyncInatorService.Companion.RetrieveFile.ParentNotFound -> return GoogleDriveState.Error //  WTF ?? Thats not possible
				is GDriveSyncInatorService.Companion.RetrieveFile.UnknownError -> return GoogleDriveState.Error
			}
		}

		val deletedAttachmentFileId = fileList[DeletedAttachmentFileName]?.id ?: kotlin.run {
			when (val getFileResult =
				createFile(drive, DeletedAttachmentFileName, JsonFileMimeType, RootFolderId)) {
				is GDriveSyncInatorService.Companion.RetrieveFile.Success -> getFileResult.fileId
				is GDriveSyncInatorService.Companion.RetrieveFile.TooManyRetries -> return GoogleDriveState.Error
				is GDriveSyncInatorService.Companion.RetrieveFile.ParentNotFound -> return GoogleDriveState.Error //  WTF ?? Thats not possible
				is GDriveSyncInatorService.Companion.RetrieveFile.UnknownError -> return GoogleDriveState.Error
			}
		}

		val backupFolderId = fileList[BackupFolderName]?.id ?: kotlin.run {
			when (val getFileResult =
				createFile(drive, BackupFolderName, FolderMimeType, RootFolderId)) {
				is GDriveSyncInatorService.Companion.RetrieveFile.Success -> getFileResult.fileId
				is GDriveSyncInatorService.Companion.RetrieveFile.TooManyRetries -> return GoogleDriveState.Error
				is GDriveSyncInatorService.Companion.RetrieveFile.ParentNotFound -> return GoogleDriveState.Error //  WTF ?? Thats not possible
				is GDriveSyncInatorService.Companion.RetrieveFile.UnknownError -> return GoogleDriveState.Error
			}
		}

		GoogleDriveState.Success(
			lockFileId = lockFileId,
			noteFolderId = noteFolderId,
			chapterFolderId = chapterFolderId,
			bucketFolderId = bucketFolderId,
			bucketItemFolderId = bucketItemFolderId,
			tagFolderId = tagFolderId,
			attachmentFolderId = attachmentFolderId,
			deletedObjectFileId = deletedObjectFileId,
			deletedAttachmentFileId = deletedAttachmentFileId,
			backupFolderId = backupFolderId
		).let {
			googleDriveState = Pair(it, Instant.now().toEpochMilli())
			return it
		}
	}

	val snapshot: Snapshot = Snapshot()

	inner class Snapshot {
		fun uploadSnapshot(
			drive: Drive,
			byteArray: ByteArray,
		): GDriveSyncInatorService.Companion.RetrieveFile {
			val googleDriveState = getGoogleDriveState(drive)
			val modifiedTime = Instant.now().toEpochMilli()
			return if (googleDriveState is GoogleDriveState.Success) {
				createFile(
					drive = drive,
					fileName = "graphite_snapshot_$modifiedTime.7z",
					mimeType = JsonFileMimeType,
					parentId = googleDriveState.backupFolderId,
					modifiedTime = modifiedTime,
					byteArray = byteArray
				)
			}
			else GDriveSyncInatorService.Companion.RetrieveFile.UnknownError()
		}
	}

	companion object {
		const val LockFileName: String = "graphite.lock"

		const val DeletedObjectFileName: String = "deletedObjectList.json"
		const val DeletedAttachmentFileName: String = "deletedAttachmentList.json"
		const val AttachmentFolderName: String = "AttachmentObject"
		const val NoteFolderName: String = "NoteObject"
		const val ChapterFolderName: String = "ChapterObject"
		const val BucketFolderName: String = "BucketObject"
		const val BucketItemFolderName: String = "BucketItemObject"
		const val TagFolderName: String = "TagObject"
		const val BackupFolderName: String = "Backup"

		const val RootFolderId: String = "appDataFolder"

		const val FolderMimeType = "application/vnd.google-apps.folder"
		const val JsonFileMimeType = "application/json"
		const val OctetStreamFileMimeType = "application/octet-stream"

		sealed class GoogleDriveState() {
			data class Success(
				val lockFileId: String?,
				val chapterFolderId: String,
				val noteFolderId: String,
				val bucketFolderId: String,
				val bucketItemFolderId: String,
				val tagFolderId: String,
				val attachmentFolderId: String,
				val deletedObjectFileId: String,
				val deletedAttachmentFileId: String,
				val backupFolderId: String,
			) : GoogleDriveState()

			object Error : GoogleDriveState()
		}

	}
}

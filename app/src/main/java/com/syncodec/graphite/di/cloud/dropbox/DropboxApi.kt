package com.syncodec.graphite.di.cloud.dropbox

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.annotation.WorkerThread
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.InvalidAccessTokenException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.DeleteErrorException
import com.dropbox.core.v2.files.DeleteResult
import com.dropbox.core.v2.files.DownloadErrorException
import com.dropbox.core.v2.files.ListFolderErrorException
import com.dropbox.core.v2.files.Metadata
import com.dropbox.core.v2.files.UploadErrorException
import com.dropbox.core.v2.files.WriteMode
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.R
import com.syncodec.graphite.di.network.NetworkRequest
import com.syncodec.graphite.utils.alice.deleteSecretData
import com.syncodec.graphite.utils.alice.getSecretData2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import com.syncodec.graphite.di.cloud.dropbox.DropboxOperation.PassiveOperationResult.ListFolderResult
import com.syncodec.graphite.di.cloud.dropbox.DropboxOperation.PassiveOperationResult.DownloadFileResult
import com.syncodec.graphite.di.cloud.dropbox.DropboxOperation.ActiveOperationResult.UploadFileResult
import com.syncodec.graphite.di.cloud.dropbox.DropboxOperation.ActiveOperationResult.DeleteFileResult


class DropboxApi(private val context: Context, private var dbxClientV2: DbxClientV2, private var expireAt: Long) {

	constructor(context: Context, accessToken: String, expireAt: Long) : this(context = context, dbxClientV2 = DbxClientV2(DbxRequestConfig("Graphite"), accessToken), expireAt = expireAt)

	private val remoteSnapshotCache: MutableMap<String, ByteArray> = mutableMapOf()

	private fun isAccessTokenExpired() = System.currentTimeMillis() > expireAt

	suspend fun <T> withConnection(onError: () -> Unit = {}, block: suspend () -> T): T? {
		if (isAccessTokenExpired()) {
			Log.d("rits", "DropboxConnector.refreshTokenToAccessToken")
			val refreshToken = context.getSecretData2(DropboxApi.DROPBOX_REFRESH_TOKEN).getDataOrNull()?.decodeToString() ?: return null
			val requestData = hashMapOf("refreshToken" to refreshToken)
			val exchangeTokenResult = Firebase
				.functions
				.getHttpsCallable("dropboxExchangeRefreshTokenForAccessToken")
				.call(requestData)
				.await()

			val resultData = (exchangeTokenResult.data as HashMap<*, *>)
			when (val response = resultData["response"] as String) {
				"Ok" -> {
					Log.d("npr71", "DropboxConnector.refreshTokenToAccessToken: ${resultData["result"] as String}")
					val result = JSONObject(resultData["result"] as String)
					val expireIn = result.optLong("expires_in")
					val accessToken = result.optString("access_token")
					dbxClientV2 = DbxClientV2(DbxRequestConfig("Graphite"), accessToken)
					expireAt = System.currentTimeMillis() + expireIn * 1000
					return try {
						block()
					} catch (e: InvalidAccessTokenException) {
						if (BuildConfig.DEBUG) e.printStackTrace()
						onError()
						return null
					} catch (e: Exception) {
						if (BuildConfig.DEBUG) e.printStackTrace()
						onError()
						return null
					}
				}

				"Error" -> {
					withContext(Dispatchers.Main) { Toast.makeText(context, context.getText(R.string.toast_error_connecting_with_dropbox), Toast.LENGTH_SHORT).show() }
					onError()
					return null
				}

				else -> {
					withContext(Dispatchers.Main) { Toast.makeText(context, context.getText(R.string.toast_error_connecting_with_dropbox), Toast.LENGTH_SHORT).show() }
					onError()
					return null
				}
			}
		} else return try {
			block()
		} catch (e: InvalidAccessTokenException) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			onError()
			return null
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			onError()
			return null
		}
	}

	@WorkerThread
	suspend fun disconnect() {
		Log.d("npr71", "Dropboxxer.disconnect")
		withConnection(
			onError = {
				context.deleteSecretData(DROPBOX_REFRESH_TOKEN)
				return@withConnection
			}
		) {
			dbxClientV2.auth().tokenRevoke()
			context.deleteSecretData(DROPBOX_REFRESH_TOKEN)
		}
	}

	suspend fun uploadSnapshot(snapshotFile: File) {
		Log.d("npr71", "Dropboxxer.uploadSnapshot: start")
		val snapshotFileName = snapshotFile.name
		try {
			withConnection {
				snapshotFile.inputStream().use { snapshotFileInputStream ->
					dbxClientV2.files().uploadBuilder("/backup/$snapshotFileName").uploadAndFinish(snapshotFileInputStream)
				}
			}
			scanSnapshot()
			Log.d("npr71", "Dropboxxer.uploadSnapshot: success")
			withContext(Dispatchers.Main) { Toast.makeText(context, "Snapshot uploaded", Toast.LENGTH_SHORT).show() }
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			Log.d("npr71", "Dropboxxer.uploadSnapshot: error")
			withContext(Dispatchers.Main) { Toast.makeText(context, "Error uploading snapshot", Toast.LENGTH_SHORT).show() }
		}
	}

	@WorkerThread
	suspend fun scanSnapshot(): List<RemoteSnapshot> {
		return withConnection {
			val listFolderResult = dbxClientV2.files().listFolder("/backup")
			val remoteSnapshotList = listFolderResult.entries.map {
				RemoteSnapshot(remoteName = it.name, pathLower = it.pathLower, pathDisplay = it.pathDisplay, previewUrl = it.previewUrl)
			}.sortedByDescending { it.timestamp }
			return@withConnection remoteSnapshotList
		} ?: listOf()
	}

	/**
	 * @param fileId : [com.dropbox.core.v2.files.Metadata.pathLower] of Dropbox file. Also available at [RemoteSnapshot.pathLower]
	 */
	@WorkerThread
	suspend fun downloadSnapshot(fileId: String): ByteArray? {
		val cachedByteArray = remoteSnapshotCache[fileId]
		return if (cachedByteArray == null) {
			val outputStream = ByteArrayOutputStream()
			withConnection { dbxClientV2.files().downloadBuilder(fileId).download(outputStream) }
			val byteArray = outputStream.toByteArray()
			outputStream.close()
			remoteSnapshotCache[fileId] = byteArray
			byteArray
		} else cachedByteArray
	}

	@WorkerThread
	suspend fun getAccountInfo(): NetworkRequest<DropboxAccountInfo> {
		try {
			val account = withConnection { dbxClientV2.users().currentAccount } ?: return NetworkRequest.Error(Exception("Account is null"))
			return NetworkRequest.Success(
				DropboxAccountInfo(
					name = account.name.displayName,
					email = account.email,
					profilePhotoUrl = account.profilePhotoUrl,
					spaceUsage = DropboxSpaceUsage(
						used = dbxClientV2.users().spaceUsage.used,
						allocated = dbxClientV2.users().spaceUsage.allocation.individualValue.allocated
					)
				)
			)
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			return NetworkRequest.Error(e)
		}
	}

	@WorkerThread
	fun uploadFile(path: String, byteArray: ByteArray, modifiedTimestamp: Long = Instant.now().toEpochMilli()): UploadFileResult {
		try {
			Log.d("rits", "Dropboxxer.uploadFile: ${Date(modifiedTimestamp)}")
			byteArray.inputStream().use { inputStream ->
				val fileMetadata = dbxClientV2
					.files()
					.uploadBuilder(path)
					.withClientModified(Date(modifiedTimestamp))
					.withMode(WriteMode.OVERWRITE)
					.uploadAndFinish(inputStream)
				return UploadFileResult.Success(metadata = fileMetadata)
			}
		} catch (e: IOException) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			return UploadFileResult.Error.NetworkError
		} catch (e: InvalidAccessTokenException) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			return UploadFileResult.Error.CredentialsError
		} catch (e: UploadErrorException) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			return UploadFileResult.Error.UploadError
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			return UploadFileResult.Error.UnknownError(exception = e)
		}
	}

	@WorkerThread
	fun downloadFile(path: String): DownloadFileResult {
		return try {
			val inputStream = dbxClientV2
				.files()
				.download(path)
				.inputStream
			inputStream.use { DownloadFileResult.Success(byteArray = it.readBytes()) }
		} catch (e: IOException) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			DownloadFileResult.Error.NetworkError
		} catch (e: InvalidAccessTokenException) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			return DownloadFileResult.Error.CredentialsError
		} catch (e: DownloadErrorException) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			when (e.errorValue.pathValue.isNotFound) {
				true -> DownloadFileResult.FileNotFound
				false -> DownloadFileResult.Error.DownloadError
			}
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			DownloadFileResult.Error.UnknownError(exception = e)
		}
	}

	@WorkerThread
	fun deleteFile(path: String): DeleteFileResult {
		try {
			val deleteResult = dbxClientV2
				.files()
				.deleteV2(path)
			return DeleteFileResult.Success(deleteResult = deleteResult)
		} catch (e: IOException) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			return DeleteFileResult.Error.NetworkError
		} catch (e: InvalidAccessTokenException) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			return DeleteFileResult.Error.CredentialsError
		} catch (e: DeleteErrorException) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			return if (e.errorValue.isPathLookup) {
				when (e.errorValue.pathLookupValue.isNotFound) {
					true -> DeleteFileResult.Success(deleteResult = DeleteResult( null))
					false -> DeleteFileResult.Error.DeleteError
				}
			} else DeleteFileResult.Error.DeleteError
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			return DeleteFileResult.Error.UnknownError(exception = e)
		}
	}

	@WorkerThread
	fun listFolder(path: String): ListFolderResult {
		val metadataList: MutableList<Metadata> = mutableListOf()
		var cursor: String?    //  = null
		var hasMore: Boolean   //  = false

		try {
			val listFolderResult = dbxClientV2
				.files()
				.listFolderBuilder(path)
				.withIncludeDeleted(false)
				.withRecursive(false)
				.start()

			cursor = listFolderResult.cursor
			hasMore = listFolderResult.hasMore
			metadataList.addAll(listFolderResult.entries)
		} catch (e: IOException) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			return ListFolderResult.Error.NetworkError
		} catch (e: InvalidAccessTokenException) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			return ListFolderResult.Error.CredentialsError
		} catch (e: ListFolderErrorException) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			return if (e.errorValue.isPath) {
				when (e.errorValue.pathValue.isNotFound) {
					true -> ListFolderResult.Error.FolderNotFound
					false -> ListFolderResult.Error.ListFolderError
				}
			} else ListFolderResult.Error.ListFolderError
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			return ListFolderResult.Error.UnknownError(exception = e)
		}

		while (hasMore) {
			try {
				val listFolderResult = dbxClientV2
					.files()
					.listFolderContinue(cursor)
				cursor = listFolderResult.cursor
				hasMore = listFolderResult.hasMore
				metadataList.addAll(listFolderResult.entries)
			} catch (e: IOException) {
				if (BuildConfig.DEBUG) e.printStackTrace()
				return ListFolderResult.Error.NetworkError
			} catch (e: InvalidAccessTokenException) {
				if (BuildConfig.DEBUG) e.printStackTrace()
				return ListFolderResult.Error.CredentialsError
			} catch (e: ListFolderErrorException) {
				if (BuildConfig.DEBUG) e.printStackTrace()
				return if (e.errorValue.isPath) {
					when (e.errorValue.pathValue.isNotFound) {
						true -> ListFolderResult.Error.FolderNotFound
						false -> ListFolderResult.Error.ListFolderError
					}
				} else ListFolderResult.Error.ListFolderError
			} catch (e: Exception) {
				if (BuildConfig.DEBUG) e.printStackTrace()
				return ListFolderResult.Error.UnknownError(exception = e)
			}
		}

		return ListFolderResult.Success(metadataList = metadataList)
	}

	companion object {

		const val DROPBOX_REFRESH_TOKEN = "dropbox_refresh_token"

		fun getDropboxFilePath(pathLower: String) = "https://www.dropbox.com/home/Apps/Graphite%20Data/$pathLower"

		data class DropboxAccountInfo(
			val name: String,
			val email: String,
			val profilePhotoUrl: String?,
			val spaceUsage: DropboxSpaceUsage,
		)

		data class DropboxSpaceUsage(
			val used: Long,
			val allocated: Long,
		)

		data class RemoteSnapshot(
			val name: String,
			val timestampPretty: String,
			val timestamp: Long,
			val pathLower: String,
			val pathDisplay: String,
			val previewUrl: String = pathDisplay,
		) {
			constructor(remoteName: String, pathLower: String, pathDisplay: String, previewUrl: String?) : this(
				name = remoteName,
				timestampPretty = try {
					val timestamp = remoteName.substringAfterLast("_").substringBefore(".zip")
					DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(timestamp).let {
						DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm:ss a").format(it)
					}
				} catch (e: Exception) {
					if (BuildConfig.DEBUG) e.printStackTrace()
					remoteName
				},
				timestamp = try {
					val timestamp = remoteName.substringAfterLast("_").substringBefore(".zip")
					LocalDateTime.parse(timestamp).toEpochSecond(ZoneOffset.UTC)
				} catch (e: Exception) {
					if (BuildConfig.DEBUG) e.printStackTrace()
					0
				},
				pathLower = pathLower,
				pathDisplay = pathDisplay,
				previewUrl = previewUrl ?: pathDisplay,
			)
		}

		enum class Path(val path: String) {
			Root("/sync"),
			Lock("/sync/lock.json"),
			Chapter("/sync/chapter"),
		}
	}
}
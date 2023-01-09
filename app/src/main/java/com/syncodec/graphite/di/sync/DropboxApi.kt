package com.syncodec.graphite.di.sync

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.dropbox.core.BadRequestException
import com.dropbox.core.DbxAppInfo
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.DbxWebAuth
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.users.SpaceUsage
import com.google.firebase.functions.FirebaseFunctions
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.alice.AliceRequestResult
import com.syncodec.graphite.utils.alice.getSecretData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


data class DbxToken(val accessToken : String, val refreshToken : String)

data class DropboxApiResponse<T>(
	val success: Boolean,
	val result: T? = null,
	val exception: Exception? = null,
	val message: String? = null,
)

interface DropboxApi {

	fun dropboxSignIn(context : Context) {
		val authorizeUrl =
//			"https://www.dropbox.com/oauth2/authorize?client_id=wqgzkie6sm7xxvw&response_type=code&grant_type=authorization_code&token_access_type=offline&scope=account_info.read%20files.metadata.write%20files.metadata.read%20files.content.write%20files.content.read%20file_requests.write%20file_requests.read&redirect_uri=https%3A%2F%2Fus-central1-graphite-diary.cloudfunctions.net%2FdropboxCallback"
//			"https://www.dropbox.com/oauth2/authorize?client_id=wqgzkie6sm7xxvw&response_type=code&grant_type=authorization_code&token_access_type=offline&scope=account_info.read%20files.metadata.write%20files.metadata.read%20files.content.write%20files.content.read%20file_requests.write%20file_requests.read&redirect_uri=https%3A%2F%2F192.168.2.154%3A8001%2Fgraphite-diary%2Fus-central1%2FdropboxCallback"
//			"https://www.dropbox.com/oauth2/authorize?client_id=wqgzkie6sm7xxvw&response_type=code&grant_type=authorization_code&token_access_type=offline&scope=account_info.read%20files.metadata.write%20files.metadata.read%20files.content.write%20files.content.read%20file_requests.write%20file_requests.read&redirect_uri=http%3A%2F%2Flocalhost%3A5001%2Fgraphite-diary%2Fus-central1%2FexchangeDropboxCodeForToken"
			"https://www.dropbox.com/oauth2/authorize?client_id=wqgzkie6sm7xxvw&response_type=code&grant_type=authorization_code&token_access_type=offline&scope=account_info.read%20files.metadata.write%20files.metadata.read%20files.content.write%20files.content.read%20file_requests.write%20file_requests.read"

		Intent(Intent.ACTION_VIEW).apply {
			data = Uri.parse(authorizeUrl)
			context.startActivity(this)
		}
	}

	fun authenticateWithDropbox(code : String, dropboxApiResponse : (DropboxApiResponse<DbxToken>) -> Unit) {
		CoroutineScope(Dispatchers.IO).launch {
			val data = hashMapOf("code" to code)
			FirebaseFunctions
				.getInstance()
				.getHttpsCallable("exchangeDropboxCodeForToken")
				.call(data)
				.addOnSuccessListener {
//					it.data
				}
				.addOnFailureListener {
					it.printStackTrace()
				}

			try {
				val config = DbxRequestConfig("Graphite")
				val appInfo = DbxAppInfo("wqgzkie6sm7xxvw", "kgu8ymntbtwnsxc")

				val dbxAuthFinish = DbxWebAuth(config, appInfo).finishFromCode(code)

				dropboxApiResponse(DropboxApiResponse(true, DbxToken(dbxAuthFinish.accessToken, dbxAuthFinish.refreshToken), null, null))
			} catch (e : BadRequestException) {
				dropboxApiResponse(DropboxApiResponse(false, null, e, "Code has expired. Please try again."))
			} catch (e : Exception) {
				dropboxApiResponse(DropboxApiResponse(false, null, e, "Something went wrong. Please try again."))
			}
		}
	}

	fun Context.getDropboxAccessToken(callback: (DropboxApiResponse<DbxToken?>) -> Unit) {
		CoroutineScope(Dispatchers.IO).launch {
			getSecretData("dropbox_refresh_token").let {
				if (it.result == AliceRequestResult.KEY_NOT_FOUND) null else it.data?.decodeToString()
			}.let {refreshToken ->
				try {
					val config = DbxRequestConfig("Graphite")
					DbxCredential("", 0, refreshToken, "wqgzkie6sm7xxvw", "kgu8ymntbtwnsxc").refresh(config).let {
						callback(DropboxApiResponse(true, refreshToken?.let { it1 -> DbxToken(it.accessToken, it1) }, null, null))
					}
				} catch (e : Exception) {
					callback(DropboxApiResponse(false, null, e, "Something went wrong. Please try again."))
				}
			}
		}
	}

	fun Context.testDropboxConnectionConnection(callback : (DropboxApiResponse<Boolean>) -> Unit) {
		try {
			CoroutineScope(Dispatchers.IO).launch {
				getDropboxAccessToken {
					if (it.success) {
						it.result?.let { dbxToken ->
							DbxClientV2(DbxRequestConfig("Graphite"), dbxToken.accessToken).users().getCurrentAccount().let {
								callback(DropboxApiResponse(true, true, null, null))
							}
						}
					} else {
						callback(DropboxApiResponse(false, null, it.exception, it.message))
					}
				}
			}
		} catch (e : Exception) {
			callback(DropboxApiResponse(false, null, e, "Something went wrong. Please try again."))
		}
	}

	fun Context.getDropboxStorageData(callback : (DropboxApiResponse<SpaceUsage?>) -> Unit) {
		try {
			getDropboxAccessToken {
				if (it.success) {
					val config = DbxRequestConfig("Graphite")
					val client = DbxClientV2(config, it.result?.accessToken ?: "")
					callback(DropboxApiResponse(true, client.users().spaceUsage, null, null))
				}
			}
		} catch (e : Exception) {
			callback(DropboxApiResponse(false, null, e, "Something went wrong. Please try again."))
		}
	}

	fun Context.disconnectFromDropbox(callback : (DropboxApiResponse<Boolean>) -> Unit) {
		try {
			Intent(Intent.ACTION_VIEW, Uri.parse("https://www.dropbox.com/account/connected_apps")).apply {
				startActivity(this)
			}
		} catch (e : Exception) {
		}

		try {
			getDropboxAccessToken {
				if (it.success) {
					val config = DbxRequestConfig("Graphite")
					val client = DbxClientV2(config, it.result?.accessToken ?: "")
					client.auth().tokenRevoke()
					callback(DropboxApiResponse(true, true, null, null))
				}
			}
		} catch (e : Exception) {
			callback(DropboxApiResponse(false, null, e, "Something went wrong. Please try again."))
		}
	}

	fun Context.checkSyncPermission(isSyncEnabled : (Boolean) -> Unit) {
		val dataStoreInstance = DataStoreInstance(this)
		CoroutineScope(Dispatchers.IO).launch {
			dataStoreInstance.getIsSyncEnabled().collect {
				isSyncEnabled(it)
				this.cancel()
			}
		}
	}

	fun Context.connectWithDropbox(callback : (DropboxApiResponse<Pair<DbxToken?, SpaceUsage?>>) -> Unit) {
		getDropboxAccessToken {
			if (it.success) {
				getDropboxStorageData { storageData ->
					if (storageData.success) {
						callback(DropboxApiResponse(true, Pair(it.result, storageData.result), null, null))
					} else {
						callback(DropboxApiResponse(false, null, storageData.exception, storageData.message))
					}
				}
			} else {
				callback(DropboxApiResponse(false, null, it.exception, it.message))
			}
		}
	}
}

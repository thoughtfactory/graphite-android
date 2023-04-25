package com.syncodec.graphite.di.sync.dropbox

import android.content.Context
import androidx.annotation.WorkerThread
import com.dropbox.core.BadRequestException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.InvalidAccessTokenException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.ListFolderErrorException
import com.dropbox.core.v2.files.Metadata
import com.dropbox.core.v2.users.DbxUserUsersRequests
import com.dropbox.core.v2.users.FullAccount
import com.dropbox.core.v2.users.SpaceUsage
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.syncodec.graphite.utils.alice.AliceRequestResult
import com.syncodec.graphite.utils.alice.deleteSecretData
import com.syncodec.graphite.utils.alice.getSecretData
import com.syncodec.graphite.utils.alice.putSecretData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.InputStream


class DBox(private val context : Context) {

	private var dbxAccessToken : Pair<String, Long>? = null

	@WorkerThread
	fun exchangeCodeForToken(code : String, callback : (ExchangeCodeForTokenResponse) -> Unit) {
		callback(ExchangeCodeForTokenResponse.Loading)
		try {
			val data = hashMapOf("code" to code)

			Firebase
				.functions
				.getHttpsCallable("dropboxExchangeCodeForToken2")
				.call(data)
				.addOnSuccessListener {
					try {
						((it.data as HashMap<*, *>)["data"] as HashMap<*, *>)
							.let { data ->
								val refreshToken = data["refresh_token"] as String
								val accessToken = data["access_token"] as String

								context.putSecretData("dropbox_refresh_token", refreshToken)
								callback(ExchangeCodeForTokenResponse.Success)
							}
					} catch (e : Exception) {
						callback(ExchangeCodeForTokenResponse.Error(e, "Something went wrong. Please try again."))
					}
				}
				.addOnFailureListener {
					callback(ExchangeCodeForTokenResponse.Error(it, "Something went wrong. Please try again."))
				}
		} catch (e : BadRequestException) {
			callback(ExchangeCodeForTokenResponse.Error(e, "Code has expired. Please try again."))
		} catch (e : Exception) {
			callback(ExchangeCodeForTokenResponse.Error(e, "Something went wrong. Please try again."))
			e.printStackTrace()
		}
	}

	@WorkerThread
	fun getAccessToken(callback : (AccessTokenResponseResponse) -> Unit) {
		if (dbxAccessToken != null && dbxAccessToken !!.second > System.currentTimeMillis()) {
			try {
				DbxClientV2(DbxRequestConfig("Graphite"), dbxAccessToken !!.first).check().user().result.let {
					callback(AccessTokenResponseResponse.Success(dbxAccessToken !!.first))
					return
				}
			} catch (e : Exception) {
				e.printStackTrace()
			}
		} else {
			context.getSecretData("dropbox_access_token").let {
				if (it.result == AliceRequestResult.SUCCESS) {
					val jsonObject = it.data?.decodeToString()?.let { it1 -> JSONObject(it1) }
					val accessToken = jsonObject?.getString("accessToken")
					val expiresAt = jsonObject?.getLong("expiresAt")
					if (accessToken != null && expiresAt != null) {
						dbxAccessToken = Pair(accessToken, expiresAt)
						if (expiresAt > System.currentTimeMillis()) {
							try {
								DbxClientV2(DbxRequestConfig("Graphite"), dbxAccessToken !!.first).check().user().result.let {
									callback(AccessTokenResponseResponse.Success(dbxAccessToken !!.first))
									return
								}
							} catch (e : Exception) {
								e.printStackTrace()
							}
						}
					}
				}
			}
		}
		context.getSecretData("dropbox_refresh_token").let {
			if (it.result == AliceRequestResult.KEY_NOT_FOUND) {
				callback(AccessTokenResponseResponse.KeyNotFound)
				null
			} else it.data?.decodeToString()
		}?.let { refreshToken ->
			try {
				val data = hashMapOf("refreshToken" to refreshToken)

				Firebase
					.functions
					.getHttpsCallable("dropboxExchangeRefreshTokenForAccessToken")
					.call(data)
					.addOnSuccessListener {
						val data = it.data as HashMap<*, *>
						try {
							when (data["response"]) {
								"Ok" -> {
									val result = JSONObject(data["result"] as String)
									val accessToken = result.getString("access_token")
									val expiresIn = result.getLong("expires_in")
									dbxAccessToken = Pair(accessToken, System.currentTimeMillis() + (expiresIn * 1000))
									val jsonObject = JSONObject()
									jsonObject.put("accessToken", accessToken)
									jsonObject.put("expiresAt", System.currentTimeMillis() + (expiresIn * 1000))
									context.putSecretData("dropbox_access_token", jsonObject.toString())
									callback(AccessTokenResponseResponse.Success(accessToken))
								}

								"Error" -> callback(AccessTokenResponseResponse.Error(Exception("Error"), "Something went wrong. Please try again."))
								else -> callback(AccessTokenResponseResponse.Error(Exception("Error"), "Something went wrong. Please try again."))
							}
						} catch (e : Exception) {
							callback(AccessTokenResponseResponse.Error(e, "Something went wrong. Please try again."))
						}
					}
					.addOnFailureListener {
						callback(AccessTokenResponseResponse.Error(it, "Something went wrong. Please try again."))
					}
			} catch (e : Exception) {
				callback(AccessTokenResponseResponse.Error(e, "Something went wrong. Please try again."))
			}
		}
	}

	@WorkerThread
	fun testConnection(callback : (TestConnectionResponse) -> Unit) {
		callback(TestConnectionResponse.Loading)
		getAccessToken { accessTokenResponseResponse ->
			when (accessTokenResponseResponse) {
				is AccessTokenResponseResponse.Success -> {
					try {
						DbxClientV2(DbxRequestConfig("Graphite"), accessTokenResponseResponse.accessToken)
							.users()
							.let { callback(TestConnectionResponse.Success(it.currentAccount, it.spaceUsage)) }
					} catch (e : Exception) {
						callback(TestConnectionResponse.Error(e, "Something went wrong. Please try again."))
					}
				}

				is AccessTokenResponseResponse.KeyNotFound -> {
					callback(TestConnectionResponse.NotLoggedIn)
				}

				is AccessTokenResponseResponse.Error -> {
					accessTokenResponseResponse.exception.printStackTrace()
					when (accessTokenResponseResponse.exception) {
						is JSONException -> {
							if (accessTokenResponseResponse.exception.message == "No value for access_token") {
								callback(TestConnectionResponse.NotLoggedIn)
							} else {
								callback(TestConnectionResponse.Error(accessTokenResponseResponse.exception, accessTokenResponseResponse.message))
							}
						}

						is InvalidAccessTokenException -> {
							callback(TestConnectionResponse.NotLoggedIn)
						}

						else -> {
							callback(TestConnectionResponse.Error(accessTokenResponseResponse.exception, accessTokenResponseResponse.message))
						}
					}
				}
			}
		}
	}

	@WorkerThread
	fun getSnapshot(callback : (GetSnapshotResponse) -> Unit) {
		callback(GetSnapshotResponse.Loading)
		getAccessToken { accessTokenResponseResponse ->
			when (accessTokenResponseResponse) {
				is AccessTokenResponseResponse.Success -> {
					val dbxClientV2 = DbxClientV2(DbxRequestConfig("Graphite"), accessTokenResponseResponse.accessToken)
					dbxClientV2
						.files()
						.listFolder("/backup")
						.let {
							if (it.entries.isEmpty()) {
								callback(GetSnapshotResponse.SuccessEmpty())
							} else {
								callback(GetSnapshotResponse.Success(it.entries))
								var hasMore = it.hasMore
								var cursor = it.cursor
								while (hasMore) {
									dbxClientV2
										.files()
										.listFolderContinue(cursor)
										.let {
											hasMore = it.hasMore
											cursor = it.cursor
											callback(GetSnapshotResponse.SuccessContinue(it.entries))
										}
								}
							}
						}
					try {
					} catch (e : Exception) {
						callback(GetSnapshotResponse.Error(e, "Something went wrong. Please try again."))
					}
				}

				is AccessTokenResponseResponse.KeyNotFound -> {
					callback(GetSnapshotResponse.Error(Exception("Key not found"), "Not logged in."))
				}

				is AccessTokenResponseResponse.Error -> {
					callback(GetSnapshotResponse.Error(accessTokenResponseResponse.exception, accessTokenResponseResponse.message))
				}
			}
		}
	}

	@WorkerThread
	fun disconnect(callback : () -> Unit) {
		getAccessToken { accessTokenResponseResponse ->
			context.deleteSecretData("dropbox_refresh_token")
			context.deleteSecretData("dropbox_access_token")
			dbxAccessToken = null
			when (accessTokenResponseResponse) {
				is AccessTokenResponseResponse.Success -> {
					try {
						DbxClientV2(DbxRequestConfig("Graphite"), accessTokenResponseResponse.accessToken)
							.auth()
							.tokenRevoke()
						callback()
					} catch (e : Exception) {
						callback()
					}
				}

				is AccessTokenResponseResponse.KeyNotFound -> {
					callback()
				}

				is AccessTokenResponseResponse.Error -> {
					callback()
				}
			}
		}
	}

	val snapshot : Snapshot = Snapshot()

	inner class Snapshot {

		fun observeBackupFolder(onChange : (Boolean) -> Unit) {
			val config = DbxRequestConfig("Graphite")
			getAccessToken { accessTokenResponseResponse ->
				CoroutineScope(Dispatchers.IO).launch {
					if (accessTokenResponseResponse is AccessTokenResponseResponse.Success) {
						val dbxClientV2 = DbxClientV2(config, accessTokenResponseResponse.accessToken)
						try {
							dbxClientV2.files().listFolder("/backup").cursor.let {
								dbxClientV2.files().listFolderLongpoll(it).let { onChange(true) }
							}
						} catch (e : ListFolderErrorException) {
							if (e.errorValue.pathValue.isNotFound) {
								dbxClientV2.files().createFolderV2("/backup").let {
									dbxClientV2.files().listFolder("/backup").cursor.let {
										dbxClientV2.files().listFolderLongpoll(it).let { onChange(true) }
									}
								}
							}
						} catch (e : Exception) {
							try {
								dbxClientV2.files().createFolderV2("/backup").let {
									dbxClientV2.files().listFolder("/backup").cursor.let {
										dbxClientV2.files().listFolderLongpoll(it).let { onChange(true) }
									}
								}
							} catch (_ : Exception) {
							}
						}
					}
				}
			}
		}

		fun uploadSnapshot(file : File) {
			val config = DbxRequestConfig("Graphite")
			getAccessToken { accessTokenResponseResponse ->
				CoroutineScope(Dispatchers.IO).launch {
					if (accessTokenResponseResponse is AccessTokenResponseResponse.Success) {
						val dbxClientV2 = DbxClientV2(config, accessTokenResponseResponse.accessToken)
						try {
							dbxClientV2.files().uploadBuilder("/backup/${file.name}").uploadAndFinish(file.inputStream())
						} catch (e : Exception) {
						}
					}
				}
			}
		}

		fun downloadSnapshot(metadata : Metadata, callback : (DownloadSnapshotResponse) -> Unit) {
			val config = DbxRequestConfig("Graphite")
			getAccessToken { accessTokenResponseResponse ->
				CoroutineScope(Dispatchers.IO).launch {
					if (accessTokenResponseResponse is AccessTokenResponseResponse.Success) {
						val dbxClientV2 = DbxClientV2(config, accessTokenResponseResponse.accessToken)
						try {
							dbxClientV2.files().downloadBuilder(metadata.pathLower).start().inputStream.let {
								callback(DownloadSnapshotResponse.Success(metadata.name, it))
							}
						} catch (e : Exception) {
							callback(DownloadSnapshotResponse.Error(e, "Something went wrong. Please try again."))
						}
					} else callback(
						DownloadSnapshotResponse.Error(
							exception = Exception("Something went wrong. Please try again."),
							message = "Something went wrong. Please try again."
						)
					)
				}
			}
		}
	}

	companion object {
		sealed class ExchangeCodeForTokenResponse {
			object Loading : ExchangeCodeForTokenResponse()
			object Success : ExchangeCodeForTokenResponse()
			class Error(val exception : Exception, val message : String) : ExchangeCodeForTokenResponse()
		}

		sealed class TestConnectionResponse {
			object Loading : TestConnectionResponse()
			class Success(val fullAccount : FullAccount, val spaceUsage : SpaceUsage) : TestConnectionResponse()
			object NotLoggedIn : TestConnectionResponse()
			class Error(val exception : Exception, val message : String) : TestConnectionResponse()
		}

		sealed class AccessTokenResponseResponse {
			class Success(val accessToken : String) : AccessTokenResponseResponse()
			object KeyNotFound : AccessTokenResponseResponse()
			class Error(val exception : Exception, val message : String) : AccessTokenResponseResponse()
		}

		sealed class GetSnapshotResponse {
			object Loading : GetSnapshotResponse()
			class Success(val snapshotList : List<Metadata>) : GetSnapshotResponse()
			class SuccessContinue(val snapshotList : List<Metadata>) : GetSnapshotResponse()
			class SuccessEmpty() : GetSnapshotResponse()
			class Error(val exception : Exception, val message : String) : GetSnapshotResponse()
		}

		sealed class DownloadSnapshotResponse {
			class Success(val fileName : String, val inputStream : InputStream) : DownloadSnapshotResponse()
			class Error(val exception : Exception, val message : String) : DownloadSnapshotResponse()
		}

		val DROPBOX_CONNECT =
			"https://www.dropbox.com/oauth2/authorize?client_id=wqgzkie6sm7xxvw&response_type=code&token_access_type=offline&redirect_uri=https://us-central1-graphite-diary.cloudfunctions.net/dropboxCallback"
	}
}

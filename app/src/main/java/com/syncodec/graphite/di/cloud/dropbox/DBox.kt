package com.syncodec.graphite.di.cloud.dropbox

import android.content.Context
import androidx.annotation.WorkerThread
import com.dropbox.core.BadRequestException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.InvalidAccessTokenException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.ListFolderErrorException
import com.dropbox.core.v2.files.Metadata
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.syncodec.graphite.di.cloud.dropbox.DBox.Companion.TestConnectionResponse.Error
import com.syncodec.graphite.di.cloud.dropbox.DBox.Companion.TestConnectionResponse.Loading
import com.syncodec.graphite.di.cloud.dropbox.DBox.Companion.TestConnectionResponse.NotLoggedIn
import com.syncodec.graphite.di.cloud.dropbox.DBox.Companion.TestConnectionResponse.Success
import com.syncodec.graphite.utils.alice.deleteSecretData
import com.syncodec.graphite.utils.alice.putSecretData
import com.syncodec.graphite.utils.dataStore.SyncDataStoreInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
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
//								!!!	Is this the right way to do this?	!!!
								val refreshToken = data["refresh_token"] as String
								val accessToken = data["access_token"] as String

								context.putSecretData("dropbox_refresh_token", refreshToken)
								val syncDataStoreInstance = SyncDataStoreInstance(context)
								syncDataStoreInstance.setSyncProvider(SyncDataStoreInstance.Companion.SyncProvider.Dropbox)
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
//			e.printStackTrace()
		}
	}

	@WorkerThread
	fun getAccessToken(callback : (AccessTokenResponseResponse) -> Unit) {
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
							.let { callback(TestConnectionResponse.Success(
								it.currentAccount.name.displayName,
								it.getCurrentAccount().email,
								it.getCurrentAccount().profilePhotoUrl,
								it.spaceUsage.used,
								it.spaceUsage.allocation.individualValue.allocated
							)) }
					} catch (e : Exception) {
						callback(TestConnectionResponse.Error(e, "Something went wrong. Please try again."))
					}
				}

				is AccessTokenResponseResponse.KeyNotFound -> {
					callback(TestConnectionResponse.NotLoggedIn)
				}

				is AccessTokenResponseResponse.Error -> {
//					accessTokenResponseResponse.exception.printStackTrace()
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

			val syncDataStoreInstance = SyncDataStoreInstance(context)
			syncDataStoreInstance.setSyncProvider(SyncDataStoreInstance.Companion.SyncProvider.NotConfigured)
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

		/**
		 * Ping remote server to check if user is logged in and get user info
		 *   *   [Loading]: Loading.
		 *   *   [Success]: User is logged in. [name], [email], [profilePictureUrl], [spaceUsed], [spaceTotal] are available.
		 *   *   [NotLoggedIn]: User is not logged in.
		 *   *   [Error]: Something went wrong. [exception] and [message] are available.
		 */
		sealed class TestConnectionResponse {
			object Loading : TestConnectionResponse()
			class Success(
				val name : String?,
				val email : String?,
				val profilePictureUrl : String?,
				val spaceUsed : Long?,
				val spaceTotal : Long?,
			) : TestConnectionResponse()
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

	}
}

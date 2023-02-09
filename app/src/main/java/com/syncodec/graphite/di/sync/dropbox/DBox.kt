package com.syncodec.graphite.di.sync.dropbox

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import com.dropbox.core.BadRequestException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.users.FullAccount
import com.dropbox.core.v2.users.SpaceUsage
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.utils.alice.AliceRequestResult
import com.syncodec.graphite.utils.alice.getSecretData
import com.syncodec.graphite.utils.alice.putSecretData


class DBox(private val context: Context) {

	@WorkerThread
	fun exchangeCodeForToken(code : String, callback : (DropboxResponse) -> Unit) {
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
								val accessToken = data["access_token"] as String
								val refreshToken = data["refresh_token"] as String

								context.putSecretData("dropbox_refresh_token", refreshToken)

								if (BuildConfig.DEBUG) Log.d("npr71", "DBox : accessToken: $accessToken")
								if (BuildConfig.DEBUG) Log.d("npr71", "DBox : data: $data")

								callback(DropboxResponse.Success(null))
							}
					} catch (e : Exception) {
						callback(DropboxResponse.Error(e, "Something went wrong. Please try again."))
					}
				}
				.addOnFailureListener {
					callback(DropboxResponse.Error(it, "Something went wrong. Please try again."))
				}
		} catch (e : BadRequestException) {
			callback(DropboxResponse.Error(e, "Code has expired. Please try again."))
		} catch (e : Exception) {
			callback(DropboxResponse.Error(e, "Something went wrong. Please try again."))
			e.printStackTrace()
		}
	}

	@WorkerThread
	fun getAccessToken(callback : (DropboxResponse) -> Unit) {
		if (BuildConfig.DEBUG) {
			callback(DropboxResponse.Success("sl.BYfMoQEjfaxSMMnockXtZZsXQ2CpyTW8-k9lOy2ilun7CcqiM7L4m9oOp0vg7KDMn4Qek3aWKebkpGJ5c627CXu50qN4FpFzJRGL1I9hvjufn0OGlaVNETYWlS2Aka-ryMvhKs2g"))
			return
		}

		context.getSecretData("dropbox_refresh_token").let {
			if (it.result == AliceRequestResult.KEY_NOT_FOUND) null else it.data?.decodeToString()
		}.let { refreshToken ->
			try {
				val data = hashMapOf("refreshToken" to refreshToken)

				Firebase
					.functions
					.getHttpsCallable("dropboxExchangeRefreshTokenForAccessToken")
					.call(data)
					.addOnSuccessListener {
						try {
							((it.data as HashMap<*, *>)["data"] as HashMap<*, *>)
								.let { data ->
									val accessToken = data["access_token"] as String
									callback(DropboxResponse.Success(accessToken))
								}
						} catch (e : Exception) {
							callback(DropboxResponse.Error(e, "Something went wrong. Please try again."))
						}
					}
					.addOnFailureListener {
						callback(DropboxResponse.Error(it, "Something went wrong. Please try again."))
					}
			} catch (e : Exception) {
				callback(DropboxResponse.Error(e, "Something went wrong. Please try again."))
			}
		}
	}

	@WorkerThread
	fun getDropboxStorageData(accessToken : String, callback : (DropboxResponse) -> Unit) {
		try {
			val config = DbxRequestConfig("Graphite")
			val client = DbxClientV2(config, accessToken)
			callback(DropboxResponse.Success(client.users().spaceUsage))
		} catch (e : Exception) {
			callback(DropboxResponse.Error(e, "Something went wrong. Please try again."))
		}
	}

	@WorkerThread
	fun testConnectionConnection(callback : (DropboxResponse) -> Unit) {
		try {
			getAccessToken { dropboxResponse ->
				if (dropboxResponse is DropboxResponse.Success<*>) {
					dropboxResponse.result?.let { accessToken ->
						DbxClientV2(DbxRequestConfig("Graphite"), accessToken as String).users().let { usersRequests ->
							callback(DropboxResponse.Success(Pair(usersRequests.currentAccount, usersRequests.spaceUsage)))
						}
					} ?: callback(DropboxResponse.Error(Exception("Something went wrong. Please try again."), "Something went wrong. Please try again."))
				} else {
					callback(DropboxResponse.Error(Exception("Something went wrong. Please try again."), "Something went wrong. Please try again."))
				}
			}
		} catch (e : Exception) {
			callback(DropboxResponse.Error(e, "Something went wrong. Please try again."))
		}
	}


	companion object {

		sealed class DropboxState {
			object Init : DropboxState()
			object Loading : DropboxState()
			object TokenExpired : DropboxState()
			object Error : DropboxState()
			object NotAuthenticated : DropboxState()
			object NetworkError : DropboxState()
			class Connected(val fullAccount : FullAccount, val spaceUsage : SpaceUsage) : DropboxState()
		}


		private const val CONNECT_WITH_DROPBOX = "graphite-diary/us-central1/connectWithDropbox"

		object Debug {
			private const val NGROK_ADDRESS = "https://f22d-2001-1970-5d1f-d000-00-dcfb.ngrok.io"

			const val DROPBOX_CONNECT = "$NGROK_ADDRESS/$CONNECT_WITH_DROPBOX"
		}

		object Production {
			private const val FIREBASE_ADDRESS = "https://us-central1-graphite-diary.cloudfunctions.net"

			const val DROPBOX_CONNECT = "$FIREBASE_ADDRESS/$CONNECT_WITH_DROPBOX"
		}

		//		val DROPBOX_AUTH_URL = if (BuildConfig.DEBUG) Debug.DROPBOX_CONNECT else Production.DROPBOX_CONNECT
		val DROPBOX_CONNECT =
			"https://www.dropbox.com/oauth2/authorize?client_id=wqgzkie6sm7xxvw&response_type=code&token_access_type=offline&redirect_uri=https://us-central1-graphite-diary.cloudfunctions.net/dropboxCallback"
	}
}

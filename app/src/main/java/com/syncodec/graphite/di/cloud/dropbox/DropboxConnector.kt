package com.syncodec.graphite.di.cloud.dropbox

import android.content.Context
import android.util.Log
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.utils.alice.getSecretData2
import com.syncodec.graphite.utils.alice.putSecretData
import kotlinx.coroutines.tasks.await
import org.json.JSONObject


class DropboxConnector(private val context: Context) {

	private var dropboxApi: DropboxApi? = null

	/**
	 * @return [ExchangeCodeForTokenResponse.Success] if success else [ExchangeCodeForTokenResponse.Error]
	 */
	suspend fun exchangeCodeForToken(code: String) : ExchangeCodeForTokenResponse {
		val requestData = hashMapOf("code" to code)
		val exchangeCodeResult = Firebase
			.functions
			.getHttpsCallable("dropboxExchangeCodeForToken2")
			.call(requestData)
			.await()

		return try {
			Log.d("npr71", "DropboxConnector.exchangeCodeForToken: ${exchangeCodeResult.data}")
			val resultData = (exchangeCodeResult.data as HashMap<*, *>)["data"] as HashMap<*, *>
			val accessToken = resultData["access_token"] as String
			val refreshToken = resultData["refresh_token"] as String
			val expireIn = resultData["expires_in"] as Int

			context.putSecretData(DropboxApi.DROPBOX_REFRESH_TOKEN, refreshToken)

			ExchangeCodeForTokenResponse.Success(accessToken = accessToken, refreshToken = refreshToken, expireAt = System.currentTimeMillis() + expireIn * 1000)
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			ExchangeCodeForTokenResponse.Error(e)
		}
	}

	/**
	 * @return Pair of accessToken and expireAt in milliseconds
	 * @throws com.google.firebase.functions.FirebaseFunctionsException if there is an error while refreshing token
	 */
	private suspend fun getAccessToken(refreshToken: String) : Pair<String, Long> {
		val requestData = hashMapOf("refreshToken" to refreshToken)

		try {
			val exchangeTokenResult = Firebase
				.functions
				.getHttpsCallable("dropboxExchangeRefreshTokenForAccessToken")
				.call(requestData)
				.await()

			val resultData = (exchangeTokenResult.data as HashMap<*, *>)
			when(val response = resultData["response"] as String) {
				"Ok" -> {
					Log.d("npr71", "DropboxConnector.refreshTokenToAccessToken: ${resultData["result"] as String}")
					val result = JSONObject(resultData["result"] as String)
					val expireIn = result.optLong("expires_in")
					val accessToken = result.optString("access_token")
					return Pair(accessToken, System.currentTimeMillis() + expireIn * 1000)
				}
				"Error" -> throw Exception("Error while refreshing token")
				else -> throw Exception("Unknown response: $response")
			}
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			throw e
		}
	}

	suspend fun refreshConnection() : DropboxApi? {
		val refreshToken = context.getSecretData2(DropboxApi.DROPBOX_REFRESH_TOKEN).getDataOrNull()?.decodeToString()
		return if (refreshToken == null) null
		else {
			val (accessToken, expireAt) = getAccessToken(refreshToken)
			val dbxRequestConfig = DbxRequestConfig("Graphite")
			val dbxClientV2 = DbxClientV2(dbxRequestConfig, accessToken)
			DropboxApi(context = context, dbxClientV2 =  dbxClientV2, expireAt = expireAt)
		}
	}

	/**
	 * Tries to auto connect to dropbox. If not connected, returns null
	 */
	suspend fun connect() : DropboxApi? {
		if (dropboxApi == null) dropboxApi = refreshConnection()
		return dropboxApi
	}

	fun disconnect() {
		dropboxApi = null
	}

	companion object {

		const val DROPBOX_CONNECT_URL = "https://www.dropbox.com/oauth2/authorize?client_id=wqgzkie6sm7xxvw&response_type=code&token_access_type=offline&redirect_uri=https://us-central1-graphite-diary.cloudfunctions.net/dropboxCallback"

		sealed class ExchangeCodeForTokenResponse {
			/**
			 * @param accessToken : Access token to be used for future API calls
			 * @param refreshToken : Refresh token to be used for future access token refresh
			 * @param expireAt : Expire time of access token in milliseconds
			 */
			data class Success(val accessToken: String, val refreshToken: String, val expireAt: Long) : ExchangeCodeForTokenResponse()
			class Error(val e: Exception) : ExchangeCodeForTokenResponse()
		}

		/**
		 * [Init] Default state when assigned
		 *
		 * [Connecting] When the user is trying to connect to dropbox
		 *
		 * [NotConnected] When the user is not connected to dropbox
		 *
		 * [Connected] When the user is connected to dropbox
		 *
		 * [Error] When there is an error while connecting to dropbox
		 */
		sealed class DropBoxConnection {
			data object Init : DropBoxConnection()
			data object Connecting : DropBoxConnection()
			data object NotConnected : DropBoxConnection()
			data object Connected : DropBoxConnection()
			data class Error(val exception: Exception) : DropBoxConnection()
		}
	}
}

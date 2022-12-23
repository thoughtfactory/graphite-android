package com.syncodec.graphite.di.sync

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.dropbox.core.DbxAppInfo
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.DbxWebAuth
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.users.SpaceUsage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


data class DbxToken(val accessToken : String, val refreshToken : String)

class DropboxApi {

	fun signIn(context : Context) {
		val authorizeUrl =
			"https://www.dropbox.com/oauth2/authorize?client_id=wqgzkie6sm7xxvw&response_type=code&grant_type=authorization_code&token_access_type=offline&scope=account_info.read%20files.metadata.write%20files.metadata.read%20files.content.write%20files.content.read%20file_requests.write%20file_requests.read"

		Intent(Intent.ACTION_VIEW).apply {
			data = Uri.parse(authorizeUrl)
			context.startActivity(this)
		}
	}

	suspend fun authenticate(code : String) : DbxToken? {
		var token : DbxToken? = null
		CoroutineScope(Dispatchers.IO).launch {
			val config = DbxRequestConfig("Graphite")
			val appInfo = DbxAppInfo("wqgzkie6sm7xxvw", "kgu8ymntbtwnsxc")

			val dbxAuthFinish = DbxWebAuth(config, appInfo).finishFromCode(code)

			token = DbxToken(dbxAuthFinish.accessToken, dbxAuthFinish.refreshToken)
		}.join()

		return token
	}

	fun getAccessToken(refreshToken : String) : DbxToken? {
		try {
			val config = DbxRequestConfig("Graphite")
			DbxCredential("", 0, refreshToken, "wqgzkie6sm7xxvw", "kgu8ymntbtwnsxc").refresh(config).let {
				return refreshToken?.let { it1 -> DbxToken(it.accessToken, it1) }
			}
		} catch (e : Exception) {
			return null
		}
	}

	fun testConnection(accessToken : String) : Boolean {
		try {
			val config = DbxRequestConfig("Graphite")
			val client = DbxClientV2(config, accessToken)
			return (client.check().user("test_connection").result == "test_connection")
		} catch (e : Exception) {
			return false
		}
	}

	fun getStorageData(accessToken : String) : SpaceUsage? {
		try {
			val config = DbxRequestConfig("Graphite")
			val client = DbxClientV2(config, accessToken)
			return client.users().spaceUsage
		} catch (e : Exception) {
			return null
		}
	}
}

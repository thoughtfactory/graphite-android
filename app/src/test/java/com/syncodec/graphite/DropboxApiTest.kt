package com.syncodec.graphite

import android.content.Context
import com.syncodec.graphite.di.sync.DropboxApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.Test

class DropboxApiTest {

	val authorizeUrl = "https://www.dropbox.com/oauth2/authorize?client_id=wqgzkie6sm7xxvw&response_type=code&scope=account_info.read&grant_type=authorization_code&token_access_type=offline"
//	val authorizePkceUrl = "https://www.dropbox.com/oauth2/authorize?client_id=wqgzkie6sm7xxvw&response_type=code&scope=account_info.read"

	@Test
	fun test_authenticate() {
		val dropboxApi = DropboxApi()
		val code = "Vs53hMx1TBMAAAAAAAAALlw-3V2H4Y4er2Sh03HUCWs"

		CoroutineScope(Dispatchers.Default).launch {
			dropboxApi.authenticate(code = code).let {
				println("result" + it)
//	    		println("access token: ${it.first.accessToken}")
//		    	println("refresh token: ${it.second}")
			}
		}
	}

	@Test
	fun test_getToken() {
		val dropboxApi = DropboxApi()
		val refreshToken = "7eezSK9tMrUAAAAAAAAAAT8zIiGFDKgmBjKnEBRjFC9jh3x1MHCj6wm6r7TBdZ27"
		CoroutineScope(Dispatchers.Default).launch {
			dropboxApi.getAccessToken(refreshToken).let {
				println("result" + it)
			}
		}
	}
}

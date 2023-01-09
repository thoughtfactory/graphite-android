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
//	https://www.dropbox.com/oauth2/authorize?client_id=wqgzkie6sm7xxvw&response_type=code&grant_type=authorization_code&token_access_type=offline&scope=account_info.read%20files.metadata.write%20files.metadata.read%20files.content.write%20files.content.read%20file_requests.write%20file_requests.read&redirect_uri=https%3A%2F%2F192.168.2.154%3A8001%2FdropboxCallback
// 	http://192.168.2.154:8001/graphite-diary/us-central1/dropboxCallback?code=t6Vy-VubtMAAAAAAAAACJWeCspWs2RRR_qMpcNeEdzo


	@Test
	fun test_authenticate() {
		val dropboxApi = DropboxApi()
//		val code = "Vs53hMx1TBMAAAAAAAAALlw-3V2H4Y4er2Sh03HUCWs"
		val code = "t6Vy-VubtMAAAAAAAAACLGisHxSIGdtqq69w4NqiLYk"

		CoroutineScope(Dispatchers.Default).launch {
			dropboxApi.authenticate(code = code) {
				println(it)
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

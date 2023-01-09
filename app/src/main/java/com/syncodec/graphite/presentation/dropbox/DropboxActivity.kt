package com.syncodec.graphite.presentation.dropbox

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dropbox.core.v2.users.SpaceUsage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.di.sync.DropboxApi
import com.syncodec.graphite.presentation.dropbox.composable.dialog.DropboxDialogType
import com.syncodec.graphite.presentation.dropbox.composable.screen.DropboxScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.alice.putSecretData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DropboxActivity : ComponentActivity(), DropboxApi {

	var dropboxSpaceUsage = mutableStateOf<SpaceUsage?>(null)

	//	http://192.168.2.154:8001/graphite-diary/us-central1/dropboxCallback?code=t6Vy-VubtMAAAAAAAAACJWeCspWs2RRR_qMpcNeEdzo
	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		val code = intent.data?.getQueryParameter("code")
		code?.let { saveAccessToken(it) } ?: connectWithDropbox { dropboxApiResponse ->
			dropboxSpaceUsage.value = dropboxApiResponse.result?.second
		}

		setContent {
			BaseContent {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)
				systemUiController.setNavigationBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)

				var showEnterOAuth2CodeDialog by remember { mutableStateOf(false) }

				fun openDialog(dialogType : DropboxDialogType) {
					when (dialogType) {
						DropboxDialogType.ENTER_OAUTH2_CODE_DIALOG -> showEnterOAuth2CodeDialog = true
					}
				}

				fun closeDialog(dialogType : DropboxDialogType) {
					when (dialogType) {
						DropboxDialogType.ENTER_OAUTH2_CODE_DIALOG -> showEnterOAuth2CodeDialog = false
					}
				}

				val _spaceUsage by dropboxSpaceUsage

				CompositionLocalProvider(
					LocalSpaceUsage provides _spaceUsage,
					LocalSignInWithDropbox provides ::dropboxSignIn,
					LocalEnterOAuth2Code provides {
						saveAccessToken(it)
						closeDialog(DropboxDialogType.ENTER_OAUTH2_CODE_DIALOG)
					},
					LocalTestConnection provides ::testConnection,
					LocalShowEnterOAuth2CodeDialog provides showEnterOAuth2CodeDialog,
					LocalDisconnect provides {
						CoroutineScope(Dispatchers.Default).launch {
							disconnectFromDropbox { dropboxApiResponse ->
								if (dropboxApiResponse.success) {
									CoroutineScope(Dispatchers.Main).launch {
										Toast.makeText(this@DropboxActivity, "Disconnected from Dropbox", Toast.LENGTH_SHORT).show()
									}
								}
							}
						}
					},
					LocalOpenDialog provides ::openDialog,
					LocalCloseDialog provides ::closeDialog,
				) {
					DropboxScreen(
						onClickBack = { finish() }
					)
				}
			}
		}
	}

	private fun saveAccessToken(accessToken : String) {
		CoroutineScope(Dispatchers.Default).launch {
			authenticateWithDropbox(accessToken) { dropboxApiResponse ->
				if (dropboxApiResponse.success) {
					dropboxApiResponse.result?.refreshToken?.encodeToByteArray()?.let { it1 -> putSecretData("dropbox_refresh_token", it1) }
					connectWithDropbox { dropboxApiResponse ->
						dropboxSpaceUsage.value = dropboxApiResponse.result?.second
					}
				} else {
					dropboxApiResponse.exception?.printStackTrace()
					CoroutineScope(Dispatchers.Main).launch {
						Toast.makeText(this@DropboxActivity, dropboxApiResponse.message ?: "Something went wrong. Please try again.", Toast.LENGTH_SHORT)
							.show()
					}
				}
			}
		}
	}

	private fun testConnection() {
		testDropboxConnectionConnection {dropboxApiResponse ->
			CoroutineScope(Dispatchers.Main).launch {
				Toast.makeText(this@DropboxActivity, dropboxApiResponse.message ?: "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show()
			}
		}
	}

	companion object {
		val LocalSpaceUsage = compositionLocalOf<SpaceUsage?> { null }

		val LocalSignInWithDropbox = compositionLocalOf<(Context) -> Unit> { {} }
		val LocalEnterOAuth2Code = compositionLocalOf<(String) -> Unit> { {} }
		val LocalTestConnection = compositionLocalOf { {} }
		val LocalDisconnect = compositionLocalOf { {} }

		val LocalShowEnterOAuth2CodeDialog = compositionLocalOf { false }

		val LocalOpenDialog = compositionLocalOf<(DropboxDialogType) -> Unit> { {} }
		val LocalCloseDialog = compositionLocalOf<(DropboxDialogType) -> Unit> { {} }
	}
}

package com.syncodec.graphite.presentation.dropbox

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FloatingActionButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.dropbox.core.v2.users.SpaceUsage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.di.sync.DbxToken
import com.syncodec.graphite.di.sync.DropboxApi
import com.syncodec.graphite.presentation.dropbox.composable.dialog.DropboxDialogType
import com.syncodec.graphite.presentation.dropbox.composable.screen.DropboxScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.alice.AliceRequestResult
import com.syncodec.graphite.utils.alice.getSecretData
import com.syncodec.graphite.utils.alice.putSecretData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DropboxActivity : ComponentActivity() {

	val dropboxApi by lazy { DropboxApi() }

	var dropboxToken = mutableStateOf<DbxToken?>(null)
	var dropboxSpaceUsage = mutableStateOf<SpaceUsage?>(null)

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		getAccessToken()

		setContent {
			BaseContent {
				val scope = rememberCoroutineScope()
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
					LocalSignInWithDropbox provides dropboxApi::signIn,
					LocalEnterOAuth2Code provides {
						CoroutineScope(Dispatchers.Default).launch {
							dropboxApi.authenticate(it).let {
								it?.refreshToken?.encodeToByteArray()?.let { it1 -> putSecretData("dropbox_refresh_token", it1) }
								getAccessToken()
							}
							withContext(Dispatchers.Main) {
								closeDialog(DropboxDialogType.ENTER_OAUTH2_CODE_DIALOG)
							}
						}
					},
					LocalTestConnection provides ::testConnection,
					LocalShowEnterOAuth2CodeDialog provides showEnterOAuth2CodeDialog,
					LocalOpenDialog provides ::openDialog,
					LocalCloseDialog provides ::closeDialog,
				) {
					DropboxScreen(
						onClickBack = {
							finish()
						}
					)
				}
			}
		}
	}

	private fun getAccessToken() {
		CoroutineScope(Dispatchers.IO).launch {
			getSecretData("dropbox_refresh_token").let {
				if (it.result == AliceRequestResult.KEY_NOT_FOUND) null else it.data?.decodeToString()
			}?.let {
				dropboxApi.getAccessToken(refreshToken = it)?.let {
					dropboxToken.value = it
					getSpaceUsage(true)
				}
			}
		}
	}

	private fun testConnection() {
		CoroutineScope(Dispatchers.IO).launch {
			dropboxToken.value?.accessToken?.let {
				dropboxApi.testConnection(it).let {
					CoroutineScope(Dispatchers.Main).launch {
						if (it) Toast.makeText(this@DropboxActivity, "Connection successful", Toast.LENGTH_SHORT).show()
						else Toast.makeText(this@DropboxActivity, "Connection failed", Toast.LENGTH_SHORT).show()
					}
				}
			} ?: run {
				CoroutineScope(Dispatchers.Main).launch {
					Toast.makeText(this@DropboxActivity, "Please sign in with Dropbox", Toast.LENGTH_SHORT).show()
				}
			}
		}
	}

	private fun getSpaceUsage(silent : Boolean) {
		CoroutineScope(Dispatchers.IO).launch {
			dropboxToken.value?.accessToken?.let {
				dropboxApi.getStorageData(it).let {
					dropboxSpaceUsage.value = it
				}
			} ?: run {
				if (! silent) {
					CoroutineScope(Dispatchers.Main).launch {
						Toast.makeText(this@DropboxActivity, "Please sign in with Dropbox", Toast.LENGTH_SHORT).show()
					}
				}
			}
		}
	}

	companion object {
		val LocalSpaceUsage = compositionLocalOf<SpaceUsage?> { null }

		val LocalSignInWithDropbox = compositionLocalOf<(Context) -> Unit> { {} }
		val LocalEnterOAuth2Code = compositionLocalOf<(String) -> Unit> { {} }
		val LocalTestConnection = compositionLocalOf { {} }

		val LocalShowEnterOAuth2CodeDialog = compositionLocalOf { false }

		val LocalOpenDialog = compositionLocalOf<(DropboxDialogType) -> Unit> { {} }
		val LocalCloseDialog = compositionLocalOf<(DropboxDialogType) -> Unit> { {} }
	}
}

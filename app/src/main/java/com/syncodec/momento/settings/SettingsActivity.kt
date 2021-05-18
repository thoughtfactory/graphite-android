package com.syncodec.momento.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.momento.settings.miscellaneous.TopBar
import com.syncodec.momento.settings.screen.*
import com.syncodec.momento.ui.theme.MomentoTheme
import com.syncodec.momento.vaultComponent.EvokeReason
import com.syncodec.momento.vaultComponent.VaultScreen

class SettingsActivity : ComponentActivity() {

	private lateinit var activityState: ActivityState
	private var showVaultScreen: MutableState<Boolean> = mutableStateOf(false)

	@OptIn(ExperimentalMaterialApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			activityState = rememberActivityState()

			MomentoTheme {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)
				systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.background)
				Screen()
			}
		}
	}

	override fun onBackPressed() {
		if (showVaultScreen.value) {
			showVaultScreen.value = false
		} else {
			if (activityState.currentPath.last() == "/") super.onBackPressed() else activityState.currentPath.removeLast()
		}
	}

	private fun onClick(click: Click) {
		when (click) {
			Click.LOGIN -> activityState.currentPath.add("login")
			Click.PREFERENCE -> activityState.currentPath.add("preference")
			Click.THEME -> activityState.currentPath.add("theme")
			Click.FONT_FAMILY -> activityState.currentPath.add("font_family")
			Click.SECURITY -> activityState.currentPath.add("security")
			Click.ADD_PASSCODE -> {
				activityState.evokeReason.value = EvokeReason.NEW_PASSCODE
				showVaultScreen.value = true
			}
			Click.CHANGE_PASSCODE -> {
				activityState.evokeReason.value = EvokeReason.CHANGE_PASSCODE
				showVaultScreen.value = true
			}
			Click.REMOVE_PASSCODE -> {
				activityState.evokeReason.value = EvokeReason.REMOVE_PASSCODE
				showVaultScreen.value = true
			}
			Click.BIOMETRIC_UNLOCK -> {
				activityState.evokeReason.value = EvokeReason.REMOVE_PASSCODE
				showVaultScreen.value = true
			}
			Click.PRIVACY_POLICY -> activityState.currentPath.add("privacy_policy")
			Click.TERMS_OF_SERVICE -> activityState.currentPath.add("terms_of_service")
			Click.ABOUT_US -> activityState.currentPath.add("about_us")
		}
	}

	@OptIn(ExperimentalMaterial3Api::class, androidx.compose.animation.ExperimentalAnimationApi::class)
	@Composable
	private fun Screen() {
		var showVaultScreen by showVaultScreen
		val evokeReason by activityState.evokeReason

		AnimatedContent(targetState = showVaultScreen) {
			if (it) {
				VaultScreen(
					evokeReason = evokeReason,
					onSuccess = { showVaultScreen = false }
				) {

				}
			} else {
				Scaffold(
					topBar = { TopBar(currentPath = activityState.currentPath) }
				) {
					AnimatedContent(targetState = activityState.currentPath.last()) {
						when (it) {
							"/" -> BaseScreen { onClick(it) }
							"login" -> {}
							"security" -> SecurityScreen { onClick(it) }
							"preference" -> PreferenceScreen { onClick(it) }
							"theme" -> ThemeScreen()
							"font_family" -> FontFamilyScreen()
						}
					}
				}
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	inner class ActivityState @OptIn(ExperimentalPermissionsApi::class) constructor(
		val currentPath: SnapshotStateList<String> = mutableStateListOf("/"),
		val evokeReason: MutableState<EvokeReason> = mutableStateOf(EvokeReason.UNLOCK_VAULT)
	)

	@OptIn(ExperimentalMaterialApi::class, ExperimentalPermissionsApi::class)
	@Composable
	fun rememberActivityState() = remember { ActivityState() }

	enum class Click {
		LOGIN,
		PREFERENCE,
		THEME,
		FONT_FAMILY,
		SECURITY,
		ADD_PASSCODE,
		CHANGE_PASSCODE,
		REMOVE_PASSCODE,
		BIOMETRIC_UNLOCK,
		PRIVACY_POLICY,
		TERMS_OF_SERVICE,
		ABOUT_US
	}
}

package com.syncodec.graphite.presentation.settings.composable.screen

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.firebase.auth.FirebaseUser
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.main.composable.buildingBlock.DropdownMenuItem
import com.syncodec.graphite.presentation.pro.ProActivity
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingButtonWithDropdown
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingSwitch
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsContentTitle
import com.syncodec.graphite.presentation.settings.composable.dialog.SettingsDialogType
import com.syncodec.graphite.presentation.ui.LocalIsPro
import com.syncodec.graphite.utils.AuthenticatorScreen
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.LocalAuthenticatorAction


@OptIn(ExperimentalAnimationApi::class)
@Preview
@Composable
fun SettingsScreen(
	firebaseUser : FirebaseUser? = null,
	onClickSignIn : () -> Unit = {},
	onClickSignOut : () -> Unit = {},
	onClickDeleteAccount : () -> Unit = {},
	navigateTo : (SettingsActivity.Companion.SettingsScreen) -> Unit = {},
	openDialog : (SettingsDialogType) -> Unit = {},
) {
	val context = LocalContext.current
	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val isPro = LocalIsPro.current

	val uriHandler = LocalUriHandler.current
	val authenticatorAction = LocalAuthenticatorAction.current

	val darkTheme by dataStoreInstance.getDarkTheme.collectAsState(null)
	val isTintFavourite by dataStoreInstance.getTintFavorite.collectAsState(initial = null)
	val useBiometric = dataStoreInstance.getUseBiometric().collectAsState(initial = null).value
	val isGeolocationEnabled by dataStoreInstance.getGeolocation.collectAsState(initial = null)
	val isYearProgressEnabled by dataStoreInstance.getYearProgress.collectAsState(initial = null)
	val isNoteNotificationEnabled by dataStoreInstance.getNoteFromNotification.collectAsState(initial = null)

	var isDarkThemeDropdownMenuVisible by remember { mutableStateOf(false) }


	Column(
		modifier = Modifier
			.fillMaxSize()
			.verticalScroll(rememberScrollState())
	) {

		AnimatedVisibility(
			visible = firebaseUser != null,
			enter = expandVertically(tween(300)),
			exit = shrinkVertically(tween(300))
		) {
			ProfileCard(
				displayName = firebaseUser?.displayName ?: "Anonymous",
				email = firebaseUser?.email ?: "Sign in to sync your settings",
				photoUrl = firebaseUser?.photoUrl,
				isPro = isPro,
			)
		}

		SettingsContentTitle(title = "ACCOUNT")
		AnimatedContent(
			targetState = firebaseUser,
			transitionSpec = { expandVertically(tween(300)) with shrinkVertically(tween(300)) }
		) {
			if (it == null) {
				SettingButton(text = "Sign in", icon = R.drawable.ic_account, onClick = onClickSignIn)
			} else {
				Column(modifier = Modifier) {
					SettingButton(text = "Log out", icon = R.drawable.ic_logout, onClick = onClickSignOut)
					SettingButton(text = "Delete account", icon = R.drawable.ic_account_delete, onClick = onClickDeleteAccount)
				}
			}
		}
		AnimatedVisibility(
			visible = ! isPro,
			enter = expandVertically(tween(300)),
			exit = shrinkVertically(tween(300))
		) {
			SettingButton(text = "Subscription", icon = R.drawable.ic_subscription) {
				context.startActivity(Intent(context, ProActivity::class.java))
			}
		}

		SettingsContentTitle(title = "PREFERENCES")
		SettingSwitch(
			text = "Tint favourite note",
			icon = R.drawable.ic_favourite,
			isChecked = isTintFavourite != false
		) { dataStoreInstance.putTintFavorite(it) }
		SettingButton(text = "Font style", icon = R.drawable.ic_font_family, subText = "Ubuntu")
		SettingButtonWithDropdown(
			text = "Dark mode",
			icon = R.drawable.ic_bulb,
			subText = when (darkTheme) {
				SettingsActivity.Companion.DarkTheme.SyncWithSystem -> "Sync with system"
				SettingsActivity.Companion.DarkTheme.AlwaysOn -> "Always on"
				SettingsActivity.Companion.DarkTheme.AlwaysOff -> "Always off"
				null -> "Sync with system"
			},
			isDropdownMenuVisible = isDarkThemeDropdownMenuVisible,
			dropdownMenuList = listOf(
				DropdownMenuItem(
					title = "Sync with system",
					icon = R.drawable.ic_r2d2
				) { dataStoreInstance.putDarkTheme(SettingsActivity.Companion.DarkTheme.SyncWithSystem); isDarkThemeDropdownMenuVisible = false },
				DropdownMenuItem(
					title = "Always on",
					icon = R.drawable.ic_switch_on
				) { dataStoreInstance.putDarkTheme(SettingsActivity.Companion.DarkTheme.AlwaysOn); isDarkThemeDropdownMenuVisible = false },
				DropdownMenuItem(
					title = "Always off",
					icon = R.drawable.ic_switch_off
				) { dataStoreInstance.putDarkTheme(SettingsActivity.Companion.DarkTheme.AlwaysOff); isDarkThemeDropdownMenuVisible = false },
			)
		) { isDarkThemeDropdownMenuVisible = it }
		SettingButton(text = "Language", icon = R.drawable.ic_language, subText = "English")

		SettingsContentTitle(title = "SECURITY")
		SettingButton(text = "Add Passcode", icon = R.drawable.ic_passcode) { authenticatorAction(AuthenticatorScreen.AddPasscode) }
		SettingButton(text = "Change Passcode", icon = R.drawable.ic_passcode_change) { authenticatorAction(AuthenticatorScreen.ChangePasscode) }
		SettingSwitch(text = "Biometric Authentication", icon = R.drawable.ic_biometric, isChecked = useBiometric != false) {
			dataStoreInstance.putUseBiometric(it)
		}

		SettingsContentTitle(title = "DATA")
		SettingButton(text = "Backup and restore", icon = R.drawable.ic_local_backup) { navigateTo(SettingsActivity.Companion.SettingsScreen.BackupAndRestore) }
		SettingButton(text = "Synchronization", icon = R.drawable.ic_sync)
		SettingButton(text = "Import", icon = R.drawable.ic_import) { navigateTo(SettingsActivity.Companion.SettingsScreen.ImportData) }
		SettingButton(text = "Export", icon = R.drawable.ic_export) { openDialog(SettingsDialogType.ExportData) }
		SettingButton(text = "Clear data", icon = R.drawable.ic_broom) { openDialog(SettingsDialogType.ClearData) }

		SettingsContentTitle(title = "EXTENSIONS")
		SettingSwitch(text = "Year progress bar", icon = R.drawable.ic_advance, isChecked = isYearProgressEnabled != false) {
			dataStoreInstance.putYearProgress(it)
		}
		SettingSwitch(text = "Geotag notes automatically", icon = R.drawable.ic_map_marker, isChecked = isGeolocationEnabled != false) {
			dataStoreInstance.putGeolocation(it)
		}
		SettingSwitch(text = "Add note form notification", icon = R.drawable.ic_note_notification, isChecked = isNoteNotificationEnabled != false) {
			if (isPro) openDialog(SettingsDialogType.NotificationPermission)
			else Toast.makeText(context, "Join Graphite Pro to enable adding notes from notification", Toast.LENGTH_SHORT).show()
		}

		SettingsContentTitle(title = "ABOUT US")
		SettingButton(text = "Rate us", icon = R.drawable.ic_star) {
			Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}"))
		}
		SettingButton(text = "Spread a word", icon = R.drawable.ic_share) {
			try {
				val shareIntent = Intent(Intent.ACTION_SEND)
				shareIntent.type = "text/plain"
				shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Graphite")
				var shareMessage = "\nHey... Check out Graphite, an everyday diary and bucket list\n\n"
				shareMessage = """${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}""".trimIndent()
				shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
				context.startActivity(Intent.createChooser(shareIntent, "choose one"))
			} catch (e : Exception) {
				Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
			}
		}
		SettingButton(text = "Join us on Instagram", icon = R.drawable.ic_logo_instagram, tint = Color.Unspecified) {
			val uri = Uri.parse("https://www.instagram.com/graphite.diary/?hl=en")
			val likeIng = Intent(Intent.ACTION_VIEW, uri)

			likeIng.setPackage("com.instagram.android")

			try {
				context.startActivity(likeIng)
			} catch (e : ActivityNotFoundException) {
				context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/graphite.diary/?hl=en")))
			} catch (e : Exception) {
				Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
			}
		}
		SettingButton(text = "Privacy policy", icon = R.drawable.ic_policy) {
			uriHandler.openUri("https://graphite.syncodec.com/policy.html")
		}
		SettingButton(text = "Terms of service", icon = R.drawable.ic_terms) {
			uriHandler.openUri("https://graphite.syncodec.com/terms.html")
		}
		SettingButton(text = "Open source licenses", icon = R.drawable.ic_code) {
			context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
		}

		Spacer(modifier = Modifier.height(64.dp))
		MadeWithLove()

		Spacer(modifier = Modifier.height(64.dp))
	}
}

@Preview
@Composable
private fun ProfileCard(
	displayName : String? = null,
	email : String? = null,
	photoUrl : Uri? = null,
	isPro : Boolean = false,
) {

	val context = LocalContext.current
	var isError by remember { mutableStateOf(false) }

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier.fillMaxWidth(),
	) {
		Spacer(modifier = Modifier.height(32.dp))
		AsyncImage(
			model = ImageRequest.Builder(context)
				.data(photoUrl)
				.crossfade(300)
				.error(R.drawable.ic_user_male)
				.build(),
			placeholder = null,
			onError = { isError = true },
			contentDescription = email,
			contentScale = ContentScale.Crop,
			modifier = Modifier
				.requiredSize(96.dp)
				.clip(CircleShape),
		)
		Spacer(modifier = Modifier.height(8.dp))
		Text(
			text = displayName ?: "Anonymous",
			style = MaterialTheme.typography.titleLarge,
			color = MaterialTheme.colorScheme.onBackground,
			fontWeight = FontWeight.Bold,
		)
		Text(
			text = email ?: "Email not available",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
		)
		Spacer(modifier = Modifier.height(32.dp))
	}
}

@Preview
@Composable
private fun MadeWithLove() {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.Center,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = "made with",
			style = MaterialTheme.typography.bodyLarge,
			fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
			fontWeight = FontWeight.Bold,
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f)
		)

		Spacer(modifier = Modifier.width(6.dp))

		Icon(
			painter = painterResource(id = R.drawable.ic_heart_c),
			contentDescription = "Love",
			tint = Color.Unspecified,
			modifier = Modifier.requiredSize(24.dp)
		)

		Spacer(modifier = Modifier.width(6.dp))

		Text(
			text = "on",
			style = MaterialTheme.typography.bodyLarge,
			fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
			fontWeight = FontWeight.Bold,
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f)
		)

		Spacer(modifier = Modifier.width(10.dp))

		Icon(
			painter = painterResource(id = R.drawable.ic_earth_c),
			contentDescription = "Earth",
			tint = Color.Unspecified,
			modifier = Modifier.requiredSize(20.dp)
		)
	}
}

package com.syncodec.graphite.settingsComponent.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.settingsComponent.SettingsActivity
import com.syncodec.graphite.settingsComponent.miscellaneous.SettingButton
import com.syncodec.graphite.R
import com.syncodec.graphite.ui.theme.PremiumCompositionLocal


@Composable
fun BaseScreen(
	email: String?,
	onAction: (SettingsActivity.Action, Any?) -> Unit
) {
	val isPremium = PremiumCompositionLocal.current

	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		item {
			LoginButton(
				email = email,
				icon = R.drawable.ic_login,
				tint = Color(0xFF5463FF),
				onClick = { onAction(SettingsActivity.Action.LOGIN, null) },
				onLogout = { onAction(SettingsActivity.Action.LOGOUT, null) }
			)
		}
		if (!isPremium) {
			item {
				SettingButton(
					title = "Subscription",
					icon = R.drawable.ic_subscription,
					tint = Color(0xFF66BFBF)
				) { onAction(SettingsActivity.Action.SUBSCRIPTION, null) }
			}
		}
//		item {
//			SettingButton(
//				title = "Preference",
//				icon = R.drawable.ic_preference,
//				tint = Color(0xFFFE7E6D)
//			) {
//				onAction(
//					SettingsActivity.Action.NAVIGATION,
//					SettingsActivity.Companion.Path.PREFERENCE
//				)
//			}
//		}
		item {
			SettingButton(
				title = "Security",
				icon = R.drawable.ic_security,
				tint = Color(0xFF28527A)
			) {
				onAction(
					SettingsActivity.Action.NAVIGATION,
					SettingsActivity.Companion.Path.SECURITY
				)
			}
		}
		item {
			SettingButton(
				title = "Data",
				icon = R.drawable.ic_data,
				tint = Color(0xFF5EAAA8)
			) { onAction(SettingsActivity.Action.NAVIGATION, SettingsActivity.Companion.Path.DATA) }
		}
		item {
			SettingButton(
				title = "Backup and Sync",
				icon = R.drawable.ic_sync,
				tint = Color(0xFFCC9B6D)
			) { onAction(SettingsActivity.Action.NAVIGATION, SettingsActivity.Companion.Path.SYNC) }
		}
		item {
			SettingButton(
				title = "Privacy policy",
				icon = R.drawable.ic_privacy,
				tint = Color(0xFF886F6F)
			) { onAction(SettingsActivity.Action.POLICY, null) }
		}
		item {
			SettingButton(
				title = "Terms of service",
				icon = R.drawable.ic_terms,
				tint = Color(0xFF316B83)
			) { onAction(SettingsActivity.Action.TERMS, null) }
		}
//		item {
//			OssLicensesMenuActivity.setActivityTitle("Open Source Licenses")
//			Intent(this, OssLicensesMenuActivity::class.java).apply {
//				startActivity(this)
//			}
//		}
		item {
			SettingButton(
				title = "Graphite",
				icon = R.drawable.ic_icon,
				tint = Color.Unspecified
			) {
				onAction(
					SettingsActivity.Action.NAVIGATION,
					SettingsActivity.Companion.Path.GRAPHITE
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun LoginButton(
	email: String?,
	icon: Int? = null,
	tint: Color? = null,
	enabled: Boolean = true,
	onClick: () -> Unit,
	onLogout: () -> Unit
) {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.height(64.dp),
		elevation = 0.dp,
		backgroundColor = Color.Transparent,
		enabled = email == null,
		onClick = { onClick() }
	) {
		Row(
			modifier = Modifier
				.fillMaxSize()
				.padding(24.dp, 0.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			if (icon != null) {
				Icon(
					painter = painterResource(id = icon),
					contentDescription = email,
					tint = tint ?: Color.Unspecified,
					modifier = Modifier.requiredSize(24.dp)
				)
				Spacer(modifier = Modifier.width(24.dp))
			} else {
				Spacer(modifier = Modifier.width(48.dp))
			}
			Text(
				text = email ?: "Login",
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Bold,
				color = if (enabled) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(
					0.47f
				),
				modifier = Modifier
			)

			Spacer(modifier = Modifier.weight(1f))

			if (email != null) {
				OutlinedButton(
					onClick = { onLogout() },
					modifier = Modifier,
					colors = ButtonDefaults.outlinedButtonColors(
						containerColor = MaterialTheme.colorScheme.background,
						contentColor = MaterialTheme.colorScheme.onBackground
					),
					border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
				) {
					Text(
						text = "Logout",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onBackground
					)
				}
			}
		}
	}
}

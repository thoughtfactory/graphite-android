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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.settingsComponent.SettingsActivity
import com.syncodec.graphite.settingsComponent.miscellaneous.SettingButton
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
				onClick = { onAction(SettingsActivity.Action.LOGIN, null) },
				onLogout = { onAction(SettingsActivity.Action.LOGOUT, null) }
			)
		}
		if (!isPremium) {
			item {
				SettingButton(
					title = "Subscription",
					leadingIcon = R.drawable.ic_subscription,
				) { onAction(SettingsActivity.Action.SUBSCRIPTION, null) }
			}
		}
		item {
			SettingButton(
				title = "Preference",
				leadingIcon = R.drawable.ic_preference,
			) {
				onAction(
					SettingsActivity.Action.NAVIGATION,
					SettingsActivity.Companion.Path.PREFERENCE
				)
			}
		}
		item {
			SettingButton(
				title = "Security",
				leadingIcon = R.drawable.ic_lock_close,
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
				leadingIcon = R.drawable.ic_data,
			) { onAction(SettingsActivity.Action.NAVIGATION, SettingsActivity.Companion.Path.DATA) }
		}
		item {
			SettingButton(
				title = "Backup and Sync",
				leadingIcon = R.drawable.ic_sync,
			) { onAction(SettingsActivity.Action.NAVIGATION, SettingsActivity.Companion.Path.SYNC) }
		}
		item {
			SettingButton(
				title = "Privacy Policy",
				leadingIcon = R.drawable.ic_policy,
			) { onAction(SettingsActivity.Action.POLICY, null) }
		}
		item {
			SettingButton(
				title = "Terms of Service",
				leadingIcon = R.drawable.ic_terms,
			) { onAction(SettingsActivity.Action.TERMS, null) }
		}
		item {
			SettingButton(
				title = "About Us",
				leadingIcon = R.drawable.ic_about_us,
			) {
				onAction(
					SettingsActivity.Action.NAVIGATION,
					SettingsActivity.Companion.Path.ABOUT_US
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun LoginButton(
	email: String?,
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
				.padding(16.dp, 0.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_account),
				contentDescription = email,
				tint = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier.requiredSize(28.dp)
			)
			Spacer(modifier = Modifier.width(24.dp))
			Text(
				text = email ?: "Login",
				style = MaterialTheme.typography.titleMedium,
				color = if (enabled) MaterialTheme.colorScheme.onBackground
				else MaterialTheme.colorScheme.onBackground.copy(0.47f),
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.weight(1f)
			)


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

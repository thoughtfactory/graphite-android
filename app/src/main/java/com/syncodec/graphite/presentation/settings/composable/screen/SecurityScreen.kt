package com.syncodec.graphite.presentation.settings.composable.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScaffold
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButtonDefaults


@Preview
@Composable
fun SecurityScreen(
	onClickBack : () -> Unit = {},
) {
	GenericSettingsScaffold(
		title = "Security",
		onClickBack = onClickBack,
	) {
		LazyColumn(
			modifier = Modifier.fillMaxSize()
		) {
			item {
				SettingsButton(
					title = "Add/Change passcode",
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_flat_password),
					onClick = { },
				)
			}
			item {
				SettingsButton(
					title = "Remove passcode",
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_flat_password),
					onClick = { },
				)
			}
			item {
				SettingsButton(
					title = "Biometric authentication",
					subTitle = "Ask for fingerprint authentication when opening the app",
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_flat_fingerprint),
					onClick = { },
				)
			}
			item {
				SettingsButton(
					title = "Vault timeout",
					subTitle = "Automatically lock the vault after a period of inactivity",
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_chronometer),
					onClick = { },
				)
			}
			item {
				SettingsButton(
					title = "Screenshot protection",
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_flat_circle_dashed),
					onClick = { },
				)
			}
		}
	}
}

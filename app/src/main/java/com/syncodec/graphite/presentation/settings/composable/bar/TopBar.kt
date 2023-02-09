package com.syncodec.graphite.presentation.settings.composable.bar

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.settings.SettingsActivity


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun TopBar(
	settingsScreen : SettingsActivity.Companion.SettingsScreen = SettingsActivity.Companion.SettingsScreen.Settings,
	onClickBack : () -> Unit,
) {
	TopAppBar(
		modifier = Modifier.fillMaxWidth(),
		navigationIcon = {
			MenuButton(
				icon = R.drawable.ic_back,
				onClick = onClickBack
			)
		},
		title = {
			AnimatedText(
				text = when (settingsScreen) {
					SettingsActivity.Companion.SettingsScreen.Settings -> "Settings"
					SettingsActivity.Companion.SettingsScreen.BackupAndRestore -> "Backup & Restore"
					SettingsActivity.Companion.SettingsScreen.LocalBackup -> "Local Backup"
					SettingsActivity.Companion.SettingsScreen.ImportData -> "Import Data"
				},
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.Bold,
			)
		},
		colors = TopAppBarDefaults.topAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
			titleContentColor = MaterialTheme.colorScheme.onBackground,
		)
	)
}

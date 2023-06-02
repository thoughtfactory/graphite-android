package com.syncodec.graphite.presentation.settings.composable.bottomSheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButtonDefaults
import com.syncodec.graphite.presentation.settings.composable.dialog.DeleteAccountDialog


@Preview
@Composable
fun AccountBottomSheet(
	isSignedIn: Boolean = false,
	onClickSignIn: () -> Unit = { },
	onClickSignOut: () -> Unit = { },
	onClickDeleteAccount: () -> Unit = { },
) {
	var showDeleteAccountDialog by remember { mutableStateOf(false) }

	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		Text(
			text = "Account",
			style = MaterialTheme.typography.headlineSmall,
			fontWeight = FontWeight.Bold,
			modifier = Modifier.padding(horizontal = 24.dp)
		)
		Spacer(modifier = Modifier.height(24.dp))
		if (isSignedIn) {
			SettingsButton(
				title = "Sign out",
				leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_flat_sign_out),
				trailingIcon = null,
				onClick = onClickSignOut,
			)
			SettingsButton(
				title = "Delete account",
				leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_flat_trash, color = MaterialTheme.colorScheme.error),
				trailingIcon = null,
				colors = SettingsButtonDefaults.warningSettingsButtonColors(),
				onClick = { showDeleteAccountDialog = true },
			)
		}
		else {
			SignInWithGoogleButton(onClick = onClickSignIn)
		}
		Spacer(modifier = Modifier.height(24.dp))
	}

	DeleteAccountDialog(
		showDialog = showDeleteAccountDialog,
		onConfirmDelete = {
			onClickDeleteAccount()
			showDeleteAccountDialog = false
		},
		onDismiss = { showDeleteAccountDialog = false }
	)
}

@Preview
@Composable
private fun SignInWithGoogleButton(
	onClick: () -> Unit = { },
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onClick() }
			.padding(horizontal = 24.dp, vertical = 20.dp)
	) {
		Icon(
			painter = painterResource(id = R.drawable.ic_logo_google),
			contentDescription = "Google",
			tint = Color.Companion.Unspecified,
			modifier = Modifier.requiredSize(32.dp)
		)
		Spacer(modifier = Modifier.width(24.dp))

		Text(
			text = "Sign in with Google",
			style = MaterialTheme.typography.titleSmall,
			fontWeight = FontWeight.Bold,
		)
	}
}

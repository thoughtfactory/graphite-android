package com.syncodec.graphite.presentation.common.dialog.whereDialog.composable.bar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.utils.AuthenticatorScreen
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalIsAuthenticated


@Preview
@Composable
fun BottomBar(
	onClickSelect : () -> Unit = {},
	onClickEverywhere : () -> Unit = {},
) {
	val isAuthenticated = LocalIsAuthenticated.current
	val authenticatorAction = LocalAuthenticatorAction.current

	BottomAppBar(
		modifier = Modifier.fillMaxWidth(),
		tonalElevation = 8.dp,
	) {

		Spacer(modifier = Modifier.width(12.dp))

		Box(
			contentAlignment = Alignment.CenterStart,
			modifier = Modifier
				.weight(1f)
				.background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.medium)
				.clip(MaterialTheme.shapes.medium)
				.clickable(onClick = onClickSelect)
				.height((IconButtonSize * 2) - 2.dp),
		) {
			Text(
				text = "Select",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier.padding(start = 12.dp),
			)
		}

		Spacer(modifier = Modifier.width(8.dp))

		Box(
			contentAlignment = Alignment.CenterStart,
			modifier = Modifier
				.weight(1f)
				.background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.medium)
				.clip(MaterialTheme.shapes.medium)
				.clickable(onClick = onClickEverywhere)
				.height((IconButtonSize * 2) - 2.dp),
		) {
			Text(
				text = "Everywhere",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier.padding(start = 12.dp),
			)
		}

		Spacer(modifier = Modifier.width(12.dp))

		MenuButton(
			icon = R.drawable.ic_vault,
			tooltip = "Vault",
			checked = isAuthenticated,
			colors = MenuButtonDefaults.menuButtonColorsOnSurface(),
		) { authenticatorAction(AuthenticatorScreen.Authenticate) }

		Spacer(modifier = Modifier.width(12.dp))
	}
}

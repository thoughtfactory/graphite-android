package com.syncodec.graphite.presentation.notebook.screen.composable.bar

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.GenericButton
import com.syncodec.graphite.presentation.common.button.GenericButtonDefaults
import com.syncodec.graphite.utils.AuthenticatorScreen
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalIsAuthenticated


@Preview
@Composable
fun BottomBar(
	onClickMenu : () -> Unit = {},
) {
	val isAuthenticated = LocalIsAuthenticated.current
	val onAuthenticationAction = LocalAuthenticatorAction.current
	
	BottomAppBar(
		modifier = Modifier.fillMaxWidth(),
		tonalElevation = 8.dp,
	) {
		Spacer(modifier = Modifier.width(12.dp))


		Spacer(modifier = Modifier.weight(1f))

		GenericButton(
			icon = R.drawable.ic_vault,
			checked = isAuthenticated,
			colors = GenericButtonDefaults.genericButtonColorsOnSurface(),
		) { onAuthenticationAction(AuthenticatorScreen.Authenticate) }

		GenericButton(
			icon = R.drawable.ic_menu,
			colors = GenericButtonDefaults.genericButtonColorsOnSurface(),
			onClick = onClickMenu
		)

		Spacer(modifier = Modifier.width(12.dp))
	}
}

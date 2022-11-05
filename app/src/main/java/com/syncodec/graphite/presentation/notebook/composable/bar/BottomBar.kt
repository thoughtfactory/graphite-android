package com.syncodec.graphite.presentation.notebook.composable.bar

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.notebook.NotebookActivity
import com.syncodec.graphite.presentation.notebook.composable.bottomSheet.NotebookBottomSheetType
import com.syncodec.graphite.utils.Authenticator
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalVaultIsOpened
import com.syncodec.graphite.utils.tone


@Composable
fun BottomBar(
	modifier: Modifier = Modifier,
) {
	val containerColor = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)
	val contentColor = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1)

	val openSheet = NotebookActivity.LocalOpenBottomSheet.current

	val onAuthenticatorAction = LocalAuthenticatorAction.current
	val isVaultOpened = LocalVaultIsOpened.current

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = modifier
			.fillMaxWidth()
			.height(80.dp)
			.background(containerColor)
	) {
		Spacer(modifier = Modifier.width(16.dp))

		MenuButton(
			icon = R.drawable.ic_share,
			contentDescription = "Share Bucket",
			tint = contentColor
		) {}

		MenuButton(
			icon = R.drawable.ic_export,
			contentDescription = "Export Bucket",
			tint = contentColor
		) {}

		Spacer(modifier = Modifier.weight(1f))

		Spacer(modifier = Modifier.width(16.dp))

		MenuButton(
			icon = R.drawable.ic_vault,
			contentDescription = "Vault",
			tint = if (isVaultOpened) MaterialTheme.colorScheme.onBackground else contentColor,
			containerColor = if (isVaultOpened) MaterialTheme.colorScheme.background else Color.Companion.Transparent
		) { onAuthenticatorAction(Authenticator.AUTHENTICATE) }

		MenuButton(
			icon = R.drawable.ic_menu,
			contentDescription = "Menu",
			tint = contentColor
		) { openSheet(NotebookBottomSheetType.MENU) }

		Spacer(modifier = Modifier.width(16.dp))
	}
}

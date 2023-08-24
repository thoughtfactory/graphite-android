package com.syncodec.graphite.presentation.note2.composable.bar.viewer

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
fun ViewerBottomBar(
	attachmentCount: Int = 0,
	onClickMetadata: () -> Unit = {},
	onClickAttachment: () -> Unit = {},
	onClickLocation: () -> Unit = {},
	onClickExport : () -> Unit = {},
	onClickEdit: () -> Unit = {}
) {
	val isAuthenticated = LocalIsAuthenticated.current
	val onAuthenticationAction = LocalAuthenticatorAction.current

	BottomAppBar(
		modifier = Modifier.fillMaxWidth(),
		tonalElevation = 0.dp,
	) {
		Spacer(modifier = Modifier.width(12.dp))
		GenericButton(
			icon = R.drawable.ic_fa_info,
			tooltip = "Metadata",
			colors = GenericButtonDefaults.genericButtonColorsOnSurface(),
			onClick = onClickMetadata
		)

		GenericButton(
			icon = R.drawable.ic_fa_files,
			tooltip = "Attachments",
			badgeCount = attachmentCount,
			colors = GenericButtonDefaults.genericButtonColorsOnSurface(),
			onClick = onClickAttachment,
		)

		GenericButton(
			icon = R.drawable.ic_fa_map_marker_dot,
			tooltip = "Location",
			colors = GenericButtonDefaults.genericButtonColorsOnSurface(),
			onClick = onClickLocation,
		)

		Spacer(modifier = Modifier.weight(1f))

		GenericButton(
			icon = R.drawable.ic_fa_export,
			tooltip = "Export",
			colors = GenericButtonDefaults.genericButtonColorsOnSurface(),
			onClick = onClickExport
		)

		GenericButton(
			icon = R.drawable.ic_fa_vault,
			tooltip = "Vault",
			checked = isAuthenticated,
			colors = GenericButtonDefaults.genericButtonColorsOnSurface(),
		) { onAuthenticationAction(AuthenticatorScreen.Authenticate) }

		GenericButton(
			icon = R.drawable.ic_fa_pen,
			tooltip = "Edit Note",
			colors = GenericButtonDefaults.genericButtonColorsOnSurface(),
			onClick = onClickEdit
		)
		Spacer(modifier = Modifier.width(12.dp))
	}
}


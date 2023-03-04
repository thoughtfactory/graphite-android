package com.syncodec.graphite.presentation.note.screen.viewerScreen.bar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import com.syncodec.graphite.presentation.main.composable.buildingBlock.DropdownMenu
import com.syncodec.graphite.presentation.main.composable.buildingBlock.DropdownMenuItem
import com.syncodec.graphite.utils.AuthenticatorScreen
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalIsAuthenticated


@Preview
@Composable
fun BottomBar(
	onClickExportAsTxt : () -> Unit = {},
	onClickExportAsPdf : () -> Unit = {},
	onClickExportAsHtml : () -> Unit = {},
	onClickExportAsJson : () -> Unit = {},
	onClickExportAsMarkdown : () -> Unit = {},
	onClickExportAttachments : () -> Unit = {},
	onClickMetadata : () -> Unit = {},
	onClickEditNote : () -> Unit = {}
) {

	val isAuthenticated = LocalIsAuthenticated.current
	val onAuthenticationAction = LocalAuthenticatorAction.current

	var isExportDropdownVisible by remember { mutableStateOf(false) }

	BottomAppBar(
		modifier = Modifier.fillMaxWidth(),
		tonalElevation = 8.dp,
	) {
		Spacer(modifier = Modifier.width(12.dp))
		MenuButton(
			icon = R.drawable.ic_info,
			tooltip = "Info",
			colors = MenuButtonDefaults.menuButtonColorsOnSurface(),
			onClick = onClickMetadata
		)

		Box {
			MenuButton(
				icon = R.drawable.ic_export,
				tooltip = "Export Note",
				colors = MenuButtonDefaults.menuButtonColorsOnSurface(),
				onClick = { isExportDropdownVisible = true }
			)

			DropdownMenu(
				title = "Export note",
				itemList = listOf(
					DropdownMenuItem(title = "As txt", icon = R.drawable.ic_file_txt, onClick = onClickExportAsTxt),
					DropdownMenuItem(title = "As pdf", icon = R.drawable.ic_file_pdf, onClick = onClickExportAsPdf),
					DropdownMenuItem(title = "As html", icon = R.drawable.ic_file_html, onClick = onClickExportAsHtml, isPro = true),
					DropdownMenuItem(title = "As json", icon = R.drawable.ic_file_json, onClick = onClickExportAsJson, isPro = true),
					DropdownMenuItem(title = "As markdown", icon = R.drawable.ic_file_md, onClick = onClickExportAsMarkdown, isPro = true),
					DropdownMenuItem(title = "Attachments", icon = R.drawable.ic_gallery, onClick = onClickExportAttachments),
				),
				isVisible = isExportDropdownVisible,
			) { isExportDropdownVisible = false }
		}

		Spacer(modifier = Modifier.weight(1f))

		MenuButton(
			icon = R.drawable.ic_vault,
			tooltip = "Vault",
			checked = isAuthenticated,
			colors = MenuButtonDefaults.menuButtonColorsOnSurface(),
		) { onAuthenticationAction(AuthenticatorScreen.Authenticate) }

		MenuButton(
			icon = R.drawable.ic_pencil,
			tooltip = "Edit Note",
			colors = MenuButtonDefaults.menuButtonColorsOnSurface(),
			onClick = onClickEditNote
		)
		Spacer(modifier = Modifier.width(12.dp))
	}
}

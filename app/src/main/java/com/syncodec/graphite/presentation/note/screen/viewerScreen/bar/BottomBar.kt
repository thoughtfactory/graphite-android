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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.GenericButton
import com.syncodec.graphite.presentation.common.button.GenericButtonDefaults
import com.syncodec.graphite.presentation.main.composable.buildingBlock.DropdownMenu
import com.syncodec.graphite.presentation.main.composable.buildingBlock.DropdownMenuItem
import com.syncodec.graphite.presentation.base.secureComposable.LocalIsRepoUnlocked


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

	val isAuthenticated = LocalIsRepoUnlocked.current
//	val onAuthenticationAction = LocalAuthenticatorAction.current

	var isExportDropdownVisible by remember { mutableStateOf(false) }

	BottomAppBar(
		modifier = Modifier.fillMaxWidth(),
		tonalElevation = 8.dp,
	) {
		Spacer(modifier = Modifier.width(12.dp))
		GenericButton(
			icon = R.drawable.ic_info,
			tooltip = "Info",
			colors = GenericButtonDefaults.genericButtonColorsOnSurface(),
			onClick = onClickMetadata
		)

		Box {
			GenericButton(
				icon = R.drawable.ic_export,
				tooltip = stringResource(id = R.string.export_note),
				colors = GenericButtonDefaults.genericButtonColorsOnSurface(),
				onClick = { isExportDropdownVisible = true }
			)

			DropdownMenu(
				title = stringResource(id = R.string.export_note),
				itemList = listOf(
					DropdownMenuItem(title = stringResource(id = R.string.as_txt), icon = R.drawable.ic_file_txt, onClick = onClickExportAsTxt),
					DropdownMenuItem(title = stringResource(id = R.string.as_pdf), icon = R.drawable.ic_file_pdf, onClick = onClickExportAsPdf),
					DropdownMenuItem(title = stringResource(id = R.string.as_html), icon = R.drawable.ic_file_html, onClick = onClickExportAsHtml, isPro = true),
					DropdownMenuItem(title = stringResource(id = R.string.as_json), icon = R.drawable.ic_file_json, onClick = onClickExportAsJson, isPro = true),
					DropdownMenuItem(title = stringResource(id = R.string.as_markdown), icon = R.drawable.ic_file_md, onClick = onClickExportAsMarkdown, isPro = true),
					DropdownMenuItem(title = stringResource(id = R.string.attachments), icon = R.drawable.ic_gallery, onClick = onClickExportAttachments),
				),
				isVisible = isExportDropdownVisible,
			) { isExportDropdownVisible = false }
		}

		Spacer(modifier = Modifier.weight(1f))

//		GenericButton(
//			icon = R.drawable.ic_vault,
//			tooltip = "Vault",
//			checked = isAuthenticated,
//			colors = GenericButtonDefaults.genericButtonColorsOnSurface(),
//		) { onAuthenticationAction(AuthenticationState.Authenticate) }

		GenericButton(
			icon = R.drawable.ic_pencil,
			tooltip = "Edit Note",
			colors = GenericButtonDefaults.genericButtonColorsOnSurface(),
			onClick = onClickEditNote
		)
		Spacer(modifier = Modifier.width(12.dp))
	}
}

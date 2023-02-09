package com.syncodec.graphite.presentation.settings.composable.screen.importDataScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsContentTitle
import com.syncodec.graphite.presentation.settings.composable.dialog.SettingsDialogType
import org.koin.androidx.compose.koinViewModel


@Preview
@Composable
fun ImportDataScreen(
	openDialog : (SettingsDialogType) -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val viewModel : ImportDataViewModel = koinViewModel()

	Column(
		modifier = Modifier.fillMaxSize()
	) {
		SettingsContentTitle(title = "Import from")
		SettingButton(text = "Graphite", icon = R.drawable.ic_state)
		SettingButton(text = "Journey", icon = R.drawable.ic_state) { openDialog(SettingsDialogType.ImportDataJourney) }
	}
}

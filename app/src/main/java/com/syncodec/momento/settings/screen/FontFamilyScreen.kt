package com.syncodec.momento.settings.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.syncodec.momento.miscellaneous.DataStore
import com.syncodec.momento.settings.SettingsActivity
import com.syncodec.momento.settings.miscellaneous.SettingButton


@Composable
fun FontFamilyScreen() {
	val context = LocalContext.current
	val dataStore = DataStore(context = context)
	dataStore.getTypography.collectAsState(initial = 0)

	LazyColumn(
		modifier = Modifier
			.fillMaxSize()
	) {
		item { SettingButton(title = "Overlock") { dataStore.putTypography(0) } }
		item { SettingButton(title = "Source Sans Pro") { dataStore.putTypography(1) } }
		item { SettingButton(title = "Ubuntu") { dataStore.putTypography(2) } }
		item { SettingButton(title = "ATWriter") { dataStore.putTypography(3) } }
	}
}

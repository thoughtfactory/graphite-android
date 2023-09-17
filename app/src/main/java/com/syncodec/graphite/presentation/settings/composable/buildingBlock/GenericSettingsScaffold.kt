package com.syncodec.graphite.presentation.settings.composable.buildingBlock

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.presentation.common.button.BackButton
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun GenericSettingsScaffold(
	title: String = "Settings",
	onClickBack: () -> Unit = {},
	content: @Composable BoxScope.() -> Unit = {},
) {
	GenericScaffold2(
		topBar = {
			TopAppBar(
				navigationIcon = { BackButton() },
				title = { Text(text = title) },
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.background,
					navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
					titleContentColor = MaterialTheme.colorScheme.onBackground,
				)
			)
		},
		content = content
	)
}

package com.syncodec.graphite.presentation.settings.composable.buildingBlock

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.presentation.common.bar.GenericTopBar


@Preview
@Composable
fun GenericSettingsScaffold(
	title: String = "Settings",
	onClickBack: () -> Unit = {},
	content: @Composable () -> Unit = {},
) {
	Scaffold(
		topBar = {
			GenericTopBar(
				title = title,
				onNavigationIconClick = onClickBack,
			)
		}
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(it),
		) {
			content()
		}
	}
}

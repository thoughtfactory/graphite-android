package com.syncodec.graphite.presentation.common.bar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Preview
@Composable
fun GenericBottomBar(
	content: @Composable RowScope.() -> Unit = {}
) {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		Divider()
		BottomAppBar(
			modifier = Modifier.fillMaxWidth(),
			tonalElevation = 0.dp,
			containerColor = MaterialTheme.colorScheme.background,
			contentColor = MaterialTheme.colorScheme.onBackground,
		) {
			Spacer(modifier = Modifier.width(12.dp))
			content()
			Spacer(modifier = Modifier.width(12.dp))
		}
	}
}

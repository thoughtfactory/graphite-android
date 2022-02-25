package com.syncodec.momento.bucketComponent.miscellaneous

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun TopBar() {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(64.dp)
			.background(MaterialTheme.colorScheme.primaryContainer)
	) {
		Row(
			modifier = Modifier
				.fillMaxSize()
				.padding(8.dp)
		) {

		}
	}
}

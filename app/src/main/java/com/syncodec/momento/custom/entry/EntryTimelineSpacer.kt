package com.syncodec.momento.custom.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun EntryTimelineSpacer(
	tint: Color
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(8.dp)
			.padding(8.dp, 0.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Box(
			modifier = Modifier
				.width(16.dp)
				.height(8.dp),
			contentAlignment = Alignment.Center
		) {
			Box(
				modifier = Modifier
					.width(4.dp)
					.fillMaxHeight()
					.background(tint)
			)
		}
	}
}

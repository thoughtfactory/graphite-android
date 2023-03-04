package com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun NotebookTimelineSpacer(
	isVisible : Boolean
) {
	AnimatedVisibility(visible = isVisible) {
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
						.background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp))
				)
			}
		}
	}
}

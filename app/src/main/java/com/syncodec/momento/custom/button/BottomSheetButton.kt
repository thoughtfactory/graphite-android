package com.syncodec.momento.custom.button

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


data class MenuBottomSheetButtonData(val title: String, val imageVector: ImageVector, val highlight: Boolean = false, val onClick: () -> Unit)

@Composable
fun MenuBottomSheetButton(
	menuBottomSheetButtonData: MenuBottomSheetButtonData?
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		if (menuBottomSheetButtonData != null) {
			Crossfade(targetState = menuBottomSheetButtonData.highlight) { highlight ->
				if (highlight) {
					Card(
						elevation = 0.dp,
						backgroundColor = MaterialTheme.colorScheme.onPrimaryContainer,
						shape = RoundedCornerShape(12.dp),
						modifier = Modifier
							.fillMaxWidth()
							.aspectRatio(1f)
							.padding(6.dp)
							.focusable(true)
							.clickable(true) { menuBottomSheetButtonData.onClick() },
					) {
						Icon(
							imageVector = menuBottomSheetButtonData.imageVector,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.primaryContainer,
							modifier = Modifier
								.requiredSize(24.dp)
						)
					}
				} else {
					Card(
						elevation = 0.dp,
						backgroundColor = MaterialTheme.colorScheme.primaryContainer,
						shape = RoundedCornerShape(12.dp),
						modifier = Modifier
							.fillMaxWidth()
							.aspectRatio(1f)
							.padding(6.dp)
							.focusable(true)
							.clip(RoundedCornerShape(12.dp))
							.clickable(true) { menuBottomSheetButtonData.onClick() },
					) {
						Icon(
							imageVector = menuBottomSheetButtonData.imageVector,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.onPrimaryContainer,
							modifier = Modifier
								.requiredSize(24.dp)
						)
					}
				}
			}
			Text(
				text = menuBottomSheetButtonData.title,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onBackground,
				textAlign = TextAlign.Center,
				maxLines = 2,
				modifier = Modifier
					.fillMaxWidth()
			)
		}
	}
}

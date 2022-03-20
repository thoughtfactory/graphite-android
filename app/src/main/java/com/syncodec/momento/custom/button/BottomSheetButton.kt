package com.syncodec.momento.custom.button

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


data class MenuBottomSheetButtonData(
	val title: String,
	val resourceId: Int,
	val highlight: Boolean = false,
	val onClick: () -> Unit
)

@Composable
fun MenuBottomSheetButton(
	menuBottomSheetButtonData: MenuBottomSheetButtonData?,
	modifier: Modifier = Modifier
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = modifier
	) {
		if (menuBottomSheetButtonData != null) {
			val containerColor by animateColorAsState(targetValue = if (menuBottomSheetButtonData.highlight) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.onSecondaryContainer)
			val contentColor by animateColorAsState(targetValue = if (menuBottomSheetButtonData.highlight) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.secondaryContainer)
			Box(
				modifier = Modifier
					.requiredSize(72.dp)
					.clip(RoundedCornerShape(12.dp))
					.background(containerColor)
					.focusable(true)
					.clickable(true) { menuBottomSheetButtonData.onClick() },
				contentAlignment = Alignment.Center
			) {
				Icon(
					painter = painterResource(id = menuBottomSheetButtonData.resourceId),
					contentDescription = null,
					tint = contentColor,
					modifier = Modifier
						.requiredSize(24.dp)
				)
			}

			Spacer(modifier = Modifier.height(4.dp))

			Text(
				text = menuBottomSheetButtonData.title,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground,
				textAlign = TextAlign.Center,
				maxLines = 2,
				modifier = Modifier
					.fillMaxWidth()
			)
		} else {
			Spacer(modifier = Modifier.requiredSize(84.dp))
		}
		Spacer(modifier = Modifier.height(12.dp))
	}
}

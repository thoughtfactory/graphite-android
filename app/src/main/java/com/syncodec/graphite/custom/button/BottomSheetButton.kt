package com.syncodec.graphite.custom.button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


data class MenuBottomSheetButtonData(
	val title: String,
	val icon: Int,
	val highlight: Boolean = false,
	val onClick: () -> Unit
)

@Composable
fun MenuBottomSheetButton(
	buttonData: MenuBottomSheetButtonData?
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		if (buttonData != null) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.aspectRatio(1f)
					.padding(4.dp)
					.clip(RoundedCornerShape(12.dp))
					.background(MaterialTheme.colorScheme.background)
					.focusable(true)
					.clickable(true) { buttonData.onClick() },
				contentAlignment = Alignment.Center
			) {
				Icon(
					painter = painterResource(id = buttonData.icon),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.requiredSize(24.dp)
				)
			}

			Text(
				text = buttonData.title,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface,
				textAlign = TextAlign.Center,
				maxLines = 1,
				modifier = Modifier.fillMaxWidth()
			)

			Spacer(modifier = Modifier.height(4.dp))
		}
	}
}

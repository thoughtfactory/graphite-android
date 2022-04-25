package com.syncodec.momento.custom.button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.syncodec.momento.custom.squircle.SquircleShape
import com.syncodec.momento.miscellaneous.ThemeUtils.Companion.tone


data class MenuBottomSheetButtonData(
	val title: String,
	val icon: Int,
	val highlight: Boolean = false,
	val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
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
					.padding(6.dp)
					.clip(SquircleShape(4.0))
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
				maxLines = 2,
				modifier = Modifier.fillMaxWidth()
			)

			Spacer(modifier = Modifier.height(4.dp))
		}
	}
}

package com.syncodec.graphite.presentation.custom.button

import android.view.HapticFeedbackConstants
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.utils.MenuBottomSheetButtonDataPreviewParameter


data class MenuBottomSheetButtonData(
	val title: String,
	val icon: Int,
	val highlight: Boolean = false,
	val onClick: () -> Unit
)

@Preview
@Composable
fun MenuBottomSheetButton(
	@PreviewParameter(MenuBottomSheetButtonDataPreviewParameter::class) buttonData: MenuBottomSheetButtonData?,
	modifier: Modifier = Modifier,
) {
	val haptic = LocalHapticFeedback.current

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = modifier
	) {
		if (buttonData != null) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.aspectRatio(1f)
					.padding(4.dp)
					.clip(RoundedCornerShape(37))
					.background(MaterialTheme.colorScheme.background, RoundedCornerShape(28.dp))
					.focusable(true)
					.clickable(true) {
						buttonData.onClick()
						haptic.performHapticFeedback(HapticFeedbackType(HapticFeedbackConstants.LONG_PRESS))
					},
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
		} else {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.aspectRatio(1f)
			)
		}
	}
}

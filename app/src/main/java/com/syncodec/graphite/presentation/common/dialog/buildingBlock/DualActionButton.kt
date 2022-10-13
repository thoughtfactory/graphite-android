package com.syncodec.graphite.presentation.common.dialog.buildingBlock

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.utils.getInverseBWColor


@Composable
fun DualActionButtons(
	primaryText: String,
	secondaryText: String,
	onPrimaryClick: () -> Unit,
	onSecondaryClick: () -> Unit,
	primaryColor: Color? = null,
	secondaryColor: Color? = null,
	isOutlinedButton: Boolean = true,
	primaryEnabled: Boolean = true,
	secondaryEnabled: Boolean = true,
) {
	Row(
		modifier = Modifier.fillMaxWidth()
	) {
		Spacer(modifier = Modifier.weight(1f))
		if (isOutlinedButton) {
			OutlinedButton(
				onClick = onSecondaryClick,
				modifier = Modifier,
				colors = ButtonDefaults.outlinedButtonColors(
					containerColor = secondaryColor ?: MaterialTheme.colorScheme.background,
					contentColor = secondaryColor?.getInverseBWColor() ?: MaterialTheme.colorScheme.onBackground,
				),
				enabled = secondaryEnabled
			) {
				Text(
					text = secondaryText,
					color = secondaryColor?.getInverseBWColor() ?: MaterialTheme.colorScheme.onBackground,
					fontWeight = FontWeight.Bold
				)
			}
		} else {
			Button(
				onClick = onSecondaryClick,
				modifier = Modifier,
				colors = ButtonDefaults.buttonColors(
					containerColor = secondaryColor ?: MaterialTheme.colorScheme.background,
					contentColor = secondaryColor?.getInverseBWColor() ?: MaterialTheme.colorScheme.onBackground,
				),
				enabled = secondaryEnabled
			) {
				Text(
					text = secondaryText,
					color = secondaryColor?.getInverseBWColor() ?: MaterialTheme.colorScheme.onBackground,
					fontWeight = FontWeight.Bold
				)
			}
		}

		Spacer(modifier = Modifier.width(8.dp))

		Button(
			onClick = onPrimaryClick,
			modifier = Modifier,
			colors = ButtonDefaults.buttonColors(
				containerColor = primaryColor ?: MaterialTheme.colorScheme.primary,
				contentColor = primaryColor?.getInverseBWColor() ?: MaterialTheme.colorScheme.onPrimary,
			),
			enabled = primaryEnabled
		) {
			Text(
				text = primaryText,
				color = primaryColor?.getInverseBWColor() ?: MaterialTheme.colorScheme.onPrimary,
				fontWeight = FontWeight.Bold
			)
		}
	}
}

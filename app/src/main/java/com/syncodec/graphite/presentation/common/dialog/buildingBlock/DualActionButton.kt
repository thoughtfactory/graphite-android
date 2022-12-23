package com.syncodec.graphite.presentation.common.dialog.buildingBlock

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.utils.getInverseBWColor


@Preview
@Composable
fun DualActionButtons(
	primaryText: String = "Primary",
	secondaryText: String = "Secondary",
	onPrimaryClick: () -> Unit = {},
	onSecondaryClick: () -> Unit = {},
	primaryColor: Color? = null,
	secondaryColor: Color? = null,
	isOutlinedButton: Boolean = true,
	primaryEnabled: Boolean = true,
	secondaryEnabled: Boolean = true,
) {
	Row(
		modifier = Modifier.fillMaxWidth()
	) {
		if (isOutlinedButton) {
			Button(
				onClick = onSecondaryClick,
				shape = MaterialTheme.shapes.small,
				colors = ButtonDefaults.buttonColors(
					containerColor = secondaryColor ?: MaterialTheme.colorScheme.background,
					contentColor = secondaryColor?.getInverseBWColor() ?: MaterialTheme.colorScheme.onBackground,
				),
				enabled = secondaryEnabled,
				modifier = Modifier.weight(1f)
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
				shape = MaterialTheme.shapes.small,
				colors = ButtonDefaults.buttonColors(
					containerColor = secondaryColor ?: MaterialTheme.colorScheme.background,
					contentColor = secondaryColor?.getInverseBWColor() ?: MaterialTheme.colorScheme.onBackground,
				),
				enabled = secondaryEnabled,
				modifier = Modifier.weight(1f)
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
			shape = MaterialTheme.shapes.small,
			colors = ButtonDefaults.buttonColors(
				containerColor = primaryColor ?: MaterialTheme.colorScheme.primary,
				contentColor = primaryColor?.getInverseBWColor() ?: MaterialTheme.colorScheme.onPrimary,
			),
			enabled = primaryEnabled,
			modifier = Modifier.weight(1f)
		) {
			Text(
				text = primaryText,
				color = primaryColor?.getInverseBWColor() ?: MaterialTheme.colorScheme.onPrimary,
				fontWeight = FontWeight.Bold
			)
		}
	}
}

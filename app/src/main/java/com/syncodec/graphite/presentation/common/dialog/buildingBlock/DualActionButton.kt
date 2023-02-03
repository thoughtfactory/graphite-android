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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.utils.getInverseBWColor


@Preview
@Composable
fun DualActionButtons(
	primaryText: String = "Primary",
	secondaryText: String? = null,
	primaryColor: Color = MaterialTheme.colorScheme.primary,
	secondaryColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.31f),
	primaryEnabled: Boolean = true,
	secondaryEnabled: Boolean = true,
	onClickPrimary: () -> Unit = {},
	onClickSecondary: () -> Unit = {},
) {
	Row(
		modifier = Modifier.fillMaxWidth()
	) {
		secondaryText?.let {
			Button(
				onClick = onClickSecondary,
				shape = MaterialTheme.shapes.medium,
				colors = ButtonDefaults.buttonColors(
					containerColor = secondaryColor,
					contentColor = secondaryColor.getInverseBWColor(),
				),
				enabled = secondaryEnabled,
				modifier = Modifier.weight(1f)
			) {
				Text(
					text = it,
					color = secondaryColor.getInverseBWColor(),
				)
			}

			Spacer(modifier = Modifier.width(8.dp))
		}

		Button(
			onClick = onClickPrimary,
			shape = MaterialTheme.shapes.medium,
			colors = ButtonDefaults.buttonColors(
				containerColor = primaryColor,
				contentColor = primaryColor.getInverseBWColor(),
				disabledContainerColor = primaryColor.copy(alpha = 0.31f),
				disabledContentColor = primaryColor.getInverseBWColor().copy(alpha = 0.31f),
			),
			enabled = primaryEnabled,
			modifier = Modifier.weight(1f)
		) {
			Text(
				text = primaryText,
				color = primaryColor.getInverseBWColor(),
			)
		}
	}
}

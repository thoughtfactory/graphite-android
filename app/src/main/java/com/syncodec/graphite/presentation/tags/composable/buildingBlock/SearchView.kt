package com.syncodec.graphite.presentation.tags.composable.buildingBlock

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import com.syncodec.graphite.presentation.ui.IconButtonSize


@Preview
@Composable
fun SearchView(
	value: String = "",
	placeholder: String = "Add or search tags",
	isTagPresent: Boolean = false,
	onAddTag: () -> Unit = {},
	onValueChange: (String) -> Unit = {},
) {

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.padding(12.dp, 0.dp),
	) {
		BasicTextField(
			value = value,
			textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
			cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
			singleLine = true,
			onValueChange = onValueChange,
			modifier = Modifier.weight(1f),
		) {
			Box(
				contentAlignment = Alignment.CenterStart,
				modifier = Modifier
					.fillMaxWidth()
					.height((IconButtonSize * 2) + 2.dp)
					.background(
						MaterialTheme.colorScheme
							.surfaceColorAtElevation(8.dp)
							.copy(alpha = 0.31f), MaterialTheme.shapes.medium
					)
					.padding(12.dp, 0.dp),
			) {
				androidx.compose.animation.AnimatedVisibility(
					visible = value.isEmpty(),
					enter = fadeIn(tween(300)),
					exit = fadeOut(tween(300)),
				) {
					Text(
						text = placeholder,
						color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.47f),
						style = MaterialTheme.typography.bodyMedium,
					)
				}
				it()
			}
		}
		Spacer(modifier = Modifier.width(2.dp))
		MenuButton(
			icon = R.drawable.ic_add,
			colors = MenuButtonDefaults.menuButtonColorsOnSurface(),
			onClick = onAddTag,
		)
	}
}

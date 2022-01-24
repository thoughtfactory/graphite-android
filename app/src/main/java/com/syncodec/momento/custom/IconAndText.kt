package com.syncodec.momento.custom

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.roundToInt

@Composable
fun IconAndTextRow(
	icon: @Composable () -> Unit,
	text: @Composable () -> Unit
) {
	Layout(
		content = {
			icon.invoke()
			Box() {
				text.invoke()
			}
		}
	) { measurables, constraints ->
		val iconPlaceable = measurables[0].measure(constraints)
		val textPlaceable = measurables[1].measure(constraints)

		val height = constraints.maxHeight
		val iconPadding = (height.toFloat() - iconPlaceable.width) / 2f
		val expandedWidth = iconPlaceable.width + textPlaceable.width + iconPadding * 3

		layout(expandedWidth.toInt(), height) {
			iconPlaceable.place(
				iconPadding.roundToInt(),
				constraints.maxHeight / 2 - iconPlaceable.height / 2
			)
			textPlaceable.place(
				(iconPlaceable.width + iconPadding * 2).roundToInt(),
				constraints.maxHeight / 2 - textPlaceable.height / 2
			)
		}
	}
}

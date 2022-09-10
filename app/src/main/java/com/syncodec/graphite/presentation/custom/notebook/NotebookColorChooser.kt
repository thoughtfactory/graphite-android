package com.syncodec.graphite.presentation.custom.notebook

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.NotebookCard
import com.syncodec.graphite.utils.colorList


@Composable
fun NotebookColorChooser(
	currentColor: Color?,
	onChooseColor: (Color) -> Unit
) {
	LazyRow(
		modifier = Modifier.fillMaxWidth()
	) {
		item { Spacer(modifier = Modifier.width(20.dp)) }
		for (color in colorList) {
			item {
				NotebookCard(
					color = color.toArgb(),
					isSelected = currentColor == color,
					onClick = { onChooseColor(color) },
				)
			}
		}
		item { Spacer(modifier = Modifier.width(20.dp)) }
	}
}

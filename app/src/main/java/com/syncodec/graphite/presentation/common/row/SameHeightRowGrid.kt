package com.syncodec.graphite.presentation.common.row

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.utils.isTablet


@Composable
fun SameHeightRowGrid(
	modifier: Modifier = Modifier,
	minWidthItem: Dp = if (isTablet()) 144.dp else 96.dp,
	content: @Composable () -> Unit
) {
	SubcomposeLayout(
		modifier = modifier
	) { constraints ->
		val elementsPerRow = (constraints.maxWidth / minWidthItem.toPx()).toInt()
		val elementWidth = (constraints.maxWidth / elementsPerRow)

		val placeableList = subcompose(slotId = 0, content = content).map { it.measure(Constraints(maxWidth = elementWidth)) }.chunked(elementsPerRow)
		val maxHeightList = placeableList.map { it.maxOf { it.height } }
		val resizedPlaceables: List<List<Placeable>> = subcompose(slotId = 1, content = content).mapIndexed { index, measurable ->
			measurable.measure(Constraints(minWidth = elementWidth, maxWidth = elementWidth, minHeight = maxHeightList[index / elementsPerRow], maxHeight = maxHeightList[index / elementsPerRow]))
		}.chunked(elementsPerRow)


		val layoutHeight = resizedPlaceables.fold(0) { foldedHeight, current -> foldedHeight + current.maxOf { it.height } }

		layout(constraints.maxWidth, layoutHeight) {
			var currentY = 0
			resizedPlaceables.forEachIndexed { index, placeableList ->
				var currentX = 0
				placeableList.forEach { placeable ->
					placeable.place(currentX, currentY)
					currentX += placeable.width
				}
				currentY += placeableList.firstOrNull()?.height ?: 0
			}
		}
	}
}

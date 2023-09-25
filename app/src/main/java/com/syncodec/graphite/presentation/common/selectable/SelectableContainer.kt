package com.syncodec.graphite.presentation.common.selectable

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.conditional


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun SelectableContainer(
	modifier: Modifier = Modifier,
	shape: Shape = RectangleShape,
	border: BorderStroke? = null,
	selected: Boolean = false,
	enabled: Boolean = true,
	colors: SelectableContainerColors = SelectableContainerDefaults.selectableContainerColors(),
	onClick: () -> Unit = {},
	onLongClick: () -> Unit = {},
	content: @Composable () -> Unit = {},
) {
	val hapticFeedback = LocalHapticFeedback.current

	val containerColor by colors.containerColor(selected = selected)
	val contentColor by colors.contentColor(selected = selected)

	Surface(
		shape = shape,
		color = containerColor,
		contentColor = contentColor,
		tonalElevation = 0.dp,
		shadowElevation = 0.dp,
		modifier = modifier
			.conditional(border != null) { border(border ?: BorderStroke(Dp.Hairline, Color.Transparent), shape) }
			.clip(shape)
			.combinedClickable(
				enabled = enabled,
				onClick = onClick,
				onLongClick = {
					hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
					onLongClick()
				}
			),
		content = content,
	)
}

@Immutable
data class SelectableContainerColors(
	val containerColor: Color,
	val contentColor: Color,
	val selectedContainerColor: Color,
	val selectedContentColor: Color,
) {
	@Composable
	internal fun containerColor(selected: Boolean): State<Color> = animateColorAsState(targetValue = if (selected) selectedContainerColor else containerColor, label = "containerColor_animation")

	@Composable
	internal fun contentColor(selected: Boolean): State<Color> = animateColorAsState(targetValue = if (selected) selectedContentColor else contentColor, label = "contentColor_animation")

	override fun hashCode(): Int {
		var result = containerColor.hashCode()
		result = 31 * result + contentColor.hashCode()
		result = 31 * result + selectedContainerColor.hashCode()
		result = 31 * result + selectedContentColor.hashCode()
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as SelectableContainerColors

		if (containerColor != other.containerColor) return false
		if (contentColor != other.contentColor) return false
		if (selectedContainerColor != other.selectedContainerColor) return false
		return selectedContentColor == other.selectedContentColor
	}
}

object SelectableContainerDefaults {
	@Composable
	fun selectableContainerColors(
		containerColor: Color = MaterialTheme.colorScheme.background,
		contentColor: Color = MaterialTheme.colorScheme.onBackground,
		selectedContainerColor: Color = MaterialTheme.colorScheme.surface,
		selectedContentColor: Color = MaterialTheme.colorScheme.onSurface,
	): SelectableContainerColors = SelectableContainerColors(
		containerColor = containerColor,
		contentColor = contentColor,
		selectedContainerColor = selectedContainerColor,
		selectedContentColor = selectedContentColor,
	)

	@Composable
	fun surfaceColors(
		containerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.47f),
		contentColor: Color = MaterialTheme.colorScheme.onSurface,
		selectedContainerColor: Color = MaterialTheme.colorScheme.surface,
		selectedContentColor: Color = MaterialTheme.colorScheme.onSurface,
	): SelectableContainerColors = SelectableContainerColors(
		containerColor = containerColor,
		contentColor = contentColor,
		selectedContainerColor = selectedContainerColor,
		selectedContentColor = selectedContentColor,
	)
}

package com.syncodec.graphite.presentation.common.button

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.ui.IconButtonSize
import kotlinx.coroutines.launch


@Immutable
class MenuButtonColors constructor(
	val containerColor : Color,
	val iconColor : Color,
	val checkedContainerColor : Color = containerColor,
	val checkedIconColor : Color = iconColor,
) {
	@Composable
	internal fun containerColor(checked : Boolean) : State<Color> {
		return rememberUpdatedState(if (checked) containerColor else checkedContainerColor)
	}

	@Composable
	internal fun contentColor(enabled : Boolean) : State<Color> {
		return rememberUpdatedState(if (enabled) iconColor else checkedIconColor)
	}

	override fun hashCode() : Int {
		var result = containerColor.hashCode()
		result = 31 * result + iconColor.hashCode()
		result = 31 * result + checkedContainerColor.hashCode()
		result = 31 * result + checkedIconColor.hashCode()
		return result
	}

	override fun equals(other : Any?) : Boolean {
		if (this === other) return true
		if (other !is MenuButtonColors) return false

		if (containerColor != other.containerColor) return false
		if (iconColor != other.iconColor) return false
		if (checkedContainerColor != other.checkedContainerColor) return false
		if (checkedIconColor != other.checkedIconColor) return false

		return true
	}
}

object MenuButtonDefaults {
	@Composable
	fun menuButtonColors(
		containerColor : Color = MaterialTheme.colorScheme.background,
		iconColor : Color = MaterialTheme.colorScheme.onBackground,
		checkedContainerColor : Color = MaterialTheme.colorScheme.surface,
		checkedIconColor : Color = MaterialTheme.colorScheme.onSurface,
	) : MenuButtonColors = MenuButtonColors(
		containerColor = containerColor,
		iconColor = iconColor,
		checkedContainerColor = checkedContainerColor,
		checkedIconColor = checkedIconColor,
	)

	@Composable
	fun menuButtonColorsOnSurface(
		containerColor : Color = Color.Transparent,
		iconColor : Color = MaterialTheme.colorScheme.onSurface,
		checkedContainerColor : Color = MaterialTheme.colorScheme.background,
		checkedIconColor : Color = MaterialTheme.colorScheme.onBackground,
	) : MenuButtonColors = MenuButtonColors(
		containerColor = containerColor,
		iconColor = iconColor,
		checkedContainerColor = checkedContainerColor,
		checkedIconColor = checkedIconColor,
	)

	@Composable
	fun deleteButtonColors(
		containerColor : Color = Color.Transparent,
		iconColor : Color = MaterialTheme.colorScheme.error,
		checkedContainerColor : Color = Color.Transparent,
		checkedIconColor : Color = MaterialTheme.colorScheme.error,
	) : MenuButtonColors = MenuButtonColors(
		containerColor = containerColor,
		iconColor = iconColor,
		checkedContainerColor = checkedContainerColor,
		checkedIconColor = checkedIconColor,
	)
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MenuButton(
	modifier : Modifier = Modifier,
	icon : Int,
	tooltip : String? = null,
	checked : Boolean? = null,
	enabled : Boolean = true,
	shape : Shape = MaterialTheme.shapes.medium,
	colors : MenuButtonColors = MenuButtonDefaults.menuButtonColors(),
	showTooltipOnClick : Boolean = false,
	onClick : () -> Unit = {},
) {
	val scope = rememberCoroutineScope()

	val containerColor by animateColorAsState(
		targetValue = if (checked == true) colors.checkedContainerColor else colors.containerColor,
		animationSpec = tween(300)
	)
	val iconColor by animateColorAsState(
		targetValue = if (checked == true) colors.checkedIconColor else colors.iconColor,
		animationSpec = tween(300)
	)

	val rippleColor = if (checked == true) colors.containerColor else colors.checkedContainerColor
	val rippleIndication = rememberRipple(color = rippleColor)

	val tooltipState = remember { TooltipState() }

	CompositionLocalProvider(
		LocalIndication provides rippleIndication,
	) {
		PlainTooltipBox(
			tooltip = {
				Text(text = tooltip ?: "", style = MaterialTheme.typography.bodyMedium)
			},
			tooltipState = tooltipState,
		) {
			Box(
				contentAlignment = Alignment.Center,
				modifier = modifier
					.requiredSize((IconButtonSize * 2) + 2.dp)
					.padding(2.dp)
					.background(containerColor, shape)
					.clip(shape)
					.combinedClickable(
						enabled = enabled,
						onClick = {
							onClick()
							if (showTooltipOnClick) scope.launch { tooltipState.show() }
						},
						onLongClick = { scope.launch { tooltipState.show() } },
					)
			) {
				Icon(
					painter = painterResource(id = icon),
					contentDescription = tooltip,
					tint = iconColor.copy(alpha = if (enabled) 1f else 0.31f),
					modifier = Modifier.requiredSize(IconButtonSize)
				)
			}
		}
	}
}

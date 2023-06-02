package com.syncodec.graphite.presentation.settings.composable.buildingBlock

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R


@Preview
@Composable
fun SettingsButton(
	modifier: Modifier = Modifier,
	title: String = "Settings",
	subTitle: String? = null,
	leadingIcon: SettingsButtonIcon? = null,
	trailingIcon: SettingsButtonIcon? = SettingsButtonDefaults.settingsButtonTrailingIcon(),
	colors: SettingsButtonColors = SettingsButtonDefaults.settingsButtonColors(),
	enabled: Boolean = true,
	onClick: () -> Unit = { },
) {
	val contentColor by colors.contentColor(enabled)
	val containerColor by colors.containerColor(enabled)

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = modifier
			.fillMaxWidth()
			.background(containerColor)
			.clickable { onClick() }
			.padding(vertical = 20.dp)
	) {
		Spacer(modifier = Modifier.width(24.dp))
		leadingIcon?.let {
			Icon(
				painter = painterResource(id = it.icon),
				contentDescription = title,
				tint = it.color,
				modifier = Modifier.requiredSize(it.size)
			)
			Spacer(modifier = Modifier.width(24.dp))
		}
		Column(
			modifier = Modifier.weight(1f)
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.titleSmall,
				color = contentColor,
				fontWeight = FontWeight.Bold,
			)

			subTitle?.let {
				Text(
					text = it,
					style = MaterialTheme.typography.bodyMedium,
					color = contentColor.copy(alpha = 0.71f),
				)
			}
		}

		AnimatedContent(
			targetState = trailingIcon,
			label = "settingsButtonTrailingIcon_animation",
			transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) }
		) {
			it?.let {
				Column {
					Spacer(modifier = Modifier.width(24.dp))
					Icon(
						painter = painterResource(id = it.icon),
						contentDescription = "Next",
						tint = it.color,
						modifier = Modifier.requiredSize(it.size)
					)
				}
			}
		}

		Spacer(modifier = Modifier.width(24.dp))
	}
}

@Immutable
data class SettingsButtonIcon(
	val icon: Int,
	val color: Color,
	val size : Dp = 20.dp
) {
	override fun hashCode(): Int {
		var result = icon
		result = 31 * result + color.hashCode()
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is SettingsButtonIcon) return false

		if (icon != other.icon) return false
		return color == other.color
	}
}

@Immutable
data class SettingsButtonColors(
	val containerColor: Color,
	val contentColor: Color,
	val disabledContainerColor: Color,
	val disabledContentColor: Color,
) {
	@Composable
	internal fun containerColor(enabled: Boolean): State<Color> {
		return rememberUpdatedState(if (enabled) containerColor else disabledContainerColor)
	}

	@Composable
	internal fun contentColor(enabled: Boolean): State<Color> {
		return rememberUpdatedState(if (enabled) contentColor else disabledContentColor)
	}

	override fun hashCode(): Int {
		var result = containerColor.hashCode()
		result = 31 * result + contentColor.hashCode()
		result = 31 * result + disabledContainerColor.hashCode()
		result = 31 * result + disabledContentColor.hashCode()
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is SettingsButtonColors) return false

		if (containerColor != other.containerColor) return false
		if (contentColor != other.contentColor) return false
		if (disabledContainerColor != other.disabledContainerColor) return false
		return disabledContentColor == other.disabledContentColor
	}
}

object SettingsButtonDefaults {
	@Composable
	fun settingsButtonColors(
		containerColor: Color = MaterialTheme.colorScheme.background,
		contentColor: Color = MaterialTheme.colorScheme.onBackground,
		disabledContainerColor: Color = MaterialTheme.colorScheme.background,
		disabledContentColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
	): SettingsButtonColors = SettingsButtonColors(
		containerColor = containerColor,
		contentColor = contentColor,
		disabledContainerColor = disabledContainerColor,
		disabledContentColor = disabledContentColor,
	)

	@Composable
	fun warningSettingsButtonColors(
		containerColor: Color = MaterialTheme.colorScheme.background,
		contentColor: Color = MaterialTheme.colorScheme.error,
		disabledContainerColor: Color = MaterialTheme.colorScheme.background,
		disabledContentColor: Color = MaterialTheme.colorScheme.error.copy(alpha = 0.47f),
	): SettingsButtonColors = SettingsButtonColors(
		containerColor = containerColor,
		contentColor = contentColor,
		disabledContainerColor = disabledContainerColor,
		disabledContentColor = disabledContentColor,
	)

	@Composable
	fun settingsButtonLeadingIcon(
		icon: Int,
		color: Color = MaterialTheme.colorScheme.onBackground,
		size: Dp = 20.dp
	): SettingsButtonIcon = SettingsButtonIcon(icon = icon, color = color, size = size,)

	@Composable
	fun settingsButtonTrailingIcon(
		icon: Int = R.drawable.ic_flat_chevron,
		color: Color = MaterialTheme.colorScheme.onBackground,
		size: Dp = 20.dp
	): SettingsButtonIcon = SettingsButtonIcon(icon = icon, color = color, size = size,)
}

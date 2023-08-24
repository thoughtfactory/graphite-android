package com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.syncodec.graphite.R


@Preview
@Composable
fun GenericBottomSheetButton2(
	icon : Int = R.drawable.ic_flat_gallery,
	text : String = "Gallery",
	contentDescription : String = "Add from gallery",
	colors: GenericBottomSheetButton2Colors = GenericBottomSheetButton2Defaults.buttonColors(),
	enabled: Boolean = true,
	onClick : () -> Unit = {}
) {
	val containerColor by colors.containerColor(enabled = enabled)
	val iconColor by colors.iconColor(enabled = enabled)
	val contentColor by colors.contentColor(enabled = enabled)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
		modifier = Modifier
			.fillMaxWidth()
			.padding(4.dp)
			.background(containerColor, MaterialTheme.shapes.large)
			.clip(MaterialTheme.shapes.large)
			.clickable { onClick() }
			.padding(horizontal = 12.dp, vertical = 20.dp)
	) {
		Icon(
			painter = painterResource(id = icon),
			contentDescription = contentDescription,
			tint = iconColor,
			modifier = Modifier.requiredSize(32.dp)
		)
		Spacer(modifier = Modifier.height(4.dp))
		Text(
			text = text,
			style = MaterialTheme.typography.bodySmall,
			color = contentColor,
			fontWeight = FontWeight.Bold
		)
	}
}

@Immutable
data class GenericBottomSheetButton2Colors(
	val containerColor : Color,
	val contentColor : Color,
	val iconColor : Color,
	val disabledContainerColor : Color,
	val disabledContentColor : Color,
	val disabledIconColor : Color,
) {
	@Composable
	internal fun containerColor(enabled : Boolean) : State<Color> = rememberUpdatedState(if (enabled) containerColor else disabledContainerColor)
	@Composable
	internal fun contentColor(enabled : Boolean) : State<Color> = rememberUpdatedState(if (enabled) contentColor else disabledContentColor)
	@Composable
	internal fun iconColor(enabled : Boolean) : State<Color> = rememberUpdatedState(if (enabled) iconColor else disabledIconColor)

	override fun hashCode(): Int {
		var result = containerColor.hashCode()
		result = 31 * result + contentColor.hashCode()
		result = 31 * result + iconColor.hashCode()
		result = 31 * result + disabledContainerColor.hashCode()
		result = 31 * result + disabledContentColor.hashCode()
		result = 31 * result + disabledIconColor.hashCode()
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as GenericBottomSheetButton2Colors

		if (containerColor != other.containerColor) return false
		if (contentColor != other.contentColor) return false
		if (iconColor != other.iconColor) return false
		if (disabledContainerColor != other.disabledContainerColor) return false
		if (disabledContentColor != other.disabledContentColor) return false
		return disabledIconColor == other.disabledIconColor
	}
}

object GenericBottomSheetButton2Defaults {
	@Composable
	fun buttonColors(
		containerColor : Color = Color(ColorUtils.blendARGB(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).toArgb(), MaterialTheme.colorScheme.background.toArgb(), 0.71f)),
		iconColor : Color = MaterialTheme.colorScheme.onSurface,
		contentColor: Color = MaterialTheme.colorScheme.onSurface,
		disabledContainerColor : Color = Color(ColorUtils.blendARGB(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).toArgb(), MaterialTheme.colorScheme.background.toArgb(), 0.88f)),
		disabledIconColor : Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.31f),
		disabledContentColor : Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.31f),
	) : GenericBottomSheetButton2Colors = GenericBottomSheetButton2Colors(
		containerColor = containerColor,
		contentColor = contentColor,
		iconColor = iconColor,
		disabledContainerColor = disabledContainerColor,
		disabledContentColor = disabledContentColor,
		disabledIconColor = disabledIconColor,
	)

	@Composable
	fun primaryButtonColors(
		containerColor : Color = MaterialTheme.colorScheme.primary,
		iconColor : Color = MaterialTheme.colorScheme.onPrimary,
		contentColor: Color = MaterialTheme.colorScheme.onPrimary,
		disabledContainerColor : Color = MaterialTheme.colorScheme.primary,
		disabledIconColor : Color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.31f),
		disabledContentColor : Color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.31f),
	) : GenericBottomSheetButton2Colors = GenericBottomSheetButton2Colors(
		containerColor = containerColor,
		contentColor = contentColor,
		iconColor = iconColor,
		disabledContainerColor = disabledContainerColor,
		disabledContentColor = disabledContentColor,
		disabledIconColor = disabledIconColor,
	)

	@Composable
	fun errorButtonColors(
		containerColor : Color = MaterialTheme.colorScheme.errorContainer,
		iconColor : Color = MaterialTheme.colorScheme.onErrorContainer,
		contentColor: Color = MaterialTheme.colorScheme.onErrorContainer,
		disabledContainerColor : Color = MaterialTheme.colorScheme.errorContainer,
		disabledIconColor : Color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.31f),
		disabledContentColor : Color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.31f),
	) : GenericBottomSheetButton2Colors = GenericBottomSheetButton2Colors(
		containerColor = containerColor,
		contentColor = contentColor,
		iconColor = iconColor,
		disabledContainerColor = disabledContainerColor,
		disabledContentColor = disabledContentColor,
		disabledIconColor = disabledIconColor,
	)
}

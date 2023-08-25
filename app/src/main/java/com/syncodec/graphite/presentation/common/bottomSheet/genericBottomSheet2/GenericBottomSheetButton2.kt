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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.ui.IconButtonSize


@Preview
@Composable
fun GenericBottomSheetButton2(
	icon : Int = R.drawable.ic_fa_gallery,
	text : String = "Gallery",
	contentDescription : String = "Add from gallery",
	colors: GenericBottomSheetButton2Colors = GenericBottomSheetButton2Defaults.buttonColors(),
	checked: Boolean = false,
	onClick : () -> Unit = {}
) {
	val containerColor by colors.containerColor(checked = checked)
	val iconColor by colors.iconColor(checked = checked)
	val contentColor by colors.contentColor(checked = checked)

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
			modifier = Modifier.requiredSize(IconButtonSize)
		)
		Spacer(modifier = Modifier.height(8.dp))
		Text(
			text = text,
			style = MaterialTheme.typography.bodySmall,
			color = contentColor,
			textAlign = TextAlign.Center,
			fontWeight = FontWeight.Bold,
		)
	}
}

@Immutable
data class GenericBottomSheetButton2Colors(
	val containerColor : Color,
	val contentColor : Color,
	val iconColor : Color,
	val checkedContainerColor : Color,
	val checkedContentColor : Color,
	val checkedIconColor : Color,
) {
	@Composable
	internal fun containerColor(checked : Boolean) : State<Color> = rememberUpdatedState(if (checked) checkedContainerColor else containerColor)
	@Composable
	internal fun contentColor(checked : Boolean) : State<Color> = rememberUpdatedState(if (checked) checkedContentColor else contentColor)
	@Composable
	internal fun iconColor(checked : Boolean) : State<Color> = rememberUpdatedState(if (checked) checkedIconColor else iconColor)

	override fun hashCode(): Int {
		var result = containerColor.hashCode()
		result = 31 * result + contentColor.hashCode()
		result = 31 * result + iconColor.hashCode()
		result = 31 * result + checkedContainerColor.hashCode()
		result = 31 * result + checkedContentColor.hashCode()
		result = 31 * result + checkedIconColor.hashCode()
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as GenericBottomSheetButton2Colors

		if (containerColor != other.containerColor) return false
		if (contentColor != other.contentColor) return false
		if (iconColor != other.iconColor) return false
		if (checkedContainerColor != other.checkedContainerColor) return false
		if (checkedContentColor != other.checkedContentColor) return false
		return checkedIconColor == other.checkedIconColor
	}
}

object GenericBottomSheetButton2Defaults {
	@Composable
	fun buttonColors(
		containerColor : Color = Color(ColorUtils.blendARGB(MaterialTheme.colorScheme.surface.toArgb(), MaterialTheme.colorScheme.background.toArgb(), 0.71f)),
		iconColor : Color = MaterialTheme.colorScheme.onSurface,
		contentColor : Color = MaterialTheme.colorScheme.onSurface,
		checkedContainerColor : Color = MaterialTheme.colorScheme.primary,
		checkedIconColor : Color = MaterialTheme.colorScheme.onPrimary,
		checkedContentColor : Color = MaterialTheme.colorScheme.onPrimary,
	) : GenericBottomSheetButton2Colors = GenericBottomSheetButton2Colors(
		containerColor = containerColor,
		contentColor = contentColor,
		iconColor = iconColor,
		checkedContainerColor = checkedContainerColor,
		checkedContentColor = checkedContentColor,
		checkedIconColor = checkedIconColor,
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
		checkedContainerColor = disabledContainerColor,
		checkedContentColor = disabledContentColor,
		checkedIconColor = disabledIconColor,
	)
}

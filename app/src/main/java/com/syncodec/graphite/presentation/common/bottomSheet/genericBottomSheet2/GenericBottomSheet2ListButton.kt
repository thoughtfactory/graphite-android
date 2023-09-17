package com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.IconButtonSize


@Preview
@Composable
fun GenericBottomSheetButton2ListButton(
	icon : Int = R.drawable.ic_flat_gallery,
	text : String = "Gallery",
	subText : String? = null,
	suffixContent : (@Composable () -> Unit)? = null,
	contentDescription : String = "Add from gallery",
	colors: GenericBottomSheet2ListButtonColors = GenericBottomSheet2ListButtonDefaults.buttonColors(),
	enabled: Boolean = true,
	onClick : () -> Unit = {}
) {
	val containerColor by colors.containerColor(enabled = enabled)
	val iconColor by colors.iconColor(enabled = enabled)
	val contentColor by colors.contentColor(enabled = enabled)

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.background(containerColor)
			.clickable(enabled = enabled, onClick = onClick)
			.padding(vertical = 16.dp)
	) {
		Spacer(modifier = Modifier.width(32.dp))
		Icon(
			painter = painterResource(id = icon),
			contentDescription = contentDescription,
			tint = iconColor,
			modifier = Modifier.requiredSize(IconButtonSize)
		)

		Spacer(modifier = Modifier.width(16.dp))

		Column(
			modifier = Modifier.weight(1f)
		) {
			Text(
				text = text,
				style = MaterialTheme.typography.titleSmall,
				color = contentColor
			)
			subText?.let {
				Spacer(modifier = Modifier.height(2.dp))
				Text(
					text = text,
					style = MaterialTheme.typography.bodySmall,
					color = contentColor
				)
			}
		}

		suffixContent?.let {
			Spacer(modifier = Modifier.width(12.dp))
			it.invoke()
		}

		Spacer(modifier = Modifier.width(32.dp))
	}
}

@Immutable
data class GenericBottomSheet2ListButtonColors(
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

		other as GenericBottomSheet2ListButtonColors

		if (containerColor != other.containerColor) return false
		if (contentColor != other.contentColor) return false
		if (iconColor != other.iconColor) return false
		if (disabledContainerColor != other.disabledContainerColor) return false
		if (disabledContentColor != other.disabledContentColor) return false
		return disabledIconColor == other.disabledIconColor
	}
}

object GenericBottomSheet2ListButtonDefaults {

	@Composable
	fun transparentButtonColors(
		containerColor : Color = Color.Transparent,
		iconColor : Color = MaterialTheme.colorScheme.onBackground,
		contentColor: Color = MaterialTheme.colorScheme.onBackground,
		disabledContainerColor : Color = Color.Transparent,
		disabledIconColor : Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.31f),
		disabledContentColor : Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.31f),
	) : GenericBottomSheet2ListButtonColors = GenericBottomSheet2ListButtonColors(
		containerColor = containerColor,
		contentColor = contentColor,
		iconColor = iconColor,
		disabledContainerColor = disabledContainerColor,
		disabledContentColor = disabledContentColor,
		disabledIconColor = disabledIconColor,
	)

	@Composable
	fun buttonColors(
		containerColor : Color = Color(ColorUtils.blendARGB(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).toArgb(), MaterialTheme.colorScheme.background.toArgb(), 0.71f)),
		iconColor : Color = MaterialTheme.colorScheme.onSurface,
		contentColor: Color = MaterialTheme.colorScheme.onSurface,
		disabledContainerColor : Color = Color(ColorUtils.blendARGB(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).toArgb(), MaterialTheme.colorScheme.background.toArgb(), 0.88f)),
		disabledIconColor : Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.31f),
		disabledContentColor : Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.31f),
	) : GenericBottomSheet2ListButtonColors = GenericBottomSheet2ListButtonColors(
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
	) : GenericBottomSheet2ListButtonColors = GenericBottomSheet2ListButtonColors(
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
	) : GenericBottomSheet2ListButtonColors = GenericBottomSheet2ListButtonColors(
		containerColor = containerColor,
		contentColor = contentColor,
		iconColor = iconColor,
		disabledContainerColor = disabledContainerColor,
		disabledContentColor = disabledContentColor,
		disabledIconColor = disabledIconColor,
	)
}


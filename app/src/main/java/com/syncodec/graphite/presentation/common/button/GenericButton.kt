package com.syncodec.graphite.presentation.common.button

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.composable.FilterAndViewBottomSheet
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.presentation.ui.LockClosedContainer
import com.syncodec.graphite.utils.AuthenticatorScreen
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalIsAuthenticated


@Immutable
class GenericButtonColors(
	val containerColor: Color,
	val iconColor: Color,
	val checkedContainerColor: Color = containerColor,
	val checkedIconColor: Color = iconColor,
) {
	@Composable
	internal fun containerColor(checked: Boolean): State<Color> {
		return rememberUpdatedState(if (checked) containerColor else checkedContainerColor)
	}

	@Composable
	internal fun contentColor(enabled: Boolean): State<Color> {
		return rememberUpdatedState(if (enabled) iconColor else checkedIconColor)
	}

	override fun hashCode(): Int {
		var result = containerColor.hashCode()
		result = 31 * result + iconColor.hashCode()
		result = 31 * result + checkedContainerColor.hashCode()
		result = 31 * result + checkedIconColor.hashCode()
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is GenericButtonColors) return false

		if (containerColor != other.containerColor) return false
		if (iconColor != other.iconColor) return false
		if (checkedContainerColor != other.checkedContainerColor) return false
		if (checkedIconColor != other.checkedIconColor) return false

		return true
	}
}

object GenericButtonDefaults {
	@Composable
	fun genericButtonColors(
		containerColor: Color = MaterialTheme.colorScheme.background,
		iconColor: Color = MaterialTheme.colorScheme.onBackground,
		checkedContainerColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.47f),
		checkedIconColor: Color = MaterialTheme.colorScheme.onSurface,
	): GenericButtonColors = GenericButtonColors(
		containerColor = containerColor,
		iconColor = iconColor,
		checkedContainerColor = checkedContainerColor,
		checkedIconColor = checkedIconColor,
	)

	@Composable
	fun transparentButtonColors(
		containerColor: Color = Color.Transparent,
		iconColor: Color,
		checkedContainerColor: Color = Color.Transparent,
		checkedIconColor: Color = iconColor,
	): GenericButtonColors = GenericButtonColors(
		containerColor = containerColor,
		iconColor = iconColor,
		checkedContainerColor = checkedContainerColor,
		checkedIconColor = checkedIconColor,
	)

	@Composable
	fun bottomBarColorBlack(
		containerColor: Color = Color.Black,
		iconColor: Color = Color.White,
		checkedContainerColor: Color = Color.White,
		checkedIconColor: Color = Color.Black,
	): GenericButtonColors = GenericButtonColors(
		containerColor = containerColor,
		iconColor = iconColor,
		checkedContainerColor = checkedContainerColor,
		checkedIconColor = checkedIconColor,
	)

	@Composable
	fun bottomBarColorWhite(
		containerColor: Color = Color.White,
		iconColor: Color = Color.Black,
		checkedContainerColor: Color = Color.Black,
		checkedIconColor: Color = Color.White,
	): GenericButtonColors = GenericButtonColors(
		containerColor = containerColor,
		iconColor = iconColor,
		checkedContainerColor = checkedContainerColor,
		checkedIconColor = checkedIconColor,
	)

	@Composable
	fun genericButtonColorsOnSurface(
		containerColor: Color = MaterialTheme.colorScheme.surface,
		iconColor: Color = MaterialTheme.colorScheme.onSurface,
		checkedContainerColor: Color = MaterialTheme.colorScheme.background,
		checkedIconColor: Color = MaterialTheme.colorScheme.onBackground,
	): GenericButtonColors = GenericButtonColors(
		containerColor = containerColor,
		iconColor = iconColor,
		checkedContainerColor = checkedContainerColor,
		checkedIconColor = checkedIconColor,
	)

	@Composable
	fun deleteButtonColors(
		containerColor: Color = Color.Transparent,
		iconColor: Color = MaterialTheme.colorScheme.error,
		checkedContainerColor: Color = Color.Transparent,
		checkedIconColor: Color = MaterialTheme.colorScheme.error,
	): GenericButtonColors = GenericButtonColors(
		containerColor = containerColor,
		iconColor = iconColor,
		checkedContainerColor = checkedContainerColor,
		checkedIconColor = checkedIconColor,
	)
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GenericButton(
	modifier: Modifier = Modifier,
	icon: Int,
	tooltip: String? = null,
	badgeCount: Int? = null,
	checked: Boolean? = null,
	enabled: Boolean = true,
	shape: Shape = MaterialTheme.shapes.medium,
	colors: GenericButtonColors = GenericButtonDefaults.genericButtonColors(),
	buttonSize: Dp = IconButtonSize,
	showTooltipOnClick: Boolean = false,
	onClick: () -> Unit = {},
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


	CompositionLocalProvider(
		LocalIndication provides rippleIndication,
	) {
//		PlainTooltipBox(
//			tooltip = {
//				Text(text = tooltip ?: "", style = MaterialTheme.typography.bodyMedium)
//			},
//			tooltipState = tooltipState,
//		) {
		Box(
			contentAlignment = Alignment.Center,
			modifier = modifier
				.requiredSize((buttonSize * 2) + 2.dp)
				.padding(2.dp)
				.background(containerColor, shape)
				.combinedClickable(
					enabled = enabled,
					onClick = { onClick() },
				)
		) {
			AnimatedContent(
				targetState = icon,
				transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
				label = "genericButton"
			) { icon1 ->
				badgeCount?.let {
					BadgedBox(
						badge = {
							Badge(
								containerColor = MaterialTheme.colorScheme.primary,
								contentColor = MaterialTheme.colorScheme.onPrimary
							) {
								Text(text = it.toString())
							}
						}
					) {
						Icon(
							painter = painterResource(id = icon1),
							contentDescription = tooltip,
							tint = iconColor.copy(alpha = if (enabled) 1f else 0.31f),
							modifier = Modifier.requiredSize(buttonSize)
						)
					}
				} ?: Icon(
					painter = painterResource(id = icon1),
					contentDescription = tooltip,
					tint = iconColor.copy(alpha = if (enabled) 1f else 0.31f),
					modifier = Modifier.requiredSize(buttonSize)
				)
			}
		}
//		}
	}
}

@Preview
@Composable
fun BackButton(
	colors: GenericButtonColors = GenericButtonDefaults.transparentButtonColors(iconColor = MaterialTheme.colorScheme.onBackground),
	onClick: () -> Unit = {},
) {
	GenericButton(
		icon = R.drawable.ic_fa_back,
		colors = colors,
		onClick = onClick
	)
}

@Preview
@Composable
fun MenuButton(
	colors: GenericButtonColors = GenericButtonDefaults.transparentButtonColors(iconColor = MaterialTheme.colorScheme.onBackground),
	onClick: () -> Unit = {},
) {
	GenericButton(
		icon = R.drawable.ic_fa_menu,
		colors = colors,
		onClick = onClick
	)
}

@Preview
@Composable
fun ShareButton(
	colors: GenericButtonColors = GenericButtonDefaults.transparentButtonColors(iconColor = MaterialTheme.colorScheme.onBackground),
	onClick: () -> Unit = {},
) {
	GenericButton(
		icon = R.drawable.ic_fa_share,
		colors = colors,
		onClick = onClick
	)
}

@Preview
@Composable
fun DeleteButton(
	colors: GenericButtonColors = GenericButtonDefaults.deleteButtonColors(),
	onClick: () -> Unit = {},
) {
	GenericButton(
		icon = R.drawable.ic_fa_delete,
		colors = colors,
		onClick = onClick
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun FilterButton(
	colors: GenericButtonColors = GenericButtonDefaults.transparentButtonColors(iconColor = MaterialTheme.colorScheme.onBackground),
) {
	val bottomSheetState = rememberModalBottomSheetState()
	var isFilterAndViewBottomSheetVisible by remember { mutableStateOf(false) }

	GenericButton(
		icon = R.drawable.ic_fa_filter,
		colors = colors,
		onClick = { isFilterAndViewBottomSheetVisible = true }
	)

	FilterAndViewBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isFilterAndViewBottomSheetVisible,
		onDismissRequest = { isFilterAndViewBottomSheetVisible = false },
	)
}

@Preview
@Composable
fun MetadataButton(
	colors: GenericButtonColors = GenericButtonDefaults.transparentButtonColors(iconColor = MaterialTheme.colorScheme.onBackground),
	onClick: () -> Unit = {},
) {
	GenericButton(
		icon = R.drawable.ic_fa_info,
		tooltip = stringResource(id = R.string.info),
		colors = colors,
		onClick = onClick
	)
}

@Preview
@Composable
fun FavouriteButton(
	isFavourite: Boolean = false,
	colors: GenericButtonColors = GenericButtonDefaults.transparentButtonColors(
		iconColor = MaterialTheme.colorScheme.onBackground,
		checkedIconColor = Color.FavouriteContainer
	),
	onClick: () -> Unit = {},
) {
	GenericButton(
		icon = if (isFavourite) R.drawable.ic_fa_heart_solid else R.drawable.ic_fa_heart,
		tooltip = stringResource(id = R.string.favourite),
		checked = isFavourite,
		colors = colors,
		onClick = onClick
	)
}

@Preview
@Composable
fun LockButton(
	isLocked: Boolean = false,
	colors: GenericButtonColors = GenericButtonDefaults.transparentButtonColors(
		iconColor = MaterialTheme.colorScheme.onBackground,
		checkedIconColor = Color.LockClosedContainer
	),
	onClick: () -> Unit = {},
) {
	val context = LocalContext.current
	val isAuthenticate = LocalIsAuthenticated.current

	GenericButton(
		icon = if (isLocked) R.drawable.ic_fa_lock_close_solid else R.drawable.ic_fa_lock_open,
		tooltip = stringResource(id = R.string.lock),
		checked = isLocked,
		colors = colors,
		onClick = {
			if (isAuthenticate) onClick()
			else Toast.makeText(context, context.getText(R.string.toast_not_authenticated), Toast.LENGTH_SHORT).show()
		}
	)
}

@Preview
@Composable
fun VaultButton(
	colors: GenericButtonColors = GenericButtonDefaults.transparentButtonColors(iconColor = MaterialTheme.colorScheme.onBackground),
) {
	val isAuthenticate = LocalIsAuthenticated.current
	val onAuthenticationAction = LocalAuthenticatorAction.current

	GenericButton(
		icon = R.drawable.ic_fa_vault_duotone,
		tooltip = stringResource(id = R.string.vault),
		checked = isAuthenticate,
		colors = colors,
		onClick = { onAuthenticationAction(AuthenticatorScreen.Authenticate) },
	)
}

@Preview
@Composable
fun CheckButton(
	colors: GenericButtonColors = GenericButtonDefaults.transparentButtonColors(iconColor = MaterialTheme.colorScheme.onBackground),
	onClick: () -> Unit = {},
) {
	GenericButton(
		icon = R.drawable.ic_fa_check,
		tooltip = stringResource(id = R.string.done),
		colors = colors,
		onClick = onClick,
	)
}

@Preview
@Composable
fun PasteButton(
	colors: GenericButtonColors = GenericButtonDefaults.transparentButtonColors(iconColor = MaterialTheme.colorScheme.onBackground),
	onClick: (String) -> Unit = {},
) {
	val context = LocalContext.current
	val clipboardManager = LocalClipboardManager.current

	GenericButton(
		icon = R.drawable.ic_fa_paste,
		tooltip = stringResource(id = R.string.paste),
		colors = colors,
		onClick = {
			if (clipboardManager.hasText()) clipboardManager.getText()?.let { onClick(it.text) }
			else Toast.makeText(context, context.getText(R.string.toast_clipboard_is_empty), Toast.LENGTH_SHORT).show()
		}
	)
}

@Preview
@Composable
fun SearchButton(
	colors: GenericButtonColors = GenericButtonDefaults.transparentButtonColors(iconColor = MaterialTheme.colorScheme.onBackground),
	onClick: () -> Unit = {},
) {
	GenericButton(
		icon = R.drawable.ic_fa_search,
		tooltip = stringResource(id = R.string.search),
		colors = colors,
		onClick = onClick
	)
}

@Preview
@Composable
fun ClearButton(
	colors: GenericButtonColors = GenericButtonDefaults.transparentButtonColors(iconColor = MaterialTheme.colorScheme.onBackground),
	onClick: () -> Unit = {},
) {
	GenericButton(
		icon = R.drawable.ic_fa_x,
		tooltip = stringResource(id = R.string.clear),
		colors = colors,
		onClick = onClick
	)
}

@Preview
@Composable
fun OpenExternallyButton(
	colors: GenericButtonColors = GenericButtonDefaults.transparentButtonColors(iconColor = MaterialTheme.colorScheme.onBackground),
	onClick: () -> Unit = {},
) {
	GenericButton(
		icon = R.drawable.ic_fa_open_externally,
		tooltip = stringResource(id = R.string.open_externally),
		colors = colors,
		onClick = onClick
	)
}

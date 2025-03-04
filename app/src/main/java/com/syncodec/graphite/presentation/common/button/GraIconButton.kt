package com.syncodec.graphite.presentation.common.button

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import com.syncodec.graphite.presentation.ui.ColorDefaults.Favourite
import com.syncodec.graphite.presentation.ui.ColorDefaults.Lock
import com.syncodec.graphite.presentation.ui.ICON_SIZE


object GraIconButton {
    data class Colors(
        val iconColor: Color,
        val containerColor: Color,
        val checkedIconColor: Color = iconColor,
        val checkedContainerColor: Color = containerColor,
        val outlineColor: Color = Color.Transparent
    ) {
        @Composable
        fun containerColor(checked: Boolean): State<Color> {
            return rememberUpdatedState(if (checked) containerColor else checkedContainerColor)
        }

        @Composable
        fun contentColor(enabled: Boolean): State<Color> {
            return rememberUpdatedState(if (enabled) iconColor else checkedIconColor)
        }
    }

    object Defaults {
        @Composable
        fun colors(
            iconColor: Color = MaterialTheme.colorScheme.onBackground,
            containerColor: Color = MaterialTheme.colorScheme.background,
            checkedIconColor: Color = MaterialTheme.colorScheme.onSurface,
            checkedContainerColor: Color = MaterialTheme.colorScheme.surface,
            outlineColor: Color = Color.Transparent
        ) = Colors(iconColor = iconColor, containerColor = containerColor, checkedIconColor = checkedIconColor, checkedContainerColor = checkedContainerColor, outlineColor = outlineColor)

        @Composable
        fun transparentColors(
            iconColor: Color = MaterialTheme.colorScheme.onBackground,
            containerColor: Color = Color.Transparent,
            checkedIconColor: Color = MaterialTheme.colorScheme.onSurface,
            checkedContainerColor: Color = Color.Transparent,
            outlineColor: Color = Color.Transparent
        ) = Colors(iconColor = iconColor, containerColor = containerColor, checkedIconColor = checkedIconColor, checkedContainerColor = checkedContainerColor, outlineColor = outlineColor)

        @Composable
        fun deleteButtonColors(
            iconColor: Color = MaterialTheme.colorScheme.error,
            containerColor: Color = MaterialTheme.colorScheme.background,
            checkedIconColor: Color = MaterialTheme.colorScheme.onErrorContainer,
            checkedContainerColor: Color = MaterialTheme.colorScheme.errorContainer,
            outlineColor: Color = Color.Transparent
        ) = Colors(iconColor = iconColor, containerColor = containerColor, checkedIconColor = checkedIconColor, checkedContainerColor = checkedContainerColor, outlineColor = outlineColor)

        @Composable
        fun favouriteColors(
            iconColor: Color = MaterialTheme.colorScheme.onBackground,
            containerColor: Color = MaterialTheme.colorScheme.background,
            checkedIconColor: Color = Color.Favourite,
            checkedContainerColor: Color = containerColor,
            outlineColor: Color = Color.Transparent
        ) = Colors(iconColor = iconColor, containerColor = containerColor, checkedIconColor = checkedIconColor, checkedContainerColor = checkedContainerColor, outlineColor = outlineColor)

        @Composable
        fun lockColors(
            iconColor: Color = MaterialTheme.colorScheme.onBackground,
            containerColor: Color = MaterialTheme.colorScheme.background,
            checkedIconColor: Color = Color.Lock,
            checkedContainerColor: Color = containerColor,
            outlineColor: Color = Color.Transparent
        ) = Colors(iconColor = iconColor, containerColor = containerColor, checkedIconColor = checkedIconColor, checkedContainerColor = checkedContainerColor, outlineColor = outlineColor)

    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    fun Composable(
        icon: Int,
        checkedIcon: Int = icon,
        tooltip: String? = null,
        enabled: Boolean = true,
        checked: Boolean? = null,
        colors: Colors = Defaults.colors(),
        shape: Shape = MaterialTheme.shapes.medium,
        onClick: () -> Unit = {}
    ) {

        val containerColor by animateColorAsState(targetValue = if (checked == true) colors.checkedContainerColor else colors.containerColor, animationSpec = AnimationDefaults.stateAnimationSpec())
        val iconColor by animateColorAsState(targetValue = if (checked == true) colors.checkedIconColor else colors.iconColor, animationSpec = AnimationDefaults.stateAnimationSpec())

        tooltip?.let { tooltipText ->
            TooltipBox(
                state = rememberTooltipState(),
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = { PlainTooltip { Text(text = tooltipText) } },
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .requiredSize((ICON_SIZE * 2) + 2.dp)
                        .padding(2.dp)
                        .background(containerColor, shape)
                        .clip(shape)
                        .border(1.dp, colors.outlineColor, shape)
                        .combinedClickable(
                            enabled = enabled,
                            onClick = { onClick() },
                        )
                ) {
                    AnimatedContent(
                        targetState = checked,
                        transitionSpec = { AnimationDefaults.Fade },
                        label = "genericButton"
                    ) { checked1 ->
                        Icon(
                            painter = if (checked1==true) painterResource(id = checkedIcon) else painterResource(id = icon),
                            contentDescription = tooltip,
                            tint = iconColor.copy(alpha = if (enabled) 1f else 0.31f),
                            modifier = Modifier.requiredSize(ICON_SIZE)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun BackButton(
        colors: Colors = Defaults.transparentColors(),
        onClick: (() -> Unit)? = null,
    ) {
        val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

        Composable(
            icon = R.drawable.ic_fa_back,
            tooltip = stringResource(id = R.string.back),
            colors = colors,
            onClick = { onClick?.invoke() ?: onBackPressedDispatcher?.onBackPressed() },
        )
    }

    @Composable
    fun SearchButton(
        colors: Colors = Defaults.colors(),
        onClick: () -> Unit = {},
    ) {
        Composable(
            icon = R.drawable.ic_fa_search,
            tooltip = stringResource(id = R.string.search),
            colors = colors,
            onClick = onClick,
        )
    }

    @Composable
    fun MenuButton(
        colors: Colors = Defaults.colors(),
        onClick: () -> Unit = {},
    ) {
        Composable(
            icon = R.drawable.ic_fa_menu,
            tooltip = stringResource(id = R.string.menu),
            colors = colors,
            onClick = onClick,
        )
    }

    @Composable
    fun InfoButton(
        colors: Colors = Defaults.colors(),
        onClick: () -> Unit = {},
    ) {
        Composable(
            icon = R.drawable.ic_fa_info,
            tooltip = stringResource(id = R.string.metadata),
            colors = colors,
            onClick = onClick,
        )
    }

    @Composable
    fun VaultButton(
        colors: Colors = Defaults.colors(),
        checked: Boolean,
        onClick: () -> Unit = {},
    ) {
        Composable(
            icon = R.drawable.ic_fa_vault,
            tooltip = stringResource(id = R.string.vault),
            checked = checked,
            colors = colors,
            onClick = onClick,
        )
    }

    @Composable
    fun ClearTextButton(
        colors: Colors = Defaults.colors(),
        onClick: () -> Unit = {},
    ) {
        Composable(
            icon = R.drawable.ic_fa_x,
            tooltip = stringResource(id = R.string.clear_text),
            colors = colors,
            onClick = onClick,
        )
    }

    @Composable
    fun FilterAndSortTextButton(
        colors: Colors = Defaults.colors(),
        onClick: () -> Unit = {},
    ) {
        Composable(
            icon = R.drawable.ic_fa_sort,
            tooltip = stringResource(id = R.string.filter_and_sort),
            colors = colors,
            onClick = onClick,
        )
    }

    @Composable
    fun LockButton(
        isLocked: Boolean = false,
        colors: Colors = Defaults.lockColors(),
        onClick: () -> Unit = {},
    ) {
        Composable(
            icon = R.drawable.ic_fa_lock_opened,
            checkedIcon = R.drawable.ic_fa_lock_closed_duotone,
            tooltip = if (isLocked) stringResource(id = R.string.entry_locked) else stringResource(id = R.string.entry_not_locked),
            checked = isLocked,
            colors = colors,
            onClick = onClick,
        )
    }

    @Composable
    fun FavouriteButton(
        isFavourite: Boolean = false,
        colors: Colors = Defaults.favouriteColors(),
        onClick: () -> Unit = {},
    ) {
        Composable(
            icon = R.drawable.ic_fa_heart,
            checkedIcon = R.drawable.ic_fa_heart_solid,
            tooltip = stringResource(id = R.string.favourite),
            checked = isFavourite,
            colors = colors,
            onClick = onClick,
        )
    }

    @Composable
    fun PreviousButton(
        colors: Colors = Defaults.colors(),
        onClick: () -> Unit = {},
    ) {
        Composable(
            icon = R.drawable.ic_fa_arrow_left,
            tooltip = stringResource(id = R.string.previous),
            colors = colors,
            onClick = onClick,
        )
    }

    @Composable
    fun NextButton(
        colors: Colors = Defaults.colors(),
        onClick: () -> Unit = {},
    ) {
        Composable(
            icon = R.drawable.ic_fa_arrow_right,
            tooltip = stringResource(id = R.string.next),
            colors = colors,
            onClick = onClick,
        )
    }

    @Composable
    fun EditButton(
        colors: Colors = Defaults.colors(),
        onClick: () -> Unit = {},
    ) {
        Composable(
            icon = R.drawable.ic_fa_pencil,
            tooltip = stringResource(id = R.string.edit),
            colors = colors,
            onClick = onClick,
        )
    }

}

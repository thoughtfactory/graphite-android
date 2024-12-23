package com.syncodec.graphite.presentation.common.button

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.scaffold.GenericButton
import com.syncodec.graphite.presentation.ui.ANIMATION_TIME
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
        fun deleteButtonColor(
            iconColor: Color = MaterialTheme.colorScheme.error,
            containerColor: Color = MaterialTheme.colorScheme.background,
            checkedIconColor: Color = MaterialTheme.colorScheme.onErrorContainer,
            checkedContainerColor: Color = MaterialTheme.colorScheme.errorContainer,
            outlineColor: Color = Color.Transparent
        ) = Colors(iconColor = iconColor, containerColor = containerColor, checkedIconColor = checkedIconColor, checkedContainerColor = checkedContainerColor, outlineColor = outlineColor)

    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    fun Composable(
        icon: Int,
        tooltip: String? = null,
        enabled: Boolean = true,
        checked: Boolean? = null,
        colors: Colors = Defaults.colors(),
        shape: Shape = MaterialTheme.shapes.medium,
        onClick: () -> Unit = {}
    ) {

        val containerColor by animateColorAsState(targetValue = if (checked == true) colors.checkedContainerColor else colors.containerColor, animationSpec = tween(ANIMATION_TIME))
        val iconColor by animateColorAsState(targetValue = if (checked == true) colors.checkedIconColor else colors.iconColor, animationSpec = tween(ANIMATION_TIME))

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
                        targetState = icon,
                        transitionSpec = { fadeIn(tween(ANIMATION_TIME)) togetherWith fadeOut(tween(ANIMATION_TIME)) },
                        label = "genericButton"
                    ) { icon1 ->
                        Icon(
                            painter = painterResource(id = icon1),
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
        colors: Colors = Defaults.colors(),
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

}

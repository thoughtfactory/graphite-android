package com.syncodec.graphite.presentation.common.v2.selectable2

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import kotlinx.coroutines.flow.MutableStateFlow


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectableContainer2(
    modifier: Modifier = Modifier,
    selected: Boolean,
    enabled: Boolean,
    shape: Shape,
    border: BorderStroke? = null,
    shadowElevation: Dp = 0.dp,
    color: SelectableContainer2Defaults.Colors = SelectableContainer2Defaults.defaultColors(),
    interactionSource: MutableInteractionSource? = null,
    onClickLabel: String? = null,
    onLongClickLabel: String? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val containerColor by animateColorAsState(targetValue = if (selected) color.selectedContainerColor else color.containerColor, animationSpec = AnimationDefaults.stateAnimationSpec())

    Box(
        modifier = modifier
            .clip(shape = shape)
            .then(
                if (shadowElevation > 0.dp) Modifier.graphicsLayer(
                    shadowElevation = with(LocalDensity.current) { shadowElevation.toPx() },
                    shape = shape,
                    clip = false
                ) else Modifier
            )
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .background(color = containerColor, shape = shape)
            .clip(shape)
            .combinedClickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClickLabel = onClickLabel,
                onLongClickLabel = onLongClickLabel,
                role = Role.Button,
                onClick = onClick,
                onLongClick = {
                    onLongClick()
                    hapticFeedback.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                },
            ),
        content = content
    )
}

object SelectableContainer2Defaults {

    data class Colors(
        val containerColor: Color,
        val onContainerColor: Color,
        val selectedContainerColor: Color,
        val onSelectedContainerColor: Color,
    )

    @Composable
    fun defaultColors(
        containerColor: Color = MaterialTheme.colorScheme.surface,
        onContainerColor: Color = MaterialTheme.colorScheme.onSurface,
        selectedContainerColor: Color = MaterialTheme.colorScheme.surfaceBright,
        onSelectedContainerColor: Color = MaterialTheme.colorScheme.onSurface,
    ): Colors = Colors(
        containerColor = containerColor,
        onContainerColor = onContainerColor,
        selectedContainerColor = selectedContainerColor,
        onSelectedContainerColor = onSelectedContainerColor,
    )

    @Composable
    fun backgroundColors(
        containerColor: Color = MaterialTheme.colorScheme.background,
        onContainerColor: Color = MaterialTheme.colorScheme.onBackground,
        selectedContainerColor: Color = MaterialTheme.colorScheme.surfaceBright,
        onSelectedContainerColor: Color = MaterialTheme.colorScheme.onSurface,
    ): Colors = Colors(
        containerColor = containerColor,
        onContainerColor = onContainerColor,
        selectedContainerColor = selectedContainerColor,
        onSelectedContainerColor = onSelectedContainerColor,
    )

}

val LocalSelectionContainerActor = staticCompositionLocalOf<SelectionContainerActor> { error("SelectionContainerActor not provided") }

data class SelectionContainerActor(
    val isSelectingFlow: MutableStateFlow<Boolean> = MutableStateFlow(value = false),
    val selectedItemIdListFlow: MutableStateFlow<Set<Long>> = MutableStateFlow(value = setOf())
) {

    fun selectItem(objectBoxId: Long) {
        this.isSelectingFlow.tryEmit(value = true)
        val selectedItemIdList = selectedItemIdListFlow.value.toMutableSet()
        if (objectBoxId in selectedItemIdList) selectedItemIdList.remove(element = objectBoxId) else selectedItemIdList.add(element = objectBoxId)
        this.selectedItemIdListFlow.tryEmit(value = selectedItemIdList.toSet())
    }

    fun unselect() {
        this.isSelectingFlow.tryEmit(value = false)
        this.selectedItemIdListFlow.tryEmit(value = setOf())
    }

    companion object {
        fun initialize(): SelectionContainerActor = SelectionContainerActor()
    }
}

package com.syncodec.graphite.presentation.common.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.syncodec.graphite.presentation.ui.AnimationDefaults


@Composable
fun ScaleVisibility(
    modifier: Modifier = Modifier,
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = AnimationDefaults.ScaleAndFadeEnter,
        exit = AnimationDefaults.ScaleAndFadeExit,
        content = content
    )
}

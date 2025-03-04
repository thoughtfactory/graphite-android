package com.syncodec.graphite.presentation.common

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.syncodec.graphite.presentation.ui.AnimationDefaults

enum class ExpandableBoxOrientation {
	Horizontal,
	Vertical
}

@Composable
fun ExpandableBox(
	isVisible: Boolean = true,
	orientation: ExpandableBoxOrientation = ExpandableBoxOrientation.Vertical,
	durationMillis: Int = AnimationDefaults.ANIMATION_TIME,
	content: @Composable () -> Unit
) {
	val enterFadeIn = remember { fadeIn(animationSpec = TweenSpec(durationMillis = durationMillis, easing = FastOutLinearInEasing)) }
	val enterExpand = remember {
		if (orientation == ExpandableBoxOrientation.Vertical) {
			expandVertically(animationSpec = TweenSpec(durationMillis = durationMillis, easing = FastOutLinearInEasing))
		} else {
			expandHorizontally(animationSpec = TweenSpec(durationMillis = durationMillis, easing = FastOutLinearInEasing))
		}
	}
	val exitFadeOut = remember { fadeOut(animationSpec = TweenSpec(durationMillis = durationMillis, easing = LinearOutSlowInEasing)) }
	val exitCollapse = remember {
		if (orientation == ExpandableBoxOrientation.Vertical) {
			shrinkVertically(animationSpec = TweenSpec(durationMillis = durationMillis, easing = LinearOutSlowInEasing))
		} else {
			shrinkHorizontally(animationSpec = TweenSpec(durationMillis = durationMillis, easing = LinearOutSlowInEasing))
		}
	}

	AnimatedVisibility(
		visible = isVisible,
		enter = enterExpand + enterFadeIn,
		exit = exitCollapse + exitFadeOut
	) {
		content()
	}
}

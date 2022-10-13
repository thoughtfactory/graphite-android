package com.syncodec.graphite.presentation.common

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

enum class ExpandableBoxOrientation {
	HORIZONTAL,
	VERTICAL
}

@Composable
fun ExpandableBox(
	isVisible: Boolean = true,
	orientation: ExpandableBoxOrientation = ExpandableBoxOrientation.VERTICAL,
	durationMillis: Int = 300,
	content: @Composable () -> Unit
) {
	val enterFadeIn = remember { fadeIn(animationSpec = TweenSpec(durationMillis = durationMillis, easing = FastOutLinearInEasing)) }
	val enterExpand = remember {
		if (orientation == ExpandableBoxOrientation.VERTICAL) {
			expandVertically(animationSpec = TweenSpec(durationMillis = durationMillis, easing = FastOutLinearInEasing))
		} else {
			expandHorizontally(animationSpec = TweenSpec(durationMillis = durationMillis, easing = FastOutLinearInEasing))
		}
	}
	val exitFadeOut = remember { fadeOut(animationSpec = TweenSpec(durationMillis = durationMillis, easing = LinearOutSlowInEasing)) }
	val exitCollapse = remember {
		if (orientation == ExpandableBoxOrientation.VERTICAL) {
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

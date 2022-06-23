package com.syncodec.graphite.custom

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun ExpandableBox(
	isVisible: Boolean = true,
	initialVisibility: Boolean = false,
	content: @Composable () -> Unit
) {
	val enterFadeIn = remember {
		fadeIn(
			animationSpec = TweenSpec(
				durationMillis = 600,
				easing = FastOutLinearInEasing
			)
		)
	}
	val enterExpand = remember {
		expandVertically(animationSpec = tween(600))
	}
	val exitFadeOut = remember {
		fadeOut(
			animationSpec = TweenSpec(
				durationMillis = 600,
				easing = LinearOutSlowInEasing
			)
		)
	}
	val exitCollapse = remember {
		shrinkVertically(animationSpec = tween(600))
	}
	AnimatedVisibility(
		visible = isVisible,
		enter = enterExpand + enterFadeIn,
		exit = exitCollapse + exitFadeOut
	) {
		content()
	}
}

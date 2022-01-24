package com.syncodec.momento.custom

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
@OptIn(ExperimentalAnimationApi::class)
fun ExpandableBox(
	visible: Boolean = true,
	initialVisibility: Boolean = false,
	content: @Composable () -> Unit
) {
	val enterFadeIn = remember {
		fadeIn(
			animationSpec = TweenSpec(
				durationMillis = 400,
				easing = FastOutLinearInEasing
			)
		)
	}
	val enterExpand = remember {
		expandVertically(animationSpec = tween(400))
	}
	val exitFadeOut = remember {
		fadeOut(
			animationSpec = TweenSpec(
				durationMillis = 400,
				easing = LinearOutSlowInEasing
			)
		)
	}
	val exitCollapse = remember {
		shrinkVertically(animationSpec = tween(400))
	}
	AnimatedVisibility(
		visible = visible,
		initiallyVisible = initialVisibility,
		enter = enterExpand + enterFadeIn,
		exit = exitCollapse + exitFadeOut
	) {
		content()
	}
}

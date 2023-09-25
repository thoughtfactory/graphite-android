package com.syncodec.graphite.presentation.common.component.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Preview
@Composable
fun HeaderText(
	modifier : Modifier = Modifier,
	text : String = "Header",
	fontStyle: FontStyle = FontStyle.Normal,
) {
	AnimatedContent(
		targetState = text,
		label = "headerText_animation",
		transitionSpec = { scaleIn(tween(470)) + fadeIn(tween(470)) togetherWith scaleOut(tween(470)) + fadeOut(tween(470)) },
		modifier = modifier
	) {
		Text(
			text = it,
			style = MaterialTheme.typography.bodySmall,
			fontWeight = FontWeight.Bold,
			fontStyle = fontStyle,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
		)
	}
}


@Preview
@Composable
fun DotSeparator() {
	Text(
		text = "·",
		style = MaterialTheme.typography.bodySmall,
		fontWeight = FontWeight.Bold,
		maxLines = 1,
		overflow = TextOverflow.Ellipsis,
		modifier = Modifier.padding(horizontal = 2.dp)
	)
}

package com.syncodec.graphite.presentation.common.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS


@Composable
fun AnimatedText(
	modifier: Modifier = Modifier,
	text: String?,
	style: TextStyle = LocalTextStyle.current,
	color: Color = Color.Unspecified,
	fontStyle: FontStyle? = null,
	fontWeight: FontWeight? = null,
	textAlign: TextAlign? = null,
	maxLines: Int = Int.MAX_VALUE,
	overflow: TextOverflow = TextOverflow.Clip,
	transitionSpec: AnimatedContentTransitionScope<String?>.() -> ContentTransform = { fadeIn(tween(ANIMATION_DURATION_MILLIS)) togetherWith fadeOut(tween(ANIMATION_DURATION_MILLIS)) }
) {
	AnimatedContent(
		targetState = text,
		transitionSpec = transitionSpec,
		label = "AnimatedText_animation"
	) {
		Text(
			text = it ?: "",
			style = style,
			color = color,
			fontStyle = fontStyle,
			fontWeight = fontWeight,
			textAlign = textAlign,
			maxLines = maxLines,
			overflow = overflow,
			modifier = modifier,
		)
	}
}

package com.syncodec.graphite.presentation.common.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.R


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedScrollText(
	animatedText : String,
	staticText : String,
	color : Color
) {
	Row(
		modifier = Modifier,
		verticalAlignment = Alignment.CenterVertically
	) {
		AnimatedContent(
			targetState = animatedText,
			transitionSpec = {
				if (targetState > initialState) {
					slideInVertically { height -> height } + fadeIn() with
							slideOutVertically { height -> - height } + fadeOut()
				} else {
					slideInVertically { height -> - height } + fadeIn() with
							slideOutVertically { height -> height } + fadeOut()
				}.using(
					SizeTransform(clip = false)
				)
			}
		) {
			Text(
				text = it,
				fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
				fontWeight = FontWeight.Bold,
				fontSize = 20.sp,
				lineHeight = 24.sp,
				letterSpacing = 2.sp,
				color = color
			)
		}

		Spacer(modifier = Modifier.width(0.dp))

		Text(
			text = staticText,
			fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
			fontWeight = FontWeight.Bold,
			fontSize = 20.sp,
			lineHeight = 24.sp,
			letterSpacing = 2.sp,
			color = color
		)
	}
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedText(
	modifier : Modifier = Modifier,
	text : String?,
	style : TextStyle = LocalTextStyle.current,
	color : Color = Color.Unspecified,
	fontStyle : FontStyle? = null,
	fontWeight : FontWeight? = null,
	textAlign: TextAlign? = null,
	transitionSpec : AnimatedContentScope<String?>.() -> ContentTransform = { fadeIn(tween(300)) with fadeOut(tween(300)) }
) {
	AnimatedContent(
		targetState = text,
		transitionSpec = transitionSpec
	) {
		Text(
			text = it ?: "",
			style = style,
			color = color,
			modifier = modifier,
			fontStyle = fontStyle,
			fontWeight = fontWeight,
			textAlign = textAlign
		)
	}
}

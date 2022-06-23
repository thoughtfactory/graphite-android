package com.syncodec.graphite.custom.animation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.R


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedText(
	animatedText: String,
	staticText: String,
	color: Color
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
							slideOutVertically { height -> -height } + fadeOut()
				} else {
					slideInVertically { height -> -height } + fadeIn() with
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

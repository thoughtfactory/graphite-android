package com.syncodec.graphite.presentation.main.composable.buildingBlock

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.R
import java.time.LocalDate


@Preview
@Composable
fun YearProgressBar() {
	var startAnimation by rememberSaveable { mutableStateOf(false) }
	val localDate = remember { LocalDate.now() }
	val totalDays = remember { if (localDate.isLeapYear) 366 else 365 }
	val progress by animateIntAsState(
		targetValue = if (startAnimation) localDate.dayOfYear else 0,
		animationSpec = tween(2400),
		label = "yearProgressBar_animation"
	)

	LaunchedEffect(key1 = startAnimation) { startAnimation = true }

	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		Card(
			colors = CardDefaults.cardColors(Color.Transparent),
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp, 24.dp, 12.dp, 12.dp),
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = "${localDate.year}",
					fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
					fontWeight = FontWeight.Bold,
					fontSize = 18.sp,
					lineHeight = 20.sp,
					letterSpacing = 2.sp,
					color = MaterialTheme.colorScheme.onBackground
				)

				Spacer(modifier = Modifier.width(12.dp))

				Box(
					modifier = Modifier
						.weight(1f)
						.background(Color.Transparent)
						.clip(MaterialTheme.shapes.small)
						.border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
				) {
					Box(
						modifier = Modifier
							.fillMaxWidth(progress.toFloat() / totalDays)
							.height(12.dp)
							.background(MaterialTheme.colorScheme.primary)
					)
				}

				Spacer(modifier = Modifier.width(12.dp))

				YearPercentageAnimation(
					animatedText = (progress.toFloat() * 100 / totalDays).toInt().toString(),
					staticText = "%",
					color = MaterialTheme.colorScheme.onBackground
				)
			}
		}

		Spacer(modifier = Modifier.height(8.dp))
	}
}

@Composable
private fun YearPercentageAnimation(
	animatedText: String,
	staticText: String,
	color: Color
) {
	Row(
		modifier = Modifier,
		verticalAlignment = Alignment.Bottom
	) {
		AnimatedContent(
			targetState = animatedText,
			transitionSpec = {
				if (targetState > initialState) (slideInVertically { height -> height } + fadeIn() togetherWith slideOutVertically { height -> -height } + fadeOut()).using(SizeTransform(clip = false))
				else (slideInVertically { height -> -height } + fadeIn() togetherWith slideOutVertically { height -> height } + fadeOut()).using(SizeTransform(clip = false))
			},
			label = "animatedScrollText_animation"
		) {
			Text(
				text = it,
				fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
				fontWeight = FontWeight.Bold,
				fontSize = 24.sp,
				lineHeight = 28.sp,
				letterSpacing = 2.sp,
				color = color
			)
		}

		Text(
			text = staticText,
			fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
			fontWeight = FontWeight.Bold,
			fontSize = 24.sp,
			lineHeight = 28.sp,
			letterSpacing = 2.sp,
			color = color
		)
	}
}


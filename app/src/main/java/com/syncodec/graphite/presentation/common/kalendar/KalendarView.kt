package com.syncodec.graphite.presentation.common.kalendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import com.syncodec.graphite.presentation.common.kalendar.horizontalKalendar.HorizontalKalendar
import com.syncodec.graphite.presentation.common.kalendar.verticalKalendar.VerticalKalendar


enum class KalendarOrientation {
	HORIZONTAL,
	VERTICAL
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Kalendar(
	orientation: KalendarOrientation = KalendarOrientation.VERTICAL,
) {
	AnimatedContent(targetState = orientation) {
		when (it) {
			KalendarOrientation.HORIZONTAL -> HorizontalKalendar()
			KalendarOrientation.VERTICAL -> VerticalKalendar()
		}
	}
}

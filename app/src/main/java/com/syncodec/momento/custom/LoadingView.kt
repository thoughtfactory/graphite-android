package com.syncodec.momento.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.momento.R


@Composable
fun LoadingView() {
	val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading))

	Box(
		modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
		contentAlignment = Alignment.Center
	) {
		LottieAnimation(
			composition = lottieComposition,
			iterations = LottieConstants.IterateForever,
			modifier = Modifier.requiredSize(64.dp)
		)
	}
}

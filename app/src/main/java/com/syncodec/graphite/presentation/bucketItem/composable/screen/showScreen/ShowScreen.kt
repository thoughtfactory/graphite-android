package com.syncodec.graphite.presentation.bucketItem.composable.screen.showScreen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.presentation.bucketItem.BucketItemViewModel
import com.syncodec.graphite.presentation.common.ErrorView
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.utils.Status


@Composable
fun ShowScreen(
	movieId: String?,
	tvId: String?,
	currentState: Int,
	onChangeState: (Int) -> Unit
) {
	when {
		movieId != null -> MovieScreen(currentState = currentState, onChangeState = onChangeState)
		tvId != null -> TvScreen(currentState = currentState, onChangeState = onChangeState)
		else -> ErrorView()
	}
}

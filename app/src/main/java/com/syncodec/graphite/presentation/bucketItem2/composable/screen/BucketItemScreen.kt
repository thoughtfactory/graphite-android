package com.syncodec.graphite.presentation.bucketItem2.composable.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.ShowType
import com.syncodec.graphite.presentation.bucketItem2.composable.bar.BottomBar
import com.syncodec.graphite.presentation.bucketItem2.composable.bar.TopBar
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionBucketType
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionShowType
import com.syncodec.graphite.presentation.common.ErrorView
import com.syncodec.graphite.presentation.common.LoadingView


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BucketItemScreen() {

	val bucketType = LocalCompositionBucketType.current
	val showType = LocalCompositionShowType.current

	Scaffold(
		topBar = { TopBar() },
		bottomBar = {
			BottomBar(
				onClickShare = {},
				onClickDelete = { },
				onClickMove = {}
			)
		},
		modifier = Modifier.fillMaxSize()
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(it)
		) {
			Crossfade(targetState = bucketType) {
				when (it) {
					BucketType.BOOK -> BookScreen()
					BucketType.SHOW -> when (showType) {
						ShowType.TV -> TvScreen()
						ShowType.MOVIE -> MovieScreen()
						null -> null
					}
					else -> ErrorView()
				}
			}
		}
	}
}

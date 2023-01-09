package com.syncodec.graphite.presentation.common.scaffold

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Preview
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GenericScaffold(
	modalBottomSheetState : ModalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden),
	sheetContent : @Composable ColumnScope.() -> Unit = { },
	topBar : @Composable () -> Unit = { },
	bottomBar : @Composable () -> Unit = { },
	floatingActionButton : @Composable () -> Unit = { },
	dialogContent : @Composable () -> Unit = { },
	snackbarHost: @Composable () -> Unit = { },
	content : @Composable () -> Unit = { },
) {
	val scaffoldBlurRadius by animateFloatAsState(targetValue = if (modalBottomSheetState.progress.to == ModalBottomSheetValue.Hidden) (32.002f - 0.001f - (modalBottomSheetState.progress.fraction * 32f)) else (0.001f + (modalBottomSheetState.progress.fraction * 32f)))
	val scaffoldBackgroundColor by animateColorAsState(
		targetValue = if (modalBottomSheetState.progress.to == ModalBottomSheetValue.Hidden) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f * (1 - modalBottomSheetState.progress.fraction))
		else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f * modalBottomSheetState.progress.fraction)
	)

	ModalBottomSheetLayout(
		sheetState = modalBottomSheetState,
		sheetElevation = 0.dp,
		sheetBackgroundColor = Color.Transparent,
		sheetContent = {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp)
					.background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.extraLarge)
			) {
				Column(
					horizontalAlignment = Alignment.CenterHorizontally,
					modifier = Modifier
						.fillMaxWidth()
						.padding(24.dp, 12.dp, 24.dp, 20.dp),
					content = sheetContent
				)
			}
		},
		modifier = Modifier.fillMaxSize()
	) {
		Scaffold(
			topBar = topBar,
			bottomBar = bottomBar,
			modifier = Modifier
				.fillMaxSize()
				.graphicsLayer {
					this.renderEffect = BlurEffect(scaffoldBlurRadius, scaffoldBlurRadius, TileMode.Clamp)
				},
			snackbarHost = snackbarHost,
			floatingActionButton = floatingActionButton,
			floatingActionButtonPosition = FabPosition.End
		) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(it),
			) {
				content()
				dialogContent()
			}
		}
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(scaffoldBackgroundColor)
		)
	}
}

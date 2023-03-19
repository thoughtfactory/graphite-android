package com.syncodec.graphite.presentation.common

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun LoadingView(
	modifier : Modifier = Modifier.fillMaxSize(),
	backgroundColor : Color = Color.Transparent,
) {
	Box(
		modifier = modifier.background(backgroundColor),
		contentAlignment = Alignment.Center
	) {
		CircularProgressIndicator(
			color = MaterialTheme.colorScheme.primary,
			strokeWidth = 4.dp
		)
	}
}

@Composable
fun LoadingView(
	onReload: () -> Unit
) {
	val scope = rememberCoroutineScope()

	var showReloadButton by remember { mutableStateOf(false) }

	LaunchedEffect(key1 = showReloadButton) {
		if (!showReloadButton) {
			scope.launch {
				delay(3000)
				showReloadButton = true
			}
		}
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(Color.Transparent),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Spacer(modifier = Modifier.weight(1f))
		CircularProgressIndicator(
			color = MaterialTheme.colorScheme.primary,
			strokeWidth = 4.dp
		)
		Spacer(modifier = Modifier.height(16.dp))
		Box(
			modifier = Modifier.weight(1f),
			contentAlignment = Alignment.TopCenter
		) {
			Crossfade(
				targetState = showReloadButton,
				animationSpec = tween(300)
			) {
				if (it) {
					OutlinedButton(
						onClick = {
							onReload()
							showReloadButton = false
						}
					) {
						Text(text = "Reload")
					}
				}
			}
		}
	}
}

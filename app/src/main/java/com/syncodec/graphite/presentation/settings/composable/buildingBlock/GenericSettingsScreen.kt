package com.syncodec.graphite.presentation.settings.composable.buildingBlock

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.min


@Composable
fun GenericSettingsScreen(
	title: String,
	scrollState: ScrollState,
	content: @Composable () -> Unit
) {
	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp

	Box {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.verticalScroll(scrollState),
		) {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
					.fillMaxWidth()
					.height(screenHeight / 3)
					.background(MaterialTheme.colorScheme.background)
					.graphicsLayer { translationY = 0.5f * scrollState.value }
			) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.height(screenHeight / 3)
						.padding(24.dp, 16.dp, 16.dp, 16.dp)
				) {
					Spacer(modifier = Modifier.weight(1f))
					Text(
						text = title,
						style = MaterialTheme.typography.displayMedium,
						color = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier
					)
					Spacer(modifier = Modifier.height(with(LocalDensity.current) { scrollState.value.toDp() / 2 }))
				}
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(screenHeight / 3)
						.background(
							MaterialTheme.colorScheme.background.copy(
								alpha = min(1f, ((scrollState.value.toFloat() / screenHeight.value) * 1.66f))
							)
						)
				)
			}
			content()

			Spacer(modifier = Modifier.height(screenHeight / 3))
		}
	}

}

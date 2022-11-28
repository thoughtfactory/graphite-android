package com.syncodec.graphite.presentation.settings.composable.bar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import kotlin.math.min


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun TopBar(
	title: String,
	scrollState: ScrollState,
	onClickBack: () -> Unit,
) {

	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp
	val screenHeight = configuration.screenHeightDp.dp

	val alpha by animateFloatAsState(targetValue = min(1f, ((scrollState.value.toFloat() / screenHeight.value) * 1.66f)))

	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		TopAppBar(
			modifier = Modifier.fillMaxWidth(),
			navigationIcon = {
				MenuButton(
					icon = R.drawable.ic_back,
					tint = MaterialTheme.colorScheme.onBackground,
					onClick = onClickBack
				)
			},
			title = {
				AnimatedContent(targetState = title) {
					Text(
						text = it,
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Bold,
						modifier = Modifier.graphicsLayer { this.alpha = alpha }
					)
				}
			},
			colors = TopAppBarDefaults.topAppBarColors(
				containerColor = MaterialTheme.colorScheme.background,
				navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
				titleContentColor = MaterialTheme.colorScheme.onBackground,
			)
		)

		Spacer(
			modifier = Modifier
				.fillMaxWidth(min(1f, ((scrollState.value.toFloat() / screenHeight.value) * 1.66f)))
				.height(1.dp)
				.padding(32.dp, 0.dp)
				.background(
					MaterialTheme
						.colorScheme
						.onBackground.copy(alpha = alpha.times(0.47f))
				)
		)
	}
}

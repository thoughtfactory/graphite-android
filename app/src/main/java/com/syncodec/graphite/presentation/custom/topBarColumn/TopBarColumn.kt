package com.syncodec.graphite.presentation.custom.topBarColumn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.custom.button.MenuButton
import kotlin.math.min


@Composable
fun TopBarColumn(
	title: String,
	pinnedHeight: Dp = 194.dp,
	content: @Composable () -> Unit
) {
	val density = LocalDensity.current
	val scrollState = rememberScrollState()
	val pinnedHeightPx = with(density) { (pinnedHeight.toPx()) }
	val percentScroll = min(pinnedHeightPx, scrollState.value.toFloat()) / pinnedHeightPx
	val titleFontSize = (45 - (23 * percentScroll)).sp
	val titleRowSpace = (16 + (48 * percentScroll)).dp
	val isScrolled = scrollState.value > pinnedHeightPx

	Box(
		modifier = Modifier.fillMaxSize()
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(scrollState)
		) {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier.fillMaxWidth()
			) {
				Spacer(
					modifier = Modifier
						.fillMaxWidth()
						.height(pinnedHeight)
				)
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier
						.fillMaxWidth()
						.height(64.dp)
				) {
					Spacer(modifier = Modifier.width(titleRowSpace))
					Text(
						text = title,
						style = MaterialTheme.typography.titleLarge,
						fontSize = titleFontSize
					)
				}
				Spacer(modifier = Modifier.height((8 * (1 - percentScroll)).dp))
				Spacer(
					modifier = Modifier
						.fillMaxWidth(percentScroll)
						.height(1.dp)
						.background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.13f))
				)
			}

			content()
		}

		if (isScrolled) {
			Column(
				modifier = Modifier.fillMaxWidth()
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier
						.fillMaxWidth()
						.height(64.dp)
						.background(MaterialTheme.colorScheme.background)
				) {
					Spacer(modifier = Modifier.width(4.dp))
					MenuButton(
						icon = R.drawable.ic_back
					) {}
					Spacer(modifier = Modifier.width(16.dp))
					Text(
						text = "Settings",
						style = MaterialTheme.typography.titleLarge,
						fontSize = 22.sp
					)
				}
				Spacer(
					modifier = Modifier
						.fillMaxWidth()
						.height(1.dp)
						.background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f))
				)
			}
		}
	}
}

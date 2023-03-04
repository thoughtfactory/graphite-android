package com.syncodec.graphite.presentation.notebook.screen.buildingBlock

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.utils.getInverseBWColor
import io.github.esentsov.PackagePrivate


@OptIn(ExperimentalAnimationApi::class)
@PackagePrivate
@Preview
@Composable
fun Header(
	title : String = "Chapters",
	subTitle : String = "71 chapters",
	minHeight : Dp = Dp.Hairline,
	backgroundColor : Color = MaterialTheme.colorScheme.background,
	enabled : Boolean = true,
	onClick : () -> Unit = {},
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(backgroundColor)
	) {
		Box(
			contentAlignment = Alignment.CenterStart,
			modifier = Modifier
				.fillMaxWidth()
				.heightIn(min = minHeight)
				.padding(12.dp, 0.dp)
				.clip(MaterialTheme.shapes.medium)
				.clickable(enabled = enabled) { onClick() }
		) {
			Row(
				verticalAlignment = Alignment.Bottom,
				modifier = Modifier
					.fillMaxWidth()
					.padding(8.dp, 4.dp)
			) {
				AnimatedText(
					text = title,
					style = MaterialTheme.typography.bodyLarge,
					color = backgroundColor.getInverseBWColor(),
					fontWeight = FontWeight.Bold,
					transitionSpec = { scaleIn(tween(300), 0.71f) + fadeIn(tween(300)) with scaleOut(tween(300), 0.71f) + fadeOut(tween(300)) },
				)

				Spacer(modifier = Modifier.weight(1f))

				AnimatedText(
					text = subTitle,
					style = MaterialTheme.typography.bodyMedium,
					color = backgroundColor.getInverseBWColor(),
					fontWeight = FontWeight.Bold,
					transitionSpec = { scaleIn(tween(300), 0.71f) + fadeIn(tween(300)) with scaleOut(tween(300), 0.71f) + fadeOut(tween(300)) },
				)
			}
		}
	}
}

package com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NotebookHeaderCard(
	title : String,
	noEntries : String,
	color : Color,
	onClick : (() -> Unit)? = null
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.background(color = color)
			.clickable(enabled = onClick != null) { onClick?.invoke() }
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.fillMaxWidth()
		) {
			Spacer(modifier = Modifier.width(14.dp))
			Box(
				modifier = Modifier
					.width(4.dp)
					.height(40.dp)
					.clip(RoundedCornerShape(4.dp))
					.background(MaterialTheme.colorScheme.primary)
			)

			Spacer(modifier = Modifier.width(12.dp))

			AnimatedContent(
				targetState = title,
				transitionSpec = { fadeIn(tween(300)) + scaleIn(tween(300)) with fadeOut(tween()) + scaleOut(tween(300)) }
			) {
				Text(
					text = it,
					color = MaterialTheme.colorScheme.onBackground,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold
				)
			}
			Spacer(modifier = Modifier.weight(1f))

			AnimatedContent(
				targetState = noEntries,
				transitionSpec = { fadeIn(tween(300)) + scaleIn(tween(300)) with fadeOut(tween()) + scaleOut(tween(300)) }
			) {
				Text(
					text = it,
					color = MaterialTheme.colorScheme.onBackground,
					style = MaterialTheme.typography.titleSmall,
				)
			}

			Spacer(modifier = Modifier.width(12.dp))
		}
	}
}

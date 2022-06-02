package com.syncodec.graphite.custom.notebook

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
	title: String,
	noEntries: String,
	color: Color,
	onClick: (() -> Unit)? = null
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

			Text(
				text = title,
				color = MaterialTheme.colorScheme.primary,
				style = MaterialTheme.typography.bodyLarge,
				fontWeight = FontWeight.Bold
			)

			Spacer(modifier = Modifier.weight(1f))

			AnimatedContent(targetState = noEntries) {
				Text(
					text = it,
					color = MaterialTheme.colorScheme.onBackground,
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.Bold
				)
			}

			Spacer(modifier = Modifier.width(12.dp))
		}
	}
}

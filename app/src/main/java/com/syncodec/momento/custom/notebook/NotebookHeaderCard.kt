package com.syncodec.momento.custom.notebook

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NotebookHeaderCard(
	title: String,
	noEntries: String,
	onClick: (() -> Unit)? = null
) {
	Surface(
		onClick = {onClick?.invoke()},
		enabled = onClick!=null,
		color = MaterialTheme.colorScheme.surface,
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface)
	) {
		Row(
			verticalAlignment = Alignment.Bottom,
			modifier = Modifier
				.fillMaxWidth()
				.padding(6.dp, 0.dp)
		) {
			Spacer(modifier = Modifier.width(8.dp))
			Box(
				modifier = Modifier
					.width(4.dp)
					.height(40.dp)
					.clip(RoundedCornerShape(4.dp))
					.background(MaterialTheme.colorScheme.primary)
			)

			Spacer(modifier = Modifier.width(8.dp))

			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(0.dp, 8.dp),
				verticalAlignment = Alignment.Bottom
			) {
				Text(
					text = title,
					color = MaterialTheme.colorScheme.primary,
					style = MaterialTheme.typography.bodyLarge
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
			}

			Spacer(modifier = Modifier.width(8.dp))
		}
	}
}

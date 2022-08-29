package com.syncodec.graphite.presentation.bucketItem.composable.buildingBlock

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun MovieHeaderCard(
	title: String?,
	releaseDate: String?,
	runtime: Int? = null,
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.border(1.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
	) {
		Column(
			modifier = Modifier.padding(16.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				if (title == null) {
					Text(
						text = "Untitled",
						style = MaterialTheme.typography.bodyLarge,
						fontStyle = FontStyle.Italic
					)
				} else {
					Text(
						text = title,
						style = MaterialTheme.typography.bodyLarge,
						fontWeight = FontWeight.Bold,
					)
				}

				Spacer(modifier = Modifier.width(8.dp))
				Spacer(modifier = Modifier.weight(1f))
				Spacer(modifier = Modifier.width(8.dp))
			}

			Spacer(modifier = Modifier.height(8.dp))

			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				AnimatedVisibility(
					visible = runtime != null,
					enter = fadeIn(tween(600)),
					exit = fadeOut(tween(600))
				) {
					Text(
						text = "~ $runtime min",
						style = MaterialTheme.typography.bodyMedium,
					)
				}
				Spacer(modifier = Modifier.weight(1f))
				AnimatedVisibility(
					visible = releaseDate != null,
					enter = fadeIn(tween(600)),
					exit = fadeOut(tween(600))
				) {
					Text(
						text = releaseDate ?: "",
						style = MaterialTheme.typography.bodyMedium,
					)
				}
			}
		}
	}
}

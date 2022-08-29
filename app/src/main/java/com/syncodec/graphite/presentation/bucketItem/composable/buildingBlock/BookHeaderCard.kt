package com.syncodec.graphite.presentation.bucketItem.composable.buildingBlock

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer


@Composable
fun BookHeaderCard(
	title: String?,
	releaseDate: Int?,
	author: List<String?>?
) {
	SelectionContainer(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.border(1.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
	) {
		Column(
			modifier = Modifier.padding(16.dp)
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

			Spacer(modifier = Modifier.height(4.dp))

			Row(
				modifier = Modifier
					.fillMaxWidth()
			) {
				if (author != null) {
					author.firstOrNull()?.let {
						Text(
							text = it,
							style = MaterialTheme.typography.bodyMedium,
							fontWeight = FontWeight.Bold,
						)
					}
				}

				Spacer(modifier = Modifier.weight(1f))
				if (releaseDate != null) {
					Text(
						text = "$releaseDate",
						style = MaterialTheme.typography.bodyMedium,
						fontWeight = FontWeight.Bold,
					)
				}
			}
		}
	}
}

@Composable
fun BookHeaderShimmerCard() {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(12.dp))
			.border(1.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
	) {
		Column(
			modifier = Modifier.padding(16.dp)
		) {
			Text(
				text = "Untitled",
				style = MaterialTheme.typography.bodyLarge,
				fontStyle = FontStyle.Italic,
				modifier = Modifier.shimmer()
			)

			Spacer(modifier = Modifier.height(4.dp))

			Row(
				modifier = Modifier
					.fillMaxWidth()
			) {
				Text(
					text = "Author",
					color = MaterialTheme.colorScheme.primary,
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.Bold,
					modifier = Modifier.shimmer()
				)

				Spacer(modifier = Modifier.weight(1f))
				Text(
					text = "Release Date",
					color = MaterialTheme.colorScheme.primary,
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.Bold,
					modifier = Modifier.shimmer()
				) }
		}
	}
}

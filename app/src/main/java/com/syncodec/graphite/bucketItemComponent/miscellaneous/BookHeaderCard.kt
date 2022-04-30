package com.syncodec.graphite.bucketItemComponent.miscellaneous

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BookHeaderCard(
	title: String,
	releaseDate: String?,
	author: String? = null,
) {
	Card(
		modifier = Modifier
			.fillMaxWidth(),
		elevation = 0.dp,
		shape = RoundedCornerShape(12.dp),
		border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondaryContainer),
		backgroundColor = Color.Transparent,
	) {
		Column(
			modifier = Modifier
				.padding(16.dp)
		) {
			Text(
				text = title,
				color = MaterialTheme.colorScheme.onBackground,
				style = MaterialTheme.typography.bodyLarge,
				fontWeight = FontWeight.Bold,
				modifier = Modifier,
			)

			Spacer(modifier = Modifier.height(4.dp))

			Row(
				modifier = Modifier
					.fillMaxWidth()
			) {
				if (author != null) {
					Text(
						text = author,
						color = MaterialTheme.colorScheme.primary,
						style = MaterialTheme.typography.bodyMedium,
						fontWeight = FontWeight.Bold,
						modifier = Modifier
					)
				}

				Spacer(modifier = Modifier.weight(1f))
				if (releaseDate != null) {
					Text(
						text = releaseDate,
						color = MaterialTheme.colorScheme.primary,
						style = MaterialTheme.typography.bodyMedium,
						fontWeight = FontWeight.Bold,
						modifier = Modifier
					)
				}
			}
		}
	}
}

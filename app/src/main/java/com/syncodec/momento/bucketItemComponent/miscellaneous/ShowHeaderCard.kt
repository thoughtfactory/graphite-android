package com.syncodec.momento.bucketItemComponent.miscellaneous

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
fun ShowHeaderCard(
	title: String,
	inProduction: Boolean?,
	releaseDate: String?,
	showLength: Int? = null,
	noSeason: Int?,
	noEpisode: Int?
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
			Row(
				modifier = Modifier
					.fillMaxWidth()
			) {
				Text(
					text = title,
					color = MaterialTheme.colorScheme.onBackground,
					style = MaterialTheme.typography.bodyLarge,
					fontWeight = FontWeight.Bold,
					modifier = Modifier,
				)

				Spacer(modifier = Modifier.width(8.dp))
				Spacer(modifier = Modifier.weight(1f))
				Spacer(modifier = Modifier.width(8.dp))

				if (inProduction != null) {
					Text(
						text = if (inProduction) "In Production" else "Ended",
						color = MaterialTheme.colorScheme.primary,
						style = MaterialTheme.typography.bodyMedium,
						modifier = Modifier
					)
				}
			}


			Spacer(modifier = Modifier.height(8.dp))

			Row(
				modifier = Modifier
					.fillMaxWidth()
			) {
				if (showLength != null) {
					Text(
						text = "~ $showLength min / ep",
						color = MaterialTheme.colorScheme.primary,
						style = MaterialTheme.typography.bodyMedium,
						modifier = Modifier
					)
				}

				if (showLength != null && (noSeason != null || noEpisode != null)) {
					Text(
						text = " | ",
						color = MaterialTheme.colorScheme.primary,
						style = MaterialTheme.typography.bodyMedium,
						modifier = Modifier
					)
				}

				if (noSeason != null) {
					Text(
						text = "$noSeason seas",
						color = MaterialTheme.colorScheme.primary,
						style = MaterialTheme.typography.bodyMedium,
						modifier = Modifier
					)
				}

				if (noSeason != null && noEpisode != null) {
					Text(
						text = " | ",
						color = MaterialTheme.colorScheme.primary,
						style = MaterialTheme.typography.bodyMedium,
						modifier = Modifier
					)
				}

				if (noEpisode != null) {
					Text(
						text = "$noEpisode epis",
						color = MaterialTheme.colorScheme.primary,
						style = MaterialTheme.typography.bodyMedium,
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

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
import com.syncodec.momento.bucketComponent.modalBottomSheet.BookData

data class HeaderData(
	val title: String,
	val releaseDate: String?,
	val director: String?
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HeaderCard(
	headerData: HeaderData
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
				text = headerData.title,
				color = MaterialTheme.colorScheme.onBackground,
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Bold,
				modifier = Modifier,
			)

			Spacer(modifier = Modifier.height(4.dp))

			Row(
				modifier = Modifier
					.fillMaxWidth()
			) {
				if (headerData.director != null) {
					Text(
						text = headerData.director,
						color = MaterialTheme.colorScheme.primary,
						style = MaterialTheme.typography.bodyMedium,
						fontWeight = FontWeight.Bold,
						modifier = Modifier
					)
				}
				Spacer(modifier = Modifier.weight(1f))
				if (headerData.releaseDate != null) {
					Text(
						text = headerData.releaseDate,
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

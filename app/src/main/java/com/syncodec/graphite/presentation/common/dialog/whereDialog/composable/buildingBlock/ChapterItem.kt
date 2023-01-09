package com.syncodec.graphite.presentation.common.dialog.whereDialog.composable.buildingBlock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.presentation.ui.LockClosedContainer


@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterItem(
	@PreviewParameter(provider = ChapterObjectLitePreviewParameter::class) chapterObject : ChapterObjectLite,
	onClick : () -> Unit = {},
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(12.dp, 4.dp)
	) {
		OutlinedCard(
			shape = RoundedCornerShape(12.dp),
			border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.17f)),
			colors = CardDefaults.cardColors(containerColor = Color.Transparent),
			elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp),
			onClick = onClick,
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(12.dp))
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp)
			) {
				Row(
					modifier = Modifier.fillMaxWidth()
				) {
					Text(
						text = chapterObject.title ?: "Untitled",
						style = MaterialTheme.typography.bodyLarge,
						fontWeight = FontWeight.Bold,
						fontStyle = if (chapterObject.title.isNullOrBlank()) FontStyle.Italic else FontStyle.Normal,
						color = MaterialTheme.colorScheme.onBackground
					)

					Spacer(modifier = Modifier.weight(1f))

					if (chapterObject.isLocked) {
						Icon(
							painter = painterResource(id = R.drawable.ic_shield),
							contentDescription = "Locked",
							tint = Color.LockClosedContainer,
							modifier = Modifier.requiredSize(14.dp)
						)
						if (chapterObject.isFavourite) {
							Spacer(modifier = Modifier.width(2.dp))
							Text(
								text = "·",
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onBackground,
								fontWeight = FontWeight.Bold,
								maxLines = 1,
								modifier = Modifier
							)
							Spacer(modifier = Modifier.width(2.dp))
							Icon(
								painter = painterResource(id = R.drawable.ic_favourite),
								contentDescription = "Favourite",
								tint = Color.FavouriteContainer,
								modifier = Modifier.requiredSize(14.dp)
							)
						}
					}
				}
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = chapterObject.description.let { if (it.isNullOrBlank()) "No description" else it },
					style = MaterialTheme.typography.bodyMedium,
					fontStyle = if (chapterObject.description.isNullOrBlank()) FontStyle.Italic else FontStyle.Normal,
					color = MaterialTheme.colorScheme.onBackground
				)
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = chapterObject.id.toString(),
					style = MaterialTheme.typography.bodySmall,
					fontStyle = FontStyle.Italic,
					color = MaterialTheme.colorScheme.onBackground,
					overflow = TextOverflow.Ellipsis,
					maxLines = 1,
				)
			}
		}
	}
}

private class ChapterObjectLitePreviewParameter : PreviewParameterProvider<ChapterObjectLite> {
	override val values = sequenceOf(
		ChapterObjectLite.getRandomInstance(),
		ChapterObjectLite.getRandomInstance(),
		ChapterObjectLite.getRandomInstance(),
		ChapterObjectLite.getRandomInstance(),
	)
}

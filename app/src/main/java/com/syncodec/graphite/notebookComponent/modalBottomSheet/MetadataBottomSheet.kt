package com.syncodec.graphite.notebookComponent.modalBottomSheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.custom.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.custom.bottomSheet.BottomSheetKeyCard
import com.syncodec.graphite.custom.bottomSheet.BottomSheetStrip


@Composable
fun MetadataBottomSheet(
	key: String?,
	chapterSize: Int,
	noteSize: Int,
) {
	Surface(
		shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
		color = MaterialTheme.colorScheme.surface,
		modifier = Modifier.heightIn(120.dp),
	) {
		Column(
			modifier = Modifier,
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			BottomSheetStrip()

			BottomSheetHeader(title = "Metadata", icon = R.drawable.ic_info)

			BottomSheetKeyCard(
				key = key,
				createdTimestamp = 0,
				modifiedTimestamp = 0
			)

			Spacer(modifier = Modifier.height(8.dp))

			DataCard(
				chapterSize = chapterSize,
				noteSize =  noteSize
			)

			Spacer(modifier = Modifier.height(24.dp))
		}
	}
}

@Composable
fun DataCard(
	chapterSize: Int,
	noteSize: Int,
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp),
	) {
		Row(
			Modifier
				.fillMaxWidth()
				.height(48.dp)
		) {
			DataButton(
				modifier = Modifier
					.fillMaxHeight()
					.weight(1f)
					.clip(RoundedCornerShape(12.dp))
					.background(MaterialTheme.colorScheme.background),
				icon = R.drawable.ic_book,
				title = "Chapter",
				count = chapterSize
			)

			Spacer(modifier = Modifier.width(8.dp))

			DataButton(
				modifier = Modifier
					.fillMaxHeight()
					.weight(1f)
					.clip(RoundedCornerShape(12.dp))
					.background(MaterialTheme.colorScheme.background),
				icon = R.drawable.ic_note,
				title = "Note",
				count = noteSize
			)
		}
	}
}

@Composable
private fun DataButton(
	modifier: Modifier,
	icon: Int,
	title: String,
	count: Int
) {
	Row(
		modifier = modifier,
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.Center
	) {
		Icon(
			painter = painterResource(id = icon),
			contentDescription = title,
			tint = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier.requiredSize(20.dp)
		)
		Spacer(modifier = Modifier.width(8.dp))
		Text(
			text = title,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground
		)
		Spacer(modifier = Modifier.width(8.dp))
		Text(
			text = "$count",
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onBackground
		)
	}
}

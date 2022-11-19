package com.syncodec.graphite.presentation.common.info

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.utils.getInverseBWColor
import com.syncodec.graphite.utils.timeStampToPrettyFull
import io.realm.kotlin.types.RealmUUID


@Composable
fun InfoView(
	id : RealmUUID?,
	createdTimestamp : Long?,
	modifiedTimestamp : Long?,
	description : String?,
	color : Color = MaterialTheme.colorScheme.background.copy(alpha = 0.71f),
	thumbnail : Bitmap?,
) {

	var size by remember { mutableStateOf<IntSize?>(null) }

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp)
			.background(color, RoundedCornerShape(16.dp))
			.clip(RoundedCornerShape(16.dp))
			.onGloballyPositioned { size = it.size }
	) {
		thumbnail?.let {
			Image(
				bitmap = it.asImageBitmap(),
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = with(LocalDensity.current) {
					Modifier.size(size?.width?.toDp() ?: 1.dp, size?.height?.toDp() ?: 1.dp)
				}
			)
		}

		if (thumbnail != null) {
			Box(
				modifier = with(LocalDensity.current) {
					Modifier
						.size(size?.width?.toDp() ?: 1.dp, size?.height?.toDp() ?: 1.dp)
						.background(Color.Black.copy(alpha = 0.31f))
				}
			)
		}

		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp)
		) {
			Text(
				text = id?.toString() ?: "Loading...",
				style = MaterialTheme.typography.bodyMedium,
				color = color.getInverseBWColor(),
			)
			Spacer(modifier = Modifier.height(8.dp))

			Text(
				text = if (description.isNullOrBlank()) "No description" else description,
				style = if (description.isNullOrBlank()) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium,
				color = color.getInverseBWColor(),
				fontWeight = if (description.isNullOrBlank()) FontWeight.Normal else FontWeight.Bold,
				fontStyle = if (description.isNullOrBlank()) FontStyle.Italic else FontStyle.Normal
			)
			Spacer(modifier = Modifier.height(8.dp))

			Text(
				text = "Created on: ${createdTimestamp?.timeStampToPrettyFull() ?: "Loading..."}",
				style = MaterialTheme.typography.bodyMedium,
				color = color.getInverseBWColor(),
				fontStyle = FontStyle.Italic
			)
			Spacer(modifier = Modifier.height(2.dp))

			if (modifiedTimestamp != null) {
				Text(
					text = "Modified on: ${modifiedTimestamp.timeStampToPrettyFull()}",
					style = MaterialTheme.typography.bodyMedium,
					color = color.getInverseBWColor(),
					fontStyle = FontStyle.Italic
				)
			}
		}
	}
}

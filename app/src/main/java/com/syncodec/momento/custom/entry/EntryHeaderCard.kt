package com.syncodec.momento.custom.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun EntryHeaderCard(
	title: String,
	noEntries: String,
	onClick: (() -> Unit)? = null
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(8.dp, 0.dp, 6.dp, 0.dp)
			.clip(RoundedCornerShape(12.dp))
			.background(MaterialTheme.colorScheme.background)
			.clickable(enabled = onClick != null) { onClick?.invoke() },
	) {
		Row(
			verticalAlignment = Alignment.Bottom,
			modifier = Modifier
				.fillMaxWidth()
				.padding(6.dp, 0.dp)
		) {
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

				Text(
					text = noEntries,
					color = MaterialTheme.colorScheme.onSurface,
					style = MaterialTheme.typography.bodyMedium,
				)
			}
		}
	}
}

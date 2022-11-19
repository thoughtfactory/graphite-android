package com.syncodec.graphite.presentation.common.bottomSheet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.utils.timeStampToPrettyFull
import io.realm.kotlin.types.RealmUUID
import java.util.*
import java.util.concurrent.TimeUnit


@Composable
fun BottomSheetKeyCard(
	id: RealmUUID?,
	createdTimestamp: Long,
	modifiedTimestamp: Long
) {
	Card(
		shape = RoundedCornerShape(12.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.background,
			contentColor = MaterialTheme.colorScheme.onBackground
		),
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp)
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
					text = if (id == null) "Unsaved" else "$id",
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.Bold,
					fontStyle = if (id == null) FontStyle.Italic else FontStyle.Normal,
					color = MaterialTheme.colorScheme.onBackground
				)
			}

			Spacer(modifier = Modifier.height(8.dp))
			Row(
				modifier = Modifier.fillMaxWidth()
			) {
				Text(
					text = "Created on : ",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground
				)
				Spacer(modifier = Modifier.weight(1f))
				Text(
					text = createdTimestamp.timeStampToPrettyFull(),
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onBackground
				)
			}

			Spacer(modifier = Modifier.height(8.dp))

			Row(
				modifier = Modifier.fillMaxWidth()
			) {
				Text(
					text = "Last edited on : ",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground
				)
				Spacer(modifier = Modifier.weight(1f))

				val calendar = Calendar.getInstance()
				val days = TimeUnit.MILLISECONDS.toDays(calendar.timeInMillis - modifiedTimestamp)
				val hours = TimeUnit.MILLISECONDS.toHours(calendar.timeInMillis - modifiedTimestamp)
				val minutes =
					TimeUnit.MILLISECONDS.toMinutes(calendar.timeInMillis - modifiedTimestamp)

				val modifiedTimestampPretty = if (days in 1..7) {
					"About $days days ago"
				} else if (days > 7) {
					modifiedTimestamp.timeStampToPrettyFull()
				} else {
					if (hours in 1..24) {
						"About $hours hour${if (hours == 1L) "" else "s"} ago"
					} else {
						if (minutes in 1..60) {
							"About $minutes minute${if (minutes == 1L) "" else "s"} ago"
						} else {
							"About few seconds ago"
						}
					}
				}

				Text(
					text = modifiedTimestampPretty,
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onBackground
				)
			}
		}
	}
}

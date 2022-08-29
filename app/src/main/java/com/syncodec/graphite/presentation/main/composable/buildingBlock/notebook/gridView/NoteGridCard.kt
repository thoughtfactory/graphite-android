package com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.gridView

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.utils.roundTo
import io.realm.kotlin.types.ObjectId


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteGridCard(
	id: ObjectId,
	timestamp: Long,
	showFullTime: Boolean,
	isLocked: Boolean,
	isSelected: Boolean,
	isFavourite: Boolean,
	isDeleted: Boolean,
	isLast: Boolean,
	title: String?,
	contentThumbnail: String?,
	attachmentCount: Int,
	attachmentThumbnail: Bitmap?,
	address: String?,
	latLng: LatLng?,
	isVisible: Boolean = false,
	selectedColor: Color,
	onClick: () -> Unit,
	onLongClick: (() -> Unit)? = null,
) {

	val containerColor by animateColorAsState(
		when {
			isSelected -> selectedColor
			isFavourite -> Color(0x13FE3A58)
			else -> Color.Transparent
		}
	)

	OutlinedCard(
		shape = RoundedCornerShape(12.dp),
		border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.17f)),
		colors = CardDefaults.cardColors(containerColor),
		elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp),
		modifier = Modifier
			.fillMaxWidth()
			.padding(4.dp)
			.clip(RoundedCornerShape(12.dp))
			.combinedClickable(
				onClick = { onClick() },
				onLongClick = { onLongClick?.invoke() }
			)
	) {
		Column(
			modifier = Modifier.padding(12.dp, 8.dp)
		) {
			if (title != null) {
				Text(
					text = title,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground,
					fontWeight = FontWeight.Bold,
					maxLines = 1,
					modifier = Modifier
				)
				Spacer(modifier = Modifier.height(4.dp))
			}
			if (contentThumbnail != null) {
				Text(
					text = contentThumbnail,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface,
					maxLines = 6,
					modifier = Modifier
				)
				Spacer(modifier = Modifier.height(4.dp))
			}
			if (address != null || latLng != null) {
				Spacer(modifier = Modifier.height(4.dp))
				Location(
					address = address,
					latLng = latLng
				)
			}
		}
	}
}

@Composable
private fun Location(
	address: String?,
	latLng: LatLng?
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			painter = painterResource(id = R.drawable.ic_map_marker),
			contentDescription = null,
			tint = Color(0xFF318DFD),
			modifier = Modifier.requiredSize(14.dp)
		)

		Spacer(modifier = Modifier.width(4.dp))

		if (!address.isNullOrBlank()) {
			Text(
				text = address,
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
				fontStyle = FontStyle.Italic,
				overflow = TextOverflow.Ellipsis,
				maxLines = 1
			)
		} else if (latLng != null) {
			Text(
				text = "${latLng.latitude?.roundTo(6)}, ${latLng.longitude?.roundTo(6)}",
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
				fontStyle = FontStyle.Italic,
				overflow = TextOverflow.Ellipsis,
				maxLines = 1
			)
		}
	}
}

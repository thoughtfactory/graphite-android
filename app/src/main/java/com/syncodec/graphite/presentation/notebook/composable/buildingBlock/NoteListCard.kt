package com.syncodec.graphite.presentation.notebook.composable.buildingBlock

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.TagObjectLite
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.presentation.ui.FavouriteContent
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.addEmptyLines
import com.syncodec.graphite.utils.entryTimestamp0
import com.syncodec.graphite.utils.entryTimestamp1
import com.syncodec.graphite.utils.getInverseBWColor
import com.syncodec.graphite.utils.timeStampToTime
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun NoteListCard(
	id: RealmUUID,
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
	tagList: List<TagObjectLite>,
	isVisible: Boolean = false,
	selectedColor: Color,
	onClick: () -> Unit,
	onLongClick: (() -> Unit)? = null
) {
	val context = LocalContext.current
	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val isFavouriteTinted by dataStoreInstance.getTintFavorite.collectAsState(initial = false)

	val containerColor by animateColorAsState(
		when {
			isSelected -> selectedColor
			isFavourite -> if (isFavouriteTinted) Color(
				ColorUtils.blendARGB(
					MaterialTheme.colorScheme.background.toArgb(),
					Color.FavouriteContainer.toArgb(),
					0.31f
				)
			) else MaterialTheme.colorScheme.background

			else -> MaterialTheme.colorScheme.background
		}
	)
	val contentColor by animateColorAsState(
		when {
			isSelected -> selectedColor.getInverseBWColor()
			isFavourite -> if (isFavouriteTinted) Color.FavouriteContent else MaterialTheme.colorScheme.onBackground
			else -> MaterialTheme.colorScheme.onSurface
		}
	)

	if (isVisible) {
		OutlinedCard(
			border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.17f)),
			colors = CardDefaults.outlinedCardColors(
				containerColor = containerColor,
				contentColor = contentColor,
			),
			modifier = Modifier
				.padding(12.dp, 4.dp)
				.combinedClickable(
					onClick = { onClick() },
					onLongClick = { onLongClick?.invoke() }
				)
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(12.dp)
			) {
				Row(
					modifier = Modifier.fillMaxWidth()
				) {
					Title(
						showFullTime = true,
						timestamp = timestamp,
						title = title,
						isLocked = isLocked,
						isFavourite = isFavourite,
						attachmentCount = attachmentCount
					)
				}

				Spacer(modifier = Modifier.height(4.dp))

				Content(
					contentThumbnail = contentThumbnail,
					attachmentCount = attachmentCount,
					attachmentThumbnail = attachmentThumbnail
				)
			}
		}
	}
}


@Composable
private fun Title(
	showFullTime: Boolean,
	timestamp: Long,
	title: String?,
	isLocked: Boolean,
	isFavourite: Boolean,
	attachmentCount: Int
) {
	Row(
		modifier = Modifier,
		verticalAlignment = Alignment.CenterVertically,
	) {
		if (showFullTime) {
			TitleText(text = entryTimestamp0(timestamp))
			TitleText(text = entryTimestamp1(timestamp))
		} else {
			TitleText(text = timeStampToTime(timestamp))
		}
		if (!title.isNullOrBlank()) {
			Spacer(modifier = Modifier.width(2.dp))
			TitleText(text = "·")
			Spacer(modifier = Modifier.width(2.dp))
			TitleText(text = title)
		}

		Spacer(modifier = Modifier.weight(1f))

		if (isLocked) {
			Icon(
				painter = painterResource(id = R.drawable.ic_shield),
				contentDescription = "Locked",
				tint = Color(0xFF5ACE8F),
				modifier = Modifier.requiredSize(14.dp)
			)
		}
		if (isFavourite) {
			Icon(
				painter = painterResource(id = R.drawable.ic_favourite),
				contentDescription = "Favourite",
				tint = Color(0xFFFF5E78),
				modifier = Modifier.requiredSize(14.dp)
			)
		}
		if (isFavourite && attachmentCount != 0) {
			Spacer(modifier = Modifier.width(2.dp))
			TitleText(text = "·")
			Spacer(modifier = Modifier.width(2.dp))
		}
		if (attachmentCount != 0) {
			Icon(
				painter = painterResource(id = R.drawable.ic_attachment),
				contentDescription = "Attachment count",
				tint = Color(0xFFF5B971),
				modifier = Modifier.requiredSize(14.dp)
			)
			Spacer(modifier = Modifier.width(2.dp))
			TitleText(text = "·")
			Spacer(modifier = Modifier.width(2.dp))
			TitleText(text = "$attachmentCount")
		}
	}
}

@Composable
private fun TitleText(text: String) {
	Text(
		text = text,
		style = MaterialTheme.typography.bodyMedium,
		color = MaterialTheme.colorScheme.onBackground,
		fontWeight = FontWeight.Bold,
		maxLines = 1,
		modifier = Modifier
	)
}

@Composable
private fun Content(
	contentThumbnail: String?,
	attachmentCount: Int,
	attachmentThumbnail: Bitmap?
) {
	val context = LocalContext.current

	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		if (attachmentCount == 0 || attachmentThumbnail == null) {
			Text(
				text = (contentThumbnail ?: "").addEmptyLines(6),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onBackground,
				maxLines = 6,
				modifier = Modifier
			)
		} else {
			Text(
				text = (contentThumbnail ?: "").addEmptyLines(6),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface,
				maxLines = 6,
				modifier = Modifier.weight(1f)
			)

			Spacer(modifier = Modifier.width(8.dp))

			AsyncImage(
				model = ImageRequest.Builder(context)
					.data(attachmentThumbnail)
					.crossfade(300)
					.build(),
				placeholder = null,
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = Modifier
					.requiredSize(80.dp)
					.clip(RoundedCornerShape(12.dp)),
			)
		}
	}
}

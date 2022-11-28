package com.syncodec.graphite.presentation.notebook.composable.buildingBlock

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.ui.AttachmentContainer
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.presentation.ui.FavouriteContent
import com.syncodec.graphite.presentation.ui.LocationContainer
import com.syncodec.graphite.presentation.ui.LockClosedContainer
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.addEmptyLines
import com.syncodec.graphite.utils.entryTimestamp0
import com.syncodec.graphite.utils.entryTimestamp1
import com.syncodec.graphite.utils.getAttachmentCountFromNoteId
import com.syncodec.graphite.utils.getInverseBWColor
import com.syncodec.graphite.utils.roundTo
import com.syncodec.graphite.utils.timeStampToTime
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteListCard(
	id : RealmUUID,
	timestamp : Long,
	isLocked : Boolean,
	isSelected : Boolean,
	isFavourite : Boolean,
	isLast : Boolean,
	title : String?,
	contentThumbnail : String?,
	attachmentThumbnail : Bitmap?,
	address : String?,
	latLng : LatLng?,
	tagList : List<TagObject>,
	isVisible : Boolean = false,
	selectedColor : Color,
	onClick : () -> Unit,
	onLongClick : (() -> Unit)? = null
) {
	val context = LocalContext.current
	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val isFavouriteTinted by dataStoreInstance.getTintFavorite.collectAsState(initial = false)

	val containerColor by animateColorAsState(
		when {
			isSelected -> selectedColor
			isFavourite -> if (isFavouriteTinted) Color(
				ColorUtils.blendARGB(MaterialTheme.colorScheme.background.toArgb(), Color.FavouriteContainer.toArgb(), 0.31f)
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
			shape = RoundedCornerShape(12.dp),
			modifier = Modifier
				.padding(12.dp, 4.dp)
				.clip(RoundedCornerShape(12.dp))
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
						attachmentCount = context.getAttachmentCountFromNoteId(id),
						contentColor = contentColor,
						isFavouriteTinted = isFavouriteTinted
					)
				}

				Spacer(modifier = Modifier.height(4.dp))

				Content(
					contentThumbnail = contentThumbnail,
					attachmentCount = context.getAttachmentCountFromNoteId(id),
					attachmentThumbnail = attachmentThumbnail,
					contentColor = contentColor
				)

				TagView(tagList = tagList)

				if (address != null || latLng != null) {
					Spacer(modifier = Modifier.height(8.dp))
					Location(
						address = address,
						latLng = latLng,
						contentColor = contentColor,
						isFavouriteTinted = isFavouriteTinted,
						isFavourite = isFavourite
					)
				}

			}
		}
	}
}


@Composable
private fun Title(
	showFullTime : Boolean,
	timestamp : Long,
	title : String?,
	isLocked : Boolean,
	isFavourite : Boolean,
	attachmentCount : Int,
	contentColor : Color,
	isFavouriteTinted : Boolean
) {
	Row(
		modifier = Modifier,
		verticalAlignment = Alignment.CenterVertically,
	) {
		if (showFullTime) {
			TitleText(text = entryTimestamp0(timestamp), contentColor = contentColor)
			TitleText(text = entryTimestamp1(timestamp), contentColor = contentColor)
		} else {
			TitleText(text = timeStampToTime(timestamp), contentColor = contentColor)
		}
		if (! title.isNullOrBlank()) {
			Spacer(modifier = Modifier.width(2.dp))
			TitleText(text = "·", contentColor = contentColor)
			Spacer(modifier = Modifier.width(2.dp))
			TitleText(text = title, contentColor = contentColor)
		}

		Spacer(modifier = Modifier.weight(1f))

		if (isLocked) {
			Icon(
				painter = painterResource(id = R.drawable.ic_shield),
				contentDescription = "Locked",
				tint = if (isFavouriteTinted && isFavourite) contentColor else Color.LockClosedContainer,
				modifier = Modifier.requiredSize(14.dp)
			)
		}
		if (isFavourite) {
			Icon(
				painter = painterResource(id = R.drawable.ic_favourite),
				contentDescription = "Favourite",
				tint = if (isFavouriteTinted) contentColor else Color.FavouriteContainer,
				modifier = Modifier.requiredSize(14.dp)
			)
		}
		if (isFavourite && attachmentCount != 0) {
			Spacer(modifier = Modifier.width(2.dp))
			TitleText(text = "·", contentColor = contentColor)
			Spacer(modifier = Modifier.width(2.dp))
		}
		if (attachmentCount != 0) {
			Icon(
				painter = painterResource(id = R.drawable.ic_attachment),
				contentDescription = "Attachment count",
				tint = if (isFavouriteTinted && isFavourite) contentColor else Color.AttachmentContainer,
				modifier = Modifier.requiredSize(14.dp)
			)
			Spacer(modifier = Modifier.width(2.dp))
			TitleText(text = "·", contentColor = contentColor)
			Spacer(modifier = Modifier.width(2.dp))
			TitleText(text = "$attachmentCount", contentColor = contentColor)
		}
	}
}

@Composable
private fun TitleText(
	text : String,
	contentColor : Color
) {
	Text(
		text = text,
		style = MaterialTheme.typography.bodyMedium,
		color = contentColor,
		fontWeight = FontWeight.Bold,
		maxLines = 1,
		modifier = Modifier
	)
}

@Composable
private fun Content(
	contentThumbnail : String?,
	attachmentCount : Int,
	attachmentThumbnail : Bitmap?,
	contentColor : Color
) {
	val context = LocalContext.current

	Row(
		modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
	) {
		if (attachmentCount == 0 || attachmentThumbnail == null) {
			Text(
				text = (contentThumbnail ?: "").addEmptyLines(6),
				style = MaterialTheme.typography.bodySmall,
				color = contentColor,
				maxLines = 6,
				modifier = Modifier
			)
		} else {
			Text(
				text = (contentThumbnail ?: "").addEmptyLines(6),
				style = MaterialTheme.typography.bodySmall,
				color = contentColor,
				maxLines = 6,
				modifier = Modifier.weight(1f)
			)

			Spacer(modifier = Modifier.width(8.dp))

			AsyncImage(
				model = ImageRequest.Builder(context).data(attachmentThumbnail).crossfade(300).build(),
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

@Composable
private fun TagView(
	tagList : List<TagObject>
) {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		if (tagList.isNotEmpty()) Spacer(modifier = Modifier.height(4.dp))
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.horizontalScroll(rememberScrollState()),
		) {
			tagList.forEach { tag ->
				Box(
					modifier = Modifier.background(Color(tag.color).copy(alpha = 0.47f), RoundedCornerShape(25))
				) {
					Text(
						text = tag.tag,
						style = MaterialTheme.typography.bodySmall,
						color = Color(tag.color).getInverseBWColor(),
						modifier = Modifier.padding(8.dp, 4.dp)
					)
				}
				Spacer(modifier = Modifier.width(2.dp))
			}
		}
	}
}

@Composable
private fun Location(
	address : String?,
	latLng : LatLng?,
	contentColor : Color,
	isFavouriteTinted : Boolean,
	isFavourite : Boolean
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			painter = painterResource(id = R.drawable.ic_map_marker),
			contentDescription = null,
			tint = if (isFavouriteTinted && isFavourite) contentColor else Color.LocationContainer,
			modifier = Modifier.requiredSize(14.dp)
		)

		Spacer(modifier = Modifier.width(4.dp))

		if (! address.isNullOrBlank()) {
			Text(
				text = address,
				style = MaterialTheme.typography.labelMedium,
				color = contentColor,
				fontStyle = FontStyle.Italic,
				overflow = TextOverflow.Ellipsis,
				maxLines = 1
			)
		} else if (latLng != null) {
			Text(
				text = "${latLng.latitude?.roundTo(6)}, ${latLng.longitude?.roundTo(6)}",
				style = MaterialTheme.typography.labelMedium,
				color = contentColor,
				fontStyle = FontStyle.Italic,
				overflow = TextOverflow.Ellipsis,
				maxLines = 1
			)
		}
	}
}

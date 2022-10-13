package com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.listView

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FixedThreshold
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.utils.addEmptyLines
import com.syncodec.graphite.utils.entryTimestamp0
import com.syncodec.graphite.utils.entryTimestamp1
import com.syncodec.graphite.utils.roundTo
import com.syncodec.graphite.utils.timeStampToTime
import com.syncodec.graphite.utils.tone
import io.realm.kotlin.types.ObjectId
import kotlin.math.roundToInt


@OptIn(
	ExperimentalFoundationApi::class,
	ExperimentalAnimationApi::class, ExperimentalMaterialApi::class
)
@Composable
fun NoteListCard(
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
	containerColor : Color = Color.Transparent,
	selectedColor: Color,
	onClick: () -> Unit,
	onLongClick: (() -> Unit)? = null,
) {
	val containerColor by animateColorAsState(
		when {
			isSelected -> selectedColor
			isFavourite -> Color.FavouriteContainer
			else -> containerColor
		}
	)

	val swipeableState = rememberSwipeableState(0)
	val anchors = mapOf(0f to 0, with(LocalDensity.current) { 128.dp.toPx() } to 1)

	var cardHeight by remember { mutableStateOf(0) }

	AnimatedVisibility(
		visible = isVisible,
		enter = expandVertically(tween(600)) + scaleIn(tween(600)),
		exit = shrinkVertically(tween(600)) + scaleOut(tween(600))
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(8.dp, 0.dp, 12.dp, if (isLast) 8.dp else 0.dp)
				.onGloballyPositioned {
					cardHeight = it.size.height
				},
		) {
			NoteSpacer(
				isLast = isLast,
				height = cardHeight
			)
			Spacer(modifier = Modifier.width(4.dp))
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.swipeable(
						state = swipeableState,
						anchors = anchors,
						thresholds = { _, _ -> FractionalThreshold(0.31f) },
						orientation = Orientation.Horizontal
					)
					.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
			) {
				OutlinedCard(
					shape = RoundedCornerShape(12.dp),
					border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.17f)),
					colors = CardDefaults.cardColors(containerColor),
					elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp),
					modifier = Modifier
						.fillMaxWidth()
						.offset { IntOffset(swipeableState.offset.value.roundToInt(), 0) }
						.clip(RoundedCornerShape(12.dp))
						.combinedClickable(
							onClick = { onClick() },
							onLongClick = { onLongClick?.invoke() }
						)
						.swipeable(
							state = swipeableState,
							anchors = anchors,
							thresholds = { _, _ -> FractionalThreshold(0.31f) },
							orientation = Orientation.Horizontal
						)
				) {
					Box(modifier = Modifier) {
						Box(
							contentAlignment = Alignment.CenterEnd,
							modifier = Modifier
								.fillMaxWidth()
								.height(with(LocalDensity.current) { cardHeight.toDp() }),
						) {
							Box(
								modifier = Modifier
									.width(6.dp)
									.height(80.dp)
									.padding(0.dp, 8.dp)
									.clip(CutCornerShape(16.dp, 0.dp, 0.dp, 16.dp))
									.background(
										MaterialTheme
											.colorScheme
											.surface
											.tone(isSystemInDarkTheme(), 1)
											.copy(alpha = 0.71f)
									)
							)
						}

						Column(
							modifier = Modifier
								.fillMaxWidth()
								.padding(12.dp, 8.dp)
						) {
							Title(
								showFullTime = showFullTime,
								timestamp = timestamp,
								title = title,
								isLocked = isLocked,
								isFavourite = isFavourite,
								attachmentCount = attachmentCount
							)

							Spacer(modifier = Modifier.height(4.dp))

							Content(
								contentThumbnail = contentThumbnail,
								attachmentCount = attachmentCount,
								attachmentThumbnail = attachmentThumbnail
							)

							if (address != null || latLng != null) {
								Spacer(modifier = Modifier.height(8.dp))
								Location(
									address = address,
									latLng = latLng
								)
							}
						}
					}
				}
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
				color = MaterialTheme.colorScheme.onSurface,
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

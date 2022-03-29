package com.syncodec.momento.custom.notebook

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.android.gms.maps.model.LatLng
import com.syncodec.momento.R
import com.syncodec.momento.custom.squircle.Squircle
import com.syncodec.momento.miscellaneous.TimeUtils.Companion.entryTimestamp0
import com.syncodec.momento.miscellaneous.TimeUtils.Companion.entryTimestamp1
import com.syncodec.momento.miscellaneous.TimeUtils.Companion.timeStampToTime
import compose.icons.TablerIcons
import compose.icons.tablericons.Paperclip
import dev.jorgecastillo.androidcolorx.library.tints


data class NoteCardData(
	val key: String,
	val timestamp: Long,
	val showFullTime:Boolean,
	val isLocked: Boolean,
	val isSelected: Boolean,
	val isArchived: Boolean,
	val isFavourite: Boolean,
	val isDeleted: Boolean,
	val isLast: Boolean,
	val title: String?,
	val contentThumbnail: String?,
	val attachmentCount: Int,
	val attachmentThumbnail: Bitmap?,
	val address: String?,
	val latLng: LatLng?,
	var isVisible: Boolean = false,
	val onClick: () -> Unit,
	val onLongClick: (() -> Unit)? = null,
)

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun NoteCard(
	noteCardData: NoteCardData,
) {
	val containerColor by animateColorAsState(
		targetValue = if (noteCardData.isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
		animationSpec = tween(600)
	)

	AnimatedVisibility(
		visible = noteCardData.isVisible,
		enter = expandVertically(tween(600)) + scaleIn(tween(600)),
		exit = shrinkVertically(tween(600)) + scaleOut(tween(600))
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.height(144.dp)
				.padding(8.dp, 0.dp, 8.dp, if (noteCardData.isLast) 8.dp else 0.dp),
		) {
			NoteSpacer(isLast = noteCardData.isLast)
			Spacer(modifier = Modifier.width(4.dp))
			Card(
				elevation = 0.dp,
				shape = RoundedCornerShape(12.dp),
				backgroundColor = containerColor,
				border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer),
				modifier = Modifier
					.fillMaxWidth()
					.clip(RoundedCornerShape(12.dp))
					.combinedClickable(
						onClick = { noteCardData.onClick() },
						onLongClick = { noteCardData.onLongClick?.invoke() }
					)
			) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(12.dp, 8.dp)
				) {
					Row(
						modifier = Modifier,
						verticalAlignment = Alignment.CenterVertically
					) {
						if (noteCardData.showFullTime) {
							Text(
								text = entryTimestamp0(noteCardData.timestamp),
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.primary,
								fontWeight = FontWeight.Bold,
								maxLines = 1,
								modifier = Modifier
							)
							Text(
								text = entryTimestamp1(noteCardData.timestamp),
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.primary.copy(0.31f),
								fontWeight = FontWeight.Bold,
								maxLines = 1,
								overflow = TextOverflow.Ellipsis,
								modifier = Modifier
							)
						} else {
							Text(
								text = timeStampToTime(noteCardData.timestamp),
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.primary,
								fontWeight = FontWeight.Bold,
								maxLines = 1,
								modifier = Modifier
							)
						}

						if (!noteCardData.title.isNullOrBlank()) {
							Spacer(modifier = Modifier.width(2.dp))
							Text(
								text = "·",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.primary,
								fontWeight = FontWeight.Bold,
								maxLines = 1,
								modifier = Modifier
							)
							Spacer(modifier = Modifier.width(2.dp))
							Text(
								text = noteCardData.title,
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.primary,
								fontWeight = FontWeight.Bold,
								maxLines = 1,
								modifier = Modifier
							)
						}

						Spacer(modifier = Modifier.weight(1f))

						if (noteCardData.isLocked) {
							Icon(
								painter = painterResource(id = R.drawable.ic_security),
								contentDescription = "Locked",
								tint = Color(MaterialTheme.colorScheme.primary.toArgb().tints()[1]),
								modifier = Modifier
									.requiredSize(14.dp)
							)
						}

						if (noteCardData.isArchived) {
							if (noteCardData.isLocked) {
								Spacer(modifier = Modifier.width(2.dp))
								Text(
									text = "·",
									style = MaterialTheme.typography.bodySmall,
									color = MaterialTheme.colorScheme.primary,
									fontWeight = FontWeight.Bold,
									maxLines = 1,
									modifier = Modifier
								)
								Spacer(modifier = Modifier.width(2.dp))
							}
							Icon(
								painter = painterResource(id = R.drawable.ic_archive_3),
								contentDescription = "Archived",
								tint = MaterialTheme.colorScheme.primary,
								modifier = Modifier
									.requiredSize(14.dp)
							)
						}

						if (noteCardData.isFavourite) {
							if (noteCardData.isLocked || noteCardData.isArchived) {
								Spacer(modifier = Modifier.width(2.dp))
								Text(
									text = "·",
									style = MaterialTheme.typography.bodySmall,
									color = MaterialTheme.colorScheme.primary,
									fontWeight = FontWeight.Bold,
									maxLines = 1,
									modifier = Modifier
								)
								Spacer(modifier = Modifier.width(2.dp))
							}

							Icon(
								painter = painterResource(id = R.drawable.ic_heart),
								contentDescription = "Favourite",
								tint = MaterialTheme.colorScheme.primary,
								modifier = Modifier
									.requiredSize(14.dp)
							)
						}

						if (noteCardData.attachmentCount != 0) {
							if (noteCardData.isLocked || noteCardData.isArchived || noteCardData.isFavourite) {
								Spacer(modifier = Modifier.width(2.dp))
								Text(
									text = "·",
									style = MaterialTheme.typography.bodySmall,
									color = MaterialTheme.colorScheme.primary,
									fontWeight = FontWeight.Bold,
									maxLines = 1,
									modifier = Modifier
								)
								Spacer(modifier = Modifier.width(2.dp))
							}

							Icon(
								imageVector = TablerIcons.Paperclip,
								contentDescription = "Attachment",
								tint = MaterialTheme.colorScheme.primary,
								modifier = Modifier
									.requiredSize(14.dp)
							)
							Spacer(modifier = Modifier.width(2.dp))
							Text(
								text = "·",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.primary,
								fontWeight = FontWeight.Bold,
								maxLines = 1,
								modifier = Modifier
							)
							Spacer(modifier = Modifier.width(2.dp))
							Text(
								text = "${noteCardData.attachmentCount}",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.primary,
								fontWeight = FontWeight.Bold,
								maxLines = 1,
								modifier = Modifier
							)
						}

					}

					Spacer(modifier = Modifier.height(4.dp))

					if (noteCardData.attachmentThumbnail == null) {
						Text(
							text = "${noteCardData.contentThumbnail}",
							style = MaterialTheme.typography.bodyMedium,
							maxLines = 4,
							modifier = Modifier
								.height(80.dp)
						)
					} else {
						Row(
							modifier = Modifier
								.fillMaxWidth()
						) {
							Text(
								text = "${noteCardData.contentThumbnail}",
								style = MaterialTheme.typography.bodyMedium,
								maxLines = 4,
								modifier = Modifier
									.height(80.dp)
									.weight(1f)
							)
							Spacer(modifier = Modifier.width(8.dp))
							Squircle(
								sizeInDp = 80.dp,
								smoothing = 4.0
							) {
								Image(
									painter = rememberImagePainter(data = noteCardData.attachmentThumbnail),
									contentDescription = null,
									contentScale = ContentScale.Crop,
									modifier = Modifier.fillMaxSize(),
								)
							}
						}
					}

					Spacer(modifier = Modifier.weight(1f))

					if (!noteCardData.address.isNullOrBlank()) {
						Row(
							modifier = Modifier
								.fillMaxWidth(),
							verticalAlignment = Alignment.CenterVertically
						) {
							Icon(
								painter = painterResource(id = R.drawable.ic_location_pin_3),
//							imageVector = TablerIcons.MapPin,
								contentDescription = null,
//							tint = Color.Unspecified,
								tint = Color(MaterialTheme.colorScheme.primary.toArgb().tints()[1]),
								modifier = Modifier.requiredSize(16.dp)
							)
							Spacer(modifier = Modifier.width(2.dp))
							Text(
								text = "${noteCardData.address}",
								style = MaterialTheme.typography.bodySmall,
								fontStyle = FontStyle.Italic,
								color = Color(MaterialTheme.colorScheme.primary.toArgb().tints()[1]),
								maxLines = 1,
								overflow = TextOverflow.Ellipsis,
								modifier = Modifier
							)
						}
					}
				}

				Box(
					modifier = Modifier
						.fillMaxSize(),
					contentAlignment = Alignment.CenterEnd
				) {
					Box(
						modifier = Modifier
							.width(8.dp)
							.height(80.dp)
							.clip(CutCornerShape(16.dp, 0.dp, 0.dp, 16.dp))
							.background(MaterialTheme.colorScheme.secondaryContainer)
					)
				}
			}
		}
	}
}

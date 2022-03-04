package com.syncodec.momento.custom.entry

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.syncodec.momento.R
import com.syncodec.momento.custom.squircle.Squircle
import com.syncodec.momento.miscellaneous.base64stringToBitmap
import com.syncodec.momento.miscellaneous.timeStampToTime
import compose.icons.TablerIcons
import compose.icons.tablericons.Paperclip
import dev.jorgecastillo.androidcolorx.library.tints


data class EntryCard(
	val timestamp: Long,
	val isLocked: Boolean,
	val isSelected: Boolean,
	val isArchived: Boolean,
	val isFavourite: Boolean,
	val isDeleted: Boolean,
	val isLast: Boolean,
	val title: String?,
	val contentThumbnail: String?,
	val attachmentCount: Int,
	val attachmentThumbnail: String?,
	val tint: Color = Color.LightGray,
	val address: String?,
	val isVisible: Boolean = false,
	val onClick: () -> Unit,
	val onLongClick: (() -> Unit)?,
)

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun EntryCard(
	entryCard: EntryCard,
) {
	AnimatedVisibility(
		visible = entryCard.isVisible,
		enter = expandVertically() + scaleIn(),
		exit = shrinkVertically() + scaleOut()
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.height(if (entryCard.isLast) 152.dp else 144.dp)
				.padding(8.dp, 0.dp, 8.dp, if (entryCard.isLast) 8.dp else 0.dp),
		) {
			EntrySpacer(
				isLast = entryCard.isLast,
				tint = entryCard.tint
			)
			Spacer(modifier = Modifier.width(4.dp))
			Card(
				elevation = 0.dp,
				shape = RoundedCornerShape(12.dp),
				backgroundColor = if (entryCard.isSelected) Color.LightGray else Color.Transparent,
				border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer),
				modifier = Modifier
					.fillMaxSize()
					.clip(RoundedCornerShape(12.dp))
					.combinedClickable(
						onClick = { entryCard.onClick() },
						onLongClick = { entryCard.onLongClick?.invoke() }
					)
			) {
				Column(
					modifier = Modifier
						.fillMaxSize()
						.padding(12.dp, 8.dp, 12.dp, 8.dp)
				) {
					Row(
						modifier = Modifier,
						verticalAlignment = Alignment.CenterVertically
					) {

						Text(
							text = timeStampToTime(entryCard.timestamp),
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.primary,
							fontWeight = FontWeight.Bold,
							maxLines = 1,
							modifier = Modifier
						)

						if (entryCard.title != null) {
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
								text = entryCard.title,
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.primary,
								fontWeight = FontWeight.Bold,
								maxLines = 1,
								modifier = Modifier
							)
						}

						Spacer(modifier = Modifier.weight(1f))

						if (entryCard.isLocked) {
							Icon(
								painter = painterResource(id = R.drawable.ic_lock_3),
								contentDescription = "Locked",
								tint = Color(MaterialTheme.colorScheme.primary.toArgb().tints()[1]),
								modifier = Modifier
									.requiredSize(14.dp)
							)
						}

						if (entryCard.isArchived) {
							if (entryCard.isLocked) {
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

						if (entryCard.isFavourite) {
							if (entryCard.isLocked || entryCard.isArchived) {
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
								painter = painterResource(id = R.drawable.ic_heart_3),
								contentDescription = "Favourite",
								tint = MaterialTheme.colorScheme.primary,
								modifier = Modifier
									.requiredSize(14.dp)
							)
						}

						if (entryCard.attachmentCount != 0) {
							if (entryCard.isLocked || entryCard.isArchived || entryCard.isFavourite) {
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
								text = "${entryCard.attachmentCount}",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.primary,
								fontWeight = FontWeight.Bold,
								maxLines = 1,
								modifier = Modifier
							)
						}

					}

					Spacer(modifier = Modifier.height(8.dp))

					if (entryCard.attachmentThumbnail == null) {
						Text(
							text = "${entryCard.contentThumbnail}",
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
								text = "${entryCard.contentThumbnail}",
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
									painter = rememberImagePainter(data = entryCard.attachmentThumbnail!!.base64stringToBitmap()),
									contentDescription = null,
									modifier = Modifier
										.fillMaxSize()
								)
							}
						}
					}

					Spacer(modifier = Modifier.weight(1f))

					if (entryCard.address != null) {
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
								modifier = Modifier
									.requiredSize(16.dp)
							)
							Spacer(modifier = Modifier.width(2.dp))
							Text(
								text = "${entryCard.address}",
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

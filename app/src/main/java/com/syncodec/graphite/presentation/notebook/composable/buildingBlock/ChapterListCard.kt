package com.syncodec.graphite.presentation.notebook.composable.buildingBlock

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.TagObjectLite
import com.syncodec.graphite.presentation.common.ExpandableBox
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.presentation.ui.FavouriteContent
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.utils.getInverseBWColor
import com.syncodec.graphite.utils.timeStampToPrettyFull
import io.realm.kotlin.types.RealmUUID


@OptIn(
	ExperimentalFoundationApi::class
)
@Composable
fun ChapterListCard(
	id : RealmUUID,
	timestamp : Long,
	isSelected : Boolean,
	isLocked : Boolean,
	isFavourite : Boolean,
	isDeleted : Boolean,
	isLast : Boolean,
	title : String?,
	description : String?,
	color : Color?,
	thumbnail : Bitmap?,
	noteCount : Int,
	chapterCount : Int,
	tagList : List<TagObjectLite>,
	isVisible : Boolean = false,
	selectedColor : Color,
	onClick : () -> Unit,
	onLongClick : (() -> Unit)? = null,
) {
	val containerColor by animateColorAsState(
		when {
			isSelected -> selectedColor
			isFavourite -> Color(ColorUtils.blendARGB(Color.FavouriteContainer.toArgb(), MaterialTheme.colorScheme.background.toArgb(), 0.71f))
			else -> MaterialTheme.colorScheme.background.copy(alpha = 0.71f)
		}
	)
	val contentColor by animateColorAsState(
		when {
			isSelected -> selectedColor.getInverseBWColor()
			isFavourite -> Color.FavouriteContent.getInverseBWColor()
			else -> MaterialTheme.colorScheme.onSurface
		}
	)

	var size by remember { mutableStateOf<IntSize?>(null) }

	var isExpanded by remember { mutableStateOf(false) }

	if (isVisible) {
		OutlinedCard(
			shape = RoundedCornerShape(12.dp),
			border = BorderStroke(1.dp, if (isFavourite) containerColor else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.17f)),
			colors = CardDefaults.cardColors(containerColor = containerColor),
			elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp),
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp, 4.dp)
				.clip(RoundedCornerShape(12.dp))
				.combinedClickable(
					onClick = { onClick() },
					onLongClick = { onLongClick?.invoke() }
				)
		) {
			Box(
				modifier = Modifier.fillMaxWidth()
			) {

				thumbnail?.let {
					Image(
						bitmap = it.asImageBitmap(),
						contentDescription = null,
						contentScale = ContentScale.Crop,
						modifier = with(LocalDensity.current) {
							Modifier.size(size?.width?.toDp()?.plus(12.dp) ?: 1.dp, size?.height?.toDp()?.plus(16.dp) ?: 1.dp)
						}
					)
				}

				if (thumbnail != null) {
					Box(
						modifier = with(LocalDensity.current) {
							Modifier
								.size(
									size?.width
										?.toDp()
										?.plus(12.dp) ?: 1.dp,
									size?.height
										?.toDp()
										?.plus(16.dp) ?: 1.dp
								)
								.background(Color.Black.copy(alpha = 0.31f))
						}
					)
				}

				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(12.dp, 8.dp, 0.dp, 8.dp)
						.onGloballyPositioned {
							size = it.size
						}
				) {
					Title(
						timestamp = timestamp,
						title = title,
						contentColor = if (thumbnail == null) contentColor else Color.White,
						noteCount = noteCount,
						chapterCount = chapterCount,
						isExpanded = isExpanded,
					) { isExpanded = ! isExpanded }

					ExpandableBox(
						isVisible = isExpanded,
					) {
						ExpandedContent(
							id = id,
							timestamp = timestamp,
							title = title,
							description = description,
							contentColor = if (thumbnail == null) contentColor else Color.White,
						)
					}

					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier.fillMaxWidth()
					) {
						Text(
							text = id.toString(),
							style = MaterialTheme.typography.bodySmall,
							fontStyle = FontStyle.Italic,
							color = if (thumbnail == null) contentColor else Color.White,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis,
						)

						Spacer(modifier = Modifier.width(12.dp))

						if (color != null) {
							Box(
								modifier = Modifier
									.weight(1f)
									.height(8.dp)
									.background(color = color, shape = RoundedCornerShape(50))
							)
						}
						if (isLocked) {
							Spacer(modifier = Modifier.width(12.dp))

							Icon(
								painter = painterResource(id = R.drawable.ic_lock_close),
								contentDescription = "Locked",
								tint = if (thumbnail == null) contentColor else Color.White,
								modifier = Modifier.size(20.dp)
							)
						}

						Spacer(modifier = Modifier.width(12.dp))
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun Title(
	timestamp : Long,
	title : String?,
	contentColor : Color,
	noteCount : Int,
	chapterCount : Int,
	isExpanded : Boolean,
	onMoreInfo : () -> Unit,
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = title ?: "Untitled",
			style = MaterialTheme.typography.titleLarge,
			fontWeight = if (title.isNullOrEmpty()) FontWeight.Normal else FontWeight.Bold,
			fontStyle = if (title.isNullOrEmpty()) FontStyle.Italic else FontStyle.Normal,
			color = contentColor,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.weight(1f),
		)

		Spacer(modifier = Modifier.width(32.dp))

		Icon(
			painter = painterResource(id = R.drawable.ic_notebook),
			contentDescription = "Chapter count",
			tint = contentColor,
			modifier = Modifier.size(IconButtonSize)
		)

		Spacer(modifier = Modifier.width(4.dp))

		Text(
			text = "$chapterCount",
			style = MaterialTheme.typography.bodyMedium,
			fontWeight = FontWeight.Bold,
			color = contentColor,
		)

		Spacer(modifier = Modifier.width(16.dp))

		Icon(
			painter = painterResource(id = R.drawable.ic_note),
			contentDescription = "Note count",
			tint = contentColor,
			modifier = Modifier.size(IconButtonSize)
		)

		Spacer(modifier = Modifier.width(4.dp))

		Text(
			text = "$noteCount",
			style = MaterialTheme.typography.bodyMedium,
			fontWeight = FontWeight.Bold,
			color = contentColor,
		)

		Spacer(modifier = Modifier.width(4.dp))

		val caretAngle by animateFloatAsState(if (isExpanded) 180f else 90f)

		MenuButton(
			icon = R.drawable.ic_caret,
			contentDescription = "Less info",
			tint = contentColor,
			modifier = Modifier.graphicsLayer {
				rotationZ = caretAngle
			},
			onClick = onMoreInfo
		)
	}
}

@Composable
private fun ExpandedContent(
	id : RealmUUID,
	timestamp : Long,
	title : String?,
	description : String?,
	contentColor : Color
) {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		Text(
			text = title ?: "",
			style = MaterialTheme.typography.bodyMedium,
			fontStyle = if (title.isNullOrEmpty()) FontStyle.Italic else FontStyle.Normal,
			color = contentColor,
		)

		Spacer(modifier = Modifier.height(4.dp))

		Text(
			text = if (description.isNullOrBlank()) "No description" else description,
			style = MaterialTheme.typography.bodyMedium,
			fontStyle = if (description.isNullOrBlank()) FontStyle.Italic else FontStyle.Normal,
			color = contentColor,
		)

		Spacer(modifier = Modifier.height(4.dp))

		Text(
			text = "Created on ${timestamp.timeStampToPrettyFull()}",
			style = MaterialTheme.typography.bodyMedium,
			fontStyle = if (description.isNullOrBlank()) FontStyle.Italic else FontStyle.Normal,
			color = contentColor,
		)

		Spacer(modifier = Modifier.height(4.dp))
	}
}

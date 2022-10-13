package com.syncodec.graphite.presentation.note.composable.dialog.chapterSelectorDialog

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.syncodec.graphite.presentation.common.ExpandableBox
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.utils.getInverseBWColor
import com.syncodec.graphite.utils.timeStampToPrettyFull
import io.realm.kotlin.types.ObjectId


@OptIn(
	ExperimentalFoundationApi::class,
	ExperimentalAnimationApi::class
)
@Composable
fun ChapterListCard(
	id: ObjectId,
	timestamp: Long,
	isLocked: Boolean,
	isSelected: Boolean,
	isFavourite: Boolean,
	isDeleted: Boolean,
	isLast: Boolean,
	title: String,
	description: String?,
	color: Color? = MaterialTheme.colorScheme.surface,
	thumbnail: Bitmap?,
	noteCount: Int,
	chapterCount: Int,
	isVisible: Boolean = false,
	selectedColor: Color,
	onClick: () -> Unit,
	onLongClick: (() -> Unit)? = null,
) {
	val containerColor by animateColorAsState(
		when {
			isSelected -> selectedColor
			isFavourite -> Color(0x13FE3A58)
			else -> color!!.copy(alpha = 0.47f)
		}
	)

	var isExpanded by remember { mutableStateOf(false) }

	AnimatedVisibility(
		visible = isVisible,
		enter = expandVertically(tween(600)) + scaleIn(tween(600)),
		exit = shrinkVertically(tween(600)) + scaleOut(tween(600))
	) {
		OutlinedCard(
			shape = RoundedCornerShape(12.dp),
			border = BorderStroke(2.dp, color!!),
			colors = CardDefaults.cardColors(containerColor),
			elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp),
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp, 4.dp)
				.clip(RoundedCornerShape(12.dp))
				.combinedClickable(
					onClick = { onClick() },
					onLongClick = { onLongClick?.invoke() }
				),
		) {
			Box(modifier = Modifier) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(12.dp, 8.dp)
				) {
					Title(
						timestamp = timestamp,
						title = title,
						color = color,
						isLocked = isLocked,
						isFavourite = isFavourite,
						noteCount = noteCount,
						chapterCount = chapterCount,
						isExpanded = isExpanded,
					) { isExpanded = !isExpanded }

					ExpandableBox(
						isVisible = isExpanded,
					) {
						ExpandedContent(
							id = id,
							timestamp = timestamp,
							title = title,
							description = description,
							color = color,
						)
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun Title(
	timestamp: Long,
	title: String,
	color: Color,
	isLocked: Boolean,
	isFavourite: Boolean,
	noteCount: Int,
	chapterCount: Int,
	isExpanded: Boolean,
	onMoreInfo: () -> Unit,
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = title,
			style = MaterialTheme.typography.titleLarge,
			fontWeight = FontWeight.Bold,
			color = color.getInverseBWColor(),
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.weight(1f),
		)

		Spacer(modifier = Modifier.width(32.dp))

		Icon(
			painter = painterResource(id = R.drawable.ic_notebook),
			contentDescription = "Chapter count",
			tint = color.getInverseBWColor(),
		)

		Spacer(modifier = Modifier.width(4.dp))

		Text(
			text = "$chapterCount chapters",
			style = MaterialTheme.typography.bodyMedium,
			color = color.getInverseBWColor(),
		)

		Spacer(modifier = Modifier.width(16.dp))

		Icon(
			painter = painterResource(id = R.drawable.ic_note),
			contentDescription = "Note count",
			tint = color.getInverseBWColor(),
		)

		Spacer(modifier = Modifier.width(4.dp))

		Text(
			text = "$noteCount notes",
			style = MaterialTheme.typography.bodyMedium,
			color = color.getInverseBWColor(),
		)

		Spacer(modifier = Modifier.width(4.dp))

		if (isLocked) {
			Icon(
				painter = painterResource(id = R.drawable.ic_lock_close),
				contentDescription = "Locked",
				tint = color.getInverseBWColor(),
			)
		}

		if (isFavourite) {
			Icon(
				painter = painterResource(id = R.drawable.ic_favourite),
				contentDescription = "Favourite",
				tint = color.getInverseBWColor(),
			)
		}

		AnimatedContent(
			targetState = isExpanded,
			transitionSpec = { fadeIn(tween(300)) with fadeOut(tween(300)) }
		) {
			if (it) {
				MenuButton(
					icon = R.drawable.ic_chevron_down,
					contentDescription = "Less info",
					tint = color.getInverseBWColor(),
					onClick = onMoreInfo
				)

			} else {
				MenuButton(
					icon = R.drawable.ic_chevron_right,
					contentDescription = "More info",
					tint = color.getInverseBWColor(),
					onClick = onMoreInfo
				)
			}
		}
	}
}

@Composable
private fun ExpandedContent(
	id: ObjectId,
	timestamp: Long,
	title: String,
	description: String?,
	color: Color
) {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		Text(
			text = title,
			style = MaterialTheme.typography.bodyMedium,
			color = color.getInverseBWColor(),
		)

		Spacer(modifier = Modifier.height(4.dp))

		Text(
			text = if (description.isNullOrBlank()) "No description" else description,
			style = MaterialTheme.typography.bodyMedium,
			fontStyle = if (description.isNullOrBlank()) FontStyle.Italic else FontStyle.Normal,
			color = color.getInverseBWColor(),
		)

		Spacer(modifier = Modifier.height(4.dp))

		Text(
			text = "Created on ${timestamp.timeStampToPrettyFull()}",
			style = MaterialTheme.typography.bodyMedium,
			fontStyle = if (description.isNullOrBlank()) FontStyle.Italic else FontStyle.Normal,
			color = color.getInverseBWColor(),
		)

		Spacer(modifier = Modifier.height(4.dp))

		Text(
			text = id.toString(),
			style = MaterialTheme.typography.bodySmall,
			fontStyle = FontStyle.Italic,
			color = color.getInverseBWColor(),
		)
	}
}

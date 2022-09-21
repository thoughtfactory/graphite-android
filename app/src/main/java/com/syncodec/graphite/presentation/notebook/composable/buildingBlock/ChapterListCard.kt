package com.syncodec.graphite.presentation.notebook.composable.buildingBlock

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.TagObjectLite
import com.syncodec.graphite.presentation.custom.ExpandableBox
import com.syncodec.graphite.presentation.custom.button.MenuButton
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.presentation.ui.FavouriteContent
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
	isSelected: Boolean,
	isLocked: Boolean,
	isFavourite: Boolean,
	isDeleted: Boolean,
	isLast: Boolean,
	title: String,
	description: String?,
	color: Color,
	noteCount: Int,
	chapterCount: Int,
	tagList: List<TagObjectLite>,
	isVisible: Boolean = false,
	selectedColor: Color,
	onClick: () -> Unit,
	onLongClick: (() -> Unit)? = null,
) {
	val containerColor by animateColorAsState(
		when {
			isSelected -> selectedColor
//			else -> color
			isFavourite -> Color.FavouriteContainer.copy(alpha = 0.71f)
			else -> MaterialTheme.colorScheme.background.copy(alpha = 0.71f)
		}
	)
	val contentColor by animateColorAsState(
		when {
			isSelected -> selectedColor.getInverseBWColor()
			isFavourite -> Color.FavouriteContent
			else -> MaterialTheme.colorScheme.onSurface
		}
	)

	var isExpanded by remember { mutableStateOf(false) }

	AnimatedVisibility(
		visible = isVisible,
		enter = expandVertically(tween(600)) + scaleIn(tween(600)),
		exit = shrinkVertically(tween(600)) + scaleOut(tween(600))
	) {
		Card(
			shape = RoundedCornerShape(12.dp),
			colors = CardDefaults.cardColors(containerColor = containerColor),
			elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp),
			border = BorderStroke(2.dp, if (isFavourite) containerColor else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.17f)),
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp, 4.dp)
				.clip(RoundedCornerShape(12.dp))
				.combinedClickable(
					onClick = { onClick() },
					onLongClick = { onLongClick?.invoke() }
				),
		) {
			Box(
				modifier = Modifier.fillMaxWidth()
			) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(12.dp, 8.dp, 0.dp, 8.dp)
				) {
					Title(
						timestamp = timestamp,
						title = title,
						contentColor = contentColor,
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
							contentColor = contentColor,
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
							color = contentColor,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis,
						)

						Spacer(modifier = Modifier.width(12.dp))

						Box(
							modifier = Modifier
								.weight(1f)
								.height(8.dp)
								.background(color = color, shape = RoundedCornerShape(50))
						)

						if (isLocked) {
							Spacer(modifier = Modifier.width(12.dp))

							Icon(
								painter = painterResource(id = R.drawable.ic_lock_close),
								contentDescription = "Locked",
								tint = contentColor,
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
	timestamp: Long,
	title: String,
	contentColor: Color,
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
			color = contentColor,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.weight(1f),
		)

		Spacer(modifier = Modifier.width(32.dp))

		Icon(
			painter = painterResource(id = R.drawable.ic_notebook),
			contentDescription = "Chapter count",
			tint = contentColor,
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
		)

		Spacer(modifier = Modifier.width(4.dp))

		Text(
			text = "$noteCount",
			style = MaterialTheme.typography.bodyMedium,
			fontWeight = FontWeight.Bold,
			color = contentColor,
		)

		Spacer(modifier = Modifier.width(4.dp))

		AnimatedContent(
			targetState = isExpanded,
			transitionSpec = { fadeIn(tween(300)) with fadeOut(tween(300)) }
		) {
			if (it) {
				MenuButton(
					icon = R.drawable.ic_chevron_down,
					contentDescription = "Less info",
					tint = contentColor,
					onClick = onMoreInfo
				)

			} else {
				MenuButton(
					icon = R.drawable.ic_chevron_right,
					contentDescription = "More info",
					tint = contentColor,
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
	contentColor: Color
) {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		Text(
			text = title,
			style = MaterialTheme.typography.bodyMedium,
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

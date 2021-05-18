package com.syncodec.momento.custom.notebook

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.syncodec.momento.R
import com.syncodec.momento.miscellaneous.entryTimestamp0
import com.syncodec.momento.miscellaneous.entryTimestamp1
import dev.jorgecastillo.androidcolorx.library.tints

data class ChapterCardData(
	val timestamp: Long,
	val isLocked: Boolean,
	val isSelected: Boolean,
	val isArchived: Boolean,
	val isFavourite: Boolean,
	val isDeleted: Boolean,
	val isLast: Boolean,
	val title: String,
	val description: String?,
	val noteCount: Int,
	val chapterCount: Int,
	val isVisible: Boolean = false,
	val onClick: () -> Unit,
	val onLongClick: (() -> Unit)? = null,
)

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun ChapterCard(
	chapterCardData: ChapterCardData,
) {
	val containerColor by animateColorAsState(
		targetValue = if (chapterCardData.isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
		animationSpec = tween(600)
	)

	AnimatedVisibility(
		visible = chapterCardData.isVisible,
		enter = expandVertically(tween(600)) + scaleIn(tween(600)),
		exit = shrinkVertically(tween(600)) + scaleOut(tween(600))
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.height(if (chapterCardData.isLast) 64.dp else 56.dp)
				.padding(8.dp, 0.dp, 8.dp, if (chapterCardData.isLast) 8.dp else 0.dp),
		) {
			NoteSpacer(isLast = chapterCardData.isLast)
			Spacer(modifier = Modifier.width(4.dp))
			Card(
				elevation = 0.dp,
				shape = RoundedCornerShape(12.dp),
				backgroundColor = containerColor,
				border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer),
				modifier = Modifier
					.fillMaxSize()
					.clip(RoundedCornerShape(12.dp))
					.combinedClickable(
						onClick = { chapterCardData.onClick() },
						onLongClick = { chapterCardData.onLongClick?.invoke() }
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
							text = entryTimestamp0(chapterCardData.timestamp),
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.primary,
							fontWeight = FontWeight.Bold,
							maxLines = 1,
							modifier = Modifier
						)
						Text(
							text = entryTimestamp1(chapterCardData.timestamp),
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.primary.copy(0.31f),
							fontWeight = FontWeight.Bold,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis,
							modifier = Modifier
						)

						Spacer(modifier = Modifier.weight(1f))

						if (chapterCardData.isLocked) {
							Icon(
								painter = painterResource(id = R.drawable.ic_lock_3),
								contentDescription = "Locked",
								tint = Color(MaterialTheme.colorScheme.primary.toArgb().tints()[1]),
								modifier = Modifier
									.requiredSize(14.dp)
							)
						}

						if (chapterCardData.isArchived) {
							if (chapterCardData.isLocked) {
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

						if (chapterCardData.isFavourite) {
							if (chapterCardData.isLocked || chapterCardData.isArchived) {
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
					}
					Text(
						text = chapterCardData.title,
						style = MaterialTheme.typography.titleSmall,
						color = MaterialTheme.colorScheme.onBackground,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
						modifier = Modifier
							.height(80.dp)
					)
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

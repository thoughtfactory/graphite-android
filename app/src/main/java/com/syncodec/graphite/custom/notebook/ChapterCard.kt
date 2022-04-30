package com.syncodec.graphite.custom.notebook

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
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
import com.syncodec.graphite.R
import com.syncodec.graphite.miscellaneous.TimeUtils.Companion.entryTimestamp0
import com.syncodec.graphite.miscellaneous.TimeUtils.Companion.entryTimestamp1
import dev.jorgecastillo.androidcolorx.library.tints


@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class, ExperimentalAnimationApi::class,
	ExperimentalMaterial3Api::class
)
@Composable
fun ChapterCard(
	timestamp: Long,
	isLocked: Boolean,
	isSelected: Boolean,
	isArchived: Boolean,
	isFavourite: Boolean,
	isDeleted: Boolean,
	isLast: Boolean,
	title: String,
	description: String?,
	noteCount: Int,
	chapterCount: Int,
	isVisible: Boolean = false,
	selectedColor: Color,
	onClick: () -> Unit,
	onLongClick: (() -> Unit)? = null,
) {
	val containerColor by animateColorAsState(
		if (isSelected) selectedColor else Color.Transparent
	)

	AnimatedVisibility(
		visible = isVisible,
		enter = expandVertically(tween(600)) + scaleIn(tween(600)),
		exit = shrinkVertically(tween(600)) + scaleOut(tween(600))
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.height(if (isLast) 64.dp else 56.dp)
				.padding(8.dp, 0.dp, 8.dp, if (isLast) 8.dp else 0.dp),
		) {
			NoteSpacer(isLast = isLast)
			Spacer(modifier = Modifier.width(4.dp))
			OutlinedCard(
				shape = RoundedCornerShape(12.dp),
				border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.31f)),
				containerColor = containerColor,
				elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp),
				modifier = Modifier
					.fillMaxSize()
					.clip(RoundedCornerShape(12.dp))
					.combinedClickable(
						onClick = { onClick() },
						onLongClick = { onLongClick?.invoke() }
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
							text = entryTimestamp0(timestamp),
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.primary,
							fontWeight = FontWeight.Bold,
							maxLines = 1,
							modifier = Modifier
						)
						Text(
							text = entryTimestamp1(timestamp),
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.primary.copy(0.31f),
							fontWeight = FontWeight.Bold,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis,
							modifier = Modifier
						)

						Spacer(modifier = Modifier.weight(1f))

						if (isLocked) {
							Icon(
								painter = painterResource(id = R.drawable.ic_security),
								contentDescription = "Locked",
								tint = Color(MaterialTheme.colorScheme.primary.toArgb().tints()[1]),
								modifier = Modifier
									.requiredSize(14.dp)
							)
						}

						if (isArchived) {
							if (isLocked) {
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
								painter = painterResource(id = R.drawable.ic_archive),
								contentDescription = "Archived",
								tint = MaterialTheme.colorScheme.primary,
								modifier = Modifier
									.requiredSize(14.dp)
							)
						}

						if (isFavourite) {
							if (isLocked || isArchived) {
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
								painter = painterResource(id = R.drawable.ic_favourite),
								contentDescription = "Favourite",
								tint = MaterialTheme.colorScheme.primary,
								modifier = Modifier
									.requiredSize(14.dp)
							)
						}
					}
					Text(
						text = title,
						style = MaterialTheme.typography.titleSmall,
						color = MaterialTheme.colorScheme.onBackground,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
						modifier = Modifier
							.height(80.dp)
					)
				}
			}
		}
	}
}

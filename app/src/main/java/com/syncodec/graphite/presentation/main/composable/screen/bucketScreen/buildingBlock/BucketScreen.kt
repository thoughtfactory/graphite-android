package com.syncodec.graphite.presentation.main.composable.screen.bucketScreen.buildingBlock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.model.bucketTypeIconMap
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.presentation.ui.LockClosedContainer


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun BucketCard(
	title : String? = null,
	bucketSize : Int = 0,
	bucketType : BucketType = BucketType.BOOK,
	isLocked : Boolean = false,
	isFavourite : Boolean = false,
	isSelecting : Boolean = false,
	isSelected : Boolean = false,
	isDragging : Boolean = false,
	handle : @Composable () -> Unit = {},
	onClick : () -> Unit = {},
	onLongClick : () -> Unit = {},
) {
	val hapticFeedback = LocalHapticFeedback.current
	val scale by animateFloatAsState(
		targetValue = when {
			isDragging -> 1.17f
			isSelected -> 0.91f
			else -> 1f
		}, label = ""
	)

	val containerColor by animateColorAsState(
		targetValue = when {
			isSelected -> MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)
			isDragging -> MaterialTheme.colorScheme.surfaceColorAtElevation(16.dp)
			else -> Color(
				ColorUtils.blendARGB(
					MaterialTheme.colorScheme.background.toArgb(),
					MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).toArgb(),
					0.17f
				)
			)
		}, label = ""
	)

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(96.dp)
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.graphicsLayer { this.scaleX = scale;this.scaleY = scale }
		) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(containerColor, MaterialTheme.shapes.medium)
					.clip(MaterialTheme.shapes.medium)
					.combinedClickable(
						onClick = onClick,
						onLongClick = {
							hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
							onLongClick()
						}
					)
			) {
				Column(
					verticalArrangement = Arrangement.SpaceBetween,
					horizontalAlignment = Alignment.Start,
					modifier = Modifier
						.fillMaxSize()
						.padding(10.dp)
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier.fillMaxWidth()
					) {
						Icon(
							painter = painterResource(id = bucketTypeIconMap.getOrElse(bucketType) { R.drawable.ic_bucket }),
							contentDescription = null,
							tint = MaterialTheme.colorScheme.onSurface,
							modifier = Modifier.requiredSize(IconButtonSize)
						)
						Spacer(modifier = Modifier.weight(1f))

						StateInfo(
							isFavourite = isFavourite,
							isLocked = isLocked,
							bucketSize = bucketSize,
						)
					}
					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier.fillMaxWidth()
					) {
						Text(
							text = title ?: "Untitled",
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onSurface,
							fontWeight = FontWeight.Bold,
							fontStyle = if (title.isNullOrEmpty()) FontStyle.Italic else FontStyle.Normal,
							modifier = Modifier.weight(1f)
						)

						Spacer(modifier = Modifier.width(12.dp))

						AnimatedVisibility(
							visible = ! isSelecting,
							enter = fadeIn(tween(470)) + scaleIn(tween(470)),
							exit = fadeOut(tween(470)) + scaleOut(tween(470)),
						) {
							handle()
						}
					}
				}
			}
		}

		AnimatedVisibility(
			visible = isSelected,
			enter = scaleIn(tween(470)),
			exit = scaleOut(tween(470)),
			modifier = Modifier.align(Alignment.TopEnd)
		) {
			Box(
				modifier = Modifier
					.requiredSize(32.dp)
					.align(Alignment.TopEnd)
					.background(MaterialTheme.colorScheme.background, CircleShape)
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_fa_circle_check),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.primary,
					modifier = Modifier
						.requiredSize(24.dp)
						.align(Alignment.Center)
				)
			}
		}
	}
}

@Preview
@Composable
private fun StateInfo(
	isFavourite : Boolean = false,
	isLocked : Boolean = false,
	bucketSize : Int = 71,
) {
	Box(
		modifier = Modifier.background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.small)
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(8.dp, 4.dp)
		) {
			if (isLocked) {
				Icon(
					painter = painterResource(id = R.drawable.ic_shield),
					contentDescription = "Locked",
					tint = Color.LockClosedContainer,
					modifier = Modifier.requiredSize(14.dp)
				)
				HeaderText(text = "·", modifier = Modifier.padding(horizontal = 2.dp))
			}
			if (isFavourite) {
				Icon(
					painter = painterResource(id = R.drawable.ic_favourite),
					contentDescription = "Favourite",
					tint = Color.FavouriteContainer,
					modifier = Modifier.requiredSize(14.dp)
				)
				HeaderText(text = "·", modifier = Modifier.padding(horizontal = 2.dp))
			}
			HeaderText(text = "$bucketSize")
		}
	}
}

@Preview
@Composable
private fun HeaderText(
	modifier : Modifier = Modifier,
	text : String = "Header",
	contentColor : Color = MaterialTheme.colorScheme.onSurface,
) {
	Text(
		text = text,
		style = MaterialTheme.typography.bodySmall,
		color = contentColor,
		fontWeight = FontWeight.Bold,
		maxLines = 1,
		modifier = modifier
	)
}

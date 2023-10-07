package com.syncodec.graphite.presentation.bucket.composable.buildingBlock

import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.base.AttachmentContainer
import com.syncodec.graphite.presentation.base.FavouriteContainer
import com.syncodec.graphite.presentation.base.LocalIsDarkTheme
import com.syncodec.graphite.presentation.base.LocationContainer
import com.syncodec.graphite.presentation.base.LockClosedContainer
import io.realm.kotlin.types.RealmUUID


@Preview
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BucketGridItem(
	id: RealmUUID = RealmUUID.random(),
	bucketType: BucketType = BucketType.UNKNOWN,
	title: String? = null,
	thumbnail: String? = null,
	isLocked: Boolean = true,
	isFavourite: Boolean = true,
	bucketItemState: BucketItemState? = null,
	isSelected: Boolean = false,
	onLongClick: () -> Unit = {},
	onClick: () -> Unit = {}
) {
	val hapticFeedback = LocalHapticFeedback.current
	val isDarkTheme = LocalIsDarkTheme.current

	Column(
		horizontalAlignment = Alignment.Start,
		modifier = Modifier
			.fillMaxSize()
			.combinedClickable(
				onLongClick = { hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress); onLongClick() },
				onClick = onClick,
				interactionSource = remember { MutableInteractionSource() },
				indication = null
			)
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(0.6666f)
		) {
			BucketItemGridThumbnail(
				id = id,
				thumbnail = thumbnail,
				modifier = Modifier.padding(8.dp),
				onErrorIcon = when (bucketType) {
					BucketType.TODO -> R.drawable.ic_fa_bucket_todo
					BucketType.BOOK -> R.drawable.ic_fa_bucket_book
					BucketType.SHOW -> R.drawable.ic_fa_bucket_show
					BucketType.LINK -> R.drawable.ic_fa_bucket_link
					BucketType.UNKNOWN -> R.drawable.ic_fa_question
				}
			)

			androidx.compose.animation.AnimatedVisibility(
				visible = isSelected,
				enter = scaleIn(tween(ANIMATION_DURATION_MILLIS)),
				exit = scaleOut(tween(ANIMATION_DURATION_MILLIS)),
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
						tint = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier
							.requiredSize(24.dp)
							.align(Alignment.Center)
					)
				}
			}
		}

		if (bucketItemState != null) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 6.dp)
			) {
				Box(
					modifier = Modifier
						.weight(1f)
						.height(20.dp)
						.background(
							when (bucketItemState.name) {
								BucketItemState.ALPHA.name -> Color.LocationContainer.copy(alpha = if (isDarkTheme) 0.31f else 0.17f)
								BucketItemState.BETA.name -> Color.AttachmentContainer.copy(alpha = if (isDarkTheme) 0.31f else 0.17f)
								BucketItemState.GAMMA.name -> Color.LockClosedContainer.copy(alpha = if (isDarkTheme) 0.31f else 0.17f)
								else -> Color.Transparent
							},
							MaterialTheme.shapes.extraSmall
						)
				)
				if (isFavourite || isLocked) {
					Spacer(modifier = Modifier.width(4.dp))
					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier
							.height(20.dp)
							.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.47f), MaterialTheme.shapes.extraSmall)
							.padding(8.dp, 4.dp)
					) {
						if (isLocked) {
							Icon(
								painter = painterResource(id = R.drawable.ic_fa_lock_close_solid),
								contentDescription = stringResource(id = R.string.locked),
								tint = Color.LockClosedContainer,
								modifier = Modifier.requiredSize(12.dp)
							)
						}
						if (isFavourite && isLocked) {
							Text(
								text = "·",
								style = MaterialTheme.typography.bodySmall,
								fontWeight = FontWeight.Bold,
								maxLines = 1,
								modifier = Modifier.padding(horizontal = 2.dp)
							)
						}
						if (isFavourite) {
							Icon(
								painter = painterResource(id = R.drawable.ic_fa_heart_solid),
								contentDescription = stringResource(id = R.string.favourite),
								tint = Color.FavouriteContainer,
								modifier = Modifier.requiredSize(12.dp)
							)
						}
					}
				}
			}
			Spacer(modifier = Modifier.height(4.dp))
		}

		Text(
			text = title ?: stringResource(id = R.string.untitled),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
			fontStyle = if (title == null) FontStyle.Italic else FontStyle.Normal,
			maxLines = 3,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.padding(horizontal = 4.dp)
		)
	}
}

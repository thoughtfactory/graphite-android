package com.syncodec.graphite.presentation.bucket.composable.buildingBlock

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.BucketItemState
import com.syncodec.graphite.di.model.local.BucketType
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.base.FavouriteContainer
import com.syncodec.graphite.presentation.base.LockClosedContainer
import com.syncodec.graphite.presentation.common.selectable.SelectableContainer
import io.realm.kotlin.types.RealmUUID


@Preview
@Composable
fun BucketListItem(
	id: RealmUUID = RealmUUID.random(),
	bucketType: BucketType = BucketType.UNKNOWN,
	title: String? = "The Book Thief",
	description: String? = "The Book Thief tells the story of Liesel, a little girl who is taken to a new home because her mother can't afford to take care of her. The story is told by Death, who becomes a character you come to respect and even feel sorry for by the end. The narration puts an odd perspective on the story.",
	thumbnail: String? = null,
	dragHandle: @Composable () -> Unit = {},
	isLocked: Boolean = true,
	isFavourite: Boolean = true,
	bucketItemState: BucketItemState? = null,
	isSelected: Boolean = false,
	onClick: () -> Unit = {},
	onLongClick: () -> Unit = {},
) {
	val containerColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.47f),
		animationSpec = tween(ANIMATION_DURATION_MILLIS),
		label = "containerColor_animation"
	)
	val contentColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.47f),
		animationSpec = tween(ANIMATION_DURATION_MILLIS),
		label = "contentColor_animation"
	)

	SelectableContainer(
		selected = isSelected,
		onClick = onClick,
		onLongClick = onLongClick,
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 8.dp)
		) {
			dragHandle()

			Spacer(modifier = Modifier.width(8.dp))

			BucketItemListThumbnail(
				id = id,
				thumbnail = thumbnail,
				onErrorIcon = when (bucketType) {
					BucketType.TODO -> R.drawable.ic_fa_bucket_todo
					BucketType.BOOK -> R.drawable.ic_fa_bucket_book
					BucketType.SHOW -> R.drawable.ic_fa_bucket_show
					BucketType.LINK -> R.drawable.ic_fa_bucket_link
					BucketType.UNKNOWN -> R.drawable.ic_fa_question
				},
				containerColor = containerColor,
				contentColor = contentColor,
			)

			Spacer(modifier = Modifier.width(12.dp))

			Column(
				modifier = Modifier
					.weight(1f)
					.height(108.dp)
					.align(Alignment.Top)
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.fillMaxWidth()
				) {
					Text(
						text = if (title.isNullOrEmpty()) stringResource(id = R.string.untitled) else title,
						style = MaterialTheme.typography.bodyLarge,
						maxLines = 1,
						fontStyle = if (title.isNullOrBlank()) FontStyle.Italic else FontStyle.Normal,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier.weight(1f)
					)

					if (isFavourite || isLocked) {
						Spacer(modifier = Modifier.width(12.dp))
						Row(
							verticalAlignment = Alignment.CenterVertically,
							modifier = Modifier
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

				description?.let {
					Spacer(modifier = Modifier.height(4.dp))
					Text(
						text = it,
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
						maxLines = 6,
						overflow = TextOverflow.Ellipsis
					)
				}
			}
		}
	}
}

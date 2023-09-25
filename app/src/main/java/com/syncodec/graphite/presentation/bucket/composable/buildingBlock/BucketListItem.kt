package com.syncodec.graphite.presentation.bucket.composable.buildingBlock

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.selectable.SelectableContainer
import com.syncodec.graphite.presentation.base.FavouriteContainer
import com.syncodec.graphite.presentation.base.LockClosedContainer
import com.syncodec.graphite.utils.decodeBase64ToBitmap


@Preview
@Composable
fun BucketListItem(
	title: String? = "The Book Thief",
	description: String? = "The Book Thief tells the story of Liesel, a little girl who is taken to a new home because her mother can't afford to take care of her. The story is told by Death, who becomes a character you come to respect and even feel sorry for by the end. The narration puts an odd perspective on the story.",
	thumbnail: String? = null,
	dragHandle: @Composable () -> Unit = {},
	isLocked: Boolean = true,
	isFavourite: Boolean = true,
	isSelected: Boolean = false,
	onClick: () -> Unit = {},
	onLongClick: () -> Unit = {},
) {
	val context = LocalContext.current

	val thumbnail1 by remember(thumbnail) { derivedStateOf { thumbnail?.decodeBase64ToBitmap() } }

	val containerColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.47f),
		animationSpec = tween(470),
		label = "containerColor_animation"
	)
	val contentColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.47f),
		animationSpec = tween(470),
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
				.padding(horizontal = 16.dp, vertical = 12.dp)
		) {
			dragHandle()

			Spacer(modifier = Modifier.width(4.dp))

//			Thumbnail
			SubcomposeAsyncImage(
				model = ImageRequest.Builder(context)
					.data(thumbnail1)
					.crossfade(470)
					.build(),
				contentDescription = stringResource(id = R.string.thumbnail),
				error = {
					Box(
						contentAlignment = Alignment.Center,
						modifier = Modifier
							.fillMaxSize()
							.background(containerColor)
					) {
						Icon(
							painter = painterResource(id = R.drawable.ic_fa_bucket_book),
							contentDescription = stringResource(id = R.string.thumbnail),
							tint = contentColor,
							modifier = Modifier.requiredSize(32.dp)
						)
					}
				},
				contentScale = ContentScale.Crop,
				modifier = Modifier
					.width(72.dp)
					.aspectRatio(0.6666f)
					.clip(MaterialTheme.shapes.medium)
			)

			Spacer(modifier = Modifier.width(12.dp))

			Column(
				modifier = Modifier
					.weight(1f)
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
								.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.47f), MaterialTheme.shapes.small)
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
							if (isFavourite and isLocked) {
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

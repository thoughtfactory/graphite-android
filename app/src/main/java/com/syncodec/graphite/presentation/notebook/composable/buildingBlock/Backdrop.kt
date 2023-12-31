package com.syncodec.graphite.presentation.notebook.composable.buildingBlock

import androidx.annotation.FloatRange
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.base.LocalIsDarkTheme
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.common.component.composable.DotSeparator
import com.syncodec.graphite.presentation.notebook.composable.bar.COLLAPSED_TOP_BAR_HEIGHT
import com.syncodec.graphite.presentation.notebook.composable.bar.EXPANDED_TOP_BAR_HEIGHT
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import com.syncodec.graphite.utils.getInverseBWColor
import io.realm.kotlin.types.RealmUUID


@Preview
@Composable
fun Backdrop(
	id: RealmUUID? = null,
	chapterTitle: String? = null,
	chapterDescription: String? = null,
	chapterColor: Color? = null,
	thumbnail: String? = null,
	chapterCount: Int = 0,
	noteCount: Int = 0,
	isSelecting: Boolean = false,
	@FloatRange(from = 0.0, to = 1.0) offset: Float = 0f,
) {
	val context = LocalContext.current

	val isDarkTheme = LocalIsDarkTheme.current

	val bitmap by remember(thumbnail?.hashCode()) { derivedStateOf { thumbnail?.decodeBase64ToBitmap() } }

	val containerColor by animateColorAsState(
		targetValue = (if (isDarkTheme) chapterColor?.copy(alpha = 0.42f) else chapterColor) ?: MaterialTheme.colorScheme.background,
		label = "containerColor_animation"
	)
	val contentColor by animateColorAsState(
		targetValue = when {
			isDarkTheme -> MaterialTheme.colorScheme.onBackground
			chapterColor == null -> MaterialTheme.colorScheme.onBackground
			else -> chapterColor.getInverseBWColor()
		},
		label = "contentColor_animation"
	)

	val backgroundColor = MaterialTheme.colorScheme.background

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.background)
			.graphicsLayer { alpha = 1 - offset }
	) {
		Box(
			modifier = Modifier
				.background(MaterialTheme.colorScheme.background)
				.fillMaxWidth()
				.height(EXPANDED_TOP_BAR_HEIGHT),
			contentAlignment = Alignment.BottomStart
		) {
			SubcomposeAsyncImage(
				model = ImageRequest.Builder(context)
					.data(bitmap)
					.diskCachePolicy(CachePolicy.ENABLED)
					.memoryCachePolicy(CachePolicy.ENABLED)
					.diskCacheKey("${id}_${thumbnail.hashCode()}")
					.memoryCacheKey("${id}_${thumbnail.hashCode()}")
					.crossfade(ANIMATION_DURATION_MILLIS)
					.build(),
				error = {
					Crossfade(
						targetState = containerColor,
						modifier = Modifier.fillMaxSize(),
						label = "containerColor_transition"
					) {
						Box(
							modifier = Modifier
								.fillMaxSize()
								.background(it)
						)
					}
				},
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = Modifier
					.fillMaxSize()
					.graphicsLayer {
						scaleX = lerp(1.13f, 1.0f, offset)
						scaleY = lerp(1.13f, 1.0f, offset)
						translationY = -(this.size.height * offset * 0.42).toFloat()
					}
					.blur(radius = lerp(0.dp, 12.dp, offset))
			)

			Canvas(
				modifier = Modifier
					.fillMaxWidth()
					.height(EXPANDED_TOP_BAR_HEIGHT)
					.graphicsLayer { translationY = -(EXPANDED_TOP_BAR_HEIGHT - COLLAPSED_TOP_BAR_HEIGHT).toPx() * offset }
			) {
				val brush = Brush.verticalGradient(
					colors = listOf(
						when {
							isDarkTheme -> backgroundColor.copy(alpha = 0.13f)
							bitmap == null -> Color.Transparent
							else -> backgroundColor.copy(alpha = 0.13f)
						}, backgroundColor
					), startY = 0f, endY = size.height, tileMode = TileMode.Clamp
				)
				drawRect(brush)
			}

			Column(
				modifier = Modifier
					.padding(horizontal = 16.dp)
					.graphicsLayer {
						translationY = -(EXPANDED_TOP_BAR_HEIGHT - COLLAPSED_TOP_BAR_HEIGHT).toPx() * offset
						alpha = lerp(1f, 0f, minOf(offset * 1.7f, 1f))
					}
			) {
				Spacer(modifier = Modifier.height(4.dp))
				Row(
					modifier = Modifier.fillMaxWidth()
				) {
					AnimatedText(
						text = chapterTitle ?: stringResource(id = R.string.untitled),
						style = MaterialTheme.typography.headlineLarge,
						fontStyle = if (chapterTitle.isNullOrEmpty()) FontStyle.Italic else FontStyle.Normal,
						color = contentColor,
					)
					Spacer(modifier = Modifier.weight(1f))
					ChildCountInfo(
						chapterCount = chapterCount,
						noteCount = noteCount,
						containerColor = containerColor,
						contentColor = contentColor,
					)
				}

				chapterDescription?.let {
					Spacer(modifier = Modifier.height(4.dp))
					AnimatedText(
						text = it,
						style = MaterialTheme.typography.bodySmall,
						color = contentColor,
						modifier = Modifier.padding(start = 2.dp)
					)
				}

				Spacer(modifier = Modifier.height(48.dp))
			}
		}
	}
}

@Preview
@Composable
private fun ChildCountInfo(
	chapterCount: Int = 0,
	noteCount: Int = 0,
	containerColor: Color = MaterialTheme.colorScheme.background,
	contentColor: Color = MaterialTheme.colorScheme.onBackground,
) {
	Surface(
		color = containerColor.copy(alpha = 0.42f),
		contentColor = contentColor,
		shape = MaterialTheme.shapes.small,
		modifier = Modifier
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_fa_note_duotone),
				contentDescription = stringResource(id = R.string.note_count),
				modifier = Modifier.requiredSize(14.dp)
			)
			DotSeparator()
			Text(
				text = "$noteCount",
				style = MaterialTheme.typography.bodySmall,
				fontWeight = MaterialTheme.typography.bodySmall.fontWeight,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
			DotSeparator()
			Icon(
				painter = painterResource(id = R.drawable.ic_fa_notebook_duotone),
				contentDescription = stringResource(id = R.string.chapter_count),
				modifier = Modifier.requiredSize(14.dp)
			)
			DotSeparator()
			Text(
				text = "$chapterCount",
				style = MaterialTheme.typography.bodySmall,
				fontWeight = MaterialTheme.typography.bodySmall.fontWeight,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
		}
	}
}

package com.syncodec.graphite.presentation.common.component.chapter

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
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
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.base.LocalIsDarkTheme
import com.syncodec.graphite.presentation.common.component.composable.DotSeparator
import com.syncodec.graphite.presentation.common.component.composable.HeaderText
import com.syncodec.graphite.presentation.common.component.composable.StateInfo
import com.syncodec.graphite.presentation.common.selectable.SelectableContainer
import com.syncodec.graphite.presentation.common.selectable.SelectableContainerColors
import com.syncodec.graphite.presentation.common.selectable.SelectableContainerDefaults
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import io.realm.kotlin.types.RealmUUID


@Preview
@Composable
fun ChapterListCard(
	modifier: Modifier = Modifier,
	id: RealmUUID = RealmUUID.random(),
	createdTimestamp: Long? = null,
	modifiedTimestamp: Long? = null,
	title: String? = null,
	description: String? = null,
	isFavourite: Boolean = false,
	isLocked: Boolean = false,
	color: Color? = null,
	thumbnail: String? = null,
	noteCount: Int = 0,
	chapterCount: Int = 0,
	selected: Boolean = false,
	colors: SelectableContainerColors = SelectableContainerDefaults.selectableContainerColors(),
	onClick: () -> Unit = {},
	onLongClick: () -> Unit = {}
) {

	val context = LocalContext.current
	val isDarkTheme = LocalIsDarkTheme.current

	val thumbnailBitmap by remember(thumbnail?.hashCode()) { mutableStateOf(thumbnail?.decodeBase64ToBitmap()) }
	var isThumbnailLoaded by remember { mutableStateOf(false) }
	val overlayColor by animateColorAsState(
		targetValue = if (isThumbnailLoaded) Color.Black.copy(alpha = 0.17f) else color ?: Color.Transparent,
		animationSpec = tween(ANIMATION_DURATION_MILLIS),
		label = "overlayColor_animation"
	)

	val contentColor by colors.contentColor(selected = selected)

	SelectableContainer(
		shape = MaterialTheme.shapes.small,
		border = BorderStroke(1.dp, contentColor.copy(alpha = 0.31f)),
		selected = selected,
		colors = colors,
		onClick = onClick,
		onLongClick = onLongClick,
		modifier = modifier.padding(2.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(12.dp)
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(96.dp)
					.clip(MaterialTheme.shapes.small)
			) {
				SubcomposeAsyncImage(
					model = ImageRequest.Builder(context)
						.data(thumbnailBitmap)
						.diskCachePolicy(CachePolicy.ENABLED)
						.memoryCachePolicy(CachePolicy.ENABLED)
						.diskCacheKey(id.toString())
						.memoryCacheKey(id.toString())
						.build(),
					contentDescription = title,
					contentScale = ContentScale.Crop,
					onSuccess = { isThumbnailLoaded = true },
					modifier = Modifier.fillMaxSize()
				)

				Canvas(
					modifier = Modifier.fillMaxSize()
				) {
					val brush = Brush.linearGradient(
						colors = listOf(if (isDarkTheme) Color.Black.copy(alpha = 0.66f) else Color.Transparent, Color.Transparent),
						start = Offset(0f, 0f),
						end = Offset(size.width, size.height),
						tileMode = TileMode.Clamp
					)
					drawRect(color = overlayColor)
					drawRect(brush = brush)
				}

				StateInfo(
					isFavourite = isFavourite,
					isLocked = isLocked,
					modifier = Modifier
						.align(alignment = Alignment.BottomEnd)
						.padding(4.dp)
				) {
					DotSeparator()
					Icon(
						painter = painterResource(id = R.drawable.ic_fa_note_duotone),
						contentDescription = stringResource(id = R.string.note),
						tint = Color(0xFF016A70),
						modifier = Modifier.requiredSize(14.dp)
					)
					DotSeparator()
					HeaderText(text = "$noteCount")
					DotSeparator()
					Icon(
						painter = painterResource(id = R.drawable.ic_fa_notebook_duotone),
						contentDescription = stringResource(id = R.string.chapter),
						tint = Color(0xFF96B6C5),
						modifier = Modifier.requiredSize(14.dp)
					)
					DotSeparator()
					HeaderText(text = "$chapterCount")
				}
			}

			Spacer(modifier = Modifier.height(8.dp))

			Text(
				text = title ?: stringResource(id = R.string.untitled),
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Bold,
				fontStyle = if (title.isNullOrEmpty()) FontStyle.Italic else FontStyle.Normal,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
			)
		}
	}
}

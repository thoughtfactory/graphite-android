package com.syncodec.graphite.presentation.common.component.chapter

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.base.LockClosedContainer
import com.syncodec.graphite.presentation.common.component.composable.DotSeparator
import com.syncodec.graphite.presentation.common.component.composable.HeaderText
import com.syncodec.graphite.presentation.common.component.composable.StateInfo
import com.syncodec.graphite.presentation.common.selectable.SelectableContainer
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import com.syncodec.graphite.utils.getInverseBWColor
import io.realm.kotlin.types.RealmUUID


@Preview
@Composable
fun ChapterGridCard(
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
	onClick: () -> Unit = {},
	onLongClick: () -> Unit = {}
) {
	val context = LocalContext.current

	var isThumbnailLoaded by remember { mutableStateOf(false) }
	val overlayColor by animateColorAsState(
		targetValue = if (isThumbnailLoaded) Color.Black.copy(alpha = 0.17f) else Color.Transparent,
		animationSpec = tween(ANIMATION_DURATION_MILLIS),
		label = "overlayColor_animation"
	)

	SelectableContainer(
		shape = MaterialTheme.shapes.large,
		border = if (!selected) color?.let { BorderStroke(2.dp, it.copy(alpha = 0.471f)) } else null,
		selected = selected,
		onClick = onClick,
		onLongClick = onLongClick,
		modifier = modifier
			.fillMaxWidth()
			.height(144.dp)
	) {
		AnimatedVisibility(
			visible = !selected,
			enter = fadeIn(tween(ANIMATION_DURATION_MILLIS)),
			exit = fadeOut(tween(ANIMATION_DURATION_MILLIS)),
			label = "thumbnailSelected_visibility"
		) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				SubcomposeAsyncImage(
					model = ImageRequest.Builder(context)
						.data(thumbnail?.decodeBase64ToBitmap())
						.diskCachePolicy(CachePolicy.ENABLED)
						.memoryCachePolicy(CachePolicy.ENABLED)
						.diskCacheKey(id.toString())
						.memoryCacheKey(id.toString())
						.build(),
					contentDescription = title,
					contentScale = ContentScale.Crop,
					onSuccess = { isThumbnailLoaded = true },
					modifier = Modifier
						.fillMaxSize()
						.background(color?.copy(alpha = 0.13f) ?: MaterialTheme.colorScheme.background)
						.blur(12.dp)
				)
			} else {
				SubcomposeAsyncImage(
					model = ImageRequest.Builder(context)
						.data(thumbnail?.decodeBase64ToBitmap())
						.diskCachePolicy(CachePolicy.ENABLED)
						.memoryCachePolicy(CachePolicy.ENABLED)
						.diskCacheKey(id.toString())
						.memoryCacheKey(id.toString())
						.build(),
					contentDescription = title,
					contentScale = ContentScale.Crop,
					onSuccess = { isThumbnailLoaded = true },
					modifier = Modifier
						.fillMaxSize()
						.background(color?.copy(alpha = 0.31f) ?: MaterialTheme.colorScheme.background)
				)
			}
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(overlayColor)
			)
		}

		Content(
			title = title,
			description = description,
			isFavourite = isFavourite,
			isLocked = isLocked,
			noteCount = noteCount,
			chapterCount = chapterCount,
			contentColor = when {
				selected -> MaterialTheme.colorScheme.onSurface
				isThumbnailLoaded -> Color.White
				else -> color ?: MaterialTheme.colorScheme.onBackground
			},
		)
	}
}

@Preview
@Composable
private fun Content(
	title: String? = null,
	description: String? = null,
	isFavourite: Boolean = false,
	isLocked: Boolean = false,
	noteCount: Int = 0,
	chapterCount: Int = 0,
	contentColor: Color = Color.White,
) {
	Column(
		modifier = Modifier.padding(8.dp)
	) {
		Text(
			text = title ?: stringResource(id = R.string.untitled),
			style = MaterialTheme.typography.titleLarge,
			fontWeight = FontWeight.Bold,
			color = contentColor,
		)
		Spacer(modifier = Modifier.height(2.dp))
		Text(
			text = description ?: stringResource(id = R.string.no_description),
			style = MaterialTheme.typography.bodyMedium,
			fontStyle = if (description == null) FontStyle.Italic else FontStyle.Normal,
			color = contentColor,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.weight(1f)
		)
		Spacer(modifier = Modifier.height(4.dp))
		StateInfo(
			isFavourite = isFavourite,
			isLocked = isLocked,
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
}

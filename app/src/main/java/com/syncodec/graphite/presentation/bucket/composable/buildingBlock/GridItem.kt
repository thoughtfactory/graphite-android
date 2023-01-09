package com.syncodec.graphite.presentation.bucket.composable.buildingBlock

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.utils.decodeBase64ToBitmap


@Preview
@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun GridItem(
	title : String? = null,
	thumbnail : String? = null,
	isSelected : Boolean = false,
	onLongClick : () -> Unit = {},
	onClick : () -> Unit = {}
) {
	val context = LocalContext.current
	var _thumbnail by remember { mutableStateOf<Bitmap?>(null) }
	LaunchedEffect(key1 = thumbnail.hashCode()) {
		_thumbnail = thumbnail?.decodeBase64ToBitmap()
	}

	var isError by remember { mutableStateOf(false) }

	Column(
		horizontalAlignment = Alignment.Start,
		modifier = Modifier.fillMaxSize()
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(0.75f)
				.combinedClickable(
					onLongClick = onLongClick,
					onClick = onClick,
					interactionSource = remember { MutableInteractionSource() },
					indication = null
				)
		) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(8.dp)
					.background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
					.clip(MaterialTheme.shapes.medium),
				contentAlignment = Alignment.Center
			) {
				AnimatedContent(targetState = isError) {
					if (it) {
						Text(
							text = "Image unavailable",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onSurface,
							modifier = Modifier
								.align(Alignment.Center)
								.padding(8.dp, 0.dp)
						)
					} else Thumbnail(thumbnail = _thumbnail) { isError = it }
				}
			}

			androidx.compose.animation.AnimatedVisibility(
				visible = isSelected,
				enter = scaleIn(tween(300)),
				exit = scaleOut(tween(300)),
				modifier = Modifier.align(Alignment.TopEnd)
			) {
				Box(
					modifier = Modifier
						.requiredSize(32.dp)
						.align(Alignment.TopEnd)
						.background(MaterialTheme.colorScheme.background, CircleShape)
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_check_circle),
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier
							.requiredSize(24.dp)
							.align(Alignment.Center)
					)
				}
			}
		}

		Text(
			text = title ?: "Untitled",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
			fontStyle = if (title == null) FontStyle.Italic else FontStyle.Normal,
			maxLines = 2,
			overflow = TextOverflow.Ellipsis,
		)
	}
}

@Composable
private fun Thumbnail(
	thumbnail : Bitmap? = null,
	onResult : (Boolean) -> Unit = {}
) {
	val context = LocalContext.current

	AsyncImage(
		model = ImageRequest.Builder(context)
			.data(thumbnail)
			.build(),
		placeholder = null,
		contentDescription = null,
		onError = { onResult(true) },
		onSuccess = { onResult(false) },
		contentScale = ContentScale.Crop,
		modifier = Modifier.fillMaxSize(),
	)
}

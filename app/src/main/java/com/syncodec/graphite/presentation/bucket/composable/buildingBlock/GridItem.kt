package com.syncodec.graphite.presentation.bucket.composable.buildingBlock

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.utils.decodeBase64ToBitmap


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridItem(
	title : String?,
	thumbnail : String?,
	isSelected : Boolean,
	onLongClick : () -> Unit,
	onClick : () -> Unit
) {
	val context = LocalContext.current

	val borderColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
		animationSpec = tween(300)
	)
	val scaleContent by animateFloatAsState(targetValue = if (isSelected) 0.9f else 1f)

//	var _thumbnail by remember { mutableStateOf<Bitmap?>(null) }
//	LaunchedEffect(key1 = thumbnail.hashCode()) {
//		_thumbnail = thumbnail?.decodeBase64ToBitmap()
//	}

	var isError by remember { mutableStateOf(true) }

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier.padding(8.dp)
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(0.75f)
				.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
				.border(2.dp, borderColor, RoundedCornerShape(12.dp))
				.clip(RoundedCornerShape(12.dp))
				.combinedClickable(
					onClick = { onClick() },
					onLongClick = { onLongClick() }
				),
		) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.clip(RoundedCornerShape(12.dp))
					.graphicsLayer {
						scaleX = scaleContent
						scaleY = scaleContent
						shape = RoundedCornerShape(12.dp)
					},
				contentAlignment = Alignment.Center
			) {
				AsyncImage(
					model = ImageRequest.Builder(context)
						.data(thumbnail?.decodeBase64ToBitmap())
						.error(R.drawable.ic_show)
						.fallback(R.drawable.ic_show)
//						.crossfade(300)
						.build(),
					placeholder = null,
					contentDescription = null,
					onError = { isError = true },
					onSuccess = { isError = false },
					contentScale = ContentScale.Crop,
					modifier = if (isError) {
						Modifier
							.requiredSize(32.dp)
							.clip(RoundedCornerShape(12.dp))
					} else Modifier
						.fillMaxSize()
						.clip(RoundedCornerShape(12.dp)),
				)
			}
		}

		Spacer(modifier = Modifier.height(4.dp))

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

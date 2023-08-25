package com.syncodec.graphite.presentation.attachment.composable.buildingBlock

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.utils.FilePreview.Companion.preview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AttachmentCard(
	file: File,
	isSelected: Boolean = false,
	onClick: () -> Unit = {},
	onLongClick: () -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	var isPreviewAvailable by remember { mutableStateOf(null as Boolean?) }

	val filePreview by remember { derivedStateOf { file.preview(context = context) } }
	val imageBitmap by remember(filePreview) { derivedStateOf { filePreview.first } }
	val imageOverlay by remember(filePreview) { derivedStateOf { filePreview.second } }

	DisposableEffect(key1 = file) { onDispose { scope.cancel(); imageBitmap?.recycle() } }

	val padding by animateDpAsState(
		targetValue = if (isSelected) 12.dp else 0.dp,
		animationSpec = tween(470),
		label = "padding_animation"
	)

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.aspectRatio(1f)
	) {
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.fillMaxSize()
				.padding(padding)
				.background(
					MaterialTheme.colorScheme
						.surfaceColorAtElevation(8.dp)
						.copy(alpha = 0.13f)
				)
				.combinedClickable(
					enabled = true,
					onClick = onClick,
					onLongClick = onLongClick,
				),
		) {
			SubcomposeAsyncImage(
				model = ImageRequest.Builder(context)
					.data(imageBitmap)
					.crossfade(300)
					.build(),
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = Modifier
					.fillMaxSize()
					.blur(32.dp)
			)

			SubcomposeAsyncImage(
				model = ImageRequest.Builder(context)
					.data(imageBitmap)
					.crossfade(300)
					.build(),
				contentDescription = "Attachment",
				loading = { LoadingView() },
				error = {
					Column(
						horizontalAlignment = Alignment.CenterHorizontally,
						verticalArrangement = Arrangement.Center,
						modifier = Modifier.fillMaxSize(),
					) {
						Icon(
							painter = painterResource(id = R.drawable.ic_fa_file),
							contentDescription = null,
							tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f),
							modifier = Modifier.requiredSize(64.dp)
						)
						Spacer(modifier = Modifier.height(8.dp))
						Text(
							text = "Preview unavailable",
							style = MaterialTheme.typography.bodyLarge,
							color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f),
						)
					}
				},
				modifier = Modifier.fillMaxSize()
			)
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
					tint = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier
						.requiredSize(24.dp)
						.align(Alignment.Center)
				)
			}
		}
	}
}

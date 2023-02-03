package com.syncodec.graphite.presentation.common.attachment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.utils.getPreview
import com.syncodec.graphite.utils.icon
import com.syncodec.graphite.utils.mimeSubTypeIconMap
import com.syncodec.graphite.utils.mimeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


@Composable
fun AttachmentPreview(uri : Uri) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	var bitmap by remember { mutableStateOf<Bitmap?>(null) }
	LaunchedEffect(key1 = uri) { scope.launch(Dispatchers.IO) { bitmap = uri.getPreview(context) } }

	var errorIcon by remember { mutableStateOf<Int?>(null) }

	Crossfade(
		targetState = errorIcon,
		animationSpec = tween(300),
		modifier = Modifier.fillMaxSize()
	) {
		it?.let {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier.fillMaxSize()
			) {
				Icon(
					painter = painterResource(id = it),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier.requiredSize(48.dp)
				)
			}
		} ?: AsyncImage(
			model = ImageRequest.Builder(context)
				.data(bitmap)
				.crossfade(300)
				.listener(onError = { _, _ -> errorIcon = uri.icon(context) })
				.build(),
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = Modifier.fillMaxSize()
		)
	}
}

@Composable
fun AttachmentPreview(file : File) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	var bitmap by remember { mutableStateOf<Bitmap?>(null) }
	LaunchedEffect(key1 = file) { scope.launch(Dispatchers.IO) { bitmap = file.toUri().getPreview(context) } }

	var errorIcon by remember { mutableStateOf<Int?>(null) }

	Crossfade(
		targetState = errorIcon,
		animationSpec = tween(300),
		modifier = Modifier.fillMaxSize()
	) {
		it?.let {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier.fillMaxSize()
			) {
				Icon(
					painter = painterResource(id = it),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier.requiredSize(48.dp)
				)
			}
		} ?: AsyncImage(
			model = ImageRequest.Builder(context)
				.data(bitmap)
				.crossfade(300)
				.listener(onError = { _, _ -> errorIcon = file.toUri().icon(context) })
				.build(),
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = Modifier.fillMaxSize()
		)
	}
}

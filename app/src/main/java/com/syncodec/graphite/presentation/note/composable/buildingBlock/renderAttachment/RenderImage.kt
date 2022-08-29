package com.syncodec.graphite.presentation.note.composable.buildingBlock.renderAttachment

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R


@Composable
fun RenderImage(
	type: String?,
	name: String?,
	uri: Uri?
) {
	val context = LocalContext.current
	var isError by rememberSaveable { mutableStateOf(false) }

	Crossfade(targetState = isError) {
		if (it) {
			Icon(
				painter = painterResource(id = R.drawable.ic_gallery),
				contentDescription = name,
				tint = MaterialTheme.colorScheme.onSurface,
				modifier = Modifier.requiredSize(48.dp)
			)
		} else {
			Box(
				modifier = Modifier.fillMaxSize(),
				contentAlignment = Alignment.Center
			) {
				AsyncImage(
					model = ImageRequest.Builder(context)
						.data(uri)
						.crossfade(300)
						.build(),
					onError = { isError = true },
					contentDescription = name,
					contentScale = ContentScale.Crop,
					modifier = Modifier
						.fillMaxSize()
						.blur(32.dp, BlurredEdgeTreatment.Rectangle),
				)

				AsyncImage(
					model = ImageRequest.Builder(context)
						.data(uri)
						.crossfade(300)
						.build(),
					onError = { isError = true },
					contentDescription = name,
					contentScale = ContentScale.Fit,
					modifier = Modifier.fillMaxHeight(),
				)
			}
		}
	}
}

package com.syncodec.graphite.presentation.common.attachment

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.attachment.previewer.PreviewData


@Composable
fun ImageAttachmentPreview(
	previewData: PreviewData.Image,
	blur : Boolean = true,
) {
	if (blur && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)) AsyncImage(
		model = previewData.drawable,
		contentDescription = stringResource(R.string.thumbnail),
		contentScale = ContentScale.Crop,
		modifier = Modifier
			.fillMaxSize()
			.blur(24.dp)
	) else Box(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.47f))
	)

	AsyncImage(
		model = previewData.drawable,
		contentDescription = stringResource(R.string.thumbnail),
		contentScale = if (blur) ContentScale.Fit else ContentScale.Crop,
		modifier = Modifier.fillMaxSize()
	)
}

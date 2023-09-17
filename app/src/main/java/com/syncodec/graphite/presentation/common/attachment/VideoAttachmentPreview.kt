package com.syncodec.graphite.presentation.common.attachment

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.attachment.previewer.PreviewData


@Composable
fun VideoAttachmentPreview(
	previewData: PreviewData.Video
) {
	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier.fillMaxSize()
	) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) AsyncImage(
			model = previewData.drawable,
			contentDescription = "stringResource(R.string.description)",
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
			contentDescription = "stringResource(R.string.description)",
			contentScale = ContentScale.Fit,
			modifier = Modifier.fillMaxSize()
		)

		Icon(
			painter = painterResource(id = R.drawable.ic_fa_circle_play_duotone),
			contentDescription = "Play button",
			tint = Color.White.copy(alpha = 1f),
			modifier = Modifier.requiredSize(48.dp)
		)
	}
}

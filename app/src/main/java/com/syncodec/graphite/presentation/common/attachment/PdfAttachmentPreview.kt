package com.syncodec.graphite.presentation.common.attachment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.syncodec.graphite.presentation.common.attachment.previewer.PreviewData


@Composable
fun PdfAttachmentPreview(previewData: PreviewData.Pdf) {
	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier.fillMaxSize()
	) {
		AsyncImage(
			model = previewData.bitmap,
			contentDescription = "stringResource(R.string.description)",
			contentScale = ContentScale.Crop,
			modifier = Modifier.fillMaxSize()
		)
	}
}

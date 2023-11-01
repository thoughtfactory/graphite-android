package com.syncodec.graphite.presentation.common.attachment

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.attachment.previewer.FilePreviewer
import com.syncodec.graphite.presentation.common.attachment.previewer.PreviewData
import com.syncodec.graphite.presentation.common.attachment.previewer.UriPreviewer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AttachmentPreview(
	modifier: Modifier = Modifier,
	file: File,
	showFileName: Boolean = false,
	onClick: () -> Unit = {},
	onLongClick : (() -> Unit)? = null
) {
	val context = LocalContext.current
	var previewData by remember { mutableStateOf<PreviewData>(PreviewData.Init) }
	LaunchedEffect(key1 = file) {
		withContext(Dispatchers.IO) {
			previewData = PreviewData.Loading
			previewData = FilePreviewer.getPreview(file = file, context = context)
			Log.d("npr71", "file : ${file.name} : ${previewData::class.simpleName}")
		}
	}

	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.31f))
			.combinedClickable(onClick = onClick, onLongClick = onLongClick)
	) {
		previewData.let { previewData1 ->
			when (previewData1) {
				is PreviewData.Init -> LoadingView()
				is PreviewData.Loading -> LoadingView()
				is PreviewData.Image -> ImageAttachmentPreview(previewData = previewData1)
				is PreviewData.Video -> VideoAttachmentPreview(previewData = previewData1)
				is PreviewData.Pdf -> PdfAttachmentPreview(previewData = previewData1)
				else -> UnknownAttachmentPreview(previewData = previewData)
			}
		}
		if (showFileName) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.align(Alignment.BottomCenter)
					.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.71f))
					.padding(horizontal = 8.dp, vertical = 8.dp)
			) {
				Text(
					text = previewData.name ?: "No file name",
					style = MaterialTheme.typography.bodySmall,
					fontWeight = FontWeight.Bold,
					maxLines = 3,
					overflow = TextOverflow.Ellipsis,
				)
			}
		}
	}
}

@Composable
fun AttachmentPreview(
	uri: Uri,
	showFileName: Boolean = false,
	onClick: () -> Unit = {},
) {
	val context = LocalContext.current
	var previewData by remember { mutableStateOf<PreviewData>(PreviewData.Init) }
	LaunchedEffect(key1 = uri) {
		withContext(Dispatchers.IO) {
			previewData = PreviewData.Loading
			previewData = UriPreviewer.getPreview(uri = uri, context = context)
		}
	}

	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.31f))
			.clickable { onClick() }
	) {
		previewData.let { previewData1 ->
			when (previewData1) {
				is PreviewData.Init -> LoadingView()
				is PreviewData.Loading -> LoadingView()
				is PreviewData.Image -> ImageAttachmentPreview(previewData = previewData1)
				is PreviewData.Video -> VideoAttachmentPreview(previewData = previewData1)
				is PreviewData.Pdf -> PdfAttachmentPreview(previewData = previewData1)
				else -> UnknownAttachmentPreview(previewData = previewData)
			}
		}
		if (showFileName) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.align(Alignment.BottomCenter)
					.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.71f))
					.padding(horizontal = 8.dp, vertical = 8.dp)
			) {
				Text(
					text = previewData.name ?: "No file name",
					style = MaterialTheme.typography.bodySmall,
					fontWeight = FontWeight.Bold,
					maxLines = 3,
					overflow = TextOverflow.Ellipsis,
				)
			}
		}
	}
}

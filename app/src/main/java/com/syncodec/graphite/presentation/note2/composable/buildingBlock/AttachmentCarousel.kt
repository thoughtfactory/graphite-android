package com.syncodec.graphite.presentation.note2.composable.buildingBlock

import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.GenericButton
import com.syncodec.graphite.utils.share
import com.syncodec.graphite.utils.viewExternally
import java.io.File


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun AttachmentCarousel(
	modifier: Modifier = Modifier,
	fileList: List<File> = listOf()
) {
	val pagerState = rememberPagerState { fileList.size }

	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		HorizontalPager(
			state = pagerState,
			pageSpacing = 8.dp,
			modifier = modifier
		) {
			AttachmentPreview(
				file = fileList[it]
			)
		}
		AttachmentName(
			file = fileList[pagerState.currentPage]
		)
	}
}

@Preview
@Composable
private fun AttachmentPreview(
	file: File = File("")
) {
	val context = LocalContext.current
	val imageRequest = remember(file) {
		ImageRequest.Builder(context)
			.data(file.toUri())
			.crossfade(true)
			.build()
	}
	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxSize()
			.clickable { file.viewExternally(context = context) }
	) {
		if (Build.VERSION.SDK_INT >= VERSION_CODES.S) AsyncImage(
			model = imageRequest,
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
			model = imageRequest,
			contentDescription = "stringResource(R.string.description)",
			contentScale = ContentScale.Fit,
			modifier = Modifier.fillMaxSize()
		)
	}
}

@Preview
@Composable
private fun AttachmentName(
	file: File? = File("")
) {
	val context = LocalContext.current

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(48.dp)
			.background(MaterialTheme.colorScheme.surface)
			.padding(horizontal = 12.dp, vertical = 8.dp)
	) {
		Text(
			text = file?.name ?: "",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.weight(1f)
		)
		Spacer(modifier = Modifier.width(8.dp))
		GenericButton(
			icon = R.drawable.ic_fa_share
		) { file?.share(context = context) }
	}
}

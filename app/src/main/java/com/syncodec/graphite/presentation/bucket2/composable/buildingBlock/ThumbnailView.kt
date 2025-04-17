package com.syncodec.graphite.presentation.bucket2.composable.buildingBlock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemData
import com.syncodec.graphite.utils.alice2.Alice2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject


@Composable
fun GridThumbnailPreview(
    thumbnailFile: BucketItemData.Companion.Thumbnail.File?,
) {
    val context = LocalContext.current
    val alice2: Alice2 = koinInject()

    var bitmapByteArray: ByteArray? by remember { mutableStateOf(null) }
    LaunchedEffect(key1 = thumbnailFile) { withContext(context = Dispatchers.Default) { bitmapByteArray = thumbnailFile?.getAndDecryptFile(context, alice2) } }

    if (thumbnailFile != null) Box(modifier = Modifier) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(data = bitmapByteArray)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            loading = { CircularProgressIndicator(modifier = Modifier.requiredSize(size = 32.dp), strokeWidth = 2.dp) },
            modifier = Modifier.fillMaxSize()
        )
    } else Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface)
    ) {
        Text(
            text = stringResource(id = R.string.no_cover_image),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
fun ListThumbnailPreview(
    thumbnailFile: BucketItemData.Companion.Thumbnail.File?,
) {
    val context = LocalContext.current
    val alice2: Alice2 = koinInject()

    var bitmapByteArray: ByteArray? by remember { mutableStateOf(value = null) }
    LaunchedEffect(key1 = thumbnailFile) { withContext(context = Dispatchers.Default) { bitmapByteArray = thumbnailFile?.getAndDecryptFile(context, alice2) } }

    if (thumbnailFile != null) SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(data = bitmapByteArray)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        alignment = Alignment.Center,
        loading = { CircularProgressIndicator(modifier = Modifier.requiredSize(size = 32.dp), strokeWidth = 2.dp) },
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small)
            .clip(shape = MaterialTheme.shapes.small)
    ) else Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small)
            .clip(shape = MaterialTheme.shapes.large)
    ) {
        Text(
            text = stringResource(id = R.string.no_cover_image),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

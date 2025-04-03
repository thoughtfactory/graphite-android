package com.syncodec.graphite.presentation.bucketItem2.composable.buildingBlock

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.importer.ThumbnailData
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import com.syncodec.graphite.utils.DataLoader
import com.syncodec.graphite.utils.alice2.Alice2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject


@Composable
fun ThumbnailView(
    thumbnailDataFlow: StateFlow<DataLoader<ThumbnailData>>,
    isEditing: Boolean = false,
    onUpdateThumbnail: (Uri) -> Unit = {}
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val thumbnailData by thumbnailDataFlow.collectAsState()
    val thumbnailBitmap by remember(key1 = (thumbnailData as? DataLoader.Loaded)?.data?.hashCode()) { derivedStateOf { (thumbnailData as? DataLoader.Loaded)?.data } }
    Log.d("ThumbnailLoadedView", "ThumbnailView")

    val thumbnailPicker = rememberLauncherForActivityResult(contract = PickVisualMedia()) { uri ->
        if (uri == null) Toast.makeText(context, context.getString(R.string.toast_no_image_selected), Toast.LENGTH_SHORT).show()
        else onUpdateThumbnail(uri)
    }

    thumbnailData.let { thumbnailData1 ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(width = screenWidth / 2.5f)
                .aspectRatio(ratio = 0.675f)
                .background(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large)
                .clip(shape = MaterialTheme.shapes.large)
                .border(border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f)), shape = MaterialTheme.shapes.large)
                .clickable(enabled = isEditing) { thumbnailPicker.launch(input = PickVisualMediaRequest(mediaType = PickVisualMedia.ImageOnly)) }
        ) {
            Log.d("ThumbnailLoadedView", "thumbnailData1")
            when (thumbnailData1::class.simpleName) {
                DataLoader.Init::class.simpleName -> Unit
                DataLoader.Loading::class.simpleName -> LoadingView()
                DataLoader.Error::class.simpleName -> NoCoverView()
                DataLoader.NoData::class.simpleName -> NoCoverView()
                DataLoader.Loaded::class.simpleName -> thumbnailBitmap?.let { ThumbnailLoadedView(thumbnailData = it) } ?: NoCoverView()
            }
        }
    }
}

@Composable
private fun LoadingView() {
    CircularProgressIndicator(modifier = Modifier.requiredSize(size = 32.dp), strokeWidth = 2.dp)
}

@Composable
private fun NoCoverView() {
    Text(
        text = stringResource(id = R.string.no_cover_image_set_new),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f),
        modifier = Modifier.padding(horizontal = 12.dp)
    )
}

@Composable
private fun ThumbnailLoadedView(
    thumbnailData: ThumbnailData
) {
    val context = LocalContext.current
    val alice2: Alice2 = koinInject()

    var data: Any? by remember { mutableStateOf(null) }
    LaunchedEffect(key1 = thumbnailData) {
        withContext(context = Dispatchers.Default) {
            data = when (thumbnailData) {
                is ThumbnailData.Bitmap -> thumbnailData.data
                is ThumbnailData.Base64 -> thumbnailData.getAsBitmap()
                is ThumbnailData.File -> thumbnailData.data
                is ThumbnailData.EncryptedFile -> thumbnailData.getAndDecryptFile(context, alice2)
            }
        }

        Log.d("npr71", "thumbnailData : ${thumbnailData::class.simpleName}")
    }

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context = context)
            .data(data = data)
            .crossfade(durationMillis = AnimationDefaults.ANIMATION_TIME)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        alignment = Alignment.Center,
        loading = { CircularProgressIndicator(modifier = Modifier.requiredSize(size = 32.dp), strokeWidth = 2.dp) },
        modifier = Modifier.fillMaxSize()
    )
}

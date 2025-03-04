package com.syncodec.graphite.presentation.main2.composable.buildingBlock.notebook

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.customObject.Thumbnail
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import com.syncodec.graphite.utils.getInverseBWColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi
import okio.ByteString.Companion.decodeBase64


@OptIn(InternalSerializationApi::class)
@Composable
@Preview
fun NotebookCard(
    modifier: Modifier = Modifier,
    titleText: String? = null,
    descriptionText: String? = null,
    thumbnail: Thumbnail? = null
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val borderColor = if (thumbnail is Thumbnail.Color) Color(color = thumbnail.argbValue) else MaterialTheme.colorScheme.surface
    val containerColor = Color(ColorUtils.blendARGB(borderColor.toArgb(), backgroundColor.toArgb(), 0.371f))
    val contentColor = MaterialTheme.colorScheme.onBackground
    val borderContentColor = borderColor.getInverseBWColor()

    OutlinedCard(
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor, contentColor = contentColor),
        border = BorderStroke(width = 2.dp, color = borderColor),
        shape = MaterialTheme.shapes.large,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (thumbnail is Thumbnail.Image) NotebookThumbnail(thumbnail = thumbnail)

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(all = 16.dp)
                ) {
                    Text(
                        text = titleText ?: stringResource(id = R.string.no_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = borderContentColor
                    )
                    Spacer(modifier = Modifier.height(height = 4.dp))
                    Text(
                        text = descriptionText ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = borderContentColor
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = borderColor)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    MetaInfo(chapterListSize = 12, noteListSize = 16, contentColor = borderContentColor)
                }
            }
        }
    }
}

@OptIn(InternalSerializationApi::class)
@Composable
private fun NotebookThumbnail(thumbnail: Thumbnail.Image) {
    val context = LocalContext.current
    var data: Bitmap? by remember { mutableStateOf(null) }
    LaunchedEffect(key1 = thumbnail.base64String) {
        withContext(context = Dispatchers.IO) {
//            val bitmap = thumbnail.base64String.decodeBase64ToBitmap()
            data = thumbnail.base64String.decodeBase64ToBitmap()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(data = data)
                .crossfade(durationMillis = AnimationDefaults.ANIMATION_TIME)
                .allowHardware(enable = true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 16.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.171f))
        )
    }
}

@Composable
private fun MetaInfo(
    chapterListSize: Int,
    noteListSize: Int,
    contentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = painterResource(R.drawable.ic_fa_note_duotone), contentDescription = null, tint = contentColor, modifier = Modifier.size(size = 16.dp))
        Spacer(modifier = Modifier.width(width = 6.dp))
        Text(text = "$chapterListSize", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = contentColor)

        MetaInfoSeparator(color = contentColor)

        Icon(painter = painterResource(R.drawable.ic_fa_notebook_duotone), contentDescription = null, tint = contentColor, modifier = Modifier.size(size = 16.dp))
        Spacer(modifier = Modifier.width(width = 6.dp))
        Text(text = "$noteListSize", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = contentColor)
    }
}

@Composable
private fun MetaInfoSeparator(color: Color) {
    Text(
        text = "•",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier.padding(horizontal = 10.dp)
    )
}

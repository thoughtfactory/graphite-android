package com.syncodec.graphite.presentation.main2.composable.buildingBlock

import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.cheonjaeung.compose.grid.SimpleGridCells
import com.cheonjaeung.compose.grid.VerticalGrid
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.customObject.Thumbnail
import com.syncodec.graphite.presentation.main2.composable.bottomSheet.SelectedThumbnail
import com.syncodec.graphite.presentation.ui.ANIMATION_TIME
import com.syncodec.graphite.presentation.ui.ICON_SIZE
import com.syncodec.graphite.utils.encodeBase64
import com.syncodec.graphite.utils.getInverseBWColor
import kotlinx.serialization.InternalSerializationApi


val ChapterCoverImageList: List<Int> = listOf(
    R.drawable.img_1,
    R.drawable.img_2,
    R.drawable.img_3,
    R.drawable.img_4,
    R.drawable.img_5,
    R.drawable.img_6,
    R.drawable.img_7,
)


@OptIn(ExperimentalLayoutApi::class, InternalSerializationApi::class)
@Composable
fun CoverImageSelector(
    selectedThumbnail: SelectedThumbnail,
    onSelectImage: (SelectedThumbnail) -> Unit
) {

    val context = LocalContext.current

    VerticalGrid(
        columns = SimpleGridCells.Adaptive(80.dp),
    ) {
        CustomImagePicker(selectedThumbnail = selectedThumbnail, onSelectImage = onSelectImage)

        ChapterCoverImageList.forEach { image ->
            val isSelected = selectedThumbnail is SelectedThumbnail.ResourceImage && selectedThumbnail.value == image
            val padding by animateDpAsState(if (isSelected) 2.dp else 8.dp)
            val overlayColor by animateColorAsState(if (isSelected) Color.Black.copy(alpha = 0.31f) else Color.Transparent)

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
                    .padding(padding)
                    .clip(MaterialTheme.shapes.small)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(image)
                        .crossfade(300)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            onSelectImage(SelectedThumbnail.ResourceImage(value = image))
//                            TODO("bont convert to bitmap everytime")
//                            val bitmap = BitmapFactory.decodeResource(context.resources, image)
//                            val aspectRatio = if (bitmap != null) bitmap.width.toFloat() / bitmap.height.toFloat() else 1f
//                            val imageBase64 = bitmap?.let { ThumbnailUtils.extractThumbnail(it, (192 * aspectRatio).toInt(), 192) }?.encodeBase64()
//                            onSelectImage(Thumbnail.Image(imageBase64!!))
                        }
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(overlayColor),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedVisibility(
                        visible = isSelected,
                        enter = scaleIn(tween(ANIMATION_TIME), 0.71f) + fadeIn(tween(ANIMATION_TIME)),
                        exit = scaleOut(tween(ANIMATION_TIME), 0.71f) + fadeOut(tween(ANIMATION_TIME))
                    ) {
                        Icon(painter = painterResource(R.drawable.ic_check), tint = Color.White, contentDescription = null)
                    }
                }
            }
        }
    }
}

@OptIn(InternalSerializationApi::class)
@Composable
private fun CustomImagePicker(
    selectedThumbnail: SelectedThumbnail,
    onSelectImage: (SelectedThumbnail) -> Unit
) {
    val context = LocalContext.current
    val pickMedia = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri == null) Toast.makeText(context, context.getString(R.string.toast_no_image_selected), Toast.LENGTH_SHORT).show()
        else onSelectImage(SelectedThumbnail.CustomImage(uri = uri))
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.71f), MaterialTheme.shapes.small)
            .clip(MaterialTheme.shapes.small)
            .clickable { pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) }
    ) {
        if (selectedThumbnail is SelectedThumbnail.CustomImage) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(selectedThumbnail.uri)
                    .crossfade(300)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.31f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = painterResource(R.drawable.ic_check), tint = Color.White, contentDescription = null)
            }
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_fa_image),
                contentDescription = stringResource(R.string.select_notebook_cover_image),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.requiredSize(ICON_SIZE)
            )
        }
    }
}

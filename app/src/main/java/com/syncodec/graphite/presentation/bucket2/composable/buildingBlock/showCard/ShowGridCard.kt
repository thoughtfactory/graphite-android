package com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.showCard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemData
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemShow
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.StatusContainer
import com.syncodec.graphite.presentation.common.v2.selectable2.LocalSelectionContainerActor
import com.syncodec.graphite.presentation.common.v2.selectable2.SelectableContainer2
import com.syncodec.graphite.presentation.common.v2.selectable2.SelectableContainer2Defaults
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import com.syncodec.graphite.utils.alice2.Alice2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject


@Composable
fun ShowGridCard(
    bucketItemBox: BucketItemBoxDecrypted?,
    bucketItemShow: BucketItemShow? = null,
    isReorderable: Boolean = false,
    dragHandle: @Composable () -> Unit = {},
    onClick: (Long) -> Unit = {},
    onLongClick: (Long) -> Unit = {}
) {
    val context = LocalContext.current

    val selectionContainerActor = LocalSelectionContainerActor.current
    val isSelecting by selectionContainerActor.isSelectingFlow.collectAsState()
    val selectedItemIdList by selectionContainerActor.selectedItemIdListFlow.collectAsState()

    SelectableContainer2(
        selected = bucketItemBox?.id in selectedItemIdList,
        enabled = true,
        shape = MaterialTheme.shapes.large,
        border = null,
        color = SelectableContainer2Defaults.defaultColors(),
        onClick = { bucketItemBox?.id?.let(onClick) },
        onLongClick = { bucketItemBox?.id?.let(onLongClick) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 6.dp)
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(ratio = 0.675f)
                ) {
                    ThumbnailPreview(thumbnailFile = bucketItemShow?.thumbnail(context = context) as? BucketItemData.Companion.Thumbnail.File)
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .matchParentSize()
                        .padding(all = 4.dp)
                ) {
                    AnimatedVisibility(
                        visible = isReorderable && !isSelecting,
                        enter = AnimationDefaults.ScaleAndFadeEnter,
                        exit = AnimationDefaults.ScaleAndFadeExit,
                        content = { dragHandle() }
                    )
                    Spacer(modifier = Modifier.weight(weight = 1f))
                    StatusContainer(isFavourite = bucketItemBox?.isFavourite == true, isLocked = bucketItemBox?.isLocked == true)
                }
            }
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                Text(
                    text = bucketItemShow?.itemTitle() ?: "-",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                bucketItemShow?.releaseYear()?.toString()?.let {
                    Spacer(modifier = Modifier.height(height = 4.dp))
                    Text(
                        text = "[$it]",
                        style = MaterialTheme.typography.bodySmall,
                        color = SelectableContainer2Defaults.defaultColors().onContainerColor.copy(alpha = 0.71f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ThumbnailPreview(
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

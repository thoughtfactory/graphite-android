package com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.linkCard

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemLink
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.StatusContainer
import com.syncodec.graphite.presentation.common.v2.selectable2.LocalSelectionContainerActor
import com.syncodec.graphite.presentation.common.v2.selectable2.SelectableContainer2
import com.syncodec.graphite.presentation.common.v2.selectable2.SelectableContainer2Defaults
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun LinkGridCard(
    bucketItemBox: BucketItemBoxDecrypted?,
    bucketItemLink: BucketItemLink? = null,
    isReorderable: Boolean = false,
    dragHandle: @Composable () -> Unit = {},
    onClick: (Long) -> Unit = {},
    onLongClick: (Long) -> Unit = {}
) {
    val context = LocalContext.current

    val selectionContainerActor = LocalSelectionContainerActor.current
    val isSelecting by selectionContainerActor.isSelectingFlow.collectAsState()
    val selectedItemIdList by selectionContainerActor.selectedItemIdListFlow.collectAsState()

    val linkData by remember(key1 = bucketItemLink) { derivedStateOf { bucketItemLink?.linkData } }

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
        Column(
            modifier = Modifier.height(height = 256.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 128.dp)
            ) {
                ThumbnailPreview(base64String = linkData?.imageBase64)

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
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 12.dp)
                    .weight(weight = 1f)
            ) {
                Text(
                    text = linkData?.title ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Column(
                    modifier = Modifier.weight(weight = 1f)
                ) {
                    Text(
                        text = linkData?.url ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                    Text(
                        text = linkData?.description ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        overflow = TextOverflow.Ellipsis,
                        minLines = 6,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

@Composable
private fun ThumbnailPreview(
    base64String: String?,
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    var bitmap: Bitmap? by remember { mutableStateOf(null) }
    LaunchedEffect(key1 = Unit) {
        withContext(context = Dispatchers.Default) {
            val _bitmap = base64String?.decodeBase64ToBitmap()
            withContext(Dispatchers.Main) { bitmap = _bitmap }
        }
    }

    var size by remember { mutableStateOf<IntSize?>(value = null) }

    if (base64String != null) Box(modifier = Modifier) {
        size?.let { size1 ->
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(data = bitmap)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                loading = { CircularProgressIndicator(modifier = Modifier.requiredSize(size = 32.dp), strokeWidth = 2.dp) },
                modifier = Modifier
                    .size(size = DpSize(width = with(density) { size1.width.toDp() }, height = with(density) { size1.height.toDp() }))
                    .background(color = MaterialTheme.colorScheme.surface)
                    .blur(radius = 12.dp)
            )
        }
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(data = bitmap)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            alignment = Alignment.Center,
            loading = { CircularProgressIndicator(modifier = Modifier.requiredSize(size = 32.dp), strokeWidth = 2.dp) },
            modifier = Modifier
                .wrapContentSize()
                .onGloballyPositioned { size = it.size }
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

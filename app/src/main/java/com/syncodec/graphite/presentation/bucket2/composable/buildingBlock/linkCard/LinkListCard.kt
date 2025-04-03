package com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.linkCard

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
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
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemLink
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.StatusContainer
import com.syncodec.graphite.presentation.common.v2.selectable2.LocalSelectionContainerActor
import com.syncodec.graphite.presentation.common.v2.selectable2.SelectableContainer2
import com.syncodec.graphite.presentation.common.v2.selectable2.SelectableContainer2Defaults
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import com.syncodec.graphite.utils.decodeBase64ToBitmap


@Composable
fun LinkListCard(
    bucketItemBox: BucketItemBoxDecrypted?,
    bucketItemLink: BucketItemLink? = null,
    isReorderable: Boolean = false,
    isLast: Boolean = false,
    dragHandle: @Composable () -> Unit = {},
    onClick: (Long) -> Unit = {},
    onLongClick: (Long) -> Unit = {}
) {
    val selectionContainerActor = LocalSelectionContainerActor.current
    val isSelecting by selectionContainerActor.isSelectingFlow.collectAsState()
    val selectedItemIdList by selectionContainerActor.selectedItemIdListFlow.collectAsState()

    val linkData by remember(key1 = bucketItemLink) { derivedStateOf { bucketItemLink?.linkData } }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SelectableContainer2(
            selected = bucketItemBox?.id in selectedItemIdList,
            enabled = true,
            shape = RectangleShape,
            border = null,
            color = SelectableContainer2Defaults.backgroundColors(),
            onClick = { bucketItemBox?.id?.let(onClick) },
            onLongClick = { bucketItemBox?.id?.let(onLongClick) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier.requiredSize(size = 108.dp)
                ) {
                    ThumbnailPreview(base64String = linkData?.imageBase64)
                }

                Spacer(modifier = Modifier.width(width = 8.dp))

                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .weight(weight = 1f)
                        .height(height = 108.dp)
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.weight(weight = 1f)
                        ) {
                            Text(
                                text = linkData?.title ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(height = 2.dp))
                            Text(
                                text = linkData?.url ?: "",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
                            )
                        }
                        Spacer(modifier = Modifier.width(width = 4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StatusContainer(
                                isFavourite = bucketItemBox?.isFavourite == true,
                                isLocked = bucketItemBox?.isLocked == true,
                            )

                            AnimatedContent(
                                modifier = Modifier,
                                targetState = isReorderable && !isSelecting,
                                transitionSpec = { AnimationDefaults.ScaleAndFade }
                            ) {
                                if (it) dragHandle() else Unit
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(height = 4.dp))

                    Text(
                        text = linkData?.description ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
                        modifier = Modifier.weight(weight = 1f)
                    )
                }
            }
        }

        if (!isLast) HorizontalDivider(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.31f),
            modifier = Modifier.fillMaxWidth(fraction = 0.71f)
        )
    }
}

@Composable
private fun ThumbnailPreview(
    base64String: String?,
) {
    val context = LocalContext.current

    if (base64String != null) SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(data = base64String.decodeBase64ToBitmap())
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        alignment = Alignment.Center,
        loading = { CircularProgressIndicator(modifier = Modifier.requiredSize(size = 32.dp), strokeWidth = 2.dp) },
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
            .clip(shape = MaterialTheme.shapes.medium)
    ) else Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large)
            .clip(shape = MaterialTheme.shapes.large)
    ) {
        Text(
            text = stringResource(id = R.string.no_cover_image),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

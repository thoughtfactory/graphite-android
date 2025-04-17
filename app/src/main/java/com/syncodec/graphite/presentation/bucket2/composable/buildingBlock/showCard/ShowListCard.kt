package com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.showCard

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemData
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemShow
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.ListThumbnailPreview
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.StateViewButton
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.StatusContainer
import com.syncodec.graphite.presentation.common.v2.selectable2.LocalSelectionContainerActor
import com.syncodec.graphite.presentation.common.v2.selectable2.SelectableContainer2
import com.syncodec.graphite.presentation.common.v2.selectable2.SelectableContainer2Defaults
import com.syncodec.graphite.presentation.ui.AnimationDefaults


@Composable
fun ShowListCard(
    bucketItemBox: BucketItemBoxDecrypted?,
    bucketItemShow: BucketItemShow? = null,
    isReorderable: Boolean = false,
    isLast: Boolean = false,
    dragHandle: @Composable () -> Unit = {},
    onClickStateButton: () -> Unit = {},
    onClick: (Long) -> Unit = {},
    onLongClick: (Long) -> Unit = {}
) {
    val context = LocalContext.current

    val selectionContainerActor = LocalSelectionContainerActor.current
    val isSelecting by selectionContainerActor.isSelectingFlow.collectAsState()
    val selectedItemIdList by selectionContainerActor.selectedItemIdListFlow.collectAsState()

    val state by remember(key1 = bucketItemBox) { derivedStateOf { bucketItemBox?.state ?: BucketItemBoxDecrypted.State.Alpha } }

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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 12.dp, end = 16.dp, bottom = 12.dp)
            ) {

                StateViewButton(state = state, onClickStateButton = onClickStateButton)

                Box(
                    modifier = Modifier
                        .height(height = 128.dp)
                        .aspectRatio(ratio = 0.675f)
                ) {
                    ListThumbnailPreview(thumbnailFile = bucketItemShow?.thumbnail(context = context) as? BucketItemData.Companion.Thumbnail.File)
                }

                Spacer(modifier = Modifier.width(width = 8.dp))

                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .weight(weight = 1f)
                        .height(height = 128.dp)
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
                                text = bucketItemShow?.showTitle() ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(height = 4.dp))
                            Text(
                                text = bucketItemShow?.showTagline() ?: "",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
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
                        text = bucketItemShow?.showOverview() ?: "",
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

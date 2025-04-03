package com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.todoCard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemTodo
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.StatusContainer
import com.syncodec.graphite.presentation.common.v2.selectable2.LocalSelectionContainerActor
import com.syncodec.graphite.presentation.common.v2.selectable2.SelectableContainer2
import com.syncodec.graphite.presentation.common.v2.selectable2.SelectableContainer2Defaults
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import kotlin.collections.contains


@Composable
fun TodoListCard(
    bucketItemBox: BucketItemBoxDecrypted?,
    bucketItemTodo: BucketItemTodo?,
    state: BucketItemBoxDecrypted.State,
    isReorderable: Boolean = false,
    isLast: Boolean = false,
    dragHandle: @Composable () -> Unit = {},
    onClickTriStateButton: () -> Unit = {},
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {

    val selectionContainerActor = LocalSelectionContainerActor.current
    val isSelecting by selectionContainerActor.isSelectingFlow.collectAsState()
    val selectedItemIdList by selectionContainerActor.selectedItemIdListFlow.collectAsState()

    val triState by remember(key1 = state) {
        derivedStateOf {
            when (state) {
                BucketItemBoxDecrypted.State.Alpha -> ToggleableState.Off
                BucketItemBoxDecrypted.State.Beta -> ToggleableState.Indeterminate
                BucketItemBoxDecrypted.State.Gamma -> ToggleableState.On
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SelectableContainer2(
            selected = bucketItemBox?.id in selectedItemIdList,
            enabled = true,
            shape = RectangleShape,
            color = SelectableContainer2Defaults.backgroundColors(),
            onClick = onClick,
            onLongClick = onLongClick
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(height = 6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TriStateCheckbox(
                        state = triState,
                        onClick = onClickTriStateButton
                    )

                    Spacer(modifier = Modifier.width(width = 12.dp))

                    Text(
                        text = bucketItemTodo?.title ?: "-",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(weight = 1f)
                    )

                    Spacer(modifier = Modifier.width(width = 12.dp))

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
                            transitionSpec = { AnimationDefaults.ScaleAndFade },
                            content = {if (it) dragHandle() else Unit}
                        )
                    }
                }

                if (!bucketItemTodo?.description.isNullOrBlank()) Text(
                    text = bucketItemTodo.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 60.dp, bottom = 12.dp)
                )

                Spacer(modifier = Modifier.height(height = 6.dp))
            }
        }

        if (!isLast) HorizontalDivider(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.31f),
            modifier = Modifier.fillMaxWidth(fraction = 0.71f)
        )
    }
}

package com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.todoCard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemTodo
import com.syncodec.graphite.presentation.common.v2.selectable2.LocalSelectionContainerActor
import com.syncodec.graphite.presentation.common.v2.selectable2.SelectableContainer2
import com.syncodec.graphite.presentation.common.v2.selectable2.SelectableContainer2Defaults
import com.syncodec.graphite.presentation.ui.AnimationDefaults


@Composable
fun TodoGridCard(
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

//    SelectableContainer2(
//        selected = bucketItemBox?.id in selectedItemIdList,
//        enabled = true,
//        shape = MaterialTheme.shapes.large,
//        border = null,
//        color = SelectableContainer2Defaults.defaultColors(),
//        onClick = { bucketItemBox?.id?.let(onClick) },
//        onLongClick = { bucketItemBox?.id?.let(onLongClick) },
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(all = 6.dp)
//    ) {
//        Column {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .aspectRatio(ratio = 0.875f)
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.End,
//                    modifier = Modifier.fillMaxSize()
//                ) {
//                    AnimatedVisibility(
//                        visible = isReorderable && !isSelecting,
//                        enter = AnimationDefaults.ScaleAndFadeEnter,
//                        exit = AnimationDefaults.ScaleAndFadeExit,
//                        content = { dragHandle() }
//                    )
//                    Spacer(modifier = Modifier.weight(weight = 1f))
//                }
//            }
//            Column(
//                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
//            ) {
//                Text(
//                    text = bucketItemBook?.bookTitle() ?: "-",
//                    style = MaterialTheme.typography.bodyMedium,
//                    fontWeight = FontWeight.Bold,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis
//                )
//                bucketItemBook?.primaryAuthor()?.let {
//                    Spacer(modifier = Modifier.height(height = 4.dp))
//                    Text(
//                        text = it,
//                        style = MaterialTheme.typography.bodySmall,
//                        color = SelectableContainer2Defaults.defaultColors().onContainerColor.copy(alpha = 0.71f)
//                    )
//                }
//                bucketItemBook?.bookPublicationYear()?.toString()?.let {
//                    Spacer(modifier = Modifier.height(height = 4.dp))
//                    Text(
//                        text = "[$it]",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = SelectableContainer2Defaults.defaultColors().onContainerColor.copy(alpha = 0.71f)
//                    )
//                }
//            }
//        }
//    }
}

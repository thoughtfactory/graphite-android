package com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.todoCard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemData
import com.syncodec.graphite.presentation.common.button.GraIconButton
import com.syncodec.graphite.presentation.common.v2.selectable2.SelectableContainer2
import com.syncodec.graphite.presentation.common.v2.selectable2.SelectableContainer2Defaults
import com.syncodec.graphite.presentation.ui.AnimationDefaults


@Composable
fun TodoCard(
    text: String,
    state: BucketItemData.State,
    selected: Boolean = false,
    isSelecting: Boolean = false,
    isReorderable: Boolean = false,
    isLast: Boolean = false,
    dragHandle: @Composable () -> Unit = {},
    onClickTriStateButton: () -> Unit = {},
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {

    val triState by remember(key1 = state) {
        derivedStateOf {
            when (state) {
                BucketItemData.State.Alpha -> ToggleableState.Off
                BucketItemData.State.Beta -> ToggleableState.Indeterminate
                BucketItemData.State.Gamma -> ToggleableState.On
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SelectableContainer2(
            selected = selected,
            enabled = true,
            shape = RectangleShape,
            color = SelectableContainer2Defaults.backgroundColors(),
            onClick = onClick,
            onLongClick = onLongClick
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 72.dp)
            ) {
                Spacer(modifier = Modifier.width(width = 24.dp))

                TriStateCheckbox(
                    state = triState,
                    onClick = onClickTriStateButton
                )

                Spacer(modifier = Modifier.width(width = 24.dp))

                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.weight(weight = 1f))

                AnimatedVisibility(
                    visible = !isSelecting,
                    enter = AnimationDefaults.ScaleAndFadeEnter,
                    exit = AnimationDefaults.ScaleAndFadeExit,
                    content = { GraIconButton.FavouriteButton() }
                )

                Spacer(modifier = Modifier.width(width = 8.dp))

                AnimatedVisibility(
                    visible = !isSelecting,
                    enter = AnimationDefaults.ScaleAndFadeEnter,
                    exit = AnimationDefaults.ScaleAndFadeExit,
                    content = { GraIconButton.LockButton() }
                )

                Spacer(modifier = Modifier.width(width = 8.dp))

                AnimatedVisibility(
                    visible = isReorderable && !isSelecting,
                    enter = AnimationDefaults.ScaleAndFadeEnter,
                    exit = AnimationDefaults.ScaleAndFadeExit,
                    content = { dragHandle() }
                )

                Spacer(modifier = Modifier.width(width = 24.dp))
            }
        }

        if (!isLast) HorizontalDivider(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.31f),
            modifier = Modifier.fillMaxWidth(fraction = 0.71f)
        )
    }
}

package com.syncodec.graphite.presentation.bucket2.composable.bar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.v2.button.GraIconButton
import com.syncodec.graphite.presentation.ui.AnimationDefaults


@Composable
fun BottomBar(
    isSelecting: Boolean = true,
    onClickFilterAndSortButton: () -> Unit = {},
    onClickMetadata: () -> Unit = {},
) {
    AnimatedContent(
        targetState = isSelecting,
        transitionSpec = { AnimationDefaults.ExpandVerticallyEnter togetherWith AnimationDefaults.ShrinkVerticallyExit }
    ) {
        if (it) Unit
        else Column {
            HorizontalDivider()
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    GraIconButton.VaultButton()
                    Spacer(modifier = Modifier.weight(weight = 1f))
                    GraIconButton.FilterAndSortTextButton(onClick = onClickFilterAndSortButton)
                    GraIconButton.MenuButton(onClick = onClickMetadata)
                }
            }
        }
    }
}

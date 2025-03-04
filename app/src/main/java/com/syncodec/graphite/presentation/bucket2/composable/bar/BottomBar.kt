package com.syncodec.graphite.presentation.bucket2.composable.bar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.syncodec.graphite.presentation.common.button.GraIconButton
import com.syncodec.graphite.presentation.ui.AnimationDefaults


@Composable
fun BottomBar(
    isSelecting: Boolean = true,
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
                Spacer(modifier = Modifier.weight(weight = 1f))
                GraIconButton.VaultButton(checked = false)
                GraIconButton.MenuButton(onClick = onClickMetadata)
            }
        }
    }
}

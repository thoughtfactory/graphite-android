package com.syncodec.graphite.presentation.common.bar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.common.scaffold.LocalIsTopBarVisible


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericTopBar2(
    title: String? = null,
    navigationButton: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
) {

    val isTopBarVisible = LocalIsTopBarVisible.current

    AnimatedVisibility(
        visible = isTopBarVisible,
        modifier = Modifier.fillMaxWidth(),
        enter = expandVertically(tween(ANIMATION_DURATION_MILLIS)),
        exit = shrinkVertically(tween(ANIMATION_DURATION_MILLIS))
    ) {
        TopAppBar(
            title = title?.let { { Text(text = it) } } ?: {},
            modifier = Modifier.fillMaxWidth(),
            navigationIcon = navigationButton ?: {},
            actions = actions ?: {}
        )
    }
}

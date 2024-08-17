package com.syncodec.graphite.presentation.common.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.utils.LocalBackgroundColor


@Composable
fun GenericScaffold2(
    isTopBarVisible: Boolean = true,
    isBottomBarVisible: Boolean = true,
    isSelecting: Boolean = false,
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    dialogContent: @Composable (BoxScope.() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {

    CompositionLocalProvider(
        LocalIsBottomBarVisible provides isTopBarVisible,
        LocalIsBottomBarVisible provides isBottomBarVisible,
        LocalIsSelecting provides isSelecting,
        LocalBackgroundColor provides MaterialTheme.colorScheme.background,
    ) {
        Scaffold(
            topBar = topBar ?: {},
            bottomBar = bottomBar ?: {},
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = floatingActionButton ?: {}
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                content = content
            )
        }

        if (dialogContent != null) Box(
            content = dialogContent,
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        )
    }
}

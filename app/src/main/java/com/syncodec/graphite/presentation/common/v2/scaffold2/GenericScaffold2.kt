package com.syncodec.graphite.presentation.common.v2.scaffold2

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericScaffold2(
    topBar: @Composable () -> Unit = { },
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    bottomSheetContent: @Composable () -> Unit = {},
    hazeState: HazeState = remember { HazeState() },
    compositionLocalValues: List<ProvidedValue<*>> = listOf(),
    overlayContent: @Composable BoxScope.() -> Unit = {},
    scaffoldContent: @Composable BoxScope.() -> Unit
) {

    CompositionLocalProvider(
        values = compositionLocalValues.toTypedArray()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
//                .safeDrawingPadding()
        ) {
            Scaffold(
                topBar = topBar,
                bottomBar = bottomBar,
                floatingActionButton = floatingActionButton,
                floatingActionButtonPosition = floatingActionButtonPosition,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues = it)
                ) {
                    scaffoldContent()
                    overlayContent()
                }
            }
            bottomSheetContent()
        }
    }
}


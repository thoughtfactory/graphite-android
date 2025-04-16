package com.syncodec.graphite.presentation.common.v2.overlayScaffold

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.LocalOuterHazeState
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


object OverlayScaffold {
    @OptIn(ExperimentalMaterial3Api::class)
    class State<T>(
        val onCloseOverlay: () -> Unit = {}
    ) {

        private val _isOverlayVisible: MutableStateFlow<Boolean> = MutableStateFlow(value = false)
        val isOverlayVisible: StateFlow<Boolean> = this._isOverlayVisible.asStateFlow()

        private val _dataMutableFlow: MutableStateFlow<T?> = MutableStateFlow(value = null)
        val dataFlow = _dataMutableFlow.asStateFlow()

        /**
         * Open overlay and data remains unchanged
         */
        fun openOverlay() {
            this._isOverlayVisible.tryEmit(value = true)
        }

        /**
         * Open overlay and emit data
         */
        fun openOverlay(data: T?) {
            this._isOverlayVisible.tryEmit(value = true)
            this._dataMutableFlow.tryEmit(value = data)
        }

        /**
         * Close overlay and clear data
         */
        @OptIn(ExperimentalMaterial3Api::class)
        fun closeOverlay() {
            this._isOverlayVisible.tryEmit(value = false)
            this._dataMutableFlow.tryEmit(value = null)
            this.onCloseOverlay()
        }

        /**
         * Close overlay and emit data
         */
        @OptIn(ExperimentalMaterial3Api::class)
        fun closeOverlay(data: T?) {
            this._isOverlayVisible.tryEmit(value = false)
            this._dataMutableFlow.tryEmit(value = data)
            this.onCloseOverlay()
        }

        companion object {

            @Composable
            fun rememberOverlayState(onCloseOverlay: () -> Unit = {}): State<Nothing> = remember { State(onCloseOverlay = onCloseOverlay) }

            @Composable
            fun <T> rememberOverlayStateT(onCloseOverlay: () -> Unit = {}): State<T> = remember { State(onCloseOverlay = onCloseOverlay) }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun <T> Composable(
        state: State<T>,
        topBar: @Composable () -> Unit = { },
        bottomBar: @Composable () -> Unit = {},
        floatingActionButton: @Composable () -> Unit = {},
        floatingActionButtonPosition: FabPosition = FabPosition.End,
        bottomSheetContent: @Composable () -> Unit = {},
        compositionLocalValues: List<ProvidedValue<*>> = listOf(),
        scaffoldContent: @Composable BoxScope.() -> Unit
    ) {

        val isOverlayVisible by state.isOverlayVisible.collectAsState()
        BackHandler(enabled = isOverlayVisible) { state.closeOverlay() }

        val hazeState = remember { HazeState() }
        val density = LocalDensity.current

        AnimatedVisibility(
            visible = isOverlayVisible,
            enter = slideInVertically(animationSpec = AnimationDefaults.stateAnimationSpec()) { with(receiver = density) { 48.dp.roundToPx() } } + AnimationDefaults.FadeEnter,
            exit = slideOutVertically(animationSpec = AnimationDefaults.stateAnimationSpec()) { with(receiver = density) { 48.dp.roundToPx() } } + AnimationDefaults.FadeExit,
        ) {
            CompositionLocalProvider(
                values = (compositionLocalValues + (LocalOuterHazeState provides hazeState)).toTypedArray()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
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
                                .padding(paddingValues = it),
                            content = scaffoldContent
                        )
                    }
                    bottomSheetContent()
                }
            }
        }
    }
}

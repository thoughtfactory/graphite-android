package com.syncodec.graphite.presentation.common.v2.bottomSheet2

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


val LocalOuterHazeState: ProvidableCompositionLocal<HazeState> = staticCompositionLocalOf { error("no haze state provided") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> GenericBottomSheet2(
    modifier: Modifier = Modifier,
    bottomSheetState: GenericBottomSheet2State<T>,
    content: @Composable ColumnScope.() -> Unit = { },
) {

    val isBottomSheetVisible by bottomSheetState.isBottomSheetVisibleFlow.collectAsState()
    val sheetState = bottomSheetState.sheetState

    val outerHazeState = LocalOuterHazeState.current
    val hazeDp by animateDpAsState(targetValue = if (isBottomSheetVisible) 16.dp else 0.dp)

    if (isBottomSheetVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeEffect(state = outerHazeState, style = HazeStyle(backgroundColor = MaterialTheme.colorScheme.background, tint = HazeTint(color = Color.Transparent), blurRadius = hazeDp))
        ) {
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = bottomSheetState::dismissSheet,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                scrimColor = Color.Black.copy(alpha = 0.42f),
                modifier = modifier,
                content = content,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
data class GenericBottomSheet2State<T>(
    val isBottomSheetVisibleFlow: MutableStateFlow<Boolean>,
    val sheetState: SheetState,
) {

    init {
        Log.d("GenericBottomSheet2State", "GenericBottomSheet2State")
    }

    private val _dataMutableFlow: MutableStateFlow<T?> = MutableStateFlow(value = null)
    val dataFlow = _dataMutableFlow.asStateFlow()

    fun openSheet() {
        this.isBottomSheetVisibleFlow.tryEmit(value = true)
    }

    fun openSheet(data: T?) {
        this.isBottomSheetVisibleFlow.tryEmit(value = true)
        this._dataMutableFlow.tryEmit(value = data)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun hideSheet(scope: CoroutineScope, onCompletion: () -> Unit = {}) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) isBottomSheetVisibleFlow.tryEmit(value = false)
            onCompletion()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun hideSheet(scope: CoroutineScope, data: T?) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            this._dataMutableFlow.tryEmit(value = data)
            if (!sheetState.isVisible) isBottomSheetVisibleFlow.tryEmit(value = false)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun expandFull(scope: CoroutineScope) {
        scope.launch { sheetState.expand() }
    }

    fun dismissSheet() {
        isBottomSheetVisibleFlow.tryEmit(value = false)
    }

//    data class SaveableData<T>(val currentValue: SheetValue, val isBottomSheetVisible: Boolean, val data: T?)

    companion object {

        fun <T> Saver(
            isBottomSheetVisible: Boolean = false,
            skipPartiallyExpanded: Boolean = false,
            confirmValueChange: (SheetValue) -> Boolean = { true },
            density: Density,
//        ) = androidx.compose.runtime.saveable.Saver<GenericBottomSheet2State<T>, SaveableData<T>>(
        ) = androidx.compose.runtime.saveable.Saver<GenericBottomSheet2State<T>, SheetValue>(
//            save = { SaveableData(currentValue = it.sheetState.currentValue, isBottomSheetVisible = isBottomSheetVisible, data = it.dataFlow.value) },
            save = { it.sheetState.currentValue },
            restore = { savedValue ->
                val sheetState = SheetState(
                    skipPartiallyExpanded = skipPartiallyExpanded,
                    density = density,
                    initialValue = savedValue,
                    confirmValueChange = confirmValueChange,
                    skipHiddenState = false,
                )
                return@Saver GenericBottomSheet2State<T>(isBottomSheetVisibleFlow = MutableStateFlow(value = false), sheetState = sheetState).apply {
//                    this._dataMutableFlow.tryEmit(value = savedValue)
                }
            }
        )

        @Composable
        fun <T> rememberGenericBottomSheet2StateT(
            skipPartiallyExpanded: Boolean = false,
            confirmValueChange: (SheetValue) -> Boolean = { true },
        ): GenericBottomSheet2State<T> {
            val density = LocalDensity.current
            return rememberSaveable(
                skipPartiallyExpanded,
                confirmValueChange,
                saver = Saver(
                    isBottomSheetVisible = false,
                    skipPartiallyExpanded = skipPartiallyExpanded,
                    confirmValueChange = confirmValueChange,
                    density = density,
                )
            ) {
                GenericBottomSheet2State<T>(
                    isBottomSheetVisibleFlow = MutableStateFlow(value = false),
                    sheetState = SheetState(
                        skipPartiallyExpanded = skipPartiallyExpanded,
                        density = density,
                        initialValue = SheetValue.Hidden,
                        confirmValueChange = confirmValueChange,
                    )
                )
            }
        }

        @Composable
        fun rememberGenericBottomSheet2State(
            skipPartiallyExpanded: Boolean = false,
            confirmValueChange: (SheetValue) -> Boolean = { true },
        ): GenericBottomSheet2State<Nothing> {
            val density = LocalDensity.current
            return rememberSaveable(
                skipPartiallyExpanded,
                confirmValueChange,
                saver = Saver(
                    isBottomSheetVisible = false,
                    skipPartiallyExpanded = skipPartiallyExpanded,
                    confirmValueChange = confirmValueChange,
                    density = density,
                )
            ) {
                GenericBottomSheet2State(
                    isBottomSheetVisibleFlow = MutableStateFlow(value = false),
                    sheetState = SheetState(
                        skipPartiallyExpanded = skipPartiallyExpanded,
                        density = density,
                        initialValue = SheetValue.Hidden,
                        confirmValueChange = confirmValueChange,
                    )
                )
            }
        }
    }
}

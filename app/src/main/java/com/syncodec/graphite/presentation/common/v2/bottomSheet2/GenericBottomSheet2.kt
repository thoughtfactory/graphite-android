package com.syncodec.graphite.presentation.common.v2.bottomSheet2

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


val LocalBottomSheetState = compositionLocalOf<GenericBottomSheet2State> { error("Bottom sheet state not provided") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericBottomSheet2(
    modifier: Modifier = Modifier,
    bottomSheetState: GenericBottomSheet2State,
    content: @Composable ColumnScope.() -> Unit = { },
) {

    val isBottomSheetVisible by bottomSheetState.isBottomSheetVisibleFlow.collectAsState()
    val sheetState = bottomSheetState.sheetState

    if (isBottomSheetVisible) {
        CompositionLocalProvider(
            LocalBottomSheetState provides bottomSheetState
        ) {
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = bottomSheetState::dismissSheet,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                scrimColor = Color.Black.copy(alpha = 0.42f),
                modifier = modifier,
                content = content,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
data class  GenericBottomSheet2State  (
    val isBottomSheetVisibleFlow: MutableStateFlow<Boolean>,
    val sheetState: SheetState,
) {
    fun openSheet() {
        isBottomSheetVisibleFlow.tryEmit(true)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun hideSheet(scope: CoroutineScope) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) isBottomSheetVisibleFlow.tryEmit(false)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun expandFull(scope: CoroutineScope) {
        scope.launch { sheetState.expand() }
    }

    fun dismissSheet() {
        isBottomSheetVisibleFlow.tryEmit(false)
    }

    companion object {
        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun initialize(skipPartiallyExpanded: Boolean = false, confirmValueChange: (SheetValue) -> Boolean = { true }): GenericBottomSheet2State =
            GenericBottomSheet2State(isBottomSheetVisibleFlow = MutableStateFlow(false), sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded, confirmValueChange = confirmValueChange))
    }
}

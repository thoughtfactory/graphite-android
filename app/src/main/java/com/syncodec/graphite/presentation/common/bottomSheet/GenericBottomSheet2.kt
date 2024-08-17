package com.syncodec.graphite.presentation.common.bottomSheet

import android.util.Log
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


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
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = bottomSheetState::dismissSheet,
            containerColor = MaterialTheme.colorScheme.surfaceDim,
            contentColor = MaterialTheme.colorScheme.onSurface,
            scrimColor = Color.Black.copy(alpha = 0.42f),
            modifier = modifier,
            content = content,
        )
    }
}

data class GenericBottomSheet2State @OptIn(ExperimentalMaterial3Api::class) constructor(
    val isBottomSheetVisibleFlow: MutableStateFlow<Boolean>,
    val sheetState: SheetState,
) {
    fun openSheet() {
        isBottomSheetVisibleFlow.tryEmit(true)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun hideSheet() {
        val scope = rememberCoroutineScope()
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) isBottomSheetVisibleFlow.tryEmit(false)
        }
    }

    fun dismissSheet() {
        isBottomSheetVisibleFlow.tryEmit(false)
    }

    companion object {
        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun initialize(): GenericBottomSheet2State {
            Log.d("npr71", "initialize")
            return GenericBottomSheet2State(isBottomSheetVisibleFlow = MutableStateFlow(false), sheetState = rememberModalBottomSheetState())
        }
    }
}


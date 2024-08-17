package com.syncodec.graphite.presentation.common.scaffold

import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow


val LocalIsTopBarVisible = compositionLocalOf { true }
val LocalIsBottomBarVisible = compositionLocalOf { true }
val LocalIsSelecting = compositionLocalOf { false }



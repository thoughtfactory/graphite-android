package com.syncodec.graphite.presentation.common.component

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp


val LocalComponentHeight = compositionLocalOf<Dp> { error("No height provided") }
val LocalComponentWidth = compositionLocalOf<Dp> { error("No height provided") }
val LocalComponentColumnCount = compositionLocalOf<Int> { error("No height provided") }

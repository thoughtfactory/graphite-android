package com.syncodec.graphite.presentation.bucketItem.composable

import androidx.compose.runtime.compositionLocalOf


val LocalCompositionTitle = compositionLocalOf<String?> { "Title" }
val LocalCompositionIsNew = compositionLocalOf<Boolean> { false }
val LocalCompositionIsLocked = compositionLocalOf<Boolean> { false }
val LocalCompositionIsFavourite = compositionLocalOf<Boolean> { false }
val LocalCompositionOnClickSave = compositionLocalOf<() -> Unit> { {} }
val LocalCompositionOnClickLock = compositionLocalOf<() -> Unit> { {} }
val LocalCompositionOnClickFavourite = compositionLocalOf<() -> Unit> { {} }
val LocalCompositionOnClickNavigationIcon = compositionLocalOf<() -> Unit> { {} }

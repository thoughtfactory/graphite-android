package com.syncodec.graphite.presentation.main.composable

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.syncodec.graphite.presentation.main.composable.bottomSheet.MainBottomSheetType
import com.syncodec.graphite.presentation.main.composable.dialog.MainDialogType
import io.realm.kotlin.types.ObjectId


val LocalCompositionIsNoteResreshing = compositionLocalOf<Boolean> { error("No data provided") }
val LocalCompositionIsBucketResreshing = compositionLocalOf<Boolean?> { error("No data provided") }
val LocalCompositionIsNotebookResreshing = compositionLocalOf<Boolean?> { error("No data provided") }
val LocalCompositionOnRefresh = compositionLocalOf<() -> Unit> { error("No data provided") }

val LocalCompositionOpenBottomSheet = compositionLocalOf<(MainBottomSheetType) -> Unit> { error("No data provided") }
val LocalCompositionCloseBottomSheet = compositionLocalOf<() -> Unit> { error("No data provided") }
val LocalCompositionOpenDialog = compositionLocalOf<(MainDialogType) -> Unit> { error("No data provided") }
val LocalCompositionCloseDialog = compositionLocalOf<(MainDialogType) -> Unit> { error("No data provided") }

val LocalCompositionShowDeleteDialog = compositionLocalOf<Boolean> { error("No data provided") }
val LocalCompositionShowExitDialog = compositionLocalOf<Boolean> { error("No data provided") }

val LocalCompositionIsSelected = compositionLocalOf<Boolean> { error("No data provided") }
val LocalCompositionOnSelected = compositionLocalOf<(Boolean) -> Unit> { error("No data provided") }
val LocalCompositionSelectedObjectIdList = compositionLocalOf<SnapshotStateList<ObjectId>> { error("No data provided") }

val LocalCompositionOnDelete = compositionLocalOf<() -> Unit> { error("No data provided") }
val LocalCompositionOnExit = compositionLocalOf<() -> Unit> { error("No data provided") }

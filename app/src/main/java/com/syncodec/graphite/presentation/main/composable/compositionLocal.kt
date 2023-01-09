package com.syncodec.graphite.presentation.main.composable

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.main.composable.bottomSheet.MainBottomSheetType
import com.syncodec.graphite.presentation.main.composable.dialog.MainDialogType
import com.syncodec.graphite.service.DropboxSyncStatus
import io.realm.kotlin.types.RealmUUID

val LocalCompositionSyncStatus = compositionLocalOf { DropboxSyncStatus.INIT }
val LocalCompositionTagList = compositionLocalOf<SnapshotStateList<TagObject>> { mutableStateListOf() }

val LocalCompositionIsNoteRefreshing = compositionLocalOf<Boolean> { error("No data provided") }
val LocalCompositionIsBucketRefreshing = compositionLocalOf<Boolean?> { error("No data provided") }
val LocalCompositionIsNotebookRefreshing = compositionLocalOf<Boolean?> { error("No data provided") }
val LocalCompositionOnRefresh = compositionLocalOf<() -> Unit> { error("No data provided") }
val LocalCompositionOnSyncNow = compositionLocalOf { {} }
val LocalCompositionOnForceSync = compositionLocalOf { {} }

val LocalCompositionOpenBottomSheet = compositionLocalOf<(MainBottomSheetType) -> Unit> { error("No data provided") }
val LocalCompositionCloseBottomSheet = compositionLocalOf<() -> Unit> { {} }
val LocalCompositionOpenDialog = compositionLocalOf<(MainDialogType) -> Unit> { error("No data provided") }
val LocalCompositionCloseDialog = compositionLocalOf<(MainDialogType) -> Unit> { error("No data provided") }

val LocalCompositionShowNotificationPermissionDialog = compositionLocalOf<Boolean> { error("No data provided") }
val LocalCompositionShowDeleteDialog = compositionLocalOf<Boolean> { error("No data provided") }
val LocalCompositionShowExitDialog = compositionLocalOf<Boolean> { error("No data provided") }

val LocalCompositionPutBucket = compositionLocalOf<(String?, String?, BucketType) -> Unit> { error("No data provided") }
val LocalCompositionOnReorderBucketList = compositionLocalOf<(List<RealmUUID>) -> Unit> { {} }

val LocalCompositionOnDelete = compositionLocalOf<() -> Unit> { error("No data provided") }
val LocalCompositionOnExit = compositionLocalOf<() -> Unit> { error("No data provided") }

val LocalCompositionOnAddDebugData = compositionLocalOf<() -> Unit> { error("No data provided") }

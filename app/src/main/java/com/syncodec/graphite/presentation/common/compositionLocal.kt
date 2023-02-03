package com.syncodec.graphite.presentation.common

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.syncodec.graphite.presentation.common.dialog.DialogType
import io.realm.kotlin.types.RealmUUID


val LocalCompositionIsSelected = compositionLocalOf<Boolean> { false }
val LocalCompositionOnSelect = compositionLocalOf<(Boolean) -> Unit> { {} }
val LocalCompositionSelectedRealmUUIDIdList = compositionLocalOf<SnapshotStateList<RealmUUID>> { mutableStateListOf() }

val LocalCompositionOpenDialog = compositionLocalOf<(DialogType) -> Unit> { {} }
val LocalCompositionCloseDialog = compositionLocalOf<(DialogType) -> Unit> { {} }

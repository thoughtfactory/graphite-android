package com.syncodec.graphite.presentation.bucket.composable

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.kedia.ogparser.OpenGraphResult
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.BucketBottomSheetType
import io.realm.kotlin.types.RealmUUID


val LocalCompositionBucketObject = compositionLocalOf<BucketObject?> { null }
val LocalCompositionBucketItemObjectList = compositionLocalOf<SnapshotStateList<BucketItemObject>> { mutableStateListOf() }
val LocalCompositionOnRefresh = compositionLocalOf { {}}

val LocalCompositionPagerState = compositionLocalOf<Int> { error("No data provided") }

val LocalCompositionOnClickLock = compositionLocalOf<() -> Unit> { error("No data provided") }
val LocalCompositionOnClickFavourite = compositionLocalOf<() -> Unit> { error("No data provided") }
val LocalCompositionOnPagerStateChange = compositionLocalOf<(Int) -> Unit> { error("No data provided") }

val LocalCompositionBucketItemObject = compositionLocalOf<BucketItemObject?> { null }
val LocalCompositionSetBucketItemObject = compositionLocalOf<(BucketItemObject?) -> Unit> { error("No data provided") }
val LocalCompositionOnClickBucketItemLock = compositionLocalOf<(BucketItemObject) -> Unit> { error("No data provided") }
val LocalCompositionOnClickBucketItemFavourite = compositionLocalOf<(BucketItemObject) -> Unit> { error("No data provided") }
val LocalCompositionOnClickBucketItemDelete = compositionLocalOf<(BucketItemObject) -> Unit> { error("No data provided") }

val LocalCompositionOnAddLink = compositionLocalOf<(String) -> Unit> { {} }
val LocalCompositionOnPutTodo = compositionLocalOf<(RealmUUID?, String, BucketItemState) -> Unit> { { _, _, _ ->} }

val LocalCompositionOpenBottomSheet = compositionLocalOf<(BucketBottomSheetType) -> Unit> { {} }
val LocalCompositionCloseBottomSheet = compositionLocalOf<() -> Unit> { error("No data provided") }
val LocalCompositionOnBackPressed = compositionLocalOf<() -> Unit> { error("No data provided") }

val LocalCompositionSetOpenGraphResult = compositionLocalOf<(OpenGraphResult?) -> Unit> { {} }
val LocalCompositionOpenGraphResult = compositionLocalOf<OpenGraphResult?> { null }

val LocalCompositionShowDeleteDialog = compositionLocalOf<Boolean> { error("No data provided") }
val LocalCompositionShowEditBucketDialog = compositionLocalOf<Boolean> { error("No data provided") }

val LocalCompositionOnDelete = compositionLocalOf<() -> Unit> { error("No data provided") }
val LocalCompositionOnUpdateBucket = compositionLocalOf<(String?, String?) -> Unit> { {_, _ ->} }
val LocalCompositionOnShare = compositionLocalOf<(Boolean) -> Unit> { {} }

package com.syncodec.graphite.presentation.bucket.composable

import androidx.compose.runtime.compositionLocalOf
import com.kedia.ogparser.OpenGraphResult
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.BucketBottomSheetType
import io.realm.kotlin.types.RealmUUID


val LocalCompositionBucketObject = compositionLocalOf<BucketObject?> { null }
val LocalCompositionOnReorderBucketItem = compositionLocalOf<(List<RealmUUID>) -> Unit> { {} }
val LocalCompositionOnRefresh = compositionLocalOf { {} }

val LocalCompositionPagerState = compositionLocalOf<Int> { 0 }

val LocalCompositionOnClickLock = compositionLocalOf<() -> Unit> { {} }
val LocalCompositionOnClickFavourite = compositionLocalOf<() -> Unit> { {} }
val LocalCompositionOnPagerStateChange = compositionLocalOf<(Int) -> Unit> { {} }

val LocalCompositionBucketItemObject = compositionLocalOf<BucketItemObject?> { null }
val LocalCompositionSetBucketItemObject = compositionLocalOf<(BucketItemObject?) -> Unit> { {} }
val LocalCompositionOnClickBucketItemLock = compositionLocalOf<(BucketItemObject) -> Unit> { {} }
val LocalCompositionOnClickBucketItemFavourite = compositionLocalOf<(BucketItemObject) -> Unit> { {} }
val LocalCompositionOnClickBucketItemDelete = compositionLocalOf<(BucketItemObject) -> Unit> { {} }

val LocalCompositionOnAddLink = compositionLocalOf<(String) -> Unit> { {} }
val LocalCompositionOnPutTodo = compositionLocalOf<(RealmUUID?, String, BucketItemState) -> Unit> { { _, _, _ -> } }

val LocalCompositionOpenBottomSheet = compositionLocalOf<(BucketBottomSheetType) -> Unit> { {} }
val LocalCompositionCloseBottomSheet = compositionLocalOf<() -> Unit> { {} }
val LocalCompositionOnBackPressed = compositionLocalOf<() -> Unit> { {} }

val LocalCompositionSetOpenGraphResult = compositionLocalOf<(OpenGraphResult?) -> Unit> { {} }
val LocalCompositionOpenGraphResult = compositionLocalOf<OpenGraphResult?> { null }

val LocalCompositionShowDeleteDialog = compositionLocalOf<Boolean> { false }
val LocalCompositionShowEditBucketDialog = compositionLocalOf<Boolean> { false }

val LocalCompositionOnDelete = compositionLocalOf<() -> Unit> { {} }
val LocalCompositionOnUpdateBucket = compositionLocalOf<(String?, String?) -> Unit> { { _, _ -> } }
val LocalCompositionOnShare = compositionLocalOf<(Boolean) -> Unit> { {} }

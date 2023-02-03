package com.syncodec.graphite.presentation.bucket.composable

import androidx.compose.runtime.compositionLocalOf
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.BucketBottomSheetType


val LocalCompositionSetBucketItemObject = compositionLocalOf<(BucketItemObject?) -> Unit> { {} }

val LocalCompositionOpenBottomSheet = compositionLocalOf<(BucketBottomSheetType) -> Unit> { {} }
val LocalCompositionCloseBottomSheet = compositionLocalOf<() -> Unit> { {} }

val LocalCompositionShowDeleteDialog = compositionLocalOf<Boolean> { false }
val LocalCompositionShowEditBucketDialog = compositionLocalOf<Boolean> { false }

val LocalCompositionOnDelete = compositionLocalOf<() -> Unit> { {} }
val LocalCompositionOnUpdateBucket = compositionLocalOf<(String?, String?) -> Unit> { { _, _ -> } }
val LocalCompositionOnShare = compositionLocalOf<(Boolean) -> Unit> { {} }

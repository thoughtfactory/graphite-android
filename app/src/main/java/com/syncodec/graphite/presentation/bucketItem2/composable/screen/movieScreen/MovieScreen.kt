package com.syncodec.graphite.presentation.bucketItem2.composable.screen.movieScreen

import android.util.Log
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.syncodec.graphite.di.model.importer.ThumbnailData
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemData
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemShow
import com.syncodec.graphite.utils.DataLoader
import kotlinx.coroutines.flow.StateFlow


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MovieScreen(
    bucketItemBox: BucketItemBoxDecrypted? = null,
    thumbnailDataFlow: StateFlow<DataLoader<ThumbnailData>>,
    onToggleBucketItemState: (BucketItemBoxDecrypted.State) -> Unit = {},
) {
    Log.d("MovieScreen", "MovieScreen")

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val bucketItemData by remember(key1 = bucketItemBox?.bucketItemData) { derivedStateOf { bucketItemBox?.bucketItemData } }
    val movieData by remember(key1 = bucketItemData) { derivedStateOf { bucketItemData as? BucketItemShow.TraktMovie } }

    movieData?.let { movieData1 ->
        when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Compact -> when (movieData1::class.simpleName) {
                BucketItemShow.TraktMovie::class.simpleName -> MovieTraktScreenCompact(
                    bucketItemBox = bucketItemBox,
                    thumbnailDataFlow = thumbnailDataFlow,
                    onToggleBucketItemState = onToggleBucketItemState,
                )

                else -> TODO()
            }

            WindowWidthSizeClass.Medium -> MovieScreenExpanded(bucketItemBox = bucketItemBox)
            WindowWidthSizeClass.Expanded -> MovieScreenExpanded(bucketItemBox = bucketItemBox)
        }
    }
}
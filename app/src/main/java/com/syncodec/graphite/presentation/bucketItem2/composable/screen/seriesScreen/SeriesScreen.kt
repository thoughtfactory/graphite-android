package com.syncodec.graphite.presentation.bucketItem2.composable.screen.seriesScreen

import android.util.Log
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.syncodec.graphite.di.model.importer.ThumbnailData
import com.syncodec.graphite.di.modelObjectBox.BucketItemBox
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemData
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemShow
import com.syncodec.graphite.utils.DataLoader
import kotlinx.coroutines.flow.StateFlow


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SeriesScreen(
    bucketItemBox: BucketItemBox? = null,
    thumbnailDataFlow: StateFlow<DataLoader<ThumbnailData>>,
    onToggleBucketItemState: (BucketItemData.State) -> Unit = {},
) {
    Log.d("MovieScreen", "MovieScreen")

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val bucketItemData by remember(key1 = bucketItemBox?.bucketItemData) { derivedStateOf { bucketItemBox?.bucketItemData } }
    val seriesData by remember(key1 = bucketItemData) { derivedStateOf { bucketItemData as? BucketItemShow.TraktSeries } }

    seriesData?.let { seriesData1 ->
        when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Compact -> when (seriesData1::class.simpleName) {
                BucketItemShow.TraktSeries::class.simpleName -> SeriesTraktScreenCompact(
                    bucketItemBox = bucketItemBox,
                    thumbnailDataFlow = thumbnailDataFlow,
                    onToggleBucketItemState = onToggleBucketItemState,
                )

                else -> TODO()
            }

            WindowWidthSizeClass.Medium -> SeriesScreenExpanded(bucketItemBox = bucketItemBox)
            WindowWidthSizeClass.Expanded -> SeriesScreenExpanded(bucketItemBox = bucketItemBox)
        }
    }
}
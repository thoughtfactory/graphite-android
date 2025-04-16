package com.syncodec.graphite.presentation.bucketItem2.composable.screen.bookScreen

import android.net.Uri
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.syncodec.graphite.di.model.importer.ThumbnailData
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemBook
import com.syncodec.graphite.utils.DataLoader
import kotlinx.coroutines.flow.StateFlow


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun BookScreen(
    bucketItemBox: BucketItemBoxDecrypted? = null,
    thumbnailDataFlow: StateFlow<DataLoader<ThumbnailData>>,
    isEditing: Boolean = false,
    onUpdateBucketItemBox: (bucketItemBoxDecrypted: BucketItemBoxDecrypted) -> Unit = {},
    onClickEditBookTitleAuthor: (BucketItemBook?) -> Unit = {},
    onClickEditBookDescription: (BucketItemBook?) -> Unit = {},
    onUpdateThumbnail: (Uri) -> Unit = {}
) {

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val bucketItemData by remember(key1 = bucketItemBox?.bucketItemData) { derivedStateOf { bucketItemBox?.bucketItemData } }
    val bookData by remember(key1 = bucketItemData) { derivedStateOf { bucketItemData as? BucketItemBook } }

    bookData?.let { bookData1 ->
        when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Compact -> when (bookData1::class.simpleName) {
                BucketItemBook.OpenLibrary::class.simpleName -> BookOpenLibraryScreenCompact(
                    bucketItemBox = bucketItemBox,
                    thumbnailDataFlow = thumbnailDataFlow,
                    isEditing = isEditing,
                    onUpdateBucketItemBox = onUpdateBucketItemBox,
                    onClickEditBookTitleAuthor = onClickEditBookTitleAuthor,
                    onClickEditBookDescription = onClickEditBookDescription,
                    onUpdateThumbnail = onUpdateThumbnail
                )

                BucketItemBook.Custom::class.simpleName -> BookOpenLibraryScreenCompact(
                    bucketItemBox = bucketItemBox,
                    thumbnailDataFlow = thumbnailDataFlow,
                    isEditing = isEditing,
                    onUpdateBucketItemBox = onUpdateBucketItemBox,
                    onClickEditBookTitleAuthor = onClickEditBookTitleAuthor,
                    onClickEditBookDescription = onClickEditBookDescription,
                    onUpdateThumbnail = onUpdateThumbnail
                )

                else -> TODO()
            }

            WindowWidthSizeClass.Medium -> BookScreenExpanded(bucketItemBox = bucketItemBox)
            WindowWidthSizeClass.Expanded -> BookScreenExpanded(bucketItemBox = bucketItemBox)
        }
    }
}
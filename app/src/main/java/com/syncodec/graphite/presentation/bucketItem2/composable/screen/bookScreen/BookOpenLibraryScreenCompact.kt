package com.syncodec.graphite.presentation.bucketItem2.composable.screen.bookScreen

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.importer.ThumbnailData
import com.syncodec.graphite.di.modelObjectBox.BucketBoxEncrypted
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemBook
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemData
import com.syncodec.graphite.presentation.bucketItem2.composable.buildingBlock.TitleView
import com.syncodec.graphite.presentation.bucketItem2.composable.buildingBlock.BucketItemStateView
import com.syncodec.graphite.presentation.bucketItem2.composable.buildingBlock.DataChip
import com.syncodec.graphite.presentation.bucketItem2.composable.buildingBlock.DescriptionView
import com.syncodec.graphite.presentation.bucketItem2.composable.buildingBlock.ThumbnailView
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import com.syncodec.graphite.utils.DataLoader
import kotlinx.coroutines.flow.StateFlow


@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalLayoutApi::class)
@Composable
fun BookOpenLibraryScreenCompact(
    bucketItemBox: BucketItemBoxDecrypted? = null,
    thumbnailDataFlow: StateFlow<DataLoader<ThumbnailData>>,
    isEditing: Boolean = false,
    onToggleBucketItemState: (BucketItemBoxDecrypted.State) -> Unit = {},
    onClickEditBookTitleAuthor: (BucketItemBook?) -> Unit = {},
    onClickEditBookDescription: (BucketItemBook?) -> Unit = {},
    onUpdateThumbnail: (Uri) -> Unit = {}
) {
    val context = LocalContext.current

    val bucketItemData by remember(key1 = bucketItemBox?.bucketItemData) { derivedStateOf { bucketItemBox?.bucketItemData } }
    val bookData by remember(key1 = bucketItemData) { derivedStateOf { bucketItemData as? BucketItemBook } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
    ) {

        Spacer(modifier = Modifier.height(height = 16.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            ThumbnailView(thumbnailDataFlow = thumbnailDataFlow, isEditing = isEditing, onUpdateThumbnail = onUpdateThumbnail)
        }
        Spacer(modifier = Modifier.height(height = 24.dp))

        AnimatedVisibility(
            visible = !isEditing,
            enter = AnimationDefaults.ExpandVerticallyEnter,
            exit = AnimationDefaults.ShrinkVerticallyExit
        ) {
            Column {
                BucketItemStateView(
                    bucketType = BucketBoxEncrypted.BucketType.Book,
                    bucketItemState = bucketItemBox?.state?.ordinal ?: BucketItemBoxDecrypted.State.Alpha.ordinal,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    onToggleBucketItemState = onToggleBucketItemState
                )
                Spacer(modifier = Modifier.height(height = 12.dp))
            }
        }

        TitleView(
            title = bookData?.bookTitle(),
            subTitle = bookData?.allBookAuthor()?.firstOrNull(),
            isEditing = isEditing,
            onClick = { onClickEditBookTitleAuthor(bookData) }
        )
        Spacer(modifier = Modifier.height(height = 12.dp))

        DescriptionView(
            description = bookData?.bookDescription(),
            isEditing = isEditing,
            onClick = { onClickEditBookDescription(bookData) }
        )

        Spacer(modifier = Modifier.height(height = 8.dp))

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 0.dp)
        ) {
            bookData?.bookNumberOfPages()?.let { DataChip(text = "$it ${context.getString(R.string.pages)}") }
            bookData?.bookPublicationYear()?.let { DataChip(text = "$it") }
        }

        Spacer(modifier = Modifier.height(height = 8.dp))

        AnimatedVisibility(
            visible = !isEditing,
            enter = AnimationDefaults.ExpandVerticallyEnter,
            exit = AnimationDefaults.ShrinkVerticallyExit
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f)),
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.il_logo_open_library),
                    contentDescription = "Open Library",
                    modifier = Modifier
                        .fillMaxWidth(fraction = 0.471f)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }

        Spacer(modifier = Modifier.height(height = 128.dp))
    }
}

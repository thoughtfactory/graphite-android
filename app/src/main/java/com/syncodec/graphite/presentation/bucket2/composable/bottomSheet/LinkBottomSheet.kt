package com.syncodec.graphite.presentation.bucket2.composable.bottomSheet

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketBoxEncrypted
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemLink
import com.syncodec.graphite.di.network.openGraph.LinkData
import com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.buildingBlock.FavouriteButton
import com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.buildingBlock.LockButton
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.BucketItemStateView
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.v2.buildingBlock.KeyValueCard
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class, ExperimentalUuidApi::class)
@Composable
fun LinkBottomSheet(
    bottomSheet2State: GenericBottomSheet2State<BucketItemBoxDecrypted> = GenericBottomSheet2State.rememberGenericBottomSheet2StateT(),
    onUpdateBucketItemBox: (BucketItemBoxDecrypted) -> Unit = {}
) {
    val bucketItemBox by bottomSheet2State.dataFlow.collectAsState()

    val linkData by remember(key1 = bucketItemBox?.bucketItemData) { derivedStateOf { bucketItemBox?.bucketItemData as? BucketItemLink } }

    var bucketItemState: BucketItemBoxDecrypted.State by remember(key1 = bucketItemBox?.state) { mutableStateOf(value = bucketItemBox?.state ?: BucketItemBoxDecrypted.State.Alpha) }
    var isFavourite by remember(key1 = bucketItemBox?.isFavourite) { mutableStateOf(value = bucketItemBox?.isFavourite == true) }
    var isLocked by remember(key1 = bucketItemBox?.isLocked) { mutableStateOf(value = bucketItemBox?.isLocked == true) }

    fun updateBucketItemObject() {
        val updatedBucketItemBox = bucketItemBox?.copy(
            state = bucketItemState,
            isLocked = isLocked,
            isFavourite = isFavourite,
            bucketItemData = linkData
        ) ?: return

        onUpdateBucketItemBox(updatedBucketItemBox)
    }

    GenericBottomSheet2(
        bottomSheetState = bottomSheet2State,
    ) {
        GenericBottomSheetSkeleton2(
            title = stringResource(id = R.string.link),
        ) {

            Spacer(modifier = Modifier.height(height = 12.dp))

            linkData?.linkData?.let { linkData1 ->
                SuccessView(
                    linkData = linkData1,
                    isFavourite = isFavourite,
                    isLocked = isLocked,
                    bucketItemState = bucketItemState,
                    onUpdateBucketItemState = { bucketItemState = it; updateBucketItemObject() },
                    onClickFavourite = { isFavourite = it; updateBucketItemObject() },
                    onClickLock = { isLocked = it; updateBucketItemObject() }
                )
            }

            Spacer(modifier = Modifier.height(height = 12.dp))

            Button(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                onClick = ::updateBucketItemObject,
                content = { Text(text = stringResource(id = R.string.add_todo)) }
            )
        }
    }
}

@Composable
private fun SuccessView(
    linkData: LinkData,
    isFavourite: Boolean = false,
    isLocked: Boolean = false,
    bucketItemState: BucketItemBoxDecrypted.State = BucketItemBoxDecrypted.State.Alpha,
    onUpdateBucketItemState: (BucketItemBoxDecrypted.State) -> Unit = {},
    onClickFavourite: (Boolean) -> Unit = {},
    onClickLock: (Boolean) -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current

    Column {
        ThumbnailPreview(base64String = linkData.imageBase64)
        Spacer(modifier = Modifier.height(height = 6.dp))

        KeyValueCard(
            key = stringResource(id = R.string.url),
            value = linkData.url ?: "",
            modifier = Modifier.fillMaxWidth(),
            onClick = { linkData.url?.let { uriHandler.openUri(uri = it) } },
            onLongClick = { clipboardManager.setText(annotatedString = buildAnnotatedString { append(text = linkData.url) }) }
        )
        Spacer(modifier = Modifier.height(height = 6.dp))

        KeyValueCard(
            key = stringResource(id = R.string.title),
            value = linkData.title ?: "",
            modifier = Modifier.fillMaxWidth(),
            onLongClick = { clipboardManager.setText(annotatedString = buildAnnotatedString { append(text = linkData.title) }) }
        )
        Spacer(modifier = Modifier.height(height = 6.dp))

        KeyValueCard(
            key = stringResource(id = R.string.description),
            value = linkData.description ?: "",
            modifier = Modifier.fillMaxWidth(),
            onLongClick = { clipboardManager.setText(annotatedString = buildAnnotatedString { append(text = linkData.description) }) }
        )
        Spacer(modifier = Modifier.height(height = 6.dp))

        KeyValueCard(
            key = stringResource(id = R.string.site_name),
            value = linkData.siteName ?: "",
            modifier = Modifier.fillMaxWidth(),
            onLongClick = { clipboardManager.setText(annotatedString = buildAnnotatedString { append(text = linkData.siteName) }) }
        )
        Spacer(modifier = Modifier.height(height = 6.dp))

        BucketItemStateView(
            bucketType = BucketBoxEncrypted.BucketType.Link,
            bucketItemState = bucketItemState.ordinal,
            onClickBucketItemState = { onUpdateBucketItemState(BucketItemBoxDecrypted.State.entries.get(index = it)) }
        )

        Spacer(modifier = Modifier.height(height = 4.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            FavouriteButton(isFavourite = isFavourite) { onClickFavourite(!isFavourite) }
            Spacer(modifier = Modifier.width(width = 6.dp))
            LockButton(isLocked = isLocked) { onClickLock(!isLocked) }
        }
    }
}

@Composable
private fun ThumbnailPreview(
    base64String: String?,
) {
    val context = LocalContext.current

    var bitmap: Bitmap? by remember { mutableStateOf(null) }
    LaunchedEffect(key1 = Unit) {
        withContext(context = Dispatchers.Default) {
            val _bitmap = base64String?.decodeBase64ToBitmap()
            withContext(Dispatchers.Main) { bitmap = _bitmap }
        }
    }

    if (bitmap != null) SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(data = bitmap)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        alignment = Alignment.Center,
        loading = { CircularProgressIndicator(modifier = Modifier.requiredSize(size = 32.dp), strokeWidth = 2.dp) },
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.background, shape = MaterialTheme.shapes.medium)
            .clip(shape = MaterialTheme.shapes.medium)
    )
}


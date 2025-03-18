package com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.bookCard

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketItemBox
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxPlain
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemBook
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemData
import com.syncodec.graphite.presentation.common.v2.selectable2.LocalSelectionContainerActor
import com.syncodec.graphite.presentation.common.v2.selectable2.SelectableContainer2
import com.syncodec.graphite.presentation.common.v2.selectable2.SelectableContainer2Defaults
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.presentation.ui.LockClosedContainer
import com.syncodec.graphite.utils.alice2.Alice2
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject


@Composable
fun BookGridCard(
    bucketItemBox: BucketItemBoxPlain,
    bucketItemBook: BucketItemBook? = null,
    state: BucketItemData.State,
    isReorderable: Boolean = false,
    isLast: Boolean = false,
    dragHandle: @Composable () -> Unit = {},
    onClickTriStateButton: () -> Unit = {},
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val alice2: Alice2 = koinInject()

    val selectionContainerActor = LocalSelectionContainerActor.current
    val isSelecting by selectionContainerActor.isSelectingFlow.collectAsState()
    val selectedItemIdList by selectionContainerActor.selectedItemIdListFlow.collectAsState()

    SelectableContainer2(
        selected = bucketItemBox.id in selectedItemIdList,
        enabled = true,
        shape = MaterialTheme.shapes.large,
        border = null,
        color = SelectableContainer2Defaults.defaultColors(),
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 6.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio = 0.875f)
            ) {
                ThumbnailPreview(thumbnail = bucketItemBook?.thumbnail(context = context) as? BucketItemData.Companion.Thumbnail.Base64)

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.fillMaxSize()
                ) {
                    AnimatedVisibility(
                        visible = isReorderable && !isSelecting,
                        enter = AnimationDefaults.ScaleAndFadeEnter,
                        exit = AnimationDefaults.ScaleAndFadeExit,
                        content = { dragHandle() }
                    )
                    Spacer(modifier = Modifier.weight(weight = 1f))
                    StatusContainer(isFavourite = bucketItemBox.isFavourite ?: false, isLocked = bucketItemBox.isLocked ?: false)
                }
            }
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Text(
                    text = bucketItemBook?.bookTitle() ?: "-",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                bucketItemBook?.primaryAuthor()?.let {
                    Spacer(modifier = Modifier.height(height = 4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = SelectableContainer2Defaults.defaultColors().onContainerColor.copy(alpha = 0.71f)
                    )
                }
                bucketItemBook?.bookPublicationYear()?.toString()?.let {
                    Spacer(modifier = Modifier.height(height = 4.dp))
                    Text(
                        text = "[$it]",
                        style = MaterialTheme.typography.bodySmall,
                        color = SelectableContainer2Defaults.defaultColors().onContainerColor.copy(alpha = 0.71f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ThumbnailPreview(
    thumbnail: BucketItemData.Companion.Thumbnail.Base64?,
) {
    val context = LocalContext.current

    var bitmap: Bitmap? by remember { mutableStateOf(null) }
    LaunchedEffect(key1 = Unit) { withContext(context = Dispatchers.Default) { thumbnail?.data?.decodeBase64ToBitmap()?.let { withContext(Dispatchers.Main) { bitmap = it } } } }

    if (thumbnail != null) Box(modifier = Modifier) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(data = bitmap)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            loading = { CircularProgressIndicator(modifier = Modifier.requiredSize(size = 32.dp), strokeWidth = 2.dp) },
            modifier = Modifier.fillMaxSize()
        )
    } else Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface)
    ) {
        Text(
            text = stringResource(id = R.string.no_cover_image),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun StatusContainer(
    isFavourite: Boolean,
    isLocked: Boolean,
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val favouriteColor = Color(ColorUtils.blendARGB(Color.FavouriteContainer.toArgb(), backgroundColor.toArgb(), 0.31f))
    val lockColor = Color(ColorUtils.blendARGB(Color.LockClosedContainer.toArgb(), backgroundColor.toArgb(), 0.31f))

    if (isFavourite || isLocked) Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(all = 4.dp)
            .background(color = MaterialTheme.colorScheme.background.copy(alpha = 0.71f), shape = MaterialTheme.shapes.small)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        if (isFavourite) Icon(
            painter = painterResource(id = R.drawable.ic_fa_heart_solid),
            contentDescription = stringResource(id = R.string.favourite),
            tint = favouriteColor,
            modifier = Modifier.size(size = 14.dp)
        )
        if (isFavourite && isLocked) Spacer(modifier = Modifier.width(width = 4.dp))
        if (isLocked) Icon(
            painter = painterResource(id = R.drawable.ic_fa_lock_closed_solid),
            contentDescription = stringResource(id = R.string.entry_locked),
            tint = lockColor,
            modifier = Modifier.size(size = 14.dp)
        )
    }
}

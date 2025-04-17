package com.syncodec.graphite.presentation.bucket2.composable.bottomSheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import coil3.BitmapImage
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketBoxEncrypted
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemLink
import com.syncodec.graphite.di.network.NetworkResponse
import com.syncodec.graphite.di.network.openGraph.LinkData
import com.syncodec.graphite.di.network.openGraph.OpenGraphApi
import com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.buildingBlock.FavouriteButton
import com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.buildingBlock.LockButton
import com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.buildingBlock.SearchErrorView
import com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.buildingBlock.SearchInitView
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.BucketItemStateView
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.v2.textField2.GenericTextField2
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import com.syncodec.graphite.utils.encodeBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import org.koin.compose.koinInject
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class, ExperimentalUuidApi::class)
@Composable
fun AddLinkBottomSheet(
    bottomSheet2State: GenericBottomSheet2State<Nothing> = GenericBottomSheet2State.rememberGenericBottomSheet2State(),
    onAddLink: (bucketItemBox: BucketItemBoxDecrypted) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val openGraphApi: OpenGraphApi = koinInject()

    val linkTextFieldController = GenericTextField2.rememberTextField2Controller(initialFocus = true)

    var bucketItemState: BucketItemBoxDecrypted.State by remember { mutableStateOf(value = BucketItemBoxDecrypted.State.Alpha) }
    var isFavourite by remember { mutableStateOf(value = false) }
    var isLocked by remember { mutableStateOf(value = false) }

    var linkDataNetworkResponse: NetworkResponse<LinkData> by remember { mutableStateOf(value = NetworkResponse.Init) }
    var toSaveLinkData: LinkData? by remember { mutableStateOf(value = null) }

    fun getPreview() {
        val linkValidationResult = linkTextFieldController.validate { it.isNotBlank() }
        if (linkValidationResult.isValidated) {
            scope.launch(context = Dispatchers.IO) {
                openGraphApi.getLinkDataPreview(url = linkValidationResult.text) { networkResponse ->
                    linkDataNetworkResponse = networkResponse
                    toSaveLinkData = (networkResponse as? NetworkResponse.Success)?.data
                }
            }
        }
    }

    fun saveBucketItem() {
        val linkValidationResult = linkTextFieldController.validate { it.isNotBlank() }
        if (linkValidationResult.isValidated) {
            val bucketItemData = BucketItemLink(linkData = toSaveLinkData ?: return)
            val bucketItemBox = BucketItemBoxDecrypted.newInstance.copy(
                bucketItemData = bucketItemData,
                state = bucketItemState,
                isFavourite = isFavourite,
                isLocked = isLocked
            )

            onAddLink(bucketItemBox)

            linkTextFieldController.reset()
            bottomSheet2State.hideSheet(scope = scope)
        }
    }

    GenericBottomSheet2(
        bottomSheetState = bottomSheet2State,
    ) {
        GenericBottomSheetSkeleton2(
            title = stringResource(id = R.string.add_link),
        ) {

            Spacer(modifier = Modifier.height(height = 12.dp))

            GenericTextField2.BottomSheetTextField(
                controller = linkTextFieldController,
                label = stringResource(id = R.string.http_https),
                placeholder = stringResource(id = R.string.add_to_list),
                errorMessage = stringResource(id = R.string.todo_item_title_error),
                keyboardOptions = GenericTextField2.Options.getTextNextKeyboardOptionsDefault()
            )

            Spacer(modifier = Modifier.height(height = 8.dp))

            AnimatedContent(
                targetState = linkDataNetworkResponse,
                transitionSpec = { AnimationDefaults.Fade },
                modifier = Modifier.fillMaxWidth()
            ) {
                when (it) {
                    is NetworkResponse.Init -> SearchInitView(bucketType = BucketBoxEncrypted.BucketType.Link)
                    is NetworkResponse.Loading -> LoadingView()
                    is NetworkResponse.Error -> SearchErrorView()
                    is NetworkResponse.Success -> SuccessView(
                        linkData = it.data,
                        bucketItemState = bucketItemState,
                        isFavourite = isFavourite,
                        isLocked = isLocked,
                        onGetThumbnail = { bitmapImage -> toSaveLinkData = toSaveLinkData?.copy(imageBase64 = bitmapImage.bitmap.encodeBase64()) },
                        onUpdateBucketItemState = { bucketItemState = it },
                        onClickFavourite = { isFavourite = it },
                        onClickLock = { isLocked = it },
                    )
                }
            }

            Spacer(modifier = Modifier.height(height = 12.dp))

            OutlinedButton(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                onClick = ::getPreview,
                content = { Text(text = stringResource(id = R.string.preview)) }
            )
            Button(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                onClick = ::saveBucketItem,
                content = { Text(text = stringResource(id = R.string.add_link)) }
            )
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(
            modifier = Modifier.requiredSize(size = 32.dp),
            strokeWidth = 2.dp
        )
    }
}

@Composable
private fun SuccessView(
    linkData: LinkData,
    bucketItemState: BucketItemBoxDecrypted.State = BucketItemBoxDecrypted.State.Alpha,
    isFavourite: Boolean = false,
    isLocked: Boolean = false,
    onGetThumbnail: (BitmapImage) -> Unit = {},
    onUpdateBucketItemState: (BucketItemBoxDecrypted.State) -> Unit = {},
    onClickFavourite: (Boolean) -> Unit = {},
    onClickLock: (Boolean) -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        ThumbnailPreview(thumbnailUrl = linkData.imagePath, onGetThumbnail = onGetThumbnail)
        Spacer(modifier = Modifier.height(height = 6.dp))

        GenericTextField2.BottomSheetTextView(
            key = stringResource(id = R.string.url),
            value = linkData.url ?: "",
            onClick = {
                try {
                    linkData.url ?: return@BottomSheetTextView
                    uriHandler.openUri(uri = linkData.url)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            onLongClick = { clipboardManager.setText(annotatedString = buildAnnotatedString { append(linkData.url ?: return@BottomSheetTextView) }) }
        )
        Spacer(modifier = Modifier.height(height = 6.dp))

        GenericTextField2.BottomSheetTextView(
            key = stringResource(id = R.string.title),
            value = linkData.title ?: "",
            onLongClick = { clipboardManager.setText(annotatedString = buildAnnotatedString { append(linkData.title ?: return@BottomSheetTextView) }) }
        )
        Spacer(modifier = Modifier.height(height = 6.dp))

        GenericTextField2.BottomSheetTextView(
            key = stringResource(id = R.string.description),
            value = linkData.description ?: "",
            onLongClick = { clipboardManager.setText(annotatedString = buildAnnotatedString { append(linkData.description ?: return@BottomSheetTextView) }) }
        )
        Spacer(modifier = Modifier.height(height = 6.dp))

        GenericTextField2.BottomSheetTextView(
            key = stringResource(id = R.string.site_name),
            value = linkData.siteName ?: "",
        )
        Spacer(modifier = Modifier.height(height = 4.dp))

        BucketItemStateView(
            bucketType = BucketBoxEncrypted.BucketType.Link,
            bucketItemState = bucketItemState.ordinal,
            onClickBucketItemState = { onUpdateBucketItemState(BucketItemBoxDecrypted.State.entries.get(index = it)) }
        )

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            FavouriteButton(isFavourite = isFavourite) { onClickFavourite(!isFavourite) }
            Spacer(modifier = Modifier.width(width = 4.dp))
            LockButton(isLocked = isLocked) { onClickLock(!isLocked) }
        }

        Spacer(modifier = Modifier.height(height = 12.dp))

        HorizontalDivider(modifier = Modifier.fillMaxWidth(fraction = 0.71f))
    }
}

@Composable
private fun ThumbnailPreview(
    thumbnailUrl: String?,
    onGetThumbnail: (BitmapImage) -> Unit = {}
) {
    val context = LocalContext.current

    if (thumbnailUrl != null) SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(data = thumbnailUrl)
            .listener(
                onSuccess = { request, response -> (response.image as? BitmapImage)?.let(block = onGetThumbnail) },
            )
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


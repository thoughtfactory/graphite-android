package com.syncodec.graphite.presentation.bucketItem2.composable.screen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketBoxEncrypted
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemBook
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemShow
import com.syncodec.graphite.presentation.bucketItem2.BucketItemViewModel2
import com.syncodec.graphite.presentation.bucketItem2.composable.bar.BottomBar
import com.syncodec.graphite.presentation.bucketItem2.composable.bar.TopBar
import com.syncodec.graphite.presentation.bucketItem2.composable.bottomSheet.EditBookDescriptionBottomSheet
import com.syncodec.graphite.presentation.bucketItem2.composable.bottomSheet.EditBookTitleAuthorBottomSheet
import com.syncodec.graphite.presentation.bucketItem2.composable.bottomSheet.MetadataBottomSheet
import com.syncodec.graphite.presentation.bucketItem2.composable.screen.bookScreen.BookScreen
import com.syncodec.graphite.presentation.bucketItem2.composable.screen.movieScreen.MovieScreen
import com.syncodec.graphite.presentation.bucketItem2.composable.screen.seriesScreen.SeriesScreen
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.v2.scaffold2.GenericScaffold2
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import com.syncodec.graphite.presentation.ui.ICON_SIZE
import com.syncodec.graphite.utils.DataLoader
import dev.chrisbanes.haze.HazeState
import org.koin.androidx.compose.koinViewModel
import kotlin.Unit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BucketItemScreen(
    viewModel: BucketItemViewModel2 = koinViewModel()
) {

    val isDataSaved by viewModel.isDataSaved.collectAsState()
    val bucketType by viewModel.bucketType.collectAsState()
    val bucketItemBoxDataFlow = viewModel.bucketItemBoxDataFlow

    val metadataBottomSheetState = GenericBottomSheet2State.rememberGenericBottomSheet2State(skipPartiallyExpanded = true)

    val editBookTitleAuthorBottomSheetState = GenericBottomSheet2State.rememberGenericBottomSheet2StateT<BucketItemBook?>(skipPartiallyExpanded = true)
    val editBookDescriptionBottomSheetState = GenericBottomSheet2State.rememberGenericBottomSheet2StateT<BucketItemBook?>(skipPartiallyExpanded = true)

    var isEditing by remember { mutableStateOf(value = false) }


    BackHandler(enabled = isEditing) { isEditing = false }

    GenericScaffold2(
        topBar = {
            TopBar(
                bucketItemBoxDataFlow = bucketItemBoxDataFlow,
                isDataSaved = isDataSaved == true,
                isEditing = isEditing,
                onClickEdit = { isEditing = true },
                onToggleLock = viewModel::onToggleLock,
                onToggleFavourite = viewModel::onToggleFavourite
            )
        },
        bottomBar = { BottomBar(isEditing = isEditing, onClickMetadata = { metadataBottomSheetState.openSheet() }) },
        floatingActionButton = {
            SaveFAButton(
                isDataSaved = isDataSaved,
                onClickSave = viewModel::putBucketItemBox,
            )
        },
        bottomSheetContent = {
            val bucketItemBoxData by bucketItemBoxDataFlow.collectAsState()
            MetadataBottomSheet(
                bottomSheet2State = metadataBottomSheetState,
                bucketItemBox = (bucketItemBoxData as? DataLoader.Loaded)?.data,
                onClickEdit = {},
            )
            EditBookTitleAuthorBottomSheet(
                bottomSheet2State = editBookTitleAuthorBottomSheetState,
                onUpdateBucketItemData = { viewModel.updateBucketItemData(bucketItemData = it) }
            )
            EditBookDescriptionBottomSheet(
                bottomSheet2State = editBookDescriptionBottomSheetState,
                onUpdateBucketItemData = { viewModel.updateBucketItemData(bucketItemData = it) }
            )
        }
    ) {
        val bucketItemBoxData by viewModel.bucketItemBoxDataFlow.collectAsState()
        val bucketItemBoxDataString by remember { derivedStateOf { bucketItemBoxData::class.simpleName } }
        Log.d("bucketItemBoxDataString", "bucketItemBoxDataString")

//        bucketItemBoxData::class.simpleName is used to not reanimate if internal data is changed line favourite, lock
        AnimatedContent(
            targetState = bucketItemBoxDataString,
            transitionSpec = { AnimationDefaults.Fade },
            modifier = Modifier.fillMaxSize()
        ) { bucketItemBoxData1 ->
            Log.d("bucketItemBoxData1", "bucketItemBoxData1 : $bucketItemBoxData1")

            val bucketItemBox by remember(key1 = bucketItemBoxData) { derivedStateOf { (bucketItemBoxData as? DataLoader.Loaded)?.data } }

            when (bucketItemBoxData1) {
                DataLoader.Init::class.simpleName -> DataLoaderLoadingView()
                DataLoader.Loading::class.simpleName -> DataLoaderLoadingView()
                DataLoader.NoData::class.simpleName -> Unit
                DataLoader.Error::class.simpleName -> Unit
                DataLoader.Loaded::class.simpleName -> when (bucketType) {
                    BucketBoxEncrypted.BucketType.Todo -> Unit
                    BucketBoxEncrypted.BucketType.Book -> BookScreen(
                        bucketItemBox = bucketItemBox,
                        thumbnailDataFlow = viewModel.thumbnailDataFlow,
                        isEditing = isEditing,
                        onToggleBucketItemState = viewModel::onToggleBucketItemState,
                        onClickEditBookTitleAuthor = { editBookTitleAuthorBottomSheetState.openSheet(data = it) },
                        onClickEditBookDescription = { editBookDescriptionBottomSheetState.openSheet(data = it) },
                        onUpdateThumbnail = viewModel::onUpdateThumbnail
                    )

                    BucketBoxEncrypted.BucketType.Show -> when (bucketItemBox?.bucketItemData) {
                        is BucketItemShow.TraktMovie -> MovieScreen(bucketItemBox = bucketItemBox, thumbnailDataFlow = viewModel.thumbnailDataFlow, onToggleBucketItemState = viewModel::onToggleBucketItemState)
                        is BucketItemShow.TraktSeries -> SeriesScreen(bucketItemBox = bucketItemBox, thumbnailDataFlow = viewModel.thumbnailDataFlow, onToggleBucketItemState = viewModel::onToggleBucketItemState)
                        else -> Unit
                    }

                    BucketBoxEncrypted.BucketType.Link -> Unit
                    BucketBoxEncrypted.BucketType.Location -> Unit
                    BucketBoxEncrypted.BucketType.Unknown -> Unit
                    null -> Unit
                }
            }
        }
    }
}

@Composable
private fun DataLoaderLoadingView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(height = 4.dp))
        Text(text = stringResource(id = R.string.loading))
    }
}

@Composable
private fun SaveFAButton(
    isDataSaved: Boolean? = null,
    onClickSave: () -> Unit = {},
) {
    AnimatedVisibility(
        visible = isDataSaved == false,
        enter = AnimationDefaults.ScaleEnter,
        exit = AnimationDefaults.ScaleExit,
    ) {
        LargeFloatingActionButton(onClick = onClickSave) {
            Icon(
                painter = painterResource(id = R.drawable.ic_fa_check),
                contentDescription = null,
                modifier = Modifier.size(size = ICON_SIZE)
            )
        }
    }
}

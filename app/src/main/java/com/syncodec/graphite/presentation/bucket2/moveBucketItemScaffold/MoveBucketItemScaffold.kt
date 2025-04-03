package com.syncodec.graphite.presentation.bucket2.moveBucketItemScaffold

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketBoxEncrypted
import com.syncodec.graphite.presentation.bucket2.moveBucketItemScaffold.bar.BottomBar
import com.syncodec.graphite.presentation.bucket2.moveBucketItemScaffold.bar.TopBar
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.v2.buildingBlock.KeyValueCard
import com.syncodec.graphite.presentation.common.v2.button.GraIcon
import com.syncodec.graphite.presentation.common.v2.overlayScaffold.OverlayScaffold
import com.syncodec.graphite.presentation.common.v2.overlayScaffold.OverlayScaffold.State
import com.syncodec.graphite.presentation.main2.composable.bottomSheet.NewBucketBottomSheet
import com.syncodec.graphite.presentation.main2.composable.buildingBlock.bucket.BucketCard
import com.syncodec.graphite.utils.alice2.Alice2
import kotlinx.serialization.InternalSerializationApi
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject


data class MoveBucketItemData(
    val bucketType: BucketBoxEncrypted.BucketType,
    val selectedItemIdList: List<Long>
)

@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class)
@Composable
fun MoveBucketItemScaffold(
    state: State<MoveBucketItemData> = State.rememberOverlayStateT(),
) {
    val alice2: Alice2 = koinInject()

    val viewModel: MoveBucketItemViewModel = koinViewModel()

    val moveBucketItemData by state.dataFlow.collectAsState()
    val allBucketBoxList by viewModel.allBucketBoxListFlow.collectAsState(initial = listOf())

    val newBucketListBottomSheet = GenericBottomSheet2State.rememberGenericBottomSheet2State(skipPartiallyExpanded = true)

    OverlayScaffold.Composable(
        state = state,
        topBar = { TopBar() },
        bottomBar = { BottomBar() },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { newBucketListBottomSheet.openSheet() }
            ) { GraIcon(icon = R.drawable.ic_fa_plus, contentDescription = stringResource(id = R.string.new_bucket)) }
        },
        bottomSheetContent = {
            NewBucketBottomSheet(
                bottomSheet2State = newBucketListBottomSheet,
                onCreateNewBucket = viewModel::putBucketBox
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            KeyValueCard.Composable(
                value = "${moveBucketItemData?.selectedItemIdList?.size} " + stringResource(id = R.string.items_selected),
                colors = KeyValueCard.Defaults.infoColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            )

            LazyVerticalGrid(
                modifier = Modifier.weight(weight = 1f),
                columns = GridCells.Adaptive(minSize = 144.dp),
                contentPadding = PaddingValues(all = 8.dp)
            ) {
                items(
                    items = allBucketBoxList.filter { it.enc.bucketType?.decrypt(alice2) == moveBucketItemData?.bucketType },
                    contentType = { 0 },
                    key = { it.enc.id },
                ) { bucketBoxCache ->
                    val bucketBox = bucketBoxCache.decryptBlocking(alice2)

                    BucketCard(
                        titleText = bucketBox?.title,
                        descriptionText = bucketBox?.description,
                        bucketType = bucketBox?.bucketType ?: BucketBoxEncrypted.BucketType.Unknown,
                        bucketSize = bucketBoxCache.enc.childList.size,
                        selected = false,
                        onClick = {
                            viewModel.moveBucketItemBox(bucketBox = bucketBoxCache.enc, bucketItemIdList = moveBucketItemData?.selectedItemIdList ?: return@BucketCard) {
                                state.closeOverlay(data = null)
                            }
                        }
                    )
                }
            }
        }
    }
}

package com.syncodec.graphite.presentation.bucketItem2.composable.bottomSheet

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.structureExtension.toPretty
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.BottomSheetActionButton
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.v2.textField2.GenericTextField2
import com.syncodec.graphite.utils.DataLoader
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.InternalSerializationApi
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class, ExperimentalLayoutApi::class, ExperimentalUuidApi::class)
@Composable
fun MetadataBottomSheet(
    bottomSheet2State: GenericBottomSheet2State<Nothing> = GenericBottomSheet2State.rememberGenericBottomSheet2State(),
    bucketItemBoxDataFlow: StateFlow<DataLoader<BucketItemBoxDecrypted>>,
    bucketBoxDataFlow: StateFlow<DataLoader<BucketBoxDecrypted>>,
    onClickMove: () -> Unit = {},
    onClickShare: () -> Unit = {},
    onClickDelete: () -> Unit = {},
) {

    val bucketItemBoxData by bucketItemBoxDataFlow.collectAsState()
    val bucketBoxData by bucketBoxDataFlow.collectAsState()

    val bucketItemBox by remember(key1 = bucketItemBoxData) { derivedStateOf { (bucketItemBoxData as? DataLoader.Loaded)?.data } }
    val bucketBox by remember(key1 = bucketBoxData) { derivedStateOf { (bucketBoxData as? DataLoader.Loaded)?.data } }

    GenericBottomSheet2(
        bottomSheetState = bottomSheet2State,
    ) {
        GenericBottomSheetSkeleton2(
            title = stringResource(id = R.string.metadata),
        ) {

            Spacer(modifier = Modifier.height(height = 8.dp))

            GenericTextField2.BottomSheetTextView(
                key = stringResource(id = R.string.id),
                value = bucketItemBox?.uuid?.toString() ?: "-"
            )
            Spacer(modifier = Modifier.height(height = 6.dp))
            GenericTextField2.BottomSheetTextView(
                key = stringResource(id = R.string.created_on),
                value = bucketItemBox?.createdTimestamp?.toPretty() ?: "-"
            )
            Spacer(modifier = Modifier.height(height = 6.dp))
            GenericTextField2.BottomSheetTextView(
                key = stringResource(id = R.string.modified_on),
                value = bucketItemBox?.modifiedTimestamp?.toPretty() ?: "-"
            )
            Spacer(modifier = Modifier.height(height = 6.dp))
            GenericTextField2.BottomSheetTextView(
                key = stringResource(id = R.string.parent),
                value = "${bucketBox?.uuid?.toString() ?: "-"}\n${bucketBox?.title ?: "-"}"
            )

            Spacer(modifier = Modifier.height(height = 16.dp))
            HorizontalDivider(modifier = Modifier.fillMaxWidth(fraction = 0.71f))
            Spacer(modifier = Modifier.height(height = 12.dp))

            BottomSheetActionButton.BottomSheetActionGrid(itemInRow = 3) {
                BottomSheetActionButton.MoveButton(itemInRow = 3, onClick = onClickMove)
                BottomSheetActionButton.ShareButton(itemInRow = 3, onClick = onClickShare)
                BottomSheetActionButton.DeleteButton(itemInRow = 3, onClick = onClickDelete)
            }

            Spacer(modifier = Modifier.height(height = 8.dp))
        }
    }
}

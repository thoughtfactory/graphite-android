package com.syncodec.graphite.presentation.bucketItem2.composable.bottomSheet

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.structureExtension.toPretty
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.BottomSheetActionButton
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.BottomSheetKeyValue
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheetSkeleton2
import kotlinx.serialization.InternalSerializationApi


@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class, ExperimentalLayoutApi::class)
@Preview
@Composable
fun MetadataBottomSheet(
    bottomSheet2State: GenericBottomSheet2State<Nothing> = GenericBottomSheet2State.rememberGenericBottomSheet2State(),
    bucketItemBox: BucketItemBoxDecrypted? = null,
    onClickEdit: () -> Unit = {}
) {
    GenericBottomSheet2(
        bottomSheetState = bottomSheet2State,
    ) {
        GenericBottomSheetSkeleton2(
            title = stringResource(id = R.string.metadata),
        ) {

            Spacer(modifier = Modifier.height(height = 8.dp))

            BottomSheetKeyValue.Composable(
                key = stringResource(id = R.string.id),
                value = bucketItemBox?.id?.toString() ?: "-"
            )
            BottomSheetKeyValue.Composable(
                key = stringResource(id = R.string.created_on),
                value = bucketItemBox?.createdTimestamp?.toPretty() ?: "-"
            )
            BottomSheetKeyValue.Composable(
                key = stringResource(id = R.string.modified_on),
                value = bucketItemBox?.modifiedTimestamp?.toPretty() ?: "-"
            )


            Spacer(modifier = Modifier.height(height = 4.dp))

            BottomSheetActionButton.BottomSheetActionGrid(itemInRow = 3) {
                BottomSheetActionButton.EditButton(itemInRow = 3, onClick = onClickEdit)
                BottomSheetActionButton.ShareButton(itemInRow = 3) {}
                BottomSheetActionButton.DeleteButton(itemInRow = 3) {}
            }

            Spacer(modifier = Modifier.height(height = 8.dp))
        }
    }
}


package com.syncodec.graphite.presentation.bucket2.composable.bottomSheet

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.structureExtension.toPretty
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.BottomSheetActionButton
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.v2.textField2.GenericTextField2
import kotlinx.serialization.InternalSerializationApi
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class, ExperimentalLayoutApi::class, ExperimentalUuidApi::class)
@Preview
@Composable
fun MetadataBottomSheet(
    bottomSheet2State: GenericBottomSheet2State<Nothing> = GenericBottomSheet2State.rememberGenericBottomSheet2State(),
    bucketBox: BucketBoxDecrypted? = null,
    onClickEdit: () -> Unit = {}
) {
    GenericBottomSheet2(
        bottomSheetState = bottomSheet2State,
    ) {
        GenericBottomSheetSkeleton2(
            title = stringResource(id = R.string.metadata),
        ) {

            Spacer(modifier = Modifier.height(height = 8.dp))

            GenericTextField2.BottomSheetTextView(
                key = stringResource(id = R.string.id),
                value = bucketBox?.uuid?.toString() ?: "-"
            )
            Spacer(modifier = Modifier.height(height = 6.dp))
            GenericTextField2.BottomSheetTextView(
                key = stringResource(id = R.string.created_on),
                value = bucketBox?.createdTimestamp?.toPretty() ?: "-"
            )
            Spacer(modifier = Modifier.height(height = 6.dp))
            GenericTextField2.BottomSheetTextView(
                key = stringResource(id = R.string.modified_on),
                value = bucketBox?.modifiedTimestamp?.toPretty() ?: "-"
            )
            Spacer(modifier = Modifier.height(height = 6.dp))
            GenericTextField2.BottomSheetTextView(
                key = stringResource(id = R.string.description),
                value = bucketBox?.description ?: "-"
            )

            Spacer(modifier = Modifier.height(height = 16.dp))
            HorizontalDivider(modifier = Modifier.fillMaxWidth(fraction = 0.71f))
            Spacer(modifier = Modifier.height(height = 12.dp))

            BottomSheetActionButton.BottomSheetActionGrid(itemInRow = 2) {
                BottomSheetActionButton.EditButton(itemInRow = 2, onClick = onClickEdit)
                BottomSheetActionButton.DeleteButton(itemInRow = 2) {}
            }

            Spacer(modifier = Modifier.height(height = 8.dp))
        }
    }
}


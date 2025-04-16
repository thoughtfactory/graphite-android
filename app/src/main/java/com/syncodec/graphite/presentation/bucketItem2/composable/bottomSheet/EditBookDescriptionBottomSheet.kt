package com.syncodec.graphite.presentation.bucketItem2.composable.bottomSheet

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemBook
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.v2.textField2.GenericTextField2
import kotlinx.serialization.InternalSerializationApi


@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class, ExperimentalLayoutApi::class)
@Preview
@Composable
fun EditBookDescriptionBottomSheet(
    bottomSheet2State: GenericBottomSheet2State<BucketItemBook?> = GenericBottomSheet2State.rememberGenericBottomSheet2StateT(),
    onUpdateBucketItemData: (BucketItemBook) -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    val bucketItemBook by bottomSheet2State.dataFlow.collectAsState()

    val descriptionTextFieldController = GenericTextField2.rememberTextField2Controller(initialFocus = false)

    LaunchedEffect(key1 = bucketItemBook) {
        descriptionTextFieldController.onValueChange(value = bucketItemBook?.bookDescription() ?: "")
    }

    fun update() {
        val descriptionValidationResult = descriptionTextFieldController.validate { true }
        val updatedBucketItemBook = bucketItemBook?.toCustom()?.copy(
            description = descriptionValidationResult.text
        ) ?: BucketItemBook.Custom(
            description = descriptionValidationResult.text
        )
        bottomSheet2State.hideSheet(scope = scope) { onUpdateBucketItemData(updatedBucketItemBook) }
    }

    GenericBottomSheet2(
        bottomSheetState = bottomSheet2State,
    ) {
        GenericBottomSheetSkeleton2(
            title = stringResource(id = R.string.edit),
        ) {

            Spacer(modifier = Modifier.height(height = 8.dp))

            GenericTextField2.BottomSheetTextField(
                controller = descriptionTextFieldController,
                label = stringResource(R.string.description_optional),
                placeholder = stringResource(R.string.book_description),
                minLines = 4,
                maxLines = Int.MAX_VALUE,
                singleLine = false
            )

            Spacer(modifier = Modifier.height(height = 4.dp))

            Button(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                onClick = ::update,
                content = { Text(text = stringResource(id = R.string.save)) }
            )
        }
    }
}


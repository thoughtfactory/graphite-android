package com.syncodec.graphite.presentation.bucket2.composable.bottomSheet

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
import com.syncodec.graphite.di.modelObjectBox.BucketBoxDecrypted
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.v2.textField2.GenericTextField2
import com.syncodec.graphite.presentation.common.v2.textField2.GenericTextField2Defaults
import com.syncodec.graphite.presentation.common.v2.textField2.rememberTextField2Controller
import kotlinx.serialization.InternalSerializationApi
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class, ExperimentalLayoutApi::class, ExperimentalUuidApi::class)
@Preview
@Composable
fun EditBottomSheet(
    bottomSheet2State: GenericBottomSheet2State<Nothing> = GenericBottomSheet2State.rememberGenericBottomSheet2State(),
    bucketBox: BucketBoxDecrypted? = null,
    onUpdateBucket: (BucketBoxDecrypted) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val isBottomSheetVisible by bottomSheet2State.isBottomSheetVisibleFlow.collectAsState()

    val titleTextFieldController = rememberTextField2Controller(initialFocus = false)
    val descriptionTextFieldController = rememberTextField2Controller(initialFocus = false)

    LaunchedEffect(key1 = bucketBox, key2 = isBottomSheetVisible) {
        titleTextFieldController.onValueChange(value = bucketBox?.title ?: "")
        descriptionTextFieldController.onValueChange(value = bucketBox?.description ?: "")
    }

    fun updateBucket() {
        val titleValidationResult = titleTextFieldController.validate { it.isNotBlank() }
        val descriptionValidationResult = descriptionTextFieldController.validate { true }

        if (titleValidationResult.isValidated && descriptionValidationResult.isValidated && bucketBox!=null) {

            val updatedBucketBox = bucketBox.copy(
                title = titleValidationResult.text,
                description = descriptionValidationResult.text
            )
            onUpdateBucket(updatedBucketBox)

            titleTextFieldController.reset()
            descriptionTextFieldController.reset()
            bottomSheet2State.hideSheet(scope = scope)
        }
    }

    GenericBottomSheet2(
        bottomSheetState = bottomSheet2State,
    ) {
        GenericBottomSheetSkeleton2(
            title = stringResource(id = R.string.edit),
        ) {

            Spacer(modifier = Modifier.height(height = 8.dp))

            GenericTextField2(
                controller = titleTextFieldController,
                label = stringResource(id = R.string.list_title),
                placeholder = stringResource(id = R.string.name_your_bucket_list),
                errorMessage = stringResource(id = R.string.list_title_error),
                keyboardOptions = GenericTextField2Defaults.Options.getTextNextKeyboardOptionsDefault()
            )

            Spacer(modifier = Modifier.height(height = 8.dp))

            GenericTextField2(
                controller = descriptionTextFieldController,
                label = stringResource(R.string.description_optional),
                placeholder = stringResource(R.string.notebook_description_placeholder),
                minLines = 4,
                maxLines = Int.MAX_VALUE,
                singleLine = false
            )

            Spacer(modifier = Modifier.height(height = 8.dp))

            Button(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                onClick = ::updateBucket,
                content = { Text(text = stringResource(id = R.string.update)) }
            )
        }
    }
}


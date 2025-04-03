package com.syncodec.graphite.presentation.bucket2.composable.bottomSheet

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketBoxEncrypted
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemTodo
import com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.buildingBlock.FavouriteButton
import com.syncodec.graphite.presentation.bucket2.composable.bottomSheet.buildingBlock.LockButton
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.BucketItemStateView
import com.syncodec.graphite.presentation.common.v2.button.GraIconButton
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.v2.textField2.GenericTextField2
import com.syncodec.graphite.presentation.common.v2.textField2.GenericTextField2Defaults
import com.syncodec.graphite.presentation.common.v2.textField2.rememberTextField2Controller
import kotlinx.serialization.InternalSerializationApi
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class, ExperimentalUuidApi::class)
@Composable
fun AddTodoBottomSheet(
    bottomSheet2State: GenericBottomSheet2State<Nothing> = GenericBottomSheet2State.rememberGenericBottomSheet2State(),
    onAddTodo: (bucketItemBox: BucketItemBoxDecrypted) -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    val todoTitleTextFieldController = rememberTextField2Controller(initialFocus = true)
    val todoDescriptionTextFieldController = rememberTextField2Controller(initialFocus = false)
    var isFavourite by remember { mutableStateOf(value = false) }
    var isLocked by remember { mutableStateOf(value = false) }
    var bucketItemState: BucketItemBoxDecrypted.State by remember { mutableStateOf(value = BucketItemBoxDecrypted.State.Alpha) }

    fun saveBucketItem(addOther: Boolean) {
        val todoTitleValidationResult = todoTitleTextFieldController.validate { it.isNotBlank() }
        val todoDescriptionValidationResult = todoDescriptionTextFieldController.validate { true }

        if (todoTitleValidationResult.isValidated && todoDescriptionValidationResult.isValidated) {
            val bucketItemData = BucketItemTodo(title = todoTitleValidationResult.text, description = todoDescriptionValidationResult.text)
            val bucketItemBox = BucketItemBoxDecrypted.newInstance.copy(
                bucketItemData = bucketItemData,
                state = bucketItemState,
                isFavourite = isFavourite,
                isLocked = isLocked
            )
            onAddTodo(bucketItemBox)

            todoTitleTextFieldController.reset()
            todoDescriptionTextFieldController.reset()
            isFavourite = false
            isLocked = false
            bucketItemState = BucketItemBoxDecrypted.State.Alpha

            if (!addOther) bottomSheet2State.hideSheet(scope = scope)
        }
    }

    GenericBottomSheet2(
        bottomSheetState = bottomSheet2State,
    ) {
        GenericBottomSheetSkeleton2(
            title = stringResource(id = R.string.add_todo),
        ) {

            Spacer(modifier = Modifier.height(height = 12.dp))

            GenericTextField2(
                controller = todoTitleTextFieldController,
                label = stringResource(id = R.string.todo_title),
                placeholder = stringResource(id = R.string.add_to_list),
                errorMessage = stringResource(id = R.string.todo_item_title_error),
                keyboardOptions = GenericTextField2Defaults.Options.getTextNextKeyboardOptionsDefault()
            )

            Spacer(modifier = Modifier.height(height = 8.dp))

            GenericTextField2(
                controller = todoDescriptionTextFieldController,
                label = stringResource(id = R.string.todo_description),
                placeholder = stringResource(id = R.string.todo_enter_extra_detail),
                minLines = 4,
                maxLines = Int.MAX_VALUE,
                singleLine = false,
                keyboardOptions = GenericTextField2Defaults.Options.getTextNextKeyboardOptionsDefault()
            )

            Spacer(modifier = Modifier.height(height = 4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                FavouriteButton(isFavourite = isFavourite) { isFavourite = !isFavourite }
                Spacer(modifier = Modifier.width(width = 6.dp))
                LockButton(isLocked = isLocked) { isLocked = !isLocked }
            }

            BucketItemStateView(
                bucketType = BucketBoxEncrypted.BucketType.Todo,
                bucketItemState = bucketItemState.ordinal,
                onClickBucketItemState = { bucketItemState = BucketItemBoxDecrypted.State.entries.get(index = it) }
            )

            Spacer(modifier = Modifier.height(height = 20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.weight(weight = 1f),
                    onClick = { saveBucketItem(addOther = false) },
                    content = { Text(text = stringResource(id = R.string.add_todo)) }
                )
                Spacer(modifier = Modifier.width(width = 4.dp))
                GraIconButton.AddButton(padding = 0.dp, colors = GraIconButton.Defaults.primaryColors()) { saveBucketItem(addOther = true) }
            }
        }
    }
}



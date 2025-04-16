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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.v2.textField2.GenericTextField2
import kotlinx.serialization.InternalSerializationApi
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class, ExperimentalUuidApi::class)
@Composable
fun EditTodoBottomSheet(
    bottomSheet2State: GenericBottomSheet2State<BucketItemBoxDecrypted> = GenericBottomSheet2State.rememberGenericBottomSheet2StateT(),
    onUpdateBucketItemBox: (bucketItemBox: BucketItemBoxDecrypted) -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    val bucketItemBox by bottomSheet2State.dataFlow.collectAsState()
    val bucketItemTodo by remember(key1 = bucketItemBox) { derivedStateOf { bucketItemBox?.bucketItemData as? BucketItemTodo } }

    val todoTitleTextFieldController = GenericTextField2.rememberTextField2Controller(initialFocus = false)
    val todoDescriptionTextFieldController = GenericTextField2.rememberTextField2Controller(initialFocus = false)
    var isFavourite by remember(key1 = bucketItemBox) { mutableStateOf(value = bucketItemBox?.isFavourite ?: false) }
    var isLocked by remember(key1 = bucketItemBox) { mutableStateOf(value = bucketItemBox?.isLocked ?: false) }
    var bucketItemState: BucketItemBoxDecrypted.State by remember(key1 = bucketItemBox) { mutableStateOf(value = bucketItemBox?.state ?: BucketItemBoxDecrypted.State.Alpha) }

    LaunchedEffect(key1 = bucketItemBox) {
        todoTitleTextFieldController.onValueChange(value = bucketItemTodo?.title ?: "")
        todoDescriptionTextFieldController.onValueChange(value = bucketItemTodo?.description ?: "")
    }

    fun updateBucketItemObject() {
        val todoTitleValidationResult = todoTitleTextFieldController.validate { it.isNotBlank() }
        val todoDescriptionValidationResult = todoDescriptionTextFieldController.validate { true }

        val updatedBucketItemBox = bucketItemBox?.copy(
            bucketItemData = BucketItemTodo(title = todoTitleValidationResult.text, description = todoDescriptionValidationResult.text),
            state = bucketItemState,
            isFavourite = isFavourite,
            isLocked = isLocked
        ) ?: return

        onUpdateBucketItemBox(updatedBucketItemBox)
        bottomSheet2State.hideSheet(scope = scope)
    }

    GenericBottomSheet2(
        bottomSheetState = bottomSheet2State,
    ) {
        GenericBottomSheetSkeleton2(
            title = stringResource(id = R.string.edit_todo),
        ) {

            Spacer(modifier = Modifier.height(height = 12.dp))

            GenericTextField2.BottomSheetTextField(
                controller = todoTitleTextFieldController,
                label = stringResource(id = R.string.todo_title),
                placeholder = stringResource(id = R.string.add_to_list),
                errorMessage = stringResource(id = R.string.todo_item_title_error),
                keyboardOptions = GenericTextField2.Options.getTextNextKeyboardOptionsDefault()
            )

            Spacer(modifier = Modifier.height(height = 8.dp))

            GenericTextField2.BottomSheetTextField(
                controller = todoDescriptionTextFieldController,
                label = stringResource(id = R.string.todo_description),
                placeholder = stringResource(id = R.string.todo_enter_extra_detail),
                minLines = 4,
                maxLines = Int.MAX_VALUE,
                singleLine = false,
                keyboardOptions = GenericTextField2.Options.getTextNextKeyboardOptionsDefault()
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

            Spacer(modifier = Modifier.height(height = 2.dp))

            BucketItemStateView(
                bucketType = BucketBoxEncrypted.BucketType.Todo,
                bucketItemState = bucketItemState.ordinal,
                onClickBucketItemState = { bucketItemState = BucketItemBoxDecrypted.State.entries.get(index = it) }
            )

            Spacer(modifier = Modifier.height(height = 20.dp))

            Button(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                onClick = ::updateBucketItemObject,
                content = { Text(text = stringResource(id = R.string.update_todo)) }
            )
        }
    }
}



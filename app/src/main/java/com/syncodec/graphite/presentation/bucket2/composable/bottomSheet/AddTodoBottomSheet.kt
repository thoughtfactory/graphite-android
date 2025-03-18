package com.syncodec.graphite.presentation.bucket2.composable.bottomSheet

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketBox
import com.syncodec.graphite.di.modelObjectBox.BucketItemBox
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemData
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemTodo
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedBucketItemData
import com.syncodec.graphite.presentation.bucket2.composable.buildingBlock.BucketItemStateView
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheet2State
import com.syncodec.graphite.presentation.common.v2.bottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.v2.textField2.GenericTextField2
import com.syncodec.graphite.presentation.common.v2.textField2.GenericTextField2Defaults
import com.syncodec.graphite.presentation.common.v2.textField2.rememberTextField2Controller
import com.syncodec.graphite.utils.alice2.Alice2
import dev.chrisbanes.haze.HazeState
import org.koin.compose.koinInject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTodoBottomSheet(
    bottomSheet2State: GenericBottomSheet2State<Nothing> = GenericBottomSheet2State.rememberGenericBottomSheet2State(),
    outerHazeState: HazeState = remember { HazeState() },
    onAddTodo: (bucketItemBox: BucketItemBox) -> Unit = {}
) {
    val alice2: Alice2 = koinInject()
    val scope = rememberCoroutineScope()

    val todoTitleTextFieldController = rememberTextField2Controller(initialFocus = false)
    var bucketItemState: BucketItemData.State by remember { mutableStateOf(value = BucketItemData.State.Alpha) }

    fun saveBucketItem() {
        val todoTitleValidationResult = todoTitleTextFieldController.validate { it.isNotBlank() }
        if (todoTitleValidationResult.isValidated) {
            val bucketItemData = BucketItemTodo(title = todoTitleValidationResult.text, state = bucketItemState)
            val bucketItemBox = BucketItemBox(bucketItemData = EncryptedBucketItemData.fromBucketItemData(bucketItemData, alice2))
            onAddTodo(bucketItemBox)

            todoTitleTextFieldController.reset()
            bottomSheet2State.hideSheet(scope = scope)
        }
    }

    GenericBottomSheet2(
        bottomSheetState = bottomSheet2State,
        outerHazeState = outerHazeState
    ) {
        GenericBottomSheetSkeleton2(
            title = stringResource(id = R.string.add_todo),
        ) {

            Spacer(modifier = Modifier.height(height = 12.dp))

            GenericTextField2(
                controller = todoTitleTextFieldController,
                label = stringResource(id = R.string.todo_title),
                placeholder = stringResource(id = R.string.add_to_the_list),
                errorMessage = stringResource(id = R.string.todo_item_title_error),
                keyboardOptions = GenericTextField2Defaults.Options.getTextNextKeyboardOptionsDefault()
            )

            Spacer(modifier = Modifier.height(height = 8.dp))

            BucketItemStateView(
                bucketType = BucketBox.BucketType.Todo,
                bucketItemState = bucketItemState.ordinal,
                onClickBucketItemState = { bucketItemState = BucketItemData.State.entries.get(index = it) }
            )

            Spacer(modifier = Modifier.height(height = 20.dp))

            Button(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
                onClick = ::saveBucketItem,
                content = { Text(text = stringResource(id = R.string.add_todo)) }
            )
        }
    }
}



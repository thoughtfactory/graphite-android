package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionBucketItemObject
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionCloseBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnPutTodo
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.common.button.stateButton.StateButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateData
import com.syncodec.graphite.presentation.common.text.LargeTextField


@Preview
@Composable
fun AddTodoBottomSheet() {
	val onAddTodo = LocalCompositionOnPutTodo.current
	var todoText by remember { mutableStateOf("") }

	var isTextFocused by remember { mutableStateOf(false) }

	val bucketItem = LocalCompositionBucketItemObject.current

	val closeSheet = LocalCompositionCloseBottomSheet.current

	val stateList = listOf(
		StateData(
			title = "Todo",
			icon = R.drawable.ic_todo,
			stateTint = MaterialTheme.colorScheme.primary
		),
		StateData(
			title = "Doing",
			icon = R.drawable.ic_clock,
			stateTint = MaterialTheme.colorScheme.primary
		),
		StateData(
			title = "Done",
			icon = R.drawable.ic_done,
			stateTint = MaterialTheme.colorScheme.primary
		),
	)

	var currentState by remember { mutableStateOf(0) }

	LaunchedEffect(key1 = bucketItem) {
		currentState = if (bucketItem == null) 0 else BucketItemState.values().find { it.name == bucketItem.state }?.ordinal ?: 0
		todoText = bucketItem?.title ?: ""
	}

	GenericBottomSheet(
		title = "Add Todo",
		icon = R.drawable.ic_todo,
	) {

		LargeTextField(
			modifier = Modifier,
			value = todoText,
			placeholder = "Todo",
			isFocused = isTextFocused,
			onFocusChanged = { isTextFocused = it },
			keyboardOptions = KeyboardOptions.Default.copy(
				capitalization = KeyboardCapitalization.None,
				autoCorrect = true,
				keyboardType = KeyboardType.Text,
				imeAction = ImeAction.Go
			),
			keyboardActions = KeyboardActions(
				onGo = {
					onAddTodo(bucketItem?.id, todoText, BucketItemState.values()[currentState])
					todoText = ""
					closeSheet()
				}
			),
			trailingIcon = R.drawable.ic_add,
			onClickTrailingIcon = {
				onAddTodo(bucketItem?.id, todoText, BucketItemState.values()[currentState])
				todoText = ""
				closeSheet()
			},
		) { todoText = it }

		Spacer(modifier = Modifier.height(8.dp))

		StateButton(
			stateList = stateList,
			currentState = currentState,
			containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.71f),
			modifier = Modifier
				.fillMaxWidth()
				.height(32.dp),
		) {
			currentState = it
			if (bucketItem != null) {
				onAddTodo(bucketItem.id, todoText, BucketItemState.values()[currentState])
				todoText = ""
			}
		}
	}
}

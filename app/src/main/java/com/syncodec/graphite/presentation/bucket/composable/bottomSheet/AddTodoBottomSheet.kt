package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionBucketItemObject
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionCloseBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnPutTodo
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.common.button.stateButton.StateButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateData
import com.syncodec.graphite.presentation.common.text.LargeTextField


@Composable
fun AddTodoBottomSheet() {
	val onAddTodo = LocalCompositionOnPutTodo.current
	var todoText by remember { mutableStateOf("") }

	var isTextFocused by remember { mutableStateOf(false) }
	val focusRequester = remember { FocusRequester() }

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

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface)
	) {

		BottomSheetStrip()

		BottomSheetHeader(
			title = "Add Todo",
			icon = R.drawable.ic_todo,
		)

		Spacer(modifier = Modifier.height(8.dp))

		LargeTextField(
			modifier = Modifier.padding(24.dp, 0.dp),
			text = todoText,
			placeholder = "Todo",
			keyboardOptions = KeyboardOptions.Default.copy(
				capitalization = KeyboardCapitalization.None,
				autoCorrect = true,
				keyboardType = KeyboardType.Text,
				imeAction = ImeAction.Go
			),
			isFocused = isTextFocused,
			focusRequester = focusRequester,
			onFocusChanged = { isTextFocused = it },
			onValueChanged = { todoText = it },
			keyboardActions = KeyboardActions(
				onGo = {
					onAddTodo(bucketItem?.id, todoText, BucketItemState.values()[currentState])
					todoText = ""
					closeSheet()
				}
			)
		)

		Spacer(modifier = Modifier.height(8.dp))

		StateButton(
			stateList = stateList,
			currentState = currentState,
			modifier = Modifier
				.fillMaxWidth()
				.height(36.dp)
				.padding(24.dp, 0.dp),
			containerColor = MaterialTheme.colorScheme.background,
		) {
			currentState = it
			if (bucketItem != null) {
				onAddTodo(bucketItem.id, todoText, BucketItemState.values()[currentState])
				todoText = ""
			}
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}

package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetTextField
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetTextFieldDefaults
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import com.syncodec.graphite.presentation.common.button.stateButton.StateButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateData
import org.koin.androidx.compose.koinViewModel


@Preview
@Composable
fun AddTodoBottomSheet(
	closeSheet : () -> Unit = {}
) {
	val viewModel : BucketBottomSheetViewModel = koinViewModel()

	var todoText by remember { mutableStateOf("") }

	val stateList = listOf(
		StateData(
			title = "Todo",
			icon = R.drawable.ic_todo,
			stateTint = MaterialTheme.colorScheme.primary
		),
		StateData(
			title = "Doing",
			icon = R.drawable.ic_advance,
			stateTint = MaterialTheme.colorScheme.primary
		),
		StateData(
			title = "Done",
			icon = R.drawable.ic_done,
			stateTint = MaterialTheme.colorScheme.primary
		),
	)

	var currentState by remember { mutableStateOf(0) }

	fun onAddTodo() {
		viewModel.putTodo(realmUUID = null, todo = todoText, state = BucketItemState.values().getOrElse(currentState) { BucketItemState.ALPHA })
		todoText = ""
		closeSheet()
	}

	GenericBottomSheet(
		title = "Add Todo",
		icon = R.drawable.ic_todo,
	) {
		BottomSheetTextField(
			value = todoText,
			placeholder = "Todo",
			actionButtons = {
				MenuButton(
					icon = R.drawable.ic_add,
					colors = MenuButtonDefaults.menuButtonColorsOnSurface(),
					onClick = ::onAddTodo,
				)
			},
			keyboardOptions = KeyboardOptions.Default.copy(
				capitalization = KeyboardCapitalization.None,
				keyboardType = KeyboardType.Text,
				imeAction = ImeAction.Go
			),
			keyboardActions = KeyboardActions(
				onGo = { onAddTodo() },
				onDone = { onAddTodo() }
			),
			colors = BottomSheetTextFieldDefaults.textFieldColors(),
			onValueChange = { todoText = it },
		)

		Spacer(modifier = Modifier.height(4.dp))

		StateButton(
			stateList = stateList,
			currentState = currentState,
			modifier = Modifier
				.fillMaxWidth()
				.height(36.dp),
		) {
			currentState = it
			onAddTodo()
		}
	}
}

package com.syncodec.graphite.bucketComponent.modalBottomSheet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.custom.LargeTextField
import com.syncodec.graphite.custom.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.custom.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.custom.button.LargeButton
import com.syncodec.graphite.custom.button.StateButton
import com.syncodec.graphite.custom.button.StateData

@OptIn(ExperimentalFoundationApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun AddTodoSheet(
	key: String?,
	title: String,
	state: Int,
	hashCode: Int,
	onAction: (String?, String, Int) -> Unit
) {
	var todoText by remember { mutableStateOf(title) }
	var isTodoTextFocused by remember { mutableStateOf(false) }

	val stateList: List<StateData> = listOf(
		StateData(title = "To Do", icon = R.drawable.ic_todo, MaterialTheme.colorScheme.primary),
		StateData(title = "Doing", icon = R.drawable.ic_clock, Color(0xFFF5761A)),
		StateData(title = "Done", icon = R.drawable.ic_done, Color(0xFF519259)),
	)
	var currentState by remember { mutableStateOf(state) }

	LaunchedEffect(key1 = hashCode) {
		todoText = title
		currentState = state
	}

	Surface(
		shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
		color = MaterialTheme.colorScheme.surface,
		modifier = Modifier.heightIn(120.dp),
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier
		) {

			BottomSheetStrip()

			BottomSheetHeader(
				title = if (key==null) "Add a task" else "Update task",
				icon = R.drawable.ic_todo
			)

			Spacer(modifier = Modifier.height(8.dp))

			LargeTextField(
				modifier = Modifier.padding(24.dp, 0.dp),
				text = todoText,
				placeholder = "Add a task",
				keyboardOptions = KeyboardOptions.Default.copy(
					capitalization = KeyboardCapitalization.None,
					autoCorrect = true,
					keyboardType = KeyboardType.Text,
					imeAction = ImeAction.Search
				),
				keyboardActions = KeyboardActions(
					onSearch = {
					}
				),
				isFocused = isTodoTextFocused,
				onFocusChanged = { isTodoTextFocused = it },
				onValueChanged = { todoText = it }
			)

			Spacer(modifier = Modifier.height(16.dp))

			StateButton(
				stateList = stateList,
				currentState = currentState,
				modifier = Modifier
					.fillMaxWidth()
					.height(32.dp)
					.padding(24.dp, 0.dp),
			) { currentState = it }

			Spacer(modifier = Modifier.height(16.dp))

			LargeButton(
				text = "Update todo",
				enabled = todoText.isNotBlank(),
				modifier = Modifier
					.fillMaxWidth()
					.padding(24.dp, 0.dp)
			) {
				onAction(key, todoText, currentState)
				todoText = ""
				currentState = 0
			}

			Spacer(modifier = Modifier.height(16.dp))
		}
	}
}

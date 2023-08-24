package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.tab.GenericTabRow
import com.syncodec.graphite.presentation.common.tab.TabItem


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AddTodoBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = false,
	onDismissRequest: () -> Unit = { },
	onAddTodo: (String, Int) -> Unit = { _, _ -> },
) {
	var todoText by remember { mutableStateOf("") }
	var currentState by remember { mutableIntStateOf(0) }

	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		GenericBottomSheetSkeleton2(
			title = stringResource(id = R.string.add_todo),
		) {
			OutlinedTextField(
				value = todoText,
				shape = MaterialTheme.shapes.medium,
				onValueChange = { todoText = it },
				label = { Text(text = stringResource(id = R.string.title)) },
				placeholder = { Text(text = stringResource(id = R.string.todo)) },
				maxLines = 1,
				singleLine = true,
				keyboardOptions = KeyboardOptions(
					capitalization = KeyboardCapitalization.None,
					autoCorrect = true,
					keyboardType = KeyboardType.Text,
					imeAction = ImeAction.Done,
				),
				keyboardActions = KeyboardActions {
					onAddTodo(todoText, currentState)
					todoText = ""
				},
				modifier = Modifier.fillMaxWidth()
			)

			Spacer(modifier = Modifier.height(8.dp))

			GenericTabRow(
				tabItemList = listOf(
					TabItem(text = stringResource(id = R.string.todo), icon = R.drawable.ic_fa_bucket_todo, onClick = { currentState = 0 }),
					TabItem(text = stringResource(id = R.string.doing), icon = R.drawable.ic_fa_clock, onClick = { currentState = 1 }),
					TabItem(text = stringResource(id = R.string.done), icon = R.drawable.ic_fa_circle_check, onClick = { currentState = 2 }),
				),
				selectedTabIndex = currentState,
				modifier = Modifier.fillMaxWidth()
			)

			Spacer(modifier = Modifier.height(6.dp))

			Button(
				modifier = Modifier.fillMaxWidth(),
				shape = MaterialTheme.shapes.medium,
				enabled = todoText.isNotEmpty(),
				onClick = {
					onAddTodo(todoText, currentState)
					todoText = ""
				}
			) {
				Text(text = stringResource(id = R.string.add_todo))
			}
		}
	}
}

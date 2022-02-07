package com.syncodec.momento.notebookComponent.modalBottomSheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.custom.BottomSheetHeader
import com.syncodec.momento.custom.BottomSheetStrip
import com.syncodec.momento.custom.modifier.dashedBorder
import com.syncodec.momento.mainComponent.MainViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Notebook
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NewNoteBottomSheet() {

	val viewModel: MainViewModel = viewModel()

	val scope = rememberCoroutineScope()
	val focusManager = LocalFocusManager.current

	var notebookTitleText by rememberSaveable { mutableStateOf("") }
	var notebookDescriptionText by rememberSaveable { mutableStateOf("") }
	var isNotebookTitleTextFocused by remember { mutableStateOf(false) }
	var isNotebookDescriptionTextFocused by remember { mutableStateOf(false) }

	val createButtonColors = ButtonDefaults.buttonColors(
		contentColor = MaterialTheme.colorScheme.primaryContainer,
		disabledContentColor = Color.LightGray,
		containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
		disabledContainerColor = Color.LightGray.copy(alpha = 0.13f)
	)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(420.dp)
			.background(MaterialTheme.colorScheme.background),
	) {

		BottomSheetStrip()

		Spacer(modifier = Modifier.height(12.dp))

		BottomSheetHeader(
			title = "Writing a new book?",
			imageVector = TablerIcons.Notebook,
			subTitle = "Keep your notes organized in notebooks"
		)

		Spacer(modifier = Modifier.height(8.dp))

		BasicTextField(
			modifier = Modifier
				.fillMaxWidth()
				.height(48.dp)
				.padding(24.dp, 0.dp)
				.background(
					if (notebookTitleText.isEmpty() && !isNotebookTitleTextFocused) {
						Color.LightGray.copy(alpha = 0.13f)
					} else {
						MaterialTheme.colorScheme.background
					}
				)
				.onFocusChanged { focusState ->
					isNotebookTitleTextFocused = focusState.isFocused
				},
			value = notebookTitleText,
			onValueChange = { notebookTitleText = it },
			singleLine = true,
			cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
			textStyle = MaterialTheme.typography.bodyMedium,
			decorationBox = { innerTextField ->
				Card(
					backgroundColor = Color.Transparent,
					shape = RoundedCornerShape(2.dp),
					border = BorderStroke(1.dp, if (isNotebookTitleTextFocused) MaterialTheme.colorScheme.primary else Color.LightGray),
					elevation = 0.dp
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier.padding(16.dp, 0.dp)
					) {
						Box {
							if (notebookTitleText.isEmpty()) {
								Text(
									"Give your book a title",
									style = MaterialTheme.typography.bodyMedium,
									color = Color.LightGray
								)
							}
							innerTextField()
						}
					}
				}
			}
		)

		Spacer(modifier = Modifier.height(12.dp))

		BasicTextField(
			modifier = Modifier
				.fillMaxWidth()
				.height(48.dp)
				.padding(24.dp, 0.dp)
				.background(
					if (notebookDescriptionText.isEmpty() && !isNotebookDescriptionTextFocused) {
						Color.LightGray.copy(alpha = 0.13f)
					} else {
						MaterialTheme.colorScheme.background
					}
				)
				.onFocusChanged { focusState ->
					isNotebookDescriptionTextFocused = focusState.isFocused
				},
			value = notebookDescriptionText,
			onValueChange = { notebookDescriptionText = it },
			singleLine = true,
			cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
			textStyle = MaterialTheme.typography.bodyMedium,
			decorationBox = { innerTextField ->
				Card(
					backgroundColor = Color.Transparent,
					shape = RoundedCornerShape(2.dp),
					border = BorderStroke(1.dp, if (isNotebookDescriptionTextFocused) MaterialTheme.colorScheme.primary else Color.LightGray),
					elevation = 0.dp
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier.padding(16.dp, 0.dp)
					) {
						Box {
							if (notebookDescriptionText.isEmpty()) {
								Text(
									"And a little description",
									style = MaterialTheme.typography.bodyMedium,
									color = Color.LightGray
								)
							}
							innerTextField()
						}
					}
				}
			}
		)

		Spacer(modifier = Modifier.height(16.dp))

		Card(
			elevation = 0.dp,
			backgroundColor = createButtonColors.containerColor(enabled = notebookTitleText.isNotBlank()).value,
			enabled = notebookTitleText.isNotBlank(),
			modifier = Modifier
				.fillMaxWidth()
				.height(48.dp)
				.padding(24.dp, 0.dp)
				.focusable(),
			onClick = {
//				viewModel.insertNotebook(
//					title = notebookTitleText,
//					description = notebookDescriptionText
//				)
				focusManager.clearFocus()
				scope.launch {
					viewModel.mainActivityState.bottomSheetState.hide()
				}
			}
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.Center,
				modifier = Modifier
					.fillMaxWidth()
					.fillMaxHeight()
			) {
				Text(
					text = "Create",
					style = MaterialTheme.typography.titleMedium,
					color = createButtonColors.contentColor(enabled = notebookTitleText.isNotBlank()).value,
					textAlign = TextAlign.Center,
					lineHeight = 0.sp,
					maxLines = 1,
				)
			}
		}

		Spacer(modifier = Modifier.height(16.dp))

		Card(
			elevation = 0.dp,
			backgroundColor = MaterialTheme.colorScheme.background,
			onClick = {},
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
				.dashedBorder(
					width = 2.dp,
					color = MaterialTheme.colorScheme.primary,
					shape = RoundedCornerShape(4.dp),
					on = 8.dp,
					off = 8.dp
				)
		) {
			Text(
				text = "Expenses (notebook)\n" +
						"        ╠════ 2020 (chapter)\n" +
						"        ║       ├──── January (topic)\n" +
						"        ║       ├──── February\n" +
						"        ║       ├──── March\n" +
						"        ╠════ 2021\n" +
						"        ╠════ 2022",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp)
					.alpha(0.47f)
			)
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}

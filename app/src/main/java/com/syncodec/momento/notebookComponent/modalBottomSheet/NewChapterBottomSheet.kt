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
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.notebookComponent.NotebookViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Notebook
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NewChapterBottomSheet() {

	val viewModel: NotebookViewModel = viewModel()

	val scope = rememberCoroutineScope()
	val focusManager = LocalFocusManager.current

	var chapterTitleText by rememberSaveable { mutableStateOf("") }
	var chapterDescriptionText by rememberSaveable { mutableStateOf("") }
	var isChapterTitleTextFocused by remember { mutableStateOf(false) }
	var isChapterDescriptionTextFocused by remember { mutableStateOf(false) }

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
			title = "New chapter?",
			imageVector = TablerIcons.Notebook,
		)

		Spacer(modifier = Modifier.height(8.dp))

		BasicTextField(
			modifier = Modifier
				.fillMaxWidth()
				.height(48.dp)
				.padding(24.dp, 0.dp)
				.background(
					if (chapterTitleText.isEmpty() && !isChapterTitleTextFocused) {
						Color.LightGray.copy(alpha = 0.13f)
					} else {
						MaterialTheme.colorScheme.background
					}
				)
				.onFocusChanged { focusState ->
					isChapterTitleTextFocused = focusState.isFocused
				},
			value = chapterTitleText,
			onValueChange = { chapterTitleText = it },
			singleLine = true,
			cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
			textStyle = MaterialTheme.typography.bodyMedium,
			decorationBox = { innerTextField ->
				Card(
					backgroundColor = Color.Transparent,
					shape = RoundedCornerShape(2.dp),
					border = BorderStroke(1.dp, if (isChapterTitleTextFocused) MaterialTheme.colorScheme.primary else Color.LightGray),
					elevation = 0.dp
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier.padding(16.dp, 0.dp)
					) {
						Box {
							if (chapterTitleText.isEmpty()) {
								Text(
									"Don;t keep chapter name empty",
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
					if (chapterDescriptionText.isEmpty() && !isChapterDescriptionTextFocused) {
						Color.LightGray.copy(alpha = 0.13f)
					} else {
						MaterialTheme.colorScheme.background
					}
				)
				.onFocusChanged { focusState ->
					isChapterDescriptionTextFocused = focusState.isFocused
				},
			value = chapterDescriptionText,
			onValueChange = { chapterDescriptionText = it },
			singleLine = true,
			cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
			textStyle = MaterialTheme.typography.bodyMedium,
			decorationBox = { innerTextField ->
				Card(
					backgroundColor = Color.Transparent,
					shape = RoundedCornerShape(2.dp),
					border = BorderStroke(1.dp, if (isChapterDescriptionTextFocused) MaterialTheme.colorScheme.primary else Color.LightGray),
					elevation = 0.dp
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier.padding(16.dp, 0.dp)
					) {
						Box {
							if (chapterDescriptionText.isEmpty()) {
								Text(
									"What is it about?",
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
			backgroundColor = createButtonColors.containerColor(enabled = chapterTitleText.isNotBlank()).value,
			enabled = chapterTitleText.isNotBlank(),
			modifier = Modifier
				.fillMaxWidth()
				.height(48.dp)
				.padding(24.dp, 0.dp)
				.focusable(),
			onClick = {
				viewModel.createNewChapter(
					title = chapterTitleText,
					description = chapterDescriptionText
				)
				focusManager.clearFocus()
				scope.launch {
					viewModel.notebookActivityState.bottomSheetState.hide()
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
					color = createButtonColors.contentColor(enabled = chapterTitleText.isNotBlank()).value,
					textAlign = TextAlign.Center,
					lineHeight = 0.sp,
					maxLines = 1,
				)
			}
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}

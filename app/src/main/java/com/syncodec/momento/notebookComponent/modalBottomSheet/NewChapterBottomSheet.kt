package com.syncodec.momento.notebookComponent.modalBottomSheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.custom.BottomSheetHeader
import com.syncodec.momento.custom.BottomSheetStrip
import com.syncodec.momento.notebookComponent.ViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Notebook
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NewChapterBottomSheet() {

	val viewModel: ViewModel = viewModel()

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
			.heightIn(240.dp)
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
			value = chapterTitleText,
			onValueChange = { chapterTitleText = it },
			singleLine = true,
			cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
			textStyle = MaterialTheme.typography.bodyMedium.copy(
				color = MaterialTheme.colorScheme.primary,
				fontWeight = FontWeight.Bold
			),
			modifier = Modifier
				.fillMaxWidth()
				.height(48.dp)
				.padding(24.dp, 0.dp)
				.clip(RoundedCornerShape(12.dp))
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
			decorationBox = { innerTextField ->
				Card(
					modifier = Modifier
						.fillMaxWidth(),
					backgroundColor = Color.Transparent,
					elevation = 0.dp,
					shape = RoundedCornerShape(12.dp),
					border = BorderStroke(2.dp, if (isChapterTitleTextFocused) MaterialTheme.colorScheme.primary else Color.LightGray)
				) {
					Box(
						contentAlignment = Alignment.CenterStart,
						modifier = Modifier
							.fillMaxWidth()
							.padding(12.dp, 0.dp)
					) {
						if (chapterTitleText.isEmpty()) {
							Text(
								"Don't keep chapter name empty",
								style = MaterialTheme.typography.bodyMedium,
								color = Color.LightGray,
								fontWeight = FontWeight.Bold,
								maxLines = 1
							)
						}
						innerTextField()
					}
				}
			}
		)

		Spacer(modifier = Modifier.height(12.dp))

		BasicTextField(
			value = chapterDescriptionText,
			onValueChange = { chapterDescriptionText = it },
			singleLine = true,
			cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
			textStyle = MaterialTheme.typography.bodyMedium.copy(
				color = MaterialTheme.colorScheme.primary,
				fontWeight = FontWeight.Bold
			),
			modifier = Modifier
				.fillMaxWidth()
				.height(48.dp)
				.padding(24.dp, 0.dp)
				.clip(RoundedCornerShape(12.dp))
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
			decorationBox = { innerTextField ->
				Card(
					modifier = Modifier
						.fillMaxWidth(),
					backgroundColor = Color.Transparent,
					elevation = 0.dp,
					shape = RoundedCornerShape(12.dp),
					border = BorderStroke(2.dp, if (isChapterDescriptionTextFocused) MaterialTheme.colorScheme.primary else Color.LightGray)
				) {
					Box(
						contentAlignment = Alignment.CenterStart,
						modifier = Modifier
							.fillMaxWidth()
							.padding(12.dp, 0.dp)
					) {
						if (chapterDescriptionText.isEmpty()) {
							Text(
								"What is it about?",
								style = MaterialTheme.typography.bodyMedium,
								color = Color.LightGray,
								fontWeight = FontWeight.Bold
							)
						}
						innerTextField()
					}
				}
			}
		)

		Spacer(modifier = Modifier.height(16.dp))

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(48.dp)
				.padding(24.dp, 0.dp)
				.focusable()
				.clip(RoundedCornerShape(12.dp))
				.background(createButtonColors.containerColor(enabled = chapterTitleText.isNotBlank()).value)
				.clickable(chapterTitleText.isNotBlank()) {
					viewModel.createNewChapter(
						title = chapterTitleText,
						description = chapterDescriptionText
					)
					focusManager.clearFocus()
					scope.launch {
						viewModel.activityState.bottomSheetState.hide()
					}
					chapterTitleText = ""
					chapterDescriptionText = ""
				},
			contentAlignment = Alignment.Center
		) {
			Text(
				text = "Add new chapter",
				style = MaterialTheme.typography.titleMedium,
				color = createButtonColors.contentColor(enabled = chapterTitleText.isNotBlank()).value,
				textAlign = TextAlign.Center,
				lineHeight = 0.sp,
				maxLines = 1,
			)
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}

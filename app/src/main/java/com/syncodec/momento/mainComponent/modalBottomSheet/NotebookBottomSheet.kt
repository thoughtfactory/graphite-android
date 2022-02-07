package com.syncodec.momento.mainComponent.modalBottomSheet

import androidx.compose.foundation.*
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.custom.BottomSheetHeader
import com.syncodec.momento.custom.BottomSheetStrip
import com.syncodec.momento.custom.button.LargeButton
import com.syncodec.momento.mainComponent.MainViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Notebook
import kotlinx.coroutines.launch
import kotlin.random.Random


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NotebookBottomSheet() {

	val viewModel: MainViewModel = viewModel()

	val scope = rememberCoroutineScope()
	val focusManager = LocalFocusManager.current

	var notebookTitleText by rememberSaveable { mutableStateOf("") }
	var isNotebookTitleTextFocused by remember { mutableStateOf(false) }

	var notebookDescriptionText by rememberSaveable { mutableStateOf("") }
	var isNotebookDescriptionTextFocused by remember { mutableStateOf(false) }

	var notebookColor: Long? = null

	val createButtonColors = ButtonDefaults.buttonColors(
		contentColor = MaterialTheme.colorScheme.primaryContainer,
		disabledContentColor = Color.DarkGray,
		containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
		disabledContainerColor = Color.LightGray
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

		LargeTextField(
			text = notebookTitleText,
			placeholder = "Give your book a title",
			isFocused = isNotebookTitleTextFocused,
			onFocusChanged = { isNotebookTitleTextFocused = it }
		) { notebookTitleText = it }

		Spacer(modifier = Modifier.height(12.dp))

		LargeTextField(
			text = notebookDescriptionText,
			placeholder = "And a little description",
			isFocused = isNotebookDescriptionTextFocused,
			onFocusChanged = { isNotebookDescriptionTextFocused = it }
		) { notebookDescriptionText = it }

		Spacer(modifier = Modifier.height(8.dp))

		ColorList {
			notebookColor = it
		}

		Spacer(modifier = Modifier.height(16.dp))

		LargeButton(
			text = "Create",
			backgroundColor = createButtonColors.containerColor(enabled = notebookTitleText.isNotBlank()).value,
			textColor = createButtonColors.contentColor(enabled = notebookTitleText.isNotBlank()).value,
			modifier = Modifier
				.fillMaxWidth()
				.height(48.dp)
				.padding(24.dp, 0.dp)
				.focusable()
		) {
			viewModel.insertNotebook(
				title = notebookTitleText,
				description = notebookDescriptionText,
				color = notebookColor
			)
			focusManager.clearFocus()
			scope.launch {
				viewModel.mainActivityState.bottomSheetState.hide()
			}

			notebookTitleText = ""
			notebookDescriptionText = ""
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}

@Composable
private fun LargeTextField(
	text: String,
	placeholder: String,
	isFocused: Boolean,
	onFocusChanged: (Boolean) -> Unit,
	onValueChanged: (String) -> Unit
) {
	BasicTextField(
		modifier = Modifier
			.fillMaxWidth()
			.height(48.dp)
			.padding(24.dp, 0.dp)
			.background(
				if (text.isEmpty() && !isFocused) {
					Color.LightGray.copy(alpha = 0.13f)
				} else {
					MaterialTheme.colorScheme.background
				}
			)
			.onFocusChanged { onFocusChanged(it.isFocused) },
		value = text,
		onValueChange = { onValueChanged(it) },
		singleLine = true,
		cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
		textStyle = MaterialTheme.typography.bodyMedium,
		decorationBox = { innerTextField ->
			Card(
				backgroundColor = Color.Transparent,
				shape = RoundedCornerShape(2.dp),
				border = BorderStroke(1.dp, if (isFocused) MaterialTheme.colorScheme.primary else Color.LightGray),
				elevation = 0.dp
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.padding(16.dp, 0.dp)
				) {
					Box {
						if (text.isEmpty()) {
							Text(
								placeholder,
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
}

@Composable
private fun ColorList(
	onClick: (Long) -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(64.dp)
			.horizontalScroll(state = rememberScrollState()),
		verticalAlignment = Alignment.CenterVertically
	) {
		Spacer(modifier = Modifier.width(12.dp))
		for (i in 0 until 13) {
			Box(
				modifier = Modifier
					.requiredSize(48.dp)
					.padding(2.dp)
					.clip(RoundedCornerShape(8.dp))
					.background(Color(Random.nextInt()))
					.clickable {
						onClick(Random.nextLong())
					}
			)
		}
		Spacer(modifier = Modifier.width(12.dp))
	}
}

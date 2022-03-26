package com.syncodec.momento.notebookComponent.modalBottomSheet

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.custom.BottomSheetHeader
import com.syncodec.momento.custom.BottomSheetStrip
import com.syncodec.momento.custom.LargeTextField
import com.syncodec.momento.custom.button.LargeButton
import com.syncodec.momento.notebookComponent.NotebookViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Notebook
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NewChapterBottomSheet() {

	val notebookViewModel: NotebookViewModel = viewModel()

	val scope = rememberCoroutineScope()
	val focusManager = LocalFocusManager.current

	var chapterTitleText by rememberSaveable { mutableStateOf("") }
	var chapterDescriptionText by rememberSaveable { mutableStateOf("") }
	var isChapterTitleTextFocused by remember { mutableStateOf(false) }
	var isChapterDescriptionTextFocused by remember { mutableStateOf(false) }

	val containerColor by animateColorAsState(
		targetValue = if (chapterTitleText.isNotEmpty()) MaterialTheme.colorScheme.onPrimaryContainer else Color.LightGray,
		animationSpec = tween(durationMillis = 600)
	)
	val contentColor by animateColorAsState(
		targetValue = if (chapterTitleText.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer else Color.DarkGray,
		animationSpec = tween(durationMillis = 600)
	)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(240.dp)
			.background(MaterialTheme.colorScheme.background),
	) {

		BottomSheetStrip()

		BottomSheetHeader(
			title = "New chapter?",
			imageVector = TablerIcons.Notebook,
		)

		Spacer(modifier = Modifier.height(8.dp))

		LargeTextField(
			text = chapterTitleText,
			placeholder = "Don't keep chapter name empty",
			isFocused = isChapterTitleTextFocused,
			onFocusChanged = { isChapterTitleTextFocused = it },
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		) {
			chapterTitleText = it
		}

		Spacer(modifier = Modifier.height(8.dp))

		LargeTextField(
			text = chapterDescriptionText,
			placeholder = "What is it about?",
			isFocused = isChapterDescriptionTextFocused,
			onFocusChanged = { isChapterDescriptionTextFocused = it },
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		) {
			chapterDescriptionText = it
		}

		Spacer(modifier = Modifier.height(16.dp))

		LargeButton(
			text = "Add new chapter",
			containerColor = containerColor,
			contentColor = contentColor,
			isClickable = chapterTitleText.isNotBlank(),
			modifier = Modifier
				.padding(24.dp, 0.dp)
		) {
			notebookViewModel.putChapter(
				title = chapterTitleText,
				description = chapterDescriptionText
			)
			focusManager.clearFocus()
			scope.launch {
				notebookViewModel.activityState.bottomSheetState.hide()
			}
			chapterTitleText = ""
			chapterDescriptionText = ""
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}

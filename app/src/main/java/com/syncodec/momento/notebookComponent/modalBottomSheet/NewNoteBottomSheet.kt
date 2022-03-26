package com.syncodec.momento.notebookComponent.modalBottomSheet

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.custom.BottomSheetHeader
import com.syncodec.momento.custom.BottomSheetStrip
import com.syncodec.momento.custom.LargeTextField
import com.syncodec.momento.custom.button.LargeButton
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.noteComponent.NoteActivity
import com.syncodec.momento.notebookComponent.NotebookViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Note
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NewNoteBottomSheet() {
	val context = LocalContext.current
	val notebookViewModel: NotebookViewModel = viewModel()

	val scope = rememberCoroutineScope()
	val focusManager = LocalFocusManager.current

	var noteTitleText by rememberSaveable { mutableStateOf("") }
	var isNoteTitleFocused by remember { mutableStateOf(false) }

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
			title = "New note?",
			imageVector = TablerIcons.Note,
		)

		Spacer(modifier = Modifier.height(8.dp))

		LargeTextField(
			text = noteTitleText,
			placeholder = "What is this note about",
			isFocused = isNoteTitleFocused,
			onFocusChanged = { isNoteTitleFocused = it },
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
		) {
			noteTitleText = it
		}

		Spacer(modifier = Modifier.height(16.dp))

		LargeButton(
			text = "Add new note",
			containerColor = Color.LightGray,
			contentColor = Color.DarkGray,
			isClickable = noteTitleText.isNotBlank(),
			modifier = Modifier
				.padding(24.dp, 0.dp)
		) {
			focusManager.clearFocus()
			scope.launch {
				notebookViewModel.activityState.bottomSheetState.hide()
			}

			Intent(context, NoteActivity::class.java).apply {
				putExtra(Konstant.Companion.Konstant.NOTEBOOK_KEY.name, notebookViewModel.notebookKey)
				putStringArrayListExtra(Konstant.Companion.Konstant.CHAPTER_KEY.name, ArrayList(notebookViewModel.chapterPath))
				putExtra(Konstant.Companion.Konstant.TITLE.name, noteTitleText)

				context.startActivity(this)
			}
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}

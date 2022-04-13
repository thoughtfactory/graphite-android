package com.syncodec.momento.notebookComponent.modalBottomSheet

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.syncodec.momento.R
import com.syncodec.momento.custom.bottomSheet.BottomSheetHeader
import com.syncodec.momento.custom.bottomSheet.BottomSheetStrip
import com.syncodec.momento.custom.LargeTextField
import com.syncodec.momento.custom.button.LargeButton
import com.syncodec.momento.miscellaneous.ThemeUtils.Companion.tone
import com.syncodec.momento.notebookComponent.NotebookActivity

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NewNoteBottomSheet(
	onAction: (NotebookActivity.Action, Any?) -> Unit
) {
	val focusManager = LocalFocusManager.current

	var noteTitleText by rememberSaveable { mutableStateOf("") }
	var isNoteTitleFocused by remember { mutableStateOf(false) }

	Surface(
		shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
		color= MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 2),
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(180.dp),
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier.fillMaxWidth()
		) {

			BottomSheetStrip()

			Spacer(modifier = Modifier.height(12.dp))

			BottomSheetHeader(title = "New note?", icon = R.drawable.ic_note)

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
				enabled = noteTitleText.isNotBlank(),
				modifier = Modifier
					.padding(24.dp, 0.dp)
			) {
				focusManager.clearFocus()
				onAction(NotebookActivity.Action.ON_NEW_NOTE, noteTitleText)
				noteTitleText = ""
			}

			Spacer(modifier = Modifier.height(32.dp))
		}
	}
}

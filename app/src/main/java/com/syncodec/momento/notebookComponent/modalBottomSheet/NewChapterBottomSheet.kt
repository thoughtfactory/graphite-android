package com.syncodec.momento.notebookComponent.modalBottomSheet

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.syncodec.momento.R
import com.syncodec.momento.custom.LargeTextField
import com.syncodec.momento.custom.bottomSheet.BottomSheetHeader
import com.syncodec.momento.custom.bottomSheet.BottomSheetStrip
import com.syncodec.momento.custom.button.LargeButton
import com.syncodec.momento.miscellaneous.ThemeUtils.Companion.tone
import com.syncodec.momento.notebookComponent.NotebookActivity


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NewChapterBottomSheet(
	onAction: (NotebookActivity.Action, Any?) -> Unit
) {
	val focusManager = LocalFocusManager.current

	var chapterTitleText by rememberSaveable { mutableStateOf("") }
	var chapterDescriptionText by rememberSaveable { mutableStateOf("") }
	var isChapterTitleTextFocused by remember { mutableStateOf(false) }
	var isChapterDescriptionTextFocused by remember { mutableStateOf(false) }

	Surface(
		shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
		color = MaterialTheme.colorScheme.surface,
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(240.dp),
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier.fillMaxWidth()
		) {

			BottomSheetStrip()

			Spacer(modifier = Modifier.height(12.dp))

			BottomSheetHeader(title = "New chapter?", icon = R.drawable.ic_notebook)

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
			) { chapterDescriptionText = it }

			Spacer(modifier = Modifier.height(16.dp))

			LargeButton(
				text = "Add new chapter",
				enabled = chapterTitleText.isNotBlank(),
				modifier = Modifier
					.fillMaxWidth()
					.padding(24.dp, 0.dp)
			) {
				focusManager.clearFocus()
				onAction(
					NotebookActivity.Action.ON_NEW_CHAPTER,
					Pair(chapterTitleText, chapterDescriptionText)
				)
				chapterTitleText = ""
				chapterDescriptionText = ""
			}

			Spacer(modifier = Modifier.height(32.dp))
		}
	}
}

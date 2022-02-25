package com.syncodec.momento.notebookComponent.modalBottomSheet

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.Momento
import com.syncodec.momento.custom.BottomSheetHeader
import com.syncodec.momento.custom.BottomSheetStrip
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.noteComponent.NoteActivity
import com.syncodec.momento.notebookComponent.ViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Note
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NewNoteBottomSheet() {
	val context = LocalContext.current
	val viewModel: ViewModel = viewModel()

	val scope = rememberCoroutineScope()
	val focusManager = LocalFocusManager.current

	var noteTitleText by rememberSaveable { mutableStateOf("") }
	var isNoteTitleFocused by remember { mutableStateOf(false) }

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
			title = "New note?",
			imageVector = TablerIcons.Note,
		)

		Spacer(modifier = Modifier.height(8.dp))

		BasicTextField(
			value = noteTitleText,
			onValueChange = { noteTitleText = it },
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
					if (noteTitleText.isEmpty() && !isNoteTitleFocused) {
						Color.LightGray.copy(alpha = 0.13f)
					} else {
						MaterialTheme.colorScheme.background
					}
				)
				.onFocusChanged { focusState ->
					isNoteTitleFocused = focusState.isFocused
				},
			decorationBox = { innerTextField ->
				Card(
					modifier = Modifier
						.fillMaxWidth(),
					backgroundColor = Color.Transparent,
					elevation = 0.dp,
					shape = RoundedCornerShape(12.dp),
					border = BorderStroke(2.dp, if (isNoteTitleFocused) MaterialTheme.colorScheme.primary else Color.LightGray)
				) {
					Box(
						contentAlignment = Alignment.CenterStart,
						modifier = Modifier
							.fillMaxWidth()
							.padding(12.dp, 0.dp)
					) {
						if (noteTitleText.isEmpty()) {
							Text(
								"What is this note about",
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

		Spacer(modifier = Modifier.height(16.dp))

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(48.dp)
				.padding(24.dp, 0.dp)
				.focusable()
				.clip(RoundedCornerShape(12.dp))
				.background(createButtonColors.containerColor(enabled = noteTitleText.isNotBlank()).value)
				.clickable(noteTitleText.isNotBlank()) {
					focusManager.clearFocus()
					scope.launch {
						viewModel.activityState.bottomSheetState.hide()
					}

					Intent(context, NoteActivity::class.java).apply {
						putExtra(Konstant.Companion.Konstant.COMPONENT_TYPE.name, Momento.Companion.ComponentType.NOTE.ordinal)
						putExtra(Konstant.Companion.Konstant.NOTEBOOK_KEY.name, viewModel.notebookKey)
						putStringArrayListExtra(Konstant.Companion.Konstant.CHAPTER_KEY.name, ArrayList(viewModel.currentRoute))
						putExtra(Konstant.Companion.Konstant.TITLE.name, noteTitleText)

						context.startActivity(this)
					}
				},
			contentAlignment = Alignment.Center
		) {
			Text(
				text = "Add new note",
				style = MaterialTheme.typography.titleMedium,
				color = createButtonColors.contentColor(enabled = noteTitleText.isNotBlank()).value,
				textAlign = TextAlign.Center,
				lineHeight = 0.sp,
				maxLines = 1,
			)
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}

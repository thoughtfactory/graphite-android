package com.syncodec.graphite.presentation.note.composable.bar

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.custom.richText.RichTextEditor
import com.syncodec.graphite.presentation.note.NoteViewModel
import com.syncodec.graphite.presentation.note.composable.bar.bottomBar.EditorBottomBar
import com.syncodec.graphite.presentation.note.composable.bar.bottomBar.EditorToolbar
import com.syncodec.graphite.presentation.note.composable.bar.bottomBar.ViewerBottomBar
import com.syncodec.graphite.utils.LocalRichTextEditor


enum class ToolbarState {
	NONE,
	FORMAT,
	HEADING,
	LINK
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BottomBar(
	onClickAttachment: () -> Unit,
	onClickTag: () -> Unit
) {
	val context = LocalContext.current
	val viewModel: NoteViewModel = viewModel()

	val richTextEditor = LocalRichTextEditor.current

	val isViewer by viewModel.isViewer
	val isSaving by viewModel.isSaving
	val userTimestamp by viewModel.userTimestamp

	var textFormat by remember { mutableStateOf(RichTextEditor.TextFormat()) }

	richTextEditor.setOnFormatUpdate(object : RichTextEditor.OnFormatUpdateListener {
		override fun onFormatUpdate(newTextFormat: RichTextEditor.TextFormat) {
			textFormat = newTextFormat
		}
	})

	var toolbarState by remember { mutableStateOf(ToolbarState.NONE) }

	LaunchedEffect(key1 = isViewer) {
		if (isViewer) toolbarState = ToolbarState.NONE
	}

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface)
	) {
		EditorToolbar(
			richTextEditor = richTextEditor,
			textFormat = textFormat,
			toolbarState = toolbarState,
			onClickCloseToolbar = { toolbarState = ToolbarState.NONE },
			onClickFormatToolbar = { toolbarState = ToolbarState.FORMAT },
		) { toolbarState = ToolbarState.HEADING }
		Row(
			horizontalArrangement = Arrangement.Start,
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.height(80.dp),
		) {
			AnimatedContent(
				targetState = isViewer,
				transitionSpec = { slideIntoContainer(AnimatedContentScope.SlideDirection.Start, tween(300)) with slideOutOfContainer(AnimatedContentScope.SlideDirection.Start, tween(300)) },
				modifier = Modifier.weight(1f)
			) {
				if (it) {
					ViewerBottomBar(
						onCopy = {},
						onExport = {},
						onPrint = { richTextEditor.callPrintData(viewModel.content.value) },
						onDelete = { viewModel.showDeleteDialog.value = true }
					)
				} else {
					EditorBottomBar(
						userTimestamp = userTimestamp ?: 0,
						onClickUserTimestamp = { },
						onClickAttachment = onClickAttachment,
						onClickTag = onClickTag,
						onClickFormat = { toolbarState = ToolbarState.FORMAT }
					)

				}
			}
			Spacer(modifier = Modifier.width(16.dp))
			FloatingActionButton(
				onClick = {
					when {
						isSaving -> Toast.makeText(context, "Please wait while data is being saved", Toast.LENGTH_SHORT).show()
						isViewer && richTextEditor.isReady.value && viewModel.content.value == null -> Toast.makeText(
							context,
							"Please wait while editor is being loaded",
							Toast.LENGTH_SHORT
						).show()
						isViewer && richTextEditor.isReady.value -> {
							richTextEditor.exec("editor.commands.setContent(${viewModel.content.value});")
							viewModel.editNote()
						}
						isViewer && !richTextEditor.isReady.value -> Toast.makeText(context, "Please wait while editor is being loaded", Toast.LENGTH_SHORT)
							.show()
						richTextEditor.isReady.value -> richTextEditor.exec("editor.getData();")
						else -> Toast.makeText(context, "Please wait while editor is being loaded", Toast.LENGTH_SHORT).show()
					}
				},
				elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
			) {
				Crossfade(
					targetState = isViewer,
					animationSpec = tween(300)
				) {
					if (it) {
						Icon(
							painter = painterResource(id = R.drawable.ic_pencil),
							contentDescription = "Edit note",
							modifier = Modifier
						)
					} else {
						Icon(
							painter = painterResource(id = R.drawable.ic_check),
							contentDescription = "Save note",
							modifier = Modifier
						)
					}
				}
			}
			Spacer(modifier = Modifier.width(16.dp))
		}
	}
}

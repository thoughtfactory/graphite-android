package com.syncodec.graphite.presentation.note.composable.bar

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.presentation.common.richText.RichTextEditor
import com.syncodec.graphite.presentation.note.NoteViewModel
import com.syncodec.graphite.presentation.note.composable.bar.bottomBar.EditorToolbar
import com.syncodec.graphite.presentation.note.composable.bar.bottomBar.ViewerBottomBar
import com.syncodec.graphite.utils.LocalRichTextEditor
import com.syncodec.graphite.utils.tone


@Composable
fun BottomBar2(
	onClickMetadata: () -> Unit,
	onClickLocation: () -> Unit,
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

	richTextEditor.setOnFormatUpdate(
		object : RichTextEditor.OnFormatUpdateListener {
			override fun onFormatUpdate(newTextFormat: RichTextEditor.TextFormat) {
				textFormat = newTextFormat
			}
		}
	)

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(if (isViewer) MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1) else Color.Transparent)
	) {
		Spacer(modifier = Modifier.height(8.dp))

		Crossfade(
			targetState = isViewer,
			animationSpec = tween(300)
		) {
			if (it) {
				ViewerBottomBar(
					onCopy = { /*TODO*/ },
					onExport = { /*TODO*/ },
					onPrint = { /*TODO*/ },
					onDelete = { /*TODO*/ },
					onEdit = { viewModel.editNote() }
				)
			} else {
				EditorToolbar(
					richTextEditor = richTextEditor,
					textFormat = textFormat,
					toolbarState = ToolbarState.FORMAT,
					onClickMetadata = onClickMetadata,
					onClickLocation = onClickLocation,
					onClickAttachment = onClickAttachment,
					onClickTag = onClickTag,
					onClickCloseToolbar = { },
					onClickFormatToolbar = { },
					onClickHeadingToolbar = {}
				)
			}
		}

		Spacer(modifier = Modifier.height(16.dp))
	}
}

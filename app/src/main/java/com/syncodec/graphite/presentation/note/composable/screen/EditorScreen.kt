package com.syncodec.graphite.presentation.note.composable.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.note.NoteViewModel
import com.syncodec.graphite.utils.LocalRichTextEditor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun EditorScreen() {
	val viewModel: NoteViewModel = viewModel()
	val scope = rememberCoroutineScope()

	val richTextEditor = LocalRichTextEditor.current

	val noteId by viewModel.noteId
	val isViewer by viewModel.isViewer
	val isReady by richTextEditor.isReady

	LaunchedEffect(key1 = noteId.hashCode() + isViewer.hashCode()) {
		withContext(Dispatchers.IO) {
			richTextEditor.exec("editor.commands.setContent(${viewModel.content.value});")
		}
	}

	Crossfade(targetState = isReady) {
		if (it) {
			AndroidView(
				factory = { richTextEditor },
				modifier = Modifier.fillMaxSize()
			)
		} else {
			LoadingView()
		}
	}
}

package com.syncodec.graphite.presentation.note.composable.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.note.composable.LocalCompositionContent
import com.syncodec.graphite.presentation.note.composable.LocalCompositionIsViewing
import com.syncodec.graphite.presentation.note.composable.LocalCompositionNoteId
import com.syncodec.graphite.presentation.note.composable.LocalCompositionTitle
import com.syncodec.graphite.utils.LocalCompositionRichTextEditor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun EditorScreen() {
	val richTextEditor = LocalCompositionRichTextEditor.current

	val noteId = LocalCompositionNoteId.current
	val isViewing = LocalCompositionIsViewing.current
	val isReady by richTextEditor.isReady

	val content = LocalCompositionContent.current
	val title = LocalCompositionTitle.current

	LaunchedEffect(key1 = noteId.hashCode() + isViewing.hashCode()) {
		withContext(Dispatchers.IO) {
//			if (isViewing == false) richTextEditor.exec("editor.setData(\"${title ?: ""}\", ${content});")
			richTextEditor.exec("editor.setData(\"${title ?: ""}\", ${content});")
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

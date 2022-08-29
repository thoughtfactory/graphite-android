package com.syncodec.graphite.presentation.note.composable.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.syncodec.graphite.presentation.custom.LoadingView
import com.syncodec.graphite.utils.LocalRichTextEditor


@Composable
fun EditorScreen() {
	val richTextEditor = LocalRichTextEditor.current
	val isReady by richTextEditor.isReady

	Crossfade(targetState = isReady) {
		if (it) {
			AndroidView(
				factory = { richTextEditor },
				update = { viewer -> },
				modifier = Modifier.fillMaxSize()
			)
		} else {
			LoadingView()
		}
	}
}

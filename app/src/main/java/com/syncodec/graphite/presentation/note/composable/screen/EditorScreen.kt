package com.syncodec.graphite.presentation.note.composable.screen

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.syncodec.graphite.presentation.custom.LoadingView
import com.syncodec.graphite.utils.LocalRichTextEditor


@Composable
fun EditorScreen() {
	val richTextEditor = LocalRichTextEditor.current
	val isReady by richTextEditor.isReady

	var showTags by remember { mutableStateOf(true) }


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

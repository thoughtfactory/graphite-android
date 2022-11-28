package com.syncodec.graphite.presentation.note.composable.bar.bottomBar.editorBottomBar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.richText.RichTextEditor
import com.syncodec.graphite.presentation.note.composable.LocalCompositionUserTimestamp
import com.syncodec.graphite.utils.LocalCompositionRichTextEditor


@Composable
fun EditorBottomBar(
	onClickTimePicker: () -> Unit,
	onClickMetadata: () -> Unit,
	onClickLocation: () -> Unit,
	onClickAttachment: () -> Unit,
	onClickTag: () -> Unit,
) {
	val richTextEditor = LocalCompositionRichTextEditor.current

	val userTimestamp = LocalCompositionUserTimestamp.current

	var textFormat by remember { mutableStateOf(RichTextEditor.TextFormat()) }

	var isHeadingBarVisible by remember { mutableStateOf(false) }

	richTextEditor.setFormatUpdateListener(
		object : RichTextEditor.FormatUpdateListener {
			override fun onFormatUpdate(newTextFormat: RichTextEditor.TextFormat) {
				textFormat = newTextFormat
			}
		}
	)

	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		AnimatedVisibility(
			visible = isHeadingBarVisible,
			enter = expandVertically(tween(300)),
			exit = shrinkVertically(tween(300)),
		) {
			HeadingBar(
				richTextEditor = richTextEditor,
				textFormat = textFormat,
				closeBar = { isHeadingBarVisible = false },
			)
		}
		Spacer(modifier = Modifier.height(8.dp))
		FormatBar(
			richTextEditor = richTextEditor,
			textFormat = textFormat,
			userTimestamp = userTimestamp,
			onClickTimePicker = onClickTimePicker,
			onClickMetadata = onClickMetadata,
			onClickLocation = onClickLocation,
			onClickAttachment = onClickAttachment,
			onClickTag = onClickTag,
			onClickHeading = { isHeadingBarVisible = true },
		)
		Spacer(modifier = Modifier.height(16.dp))
	}
}

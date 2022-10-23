package com.syncodec.graphite.presentation.note.composable.bar.bottomBar.editorBottomBar

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
import com.syncodec.graphite.utils.LocalCompositionRichTextEditor


@Composable
fun EditorBottomBar(
	onClickMetadata: () -> Unit,
	onClickLocation: () -> Unit,
	onClickAttachment: () -> Unit,
	onClickTag: () -> Unit
) {
	val richTextEditor = LocalCompositionRichTextEditor.current

	var textFormat by remember { mutableStateOf(RichTextEditor.TextFormat()) }

	richTextEditor.setOnFormatUpdate(
		object : RichTextEditor.OnFormatUpdateListener {
			override fun onFormatUpdate(newTextFormat: RichTextEditor.TextFormat) {
				textFormat = newTextFormat
			}
		}
	)

	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		Spacer(modifier = Modifier.height(16.dp))
		FormatBar(
			richTextEditor = richTextEditor,
			textFormat = textFormat,
			onClickMetadata = onClickMetadata,
			onClickLocation = onClickLocation,
			onClickAttachment = onClickAttachment,
			onClickTag = onClickTag,
			onClickCloseToolbar = { /*TODO*/ },
			onClickHeadingToolbar = { /*TODO*/ },
		)
		Spacer(modifier = Modifier.height(16.dp))
	}
}

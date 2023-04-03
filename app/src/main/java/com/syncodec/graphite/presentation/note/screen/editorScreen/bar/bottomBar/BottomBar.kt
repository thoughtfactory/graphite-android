package com.syncodec.graphite.presentation.note.screen.editorScreen.bar.bottomBar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.richText.RichTextEditor
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue


@Preview
@Composable
fun BottomBar(
	textFormat : RichTextEditor.Companion.TextFormat = RichTextEditor.Companion.TextFormat(),
	userTimestamp : Long? = null,
	onClickTimePicker : () -> Unit = {},
	onClickMetadata : () -> Unit = {},
	onClickLocation : () -> Unit = {},
	onClickAttachment : () -> Unit = {},
	onClickTag : () -> Unit = {},
	onClickSwapEditor : () -> Unit = {},
	onEditorAction : (RichTextEditor.Companion.EditorAction) -> Unit = {},
) {
	var isHeadingBarVisible by remember { mutableStateOf(false) }

	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		AnimatedVisibility(
			visible = isHeadingBarVisible,
			enter = expandVertically(tween(300)),
			exit = shrinkVertically(tween(300)),
		) {
			HeadingBar(
				textFormat = textFormat,
				onEditorAction = onEditorAction,
				closeBar = { isHeadingBarVisible = false },
			)
		}
		Spacer(modifier = Modifier.height(8.dp))
		FormatBar(
			textFormat = textFormat,
			userTimestamp = userTimestamp,
			onClickTimePicker = onClickTimePicker,
			onClickMetadata = onClickMetadata,
			onClickLocation = onClickLocation,
			onClickAttachment = onClickAttachment,
			onClickTag = onClickTag,
			onClickHeading = { isHeadingBarVisible = true },
			onClickSwapEditor = onClickSwapEditor,
			onEditorAction = onEditorAction,
		)
		Spacer(modifier = Modifier.height(12.dp))
	}
}

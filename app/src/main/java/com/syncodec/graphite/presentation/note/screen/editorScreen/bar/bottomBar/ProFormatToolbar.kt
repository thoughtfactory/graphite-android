package com.syncodec.graphite.presentation.note.screen.editorScreen.bar.bottomBar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.richText.RichTextEditor
import com.syncodec.graphite.presentation.note.screen.editorScreen.bar.bottomBar.buildingBlock.ToolbarButton
import com.syncodec.graphite.presentation.note.screen.editorScreen.bar.bottomBar.buildingBlock.ToolbarSpacer


@Composable
fun ProFormatToolbar(
	textFormat : RichTextEditor.Companion.TextFormat = RichTextEditor.Companion.TextFormat(),
	onClickHeading : () -> Unit = {},
	onEditorAction : (RichTextEditor.Companion.EditorAction) -> Unit = {},
) {
	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.31f), MaterialTheme.shapes.small)
			.clip(MaterialTheme.shapes.small),
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(2.dp),
		) {
			Spacer(modifier = Modifier.width(2.dp))

			ToolbarButton(
				icon = R.drawable.ic_format_undo,
				contentDescription = "Undo",
				isChecked = false,
				onClick = { onEditorAction(RichTextEditor.Companion.EditorAction.UNDO) },
			)
			ToolbarButton(
				icon = R.drawable.ic_format_redo,
				contentDescription = "Redo",
				isChecked = false,
				onClick = { onEditorAction(RichTextEditor.Companion.EditorAction.REDO) },
			)

			ToolbarSpacer()

			ToolbarButton(
				icon = R.drawable.ic_format_bold,
				contentDescription = "Bold",
				isChecked = textFormat.bold,
				onClick = { onEditorAction(RichTextEditor.Companion.EditorAction.BOLD) },
			)
			ToolbarButton(
				icon = R.drawable.ic_format_italic,
				contentDescription = "Italic",
				isChecked = textFormat.italic,
				onClick = { onEditorAction(RichTextEditor.Companion.EditorAction.ITALIC) },
			)
			ToolbarButton(
				icon = R.drawable.ic_format_underline,
				contentDescription = "Underline",
				isChecked = textFormat.underline,
				onClick = { onEditorAction(RichTextEditor.Companion.EditorAction.UNDERLINE) },
			)
			ToolbarButton(
				icon = R.drawable.ic_format_strikethrough,
				contentDescription = "Strikethrough",
				isChecked = textFormat.strike,
				onClick = { onEditorAction(RichTextEditor.Companion.EditorAction.STRIKETHROUGH) },
			)
			ToolbarButton(
				icon = R.drawable.ic_format_hard_break,
				contentDescription = "Format hard break",
				isChecked = false,
				onClick = { onEditorAction(RichTextEditor.Companion.EditorAction.HARD_LINE_BREAK) },
			)
			ToolbarSpacer()

			ToolbarButton(
				icon = R.drawable.ic_format_list_check,
				contentDescription = "Check list",
				isChecked = textFormat.taskList,
				onClick = { onEditorAction(RichTextEditor.Companion.EditorAction.CHECK_LIST) },
			)
			ToolbarButton(
				icon = R.drawable.ic_format_list_bullet,
				contentDescription = "Bullet list",
				isChecked = textFormat.bulletList,
				onClick = { onEditorAction(RichTextEditor.Companion.EditorAction.BULLET_LIST) },
			)
			ToolbarButton(
				icon = R.drawable.ic_format_list_ordered,
				contentDescription = "Ordered list",
				isChecked = textFormat.orderedList,
				onClick = { onEditorAction(RichTextEditor.Companion.EditorAction.ORDERED_LIST) },
			)

			ToolbarSpacer()

			ToolbarButton(
				icon = when {
					textFormat.paragraph -> R.drawable.ic_format_paragraph
					textFormat.heading1 -> R.drawable.ic_format_h1
					textFormat.heading2 -> R.drawable.ic_format_h2
					textFormat.heading3 -> R.drawable.ic_format_h3
					textFormat.heading4 -> R.drawable.ic_format_h4
					textFormat.heading5 -> R.drawable.ic_format_h5
					textFormat.heading6 -> R.drawable.ic_format_h6
					else -> R.drawable.ic_format_paragraph
				},
				contentDescription = "Format heading",
				isChecked = textFormat.heading1 || textFormat.heading2 || textFormat.heading3 || textFormat.heading4 || textFormat.heading5 || textFormat.heading6,
				onClick = onClickHeading
			)
			ToolbarButton(
				icon = R.drawable.ic_format_blockquote,
				contentDescription = "Format blockquote",
				isChecked = textFormat.blockquote,
				onClick = { onEditorAction(RichTextEditor.Companion.EditorAction.BLOCK_QUOTE) },
			)

			ToolbarSpacer()

			ToolbarButton(
				icon = R.drawable.ic_format_indent,
				contentDescription = "Format indent",
				isChecked = false,
				onClick = { onEditorAction(RichTextEditor.Companion.EditorAction.INDENT) },
			)

			ToolbarButton(
				icon = R.drawable.ic_format_outdent,
				contentDescription = "Format outdent",
				isChecked = false,
				onClick = { onEditorAction(RichTextEditor.Companion.EditorAction.OUTDENT) },
			)

			ToolbarSpacer()

			ToolbarButton(
				icon = R.drawable.ic_format_superscript,
				contentDescription = "Format superscript",
				isChecked = textFormat.superscript,
				onClick = { onEditorAction(RichTextEditor.Companion.EditorAction.SUPERSCRIPT) },
			)
			ToolbarButton(
				icon = R.drawable.ic_format_subscript,
				contentDescription = "Format subscript",
				isChecked = textFormat.subscript,
				onClick = { onEditorAction(RichTextEditor.Companion.EditorAction.SUBSCRIPT) },
			)

			Spacer(modifier = Modifier.width(2.dp))
		}
	}
}

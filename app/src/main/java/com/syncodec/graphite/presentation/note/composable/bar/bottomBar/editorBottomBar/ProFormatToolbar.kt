package com.syncodec.graphite.presentation.note.composable.bar.bottomBar.editorBottomBar

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
import com.syncodec.graphite.presentation.note.composable.bar.bottomBar.editorBottomBar.buildingBlock.ToolbarButton
import com.syncodec.graphite.presentation.note.composable.bar.bottomBar.editorBottomBar.buildingBlock.ToolbarSpacer
import com.syncodec.graphite.utils.LocalCompositionRichTextEditor


@Composable
fun ProFormatToolbar(
	richTextEditor : RichTextEditor = LocalCompositionRichTextEditor.current,
	textFormat : RichTextEditor.TextFormat = RichTextEditor.TextFormat(),
	onClickHeading : () -> Unit = {},
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
				onClick = { richTextEditor.exec("editor.commands.undo();") }
			)
			ToolbarButton(
				icon = R.drawable.ic_format_redo,
				contentDescription = "Redo",
				isChecked = false,
				onClick = { richTextEditor.exec("editor.commands.redo();") }
			)

			ToolbarSpacer()

			ToolbarButton(
				icon = R.drawable.ic_format_bold,
				contentDescription = "Bold",
				isChecked = textFormat.bold,
				onClick = { richTextEditor.exec("editor.chain().focus().toggleBold().run()") }
			)
			ToolbarButton(
				icon = R.drawable.ic_format_italic,
				contentDescription = "Italic",
				isChecked = textFormat.italic,
				onClick = { richTextEditor.exec("editor.chain().focus().toggleItalic().run()") }
			)
			ToolbarButton(
				icon = R.drawable.ic_format_underline,
				contentDescription = "Underline",
				isChecked = textFormat.underline,
				onClick = { richTextEditor.exec("editor.chain().focus().toggleUnderline().run()") }
			)
			ToolbarButton(
				icon = R.drawable.ic_format_strikethrough,
				contentDescription = "Strikethrough",
				isChecked = textFormat.strike,
				onClick = { richTextEditor.exec("editor.chain().focus().toggleStrike().run()") }
			)
			ToolbarButton(
				icon = R.drawable.ic_format_hard_break,
				contentDescription = "Format hard break",
				isChecked = false,
				onClick = { richTextEditor.exec("editor.chain().focus().setHardBreak().run()") }
			)
			ToolbarSpacer()

			ToolbarButton(
				icon = R.drawable.ic_format_list_check,
				contentDescription = "Check list",
				isChecked = textFormat.taskList,
				onClick = { richTextEditor.exec("editor.commands.toggleTaskList();") }
			)
			ToolbarButton(
				icon = R.drawable.ic_format_list_bullet,
				contentDescription = "Bullet list",
				isChecked = textFormat.bulletList,
				onClick = { richTextEditor.exec("editor.commands.toggleBulletList();") }
			)
			ToolbarButton(
				icon = R.drawable.ic_format_list_ordered,
				contentDescription = "Ordered list",
				isChecked = textFormat.orderedList,
				onClick = { richTextEditor.exec("editor.commands.toggleOrderedList();") }
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
				onClick = { richTextEditor.exec("editor.chain().focus().toggleBlockquote().run();") }
			)

			ToolbarSpacer()

			ToolbarButton(
				icon = R.drawable.ic_format_indent,
				contentDescription = "Format indent",
				isChecked = false,
				onClick = { richTextEditor.exec("editor.chain().focus().sinkListItem('listItem').run()") }
			)

			ToolbarButton(
				icon = R.drawable.ic_format_outdent,
				contentDescription = "Format outdent",
				isChecked = false,
				onClick = { richTextEditor.exec("editor.chain().focus().liftListItem('listItem').run()") }
			)

			ToolbarSpacer()

			ToolbarButton(
				icon = R.drawable.ic_format_superscript,
				contentDescription = "Format superscript",
				isChecked = textFormat.superscript,
				onClick = { richTextEditor.exec("editor.chain().focus().toggleSuperscript().run();") }
			)
			ToolbarButton(
				icon = R.drawable.ic_format_subscript,
				contentDescription = "Format subscript",
				isChecked = textFormat.subscript,
				onClick = { richTextEditor.exec("editor.chain().focus().toggleSubscript().run();") }
			)

			Spacer(modifier = Modifier.width(2.dp))
		}
	}
}

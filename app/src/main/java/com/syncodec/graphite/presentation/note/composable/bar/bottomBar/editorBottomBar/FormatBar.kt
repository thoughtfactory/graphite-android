package com.syncodec.graphite.presentation.note.composable.bar.bottomBar.editorBottomBar

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.richText.RichTextEditor
import com.syncodec.graphite.presentation.note.composable.bar.bottomBar.editorBottomBar.buildingBlock.ToolbarSpacer
import com.syncodec.graphite.utils.tone
import java.util.Calendar


@Composable
fun FormatBar(
	richTextEditor : RichTextEditor,
	textFormat : RichTextEditor.TextFormat,
	userTimestamp : Long?,
	onClickTimePicker : () -> Unit,
	onClickMetadata : () -> Unit,
	onClickLocation : () -> Unit,
	onClickAttachment : () -> Unit,
	onClickTag : () -> Unit,
	onClickHeading : () -> Unit,
) {

	val calendar = remember { Calendar.getInstance() }

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier.horizontalScroll(rememberScrollState()),
	) {
		Spacer(modifier = Modifier.width(12.dp))

		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.height(48.dp)
				.background(
					color = MaterialTheme.colorScheme.surface
						.tone(isSystemInDarkTheme(), 1)
						.copy(alpha = 0.31f),
					shape = RoundedCornerShape(50)
				)
				.clip(RoundedCornerShape(50))
				.clickable { onClickTimePicker() }
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxHeight(),
			) {
				Spacer(modifier = Modifier.width(12.dp))

				Text(
					text = DateFormat.format("dd", userTimestamp ?: calendar.timeInMillis).toString(),
					style = MaterialTheme.typography.headlineMedium,
					color = MaterialTheme.colorScheme.onSurface,
					fontWeight = FontWeight.Bold
				)

				Spacer(modifier = Modifier.width(4.dp))

				Column(
					verticalArrangement = Arrangement.Center,
					modifier = Modifier.fillMaxHeight(),
				) {
					Text(
						text = DateFormat.format("MMM, yyyy", userTimestamp ?: calendar.timeInMillis).toString(),
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurface,
					)

					Text(
						text = DateFormat.format("hh:mm aa", userTimestamp ?: calendar.timeInMillis).toString(),
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurface,
					)
				}

				Spacer(modifier = Modifier.width(12.dp))
			}
		}

		Spacer(modifier = Modifier.width(8.dp))

		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.height(48.dp)
				.background(
					color = MaterialTheme.colorScheme.surface
						.tone(isSystemInDarkTheme(), 1)
						.copy(alpha = 0.31f),
					shape = RoundedCornerShape(50)
				)
				.clip(RoundedCornerShape(50)),
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxHeight(),
			) {
				Spacer(modifier = Modifier.width(12.dp))

				MenuButton(
					icon = R.drawable.ic_info,
					contentDescription = "Info",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					shape = CircleShape,
					onClick = onClickMetadata
				)

				MenuButton(
					icon = R.drawable.ic_map_marker,
					contentDescription = "Location",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					shape = CircleShape,
					onClick = onClickLocation
				)

				MenuButton(
					icon = R.drawable.ic_attachment,
					contentDescription = "Attachment",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					shape = CircleShape,
					onClick = onClickAttachment
				)

				MenuButton(
					icon = R.drawable.ic_hashtag,
					contentDescription = "Tag",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					shape = CircleShape,
					onClick = onClickTag
				)

				Spacer(modifier = Modifier.width(12.dp))
			}
		}

		Spacer(modifier = Modifier.width(8.dp))

		Row(
			modifier = Modifier
				.background(
					color = MaterialTheme.colorScheme.surface
						.tone(isSystemInDarkTheme(), 1)
						.copy(alpha = 0.31f),
					shape = RoundedCornerShape(50)
				)
				.clip(RoundedCornerShape(50))
		) {
			Row(
				modifier = Modifier.padding(2.dp)
			) {
				MenuButton(
					icon = R.drawable.ic_format_undo,
					contentDescription = "Undo",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					onClick = { richTextEditor.exec("editor.commands.undo();") }
				)
				MenuButton(
					icon = R.drawable.ic_format_redo,
					contentDescription = "Redo",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					onClick = { richTextEditor.exec("editor.commands.redo();") }
				)

				ToolbarSpacer()

				MenuButton(
					icon = R.drawable.ic_format_bold,
					contentDescription = "Bold",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					isChecked = textFormat.bold,
					onClick = { richTextEditor.exec("editor.chain().focus().toggleBold().run()") }
				)
				MenuButton(
					icon = R.drawable.ic_format_italic,
					contentDescription = "Italic",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					isChecked = textFormat.italic,
					onClick = { richTextEditor.exec("editor.chain().focus().toggleItalic().run()") }
				)
				MenuButton(
					icon = R.drawable.ic_format_underline,
					contentDescription = "Underline",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					isChecked = textFormat.underline,
					onClick = { richTextEditor.exec("editor.chain().focus().toggleUnderline().run()") }
				)
				MenuButton(
					icon = R.drawable.ic_format_strikethrough,
					contentDescription = "Strikethrough",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					isChecked = textFormat.strike,
					onClick = { richTextEditor.exec("editor.chain().focus().toggleStrike().run()") }
				)
				MenuButton(
					icon = R.drawable.ic_format_hard_break,
					contentDescription = "Format hard break",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					onClick = { richTextEditor.exec("editor.chain().focus().setHardBreak().run()") }
				)
				ToolbarSpacer()

				MenuButton(
					icon = R.drawable.ic_format_list_check,
					contentDescription = "Check list",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					isChecked = textFormat.taskList,
					onClick = { richTextEditor.exec("editor.commands.toggleTaskList();") }
				)
				MenuButton(
					icon = R.drawable.ic_format_list_bullet,
					contentDescription = "Bullet list",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					isChecked = textFormat.bulletList,
					onClick = { richTextEditor.exec("editor.commands.toggleBulletList();") }
				)
				MenuButton(
					icon = R.drawable.ic_format_list_ordered,
					contentDescription = "Ordered list",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					isChecked = textFormat.orderedList,
					onClick = { richTextEditor.exec("editor.commands.toggleOrderedList();") }
				)

				ToolbarSpacer()

				MenuButton(
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
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					isChecked = textFormat.heading1 || textFormat.heading2 || textFormat.heading3 || textFormat.heading4 || textFormat.heading5 || textFormat.heading6,
					onClick = onClickHeading
				)
				MenuButton(
					icon = R.drawable.ic_format_blockquote,
					contentDescription = "Format blockquote",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					isChecked = textFormat.blockquote,
					onClick = { richTextEditor.exec("editor.chain().focus().toggleBlockquote().run();") }
				)

				ToolbarSpacer()

				MenuButton(
					icon = R.drawable.ic_format_indent,
					contentDescription = "Format indent",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					onClick = { richTextEditor.exec("editor.chain().focus().sinkListItem('listItem').run()") }
				)

				MenuButton(
					icon = R.drawable.ic_format_outdent,
					contentDescription = "Format outdent",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					onClick = { richTextEditor.exec("editor.chain().focus().liftListItem('listItem').run()") }
				)

				ToolbarSpacer()

				MenuButton(
					icon = R.drawable.ic_format_superscript,
					contentDescription = "Format superscript",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					isChecked = textFormat.superscript,
					onClick = { richTextEditor.exec("editor.chain().focus().toggleSuperscript().run();") }
				)
				MenuButton(
					icon = R.drawable.ic_format_subscript,
					contentDescription = "Format subscript",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					isChecked = textFormat.subscript,
					onClick = { richTextEditor.exec("editor.chain().focus().toggleSubscript().run();") }
				)
			}
		}

		Spacer(modifier = Modifier.width(12.dp))
	}
}

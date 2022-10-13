package com.syncodec.graphite.presentation.note.composable.bar.bottomBar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.richText.RichTextEditor
import com.syncodec.graphite.presentation.note.composable.bar.*
import com.syncodec.graphite.utils.LocalCompositionPremium
import com.syncodec.graphite.utils.tone
import java.text.SimpleDateFormat


@Composable
fun EditorBottomBar(
	userTimestamp: Long,
	onClickUserTimestamp: () -> Unit,
	onClickAttachment: () -> Unit,
	onClickTag: () -> Unit,
	onClickFormat: () -> Unit,
) {
	LazyRow(
		modifier = Modifier,
		verticalAlignment = Alignment.CenterVertically
	) {
		item { Spacer(modifier = Modifier.width(8.dp)) }
		item { DateTimeButton(userTimestamp = userTimestamp, onClick = onClickUserTimestamp) }
		item { ToolbarSpacer() }
		item {
			MenuButton(
				isEnabled = true,
				icon = R.drawable.ic_attachment,
				contentDescription = "Attachment",
				onClick = onClickAttachment
			)
		}
		item {
			MenuButton(
				isEnabled = true,
				icon = R.drawable.ic_hashtag,
				contentDescription = "Tag",
				onClick = onClickTag
			)
		}
		item {
			MenuButton(
				isEnabled = true,
				icon = R.drawable.ic_text_format,
				contentDescription = "Text Format",
				onClick = onClickFormat
			)
		}
		item { Spacer(modifier = Modifier.width(32.dp)) }
	}
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun DateTimeButton(
	userTimestamp: Long,
	onClick: () -> Unit
) {
	val contentColor = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1)

	AnimatedContent(targetState = userTimestamp) {
		Row(
			modifier = Modifier
				.height(56.dp)
				.clip(RoundedCornerShape(25))
				.clickable { onClick() },
			verticalAlignment = Alignment.CenterVertically
		) {
			Spacer(modifier = Modifier.width(8.dp))
			Icon(
				painter = painterResource(id = R.drawable.ic_clock),
				contentDescription = null,
				tint = contentColor,
				modifier = Modifier.requiredSize(28.dp)
			)
			Column(
				modifier = Modifier
					.fillMaxHeight()
					.padding(8.dp, 0.dp),
				horizontalAlignment = Alignment.Start,
				verticalArrangement = Arrangement.Center
			) {
				Text(
					text = SimpleDateFormat("h:mm a, EEE").format(it),
					style = MaterialTheme.typography.bodyMedium,
					color = contentColor,
					fontWeight = FontWeight.Bold
				)
				Text(
					text = SimpleDateFormat("MMM d, yyyy").format(it),
					style = MaterialTheme.typography.bodySmall,
					color = contentColor,
					fontWeight = FontWeight.Bold
				)
			}
		}
		Spacer(modifier = Modifier.width(8.dp))
	}
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EditorToolbar(
	richTextEditor: RichTextEditor,
	textFormat: RichTextEditor.TextFormat,
	toolbarState: ToolbarState,
	onClickMetadata: () -> Unit,
	onClickLocation: () -> Unit,
	onClickAttachment: () -> Unit,
	onClickTag: () -> Unit,
	onClickCloseToolbar: () -> Unit,
	onClickFormatToolbar: () -> Unit,
	onClickHeadingToolbar: () -> Unit,
) {
	AnimatedContent(targetState = toolbarState) {
		Column(
			modifier = Modifier.fillMaxWidth()
		) {
			FormatToolbar(
				richTextEditor = richTextEditor,
				textFormat = textFormat,
				onClickMetadata = onClickMetadata,
				onClickLocation = onClickLocation,
				onClickAttachment = onClickAttachment,
				onClickTag = onClickTag,
				onClickCloseToolbar = onClickCloseToolbar,
				onClickHeadingToolbar = onClickHeadingToolbar
			)
		}
	}
}

@Composable
private fun FormatToolbar(
	richTextEditor: RichTextEditor,
	textFormat: RichTextEditor.TextFormat,
	onClickMetadata: () -> Unit,
	onClickLocation: () -> Unit,
	onClickAttachment: () -> Unit,
	onClickTag: () -> Unit,
	onClickCloseToolbar: () -> Unit,
	onClickHeadingToolbar: () -> Unit,
) {
	val isPremium = LocalCompositionPremium.current

	Row(
		modifier = Modifier.horizontalScroll(rememberScrollState())
	) {
		Spacer(modifier = Modifier.width(16.dp))
		Row(
			modifier = Modifier
				.background(
					color = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.31f),
					shape =  RoundedCornerShape(50)
				)
				.clip(RoundedCornerShape(50))
		) {
			Row(
				modifier = Modifier.padding(2.dp)
			) {

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


				ToolbarSpacer()

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
					onClick = onClickHeadingToolbar
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
		Spacer(modifier = Modifier.width(16.dp))
	}
}

@Composable
private fun HeadingToolbar(
	richTextEditor: RichTextEditor,
	textFormat: RichTextEditor.TextFormat,
	onClickFormatToolbar: () -> Unit,
) {
	Box(
		modifier = Modifier.fillMaxWidth()
	) {
		Row(
			modifier = Modifier
				.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.747f), RoundedCornerShape(50))
				.clip(RoundedCornerShape(50))
		) {
			Spacer(modifier = Modifier.width(4.dp))

			MenuButton(
				icon = R.drawable.ic_chevron_down,
				contentDescription = "Format toolbar",
				onClick = onClickFormatToolbar
			)

			ToolbarSpacer()

			MenuButton(
				icon = R.drawable.ic_format_paragraph,
				contentDescription = "Format paragraph",
				isChecked = textFormat.paragraph,
				onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 3 });") }
			)
			MenuButton(
				icon = R.drawable.ic_format_h1,
				contentDescription = "Format heading 1",
				isChecked = textFormat.heading1,
				onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 1 });") }
			)
			MenuButton(
				icon = R.drawable.ic_format_h2,
				contentDescription = "Format heading 2",
				isChecked = textFormat.heading2,
				onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 2 });") }
			)
			MenuButton(
				icon = R.drawable.ic_format_h3,
				contentDescription = "Format heading 3",
				isChecked = textFormat.heading3,
				onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 3 });") }
			)
			MenuButton(
				icon = R.drawable.ic_format_h4,
				contentDescription = "Format heading 4",
				isChecked = textFormat.heading4,
				onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 4 });") }
			)
			MenuButton(
				icon = R.drawable.ic_format_h5,
				contentDescription = "Format heading 5",
				isChecked = textFormat.heading5,
				onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 5 });") }
			)
			MenuButton(
				icon = R.drawable.ic_format_h6,
				contentDescription = "Format heading 6",
				isChecked = textFormat.heading6,
				onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 6 });") }
			)
		}
	}
}

@Composable
private fun ToolbarSpacer() {
	Row(
		modifier = Modifier.height(44.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Spacer(modifier = Modifier.width(4.dp))
		Spacer(
			modifier = Modifier
				.width(2.dp)
				.height(24.dp)
				.clip(RoundedCornerShape(50))
				.background(MaterialTheme.colorScheme.onSurface)
		)
		Spacer(modifier = Modifier.width(4.dp))
	}
}

package com.syncodec.graphite.noteComponent.toolbar

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.custom.richText.RichTextEditor
import com.syncodec.graphite.miscellaneous.ThemeUtils.Companion.tone
import com.syncodec.graphite.noteComponent.NoteActivity
import java.text.SimpleDateFormat


private enum class ToolbarState {
	BASE,
	STATE,
	HEADING,
	LINK
}

private enum class ToolbarButton {
	TIMESTAMP_PICKER,
	ATTACHMENT,
	TAG,
	STATE,
	OPEN_FORMAT,
	CLOSE_FORMAT,
	METADATA,
	MENU,
	UNDO,
	REDO,
	BOLD,
	ITALIC,
	UNDERLINE,
	STRIKE,
	HARD_BREAK,
	CHECK_LIST,
	BULLET_LIST,
	ORDERED_LIST,
	HEADING,
	PARAGRAPH,
	H1,
	H2,
	H3,
	H4,
	H5,
	H6,
	BLOCKQUOTE,
	INDENT,
	OUTDENT,
	LINK,
	SUPERSCRIPT,
	SUBSCRIPT,
	CODE,
	CODE_BLOCK,
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun EditorToolbar(
	richTextEditor: RichTextEditor,
	userTimestamp: Long,
	isFavourite: Boolean,
	isArchive: Boolean,
	isLocked: Boolean,
	onAction: (NoteActivity.Action) -> Unit,
) {
	var showFormatter by remember { mutableStateOf(false) }
	var toolbarState by remember { mutableStateOf(ToolbarState.BASE) }

	var textFormat by remember { mutableStateOf(RichTextEditor.TextFormat()) }

	richTextEditor.setOnFormatUpdate(object : RichTextEditor.OnFormatUpdateListener {
		override fun onFormatUpdate(newTextFormat: RichTextEditor.TextFormat) {
			textFormat = newTextFormat
		}
	})

	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		AnimatedContent(
			targetState = toolbarState,
			transitionSpec = {
				(scaleIn(tween(600), 0f)
						with scaleOut(tween(600), 1f))
					.using(SizeTransform(clip = false))
			}
		) {
			when (it) {
				ToolbarState.BASE -> null
				ToolbarState.STATE -> NoteStateToolbar(
					isFavourite = isFavourite,
					isArchive = isArchive,
					isLocked = isLocked
				) { onAction(it) }
				ToolbarState.HEADING -> TextHeadingToolbar(textFormat = textFormat) { toolbarButton ->
					when (toolbarButton) {
						ToolbarButton.PARAGRAPH -> richTextEditor.exec("editor.commands.toggleHeading({ level: 3 });")
						ToolbarButton.H1 -> richTextEditor.exec("editor.commands.toggleHeading({ level: 1 });")
						ToolbarButton.H2 -> richTextEditor.exec("editor.commands.toggleHeading({ level: 2 });")
						ToolbarButton.H3 -> richTextEditor.exec("editor.commands.toggleHeading({ level: 3 });")
						ToolbarButton.H4 -> richTextEditor.exec("editor.commands.toggleHeading({ level: 4 });")
						ToolbarButton.H5 -> richTextEditor.exec("editor.commands.toggleHeading({ level: 5 });")
						ToolbarButton.H6 -> richTextEditor.exec("editor.commands.toggleHeading({ level: 6 });")
					}
				}
			}
		}

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(56.dp)
				.background(MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1))
		) {
			AnimatedContent(
				targetState = showFormatter,
				transitionSpec = {
					(slideInHorizontally(tween(600)) { width -> -width } + fadeIn()
							with slideOutHorizontally(tween(600)) { width -> width } + fadeOut())
						.using(SizeTransform(clip = false))
				}
			) {
				if (it) {
					FormatEditorToolbar(
						textFormat = textFormat,
					) { toolbarButton ->
						when (toolbarButton) {
							ToolbarButton.CLOSE_FORMAT -> {
								toolbarState = ToolbarState.BASE
								showFormatter = false
							}
							ToolbarButton.UNDO -> richTextEditor.exec("editor.commands.undo();")
							ToolbarButton.REDO -> richTextEditor.exec("editor.commands.redo();")
							ToolbarButton.BOLD -> richTextEditor.exec("editor.chain().focus().toggleBold().run()")
							ToolbarButton.ITALIC -> richTextEditor.exec("editor.chain().focus().toggleItalic().run();")
							ToolbarButton.UNDERLINE -> richTextEditor.exec("editor.chain().focus().toggleUnderline().run();")
							ToolbarButton.STRIKE -> richTextEditor.exec("editor.chain().focus().toggleStrike().run();")
							ToolbarButton.HARD_BREAK -> richTextEditor.exec(
								"editor.chain().focus().setHardBreak().run()",
								false
							)
							ToolbarButton.CHECK_LIST -> richTextEditor.exec("editor.commands.toggleTaskList();")
							ToolbarButton.BULLET_LIST -> richTextEditor.exec("editor.commands.toggleBulletList();")
							ToolbarButton.ORDERED_LIST -> richTextEditor.exec("editor.commands.toggleOrderedList();")
							ToolbarButton.BLOCKQUOTE -> richTextEditor.exec("editor.chain().focus().toggleBlockquote().run();")
							ToolbarButton.HEADING -> toolbarState =
								if (toolbarState == ToolbarState.HEADING) ToolbarState.BASE else ToolbarState.HEADING
							ToolbarButton.INDENT -> richTextEditor.exec("editor.chain().focus().sinkListItem('listItem').run()")
							ToolbarButton.OUTDENT -> richTextEditor.exec("editor.chain().focus().liftListItem('listItem').run()")
							ToolbarButton.LINK -> toolbarState = ToolbarState.LINK
							ToolbarButton.SUPERSCRIPT -> richTextEditor.exec("editor.chain().focus().toggleSuperscript().run();")
							ToolbarButton.SUBSCRIPT -> richTextEditor.exec("editor.chain().focus().toggleSubscript().run();")
							ToolbarButton.CODE -> richTextEditor.exec("editor.commands.toggleCode();")
							ToolbarButton.CODE_BLOCK -> richTextEditor.exec("editor.commands.setCodeBlock();")
						}
					}
				} else {
					StateEditorToolbar(
						userTimestamp = userTimestamp
					) { toolbarButton ->
						when (toolbarButton) {
							ToolbarButton.TIMESTAMP_PICKER -> onAction(NoteActivity.Action.SELECT_TIME)
							ToolbarButton.ATTACHMENT -> onAction(NoteActivity.Action.ATTACHMENT_BUTTON)
							ToolbarButton.TAG -> onAction(NoteActivity.Action.TAG_BUTTON)
							ToolbarButton.STATE -> toolbarState =
								if (toolbarState == ToolbarState.STATE) ToolbarState.BASE else ToolbarState.STATE
							ToolbarButton.OPEN_FORMAT -> {
								toolbarState = ToolbarState.BASE
								showFormatter = true
							}
							ToolbarButton.METADATA -> onAction(NoteActivity.Action.OPEN_METADATA)
							ToolbarButton.MENU -> onAction(NoteActivity.Action.OPEN_MENU)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun StateEditorToolbar(
	userTimestamp: Long,
	onClick: (ToolbarButton) -> Unit,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(56.dp)
			.horizontalScroll(rememberScrollState()),
		verticalAlignment = Alignment.CenterVertically
	) {
		Spacer(modifier = Modifier.width(8.dp))
		DateTimeButton(userTimestamp = userTimestamp) { onClick(ToolbarButton.TIMESTAMP_PICKER) }

		ToolbarSpacer()

		ToolbarButton(
			name = "Attachment",
			icon = R.drawable.ic_attachment,
			highlight = false
		) { onClick(ToolbarButton.ATTACHMENT) }
		ToolbarButton(
			name = "Tag",
			icon = R.drawable.ic_hashtag,
			highlight = false
		) { onClick(ToolbarButton.TAG) }
		ToolbarButton(
			name = "State",
			icon = R.drawable.ic_state,
			highlight = false
		) { onClick(ToolbarButton.STATE) }
		ToolbarButton(
			name = "Text Format",
			icon = R.drawable.ic_text_format,
			highlight = false
		) { onClick(ToolbarButton.OPEN_FORMAT) }
		ToolbarButton(
			name = "Metadata",
			icon = R.drawable.ic_info,
			highlight = false
		) { onClick(ToolbarButton.METADATA) }
	}
}

@Composable
private fun FormatEditorToolbar(
	textFormat: RichTextEditor.TextFormat,
	onClick: (ToolbarButton) -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(56.dp)
			.horizontalScroll(rememberScrollState()),
		verticalAlignment = Alignment.CenterVertically
	) {
		Spacer(modifier = Modifier.width(8.dp))
		ToolbarButton(
			name = "Close formatter",
			icon = R.drawable.ic_menu_vertical,
			highlight = false
		)
		{ onClick(ToolbarButton.CLOSE_FORMAT) }
		ToolbarButton(
			name = "Undo",
			icon = R.drawable.ic_format_undo,
			highlight = false
		) { onClick(ToolbarButton.UNDO) }
		ToolbarButton(
			name = "Redo",
			icon = R.drawable.ic_format_redo,
			highlight = false
		) { onClick(ToolbarButton.REDO) }

		ToolbarSpacer()

		ToolbarButton(
			name = "Format bold",
			icon = R.drawable.ic_format_bold,
			highlight = textFormat.bold
		) { onClick(ToolbarButton.BOLD) }
		ToolbarButton(
			name = "Format italic",
			icon = R.drawable.ic_format_italic,
			highlight = textFormat.italic
		) { onClick(ToolbarButton.ITALIC) }
		ToolbarButton(
			name = "Format underline",
			icon = R.drawable.ic_format_underline,
			highlight = textFormat.underline
		) { onClick(ToolbarButton.UNDERLINE) }
		ToolbarButton(
			name = "Format strikethrough",
			icon = R.drawable.ic_format_strikethrough,
			highlight = textFormat.strike
		) { onClick(ToolbarButton.STRIKE) }
		ToolbarButton(
			name = "Hard break",
			icon = R.drawable.ic_format_hard_break,
			highlight = false
		) { onClick(ToolbarButton.HARD_BREAK) }

		ToolbarSpacer()

		ToolbarButton(
			name = "Check list",
			icon = R.drawable.ic_format_list_check,
			highlight = textFormat.taskList
		) { onClick(ToolbarButton.CHECK_LIST) }
		ToolbarButton(
			name = "Bullet list",
			icon = R.drawable.ic_format_list_bullet,
			highlight = textFormat.bulletList
		) { onClick(ToolbarButton.BULLET_LIST) }
		ToolbarButton(
			name = "Ordered list",
			icon = R.drawable.ic_format_list_ordered,
			highlight = textFormat.orderedList
		) { onClick(ToolbarButton.ORDERED_LIST) }

		ToolbarSpacer()

		ToolbarButton(
			name = "heading",
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
			highlight = textFormat.heading1 || textFormat.heading2 || textFormat.heading3 || textFormat.heading4 || textFormat.heading5 || textFormat.heading6
		) { onClick(ToolbarButton.HEADING) }
		ToolbarButton(
			name = "Blockquote",
			icon = R.drawable.ic_format_blockquote,
			highlight = textFormat.blockquote
		) { onClick(ToolbarButton.BLOCKQUOTE) }
		ToolbarSpacer()

		ToolbarButton(
			name = "Ordered list",
			icon = R.drawable.ic_format_indent,
			highlight = false
		) { onClick(ToolbarButton.INDENT) }
		ToolbarButton(
			name = "Ordered list",
			icon = R.drawable.ic_format_outdent,
			highlight = false
		) { onClick(ToolbarButton.OUTDENT) }
		ToolbarButton(
			name = if (textFormat.link == null) "Link" else "Unlink",
			icon = if (textFormat.link == null) R.drawable.ic_format_link else R.drawable.ic_format_unlink,
			highlight = textFormat.link != null
		) { onClick(ToolbarButton.LINK) }
		ToolbarButton(
			name = "Superscript",
			icon = R.drawable.ic_format_superscript,
			highlight = textFormat.superscript
		) { onClick(ToolbarButton.SUPERSCRIPT) }
		ToolbarButton(
			name = "Subscript",
			icon = R.drawable.ic_format_subscript,
			highlight = textFormat.subscript
		) { onClick(ToolbarButton.SUBSCRIPT) }
		ToolbarButton(
			name = "Code",
			icon = R.drawable.ic_format_code,
			highlight = textFormat.code
		) { onClick(ToolbarButton.CODE) }
		ToolbarButton(
			name = "Code block",
			icon = R.drawable.ic_format_code_block,
			highlight = textFormat.codeBlock
		) { onClick(ToolbarButton.CODE_BLOCK) }

		Spacer(modifier = Modifier.width(8.dp))
	}
}

@Composable
private fun ToolbarButton(
	name: String,
	icon: Int,
	highlight: Boolean,
	onClick: () -> Unit
) {
	Row(
		modifier = Modifier.height(40.dp)
	) {
		val containerColor by animateColorAsState(
			if (highlight)
				contentColorFor(MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1))
			else
				MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)
		)
		val contentColor by animateColorAsState(
			if (highlight)
				MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)
			else
				contentColorFor(MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1))
		)

		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.requiredSize(40.dp)
				.clip(RoundedCornerShape(25))
				.background(containerColor)
				.clickable { onClick() },
		) {
			Icon(
				painter = painterResource(id = icon),
				contentDescription = name,
				tint = contentColor,
				modifier = Modifier.padding(8.dp)
			)
		}

		Spacer(modifier = Modifier.width(4.dp))
	}
}

@Composable
private fun ToolbarSpacer() {
	Row(
		modifier = Modifier.height(56.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Spacer(modifier = Modifier.width(4.dp))
		Spacer(
			modifier = Modifier
				.width(2.dp)
				.height(24.dp)
				.clip(RoundedCornerShape(50))
				.background(
					contentColorFor(
						MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)
					)
				)
		)
		Spacer(modifier = Modifier.width(4.dp))
	}
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun DateTimeButton(
	userTimestamp: Long,
	onClick: () -> Unit
) {
	AnimatedContent(targetState = userTimestamp) {
		Row(
			modifier = Modifier
				.height(40.dp)
				.clip(RoundedCornerShape(25))
				.clickable { onClick() },
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_clock),
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1),
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
					fontWeight = FontWeight.Bold,
					color = contentColorFor(
						MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)
					)
				)
				Text(
					text = SimpleDateFormat("MMM d, yyyy").format(it),
					style = MaterialTheme.typography.bodySmall,
					fontWeight = FontWeight.Bold,
					color =
					contentColorFor(
						MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)
					)
				)
			}
		}
		Spacer(modifier = Modifier.width(4.dp))
	}
}

@Composable
private fun NoteStateToolbar(
	isFavourite: Boolean,
	isArchive: Boolean,
	isLocked: Boolean,
	onAction: (NoteActivity.Action) -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(48.dp)
			.background(MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Row(
			modifier = Modifier
				.height(48.dp)
				.horizontalScroll(rememberScrollState()),
			verticalAlignment = Alignment.Bottom,
		) {
			Spacer(modifier = Modifier.width(8.dp))

			ToolbarButton(
				name = "Favourite",
				icon = R.drawable.ic_favourite,
				highlight = isFavourite
			) { onAction(NoteActivity.Action.TOGGLE_FAVOURITE) }
			ToolbarButton(
				name = "Archive",
				icon = R.drawable.ic_archive,
				highlight = isArchive
			) { onAction(NoteActivity.Action.TOGGLE_ARCHIVE) }
			ToolbarButton(
				name = "Lock",
				icon = if (isLocked) R.drawable.ic_lock_close else R.drawable.ic_lock_open,
				highlight = isLocked
			) { onAction(NoteActivity.Action.TOGGLE_LOCKED) }

			Spacer(modifier = Modifier.width(8.dp))
		}
	}
}

@Composable
private fun TextHeadingToolbar(
	textFormat: RichTextEditor.TextFormat,
	onClick: (ToolbarButton) -> Unit,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(48.dp)
			.background(MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Row(
			modifier = Modifier
				.height(48.dp)
				.horizontalScroll(rememberScrollState()),
			verticalAlignment = Alignment.Bottom,
		) {
			Spacer(modifier = Modifier.width(8.dp))

			ToolbarButton(
				name = "Paragraph",
				icon = R.drawable.ic_format_paragraph,
				highlight = textFormat.paragraph
			) { onClick(ToolbarButton.PARAGRAPH) }
			ToolbarButton(
				name = "Heading 1",
				icon = R.drawable.ic_format_h1,
				highlight = textFormat.heading1
			) { onClick(ToolbarButton.H1) }
			ToolbarButton(
				name = "Heading 2",
				icon = R.drawable.ic_format_h2,
				highlight = textFormat.heading2
			) { onClick(ToolbarButton.H2) }
			ToolbarButton(
				name = "Heading 3",
				icon = R.drawable.ic_format_h3,
				highlight = textFormat.heading3
			) { onClick(ToolbarButton.H3) }
			ToolbarButton(
				name = "Heading 4",
				icon = R.drawable.ic_format_h4,
				highlight = textFormat.heading4
			) { onClick(ToolbarButton.H4) }
			ToolbarButton(
				name = "Heading 5",
				icon = R.drawable.ic_format_h5,
				highlight = textFormat.heading5
			) { onClick(ToolbarButton.H5) }
			ToolbarButton(
				name = "Heading 6",
				icon = R.drawable.ic_format_h6,
				highlight = textFormat.heading6
			) { onClick(ToolbarButton.H6) }

			Spacer(modifier = Modifier.width(8.dp))
		}
	}
}

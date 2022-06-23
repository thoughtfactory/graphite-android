package com.syncodec.graphite.noteComponent.miscellaneous

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.custom.richText.RichTextEditor
import com.syncodec.graphite.miscellaneous.LocalRichTextEditor
import com.syncodec.graphite.miscellaneous.ThemeUtils.Companion.tone
import com.syncodec.graphite.noteComponent.NoteActivity
import com.syncodec.graphite.noteComponent.toolbar.ToolbarButton
import com.syncodec.graphite.ui.theme.PremiumCompositionLocal
import java.text.SimpleDateFormat


private enum class ToolbarState {
	NONE,
	STATE,
	FORMAT,
	HEADING,
	LINK
}

@Composable
fun BottomBar(
	isViewer: Boolean,
	isSaving: Boolean,
	userTimestamp: Long,
	isFavourite: Boolean,
	isArchive: Boolean,
	isLocked: Boolean,
	onAction: (NoteActivity.Action, Any?) -> Unit
) {
	val context = LocalContext.current
	val richTextEditor = LocalRichTextEditor.current

	var textFormat by remember { mutableStateOf(RichTextEditor.TextFormat()) }

	richTextEditor.setOnFormatUpdate(object : RichTextEditor.OnFormatUpdateListener {
		override fun onFormatUpdate(newTextFormat: RichTextEditor.TextFormat) {
			textFormat = newTextFormat
		}
	})

	var toolbarState by remember { mutableStateOf(ToolbarState.NONE) }

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1))
	) {
		EditorToolbar(
			richTextEditor = richTextEditor,
			textFormat = textFormat,
			toolbarState = toolbarState,
			isFavourite = isFavourite,
			isArchive = isArchive,
			isLocked = isLocked,
			onClickCloseToolbar = { toolbarState = ToolbarState.NONE },
			onClickFormatToolbar = { toolbarState = ToolbarState.FORMAT },
			onClickHeadingToolbar = { toolbarState = ToolbarState.HEADING },
			onAction = onAction
		)
		Row(
			Modifier
				.fillMaxWidth()
				.height(80.dp)
				.padding(4.dp, 0.dp),
			horizontalArrangement = Arrangement.Start,
			verticalAlignment = Alignment.CenterVertically,
		) {
			Crossfade(
				targetState = isViewer,
				modifier = Modifier.weight(1f)
			) {
				if (it) ViewerButtons(onAction = onAction)
				else EditorButtons(
					userTimestamp = userTimestamp,
					onClickUserTimestamp = { onAction(NoteActivity.Action.SELECT_TIME, null) },
					onClickAttachment = { onAction(NoteActivity.Action.ATTACHMENT_BUTTON, null) },
					onClickTag = { onAction(NoteActivity.Action.TAG_BUTTON, null) },
					onClickState = { toolbarState = ToolbarState.STATE },
					onClickFormat = { toolbarState = ToolbarState.FORMAT }
				)
			}
			Spacer(modifier = Modifier.width(16.dp))
			FloatingActionButton(
				onClick = {
					if (isSaving) {
						Toast.makeText(
							context,
							"Please wait while data is being saved",
							Toast.LENGTH_SHORT
						).show()
					} else {
						if (isViewer) onAction(NoteActivity.Action.EDIT_NOTE, null)
						else onAction(NoteActivity.Action.SAVE_NOTE, null)
					}
				},
				elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
			) {
				Crossfade(
					targetState = isViewer,
					animationSpec = tween(300)
				) {
					if (it) {
						Icon(
							painter = painterResource(id = R.drawable.ic_pencil),
							contentDescription = null,
							modifier = Modifier
						)
					} else {
						Icon(
							painter = painterResource(id = R.drawable.ic_check),
							contentDescription = null,
							modifier = Modifier
						)
					}
				}
			}
			Spacer(modifier = Modifier.width(4.dp))
		}
	}
}

@Composable
private fun ViewerButtons(
	onAction: (NoteActivity.Action, Any?) -> Unit
) {
	LazyRow(modifier = Modifier) {
		item { Spacer(modifier = Modifier.width(4.dp)) }
		item {
			IconButton(onClick = { onAction(NoteActivity.Action.COPY, null) }) {
				Icon(
					painter = painterResource(id = R.drawable.ic_copy),
					contentDescription = "Copy note"
				)
			}
		}
		item {
			IconButton(onClick = { onAction(NoteActivity.Action.EXPORT, null) }) {
				Icon(
					painter = painterResource(id = R.drawable.ic_export),
					contentDescription = "Export note"
				)
			}
		}
		item {
			IconButton(onClick = { onAction(NoteActivity.Action.PRINT, null) }) {
				Icon(
					painter = painterResource(id = R.drawable.ic_printer),
					contentDescription = "Print note"
				)
			}
		}
	}
}

@Composable
private fun EditorButtons(
	userTimestamp: Long,
	onClickUserTimestamp: () -> Unit,
	onClickAttachment: () -> Unit,
	onClickTag: () -> Unit,
	onClickState: () -> Unit,
	onClickFormat: () -> Unit,
) {
	LazyRow(
		modifier = Modifier,
		verticalAlignment = Alignment.CenterVertically
	) {
		item { Spacer(modifier = Modifier.width(4.dp)) }
		item {
			DateTimeButton(userTimestamp = userTimestamp, onClick = onClickUserTimestamp)
		}
		item { ToolbarSpacer() }
		item {
			IconButton(onClick = onClickAttachment) {
				Icon(
					painter = painterResource(id = R.drawable.ic_attachment),
					contentDescription = "Attachment"
				)
			}
		}
		item {
			IconButton(onClick = onClickTag) {
				Icon(
					painter = painterResource(id = R.drawable.ic_hashtag),
					contentDescription = "Tag"
				)
			}
		}
		item {
			IconButton(onClick = onClickState) {
				Icon(
					painter = painterResource(id = R.drawable.ic_state),
					contentDescription = "State"
				)
			}
		}
		item {
			IconButton(onClick = onClickFormat) {
				Icon(
					painter = painterResource(id = R.drawable.ic_text_format),
					contentDescription = "Text Format"
				)
			}
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
					color = contentColorFor(
						MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)
					)
				)
				Text(
					text = SimpleDateFormat("MMM d, yyyy").format(it),
					style = MaterialTheme.typography.bodySmall,
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun EditorToolbar(
	richTextEditor: RichTextEditor,
	textFormat: RichTextEditor.TextFormat,
	toolbarState: ToolbarState,
	isFavourite: Boolean,
	isArchive: Boolean,
	isLocked: Boolean,
	onClickCloseToolbar: () -> Unit,
	onClickFormatToolbar: () -> Unit,
	onClickHeadingToolbar: () -> Unit,
	onAction: (NoteActivity.Action, Any?) -> Unit
) {
	AnimatedContent(targetState = toolbarState) {
		Column(
			modifier = Modifier.fillMaxWidth()
		) {
			Spacer(modifier = Modifier.height(4.dp))
			when (it) {
				ToolbarState.NONE -> null
				ToolbarState.STATE -> StateToolbar(
					isFavourite = isFavourite,
					isArchive = isArchive,
					isLocked = isLocked,
					onClickCloseToolbar = onClickCloseToolbar,
					onAction = onAction
				)
				ToolbarState.FORMAT -> FormatToolbar(
					richTextEditor = richTextEditor,
					textFormat = textFormat,
					onClickCloseToolbar = onClickCloseToolbar,
					onClickHeadingToolbar = onClickHeadingToolbar
				)
				ToolbarState.HEADING -> HeadingToolbar(
					richTextEditor = richTextEditor,
					textFormat = textFormat,
					onClickFormatToolbar = onClickFormatToolbar,
				)
				ToolbarState.LINK -> null
			}
		}
	}
}

@Composable
private fun StateToolbar(
	isFavourite: Boolean,
	isArchive: Boolean,
	isLocked: Boolean,
	onClickCloseToolbar: () -> Unit,
	onAction: (NoteActivity.Action, Any?) -> Unit
) {
	Box(
		modifier = Modifier.fillMaxWidth()
	) {
		LazyRow(
			modifier = Modifier
		) {
			item { Spacer(modifier = Modifier.width(4.dp)) }

			item {
				ToolbarButton(
					name = "Close toolbar",
					icon = R.drawable.ic_chevron_down,
					highlight = false,
					isEnabled = true,
					onClick = onClickCloseToolbar
				)
			}

			item { ToolbarSpacer() }

			item {
				ToolbarButton(
					name = "Favourite",
					icon = R.drawable.ic_favourite,
					highlight = isFavourite,
					isEnabled = true
				) { onAction(NoteActivity.Action.TOGGLE_FAVOURITE, null) }
			}

			item {
				ToolbarButton(
					name = "Archive",
					icon = R.drawable.ic_archive,
					highlight = isArchive,
					isEnabled = true
				) { onAction(NoteActivity.Action.TOGGLE_ARCHIVE, null) }
			}

			item {
				ToolbarButton(
					name = "Lock",
					icon = if (isLocked) R.drawable.ic_lock_close else R.drawable.ic_lock_open,
					highlight = isLocked,
					isEnabled = true
				) { onAction(NoteActivity.Action.TOGGLE_LOCKED, null) }
			}
		}
	}
}

@Composable
private fun FormatToolbar(
	richTextEditor: RichTextEditor,
	textFormat: RichTextEditor.TextFormat,
	onClickCloseToolbar: () -> Unit,
	onClickHeadingToolbar: () -> Unit,
) {
	val isPremium = PremiumCompositionLocal.current

	Box(
		modifier = Modifier.fillMaxWidth()
	) {
		LazyRow(
			modifier = Modifier
		) {
			item { Spacer(modifier = Modifier.width(4.dp)) }

			item {
				ToolbarButton(
					name = "Close formatter",
					icon = R.drawable.ic_chevron_down,
					highlight = false,
					isEnabled = true,
					onClick = onClickCloseToolbar
				)
			}
			item {
				ToolbarButton(
					name = "Undo",
					icon = R.drawable.ic_format_undo,
					highlight = false,
					isEnabled = true
				) { richTextEditor.exec("editor.commands.undo();") }
			}
			item {
				ToolbarButton(
					name = "Redo",
					icon = R.drawable.ic_format_redo,
					highlight = false,
					isEnabled = true
				) { richTextEditor.exec("editor.commands.redo();") }
			}

			item { ToolbarSpacer() }

			item {
				ToolbarButton(
					name = "Format bold",
					icon = R.drawable.ic_format_bold,
					highlight = textFormat.bold,
					isEnabled = true
				) { richTextEditor.exec("editor.chain().focus().toggleBold().run()") }
			}
			item {
				ToolbarButton(
					name = "Format italic",
					icon = R.drawable.ic_format_italic,
					highlight = textFormat.italic,
					isEnabled = true
				) { richTextEditor.exec("editor.chain().focus().toggleItalic().run()") }
			}
			item {
				ToolbarButton(
					name = "Format underline",
					icon = R.drawable.ic_format_underline,
					highlight = textFormat.underline,
					isEnabled = isPremium
				) { richTextEditor.exec("editor.chain().focus().toggleUnderline().run()") }
			}
			item {
				ToolbarButton(
					name = "Format strikethrough",
					icon = R.drawable.ic_format_strikethrough,
					highlight = textFormat.strike,
					isEnabled = isPremium
				) { richTextEditor.exec("editor.chain().focus().toggleStrike().run();") }
			}
			item {
				ToolbarButton(
					name = "Format hard break",
					icon = R.drawable.ic_format_hard_break,
					highlight = false,
					isEnabled = true
				) { richTextEditor.exec("editor.chain().focus().setHardBreak().run()") }
			}

			item { ToolbarSpacer() }
			
			item {
				ToolbarButton(
					name = "Check list",
					icon = R.drawable.ic_format_list_check,
					highlight = textFormat.taskList,
					isEnabled = isPremium
				) { richTextEditor.exec("editor.commands.toggleTaskList();") }
			}
			item {
				ToolbarButton(
					name = "Bullet list",
					icon = R.drawable.ic_format_list_bullet,
					highlight = textFormat.bulletList,
					isEnabled = isPremium
				) {richTextEditor.exec("editor.commands.toggleBulletList();")  }
			}
			item {
				ToolbarButton(
					name = "Ordered list",
					icon = R.drawable.ic_format_list_ordered,
					highlight = textFormat.orderedList,
					isEnabled = isPremium
				) { richTextEditor.exec("editor.commands.toggleOrderedList();") }
			}

			item { ToolbarSpacer() }

			item {
				ToolbarButton(
					name = "Format heading",
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
					highlight = textFormat.heading1 || textFormat.heading2 || textFormat.heading3 || textFormat.heading4 || textFormat.heading5 || textFormat.heading6,
					isEnabled = isPremium
				) { onClickHeadingToolbar() }
			}
			item {
				ToolbarButton(
					name = "Format blockquote",
					icon = R.drawable.ic_format_blockquote,
					highlight = textFormat.blockquote,
					isEnabled = isPremium
				) { richTextEditor.exec("editor.chain().focus().toggleBlockquote().run();") }
			}

			item { ToolbarSpacer() }

			item {
				ToolbarButton(
					name = "Format indent",
					icon = R.drawable.ic_format_indent,
					highlight = false,
					isEnabled = isPremium
				) { richTextEditor.exec("editor.chain().focus().sinkListItem('listItem').run()") }
			}
			item {
				ToolbarButton(
					name = "Format outdent",
					icon = R.drawable.ic_format_outdent,
					highlight = false,
					isEnabled = isPremium
				) { richTextEditor.exec("editor.chain().focus().liftListItem('listItem').run()") }
			}

			item { ToolbarSpacer() }

			item {
				ToolbarButton(
					name = "Format superscript",
					icon = R.drawable.ic_format_superscript,
					highlight = textFormat.superscript,
					isEnabled = isPremium
				) { richTextEditor.exec("editor.chain().focus().toggleSuperscript().run();") }
			}
			item {
				ToolbarButton(
					name = "Format subscript",
					icon = R.drawable.ic_format_subscript,
					highlight = textFormat.subscript,
					isEnabled = isPremium
				) { richTextEditor.exec("editor.chain().focus().toggleSubscript().run();") }
			}

			item { Spacer(modifier = Modifier.width(4.dp)) }
		}
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
		LazyRow(
			modifier = Modifier
		) {
			item { Spacer(modifier = Modifier.width(4.dp)) }

			item {
				ToolbarButton(
					name = "Format toolbar",
					icon = R.drawable.ic_chevron_left,
					highlight = false,
					isEnabled = true,
					onClick = onClickFormatToolbar
				)
			}

			item { ToolbarSpacer() }

			item {
				ToolbarButton(
					name = "Format paragraph",
					icon = R.drawable.ic_format_paragraph,
					highlight = textFormat.paragraph,
					isEnabled = true
				) { richTextEditor.exec("editor.commands.toggleHeading({ level: 3 });") }
			}
			item {
				ToolbarButton(
					name = "Format heading 1",
					icon = R.drawable.ic_format_h1,
					highlight = textFormat.heading1,
					isEnabled = true
				) { richTextEditor.exec("editor.commands.toggleHeading({ level: 1 });") }
			}
			item {
				ToolbarButton(
					name = "Format heading 2",
					icon = R.drawable.ic_format_h2,
					highlight = textFormat.heading2,
					isEnabled = true
				) { richTextEditor.exec("editor.commands.toggleHeading({ level: 2 });") }
			}
			item {
				ToolbarButton(
					name = "Format heading 3",
					icon = R.drawable.ic_format_h3,
					highlight = textFormat.heading3,
					isEnabled = true
				) { richTextEditor.exec("editor.commands.toggleHeading({ level: 3 });") }
			}
			item {
				ToolbarButton(
					name = "Format heading 4",
					icon = R.drawable.ic_format_h4,
					highlight = textFormat.heading4,
					isEnabled = true
				) { richTextEditor.exec("editor.commands.toggleHeading({ level: 4 });") }
			}
			item {
				ToolbarButton(
					name = "Format heading 5",
					icon = R.drawable.ic_format_h5,
					highlight = textFormat.heading5,
					isEnabled = true
				) { richTextEditor.exec("editor.commands.toggleHeading({ level: 5 });") }
			}
			item {
				ToolbarButton(
					name = "Format heading 6",
					icon = R.drawable.ic_format_h6,
					highlight = textFormat.heading6,
					isEnabled = true
				) { richTextEditor.exec("editor.commands.toggleHeading({ level: 6 });") }
			}
		}
	}
}

@Composable
private fun ToolbarSpacer() {
	Row(
		modifier = Modifier.height(48.dp),
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

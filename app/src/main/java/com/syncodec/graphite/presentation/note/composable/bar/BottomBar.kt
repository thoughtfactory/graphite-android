package com.syncodec.graphite.presentation.note.composable.bar

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.custom.button.MenuButton
import com.syncodec.graphite.presentation.custom.richText.RichTextEditor
import com.syncodec.graphite.presentation.note.NoteViewModel
import com.syncodec.graphite.utils.LocalCompositionPremium
import com.syncodec.graphite.utils.LocalRichTextEditor
import java.text.SimpleDateFormat


private enum class ToolbarState {
	NONE,
	FORMAT,
	HEADING,
	LINK
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BottomBar(
	onClickAttachment: () -> Unit
) {
	val context = LocalContext.current
	val viewModel: NoteViewModel = viewModel()

	val richTextEditor = LocalRichTextEditor.current

	val isViewer by viewModel.isViewer
	val isSaving by viewModel.isSaving
	val userTimestamp by viewModel.userTimestamp

	var textFormat by remember { mutableStateOf(RichTextEditor.TextFormat()) }

	richTextEditor.setOnFormatUpdate(object : RichTextEditor.OnFormatUpdateListener {
		override fun onFormatUpdate(newTextFormat: RichTextEditor.TextFormat) {
			textFormat = newTextFormat
		}
	})

	var toolbarState by remember { mutableStateOf(ToolbarState.NONE) }

	LaunchedEffect(key1 = isViewer) {
		if (isViewer) toolbarState = ToolbarState.NONE
	}

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface)
	) {
		EditorToolbar(
			richTextEditor = richTextEditor,
			textFormat = textFormat,
			toolbarState = toolbarState,
			onClickCloseToolbar = { toolbarState = ToolbarState.NONE },
			onClickFormatToolbar = { toolbarState = ToolbarState.FORMAT },
		) { toolbarState = ToolbarState.HEADING }
		Row(
			horizontalArrangement = Arrangement.Start,
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.height(80.dp),
		) {
			AnimatedContent(
				targetState = isViewer,
				transitionSpec = { slideIntoContainer(AnimatedContentScope.SlideDirection.Start, tween(300)) with slideOutOfContainer(AnimatedContentScope.SlideDirection.Start, tween(300)) },
				modifier = Modifier.weight(1f)
			) {
				if (it) {
					ViewerButtons(
						onCopy = {},
						onExport = {},
						onPrint = {},
						onDelete = { viewModel.showDeleteDialog.value = true }
					)
				} else {
					EditorButtons(
						userTimestamp = userTimestamp ?: 0,
						onClickUserTimestamp = { },
						onClickAttachment = onClickAttachment,
						onClickTag = { },
						onClickFormat = { toolbarState = ToolbarState.FORMAT }
					)

				}
			}
			Spacer(modifier = Modifier.width(16.dp))
			FloatingActionButton(
				onClick = {
					when {
						isSaving -> Toast.makeText(context, "Please wait while data is being saved", Toast.LENGTH_SHORT).show()
						isViewer && richTextEditor.isReady.value && viewModel.content.value == null -> Toast.makeText(
							context,
							"Please wait while editor is being loaded",
							Toast.LENGTH_SHORT
						).show()
						isViewer && richTextEditor.isReady.value -> {
							richTextEditor.exec("editor.commands.setContent(${viewModel.content.value});")
							viewModel.editNote()
						}
						isViewer && !richTextEditor.isReady.value -> Toast.makeText(context, "Please wait while editor is being loaded", Toast.LENGTH_SHORT)
							.show()
						richTextEditor.isReady.value -> richTextEditor.exec("editor.getData();")
						else -> Toast.makeText(context, "Please wait while editor is being loaded", Toast.LENGTH_SHORT).show()
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
							contentDescription = "Edit note",
							modifier = Modifier
						)
					} else {
						Icon(
							painter = painterResource(id = R.drawable.ic_check),
							contentDescription = "Save note",
							modifier = Modifier
						)
					}
				}
			}
			Spacer(modifier = Modifier.width(16.dp))
		}
	}
}

@Composable
private fun ViewerButtons(
	onCopy: () -> Unit,
	onExport: () -> Unit,
	onPrint: () -> Unit,
	onDelete: () -> Unit
) {
	LazyRow(modifier = Modifier) {
		item { Spacer(modifier = Modifier.width(16.dp)) }
		item {
			MenuButton(
				icon = R.drawable.ic_copy,
				contentDescription = "Copy Note",
				onClick = onCopy
			)
		}
		item {
			MenuButton(
				icon = R.drawable.ic_export,
				contentDescription = "Export Note",
				onClick = onExport
			)
		}
		item {
			MenuButton(
				icon = R.drawable.ic_printer,
				contentDescription = "Print Note",
				onClick = onPrint
			)
		}
		item {
			MenuButton(
				icon = R.drawable.ic_delete,
				contentDescription = "Delete",
				tint = Color(0xFFF05945),
				onClick = onDelete
			)
		}
	}
}

@Composable
private fun EditorButtons(
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
				tint = MaterialTheme.colorScheme.onSurface,
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
					color = MaterialTheme.colorScheme.onSurface
				)
				Text(
					text = SimpleDateFormat("MMM d, yyyy").format(it),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface
				)
			}
		}
		Spacer(modifier = Modifier.width(8.dp))
	}
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun EditorToolbar(
	richTextEditor: RichTextEditor,
	textFormat: RichTextEditor.TextFormat,
	toolbarState: ToolbarState,
	onClickCloseToolbar: () -> Unit,
	onClickFormatToolbar: () -> Unit,
	onClickHeadingToolbar: () -> Unit,
) {
	AnimatedContent(targetState = toolbarState) {
		Column(
			modifier = Modifier.fillMaxWidth()
		) {
			Spacer(modifier = Modifier.height(4.dp))
			when (it) {
				ToolbarState.NONE -> null
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
private fun FormatToolbar(
	richTextEditor: RichTextEditor,
	textFormat: RichTextEditor.TextFormat,
	onClickCloseToolbar: () -> Unit,
	onClickHeadingToolbar: () -> Unit,
) {
	val isPremium = LocalCompositionPremium.current

	Box(
		modifier = Modifier.fillMaxWidth()
	) {
		LazyRow(
			modifier = Modifier
		) {
			item { Spacer(modifier = Modifier.width(4.dp)) }

			item {
				MenuButton(
					icon = R.drawable.ic_chevron_down,
					contentDescription = "Close formatter",
					onClick = onClickCloseToolbar
				)
			}
			item {
				MenuButton(
					icon = R.drawable.ic_format_undo,
					contentDescription = "Undo",
					onClick = { richTextEditor.exec("editor.commands.undo();") }
				)
			}
			item {
				MenuButton(
					icon = R.drawable.ic_format_redo,
					contentDescription = "Redo",
					onClick = { richTextEditor.exec("editor.commands.redo();") }
				)
			}

			item { ToolbarSpacer() }

			item {
				MenuButton(
					icon = R.drawable.ic_format_bold,
					contentDescription = "Bold",
					isChecked = textFormat.bold,
					onClick = { richTextEditor.exec("editor.chain().focus().toggleBold().run()") }
				)
			}
			item {
				MenuButton(
					icon = R.drawable.ic_format_italic,
					contentDescription = "Italic",
					isChecked = textFormat.italic,
					onClick = { richTextEditor.exec("editor.chain().focus().toggleItalic().run()") }
				)
			}
			item {
				MenuButton(
					icon = R.drawable.ic_format_underline,
					contentDescription = "Underline",
					isChecked = textFormat.underline,
					onClick = { richTextEditor.exec("editor.chain().focus().toggleUnderline().run()") }
				)
			}
			item {
				MenuButton(
					icon = R.drawable.ic_format_strikethrough,
					contentDescription = "Strikethrough",
					isChecked = textFormat.strike,
					onClick = { richTextEditor.exec("editor.chain().focus().toggleStrike().run()") }
				)
			}
			item {
				MenuButton(
					icon = R.drawable.ic_format_hard_break,
					contentDescription = "Format hard break",
					onClick = { richTextEditor.exec("editor.chain().focus().setHardBreak().run()") }
				)
			}

			item { ToolbarSpacer() }

			item {
				MenuButton(
					icon = R.drawable.ic_format_list_check,
					contentDescription = "Check list",
					isChecked = textFormat.taskList,
					onClick = { richTextEditor.exec("editor.commands.toggleTaskList();") }
				)
			}
			item {
				MenuButton(
					icon = R.drawable.ic_format_list_bullet,
					contentDescription = "Bullet list",
					isChecked = textFormat.bulletList,
					onClick = { richTextEditor.exec("editor.commands.toggleBulletList();") }
				)
			}
			item {
				MenuButton(
					icon = R.drawable.ic_format_list_ordered,
					contentDescription = "Ordered list",
					isChecked = textFormat.orderedList,
					onClick = { richTextEditor.exec("editor.commands.toggleOrderedList();") }
				)
			}

			item { ToolbarSpacer() }

			item {
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
					isChecked = textFormat.heading1 || textFormat.heading2 || textFormat.heading3 || textFormat.heading4 || textFormat.heading5 || textFormat.heading6,
					onClick = onClickHeadingToolbar
				)
			}
			item {
				MenuButton(
					icon = R.drawable.ic_format_blockquote,
					contentDescription = "Format blockquote",
					isChecked = textFormat.blockquote,
					onClick = { richTextEditor.exec("editor.chain().focus().toggleBlockquote().run();") }
				)
			}

			item { ToolbarSpacer() }

			item {
				MenuButton(
					icon = R.drawable.ic_format_indent,
					contentDescription = "Format indent",
					onClick = { richTextEditor.exec("editor.chain().focus().sinkListItem('listItem').run()") }
				)
			}
			item {
				MenuButton(
					icon = R.drawable.ic_format_outdent,
					contentDescription = "Format outdent",
					onClick = { richTextEditor.exec("editor.chain().focus().liftListItem('listItem').run()") }
				)
			}

			item { ToolbarSpacer() }

			item {
				MenuButton(
					icon = R.drawable.ic_format_superscript,
					contentDescription = "Format superscript",
					isChecked = textFormat.superscript,
					onClick = { richTextEditor.exec("editor.chain().focus().toggleSuperscript().run();") }
				)
			}
			item {
				MenuButton(
					icon = R.drawable.ic_format_subscript,
					contentDescription = "Format subscript",
					isChecked = textFormat.subscript,
					onClick = { richTextEditor.exec("editor.chain().focus().toggleSubscript().run();") }
				)
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
				MenuButton(
					icon = R.drawable.ic_chevron_down,
					contentDescription = "Format toolbar",
					onClick = onClickFormatToolbar
				)
			}

			item { ToolbarSpacer() }

			item {
				MenuButton(
					icon = R.drawable.ic_format_paragraph,
					contentDescription = "Format paragraph",
					isChecked = textFormat.paragraph,
					onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 3 });") }
				)
			}
			item {
				MenuButton(
					icon = R.drawable.ic_format_h1,
					contentDescription = "Format heading 1",
					isChecked = textFormat.heading1,
					onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 1 });") }
				)
			}
			item {
				MenuButton(
					icon = R.drawable.ic_format_h2,
					contentDescription = "Format heading 2",
					isChecked = textFormat.heading2,
					onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 2 });") }
				)
			}
			item {
				MenuButton(
					icon = R.drawable.ic_format_h3,
					contentDescription = "Format heading 3",
					isChecked = textFormat.heading3,
					onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 3 });") }
				)
			}
			item {
				MenuButton(
					icon = R.drawable.ic_format_h4,
					contentDescription = "Format heading 4",
					isChecked = textFormat.heading4,
					onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 4 });") }
				)
			}
			item {
				MenuButton(
					icon = R.drawable.ic_format_h5,
					contentDescription = "Format heading 5",
					isChecked = textFormat.heading5,
					onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 5 });") }
				)
			}
			item {
				MenuButton(
					icon = R.drawable.ic_format_h6,
					contentDescription = "Format heading 6",
					isChecked = textFormat.heading6,
					onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 6 });") }
				)
			}
		}
	}
}

@Composable
private fun ToolbarSpacer() {
	Row(
		modifier = Modifier.height(40.dp),
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

//@Composable
//fun ToolbarButton(
//	name: String,
//	icon: Int,
//	highlight: Boolean,
//	isEnabled: Boolean,
//	onClick: () -> Unit
//) {
//	val context = LocalContext.current
//	val containerColor by animateColorAsState(
//		if (highlight) MaterialTheme.colorScheme.onSurface
//		else Color.Transparent
//	)
//	val contentColor by animateColorAsState(
//		if (highlight) MaterialTheme.colorScheme.surface
//		else MaterialTheme.colorScheme.onSurface
//	)
//	Box(
//		modifier = Modifier
//			.requiredSize(40.dp)
//			.padding(2.dp)
//			.clip(RoundedCornerShape(25, 25, if (isEnabled) (25) else 0, 25))
//			.background(containerColor)
//			.clickable {
//				if (isEnabled) {
//					onClick()
//				} else {
//					Toast
//						.makeText(
//							context,
//							"Subscribe to Graphite Premium to unlock rich text",
//							Toast.LENGTH_SHORT
//						)
//						.show()
//				}
//			},
//		contentAlignment = Alignment.Center
//	) {
//		Icon(
//			painter = painterResource(id = icon),
//			contentDescription = name,
//			tint = contentColor,
//			modifier = Modifier.requiredSize(24.dp)
//		)
//
//		if (!isEnabled) {
//			Box(
//				contentAlignment = Alignment.BottomEnd,
//				modifier = Modifier.requiredSize(40.dp)
//			) {
//				Icon(
//					painter = painterResource(id = R.drawable.ic_premium),
//					contentDescription = name,
//					tint = Color.Unspecified,
//					modifier = Modifier
//						.requiredSize(16.dp)
//						.padding(2.dp, 2.dp, 0.dp, 0.dp)
//				)
//			}
//		}
//	}
//}

package com.syncodec.momento.noteComponent.toolbar

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.R
import com.syncodec.momento.custom.richText.RichTextEditor
import com.syncodec.momento.konstant.Color.Companion.colorList
import com.syncodec.momento.konstant.ErrorCode
import com.syncodec.momento.miscellaneous.toHexString
import com.syncodec.momento.noteComponent.NoteViewModel
import java.text.SimpleDateFormat

private enum class ToolbarState {
	BASE,
	STATE,
	TAG,
	ALIGN,
	HEADING,

	//	TEXT_HIGHLIGHT,
//	TEXT_COLOR,
	LINK
}

private enum class NoteState {
	ARCHIVE,
	FAVOURITE,
	LOCKED
}

private enum class ToolbarButton {
	TIMESTAMP_PICKER,
	STATE,
	OPEN_FORMAT,
	CLOSE_FORMAT,
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
	ALIGN,
	ALIGN_LEFT,
	ALIGN_CENTER,
	ALIGN_RIGHT,
	ALIGN_JUSTIFY,
	TEXT_HIGHLIGHT,
	TEXT_COLOR,
	INDENT,
	OUTDENT,
	LINK,
	SUPERSCRIPT,
	SUBSCRIPT,
	CODE,
	CODE_BLOCK,
}

@OptIn(ExperimentalMaterialApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun EditorToolbar(
	richTextEditor: RichTextEditor,
	onError: (ErrorCode.Companion.ErrorCode) -> Unit,
) {
	var showFormatter by remember { mutableStateOf(false) }
	var toolbarState by remember { mutableStateOf(ToolbarState.BASE) }

	var textFormat by remember { mutableStateOf(RichTextEditor.TextFormat()) }

	var highlightColor by remember { mutableStateOf<Color?>(null) }
	var textColor by remember { mutableStateOf<Color?>(null) }

	richTextEditor.setOnFormatUpdate(object : RichTextEditor.OnFormatUpdateListener {
		override fun onFormatUpdate(newTextFormat: RichTextEditor.TextFormat) {
			textFormat = newTextFormat
		}
	})

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.secondaryContainer)
	) {
		AnimatedContent(
			targetState = toolbarState
		) {
			when (it) {
				ToolbarState.BASE -> null
				ToolbarState.STATE -> NoteStateToolbar()
				ToolbarState.TAG -> null
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
				ToolbarState.ALIGN -> TextAlignToolbar(
					textFormat = textFormat
				) { toolbarButton ->
					when (toolbarButton) {
						ToolbarButton.ALIGN_LEFT ->
							if (textFormat.alignLeft) richTextEditor.exec("editor.commands.unsetTextAlign();") else richTextEditor.exec("editor.commands.setTextAlign('left');")
						ToolbarButton.ALIGN_CENTER ->
							if (textFormat.alignCenter) richTextEditor.exec("editor.commands.unsetTextAlign();") else richTextEditor.exec("editor.commands.setTextAlign('center');")
						ToolbarButton.ALIGN_RIGHT ->
							if (textFormat.alignRight) richTextEditor.exec("editor.commands.unsetTextAlign();") else richTextEditor.exec("editor.commands.setTextAlign('right');")
						ToolbarButton.ALIGN_JUSTIFY ->
							if (textFormat.alignJustify) richTextEditor.exec("editor.commands.unsetTextAlign();") else richTextEditor.exec("editor.commands.setTextAlign('justify');")
					}
				}
//				ToolbarState.TEXT_HIGHLIGHT -> ColorToolbar(textFormat = textFormat) { color -> richTextEditor.exec("editor.commands.setColor('${color.toHexString()}');") }
//				ToolbarState.TEXT_COLOR -> ColorToolbar(textFormat = textFormat) {
//					Log.i("npr71", "color : ${it.toHexString()}")
//				}
			}
		}

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(56.dp)
				.background(MaterialTheme.colorScheme.secondaryContainer)
		) {
			AnimatedContent(
				targetState = showFormatter,
				transitionSpec = {
					(slideInHorizontally(tween(600)) { width -> -width } + fadeIn() with slideOutHorizontally(tween(600)) { width -> width } + fadeOut())
						.using(SizeTransform(clip = false))
				}
			) {
				if (it) {
					FormatEditorToolbar(
						textFormat = textFormat,
						highlightColor = highlightColor,
						textColor = textColor,
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
							ToolbarButton.HARD_BREAK -> richTextEditor.exec("editor.chain().focus().setHardBreak().run()", false)
							ToolbarButton.CHECK_LIST -> richTextEditor.exec("editor.commands.toggleTaskList();")
							ToolbarButton.BULLET_LIST -> richTextEditor.exec("editor.commands.toggleBulletList();")
							ToolbarButton.ORDERED_LIST -> richTextEditor.exec("editor.commands.toggleOrderedList();")
							ToolbarButton.BLOCKQUOTE -> richTextEditor.exec("editor.chain().focus().toggleBlockquote().run();")
							ToolbarButton.HEADING -> toolbarState = if (toolbarState == ToolbarState.HEADING) ToolbarState.BASE else ToolbarState.HEADING
							ToolbarButton.ALIGN -> toolbarState = if (toolbarState == ToolbarState.ALIGN) ToolbarState.BASE else ToolbarState.ALIGN
//							ToolbarButton.TEXT_HIGHLIGHT -> toolbarState =
//								if (toolbarState == ToolbarState.TEXT_HIGHLIGHT) ToolbarState.BASE else ToolbarState.TEXT_HIGHLIGHT
//							ToolbarButton.TEXT_COLOR -> toolbarState =
//								if (toolbarState == ToolbarState.TEXT_COLOR) ToolbarState.BASE else ToolbarState.TEXT_COLOR
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
					StateEditorToolbar { toolbarButton ->
						when (toolbarButton) {
							ToolbarButton.STATE -> toolbarState = if (toolbarState == ToolbarState.STATE) ToolbarState.BASE else ToolbarState.STATE
							ToolbarButton.OPEN_FORMAT -> {
								toolbarState = ToolbarState.BASE
								showFormatter = true
							}
						}
					}
				}
			}
		}
	}
}

@Composable
private fun StateEditorToolbar(
	onClick: (ToolbarButton) -> Unit,
) {
	val viewModel: NoteViewModel = viewModel()

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(56.dp)
			.background(MaterialTheme.colorScheme.secondaryContainer)
			.horizontalScroll(rememberScrollState()),
		verticalAlignment = Alignment.CenterVertically
	) {
		Spacer(modifier = Modifier.width(8.dp))
		DateTimeButton(userTimestamp = viewModel.userTimestamp.value) {

		}

		ToolbarSpacer()

		ToolbarButton(
			name = "Attachment",
			icon = R.drawable.ic_attachment,
			highlight = false
		) {}
		ToolbarButton(
			name = "Tag",
			icon = R.drawable.ic_hashtag,
			highlight = false
		) {}
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
	}
}

@Composable
private fun FormatEditorToolbar(
	textFormat: RichTextEditor.TextFormat,
	highlightColor: Color?,
	textColor: Color?,
	onClick: (ToolbarButton) -> Unit,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(56.dp)
			.background(MaterialTheme.colorScheme.secondaryContainer)
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
		ToolbarButton(
			name = "Text alignment",
			icon = when {
				textFormat.alignLeft -> R.drawable.ic_format_align_left
				textFormat.alignCenter -> R.drawable.ic_format_align_center
				textFormat.alignRight -> R.drawable.ic_format_align_right
				textFormat.alignJustify -> R.drawable.ic_format_align_justify
				else -> R.drawable.ic_format_align_left
			},
			highlight = false,
		) { onClick(ToolbarButton.ALIGN) }
//		FontFamilyButton(textFormat = textFormat)
//		FontSizeButton()

		ToolbarSpacer()

//		ToolbarButton(
//			name = "Highlight color",
//			icon = R.drawable.ic_tabler_icon_highlight,
//			highlight = textFormat.orderedList
//		) { onClick(ToolbarButton.TEXT_HIGHLIGHT) }
//		if (highlightColor == null) {
//			ToolbarRemoveColorButton {}
//		} else {
//			ToolbarColorButton(color = highlightColor) {}
//		}
//		ToolbarButton(
//			name = "Text color",
//			icon = R.drawable.ic_format_text_color,
//			highlight = textFormat.orderedList
//		) { onClick(ToolbarButton.TEXT_COLOR) }
//		if (textColor == null) {
//			ToolbarRemoveColorButton {}
//		} else {
//			ToolbarColorButton(color = textColor) {}
//		}
//
//		ToolbarSpacer()

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
		modifier = Modifier
			.height(40.dp)
	) {
		val containerColor by animateColorAsState(targetValue = if (highlight) MaterialTheme.colorScheme.onSecondaryContainer else Color.Companion.Transparent)
		val contentColor by animateColorAsState(targetValue = if (highlight) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.onSecondaryContainer)

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
		modifier = Modifier
			.height(56.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Spacer(modifier = Modifier.width(4.dp))
		Spacer(
			modifier = Modifier
				.width(2.dp)
				.height(24.dp)
				.clip(RoundedCornerShape(50))
				.background(MaterialTheme.colorScheme.onSecondaryContainer)
		)
		Spacer(modifier = Modifier.width(4.dp))
	}
}

@Composable
private fun DateTimeButton(
	userTimestamp: Long,
	onClick: () -> Unit
) {
	Row(
		modifier = Modifier
			.height(40.dp)
	) {
		Box(
			modifier = Modifier
				.height(40.dp)
				.clip(RoundedCornerShape(25))
				.clickable { onClick() },
			contentAlignment = Alignment.Center
		) {
			Column(
				modifier = Modifier
					.fillMaxHeight()
					.padding(8.dp, 0.dp),
				horizontalAlignment = Alignment.Start,
				verticalArrangement = Arrangement.Center
			) {
				Text(
					text = SimpleDateFormat("h:mm a, EEE").format(userTimestamp),
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onSecondaryContainer
				)
				Text(
					text = SimpleDateFormat("MMM d, yyyy").format(userTimestamp),
					style = MaterialTheme.typography.bodySmall,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onSecondaryContainer
				)
			}
		}
		Spacer(modifier = Modifier.width(4.dp))
	}
}

@Composable
private fun FontFamilyButton(
	textFormat: RichTextEditor.TextFormat
) {
	Row(
		modifier = Modifier
			.height(40.dp)
	) {
		Row(
			modifier = Modifier
				.height(40.dp)
				.clip(RoundedCornerShape(25))
				.clickable { },
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_format_font_family),
				contentDescription = "Font family",
				tint = MaterialTheme.colorScheme.onSecondaryContainer,
				modifier = Modifier
					.requiredSize(24.dp)
					.padding(8.dp, 0.dp, 0.dp, 0.dp)
			)
			Spacer(modifier = Modifier.width(4.dp))
			Text(
				text = "Font family",
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.onSecondaryContainer,
				modifier = Modifier
					.padding(0.dp, 0.dp, 8.dp, 0.dp)
			)
		}
		Spacer(modifier = Modifier.width(4.dp))
	}
}

@Composable
private fun FontSizeButton() {
	Row(
		modifier = Modifier,
		horizontalArrangement = Arrangement.Center,
		verticalAlignment = Alignment.CenterVertically
	) {
		Row(
			modifier = Modifier
				.requiredSize(40.dp)
				.clip(RoundedCornerShape(25))
				.background(Color.Companion.Transparent)
				.clickable { },
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Center
		) {
			Text(
				text = "17",
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.onSecondaryContainer,
				modifier = Modifier
			)
			Text(
				text = "pt",
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.onSecondaryContainer,
				modifier = Modifier
			)
		}
		Spacer(modifier = Modifier.width(4.dp))
	}
}

@Composable
private fun NoteStateToolbar() {
	val viewModel: NoteViewModel = viewModel()

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(48.dp)
			.padding(0.dp, 0.dp, 8.dp, 0.dp),
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
				name = "Archive",
				icon = R.drawable.ic_box,
				highlight = viewModel.isArchived
			) { viewModel.isArchived = !viewModel.isArchived }
			ToolbarButton(
				name = "Favourite",
				icon = R.drawable.ic_heart_3,
				highlight = viewModel.isFavourite
			) { viewModel.isFavourite = !viewModel.isFavourite }
			ToolbarButton(
				name = "Lock",
				icon = R.drawable.ic_locked,
				highlight = viewModel.isLocked
			) { viewModel.isLocked = !viewModel.isLocked }

			Spacer(modifier = Modifier.width(8.dp))
		}
	}
}

@Composable
private fun TextAlignToolbar(
	textFormat: RichTextEditor.TextFormat,
	onClick: (ToolbarButton) -> Unit,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(48.dp)
			.padding(0.dp, 0.dp, 8.dp, 0.dp),
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
				name = "Align left",
				icon = R.drawable.ic_format_align_left,
				highlight = textFormat.alignLeft
			) { onClick(ToolbarButton.ALIGN_LEFT) }
			ToolbarButton(
				name = "Align center",
				icon = R.drawable.ic_format_align_center,
				highlight = textFormat.alignCenter
			) { onClick(ToolbarButton.ALIGN_CENTER) }
			ToolbarButton(
				name = "Align right",
				icon = R.drawable.ic_format_align_right,
				highlight = textFormat.alignRight
			) { onClick(ToolbarButton.ALIGN_RIGHT) }
			ToolbarButton(
				name = "Align justify",
				icon = R.drawable.ic_format_align_justify,
				highlight = textFormat.alignJustify
			) { onClick(ToolbarButton.ALIGN_JUSTIFY) }

			Spacer(modifier = Modifier.width(8.dp))
		}

		Text(
			text = when {
				textFormat.alignLeft -> "Align left"
				textFormat.alignCenter -> "Align center"
				textFormat.alignRight -> "Align right"
				textFormat.alignJustify -> "Align justify"
				else -> ""
			},
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onSecondaryContainer,
			maxLines = 1,
			textAlign = TextAlign.Center,
			modifier = Modifier
		)
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
			.padding(0.dp, 0.dp, 8.dp, 0.dp),
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

@Composable
private fun ColorToolbar(
	textFormat: RichTextEditor.TextFormat,
	onClick: (Color) -> Unit,
) {

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(48.dp)
			.padding(0.dp, 0.dp, 8.dp, 0.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Row(
			modifier = Modifier
				.height(32.dp)
				.horizontalScroll(rememberScrollState()),
			verticalAlignment = Alignment.Bottom,
		) {
			Spacer(modifier = Modifier.width(8.dp))

			colorList.forEach { ToolbarColorButton(color = it) { onClick(it) } }

			Spacer(modifier = Modifier.width(4.dp))
		}
	}
}

@Composable
private fun ToolbarColorButton(
	color: Color,
	onClick: () -> Unit
) {
	Row(
		modifier = Modifier
			.height(32.dp)
	) {
		Box(
			modifier = Modifier
				.requiredSize(32.dp)
				.clip(RoundedCornerShape(25))
				.background(color = color)
				.clickable { onClick() },
		)
		Spacer(modifier = Modifier.width(4.dp))
	}
}

@Composable
private fun ToolbarRemoveColorButton(
	onClick: () -> Unit
) {
	Row(
		modifier = Modifier
			.height(40.dp)
	) {
		Box(
			modifier = Modifier
				.requiredSize(40.dp)
				.clip(RoundedCornerShape(25))
				.clickable { onClick() },
			contentAlignment = Alignment.Center
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_format_remove_color),
				contentDescription = "Remove color",
				modifier = Modifier
					.requiredSize(32.dp)
					.padding(4.dp)
			)
		}
		Spacer(modifier = Modifier.width(4.dp))
	}
}

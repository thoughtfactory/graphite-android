package com.syncodec.momento.noteComponent.toolbar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.R
import com.syncodec.momento.custom.richText.RichTextEditor
import com.syncodec.momento.konstant.ErrorCode
import com.syncodec.momento.noteComponent.NoteViewModel
import java.text.SimpleDateFormat

private enum class ToolbarState {
	BASE,
	TAG,
	ALIGN
}

enum class ToolbarButton {
	TIMESTAMP_PICKER,
	OPEN_FORMAT,
	CLOSE_FORMAT,
	UNDO,
	REDO,
	BOLD,
	ITALIC,
	UNDERLINE,
	STRIKE,
	HARD_BREAK
}

@OptIn(ExperimentalMaterialApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun EditorToolbar(
	richTextEditor: RichTextEditor,
	onError: (ErrorCode.Companion.ErrorCode) -> Unit,
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	var showFormatter by remember { mutableStateOf(false) }
	var toolbarState by remember { mutableStateOf(ToolbarState.BASE) }

	var textFormat by remember { mutableStateOf(RichTextEditor.TextFormat()) }

	var boldState by remember { mutableStateOf(false) }
	var italicState by remember { mutableStateOf(false) }
	var underlineState by remember { mutableStateOf(false) }
	var strikeState by remember { mutableStateOf(false) }

	var textAlignLeft by remember { mutableStateOf(false) }
	var textAlignCenter by remember { mutableStateOf(false) }
	var textAlignRight by remember { mutableStateOf(false) }
	var textAlignJustify by remember { mutableStateOf(false) }

	richTextEditor.setOnFormatUpdate(object : RichTextEditor.OnFormatUpdateListener {
		override fun onFormatUpdate(newTextFormat: RichTextEditor.TextFormat) {
			textFormat = newTextFormat

			boldState = textFormat.bold
			italicState = textFormat.italic
			underlineState = textFormat.underline
			strikeState = textFormat.strike

			textAlignLeft = textFormat.textAlignLeft
			textAlignCenter = textFormat.textAlignCenter
			textAlignRight = textFormat.textAlignRight
			textAlignJustify = textFormat.textAlignJustify
		}
	})

	Column(
		modifier = Modifier
			.fillMaxWidth()
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(56.dp)
				.background(MaterialTheme.colorScheme.secondaryContainer)
		) {
			AnimatedContent(
				targetState = toolbarState
			) {
				when (it) {
					ToolbarState.BASE -> null
					ToolbarState.TAG -> null
					ToolbarState.ALIGN -> TextAlignToolbar()
				}
			}
		}

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(56.dp)
				.background(MaterialTheme.colorScheme.secondaryContainer)
		) {
			AnimatedContent(
				targetState = showFormatter
			) {
				if (it) {
					FormatEditorToolbar(
						textFormat = textFormat
					) { toolbarButton ->
						when (toolbarButton) {
							ToolbarButton.CLOSE_FORMAT -> showFormatter = false
							ToolbarButton.UNDO -> richTextEditor.exec("editor.commands.undo();")
							ToolbarButton.REDO -> richTextEditor.exec("editor.commands.redo();")
							ToolbarButton.BOLD -> richTextEditor.exec("editor.commands.toggleBold();")
							ToolbarButton.ITALIC -> richTextEditor.exec("editor.commands.toggleItalic();")
							ToolbarButton.UNDERLINE -> richTextEditor.exec("editor.commands.toggleUnderline();")
							ToolbarButton.STRIKE -> richTextEditor.exec("editor.commands.toggleStrike();")
							ToolbarButton.HARD_BREAK -> richTextEditor.exec("editor.commands.setHardBreak();")
						}
					}
				} else {
					StateEditorToolbar { toolbarButton ->
						when (toolbarButton) {
							ToolbarButton.OPEN_FORMAT -> showFormatter = true
						}
					}
				}
			}
		}
	}
}

@Composable
private fun StateEditorToolbar(
	onClickToolbarButton: (ToolbarButton) -> Unit,
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
		) {}
		ToolbarButton(
			name = "Text Format",
			icon = R.drawable.ic_text_format,
			highlight = false
		) { onClickToolbarButton(ToolbarButton.OPEN_FORMAT) }
	}
}

@Composable
private fun FormatEditorToolbar(
	textFormat: RichTextEditor.TextFormat,
	onClickToolbarButton: (ToolbarButton) -> Unit,
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
			indicateClick = true,
			highlight = false
		)
		{ onClickToolbarButton(ToolbarButton.CLOSE_FORMAT) }
		ToolbarButton(
			name = "Undo",
			icon = R.drawable.ic_format_undo,
			indicateClick = true,
			highlight = false
		) { onClickToolbarButton(ToolbarButton.UNDO) }
		ToolbarButton(
			name = "Redo",
			icon = R.drawable.ic_format_redo,
			indicateClick = true,
			highlight = false
		) { onClickToolbarButton(ToolbarButton.REDO) }

		ToolbarSpacer()

		ToolbarButton(
			name = "Format bold",
			icon = R.drawable.ic_format_bold,
			highlight = textFormat.bold
		) { onClickToolbarButton(ToolbarButton.BOLD) }
		ToolbarButton(
			name = "Format italic",
			icon = R.drawable.ic_format_italic,
			highlight = textFormat.italic
		) { onClickToolbarButton(ToolbarButton.ITALIC) }
		ToolbarButton(
			name = "Format underline",
			icon = R.drawable.ic_format_underline,
			highlight = textFormat.underline
		) { onClickToolbarButton(ToolbarButton.UNDERLINE) }
		ToolbarButton(
			name = "Format strikethrough",
			icon = R.drawable.ic_format_strikethrough,
			highlight = textFormat.strike
		) { onClickToolbarButton(ToolbarButton.STRIKE) }
		ToolbarButton(
			name = "Hard break",
			icon = R.drawable.ic_format_hard_break,
			indicateClick = true,
			highlight = false
		) { onClickToolbarButton(ToolbarButton.HARD_BREAK) }

		ToolbarSpacer()

		ToolbarButton(
			name = "Check list",
			icon = R.drawable.ic_format_list_task, highlight = false
		) { }
		ToolbarButton(
			name = "Bullet list",
			icon = R.drawable.ic_format_list_bullet, highlight = false
		) { }
		ToolbarButton(
			name = "Ordered list",
			icon = R.drawable.ic_format_list_ordered, highlight = false
		) { }

		ToolbarSpacer()

//		ToolbarButton(
//			name = "Text alignment",
//			icon = when {
//				textAlignLeft -> R.drawable.ic_format_align_left
//				textAlignCenter -> R.drawable.ic_format_align_center
//				textAlignRight -> R.drawable.ic_format_align_right
//				textAlignJustify -> R.drawable.ic_format_align_justify
//				else -> R.drawable.ic_format_align_left
//			},
//			highlight = false
//		) { }


		Spacer(modifier = Modifier.width(8.dp))
	}
}

@Composable
private fun ToolbarButton(
	name: String,
	icon: Int,
	highlight: Boolean,
	indicateClick: Boolean = false,
	onClick: () -> Unit
) {
	Row(
		modifier = Modifier
			.height(40.dp)
	) {
		if (indicateClick) {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
					.requiredSize(40.dp)
					.clip(RoundedCornerShape(25))
					.background(Color.Companion.Transparent)
					.clickable { onClick() },
			) {
				Icon(
					painter = painterResource(id = icon),
					contentDescription = name,
					tint = MaterialTheme.colorScheme.onSecondaryContainer,
					modifier = Modifier.padding(8.dp)
				)
			}
		} else {
			val containerColor by animateColorAsState(targetValue = if (highlight) MaterialTheme.colorScheme.onSecondaryContainer else Color.Companion.Transparent)
			val contentColor by animateColorAsState(targetValue = if (highlight) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.onSecondaryContainer)

			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
					.requiredSize(40.dp)
					.clip(RoundedCornerShape(25))
					.background(containerColor)
					.clickable(
						interactionSource = remember { MutableInteractionSource() },
						indication = null
					) { onClick() },
			) {
				Icon(
					painter = painterResource(id = icon),
					contentDescription = name,
					tint = contentColor,
					modifier = Modifier.padding(8.dp)
				)
			}
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
private fun TextAlignToolbar() {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(56.dp)
			.background(MaterialTheme.colorScheme.secondaryContainer)
			.horizontalScroll(rememberScrollState()),
		verticalAlignment = Alignment.CenterVertically
	) {
		Spacer(modifier = Modifier.width(8.dp))

		ToolbarButton(name = "Align left", icon = R.drawable.ic_format_align_left, highlight = false) {}
		ToolbarButton(name = "Align center", icon = R.drawable.ic_format_align_center, highlight = false) {}
		ToolbarButton(name = "Align right", icon = R.drawable.ic_format_align_right, highlight = false) {}
		ToolbarButton(name = "Align justify", icon = R.drawable.ic_format_align_justify, highlight = false) { }

		Spacer(modifier = Modifier.width(8.dp))
	}
}

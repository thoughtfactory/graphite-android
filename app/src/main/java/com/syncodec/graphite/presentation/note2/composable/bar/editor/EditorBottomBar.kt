package com.syncodec.graphite.presentation.note2.composable.bar.editor

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.note2.composable.bar.editor.bottomSheet.ColorBottomSheet
import com.syncodec.graphite.presentation.note2.composable.bar.editor.bottomSheet.LinkBottomSheet
import com.syncodec.graphite.presentation.base.ICON_BUTTON_SIZE
import com.syncodec.graphite.presentation.base.LocalIsPro
import com.syncodec.graphite.presentation.note2.kitKat.KitKatAction
import com.syncodec.graphite.presentation.note2.kitKat.KitKatFormat
import com.syncodec.graphite.utils.LocationData
import com.syncodec.graphite.utils.toHexString
import kotlinx.coroutines.launch


@Preview
@Composable
fun EditorBottomBar(
	kitKatFormat: KitKatFormat = KitKatFormat(),
	locationData: LocationData = LocationData.Init,
	onClickMetadata: () -> Unit = {},
	noClickLocation: () -> Unit = {},
	onClickAttachments: () -> Unit = {},
	onClickTags: () -> Unit = {},
	onKitKatAction: (KitKatAction) -> Unit = {}
) {

	val scrollState = rememberScrollState()

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.background)
			.padding(vertical = 12.dp)
			.horizontalScroll(scrollState)
	) {
		Spacer(modifier = Modifier.width(12.dp))

		NoteAction(
			locationData = locationData,
			onClickMetadata = onClickMetadata,
			onClickLocation = noClickLocation,
			onClickAttachments = onClickAttachments,
			onClickTags = onClickTags,
			onKitKatAction = onKitKatAction,
		)

		Spacer(modifier = Modifier.width(12.dp))

		BasicAction(
			kitKatFormat = kitKatFormat,
			onKitKatAction = onKitKatAction
		)

		Spacer(modifier = Modifier.width(12.dp))

		BreakAction(
			kitKatFormat = kitKatFormat,
			onKitKatAction = onKitKatAction,
		)

		Spacer(modifier = Modifier.width(12.dp))

		ListAction(
			kitKatFormat = kitKatFormat,
			onKitKatAction = onKitKatAction,
		)

		Spacer(modifier = Modifier.width(12.dp))

		BlockAction(
			kitKatFormat = kitKatFormat,
			onKitKatAction = onKitKatAction,
		)

		Spacer(modifier = Modifier.width(12.dp))

		ColorfulAction(
			kitKatFormat = kitKatFormat,
			onKitKatAction = onKitKatAction
		)

		Spacer(modifier = Modifier.width(12.dp))

		BarBlock {
			Text(
				text = "CHAR : ${kitKatFormat.characterCount}",
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Bold,
				modifier = Modifier.padding(horizontal = 8.dp)
			)

			EditorButtonSpacer()

			Text(
				text = "WORD : ${kitKatFormat.wordCount}",
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Bold,
				modifier = Modifier.padding(horizontal = 8.dp)
			)
		}

		Spacer(modifier = Modifier.width(12.dp))
	}


}

@Preview
@Composable
private fun NoteAction(
	locationData: LocationData = LocationData.Init,
	onClickMetadata: () -> Unit = {},
	onClickLocation: () -> Unit = {},
	onClickAttachments: () -> Unit = {},
	onClickTags: () -> Unit = {},
	onKitKatAction: (KitKatAction) -> Unit = {},
) {
	BarBlock {
		EditorButton(
			icon = R.drawable.ic_fa_info,
			tooltip = "Note metadata",
			isChecked = false,
			onClick = onClickMetadata
		)

		EditorButton(
			icon = locationData.getIcon(),
			tooltip = "Location",
			iconStaticColor = locationData.getIconColor(),
			isChecked = false,
			onClick = onClickLocation,
		)

		EditorButton(
			icon = R.drawable.ic_fa_new_file,
			tooltip = "Attachments",
			isChecked = false,
			onClick = onClickAttachments,
		)

		EditorButton(
			icon = R.drawable.ic_fa_tag,
			tooltip = "Tags",
			isChecked = false,
			onClick = onClickTags,
		)

		if (BuildConfig.DEBUG) {
			EditorButton(
				icon = R.drawable.ic_fa_bug,
				tooltip = "Debug",
				isChecked = false,
				onClick = { }
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun BasicAction(
	kitKatFormat: KitKatFormat = KitKatFormat(),
	onKitKatAction: (KitKatAction) -> Unit = {},
) {
	val scope = rememberCoroutineScope()

	val bottomSheetState = rememberModalBottomSheetState()
	var isLinkBottomSheetVisible by remember { mutableStateOf(false) }

	BarBlock {
		EditorButton(
			icon = R.drawable.ic_flat_format_undo_2,
			tooltip = "Undo",
			isChecked = false,
			onClick = { onKitKatAction(KitKatAction.Undo) }
		)

		EditorButton(
			icon = R.drawable.ic_flat_format_redo_2,
			tooltip = "Redo",
			isChecked = false,
			onClick = { onKitKatAction(KitKatAction.Redo) }
		)

		EditorButtonSpacer()

		EditorButton(
			icon = R.drawable.ic_fa_format_bold,
			tooltip = "Toggle bold",
			isChecked = kitKatFormat.bold,
			onClick = { onKitKatAction(KitKatAction.Bold) }
		)

		EditorButton(
			icon = R.drawable.ic_fa_italic,
			tooltip = "Toggle italic",
			isChecked = kitKatFormat.italic,
			isPremium = true,
			onClick = { onKitKatAction(KitKatAction.Italic) }
		)

		EditorButton(
			icon = R.drawable.ic_fa_format_underline,
			tooltip = "Toggle underline",
			isPremium = true,
			isChecked = kitKatFormat.underline,
			onClick = { onKitKatAction(KitKatAction.Underline) }
		)

		EditorButton(
			icon = R.drawable.ic_fa_format_strikethrough,
			tooltip = "Toggle strikethrough",
			isChecked = kitKatFormat.strike,
			isPremium = true,
			onClick = { onKitKatAction(KitKatAction.StrikeThrough) }
		)

		EditorButton(
			icon = R.drawable.ic_fa_format_superscript,
			tooltip = "Toggle superscript",
			isChecked = kitKatFormat.superscript,
			isPremium = true,
			onClick = { onKitKatAction(KitKatAction.Superscript) }
		)

		EditorButton(
			icon = R.drawable.ic_fa_format_subscript,
			tooltip = "Toggle subscript",
			isChecked = kitKatFormat.subscript,
			isPremium = true,
			onClick = { onKitKatAction(KitKatAction.Subscript) }
		)
		EditorButton(
			icon = R.drawable.ic_fa_format_link,
			tooltip = "Link",
			isChecked = false,
			onClick = { isLinkBottomSheetVisible = true }
		)
	}

	LinkBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isLinkBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide() }; isLinkBottomSheetVisible = false },
		link = kitKatFormat.link,
		onKitKatAction = onKitKatAction
	)
}

@Preview
@Composable
private fun BreakAction(
	kitKatFormat: KitKatFormat = KitKatFormat(),
	onKitKatAction: (KitKatAction) -> Unit = {},
) {
	BarBlock {
		EditorButton(
			icon = R.drawable.ic_fa_format_hard_break,
			tooltip = "Hard break",
			isChecked = false,
			onClick = { onKitKatAction(KitKatAction.HardLineBreak) }
		)
		EditorButton(
			icon = R.drawable.ic_fa_minus,
			tooltip = "Horizontal rule",
			isChecked = false,
			onClick = { onKitKatAction(KitKatAction.HorizontalRule) }
		)
	}
}

@Preview
@Composable
private fun ListAction(
	kitKatFormat: KitKatFormat = KitKatFormat(),
	onKitKatAction: (KitKatAction) -> Unit = {},
) {
	BarBlock {
		EditorButton(
			icon = R.drawable.ic_fa_format_list_unordered,
			tooltip = "Unordered list",
			isChecked = kitKatFormat.bulletList,
			onClick = { onKitKatAction(KitKatAction.List.BulletList) }
		)
		EditorButton(
			icon = R.drawable.ic_fa_format_list_ordered,
			tooltip = "Ordered list",
			isChecked = kitKatFormat.orderedList,
			isPremium = true,
			onClick = { onKitKatAction(KitKatAction.List.OrderedList) }
		)
		EditorButton(
			icon = R.drawable.ic_fa_format_list_check,
			tooltip = "Check list",
			isChecked = kitKatFormat.taskList,
			isPremium = true,
			onClick = { onKitKatAction(KitKatAction.List.CheckList) }
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun BlockAction(
	kitKatFormat: KitKatFormat = KitKatFormat(),
	onKitKatAction: (KitKatAction) -> Unit = {},
) {
	val context = LocalContext.current
	val isPro = LocalIsPro.current

	var isAlignmentMenuVisible by remember { mutableStateOf(false) }
	var isHeadingMenuVisible by remember { mutableStateOf(false) }

	BarBlock {
		EditorButton(
			icon = R.drawable.ic_flat_format_block_quote,
			tooltip = "Blockquote",
			isChecked = kitKatFormat.blockquote,
			onClick = { onKitKatAction(KitKatAction.Blockquote) }
		)

		ExposedDropdownMenuBox(
			expanded = isHeadingMenuVisible,
			onExpandedChange = { isHeadingMenuVisible = false },
			modifier = Modifier
				.height(40.dp)
				.padding(2.dp)
		) {
			Box(modifier = Modifier) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier
						.fillMaxHeight()
						.clickable(onClickLabel = "Heading", role = Role.Button) {
							if (isPro) isHeadingMenuVisible = true
							else Toast
								.makeText(context, "Join Graphite pro to unlock full potential of editor", Toast.LENGTH_SHORT)
								.show()
						}
						.menuAnchor()
				) {
					Spacer(modifier = Modifier.width(8.dp))
					if (kitKatFormat.paragraph) {
						Icon(
							painter = painterResource(id = R.drawable.ic_fa_format_paragraph),
							contentDescription = "Paragraph",
							modifier = Modifier.requiredSize(ICON_BUTTON_SIZE)
						)
					} else {
						Text(
							text = when {
								kitKatFormat.heading1 -> "H1"
								kitKatFormat.heading2 -> "H2"
								kitKatFormat.heading3 -> "H3"
								kitKatFormat.heading4 -> "H4"
								kitKatFormat.heading5 -> "H5"
								kitKatFormat.heading6 -> "H6"
								else -> "Unknown"
							},
							style = MaterialTheme.typography.titleMedium,
							fontWeight = FontWeight.Bold,
						)
					}
					Spacer(modifier = Modifier.width(8.dp))
				}

				Box(
					modifier = Modifier
						.matchParentSize()
						.background(MaterialTheme.colorScheme.background.copy(alpha = 0.71f), MaterialTheme.shapes.small)
				)

				Icon(
					painter = painterResource(id = R.drawable.ic_fa_lock_close_solid),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier
						.requiredSize(20.dp)
						.padding(4.dp)
						.align(Alignment.BottomEnd)
				)
			}
			ExposedDropdownMenu(
				expanded = isHeadingMenuVisible,
				onDismissRequest = { isHeadingMenuVisible = false },
				modifier = Modifier.widthIn(64.dp)
			) {
				DropdownMenuItem(
					leadingIcon = { Icon(painter = painterResource(id = R.drawable.ic_fa_format_paragraph), contentDescription = "Paragraph", modifier = Modifier.requiredSize(ICON_BUTTON_SIZE)) },
					text = { Text(text = "") },
					onClick = { onKitKatAction(KitKatAction.Heading.Paragraph) }
				)
				DropdownMenuItem(
					text = { Text(text = "H1", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
					onClick = { onKitKatAction(KitKatAction.Heading.Heading1) }
				)
				DropdownMenuItem(
					text = { Text(text = "H2", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
					onClick = { onKitKatAction(KitKatAction.Heading.Heading2) }
				)
				DropdownMenuItem(
					text = { Text(text = "H3", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
					onClick = { onKitKatAction(KitKatAction.Heading.Heading3) }
				)
				DropdownMenuItem(
					text = { Text(text = "H4", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
					onClick = { onKitKatAction(KitKatAction.Heading.Heading4) }
				)
				DropdownMenuItem(
					text = { Text(text = "H5", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
					onClick = { onKitKatAction(KitKatAction.Heading.Heading5) }
				)
				DropdownMenuItem(
					text = { Text(text = "H6", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
					onClick = { onKitKatAction(KitKatAction.Heading.Heading6) }
				)

			}
		}

		ExposedDropdownMenuBox(
			expanded = isAlignmentMenuVisible,
			onExpandedChange = { isAlignmentMenuVisible = false },
			modifier = Modifier
				.height(40.dp)
				.padding(2.dp)
		) {
			PlainTooltipBox(
				tooltip = { Text(text = "Alignment") }
			) {
				Box(
					modifier = Modifier.menuAnchor()
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier
							.fillMaxHeight()
							.clip(MaterialTheme.shapes.small)
							.clickable(onClickLabel = "Alignment", role = Role.Button) {
								if (isPro) isAlignmentMenuVisible = true
								else Toast
									.makeText(context, "Join Graphite pro to unlock full potential of editor", Toast.LENGTH_SHORT)
									.show()
							}
					) {
						Spacer(modifier = Modifier.width(8.dp))
						Icon(
							painter = painterResource(
								id = when {
									kitKatFormat.alignLeft -> R.drawable.ic_fa_format_align_left
									kitKatFormat.alignCenter -> R.drawable.ic_fa_format_align_center
									kitKatFormat.alignRight -> R.drawable.ic_fa_format_align_right
									kitKatFormat.alignJustify -> R.drawable.ic_fa_format_align_justify
									else -> R.drawable.ic_fa_format_align_left
								}
							),
							contentDescription = "Alignment",
							modifier = Modifier.requiredSize(ICON_BUTTON_SIZE)
						)

						Spacer(modifier = Modifier.width(4.dp))

						Text(
							text = when {
								kitKatFormat.alignLeft -> "Left"
								kitKatFormat.alignCenter -> "Center"
								kitKatFormat.alignRight -> "Right"
								kitKatFormat.alignJustify -> "Justify"
								else -> "Unknown"
							},
							style = MaterialTheme.typography.bodyMedium,
							fontWeight = FontWeight.Bold,
							modifier = Modifier.padding(horizontal = 8.dp)
						)
						Spacer(modifier = Modifier.width(8.dp))
					}

					Box(
						modifier = Modifier
							.matchParentSize()
							.background(MaterialTheme.colorScheme.background.copy(alpha = 0.71f), MaterialTheme.shapes.small)
					)

					Icon(
						painter = painterResource(id = R.drawable.ic_fa_lock_close_solid),
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier
							.requiredSize(20.dp)
							.padding(4.dp)
							.align(Alignment.BottomEnd)
					)
				}
			}
			ExposedDropdownMenu(
				expanded = isAlignmentMenuVisible,
				onDismissRequest = { isAlignmentMenuVisible = false },
				modifier = Modifier.widthIn(128.dp)
			) {
				DropdownMenuItem(
					leadingIcon = { Icon(painter = painterResource(id = R.drawable.ic_fa_format_align_left), contentDescription = "Left align", modifier = Modifier.requiredSize(ICON_BUTTON_SIZE)) },
					text = { Text(text = "Left") },
					onClick = { onKitKatAction(KitKatAction.Align.Left) }
				)
				DropdownMenuItem(
					leadingIcon = { Icon(painter = painterResource(id = R.drawable.ic_fa_format_align_center), contentDescription = "Center align", modifier = Modifier.requiredSize(ICON_BUTTON_SIZE)) },
					text = { Text(text = "Center") },
					onClick = { onKitKatAction(KitKatAction.Align.Center) }
				)
				DropdownMenuItem(
					leadingIcon = { Icon(painter = painterResource(id = R.drawable.ic_fa_format_align_right), contentDescription = "Right align", modifier = Modifier.requiredSize(ICON_BUTTON_SIZE)) },
					text = { Text(text = "Right") },
					onClick = { onKitKatAction(KitKatAction.Align.Right) }
				)
				DropdownMenuItem(
					leadingIcon = { Icon(painter = painterResource(id = R.drawable.ic_fa_format_align_justify), contentDescription = "Justify align", modifier = Modifier.requiredSize(ICON_BUTTON_SIZE)) },
					text = { Text(text = "Justify") },
					onClick = { onKitKatAction(KitKatAction.Align.Justify) }
				)
			}
		}

		EditorButtonSpacer()

		EditorButton(
			icon = R.drawable.ic_fa_format_indent,
			tooltip = "Indent",
			isChecked = false,
			onClick = { onKitKatAction(KitKatAction.Indent) }
		)

		EditorButton(
			icon = R.drawable.ic_fa_format_outdent,
			tooltip = "Outdent",
			isChecked = false,
			onClick = { onKitKatAction(KitKatAction.Outdent) }
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun ColorfulAction(
	kitKatFormat: KitKatFormat = KitKatFormat(),
	onKitKatAction: (KitKatAction) -> Unit = {},
) {
	val scope = rememberCoroutineScope()

	val bottomSheetState = rememberModalBottomSheetState()
	var isTextColorBottomSheetVisible by remember { mutableStateOf(false) }
	var isHighlightColorBottomSheetVisible by remember { mutableStateOf(false) }

	BarBlock {
		EditorButton(
			icon = R.drawable.ic_fa_format_text_color,
			tooltip = "Text color",
			isChecked = false,
			isPremium = true,
			onClick = { isTextColorBottomSheetVisible = true }
		)
		EditorButton(
			icon = R.drawable.ic_fa_format_highlighter,
			tooltip = "Highlight color",
			isChecked = false,
			isPremium = true,
			onClick = { isHighlightColorBottomSheetVisible = true }
		)
	}

	ColorBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isTextColorBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide() }; isTextColorBottomSheetVisible = false },
		title = "Text color",
		color = kitKatFormat.textColor,
		onSetColor = { onKitKatAction(KitKatAction.TextColor.Set(it.toHexString())) },
		onUnSetColor = { onKitKatAction(KitKatAction.TextColor.Unset) },
		onExtendSelection = { onKitKatAction(KitKatAction.TextColor.ExtendSelection) },
	)

	ColorBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isHighlightColorBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide() }; isHighlightColorBottomSheetVisible = false },
		title = "Highlight color",
		color = kitKatFormat.highlightColor,
		onSetColor = { onKitKatAction(KitKatAction.HighlightColor.Set(it.toHexString())) },
		onUnSetColor = { onKitKatAction(KitKatAction.HighlightColor.Unset) },
		onExtendSelection = { onKitKatAction(KitKatAction.HighlightColor.ExtendSelection) },
	)
}

@Preview
@Composable
private fun BarBlock(
	modifier: Modifier = Modifier,
	content: @Composable RowScope.() -> Unit = {}
) {
	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier
			.height(48.dp)
			.background(
				MaterialTheme.colorScheme
					.surfaceColorAtElevation(8.dp)
					.copy(alpha = 0.31f), MaterialTheme.shapes.small
			)
			.padding(4.dp)
			.clip(MaterialTheme.shapes.small),
	) {
		Row(
			modifier = Modifier,
			verticalAlignment = Alignment.CenterVertically,
			content = content
		)
	}
}

//package com.syncodec.momento.diaryComponent.miscellaneous
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.animateColorAsState
//import androidx.compose.animation.core.AnimationSpec
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.*
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.BasicTextField
//import androidx.compose.material.Card
//import androidx.compose.material.DropdownMenu
//import androidx.compose.material.ExperimentalMaterialApi
//import androidx.compose.material.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.SolidColor
//import androidx.compose.ui.graphics.toArgb
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.window.PopupProperties
//import com.syncodec.momento.custom.EditorView
//import com.syncodec.momento.konstant.ErrorCode
//import compose.icons.TablerIcons
//import compose.icons.tablericons.*
//import okhttp3.internal.toHexString
//
//
//data class ToolItem(
//	val title: String?,
//	val icon: ImageVector,
//	var highlight: Boolean = false,
//	var _highlight: MutableState<Boolean> = mutableStateOf(false),
//	val showText: Boolean = false,
//	val fullSize: Boolean = false,
//	var dropDownMenu: @Composable () -> Unit = {},
//	val onClick: () -> Unit
//)
//
//@Composable
//fun EditorToolbar(
//	editorView: EditorView,
//	onError: (ErrorCode.Companion.ErrorCode) -> Unit,
//) {
//	var textFormat by remember { mutableStateOf(EditorView.TextFormat()) }
//
//	editorView.setOnFormatUpdate(object : EditorView.OnFormatUpdateListener {
//		override fun onFormatUpdate(newTextFormat: EditorView.TextFormat) {
//			textFormat = newTextFormat
////			if (!showUrlField || textFormat.link != null) {
////				urlString = textFormat.link!!
////			}
//		}
//	})
//
//	var showUrlField: Boolean by remember { mutableStateOf(false) }
//	var urlString: String by remember { mutableStateOf("") }
//
//	var showTextColor: Boolean by remember { mutableStateOf(false) }
//	var showHighlightColor: Boolean by remember { mutableStateOf(false) }
//
//	var alignmentMenuState by remember { mutableStateOf(false) }
//	var headingMenuState by remember { mutableStateOf(false) }
//
//	val alignmentToolList: List<ToolItem> = listOf(
//		ToolItem(title = "Align Left", icon = TablerIcons.AlignLeft, highlight = textFormat.textAlignLeft, showText = true) {
//			editorView.exec(
//				"alignment('alignleft')"
//			)
//		},
//		ToolItem(title = "Align Center", icon = TablerIcons.AlignCenter, highlight = textFormat.textAlignCenter, showText = true) {
//			editorView.exec(
//				"alignment('aligncenter')"
//			)
//		},
//		ToolItem(title = "Align Right", icon = TablerIcons.AlignRight, highlight = textFormat.textAlignRight, showText = true) {
//			editorView.exec(
//				"alignment('alignright')"
//			)
//		},
//		ToolItem(title = "Align Justified", icon = TablerIcons.AlignJustified, highlight = textFormat.textAlignJustify, showText = true) {
//			editorView.exec(
//				"alignment('alignjustify')"
//			)
//		}
//	)
//
//	val headingToolList: List<ToolItem> = listOf(
//		ToolItem(title = "P", icon = TablerIcons.LetterP, highlight = textFormat.paragraph) {
//			editorView.exec(
//				"heading('p')"
//			)
//		}, ToolItem(title = "H1", icon = TablerIcons.H1, highlight = textFormat.heading1) {
//			editorView.exec(
//				"heading('h1')"
//			)
//		},
//		ToolItem(title = "H2", icon = TablerIcons.H2, highlight = textFormat.heading2) {
//			editorView.exec(
//				"heading('h2')"
//			)
//		},
//		ToolItem(title = "H3", icon = TablerIcons.H3, highlight = textFormat.heading3) {
//			editorView.exec(
//				"heading('h3')"
//			)
//		},
//		ToolItem(title = "H4", icon = TablerIcons.H4, highlight = textFormat.heading4) {
//			editorView.exec(
//				"heading('h4')"
//			)
//		},
//		ToolItem(title = "H5", icon = TablerIcons.H5, highlight = textFormat.heading5) {
//			editorView.exec(
//				"heading('h5')"
//			)
//		},
//		ToolItem(title = "H6", icon = TablerIcons.H6, highlight = textFormat.heading6) {
//			editorView.exec(
//				"heading('h6')"
//			)
//		}
//	)
//
//	val fontFamilyToolList: List<ToolItem> = listOf(
//		ToolItem(title = "Open Sans", icon = TablerIcons.LetterP, highlight = textFormat.paragraph) {
//			editorView.exec(
//				"heading('p')"
//			)
//		}, ToolItem(title = "H1", icon = TablerIcons.H1, highlight = textFormat.heading1) {
//			editorView.exec(
//				"heading('h1')"
//			)
//		},
//		ToolItem(title = "H2", icon = TablerIcons.H2, highlight = textFormat.heading2) {
//			editorView.exec(
//				"heading('h2')"
//			)
//		},
//		ToolItem(title = "H3", icon = TablerIcons.H3, highlight = textFormat.heading3) {
//			editorView.exec(
//				"heading('h3')"
//			)
//		},
//		ToolItem(title = "H4", icon = TablerIcons.H4, highlight = textFormat.heading4) {
//			editorView.exec(
//				"heading('h4')"
//			)
//		},
//		ToolItem(title = "H5", icon = TablerIcons.H5, highlight = textFormat.heading5) {
//			editorView.exec(
//				"heading('h5')"
//			)
//		},
//		ToolItem(title = "H6", icon = TablerIcons.H6, highlight = textFormat.heading6) {
//			editorView.exec(
//				"heading('h6')"
//			)
//		}
//	)
//
//	val toolListLeft: List<ToolItem> = listOf(
//		ToolItem(title = "Undo", icon = TablerIcons.ArrowBackUp, highlight = false) {
//			editorView.exec(
//				"undo()"
//			)
//		},
//		ToolItem(title = "Redo", icon = TablerIcons.ArrowForwardUp, highlight = false) {
//			editorView.exec(
//				"redo()"
//			)
//		},
//
//		ToolItem(title = null, icon = TablerIcons.X) {},
//
//		ToolItem(title = "Clear Format", icon = TablerIcons.ClearFormatting, highlight = false) {
//			editorView.exec(
//				"removeFormat()"
//			)
//		},
//		ToolItem(title = "Bold", icon = TablerIcons.Bold, highlight = textFormat.bold, _highlight = mutableStateOf(textFormat.bold)) {
//			editorView.exec("editor.commands.toggleBold();")
//		},
//		ToolItem(title = "Italic", icon = TablerIcons.Italic, highlight = textFormat.italic) {
//			editorView.exec("editor.commands.toggleItalic();")
//		},
//		ToolItem(title = "Underline", icon = TablerIcons.Underline, highlight = textFormat.underline) {
//			editorView.exec("editor.commands.toggleUnderline();")
//		},
//		ToolItem(title = "Strikethrough", icon = TablerIcons.Strikethrough, highlight = textFormat.strike) {
//			editorView.exec("editor.commands.toggleStrike()")
//		},
//		ToolItem(title = "Superscript", icon = TablerIcons.Superscript, highlight = textFormat.superscript) {
//			editorView.exec("editor.commands.toggleSuperscript()")
//		},
//		ToolItem(title = "Subscript", icon = TablerIcons.Subscript, highlight = textFormat.subscript) {
//			editorView.exec("editor.commands.toggleSubscript()")
//		},
//
//		ToolItem(title = null, icon = TablerIcons.X) {},
//
//		ToolItem(
//			title = "Alignment",
//			icon = when {
//				textFormat.textAlignLeft -> TablerIcons.AlignLeft
//				textFormat.textAlignCenter -> TablerIcons.AlignCenter
//				textFormat.textAlignRight -> TablerIcons.AlignRight
//				textFormat.textAlignJustify -> TablerIcons.AlignJustified
//				else -> TablerIcons.AlignLeft
//			},
//			highlight = textFormat.textAlignLeft || textFormat.textAlignCenter || textFormat.textAlignRight || textFormat.textAlignJustify,
//			dropDownMenu = {
//				DropDownMenu(
//					toolList = alignmentToolList,
//					isExpanded = alignmentMenuState,
//					onDismiss = { alignmentMenuState = false }
//				)
//			},
//		) {
//			alignmentMenuState = true
//		},
//		ToolItem(
//			title = when {
//				textFormat.paragraph -> "Paragraph"
//				textFormat.heading1 -> "Heading 1"
//				textFormat.heading2 -> "Heading 2"
//				textFormat.heading3 -> "Heading 3"
//				textFormat.heading4 -> "Heading 4"
//				textFormat.heading5 -> "Heading 5"
//				textFormat.heading6 -> "Heading 6"
//				else -> "Paragraph"
//			},
//			icon = when {
//				textFormat.paragraph -> TablerIcons.LetterP
//				textFormat.heading1 -> TablerIcons.H1
//				textFormat.heading2 -> TablerIcons.H2
//				textFormat.heading3 -> TablerIcons.H3
//				textFormat.heading4 -> TablerIcons.H4
//				textFormat.heading5 -> TablerIcons.H5
//				textFormat.heading6 -> TablerIcons.H6
//				else -> TablerIcons.LetterP
//			},
//			highlight = textFormat.paragraph || textFormat.heading1 || textFormat.heading2 || textFormat.heading3 || textFormat.heading4 || textFormat.heading5 || textFormat.heading6,
//			fullSize = true,
//			dropDownMenu = {
//				DropDownMenu(
//					toolList = headingToolList,
//					isExpanded = headingMenuState,
//					onDismiss = { headingMenuState = false }
//				)
//			}
//		) {
//			headingMenuState = true
//		},
//
//		ToolItem(title = null, icon = TablerIcons.X) {},
//
//		ToolItem(title = "Link", icon = TablerIcons.Link, highlight = textFormat.link?.isNotEmpty() ?: false) {
//			showUrlField = !showUrlField
//		},
//		ToolItem(title = "Blockquote", icon = TablerIcons.Blockquote, highlight = textFormat.blockquote) {
//			editorView.exec(
//				"blockquote()"
//			)
//		},
//		ToolItem(title = "Hashtag", icon = TablerIcons.Hash, highlight = textFormat.code) {
//			editorView.exec(
//				"hashtag()"
//			)
//		},
//		ToolItem(title = "Unordered List", icon = TablerIcons.List, highlight = textFormat.bulletList) {
//			editorView.exec(
//				"insertUnorderedList()"
//			)
//		},
//		ToolItem(title = "Ordered List", icon = TablerIcons.ClipboardList, highlight = textFormat.orderedList) {
//			editorView.exec(
//				"insertOrderedList()"
//			)
//		},
//		ToolItem(title = "Checkbox", icon = TablerIcons.SquareCheck) {
//			editorView.exec(
//				"insertCheckbox()"
//			)
//		},
//
//		ToolItem(title = null, icon = TablerIcons.X) {},
//
//		ToolItem(title = "Indent", icon = TablerIcons.IndentIncrease) {
//			editorView.exec(
//				"indent()"
//			)
//		},
//		ToolItem(title = "Outdent", icon = TablerIcons.IndentDecrease) {
//			editorView.exec(
//				"outdent()"
//			)
//		},
//
//		ToolItem(title = null, icon = TablerIcons.X) {},
//
//		ToolItem(
//			title = "Text color",
//			icon = TablerIcons.Palette,
//			highlight = false,
//			dropDownMenu = {
//				EditorToolbarTextColor(
//					isExpanded = showTextColor,
//					onDismiss = { showTextColor = false },
//					onClick = { color ->
//						editorView.exec(
//							"applyTextColor('#${color.toArgb().toHexString().substring(2, 8)}');"
//						)
//					}
//				)
//			},
//		) {
//			showTextColor = true
//		},
//		ToolItem(
//			title = "Highlight color",
//			icon = TablerIcons.Paint,
//			highlight = false,
//			dropDownMenu = {
//				EditorToolbarTextColor(
//					isExpanded = showHighlightColor,
//					onDismiss = { showHighlightColor = false },
//					onClick = { color ->
//						editorView.exec(
//							"applyHighlightColor('#${color.toArgb().toHexString().substring(2, 8)}');"
//						)
//					}
//				)
//			}
//		) {
//			showHighlightColor = true
//		},
//		ToolItem(
//			title = textFormat.fontFamily,
//			icon = TablerIcons.Typography,
//			highlight = textFormat.paragraph || textFormat.heading1 || textFormat.heading2 || textFormat.heading3 || textFormat.heading4 || textFormat.heading5 || textFormat.heading6,
//			fullSize = true,
//			dropDownMenu = {
//				DropDownMenu(
//					toolList = headingToolList,
//					isExpanded = headingMenuState,
//					onDismiss = { headingMenuState = false }
//				)
//			}
//		) {},
//		ToolItem(
//			title = textFormat.fontSize,
//			icon = TablerIcons.CursorText,
//			highlight = false,
//			fullSize = true,
//			dropDownMenu = {
//				DropDownMenu(
//					toolList = headingToolList,
//					isExpanded = headingMenuState,
//					onDismiss = { headingMenuState = false }
//				)
//			}
//		) {}
//	)
//
//	Column(
//		modifier = Modifier
//			.fillMaxWidth()
//			.padding(0.dp, 0.dp)
//			.background(MaterialTheme.colorScheme.primaryContainer)
//	) {
//		EditorToolbarUrlEditor(
//			url = urlString,
//			showField = showUrlField,
//			onUpdateUrl = { urlString = it },
//			onSaveUrl = {
//				if (textFormat.startOffset != textFormat.endOffset) {
//					editorView.exec("link(\'$urlString\')")
//				} else {
//					onError(ErrorCode.Companion.ErrorCode.URL_RANGE_SELECTION_ERROR)
//				}
//			},
//			onRemoveUrl = { urlString = "" }
//		)
//
//		Row(
//			modifier = Modifier
//				.fillMaxWidth()
//				.padding(0.dp, 2.dp)
//				.horizontalScroll(rememberScrollState())
//		) {
//			Spacer(modifier = Modifier.width(4.dp))
//			toolListLeft.forEach { toolItem ->
//				if (toolItem.title == null) {
//					EditorToolbarSpacer()
//				} else {
//					EditorToolbarButton(
//						toolItem = toolItem
//					)
//				}
//			}
//			Spacer(modifier = Modifier.width(4.dp))
//		}
//	}
//}
//
//@Composable
//private fun EditorToolbarSpacer() {
//	Spacer(modifier = Modifier.width(4.dp))
//	Box(
//		modifier = Modifier
//			.width(2.dp)
//			.height(48.dp)
//			.padding(0.dp, 8.dp)
//			.background(MaterialTheme.colorScheme.primary)
//			.clip(RoundedCornerShape(2.dp))
//	)
//	Spacer(modifier = Modifier.width(4.dp))
//}
//
//@Composable
//private fun EditorToolbarButton(
//	toolItem: ToolItem
//) {
//	val highlight by remember { mutableStateOf(value = toolItem.highlight) }
//
//	val containerColor by animateColorAsState(
//		if (toolItem._highlight.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
//		animationSpec = tween(400)
//	)
//
//	val contentColor by animateColorAsState(
//		if (toolItem._highlight.value) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
//	)
//	Card(
//		shape = RoundedCornerShape(8.dp),
//		backgroundColor = containerColor,
//		modifier = Modifier
//			.width(if (toolItem.fullSize) Dp.Unspecified else 48.dp)
//			.height(48.dp)
//			.padding(2.dp)
//			.clip(RoundedCornerShape(8.dp))
//			.clickable { toolItem.onClick() }
//	) {
//		if (toolItem.fullSize) {
//			Row(
//				verticalAlignment = Alignment.CenterVertically,
//				modifier = Modifier
//					.padding(16.dp, 0.dp)
//			) {
//				Icon(
//					imageVector = toolItem.icon,
//					contentDescription = toolItem.title,
//					tint = contentColor,
//					modifier = Modifier
//						.requiredSize(20.dp)
//				)
//
//				Spacer(modifier = Modifier.width(8.dp))
//
//				Text(
//					text = toolItem.title!!,
//					style = MaterialTheme.typography.bodyMedium,
//					color = contentColor
//				)
//			}
//		} else {
//			Icon(
//				imageVector = toolItem.icon,
//				contentDescription = toolItem.title,
//				tint = if (toolItem.highlight) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
//				modifier = Modifier
//					.requiredSize(20.dp)
//			)
//		}
//		toolItem.dropDownMenu.invoke()
//	}
//}
//
//@Composable
//private fun EditorToolbarUrlEditor(
//	url: String,
//	showField: Boolean,
//	onUpdateUrl: (String) -> Unit,
//	onSaveUrl: () -> Unit,
//	onRemoveUrl: () -> Unit
//) {
//
//	AnimatedVisibility(visible = showField) {
//		Card(
//			elevation = 0.dp,
//			shape = RoundedCornerShape(4.dp),
//			backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
//			modifier = Modifier
//				.fillMaxWidth()
//				.height(56.dp)
//				.padding(6.dp, 2.dp)
//				.clip(RoundedCornerShape(4.dp))
//		) {
//			Row(
//				verticalAlignment = Alignment.CenterVertically,
//				modifier = Modifier
//					.padding(0.dp, 0.dp, 2.dp, 0.dp)
//			) {
//				BasicTextField(
//					value = url,
//					onValueChange = { newUrl -> onUpdateUrl(newUrl) },
//					singleLine = true,
//					cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
//					textStyle = MaterialTheme.typography.bodyMedium.copy(
//						color = MaterialTheme.colorScheme.primary,
//						fontWeight = FontWeight.Bold
//					),
//					modifier = Modifier
//						.weight(1f)
//						.height(52.dp),
//				) { innerTextField ->
//					Card(
//						backgroundColor = Color.Transparent,
//						shape = RoundedCornerShape(4.dp),
//						border = BorderStroke(2.dp, MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.47f)),
//						elevation = 0.dp,
//						modifier = Modifier
//							.padding(4.dp)
//							.fillMaxWidth()
//					) {
//						Box(
//							contentAlignment = Alignment.CenterStart,
//							modifier = Modifier
//								.padding(12.dp, 0.dp)
//						) {
//							if (url.isEmpty()) {
//								Text(
//									"Enter url (http://)",
//									style = MaterialTheme.typography.bodyMedium,
//									color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.47f),
//									fontWeight = FontWeight.Bold
//								)
//							}
//							innerTextField()
//						}
//					}
//				}
//
//
//				val removeUrlToolItem = ToolItem(title = "Remove Url", icon = TablerIcons.X) {
//					onRemoveUrl()
//				}
//				val saveUrlToolItem = ToolItem(title = "Save Url", icon = TablerIcons.Check) {
//					onSaveUrl()
//				}
//
//				EditorToolbarButton(toolItem = removeUrlToolItem)
//				EditorToolbarButton(toolItem = saveUrlToolItem)
//			}
//		}
//	}
//}
//
//@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
//@Composable
//private fun EditorToolbarTextColor(
//	isExpanded: Boolean,
//	onDismiss: () -> Unit,
//	onClick: (Color) -> Unit
//) {
//
//
//	val colorList: List<Color> = listOf(
//		Color(0xFF36C2D6),
//		Color(0xFF391509),
//		Color(0xFF1937F6),
//		Color(0xFF5E67F7),
//		Color(0xFFBE93E4),
//		Color(0xFFC04671),
//		Color(0xFFD1564E),
//		Color(0xFFF49CF5),
//		Color(0xFFBDC46B),
//		Color(0xFF5BF4E8),
//		Color(0xFF4FED77),
//		Color(0xFF2A703F),
//		Color(0xFFB9A36A),
//		Color(0xFF4F27D9),
//		Color(0xFF268686),
//		Color(0xFFCB6352),
//		Color(0xFFC29EDB),
//		Color(0xFF825D36),
//		Color(0xFF1BDB21),
//		Color(0xFFDF18BA),
//		Color(0xFFC0B862),
//		Color(0xFFE167D5),
//		Color(0xFF58EDDF),
//		Color(0xFF6F421D),
//		Color(0xFFEA4D37),
//		Color(0xFF657C43),
//		Color(0xFF46B644),
//		Color(0xFFF661B0)
//	)
//
//	DropdownMenu(
//		expanded = isExpanded,
//		onDismissRequest = { onDismiss() },
//		properties = PopupProperties(
//			clippingEnabled = false
//		),
//		modifier = Modifier
//			.background(MaterialTheme.colorScheme.secondaryContainer),
//	) {
//		for (i in 0 until 4) {
//			Row(
//				modifier = Modifier
//					.padding(4.dp, 0.dp)
//			) {
//				for (j in 0 until 7) {
//					Card(
//						elevation = 0.dp,
//						backgroundColor = colorList[(i * 7) + j],
//						shape = RoundedCornerShape(4.dp),
//						modifier = Modifier
//							.requiredSize(32.dp)
//							.padding(6.dp)
//							.clip(RoundedCornerShape(4.dp)),
//						onClick = {
//							onClick(colorList[(i * 7) + j])
//							onDismiss()
//						}
//					) {}
//				}
//			}
//		}
//	}
//}
//
//@Composable
//private fun DropDownMenu(
//	toolList: List<ToolItem>,
//	isExpanded: Boolean = false,
//	onDismiss: () -> Unit
//) {
//	DropdownMenu(
//		expanded = isExpanded,
//		onDismissRequest = { onDismiss() },
//		properties = PopupProperties(
//			clippingEnabled = false
//		),
//		modifier = Modifier
//			.background(MaterialTheme.colorScheme.secondaryContainer),
//	) {
//		toolList.forEach { toolItem ->
//			Card(
//				elevation = 0.dp,
//				backgroundColor = if (toolItem.highlight) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
//				shape = RoundedCornerShape(4.dp),
//				modifier = Modifier
//					.height(48.dp)
//					.padding(4.dp, 0.dp)
//					.clip(RoundedCornerShape(4.dp))
//					.clickable { toolItem.onClick() },
//			) {
//				Row(
//					verticalAlignment = Alignment.CenterVertically,
//					modifier = Modifier
//						.fillMaxWidth()
//						.padding(16.dp, 0.dp)
//				) {
//					Icon(
//						imageVector = toolItem.icon,
//						contentDescription = toolItem.title,
//						tint = if (toolItem.highlight) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
//						modifier = Modifier
//							.requiredSize(20.dp)
//					)
//
//					if (toolItem.showText) {
//						Spacer(modifier = Modifier.width(16.dp))
//
//						Text(
//							text = toolItem.title!!,
//							style = MaterialTheme.typography.bodyMedium,
//							color = if (toolItem.highlight) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
//						)
//					}
//				}
//			}
//		}
//	}
//}

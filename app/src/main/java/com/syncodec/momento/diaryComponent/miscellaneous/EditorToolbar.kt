package com.syncodec.momento.diaryComponent.miscellaneous

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.syncodec.momento.custom.EditorView
import compose.icons.TablerIcons
import compose.icons.tablericons.*

data class ToolItem(
	val name: String,
	val icon: ImageVector,
	var highlight: Boolean,
	var dropDownList: List<ToolItem>? = null,
	var dropDownState: Boolean = false,
	val onDismiss: () -> Unit = {},
	val onClick: () -> Unit
)

@Composable
fun EditorToolbar(
	editorView: EditorView,
) {
	var alignmentMenuState by remember { mutableStateOf(false) }
	var headingMenuState by remember { mutableStateOf(false) }

	val alignmentToolList: List<ToolItem> = listOf(
		ToolItem(name = "Align Left", icon = TablerIcons.AlignLeft, highlight = false) {
			editorView.exec(
				"alignment('JustifyLeft')"
			)
		},
		ToolItem(name = "Align Center", icon = TablerIcons.AlignCenter, highlight = false) {
			editorView.exec(
				"alignment('JustifyCenter')"
			)
		},
		ToolItem(name = "Align Right", icon = TablerIcons.AlignRight, highlight = false) {
			editorView.exec(
				"alignment('JustifyRight')"
			)
		},
		ToolItem(name = "Align Justified", icon = TablerIcons.AlignJustified, highlight = false) {
			editorView.exec(
				"alignment('JustifyFull')"
			)
		}
	)

	val headingToolList: List<ToolItem> = listOf(
		ToolItem(name = "H1", icon = TablerIcons.H1, highlight = false) {
			editorView.exec(
				"heading('h1')"
			)
		},
		ToolItem(name = "H2", icon = TablerIcons.H2, highlight = false) {
			editorView.exec(
				"heading('h2')"
			)
		},
		ToolItem(name = "H3", icon = TablerIcons.H3, highlight = false) {
			editorView.exec(
				"heading('h3')"
			)
		},
		ToolItem(name = "H4", icon = TablerIcons.H4, highlight = false) {
			editorView.exec(
				"heading('h4')"
			)
		},
		ToolItem(name = "H5", icon = TablerIcons.H5, highlight = false) {
			editorView.exec(
				"heading('h5')"
			)
		},
		ToolItem(name = "H6", icon = TablerIcons.H6, highlight = false) {
			editorView.exec(
				"heading('h6')"
			)
		}
	)

	val toolList: List<ToolItem> = listOf(
		ToolItem(name = "Undo", icon = TablerIcons.ArrowBackUp, highlight = false) {
			editorView.exec(
				"undo()"
			)
		},
		ToolItem(name = "Redo", icon = TablerIcons.ArrowForwardUp, highlight = false) {
			editorView.exec(
				"redo()"
			)
		},
		ToolItem(name = "Clear Format", icon = TablerIcons.ClearFormatting, highlight = false) {
			editorView.exec(
				"bold()"
			)
		},
		ToolItem(name = "Bold", icon = TablerIcons.Bold, highlight = false) {
			editorView.exec(
				"bold()"
			)
		},
		ToolItem(name = "Italic", icon = TablerIcons.Italic, highlight = false) {
			editorView.exec(
				"italic()"
			)
		},
		ToolItem(name = "Underline", icon = TablerIcons.Underline, highlight = false) {
			editorView.exec(
				"underline()"
			)
		},
		ToolItem(name = "Strikethrough", icon = TablerIcons.Strikethrough, highlight = false) {
			editorView.exec(
				"strikethrough()"
			)
		},
		ToolItem(name = "Superscript", icon = TablerIcons.Superscript, highlight = false) {
			editorView.exec(
				"superscript()"
			)
		},
		ToolItem(name = "Subscript", icon = TablerIcons.Subscript, highlight = false) {
			editorView.exec(
				"subscript()"
			)
		},
		ToolItem(
			name = "Alignment",
			icon = TablerIcons.AlignLeft,
			highlight = false,
			dropDownList = alignmentToolList,
			dropDownState = alignmentMenuState,
			onDismiss = { alignmentMenuState = false }
		) {
			alignmentMenuState = true
		},
		ToolItem(
			name = "Heading",
			icon = TablerIcons.H1,
			highlight = false,
			dropDownList = headingToolList,
			dropDownState = headingMenuState,
			onDismiss = { headingMenuState = false }
		) {
			headingMenuState = true
		},
		ToolItem(name = "Bullet List", icon = TablerIcons.List, highlight = false,) {},
		ToolItem(name = "Numbered List", icon = TablerIcons.LayoutList, highlight = false,) {},
		ToolItem(name = "Indent", icon = TablerIcons.IndentIncrease, highlight = false,) {},
		ToolItem(name = "Outdent", icon = TablerIcons.IndentDecrease, highlight = false,) {},
	)

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(48.dp)
			.horizontalScroll(rememberScrollState())
	) {
		toolList.forEach { toolItem ->
			Card(
				elevation = 0.dp,
				modifier = Modifier
					.size(48.dp)
					.padding(2.dp)
					.clip(RoundedCornerShape(4.dp))
					.clickable { toolItem.onClick() }
			) {
				Icon(
					imageVector = toolItem.icon,
					contentDescription = toolItem.name,
					modifier = Modifier
						.padding(8.dp)
				)
				if (toolItem.dropDownList != null) {
					DropDownMenu(
						toolList = toolItem.dropDownList!!,
						isExpanded = toolItem.dropDownState,
						onDismiss = toolItem.onDismiss
					)
				}
			}
		}
	}
}

@Composable
fun DropDownMenu(
	toolList: List<ToolItem>,
	isExpanded: Boolean = false,
	onDismiss: () -> Unit
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.wrapContentSize(Alignment.TopStart)
	) {
		DropdownMenu(
			expanded = isExpanded,
			onDismissRequest = { onDismiss() },
			modifier = Modifier,
		) {
			toolList.forEach { toolItem ->
				Card(
					modifier = Modifier
						.size(40.dp)
						.padding(4.dp)
						.clickable { toolItem.onClick() },
					elevation = 0.dp,
				) {
					Text(text = toolItem.name)
				}
//				DropdownMenuItem(
//					onClick = toolItem.onClick
//				) {
//					Text(text = toolItem.name)
//				}
			}
		}
	}
}

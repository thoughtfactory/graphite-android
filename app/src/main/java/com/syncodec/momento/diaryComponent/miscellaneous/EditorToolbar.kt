package com.syncodec.momento.diaryComponent.miscellaneous

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.momento.R
import com.syncodec.momento.custom.EditorView
import com.syncodec.momento.konstant.ErrorCode


data class ToolItem(
	val title: String?,
	val icon: ImageVector,
	var highlight: Boolean = false,
	var _highlight: MutableState<Boolean> = mutableStateOf(false),
	val showText: Boolean = false,
	val fullSize: Boolean = false,
	var dropDownMenu: @Composable () -> Unit = {},
	val onClick: () -> Unit
)

@Composable
fun EditorToolbar(
	editorView: EditorView,
	onError: (ErrorCode.Companion.ErrorCode) -> Unit,
) {
	var textFormat by remember { mutableStateOf(EditorView.TextFormat()) }

	var boldState by remember { mutableStateOf(textFormat.bold) }
	var italicState by remember { mutableStateOf(textFormat.italic) }
	var underlineState by remember { mutableStateOf(textFormat.underline) }
	var bulletListState by remember { mutableStateOf(textFormat.bulletList) }
	var orderedListState by remember { mutableStateOf(textFormat.orderedList) }
	var taskListState by remember { mutableStateOf(textFormat.taskList) }

	editorView.setOnFormatUpdate(object : EditorView.OnFormatUpdateListener {
		override fun onFormatUpdate(newTextFormat: EditorView.TextFormat) {
			textFormat = newTextFormat

			boldState = textFormat.bold
			italicState = textFormat.italic
			underlineState = textFormat.underline
			bulletListState = textFormat.bulletList
			orderedListState = textFormat.orderedList
			taskListState = textFormat.taskList
		}
	})


	Box(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.primaryContainer)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(0.dp, 4.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Spacer(modifier = Modifier.width(4.dp))
			ToolbarButton(icon = painterResource(id = R.drawable.ic_format_bold), description = "Bold", highlight = boldState)
			{ editorView.exec("editor.commands.toggleBold();") }
			ToolbarButton(icon = painterResource(id = R.drawable.ic_format_italic), description = "Italic", highlight = italicState)
			{ editorView.exec("editor.commands.toggleItalic();") }
			ToolbarButton(icon = painterResource(id = R.drawable.ic_format_underline), description = "Underline", highlight = underlineState)
			{ editorView.exec("editor.commands.toggleUnderline();") }

			Spacer(
				modifier = Modifier
					.width(10.dp)
					.height(32.dp)
					.padding(4.dp)
					.background(MaterialTheme.colorScheme.primary)
					.clip(RoundedCornerShape(4.dp))
			)

			ToolbarButton(icon = painterResource(id = R.drawable.ic_format_list_bullet), description = "Bullet List", highlight = bulletListState)
			{ editorView.exec("editor.commands.toggleBulletList();") }
			ToolbarButton(icon = painterResource(id = R.drawable.ic_format_list_ordered), description = "Ordered List", highlight = orderedListState)
			{ editorView.exec("editor.commands.toggleOrderedList();") }
			ToolbarButton(icon = painterResource(id = R.drawable.ic_format_list_task), description = "Task List", highlight = taskListState)
			{ editorView.exec("editor.commands.toggleTaskList();") }
		}
	}
}

@Composable
private fun ToolbarButton(
	icon: Painter,
	description: String,
	highlight: Boolean,
	onClick: () -> Unit
) {
	val containerColor = animateColorAsState(
		targetValue = if (highlight) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer,
		animationSpec = tween(200)
	)
	val contentColor = animateColorAsState(
		targetValue = if (highlight) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
		animationSpec = tween(200)
	)
	val containerElevation = animateDpAsState(
		targetValue = if (highlight) 16.dp else 0.dp,
		animationSpec = tween(200)
	)

	Surface(
		shape = RoundedCornerShape(8.dp),
		color = containerColor.value,
		modifier = Modifier
			.requiredSize(48.dp)
			.padding(2.dp)
			.clickable { onClick() },
		shadowElevation = containerElevation.value
	) {
		Icon(
			painter = icon,
			contentDescription = description,
			tint = contentColor.value,
			modifier = Modifier
				.requiredSize(24.dp)
		)
	}
}

package com.syncodec.graphite.presentation.note.composable.bar.bottomBar.editorBottomBar

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.richText.RichTextEditor
import com.syncodec.graphite.utils.tone


@Composable
fun HeadingBar(
	richTextEditor : RichTextEditor,
	textFormat : RichTextEditor.TextFormat,
	closeBar : () -> Unit,
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier.horizontalScroll(rememberScrollState()),
	) {
		Spacer(modifier = Modifier.width(12.dp))

		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.height(48.dp)
				.background(
					color = MaterialTheme.colorScheme.surface
						.tone(isSystemInDarkTheme(), 1)
						.copy(alpha = 0.31f),
					shape = RoundedCornerShape(50)
				)
				.clip(RoundedCornerShape(50)),
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxHeight(),
			) {
				Spacer(modifier = Modifier.width(12.dp))

				MenuButton(
					icon = R.drawable.ic_chevron_down,
					contentDescription = "Close Heading Bar",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					shape = CircleShape,
					onClick = closeBar
				)

				MenuButton(
					icon = R.drawable.ic_format_paragraph,
					contentDescription = "Paragraph",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					isChecked = textFormat.paragraph,
					onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 3 });") }
				)

				MenuButton(
					icon = R.drawable.ic_format_h1,
					contentDescription = "Heading 1",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					isChecked = textFormat.heading1,
					onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 1 });") }
				)

				MenuButton(
					icon = R.drawable.ic_format_h2,
					contentDescription = "Heading 2",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					isChecked = textFormat.heading2,
					onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 2 });") }
				)

				MenuButton(
					icon = R.drawable.ic_format_h3,
					contentDescription = "Heading 3",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					isChecked = textFormat.heading3,
					onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 3 });") }
				)

				MenuButton(
					icon = R.drawable.ic_format_h4,
					contentDescription = "Heading 4",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					isChecked = textFormat.heading4,
					onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 4 });") }
				)

				MenuButton(
					icon = R.drawable.ic_format_h5,
					contentDescription = "Heading 5",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					isChecked = textFormat.heading5,
					onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 5 });") }
				)

				MenuButton(
					icon = R.drawable.ic_format_h6,
					contentDescription = "Heading 6",
					tint = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1).copy(alpha = 0.71f),
					isChecked = textFormat.heading6,
					onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 6 });") }
				)

				Spacer(modifier = Modifier.width(12.dp))
			}
		}

		Spacer(modifier = Modifier.width(12.dp))
	}
}

package com.syncodec.graphite.presentation.note.composable.bar.bottomBar.editorBottomBar

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.richText.RichTextEditor
import com.syncodec.graphite.presentation.note.composable.bar.bottomBar.editorBottomBar.buildingBlock.ToolbarButton
import com.syncodec.graphite.utils.LocalCompositionRichTextEditor


@Preview
@Composable
fun HeadingBar(
	richTextEditor : RichTextEditor = LocalCompositionRichTextEditor.current,
	textFormat : RichTextEditor.TextFormat = RichTextEditor.TextFormat(),
	closeBar : () -> Unit = {},
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier.horizontalScroll(rememberScrollState()),
	) {
		Spacer(modifier = Modifier.width(8.dp))

		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.31f), MaterialTheme.shapes.small)
				.clip(MaterialTheme.shapes.small),
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.padding(2.dp),
			) {
				Spacer(modifier = Modifier.width(2.dp))

				ToolbarButton(
					icon = R.drawable.ic_caret,
					contentDescription = "Close Heading Bar",
					isChecked = false,
					modifier = Modifier.graphicsLayer { rotationZ = 180f },
					onClick = closeBar,
				)

				ToolbarButton(
					icon = R.drawable.ic_format_paragraph,
					contentDescription = "Paragraph",
					isChecked = textFormat.paragraph,
					onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 3 });") }
				)

				ToolbarButton(
					icon = R.drawable.ic_format_h1,
					contentDescription = "Heading 1",
					isChecked = textFormat.heading1,
					onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 1 });") }
				)

				ToolbarButton(
					icon = R.drawable.ic_format_h2,
					contentDescription = "Heading 2",
					isChecked = textFormat.heading2,
					onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 2 });") }
				)

				ToolbarButton(
					icon = R.drawable.ic_format_h3,
					contentDescription = "Heading 3",
					isChecked = textFormat.heading3,
					onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 3 });") }
				)

				MenuButton(
					icon = R.drawable.ic_format_h4,
					contentDescription = "Heading 4",
					isChecked = textFormat.heading4,
					onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 4 });") }
				)

				ToolbarButton(
					icon = R.drawable.ic_format_h5,
					contentDescription = "Heading 5",
					isChecked = textFormat.heading5,
					onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 5 });") }
				)

				ToolbarButton(
					icon = R.drawable.ic_format_h6,
					contentDescription = "Heading 6",
					isChecked = textFormat.heading6,
					onClick = { richTextEditor.exec("editor.commands.toggleHeading({ level: 6 });") }
				)

				Spacer(modifier = Modifier.width(2.dp))
			}
		}

		Spacer(modifier = Modifier.width(8.dp))
	}
}

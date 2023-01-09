package com.syncodec.graphite.presentation.note.composable.bar.bottomBar.editorBottomBar

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.richText.RichTextEditor
import com.syncodec.graphite.presentation.note.composable.bar.bottomBar.editorBottomBar.buildingBlock.ToolbarButton
import com.syncodec.graphite.presentation.note.composable.bar.bottomBar.editorBottomBar.buildingBlock.ToolbarSpacer
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.utils.LocalCompositionRichTextEditor


@Composable
fun FreeFormatToolbar(
	richTextEditor : RichTextEditor = LocalCompositionRichTextEditor.current,
	textFormat : RichTextEditor.TextFormat = RichTextEditor.TextFormat(),
) {
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
				icon = R.drawable.ic_format_undo,
				contentDescription = "Undo",
				isChecked = false,
				onClick = { richTextEditor.exec("editor.commands.undo();") }
			)
			ToolbarButton(
				icon = R.drawable.ic_format_redo,
				contentDescription = "Redo",
				isChecked = false,
				onClick = { richTextEditor.exec("editor.commands.redo();") }
			)

			ToolbarSpacer()

			ToolbarButton(
				icon = R.drawable.ic_format_bold,
				contentDescription = "Bold",
				isChecked = textFormat.bold,
				onClick = { richTextEditor.exec("editor.chain().focus().toggleBold().run()") }
			)
			FreeToolbarButton(
				icon = R.drawable.ic_format_italic,
				contentDescription = "Italic",
				isChecked = textFormat.italic,
			)
			FreeToolbarButton(
				icon = R.drawable.ic_format_underline,
				contentDescription = "Underline",
				isChecked = textFormat.underline,
			)
			FreeToolbarButton(
				icon = R.drawable.ic_format_strikethrough,
				contentDescription = "Strikethrough",
				isChecked = textFormat.strike,
			)
			ToolbarButton(
				icon = R.drawable.ic_format_hard_break,
				contentDescription = "Format hard break",
				isChecked = false,
				onClick = { richTextEditor.exec("editor.chain().focus().setHardBreak().run()") }
			)

			ToolbarSpacer()

			ToolbarButton(
				icon = R.drawable.ic_format_list_bullet,
				contentDescription = "Bullet list",
				isChecked = textFormat.bulletList,
				onClick = { richTextEditor.exec("editor.commands.toggleBulletList();") }
			)
			FreeToolbarButton(
				icon = R.drawable.ic_format_list_ordered,
				contentDescription = "Ordered list",
				isChecked = textFormat.orderedList,
			)
			FreeToolbarButton(
				icon = R.drawable.ic_format_list_check,
				contentDescription = "Check list",
				isChecked = textFormat.taskList,
			)

			ToolbarSpacer()

			FreeToolbarButton(
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
			)
			ToolbarButton(
				icon = R.drawable.ic_format_blockquote,
				contentDescription = "Format blockquote",
				isChecked = textFormat.blockquote,
				onClick = { richTextEditor.exec("editor.chain().focus().toggleBlockquote().run();") }
			)

			ToolbarSpacer()

			ToolbarButton(
				icon = R.drawable.ic_format_indent,
				contentDescription = "Format indent",
				isChecked = false,
				onClick = { richTextEditor.exec("editor.chain().focus().sinkListItem('listItem').run()") }
			)

			ToolbarButton(
				icon = R.drawable.ic_format_outdent,
				contentDescription = "Format outdent",
				isChecked = false,
				onClick = { richTextEditor.exec("editor.chain().focus().liftListItem('listItem').run()") }
			)

			ToolbarSpacer()

			FreeToolbarButton(
				icon = R.drawable.ic_format_superscript,
				contentDescription = "Format superscript",
				isChecked = textFormat.superscript,
			)
			FreeToolbarButton(
				icon = R.drawable.ic_format_subscript,
				contentDescription = "Format subscript",
				isChecked = textFormat.subscript,
			)

			Spacer(modifier = Modifier.width(2.dp))
		}
	}
}

@Preview
@Composable
private fun FreeToolbarButton(
	icon : Int = R.drawable.ic_format_bold,
	contentDescription : String? = null,
	isChecked : Boolean = false,
) {
	val context = LocalContext.current

	val containerColor by animateColorAsState(
		targetValue = if (isChecked) MaterialTheme.colorScheme.onSurface else Color(ColorUtils.blendARGB(MaterialTheme.colorScheme.background.toArgb(), MaterialTheme.colorScheme.surface.toArgb(), 0.31f)),
		animationSpec = tween(300)
	)
	val contentColor by animateColorAsState(
		targetValue = if (isChecked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
		animationSpec = tween(300)
	)

	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.requiredSize(40.dp)
			.padding(2.dp)
			.background(containerColor, MaterialTheme.shapes.small)
			.clip(MaterialTheme.shapes.small)
			.clickable(onClickLabel = contentDescription, role = Role.Button) {
				Toast.makeText(context, "Join Graphite Pro to access full potential of the editor.", Toast.LENGTH_SHORT).show()
			}
	) {
		Icon(
			painter = painterResource(id = icon),
			contentDescription = contentDescription,
			tint = contentColor,
			modifier = Modifier
				.requiredSize(IconButtonSize)
				.padding(2.dp)
		)

		Box(
			contentAlignment = Alignment.BottomEnd,
			modifier = Modifier
				.requiredSize(40.dp)
				.padding(2.dp)
		) {
			Box(
				modifier = Modifier.background(containerColor, CircleShape)
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_lock_close),
					contentDescription = contentDescription,
					tint = contentColor,
					modifier = Modifier
						.requiredSize(16.dp)
						.padding(2.dp)
				)
			}
		}
	}
}

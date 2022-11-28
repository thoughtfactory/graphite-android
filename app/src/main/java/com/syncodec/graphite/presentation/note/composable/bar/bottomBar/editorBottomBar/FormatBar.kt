package com.syncodec.graphite.presentation.note.composable.bar.bottomBar.editorBottomBar

import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.richText.RichTextEditor
import com.syncodec.graphite.presentation.note.composable.bar.bottomBar.editorBottomBar.buildingBlock.ToolbarSpacer
import com.syncodec.graphite.utils.tone
import java.util.Calendar


@Composable
fun FormatBar(
	richTextEditor : RichTextEditor,
	textFormat : RichTextEditor.TextFormat,
	userTimestamp : Long?,
	onClickTimePicker : () -> Unit,
	onClickMetadata : () -> Unit,
	onClickLocation : () -> Unit,
	onClickAttachment : () -> Unit,
	onClickTag : () -> Unit,
	onClickHeading : () -> Unit,
) {

	val calendar = remember { Calendar.getInstance() }

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
				.clip(RoundedCornerShape(50))
				.clickable { onClickTimePicker() }
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxHeight(),
			) {
				Spacer(modifier = Modifier.width(12.dp))

				Text(
					text = DateFormat.format("dd", userTimestamp ?: calendar.timeInMillis).toString(),
					style = MaterialTheme.typography.headlineMedium,
					color = MaterialTheme.colorScheme.onSurface,
					fontWeight = FontWeight.Bold
				)

				Spacer(modifier = Modifier.width(4.dp))

				Column(
					verticalArrangement = Arrangement.Center,
					modifier = Modifier.fillMaxHeight(),
				) {
					Text(
						text = DateFormat.format("MMM, yyyy", userTimestamp ?: calendar.timeInMillis).toString(),
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurface,
					)

					Text(
						text = DateFormat.format("hh:mm aa", userTimestamp ?: calendar.timeInMillis).toString(),
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurface,
					)
				}

				Spacer(modifier = Modifier.width(12.dp))
			}
		}

		Spacer(modifier = Modifier.width(8.dp))

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

				ToolbarButton(
					icon = R.drawable.ic_info,
					contentDescription = "Info",
					shape = CircleShape,
					isChecked = false,
					isPremium = false,
					onClick = onClickMetadata
				)

				ToolbarButton(
					icon = R.drawable.ic_map_marker,
					contentDescription = "Location",
					shape = CircleShape,
					isChecked = false,
					isPremium = false,
					onClick = onClickLocation
				)

				ToolbarButton(
					icon = R.drawable.ic_attachment,
					contentDescription = "Attachment",
					shape = CircleShape,
					isChecked = false,
					isPremium = false,
					onClick = onClickAttachment
				)

				ToolbarButton(
					icon = R.drawable.ic_hashtag,
					contentDescription = "Tag",
					shape = CircleShape,
					isChecked = false,
					isPremium = false,
					onClick = onClickTag
				)

				Spacer(modifier = Modifier.width(12.dp))
			}
		}

		Spacer(modifier = Modifier.width(8.dp))

		Row(
			modifier = Modifier
				.background(
					color = MaterialTheme.colorScheme.surface
						.tone(isSystemInDarkTheme(), 1)
						.copy(alpha = 0.31f),
					shape = RoundedCornerShape(50)
				)
				.clip(RoundedCornerShape(50))
		) {
			Row(
				modifier = Modifier.padding(2.dp)
			) {
				ToolbarButton(
					icon = R.drawable.ic_format_undo,
					contentDescription = "Undo",
					isChecked = false,
					isPremium = false,
					onClick = { richTextEditor.exec("editor.commands.undo();") }
				)
				ToolbarButton(
					icon = R.drawable.ic_format_redo,
					contentDescription = "Redo",
					isChecked = false,
					isPremium = false,
					onClick = { richTextEditor.exec("editor.commands.redo();") }
				)

				ToolbarSpacer()

				ToolbarButton(
					icon = R.drawable.ic_format_bold,
					contentDescription = "Bold",
					isChecked = textFormat.bold,
					isPremium = false,
					onClick = { richTextEditor.exec("editor.chain().focus().toggleBold().run()") }
				)
				ToolbarButton(
					icon = R.drawable.ic_format_italic,
					contentDescription = "Italic",
					isChecked = textFormat.italic,
					isPremium = true,
					onClick = { richTextEditor.exec("editor.chain().focus().toggleItalic().run()") }
				)
				ToolbarButton(
					icon = R.drawable.ic_format_underline,
					contentDescription = "Underline",
					isChecked = textFormat.underline,
					isPremium = true,
					onClick = { richTextEditor.exec("editor.chain().focus().toggleUnderline().run()") }
				)
				ToolbarButton(
					icon = R.drawable.ic_format_strikethrough,
					contentDescription = "Strikethrough",
					isChecked = textFormat.strike,
					isPremium = true,
					onClick = { richTextEditor.exec("editor.chain().focus().toggleStrike().run()") }
				)
				ToolbarButton(
					icon = R.drawable.ic_format_hard_break,
					contentDescription = "Format hard break",
					isChecked = false,
					isPremium = false,
					onClick = { richTextEditor.exec("editor.chain().focus().setHardBreak().run()") }
				)
				ToolbarSpacer()

				ToolbarButton(
					icon = R.drawable.ic_format_list_check,
					contentDescription = "Check list",
					isChecked = textFormat.taskList,
					isPremium = true,
					onClick = { richTextEditor.exec("editor.commands.toggleTaskList();") }
				)
				ToolbarButton(
					icon = R.drawable.ic_format_list_bullet,
					contentDescription = "Bullet list",
					isChecked = textFormat.bulletList,
					isPremium = true,
					onClick = { richTextEditor.exec("editor.commands.toggleBulletList();") }
				)
				ToolbarButton(
					icon = R.drawable.ic_format_list_ordered,
					contentDescription = "Ordered list",
					isChecked = textFormat.orderedList,
					isPremium = true,
					onClick = { richTextEditor.exec("editor.commands.toggleOrderedList();") }
				)

				ToolbarSpacer()

				ToolbarButton(
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
					isPremium = true,
					onClick = onClickHeading
				)
				ToolbarButton(
					icon = R.drawable.ic_format_blockquote,
					contentDescription = "Format blockquote",
					isChecked = textFormat.blockquote,
					isPremium = false,
					onClick = { richTextEditor.exec("editor.chain().focus().toggleBlockquote().run();") }
				)

				ToolbarSpacer()

				ToolbarButton(
					icon = R.drawable.ic_format_indent,
					contentDescription = "Format indent",
					isChecked = false,
					isPremium = false,
					onClick = { richTextEditor.exec("editor.chain().focus().sinkListItem('listItem').run()") }
				)

				ToolbarButton(
					icon = R.drawable.ic_format_outdent,
					contentDescription = "Format outdent",
					isChecked = false,
					isPremium = false,
					onClick = { richTextEditor.exec("editor.chain().focus().liftListItem('listItem').run()") }
				)

				ToolbarSpacer()

				ToolbarButton(
					icon = R.drawable.ic_format_superscript,
					contentDescription = "Format superscript",
					isChecked = textFormat.superscript,
					isPremium = true,
					onClick = { richTextEditor.exec("editor.chain().focus().toggleSuperscript().run();") }
				)
				ToolbarButton(
					icon = R.drawable.ic_format_subscript,
					contentDescription = "Format subscript",
					isChecked = textFormat.subscript,
					isPremium = true,
					onClick = { richTextEditor.exec("editor.chain().focus().toggleSubscript().run();") }
				)
			}
		}

		Spacer(modifier = Modifier.width(12.dp))
	}
}

@Composable
private fun ToolbarButton(
	icon : Int,
	contentDescription : String? = null,
	shape : Shape = RoundedCornerShape(25),
	isChecked : Boolean,
	isPremium : Boolean,
	onClick : () -> Unit
) {
	val context = LocalContext.current

	val containerColor by animateColorAsState(
		targetValue = if (isChecked) MaterialTheme.colorScheme.onSurface else Color.Transparent,
		animationSpec = tween(300)
	)
	val contentColor by animateColorAsState(
		targetValue = if (isChecked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
		animationSpec = tween(300)
	)

	val isPro by BaseApplication.isPro

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
		modifier = Modifier
			.requiredSize(52.dp)
			.padding(2.dp)
			.background(containerColor, shape)
			.clip(shape)
			.clickable(onClickLabel = contentDescription, role = Role.Button) {
				if (isPremium && ! isPro) Toast
					.makeText(context, "Join Graphite Pro to unlock rich text editor", Toast.LENGTH_SHORT)
					.show()
				else onClick()
			}
	) {

		Spacer(modifier = Modifier.requiredSize(12.dp))

		Icon(
			painter = painterResource(id = icon),
			contentDescription = contentDescription,
			tint = contentColor,
			modifier = Modifier.requiredSize(24.dp)
		)

		if (isPremium && ! isPro) {
			Box(
				modifier = Modifier.background(MaterialTheme.colorScheme.onSurface, RoundedCornerShape(50))
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.padding(2.dp)
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_pro_member),
						contentDescription = "Pro",
						tint = MaterialTheme.colorScheme.surface,
						modifier = Modifier.requiredSize(8.dp)
					)

					Text(
						text = "PRO",
						fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
						fontWeight = FontWeight.Bold,
						fontSize = 8.sp,
						lineHeight = 10.sp,
						letterSpacing = 1.sp,
						color = MaterialTheme.colorScheme.surface,
						modifier = Modifier
					)
				}
			}
		} else {
			Spacer(modifier = Modifier.requiredSize(12.dp))
		}
	}
}

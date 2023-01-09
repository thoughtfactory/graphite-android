package com.syncodec.graphite.presentation.tags.composable.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DialogTextField
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons
import com.syncodec.graphite.utils.getInverseBWColor
import com.syncodec.graphite.utils.getRandomColor
import com.syncodec.graphite.utils.toHexString


@Preview
@Composable
fun EditTagDialog(
	showDialog : Boolean = true,
	tag : String = "Tag",
	color : Color = getRandomColor(),
	onSave : (String, Color) -> Unit = { _, _ -> },
	onDismiss : () -> Unit = {},
) {

	var _tag by remember { mutableStateOf("") }
	var _color by remember { mutableStateOf(getRandomColor()) }

	LaunchedEffect(key1 = tag) {
		_tag = tag
	}
	LaunchedEffect(key1 = color) {
		_color = color
	}

	GenericDialog(
		showDialog = showDialog,
		title = "Edit Tag",
		onDismissRequest = onDismiss
	) {
		DialogTextField(
			value = _tag,
			label = "Tag",
			placeholder = "Add a tag",
		) { _tag = it ?: "" }

		Spacer(modifier = Modifier.height(8.dp))

		ClassicColorPicker(
			color = _color,
			onColorChanged = { _color = it.toColor() },
			showAlphaBar = false,
			modifier = Modifier
				.fillMaxWidth()
				.height(256.dp)
		)

		Spacer(modifier = Modifier.height(8.dp))

		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.fillMaxWidth()
				.height(48.dp)
				.background(_color, MaterialTheme.shapes.medium)
		) {
			Text(
				text = _color.toHexString(),
				style = MaterialTheme.typography.bodyMedium,
				color = _color.getInverseBWColor(),
				modifier = Modifier.align(Alignment.Center)
			)
		}

		Spacer(modifier = Modifier.height(24.dp))

		DualActionButtons(
			primaryText = "Save",
			secondaryText = "Discard",
			onPrimaryClick = { onSave(_tag, _color) },
			onSecondaryClick = onDismiss
		)
	}
}

@Composable
fun TextField(
	text : String,
	placeholder : String,
	onValueChanged : (String) -> Unit,
) {
	val containerColor = MaterialTheme.colorScheme.background
	val contentColor = MaterialTheme.colorScheme.onBackground

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.height(48.dp)
			.padding(0.dp, 0.dp)
			.background(containerColor, RoundedCornerShape(12.dp))
			.clip(RoundedCornerShape(12.dp))
	) {
		Spacer(modifier = Modifier.width(12.dp))

		BasicTextField(
			value = text,
			onValueChange = { onValueChanged(it) },
			singleLine = true,
			keyboardOptions = KeyboardOptions.Default,
			keyboardActions = KeyboardActions.Default,
			cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
			textStyle = MaterialTheme.typography.bodyMedium,
			modifier = Modifier.weight(1f),
			visualTransformation = { text ->
				TransformedText(
					AnnotatedString(
						text.toString(),
						SpanStyle(color = contentColor, fontWeight = FontWeight.Bold)
					),
					OffsetMapping.Identity
				)
			},
			decorationBox = { innerTextField ->
				AnimatedVisibility(
					visible = text.isEmpty(),
					enter = fadeIn(tween(300)),
					exit = fadeOut(tween(300))
				) {
					Crossfade(targetState = placeholder) {
						Text(
							text = it,
							style = MaterialTheme.typography.bodyMedium,
							color = contentColor.copy(alpha = 0.31f),
						)
					}
				}
				innerTextField()
			}
		)
	}
}

package com.syncodec.graphite.presentation.common.text

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R


@Composable
fun LargeTextField(
	modifier : Modifier = Modifier,
	text : String,
	placeholder : String,
	isFocused : Boolean,
	focusRequester : FocusRequester = FocusRequester(),
	onFocusChanged : (Boolean) -> Unit = {},
	keyboardOptions : KeyboardOptions? = null,
	keyboardActions : KeyboardActions? = null,
	containerColor : Color = MaterialTheme.colorScheme.background.copy(alpha = 0.71f),
	contentColor : Color = MaterialTheme.colorScheme.onBackground,
	onValueChanged : (String) -> Unit
) {
	val context = LocalContext.current
	val clipboardManager : ClipboardManager = LocalClipboardManager.current

	Row(
		modifier = modifier
			.clip(RoundedCornerShape(12.dp))
			.background(containerColor),
		verticalAlignment = Alignment.CenterVertically
	) {
		Spacer(modifier = Modifier.width(12.dp))

		BasicTextField(
			value = text,
			onValueChange = { onValueChanged(it) },
			singleLine = true,
			keyboardOptions = keyboardOptions ?: KeyboardOptions.Default,
			keyboardActions = keyboardActions ?: KeyboardActions.Default,
			cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
			textStyle = MaterialTheme.typography.bodyMedium,
			modifier = Modifier
				.weight(1f)
				.focusRequester(focusRequester)
				.onFocusChanged { onFocusChanged(it.isFocused) },
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

		Spacer(modifier = Modifier.width(4.dp))

		IconButton(
			onClick = {
				try {
					if (clipboardManager.hasText()) clipboardManager.getText()?.let { clipboardText -> onValueChanged(clipboardText.text ?: "") }
					else Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
				} catch (e : Exception) {
					Toast.makeText(context, "Error copying text from clipboard", Toast.LENGTH_SHORT).show()
				}
			},
			modifier = Modifier
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_paste),
				contentDescription = "Paste",
				tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
				modifier = Modifier.requiredSize(24.dp)
			)
		}

		IconButton(onClick = { onValueChanged("") }) {
			Icon(
				painter = painterResource(id = R.drawable.ic_close),
				contentDescription = "Clear text",
				tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
				modifier = Modifier
			)
		}
	}
}

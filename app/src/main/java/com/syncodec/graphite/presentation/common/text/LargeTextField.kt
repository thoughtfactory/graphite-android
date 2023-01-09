package com.syncodec.graphite.presentation.common.text

import android.widget.Toast
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
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.ui.IconButtonSize


@Composable
fun LargeTextField(
	modifier : Modifier = Modifier,
	value : String,
	placeholder : String,
	isFocused : Boolean,
	focusRequester : FocusRequester = FocusRequester(),
	onFocusChanged : (Boolean) -> Unit = {},
	keyboardOptions : KeyboardOptions = KeyboardOptions.Default,
	keyboardActions : KeyboardActions = KeyboardActions.Default,
	trailingIcon : Int? = null,
	onClickTrailingIcon : (() -> Unit)? = null,
	onValueChange : (String) -> Unit
) {
	val context = LocalContext.current
	val clipboardManager : ClipboardManager = LocalClipboardManager.current

	BasicTextField(
		value = value,
		onValueChange = onValueChange,
		maxLines = 1,
		textStyle = MaterialTheme.typography.bodyMedium,
		keyboardOptions = keyboardOptions,
		keyboardActions = keyboardActions,
		cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
		visualTransformation = VisualTransformation.None,
		modifier = modifier,
	) { innerTextField ->
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.height(40.dp)
				.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.71f), MaterialTheme.shapes.medium)
		) {
			Box(
				contentAlignment = Alignment.CenterStart,
				modifier = Modifier.weight(1f)
			) {
				androidx.compose.animation.AnimatedVisibility(
					visible = value.isEmpty(),
					enter = fadeIn(tween(300)),
					exit = fadeOut(tween(300)),
				) {
					Text(
						text = placeholder,
						color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.31f),
						style = MaterialTheme.typography.bodyMedium,
						modifier = Modifier.padding(12.dp, 0.dp)
					)
				}
				Box(
					contentAlignment = Alignment.CenterStart,
					modifier = Modifier.padding(12.dp, 0.dp)
				) {
					innerTextField()
				}
			}
			IconButton(
				colors = IconButtonDefaults.iconButtonColors(
					containerColor = Color.Companion.Transparent,
					contentColor = MaterialTheme.colorScheme.onSurface,
				),
				onClick = {
					try {
						if (clipboardManager.hasText()) clipboardManager.getText()?.let { clipboardText -> onValueChange(clipboardText.text) }
						else Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
					} catch (e : Exception) {
						Toast.makeText(context, "Error copying text from clipboard", Toast.LENGTH_SHORT).show()
					}
				},
				modifier = Modifier
					.requiredSize(32.dp)
					.padding(0.dp)
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_paste),
					contentDescription = "Paste",
					modifier = Modifier
						.requiredSize(IconButtonSize)
						.padding(2.dp)
				)
			}
			Spacer(modifier = Modifier.width(4.dp))
			onClickTrailingIcon?.let {
				IconButton(
					colors = IconButtonDefaults.iconButtonColors(
						containerColor = Color.Companion.Transparent,
						contentColor = MaterialTheme.colorScheme.onSurface,
					),
					onClick = it,
					modifier = Modifier
						.requiredSize(32.dp)
						.padding(0.dp)
				) {
					Icon(
						painter = painterResource(id = trailingIcon ?: R.drawable.ic_search),
						contentDescription = "Go",
						modifier = Modifier
							.requiredSize(IconButtonSize)
							.padding(2.dp)
					)
				}
			}
			Spacer(modifier = Modifier.width(4.dp))
		}
	}
}

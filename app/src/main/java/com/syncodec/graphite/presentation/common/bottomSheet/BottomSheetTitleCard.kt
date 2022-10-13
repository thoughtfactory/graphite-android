package com.syncodec.graphite.presentation.common.bottomSheet

import androidx.compose.animation.Crossfade
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BottomSheetTitleCard(
	title: String?,
	placeholder: String,
	onUpdateTitle: (String?) -> Unit
) {
	val keyboardController = LocalSoftwareKeyboardController.current
	val focusRequester = remember { FocusRequester() }
	var isFocused by remember { mutableStateOf(false) }

	var isEditing by remember { mutableStateOf(false) }
	var newTitle by remember { mutableStateOf(title) }

	val textColor = MaterialTheme.colorScheme.onBackground

	LaunchedEffect(key1 = title) {
		newTitle = title
	}

	LaunchedEffect(key1 = isEditing) {
		if (isEditing) focusRequester.requestFocus()
	}

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp)
			.clip(RoundedCornerShape(12.dp))
			.background(MaterialTheme.colorScheme.background),
		verticalAlignment = Alignment.CenterVertically
	) {
		Spacer(modifier = Modifier.width(12.dp))

		if (isEditing) {
			BasicTextField(
				value = newTitle ?: "",
				onValueChange = { newTitle = it },
				singleLine = true,
				keyboardOptions = KeyboardOptions.Default,
				keyboardActions = KeyboardActions.Default,
				cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
				textStyle = MaterialTheme.typography.titleMedium,
				visualTransformation = { text ->
					TransformedText(
						AnnotatedString(
							text.toString(),
							SpanStyle(color = textColor, fontWeight = FontWeight.Bold)
						),
						OffsetMapping.Identity
					)
				},
				modifier = Modifier
					.weight(1f)
					.focusRequester(focusRequester)
					.onFocusChanged { isFocused = it.isFocused }
			) { innerTextField ->
				innerTextField()

				if (newTitle.isNullOrBlank()) {
					Text(
						text = placeholder,
						style = MaterialTheme.typography.titleMedium,
						color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
						modifier = Modifier.weight(1f)
					)
				}
			}
		} else {
			if (newTitle.isNullOrBlank()) {
				Text(
					text = placeholder,
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
					modifier = Modifier.weight(1f)
				)
			} else {
				Text(
					text = title ?: "",
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.weight(1f)
				)
			}
		}

		Crossfade(targetState = isEditing) {
			if (it) {
				IconButton(
					onClick = {
						isEditing = false
						onUpdateTitle(newTitle)
						keyboardController?.hide()
					}
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_done),
						contentDescription = "Update bucket title",
						tint = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier.requiredSize(20.dp)
					)
				}
			} else {
				IconButton(
					onClick = {
						isEditing = true
						keyboardController?.show()
					}
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_pencil),
						contentDescription = "Edit bucket title",
						tint = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier.requiredSize(20.dp)
					)
				}
			}
		}
	}
}

package com.syncodec.graphite.presentation.search.composable.buildingBlock

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R


@Composable
fun SearchBar(
	onHitSearch : (String) -> Unit,
) {
	var query by rememberSaveable { mutableStateOf("") }

	var isFocused by remember { mutableStateOf(false) }
	val focusRequester = remember { FocusRequester() }

	val textColor = MaterialTheme.colorScheme.onSurface

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier.background(
			color = MaterialTheme.colorScheme.background,
			shape = RoundedCornerShape(8.dp)
		)
	) {
		Spacer(modifier = Modifier.width(12.dp))

		BasicTextField(
			value = query,
			onValueChange = { query = it },
			singleLine = true,
			keyboardOptions = KeyboardOptions.Default.copy(
				capitalization = KeyboardCapitalization.None,
				autoCorrect = true,
				keyboardType = KeyboardType.Text,
				imeAction = ImeAction.Search
			),
			keyboardActions = KeyboardActions(
				onSearch = {
					onHitSearch(query)
					query = ""
				}
			),
			cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
			modifier = Modifier
				.weight(1f)
				.padding(0.dp)
				.focusRequester(focusRequester)
				.onFocusChanged { isFocused = it.isFocused },
			textStyle = MaterialTheme.typography.bodyMedium,
			visualTransformation = { text ->
				TransformedText(
					AnnotatedString(
						text.toString(),
						SpanStyle(color = textColor, fontWeight = FontWeight.Bold)
					),
					OffsetMapping.Identity
				)
			},
			decorationBox = { innerTextField ->
				Crossfade(targetState = query.isEmpty() && ! isFocused) {
					if (it) {
						Text(
							text = "Search in notes",
							style = MaterialTheme.typography.bodyMedium,
							color = textColor.copy(alpha = 0.31f),
						)
					} else {
						innerTextField()
					}
				}
			}
		)

		IconButton(onClick = { query = "" }) {
			Icon(
				painter = painterResource(id = R.drawable.ic_close),
				contentDescription = "Clear text",
				tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
				modifier = Modifier
			)
		}
	}
}

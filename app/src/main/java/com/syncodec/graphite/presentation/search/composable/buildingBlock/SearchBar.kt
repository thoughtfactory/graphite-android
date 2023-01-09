package com.syncodec.graphite.presentation.search.composable.buildingBlock

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.ui.IconButtonSize


@Preview
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
	onClickBack : () -> Unit = {},
	onHitSearch : (String) -> Unit = {}
) {
	val keyboardController = LocalSoftwareKeyboardController.current

	var query by rememberSaveable { mutableStateOf("") }

	val textColor = MaterialTheme.colorScheme.onSurface

	TextField(
		value = query,
		textStyle = MaterialTheme.typography.bodyMedium,
		maxLines = 1,
		singleLine = true,
		onValueChange = { query = it },
		placeholder = { Text(text = "Search within notes",) },
		leadingIcon = {
			IconButton(
				onClick = onClickBack
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_back),
					contentDescription = "Search",
					tint = textColor,
					modifier = Modifier.requiredSize(IconButtonSize)
				)
			}
		},
		trailingIcon = {
			IconButton(
				onClick = {
					onHitSearch(query)
					keyboardController?.hide()
					query = ""
				}
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_search),
					contentDescription = "Search",
					tint = textColor,
					modifier = Modifier.requiredSize(IconButtonSize)
				)
			}
		},
		colors = TextFieldDefaults.textFieldColors(
			textColor = MaterialTheme.colorScheme.onBackground,
			containerColor = MaterialTheme.colorScheme.background,
			cursorColor = MaterialTheme.colorScheme.onBackground,
			placeholderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.31f),
		),
		modifier = Modifier.fillMaxWidth()
	)

//	Row(
//		verticalAlignment = Alignment.CenterVertically,
//		modifier = Modifier.background(
//			color = MaterialTheme.colorScheme.background,
//			shape = RoundedCornerShape(8.dp)
//		)
//	) {
//		Spacer(modifier = Modifier.width(12.dp))
//
//		BasicTextField(
//			value = query,
//			onValueChange = { query = it },
//			singleLine = true,
//			keyboardOptions = KeyboardOptions.Default.copy(
//				capitalization = KeyboardCapitalization.None,
//				autoCorrect = true,
//				keyboardType = KeyboardType.Text,
//				imeAction = ImeAction.Search
//			),
//			keyboardActions = KeyboardActions(
//				onSearch = {
//					onHitSearch(query)
//					query = ""
//					keyboardController?.hide()
//				}
//			),
//			cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
//			modifier = Modifier
//				.weight(1f)
//				.padding(0.dp)
//				.focusRequester(focusRequester)
//				.onFocusChanged { isFocused = it.isFocused },
//			textStyle = MaterialTheme.typography.bodyMedium,
//			visualTransformation = { text ->
//				TransformedText(
//					AnnotatedString(
//						text.toString(),
//						SpanStyle(color = textColor, fontWeight = FontWeight.Bold)
//					),
//					OffsetMapping.Identity
//				)
//			},
//			decorationBox = { innerTextField ->
//				Crossfade(targetState = query.isEmpty() && ! isFocused) {
//					if (it) {
//						Text(
//							text = "Search in notes",
//							style = MaterialTheme.typography.bodyMedium,
//							color = textColor.copy(alpha = 0.31f),
//						)
//					} else {
//						innerTextField()
//					}
//				}
//			}
//		)
//
//		IconButton(onClick = { query = "" }) {
//			Icon(
//				painter = painterResource(id = R.drawable.ic_close),
//				contentDescription = "Clear text",
//				tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
//				modifier = Modifier
//			)
//		}
//	}
}

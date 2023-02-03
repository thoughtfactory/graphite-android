package com.syncodec.graphite.presentation.explorer.screen.searchScreen.buildingBlock

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton


@Preview
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
	onClickBack : () -> Unit = {},
	onHitSearch : (String) -> Unit = {}
) {
	val keyboardController = LocalSoftwareKeyboardController.current

	var query by rememberSaveable { mutableStateOf("") }

	TextField(
		value = query,
		textStyle = MaterialTheme.typography.bodyMedium,
		maxLines = 1,
		singleLine = true,
		onValueChange = { query = it },
		placeholder = { Text(text = "Search within notes") },
		leadingIcon = {
			MenuButton(
				icon = R.drawable.ic_back,
				tooltip = "Back",
				onClick = onClickBack,
			)
		},
		trailingIcon = {
			MenuButton(
				icon = R.drawable.ic_search,
				tooltip = "Search",
			) {
				onHitSearch(query)
				keyboardController?.hide()
				query = ""
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
}

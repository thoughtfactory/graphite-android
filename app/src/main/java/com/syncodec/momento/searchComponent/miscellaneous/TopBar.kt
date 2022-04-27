package com.syncodec.momento.searchComponent.miscellaneous

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.syncodec.momento.R
import com.syncodec.momento.searchComponent.SearchActivity


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TopBar(
	query: String,
	onAction: (SearchActivity.Action, Any?) -> Unit
) {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		Bar(
			query = query,
			onQueryChange = { onAction(SearchActivity.Action.UPDATE_QUERY, it) },
			onHitSearch = { onAction(SearchActivity.Action.HIT_SEARCH, null) },
		)


		Spacer(modifier = Modifier.height(6.dp))

//		Filter(
//			showFavorite = showFavorite,
//			showArchived = showArchived,
//			showLocked = showLocked,
//		) { action, data -> onAction(action, data) }

		Spacer(modifier = Modifier.height(6.dp))
	}
}

@Composable
private fun Bar(
	query: String,
	onQueryChange: (String) -> Unit,
	onHitSearch: () -> Unit
) {
	SmallTopAppBar(
		navigationIcon = {
			IconButton(
				onClick = { }
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_back),
					contentDescription = "Back",
					tint = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier
						.requiredSize(32.dp)
						.padding(4.dp)
				)
			}
		},
		title = {
			SearchField(
				query = query,
				onQueryChange = onQueryChange,
				onHitSearch = onHitSearch
			)
		},
	)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SearchField(
	query: String,
	onQueryChange: (String) -> Unit,
	onHitSearch: () -> Unit
) {
	val keyboardController = LocalSoftwareKeyboardController.current
	val focusRequester = remember { FocusRequester() }

	androidx.compose.material.Card(
		elevation = 0.dp,
		shape = RoundedCornerShape(12.dp),
		backgroundColor = MaterialTheme.colorScheme.background,
		modifier = Modifier
			.fillMaxWidth()
			.height(40.dp)
	) {
		Row(
			modifier = Modifier.fillMaxSize(),
			verticalAlignment = Alignment.CenterVertically
		) {
			Spacer(modifier = Modifier.width(12.dp))
			BasicTextField(
				value = query,
				onValueChange = { onQueryChange(it) },
				singleLine = true,
				cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
				textStyle = MaterialTheme.typography.bodyMedium.copy(
					color = MaterialTheme.colorScheme.onBackground,
					fontWeight = FontWeight.Bold
				),
				modifier = Modifier
					.weight(1f)
					.height(48.dp)
					.clip(RoundedCornerShape(12.dp))
					.focusRequester(focusRequester),

				keyboardActions = KeyboardActions(
					onSearch = { onHitSearch() }
				),
				keyboardOptions = KeyboardOptions(
					imeAction = ImeAction.Search
				),
				decorationBox = { innerTextField ->
					androidx.compose.material.Card(
						modifier = Modifier.fillMaxWidth(),
						backgroundColor = Color.Transparent,
						elevation = 0.dp,
						shape = RoundedCornerShape(12.dp),
					) {
						Box(
							contentAlignment = Alignment.CenterStart,
							modifier = Modifier
								.fillMaxWidth()
						) {
							if (query.isEmpty()) {
								Text(
									text = "Search for diary, notes, bucket etc...",
									style = MaterialTheme.typography.bodyMedium,
									color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
									fontWeight = FontWeight.Bold,
									maxLines = 1
								)
							}
							innerTextField()
						}
					}
				}
			)

			Spacer(modifier = Modifier.width(8.dp))

			IconButton(onClick = { onQueryChange("") }) {
				Icon(
					painter = painterResource(id = R.drawable.ic_close),
					contentDescription = "Clear search query",
					tint = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.requiredSize(16.dp)
				)
			}
		}
	}

	LaunchedEffect(key1 = Unit) {
		focusRequester.requestFocus()
		keyboardController?.show()
	}
}

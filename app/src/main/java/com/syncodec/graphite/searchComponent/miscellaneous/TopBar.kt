package com.syncodec.graphite.searchComponent.miscellaneous

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.syncodec.graphite.R
import com.syncodec.graphite.searchComponent.SearchActivity
import kotlinx.coroutines.delay
import kotlin.random.Random


@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun TopBar(
	query: String,
	queryTagList: List<String>,
	queryStringList: List<String>,
	onAction: (SearchActivity.Action, Any?) -> Unit,
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface)
	) {
		Bar(
			query = query,
			onBack = { onAction(SearchActivity.Action.BACK, null) },
			onQueryChange = { onAction(SearchActivity.Action.UPDATE_QUERY, it) },
			onHitSearch = { onAction(SearchActivity.Action.HIT_SEARCH, null) },
		)

		QueryListCard(
			queryList = queryStringList,
			icon = R.drawable.ic_search,
			addExtra = false,
			onClick = { onAction(SearchActivity.Action.CLICK_STRING, it) },
			onClickExtra = {}
		)
		AnimatedVisibility(
			visible = queryStringList.isNotEmpty(),
			enter = expandVertically(tween(600)) + scaleIn(tween(600)),
			exit = shrinkVertically(tween(600)) + scaleOut(tween(600))
		) {
			Spacer(modifier = Modifier.height(8.dp))
		}

		QueryListCard(
			queryList = queryTagList,
			icon = R.drawable.ic_hashtag,
			addExtra = true,
			onClick = { onAction(SearchActivity.Action.CLICK_TAG, it) },
			onClickExtra = { onAction(SearchActivity.Action.SHOW_TAG_SCREEN, null) }
		)
		AnimatedVisibility(
			visible = queryTagList.isNotEmpty(),
			enter = expandVertically(tween(600)) + scaleIn(tween(600)),
			exit = shrinkVertically(tween(600)) + scaleOut(tween(600))
		) {
			Spacer(modifier = Modifier.height(8.dp))
		}
	}
}

@Composable
private fun Bar(
	query: String,
	onBack: () -> Unit,
	onQueryChange: (String) -> Unit,
	onHitSearch: () -> Unit
) {
	SmallTopAppBar(
		navigationIcon = {
			IconButton(
				onClick = { onBack() }
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
									text = "Search within notes...",
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

@Composable
private fun QueryListCard(
	queryList: List<String>,
	icon: Int,
	addExtra: Boolean,
	onClick: (String) -> Unit,
	onClickExtra: () -> Unit
) {
	LazyRow(
		modifier = Modifier.fillMaxWidth()
	) {
		item { Spacer(modifier = Modifier.width(8.dp)) }
		queryList.forEach {
			item { QueryCard(query = it, icon = icon) { onClick(it) } }
			item { Spacer(modifier = Modifier.width(6.dp)) }
		}
		if (addExtra && queryList.isNotEmpty()) {
			item { QueryCard(query = null, icon = R.drawable.ic_add) { onClickExtra() } }
			item { Spacer(modifier = Modifier.width(6.dp)) }
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
private fun QueryCard(
	query: String?,
	icon: Int,
	onClick: () -> Unit
) {
	var isVisible by remember { mutableStateOf(false) }
	var updateVisibility by remember { mutableStateOf(Random.nextInt()) }
	LaunchedEffect(
		key1 = updateVisibility
	) {
		if (isVisible) {
			isVisible = false
			delay(600)
			onClick()
		} else {
			isVisible = true
		}
	}

	AnimatedVisibility(
		visible = isVisible,
		enter = expandVertically(tween(600)) + scaleIn(tween(600)),
		exit = shrinkVertically(tween(600)) + scaleOut(tween(600))
	) {
		Box(
			modifier = Modifier
				.clip(RoundedCornerShape(50))
				.background(MaterialTheme.colorScheme.primary)
				.clickable { onClick() }
		) {
			Row(
				modifier = Modifier.padding(12.dp, 8.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					painter = painterResource(id = icon),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onPrimary,
					modifier = Modifier.requiredSize(16.dp)
				)
				if (query != null) {
					Spacer(modifier = Modifier.width(6.dp))
					Text(
						text = query,
						color = MaterialTheme.colorScheme.onPrimary,
						style = MaterialTheme.typography.bodyMedium,
					)
				}
			}
		}
	}
}

package com.syncodec.graphite.searchComponent.miscellaneous

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.custom.LargeTextField
import com.syncodec.graphite.searchComponent.SearchActivity
import kotlinx.coroutines.delay
import kotlin.random.Random


@OptIn(ExperimentalAnimationApi::class)
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
	var isSearchViewFocused by remember { mutableStateOf(false) }

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
			LargeTextField(
				text = query,
				placeholder = "Search within notes...",
				isFocused = isSearchViewFocused,
				onFocusChanged = { isSearchViewFocused = it },
				keyboardOptions = KeyboardOptions.Default.copy(
					capitalization = KeyboardCapitalization.None,
					autoCorrect = true,
					keyboardType = KeyboardType.Text,
					imeAction = ImeAction.Search
				),
				keyboardActions = KeyboardActions(
					onSearch = { onHitSearch() }
				),
				onValueChanged = onQueryChange
			)
//			SearchField(
//				query = query,
//				onQueryChange = onQueryChange,
//				onHitSearch = onHitSearch
//			)
		},
	)
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
		}
		if (addExtra && queryList.isNotEmpty()) {
			item { QueryCard(query = null, icon = R.drawable.ic_add) { onClickExtra() } }
		}
	}
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun QueryCard(
	query: String?,
	icon: Int,
	onClick: () -> Unit
) {
	var isVisible by remember { mutableStateOf(false) }
	val updateVisibility by remember { mutableStateOf(Random.nextInt()) }
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
		FilterChip(
			selectedIcon = {
				Icon(
					painter = painterResource(id = icon),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onPrimary,
					modifier = Modifier.requiredSize(16.dp)
				)
			},
			label = {
				if (query != null) {
					Text(
						text = query,
						color = MaterialTheme.colorScheme.onPrimary,
						style = MaterialTheme.typography.bodyMedium,
					)
				}
			},
			selected = true,
			enabled = true,
			onClick = { onClick() },
			modifier = Modifier.padding(4.dp, 0.dp)
		)
//		Row(
//			modifier = Modifier,
//			verticalAlignment = Alignment.CenterVertically
//		) {
//			Box(
//				modifier = Modifier
//					.clip(RoundedCornerShape(50))
//					.background(MaterialTheme.colorScheme.primary)
//					.clickable { onClick() }
//			) {
//				Row(
//					modifier = Modifier.padding(12.dp, 8.dp),
//					verticalAlignment = Alignment.CenterVertically
//				) {
//					Icon(
//						painter = painterResource(id = icon),
//						contentDescription = null,
//						tint = MaterialTheme.colorScheme.onPrimary,
//						modifier = Modifier.requiredSize(16.dp)
//					)
//					if (query != null) {
//						Spacer(modifier = Modifier.width(6.dp))
//						Text(
//							text = query,
//							color = MaterialTheme.colorScheme.onPrimary,
//							style = MaterialTheme.typography.bodyMedium,
//						)
//					}
//				}
//			}
//
//			Spacer(modifier = Modifier.width(6.dp))
//
//			Text(
//				text = "OR",
//				style = MaterialTheme.typography.bodyMedium,
//				color = MaterialTheme.colorScheme.onSurface
//			)
//
//			Spacer(modifier = Modifier.width(6.dp))
//		}
	}
}

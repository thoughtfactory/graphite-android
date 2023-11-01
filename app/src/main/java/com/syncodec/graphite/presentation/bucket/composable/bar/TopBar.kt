package com.syncodec.graphite.presentation.bucket.composable.bar

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.BucketType
import com.syncodec.graphite.di.model.local.bucketTypeIconMap
import com.syncodec.graphite.presentation.common.button.BackButton
import com.syncodec.graphite.presentation.common.button.CancelButton
import com.syncodec.graphite.presentation.common.button.FavouriteButton
import com.syncodec.graphite.presentation.common.button.GenericButton
import com.syncodec.graphite.presentation.common.button.GenericButtonDefaults
import com.syncodec.graphite.presentation.common.button.LockButton
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.SearchButton
import com.syncodec.graphite.presentation.common.tab.GenericTabRow
import com.syncodec.graphite.presentation.common.tab.TabItem
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun TopBar(
	title: String? = null,
	isFavourite: Boolean = false,
	isLocked: Boolean = false,
	isSearching: Boolean = false,
	isSelecting: Boolean = false,
	bucketType: BucketType = BucketType.UNKNOWN,
	pagerState: PagerState = rememberPagerState { 4 },
	searchQueryList: Set<String> = setOf(),
	onClickFavourite: () -> Unit = {},
	onClickLock: () -> Unit = {},
	onClickSearch: () -> Unit = {},
	onClickMenuButton: () -> Unit = {},
	addSearchQuery: (String) -> Unit = {},
	removeSearchQuery: (String) -> Unit = {},
) {

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.background)
	) {
		AnimatedVisibility(
			visible = !isSelecting,
			enter = expandVertically(tween(470)),
			exit = shrinkVertically(tween(470)),
			label = "isSelecting_animation"
		) {
			AnimatedContent(
				targetState = isSearching,
				label = "bar_animation"
			) {
				if (it) {
					BucketSearchBar(
						title = title,
						searchQueryList = searchQueryList,
						addSearchQuery = addSearchQuery,
						removeSearchQuery = removeSearchQuery,
					)
				} else {
					NormalTopBar(
						title = title,
						isFavourite = isFavourite,
						isLocked = isLocked,
						onClickFavourite = onClickFavourite,
						onClickLock = onClickLock,
						onClickSearch = onClickSearch,
						onClickMenuButton = onClickMenuButton,
					)
				}
			}
		}

		AnimatedVisibility(
			visible = !isSelecting && bucketType != BucketType.LINK,
			enter = expandVertically(tween(470)),
			exit = shrinkVertically(tween(470))
		) {
			StateView(
				bucketType = bucketType,
				pagerState = pagerState
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun NormalTopBar(
	title: String? = null,
	isFavourite: Boolean = false,
	isLocked: Boolean = false,
	onClickFavourite: () -> Unit = {},
	onClickLock: () -> Unit = {},
	onClickSearch: () -> Unit = {},
	onClickMenuButton: () -> Unit = {},
) {
	TopAppBar(
		navigationIcon = { BackButton() },
		title = {
			Text(
				text = title ?: stringResource(id = R.string.untitled),
				fontStyle = if (title == null) FontStyle.Italic else FontStyle.Normal
			)
		},
		actions = {
			LockButton(
				isLocked = isLocked,
				onClick = onClickLock,
			)

			FavouriteButton(
				isFavourite = isFavourite,
				onClick = onClickFavourite,
			)

			SearchButton(onClick = onClickSearch)

			MenuButton(onClick = onClickMenuButton)
		},
		colors = TopAppBarDefaults.topAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
			titleContentColor = MaterialTheme.colorScheme.onSurface,
			actionIconContentColor = MaterialTheme.colorScheme.onSurface,
		)
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun BucketSearchBar(
	title: String? = null,
	searchQueryList: Set<String> = setOf(),
	addSearchQuery: (String) -> Unit = {},
	removeSearchQuery: (String) -> Unit = {},
) {
	var searchQuery by remember { mutableStateOf("") }
	val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		SearchBar(
			query = searchQuery,
			onQueryChange = { searchQuery = it },
			onSearch = { addSearchQuery(it); searchQuery = "" },
			active = false,
			onActiveChange = {},
			placeholder = { Text(text = "Search within \"$title\"") },
			leadingIcon = { CancelButton { onBackPressedDispatcher?.onBackPressed() } },
			trailingIcon = {
				GenericButton(
					icon = R.drawable.ic_fa_search,
					colors = GenericButtonDefaults.bottomBarColorWhite(),
					onClick = { addSearchQuery(searchQuery); searchQuery = "" }
				)
			},
			tonalElevation = 0.dp,
			colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
			modifier = Modifier.fillMaxWidth(),
			content = {}
		)
		AnimatedVisibility(
			visible = searchQueryList.isNotEmpty(),
			enter = expandVertically(tween(470)),
			exit = shrinkVertically(tween(470)),
			label = "searchQueryList_animation"
		) {
			Column {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.horizontalScroll(rememberScrollState())
				) {
					Spacer(modifier = Modifier.width(16.dp))
					searchQueryList.forEach {
						SuggestionChip(
							label = { Text(text = it) },
							onClick = { removeSearchQuery(it) }
						)
						Spacer(modifier = Modifier.width(12.dp))
					}
					Spacer(modifier = Modifier.width(4.dp))
				}
				Spacer(modifier = Modifier.height(8.dp))
			}
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StateView(
	bucketType: BucketType,
	pagerState: PagerState = rememberPagerState { 4 }
) {
	val scope = rememberCoroutineScope()

	val stateTodo = when (bucketType) {
		BucketType.TODO -> "To do"
		BucketType.BOOK -> "To read"
		BucketType.SHOW -> "To watch"
		BucketType.LINK -> "To visit"
		BucketType.UNKNOWN -> "To do"
	}

	val stateDoing = when (bucketType) {
		BucketType.TODO -> "Doing"
		BucketType.BOOK -> "Reading"
		BucketType.SHOW -> "Watching"
		BucketType.LINK -> "Opened"
		BucketType.UNKNOWN -> "To do"
	}

	val stateDone = when (bucketType) {
		BucketType.TODO -> "Done"
		BucketType.BOOK -> "Read"
		BucketType.SHOW -> "Watched"
		BucketType.LINK -> "Done"
		BucketType.UNKNOWN -> "Done"
	}

	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		GenericTabRow(
			tabItemList = listOf(
				TabItem(text = "All", icon = R.drawable.ic_fa_circle_dot_duotone) { scope.launch { pagerState.animateScrollToPage(0) } },
				TabItem(text = stateTodo, icon = R.drawable.ic_fa_clock) { scope.launch { pagerState.animateScrollToPage(1) } },
				TabItem(text = stateDoing, icon = bucketTypeIconMap[bucketType] ?: R.drawable.ic_fa_question) { scope.launch { pagerState.animateScrollToPage(2) } },
				TabItem(text = stateDone, icon = R.drawable.ic_fa_circle_check) { scope.launch { pagerState.animateScrollToPage(3) } },
			),
			selectedTabIndex = pagerState.currentPage,
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 12.dp)
		)
		Spacer(modifier = Modifier.height(8.dp))
	}
}

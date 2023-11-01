package com.syncodec.graphite.presentation.search.composable.bar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.common.button.BackButton
import com.syncodec.graphite.presentation.common.button.SearchButton
import com.syncodec.graphite.presentation.search.SearchViewModel
import com.syncodec.graphite.presentation.base.AttachmentContainer
import com.syncodec.graphite.presentation.base.AttachmentContent
import com.syncodec.graphite.presentation.base.FavouriteContainer
import com.syncodec.graphite.presentation.base.FavouriteContent
import com.syncodec.graphite.presentation.base.LockClosedContainer
import com.syncodec.graphite.presentation.base.LockClosedContent
import com.syncodec.graphite.utils.getInverseBWColor


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TopBar(
	currentFilterList: Set<SearchViewModel.Companion.NoteFilter> = setOf(),
	onAddFilter: (SearchViewModel.Companion.NoteFilter) -> Unit = {},
	onRemoveFilter: (SearchViewModel.Companion.NoteFilter) -> Unit = {},
) {

	var query by remember { mutableStateOf("") }

	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		SearchBar(
			query = query,
			onQueryChange = { query = it },
			onSearch = {
				if (it.isNotEmpty()) onAddFilter(SearchViewModel.Companion.NoteFilter.Query(word = it))
				query = ""
			},
			active = false,
			onActiveChange = {},
			colors = SearchBarDefaults.colors(
				containerColor = MaterialTheme.colorScheme.background
			),
			leadingIcon = { BackButton() },
			placeholder = { Text(text = "Search in notes") },
			trailingIcon = {
				SearchButton {
					if (query.isNotEmpty()) onAddFilter(SearchViewModel.Companion.NoteFilter.Query(word = query))
					query = ""
				}
			},
			modifier = Modifier.fillMaxWidth()
		) {
		}
		FilterListView(
			currentFilterList = currentFilterList,
			onClickFilter = onRemoveFilter
		)
		Divider()
	}
}

@Preview
@Composable
private fun FilterListView(
	currentFilterList: Set<SearchViewModel.Companion.NoteFilter> = setOf(),
	onClickFilter: (SearchViewModel.Companion.NoteFilter) -> Unit = {},
) {
	AnimatedVisibility(
		visible = currentFilterList.isNotEmpty(),
		enter = expandVertically(tween(ANIMATION_DURATION_MILLIS)),
		exit = shrinkVertically(tween(ANIMATION_DURATION_MILLIS)),
		modifier = Modifier.fillMaxWidth()
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.horizontalScroll(rememberScrollState())
		) {
			Spacer(modifier = Modifier.width(12.dp))
			currentFilterList.forEach { noteFilter ->
				SuggestionChip(
					icon = {
						Icon(
							painter = when (noteFilter) {
								is SearchViewModel.Companion.NoteFilter.Favourite -> painterResource(id = R.drawable.ic_fa_heart)
								is SearchViewModel.Companion.NoteFilter.Locked -> painterResource(id = R.drawable.ic_fa_lock_close)
								is SearchViewModel.Companion.NoteFilter.WithAttachment -> painterResource(id = R.drawable.ic_fa_gallery)
								is SearchViewModel.Companion.NoteFilter.Query -> painterResource(id = R.drawable.ic_fa_search)
								is SearchViewModel.Companion.NoteFilter.Tag -> painterResource(id = R.drawable.ic_fa_tag)
							},
							contentDescription = null,
							modifier = Modifier.requiredSize(16.dp)
						)
					},
					label = {
						Text(
							text = when (noteFilter) {
								is SearchViewModel.Companion.NoteFilter.Favourite -> stringResource(id = R.string.favourite)
								is SearchViewModel.Companion.NoteFilter.Locked -> stringResource(id = R.string.locked)
								is SearchViewModel.Companion.NoteFilter.WithAttachment -> stringResource(id = R.string.with_attachments)
								is SearchViewModel.Companion.NoteFilter.Query -> noteFilter.word
								is SearchViewModel.Companion.NoteFilter.Tag -> noteFilter.tagObject.tag
							}
						)
					},
					border = null,
					colors = SuggestionChipDefaults.suggestionChipColors(
						containerColor = when (noteFilter) {
							is SearchViewModel.Companion.NoteFilter.Favourite -> Color.FavouriteContainer
							is SearchViewModel.Companion.NoteFilter.Locked -> Color.LockClosedContainer
							is SearchViewModel.Companion.NoteFilter.WithAttachment -> Color.AttachmentContainer
							is SearchViewModel.Companion.NoteFilter.Query -> MaterialTheme.colorScheme.primary
							is SearchViewModel.Companion.NoteFilter.Tag -> Color(noteFilter.tagObject.color)
						},
						labelColor = when (noteFilter) {
							is SearchViewModel.Companion.NoteFilter.Favourite -> Color.FavouriteContent
							is SearchViewModel.Companion.NoteFilter.Locked -> Color.LockClosedContent
							is SearchViewModel.Companion.NoteFilter.WithAttachment -> Color.AttachmentContent
							is SearchViewModel.Companion.NoteFilter.Query -> MaterialTheme.colorScheme.onPrimary
							is SearchViewModel.Companion.NoteFilter.Tag -> Color(noteFilter.tagObject.color).getInverseBWColor()
						},
						iconContentColor = when (noteFilter) {
							is SearchViewModel.Companion.NoteFilter.Favourite -> Color.FavouriteContent
							is SearchViewModel.Companion.NoteFilter.Locked -> Color.LockClosedContent
							is SearchViewModel.Companion.NoteFilter.WithAttachment -> Color.AttachmentContent
							is SearchViewModel.Companion.NoteFilter.Query -> MaterialTheme.colorScheme.onPrimary
							is SearchViewModel.Companion.NoteFilter.Tag -> Color(noteFilter.tagObject.color).getInverseBWColor()
						},
					),
					onClick = { onClickFilter(noteFilter) }
				)
				Spacer(modifier = Modifier.width(6.dp))
			}
			Spacer(modifier = Modifier.width(6.dp))
		}
	}
}

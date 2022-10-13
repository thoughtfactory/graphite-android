package com.syncodec.graphite.presentation.search.composable.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.presentation.search.SearchActivity
import com.syncodec.graphite.presentation.search.SearchViewModel
import com.syncodec.graphite.presentation.search.composable.bar.TopBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {

	val activity : SearchActivity = LocalContext.current as SearchActivity

	val viewModel : SearchViewModel = viewModel()

	val showResultScreen by viewModel.showResultScreen
	val tagList by viewModel.tagList.collectAsState(initial = listOf())
	val visibleNote = viewModel.visibleNoteList

	val tag by viewModel.showTag.collectAsState(initial = null)
	val query by viewModel.searchQuery.collectAsState(initial = null)

	Scaffold(
		modifier = Modifier.fillMaxSize(),
		topBar = {
			TopBar(
				tag = tag?.tag,
				query = query,
				onClickBack = { activity.onBackPressed() },
				onHitSearch = { viewModel.searchInNotes(it) }
			)
		}
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(it)
		) {
			Crossfade(targetState = showResultScreen) {
				if (it) {
					SearchResultScreen(
						visibleNote = visibleNote,
					)
				} else {
					TagScreen(
						tagList = tagList,
						onClickFavorite = { viewModel.showFavourite()},
						onClickWithAttachments = { viewModel.showWithAttachments() },
						onClickTag = { viewModel.showTag(it) }
					)
				}
			}
		}
	}
}

package com.syncodec.graphite.presentation.search.composable.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.search.SearchActivity
import com.syncodec.graphite.presentation.search.composable.bar.BottomBar
import com.syncodec.graphite.presentation.search.composable.bar.TopBar
import com.syncodec.graphite.presentation.search.composable.dialog.Dialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
	showResultScreen: Boolean,
	tagList: List<TagObject>,
	visibleNote: List<NoteObject>,
	tag: TagObject?,
	query: String?,
	onClickBack: () -> Unit
) {

	val showFavourite = SearchActivity.onShowFavourite.current
	val showWithAttachment = SearchActivity.onShowWithAttachment.current
	val showLocked = SearchActivity.onShowLocked.current
	val onShowTag = SearchActivity.onShowTag.current
	val onShowQuery = SearchActivity.onShowQuery.current

	Scaffold(
		modifier = Modifier.fillMaxSize(),
		topBar = {
			TopBar(
				tagObject = tag,
				query = query,
				onClickBack = onClickBack,
				onHitSearch = onShowQuery
			)
		},
		bottomBar = { BottomBar() }
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
						onClickFavorite = showFavourite,
						onClickWithAttachments = showWithAttachment,
						onClickLocked = showLocked,
						onClickTag = onShowTag
					)
				}
			}
		}
	}

	Dialog()
}

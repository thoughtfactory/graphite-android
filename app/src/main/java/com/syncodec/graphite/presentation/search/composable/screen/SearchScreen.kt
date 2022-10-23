package com.syncodec.graphite.presentation.search.composable.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.search.SearchActivity
import com.syncodec.graphite.presentation.search.composable.bar.TopBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
	showResultScreen: Boolean,
	tagList: List<TagObject>,
	visibleNote: List<NoteObject>,
	tag: TagObject?,
	query: String?,
) {

	val activity : SearchActivity = LocalContext.current as SearchActivity

	Scaffold(
		modifier = Modifier.fillMaxSize(),
		topBar = {
			TopBar(
				tag = tag?.tag,
				query = query,
				onClickBack = { activity.onBackPressed() },
				onHitSearch = {  }
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
						onClickFavorite = { },
						onClickWithAttachments = {  },
						onClickTag = {  }
					)
				}
			}
		}
	}
}

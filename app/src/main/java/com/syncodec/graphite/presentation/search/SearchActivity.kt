package com.syncodec.graphite.presentation.search

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.search.composable.SearchScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import org.koin.androidx.viewmodel.ext.android.viewModel


class SearchActivity : ComponentActivity() {

	val viewModel: SearchViewModel by viewModel()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			BaseContent {

//				val tagList by viewModel.tagList.collectAsState()
				val tagList = setOf(TagObject.getRandomInstance(), TagObject.getRandomInstance(), TagObject.getRandomInstance(), TagObject.getRandomInstance(), TagObject.getRandomInstance())

				val filteredNoteList by viewModel.filteredNoteList.collectAsState(initial = setOf())
				val currentFilterList by viewModel.noteFilterList.collectAsState()

				BackHandler(enabled = currentFilterList.isNotEmpty()) { viewModel.removeAllFilter() }

				SearchScreen(
					tagList = tagList,
					filteredNoteList = filteredNoteList,
					currentFilterList = currentFilterList,
					onAddFilter = { viewModel.addFilter(it) },
					onRemoveFilter = { viewModel.removeFilter(it) },
				)
			}
		}
	}
}

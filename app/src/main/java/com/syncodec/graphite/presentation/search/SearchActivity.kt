package com.syncodec.graphite.presentation.search

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.syncodec.graphite.presentation.search.composable.SearchScreen
import com.syncodec.graphite.presentation.base.BaseComposable
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.RealmUUID
import org.koin.androidx.viewmodel.ext.android.viewModel


class SearchActivity : ComponentActivity() {

	val viewModel: SearchViewModel by viewModel()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val hasTagId = intent.hasExtra(Extra.Companion.Extra.TagId.name)
		if (hasTagId) {
			val tagId = try {
				intent.getByteArrayExtra(Extra.Companion.Extra.TagId.name)?.let { RealmUUID.from(it) }
			} catch (_: Exception) {
				null
			}
			tagId?.let { viewModel.searchAndAddTagFilter(id = it) }
		}

		setContent {
			BaseComposable {

				val tagList by viewModel.tagList.collectAsState()

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

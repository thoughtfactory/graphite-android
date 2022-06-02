package com.syncodec.graphite.searchComponent

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.Graphite
import com.syncodec.graphite.konstant.Konstant
import com.syncodec.graphite.noteComponent.NoteActivity
import com.syncodec.graphite.searchComponent.miscellaneous.TopBar
import com.syncodec.graphite.searchComponent.screen.NoteScreen
import com.syncodec.graphite.searchComponent.screen.TagScreen
import com.syncodec.graphite.ui.theme.GraphiteBase

class SearchActivity : ComponentActivity() {

	private val viewModel by viewModels<SearchViewModel>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		viewModel.showLocked =
			intent.getBooleanExtra(Konstant.Companion.Konstant.SHOW_LOCKED.name, false)

		setContent {
			GraphiteBase {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.surface)
				systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.background)

				viewModel.activityState = rememberActivityState()

				Screen()
			}
		}
	}

	private fun onPerformAction(action: Action, data: Any? = null) {
		val activityState = viewModel.activityState

		when (action) {
			Action.BACK -> finish()
			Action.UPDATE_QUERY -> activityState.query.value = data as String
			Action.HIT_SEARCH -> {
				val query by activityState.query
				val queryStringList = activityState.queryStringList
				if (!queryStringList.contains(query) && query.isNotBlank()) queryStringList.add(
					query
				)
				onPerformAction(Action.HIDE_TAG_SCREEN)
				activityState.query.value = ""
				viewModel.searchInNote()
			}
			Action.CLICK_TAG -> {
				data as String
				val queryTagList = activityState.queryTagList
				val queryStringList = activityState.queryStringList
				if (queryTagList.contains(data)) {
					queryTagList.remove(data)
				} else {
					queryTagList.add(data)
					onPerformAction(Action.HIDE_TAG_SCREEN)
				}

				viewModel.searchInTag()
				if (queryStringList.isEmpty() && queryTagList.isEmpty()) onPerformAction(Action.SHOW_TAG_SCREEN)
			}
			Action.CLICK_STRING -> {
				data as String
				val queryStringList = activityState.queryStringList
				val queryTagList = activityState.queryTagList
				queryStringList.remove(data)
				if (queryStringList.isEmpty() && queryTagList.isEmpty()) onPerformAction(Action.SHOW_TAG_SCREEN)
				viewModel.searchInNote()
			}
			Action.SHOW_TAG_SCREEN -> activityState.showTagScreen.value = true
			Action.HIDE_TAG_SCREEN -> activityState.showTagScreen.value = false
			Action.CLICK_NOTE -> {
				data as Pair<*, *>
				Intent(this, NoteActivity::class.java).apply {
					putExtra(
						Konstant.Companion.Konstant.NOTEBOOK_KEY.name,
						data.second as String
					)
					putStringArrayListExtra(
						Konstant.Companion.Konstant.CHAPTER_KEY.name,
						java.util.ArrayList()
					)
					putExtra(Konstant.Companion.Konstant.NOTE_KEY.name, data.first as String)
					putExtra(Konstant.Companion.Konstant.IS_VIEWER.name, true)
					putExtra(Konstant.Companion.Konstant.IS_NEW.name, false)
					putExtra(Konstant.Companion.Konstant.SHOW_ARCHIVED.name, false)
					putExtra(Konstant.Companion.Konstant.SHOW_LOCKED.name, false)
					startActivity(this)
				}
			}
		}
	}

	@OptIn(ExperimentalMaterial3Api::class)
	@Composable
	private fun Screen() {

		val activityState = viewModel.activityState
		val query by activityState.query

		val showTagScreen by activityState.showTagScreen
		val tagList = viewModel.tagList
		val noteList = viewModel.noteList
		val queryStringList = activityState.queryStringList
		val queryTagList = activityState.queryTagList

		Scaffold(
			containerColor = MaterialTheme.colorScheme.background,
			topBar = {
				TopBar(
					query = query,
					queryTagList = queryTagList,
					queryStringList = queryStringList,
				) { action, data -> onPerformAction(action, data) }
			},
		) {
			Crossfade(targetState = showTagScreen) {
				if (it) {
					TagScreen(tagList = tagList) { onPerformAction(Action.CLICK_TAG, it) }
				} else {
					NoteScreen(noteList = noteList) { action, data ->
						onPerformAction(action, data)
					}
				}
			}
		}
	}

	inner class ActivityState {
		var query = mutableStateOf("")
		var vaultState = (application as Graphite).vaultState

		val queryStringList: SnapshotStateList<String> = mutableStateListOf()
		val queryTagList: SnapshotStateList<String> = mutableStateListOf()

		var showTagScreen = mutableStateOf(true)

		var showArchived = mutableStateOf(false)
		var showFavourite = mutableStateOf(false)
		var showLocked = mutableStateOf(false)

		var showDiary = mutableStateOf(false)
		var showNotebook = mutableStateOf(false)
		var showChapter = mutableStateOf(false)
		var showNote = mutableStateOf(false)
		var showBucket = mutableStateOf(false)
	}

	@Composable
	private fun rememberActivityState() = remember {
		ActivityState()
	}

	enum class Action {
		BACK,
		UPDATE_QUERY,
		HIT_SEARCH,
		CLICK_TAG,
		CLICK_STRING,
		SHOW_TAG_SCREEN,
		HIDE_TAG_SCREEN,
		CLICK_NOTE
	}
}

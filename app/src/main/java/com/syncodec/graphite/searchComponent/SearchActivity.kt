package com.syncodec.graphite.searchComponent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.Graphite
import com.syncodec.graphite.searchComponent.miscellaneous.TopBar
import com.syncodec.graphite.searchComponent.screen.NoteScreen
import com.syncodec.graphite.searchComponent.screen.TagScreen
import com.syncodec.graphite.ui.theme.GraphiteTheme

class SearchActivity : ComponentActivity() {

	private val viewModel by viewModels<SearchViewModel>()

	@OptIn(ExperimentalMaterialApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			GraphiteTheme {
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
				if (!queryStringList.contains(query) && query.isNotBlank()) queryStringList.add(query)
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

				if (queryStringList.isEmpty() && queryTagList.isEmpty()) onPerformAction(Action.SHOW_TAG_SCREEN)
			}
			Action.CLICK_STRING -> {
				data as String
				val queryStringList = activityState.queryStringList
				val queryTagList = activityState.queryTagList
				queryStringList.remove(data)
				if (queryStringList.isEmpty() && queryTagList.isEmpty()) onPerformAction(Action.SHOW_TAG_SCREEN)
			}
			Action.SHOW_TAG_SCREEN -> activityState.showTagScreen.value = true
			Action.HIDE_TAG_SCREEN -> activityState.showTagScreen.value = false
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
					NoteScreen(
						noteList = noteList
					)
				}
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	inner class ActivityState(
		val bottomSheetState: ModalBottomSheetState,
	) {
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

	@OptIn(ExperimentalMaterialApi::class)
	@Composable
	private fun rememberActivityState(
		bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
	) = remember {
		ActivityState(bottomSheetState)
	}

	enum class Action {
		BACK,
		UPDATE_QUERY,
		HIT_SEARCH,
		CLICK_TAG,
		CLICK_STRING,
		SHOW_TAG_SCREEN,
		HIDE_TAG_SCREEN,
	}
}

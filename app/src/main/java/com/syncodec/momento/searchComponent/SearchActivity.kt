package com.syncodec.momento.searchComponent

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.momento.Momento
import com.syncodec.momento.custom.notebook.NoteCard
import com.syncodec.momento.custom.notebook.NoteCardData
import com.syncodec.momento.custom.notebook.NotebookHeaderCard
import com.syncodec.momento.custom.notebook.NotebookTimelineSpacer
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.noteComponent.NoteActivity
import com.syncodec.momento.searchComponent.miscellaneous.SearchBar
import com.syncodec.momento.searchComponent.miscellaneous.filterData
import com.syncodec.momento.ui.theme.MomentoTheme
import java.util.regex.Pattern

class SearchActivity : ComponentActivity() {

	private val viewModel by viewModels<SearchViewModel>()
	val pattern = Pattern.compile("(\\\\\"text\\\\\":\\\\\")(([^\\\\\"]|\\\\\\\\\\\\\")*)(\\\\\")")

	@OptIn(ExperimentalMaterialApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		viewModel.readData()

		setContent {
			MomentoTheme {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.secondaryContainer)
				systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.background)

				viewModel.activityState = rememberActivityState()

				Screen()
			}
		}
	}

	@OptIn(ExperimentalFoundationApi::class)
	@Composable
	private fun Screen() {
		val context = LocalContext.current

		val query by viewModel.activityState.query
		val noteMap = viewModel.noteMap

		val vaultState by viewModel.activityState.vaultState
		var showArchived by viewModel.activityState.showArchived
		var showFavourite by viewModel.activityState.showFavourite
		var showLocked by viewModel.activityState.showLocked
		var showDiary by viewModel.activityState.showDiary
		var showNotebook by viewModel.activityState.showNotebook
		var showChapter by viewModel.activityState.showChapter
		var showNote by viewModel.activityState.showNote
		var showBucket by viewModel.activityState.showBucket
		val allState = !showArchived && !showFavourite && !showLocked
		val showAllComponent = !showDiary && !showNotebook && !showChapter && !showNote && !showBucket

		var visibleDiarySize by remember { mutableStateOf(noteMap.size) }
		var lastVisibleDiaryKey by remember { mutableStateOf<String?>(null) }

		LaunchedEffect(
			key1 = vaultState.hashCode() +
					showArchived.hashCode() +
					showFavourite.hashCode() +
					showLocked.hashCode() +
					showDiary.hashCode() +
					query.hashCode()
		) {
			Log.i("npr71", "refresh : ${showArchived.hashCode()}")
			visibleDiarySize = 0
			noteMap.forEach { (key, data) ->
				val showEntry: Boolean = filterData(
					showArchived = showArchived,
					isArchived = data.first.isArchived,
					showFavourite = showFavourite,
					isFavourite = data.first.isFavourite,
					showLocked = showLocked,
					isLocked = data.first.isLocked
				) && (data.first.content?.contains(query) == true) && (showDiary || showAllComponent)

				noteMap[key] = Pair(data.first, showEntry)
				if (showEntry) {
					visibleDiarySize++
					lastVisibleDiaryKey = key
				}
			}
		}

		Scaffold(
			modifier = Modifier
				.fillMaxSize(),
			backgroundColor = MaterialTheme.colorScheme.background,
			topBar = { SearchBar() },
		) {
			LazyColumn(
				modifier = Modifier
					.fillMaxSize(),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				item { Spacer(modifier = Modifier.height(12.dp)) }

				stickyHeader {
					NotebookHeaderCard(
						title = "Diary",
						noEntries = if (visibleDiarySize == 0) "No entries" else if (visibleDiarySize == 1) "1 entry" else "$visibleDiarySize entries"
					) {
						showDiary = !showDiary
					}
				}

				noteMap.forEach { (key, data) ->
					item {
						NoteCardData(
							timestamp = data.first.userTimestamp,
							showFullTime = true,
							isLocked = data.first.isLocked,
							isSelected = false,
							isArchived = data.first.isArchived,
							isFavourite = data.first.isFavourite,
							isDeleted = data.first.deletedTimestamp != -1L,
							isLast = key == lastVisibleDiaryKey,
							title = data.first.title,
							contentThumbnail = data.first.contentThumbnail,
							attachmentCount = data.first.attachmentCount,
							attachmentThumbnail = data.first.attachmentThumbnail,
							address = data.first.address,
							isVisible = data.second,
							onClick = {
								Intent(context, NoteActivity::class.java).apply {
									putExtra(Konstant.Companion.Konstant.IS_VIEWER.name, false)
									putExtra(Konstant.Companion.Konstant.DIARY_KEY.name, key)
									context.startActivity(this)
								}
							},
							onLongClick = null
						).apply {
							NoteCard(noteCardData = this)
						}

						NotebookTimelineSpacer(isVisible = data.second && key != lastVisibleDiaryKey)
					}
				}
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	inner class ActivityState(
		val bottomSheetState: ModalBottomSheetState,
	) {
		var query = mutableStateOf("")
		var vaultState = (application as Momento).vaultState

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
}

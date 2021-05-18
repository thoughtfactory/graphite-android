package com.syncodec.momento.noteComponent.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.syncodec.momento.custom.LoadingView
import com.syncodec.momento.database.note.Note
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.noteComponent.NoteViewModel
import com.syncodec.momento.noteComponent.miscellaneous.ViewerComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalPagerApi::class, kotlinx.coroutines.InternalCoroutinesApi::class)
@Composable
fun NoteViewerScreen() {
	val scope = rememberCoroutineScope()
	val noteViewModel: NoteViewModel = viewModel()

	val diaryKeyList by noteViewModel.noteRepository.diaryDbEntryKeyListLiveData.observeAsState()
	val pagerState = rememberPagerState()

	LaunchedEffect(key1 = diaryKeyList?.size) {
		scope.launch {
			val index = diaryKeyList?.indexOf(noteViewModel.viewerDiaryKey)
			if (index != null && index != -1) {
				pagerState.scrollToPage(page = index)
			}
		}
	}

	Crossfade(targetState = diaryKeyList.isNullOrEmpty()) {
		if (it) {
			LoadingView()
		} else {
			HorizontalPager(
				state = pagerState,
				count = diaryKeyList?.size ?: 0,
				reverseLayout = true,
				itemSpacing = 2.dp,
//		        userScrollEnabled = false,
				verticalAlignment = Alignment.Bottom,
				modifier = Modifier
					.fillMaxSize()
					.background(MaterialTheme.colorScheme.background),
			) { page ->
				Viewer(
					primaryKey = diaryKeyList!![page],
					onPrevious = {
						scope.launch {
							if (page != diaryKeyList!!.size - 1) {
								pagerState.animateScrollToPage(page = page + 1, 0f)
							}
						}
					},
					onNext = {
						scope.launch {
							if (page != 0) {
								pagerState.animateScrollToPage(page = page - 1, 0f)
							}
						}
					}
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun Viewer(
	primaryKey: String,
	onPrevious: () -> Unit,
	onNext: () -> Unit
) {
	val noteViewModel: NoteViewModel = viewModel()

	val scope = rememberCoroutineScope()

	var status by noteViewModel.status

	var diary by remember { mutableStateOf<Note?>(null) }

	LaunchedEffect(key1 = diary == null) {
		status = Status.LOADING
		scope.launch(Dispatchers.IO) {
			status = try {
				diary = noteViewModel.noteRepository.loadDiary(primaryKey = primaryKey)
				Status.LOADED
			} catch (exception: Exception) {
				Status.ERROR
			}
		}
	}

	Crossfade(
		targetState = status,
		animationSpec = tween(
			durationMillis = 400
		)
	) {
		when (it) {
			Status.INIT -> LoadingView()
			Status.LOADING -> LoadingView()
			Status.LOADED -> {
				if (diary != null) {
					diary!!.content?.let { it1 -> ViewerComponent(noteData = it1) }
				}
			}
			Status.SAVING -> {
			}
			Status.SAVED -> {
			}
			Status.SUCCESS -> {
			}
			Status.ERROR -> {
			}
		}
	}
}

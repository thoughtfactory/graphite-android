package com.syncodec.graphite.presentation.note.composable.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.syncodec.graphite.presentation.custom.LoadingView
import com.syncodec.graphite.presentation.note.NoteViewModel
import com.syncodec.graphite.presentation.note.composable.buildingBlock.ViewerComponent
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalPagerApi::class)
@Composable
fun ViewerScreen(
	noteIdList: List<ObjectId>
) {
	val scope = rememberCoroutineScope()
	val viewModel: NoteViewModel = viewModel()

	val isUserScrollEnabled by viewModel.isUserScrollEnabled

	val noteId by viewModel.noteId

	val pagerState = rememberPagerState(initialPage = maxOf(noteIdList.indexOf(noteId), 0))

	LaunchedEffect(key1 = noteIdList.getOrNull(pagerState.currentPage)) {
		noteIdList.getOrNull(pagerState.currentPage)?.let {
			scope.launch(Dispatchers.IO) {
				viewModel.getNote(it)
			}
		}
	}

	HorizontalPager(
		state = pagerState,
		count = noteIdList.size,
		reverseLayout = true,
		itemSpacing = 2.dp,
		userScrollEnabled = isUserScrollEnabled,
		verticalAlignment = Alignment.Bottom,
		modifier = Modifier.fillMaxSize()
	) { page ->
		val currentNoteId = noteIdList.getOrNull(page)
		Crossfade(targetState = currentNoteId == noteId) {
			if (it)
				Viewer()
			else
				LoadingView { if (currentNoteId != null) scope.launch(Dispatchers.IO) { viewModel.getNote(currentNoteId) } }
		}
	}
}

@Composable
private fun Viewer() {
	val viewModel: NoteViewModel = viewModel()

	val content by viewModel.content
	val userTimestamp by viewModel.userTimestamp
	val latLng by viewModel.latLng
	val address by viewModel.address
	val attachmentList = viewModel.attachmentListVisible

	if (userTimestamp != null) {
		ViewerComponent(
			content = content,
			userTimestamp = userTimestamp ?: 0,
			latLng = null,
			address = address,
			attachmentList = attachmentList,
			connectedTag = listOf()
		)
	}
}

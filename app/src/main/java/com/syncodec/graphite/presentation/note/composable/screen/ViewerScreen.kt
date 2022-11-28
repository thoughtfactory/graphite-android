package com.syncodec.graphite.presentation.note.composable.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.note.composable.LocalCompositionAddress
import com.syncodec.graphite.presentation.note.composable.LocalCompositionAttachmentList
import com.syncodec.graphite.presentation.note.composable.LocalCompositionContent
import com.syncodec.graphite.presentation.note.composable.LocalCompositionLatLng
import com.syncodec.graphite.presentation.note.composable.LocalCompositionNoteId
import com.syncodec.graphite.presentation.note.composable.LocalCompositionNoteIdList
import com.syncodec.graphite.presentation.note.composable.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionParentChapter
import com.syncodec.graphite.presentation.note.composable.LocalCompositionTagListBuffer
import com.syncodec.graphite.presentation.note.composable.LocalCompositionTitle
import com.syncodec.graphite.presentation.note.composable.LocalCompositionUserTimestamp
import com.syncodec.graphite.presentation.note.composable.LocalGetNote
import com.syncodec.graphite.presentation.note.composable.buildingBlock.ViewerComponent
import com.syncodec.graphite.presentation.note.composable.dialog.NoteDialogType
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalPagerApi::class)
@Composable
fun ViewerScreen() {
	val scope = rememberCoroutineScope()

	val noteId = LocalCompositionNoteId.current
	val noteIdList = LocalCompositionNoteIdList.current

	val getNote = LocalGetNote.current

	Crossfade(targetState = noteId) {
		if (it == null) {
			LoadingView()
		} else {
			val pagerState = rememberPagerState(initialPage = maxOf(noteIdList.indexOf(noteId), 0))
			LaunchedEffect(key1 = noteIdList.getOrNull(pagerState.currentPage)) {
				noteIdList.getOrNull(pagerState.currentPage)?.let {
					scope.launch(Dispatchers.IO) { getNote(it) }
				}
			}

			HorizontalPager(
				state = pagerState,
				count = noteIdList.size,
				reverseLayout = false,
				itemSpacing = 2.dp,
				userScrollEnabled = false,
				modifier = Modifier.fillMaxSize(),
			) { page ->
				val _currentNoteId = noteIdList.getOrNull(page)
				Crossfade(targetState = this.currentPageOffset == 0f) {
					if (it) Viewer(currentNoteId = _currentNoteId)
					else LoadingView()
				}
			}
		}
	}
}

@Composable
private fun Viewer(currentNoteId : RealmUUID?) {

	val noteId = LocalCompositionNoteId.current
	val title = LocalCompositionTitle.current
	val content = LocalCompositionContent.current
	val userTimestamp = LocalCompositionUserTimestamp.current
	val latLng = LocalCompositionLatLng.current
	val address = LocalCompositionAddress.current
	val attachmentList = LocalCompositionAttachmentList.current
//	val tagList by viewModel.tagObjectList.collectAsState(initial = listOf())
	val parentChapter = LocalCompositionParentChapter.current
	val getNote = LocalGetNote.current

	val tagList = LocalCompositionTagListBuffer.current

	val openDialog = LocalCompositionOpenDialog.current

	if (currentNoteId == noteId && noteId != null) {
		ViewerComponent(
			noteId = noteId,
			content = content,
			title = title,
			userTimestamp = userTimestamp ?: 0,
			latLng = latLng,
			address = address,
			parentChapter = parentChapter,
			attachmentList = attachmentList,
			connectedTag = tagList.map { it.toLite() }
		) { openDialog(NoteDialogType.CHAPTER_SELECTION, parentChapter?.id) }
	} else {
		LoadingView { getNote(currentNoteId ?: return@LoadingView) }
	}
}

package com.syncodec.momento.noteComponent.screen

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.syncodec.momento.custom.LoadingView
import com.syncodec.momento.database.attachment.AttachmentDbEntry
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.miscellaneous.logger
import com.syncodec.momento.noteComponent.NoteActivity
import com.syncodec.momento.noteComponent.miscellaneous.ViewerComponent


@OptIn(ExperimentalPagerApi::class, kotlinx.coroutines.InternalCoroutinesApi::class)
@Composable
fun NoteViewerScreen(
	noteKeyList: List<String>,
	noteDbEntry: NoteDbEntry?,
	connectedTag: List<String>,
	pagerState: PagerState,
	status: Status,
	onClick: (NoteActivity.Action) -> Unit
) {
	Crossfade(targetState = noteKeyList.isEmpty()) {
		if (it) {
			LoadingView()
		} else {
			HorizontalPager(
				state = pagerState,
				count = noteKeyList.size,
				reverseLayout = true,
				itemSpacing = 2.dp,
				userScrollEnabled = true,
				verticalAlignment = Alignment.Bottom,
				modifier = Modifier
					.fillMaxSize()
					.background(MaterialTheme.colorScheme.background),
			) { page ->
				Crossfade(targetState = noteDbEntry?.key == noteKeyList[page]) {
					if (it) {
						Viewer(
							status = status,
							noteDbEntry = noteDbEntry,
							connectedTag = connectedTag
						) { onClick(it) }
					} else {
						LoadingView()
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun Viewer(
	status: Status,
	noteDbEntry: NoteDbEntry?,
	connectedTag: List<String>,
	onAction: (NoteActivity.Action) -> Unit
) {
	Crossfade(
		targetState = status,
		animationSpec = tween(durationMillis = 600),
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surface)
	) {
		when (it) {
			Status.INIT -> LoadingView()
			Status.LOADING -> LoadingView()
			Status.LOADED -> if (noteDbEntry?.content != null)
				ViewerComponent(
					noteDbEntry = noteDbEntry,
					connectedTag = connectedTag
				) { onAction(it) }
			Status.ERROR -> {
			}
		}
	}
}

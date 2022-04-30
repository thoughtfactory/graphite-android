package com.syncodec.graphite.noteComponent.screen

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
import com.syncodec.graphite.custom.LoadingView
import com.syncodec.graphite.database.note.NoteDbEntry
import com.syncodec.graphite.konstant.Status
import com.syncodec.graphite.noteComponent.NoteActivity
import com.syncodec.graphite.noteComponent.miscellaneous.ViewerComponent
import org.json.JSONObject


@OptIn(ExperimentalPagerApi::class, kotlinx.coroutines.InternalCoroutinesApi::class)
@Composable
fun NoteViewerScreen(
	noteKeyList: List<String>,
	noteDbEntry: NoteDbEntry?,
	noteContent: JSONObject?,
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
							noteContent = noteContent,
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
	noteContent: JSONObject?,
	connectedTag: List<String>,
	onAction: (NoteActivity.Action) -> Unit
) {
	Crossfade(
		targetState = status,
		animationSpec = tween(durationMillis = 600),
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		when (it) {
			Status.INIT -> LoadingView()
			Status.LOADING -> LoadingView()
			Status.LOADED -> if (noteDbEntry != null && noteContent != null)
				ViewerComponent(
					noteDbEntry = noteDbEntry,
					noteContent = noteContent,
					connectedTag = connectedTag
				) { onAction(it) }
			Status.ERROR -> {
			}
		}
	}
}

package com.syncodec.momento.mainComponent.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.syncodec.momento.MainActivity
import com.syncodec.momento.custom.calendarView.Calendar
import com.syncodec.momento.custom.notebook.NoteCard
import com.syncodec.momento.custom.notebook.NoteCardData
import com.syncodec.momento.custom.notebook.NotebookHeaderCard
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.mainComponent.miscellaneous.TopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun CalendarScreen(
	noteList: List<NoteDbEntry>,
	isSelected: Boolean,
	selectedItemList: List<String>,
	onClick: (MainActivity.Click, Any?) -> Unit
) {
	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp
	val scope = rememberCoroutineScope()
	val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()

	SideEffect {
		scope.launch {
			delay(2000)
			bottomSheetScaffoldState.bottomSheetState
		}
	}

	BottomSheetScaffold(
		scaffoldState = bottomSheetScaffoldState,
		sheetContent = { BottomSheetContent() },
		modifier = Modifier,
		topBar = {
			TopBar(
				isSelected = isSelected,
				selectedItemSize = selectedItemList.size
			) { click, data -> onClick(click, data) }
		},
		sheetElevation = 32.dp,
		sheetPeekHeight = screenHeight.times(0.2f),
		sheetBackgroundColor = MaterialTheme.colorScheme.background
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(MaterialTheme.colorScheme.background)
		) {
			Calendar()
		}
	}
}

@Composable
private fun BottomSheetContent() {
	LazyColumn {
		item { Spacer(modifier = Modifier.height(16.dp)) }
		item {
			NotebookHeaderCard(
				title = "4th May, 2021",
				noEntries = "13 entries"
			)
		}
		for (i in 0 until 13) {
			item {
				NoteCardData(
					timestamp = System.currentTimeMillis(),
					showFullTime = true,
					isLocked = false,
					isSelected = false,
					isArchived = false,
					isFavourite = false,
					isDeleted = false,
					isLast = false,
					title = "Title",
					contentThumbnail = "diaryDbEntry.contentThumbnail",
					attachmentCount = 0,
					attachmentThumbnail = null,
					address = "diaryDbEntry.address",
					onClick = {},
					onLongClick = {},
				).apply {
					NoteCard(noteCardData = this)
				}
			}
		}
		item { Spacer(modifier = Modifier.height(194.dp)) }
	}
}

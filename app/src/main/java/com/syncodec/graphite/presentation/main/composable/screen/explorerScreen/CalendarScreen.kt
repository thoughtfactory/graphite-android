package com.syncodec.graphite.presentation.main.composable.screen.explorerScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.explorer.calendar.composable.calendarView.CalendarView
import com.syncodec.graphite.presentation.explorer.composable.ExplorerView
import org.koin.androidx.compose.koinViewModel


@Preview
@Composable
fun CalendarScreen() {
	val explorerViewModel: ExplorerViewModel = koinViewModel()

	val selectedDate by explorerViewModel.selectedDate.collectAsState()
	val noteListDateMap by explorerViewModel.noteListDateMap.collectAsState()
	val dateFilteredNoteList by explorerViewModel.dateFilteredNoteList.collectAsState()

	ExplorerView(
		bottomSheetTitle = stringResource(id = R.string.within_default_chapter),
		noteList = dateFilteredNoteList,
	) {
		CalendarView(
			selectedDate = selectedDate,
			noteListMap = noteListDateMap,
			onSelectDate = explorerViewModel::onSelectDate,
			modifier = Modifier.fillMaxSize()
		)
	}
}

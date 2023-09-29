package com.syncodec.graphite.presentation.explorer.calendar.composable

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.explorer.calendar.composable.calendarView.CalendarView
import com.syncodec.graphite.presentation.explorer.composable.ExplorerScreen
import io.realm.kotlin.types.RealmUUID
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Preview
@Composable
fun CalendarScreen(
	chapterObject: ChapterObjectLite? = null,
	selectedDate : LocalDate = LocalDate.now(),
	noteListDateCountMap: Map<LocalDate, Int> = mapOf(),
	contextFilteredNoteList: List<NoteObjectLite> = listOf(),
	onSelectDate : (LocalDate) -> Unit = {},
	onExploreChapter: (RealmUUID?) -> Unit = {},
) {
	ExplorerScreen(
		screenTitle = stringResource(id = R.string.calendar),
		bottomSheetTitle = selectedDate.format(DateTimeFormatter.ofPattern("dd MMM uuuu")),
		currentChapter = chapterObject,
		noteList = contextFilteredNoteList,
		onExploreChapter = onExploreChapter,
	) {
		CalendarView(
			selectedDate = selectedDate,
			noteListDateCountMap = noteListDateCountMap,
			onSelectDate = onSelectDate,
			modifier = Modifier.fillMaxSize()
		)
	}
}

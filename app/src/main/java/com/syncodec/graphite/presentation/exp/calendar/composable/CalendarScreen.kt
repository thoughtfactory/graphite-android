package com.syncodec.graphite.presentation.exp.calendar.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.maps.model.LatLngBounds
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.exp.calendar.composable.calendarView.CalendarView
import com.syncodec.graphite.presentation.exp.composable.ExplorerScreen
import io.realm.kotlin.types.RealmUUID
import java.time.LocalDate


@Preview
@Composable
fun CalendarScreen(
	chapterObject: ChapterObjectLite? = null,
	selectedDay : LocalDate = LocalDate.now(),
	chapterFilteredNoteList: List<NoteObjectLite> = listOf(),
	contextFilteredNoteList: List<NoteObjectLite> = listOf(),
	onUpdateSelectedDay : () -> Unit = {},
	onSelectChapter: (RealmUUID?) -> Unit = {},
) {
	ExplorerScreen(
		screenTitle = stringResource(id = R.string.atlas),
		currentChapter = chapterObject,
		noteList = contextFilteredNoteList,
		onSelectChapter = onSelectChapter,
	) {
		CalendarView(
			selectedDay = selectedDay,
			noteList = chapterFilteredNoteList,
			onUpdateSelectedDay = onUpdateSelectedDay,
		)
	}
}

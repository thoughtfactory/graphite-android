package com.syncodec.graphite.presentation.exp.calendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.syncodec.graphite.presentation.exp.calendar.composable.CalendarScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.RealmUUID
import org.koin.androidx.viewmodel.ext.android.viewModel


class CalendarActivity : ComponentActivity() {

	val viewModel: CalendarViewModel by viewModel()
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val chapterId = intent.getByteArrayExtra(Extra.Companion.Extra.ChapterId.name)?.let { RealmUUID.from(it) }

		viewModel.loadChapter(chapterId = chapterId)

		setContent {
			BaseContent {

				val chapterObject by viewModel.currentChapter.collectAsState()
				val chapterFilteredNoteList by viewModel.filteredNoteList.collectAsState()
				val contextFilteredNoteList by viewModel.contextFilteredNoteList.collectAsState()

				CalendarScreen(
					chapterObject = chapterObject,
//					selectedDay =,
					chapterFilteredNoteList = chapterFilteredNoteList,
					contextFilteredNoteList = contextFilteredNoteList,
					onSelectChapter = { chapterId -> viewModel.loadChapter(chapterId = chapterId) },
//					onUpdateSelectedDay =,
				)
			}
		}
	}
}
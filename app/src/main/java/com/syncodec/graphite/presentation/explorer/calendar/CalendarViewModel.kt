package com.syncodec.graphite.presentation.explorer.calendar

import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.presentation.explorer.meta.AbstractExploreViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset


@KoinViewModel
class CalendarViewModel(repositoryStateFlow: MutableStateFlow<Repository.Companion.RepositoryStatus>) : AbstractExploreViewModel(repositoryStateFlow) {

	//	Map of date and list of notes
	private val _chapterFilteredNoteListDateMap: MutableStateFlow<Map<LocalDate, List<NoteObjectLite>>> = MutableStateFlow(mapOf())
	val chapterFilteredNoteListDateMap: StateFlow<Map<LocalDate, List<NoteObjectLite>>> = _chapterFilteredNoteListDateMap

	//	List of notes filtered for specific date
	private val _contextFilteredNoteList: MutableStateFlow<List<NoteObjectLite>> = MutableStateFlow(listOf())
	val contextFilteredNoteList: StateFlow<List<NoteObjectLite>> = _contextFilteredNoteList

	private val _selectedDate: MutableStateFlow<LocalDate> = MutableStateFlow(LocalDate.now())
	val selectedDate: StateFlow<LocalDate> = _selectedDate

	init {
		viewModelScope.launch(Dispatchers.Default) {
			super.chapterFilteredNoteList.collectLatest { noteList ->
				val noteListMap = noteList.groupBy { noteObjectLite -> LocalDateTime.ofEpochSecond(noteObjectLite.userTimestamp / 1000, 0, ZoneOffset.UTC).toLocalDate() }
				this@CalendarViewModel._chapterFilteredNoteListDateMap.tryEmit(noteListMap)
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			combine(this@CalendarViewModel.chapterFilteredNoteListDateMap, this@CalendarViewModel._selectedDate) { noteListMap1, selectedDate1 ->
				Pair(noteListMap1, selectedDate1)
			}.collectLatest { (noteListMap, date) ->
				this@CalendarViewModel._contextFilteredNoteList.tryEmit(noteListMap[date] ?: listOf())
			}
		}
	}

	fun onSelectDate(date: LocalDate) {
		this._selectedDate.tryEmit(date)
	}
}

package com.syncodec.graphite.presentation.explorer.calendar

import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.repository.LockableRepo
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.presentation.explorer.meta.AbstractExploreViewModel
import io.realm.kotlin.types.RealmUUID
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
class CalendarViewModel(lockableRepo: LockableRepo) : AbstractExploreViewModel(lockableRepo) {

	//	Map of date and list of notes
	private val _chapterFilteredNoteListDateMap: MutableStateFlow<Map<LocalDate, List<NoteObjectLite>>> = MutableStateFlow(mapOf())
	private val _chapterFilteredNoteListDateCountMap: MutableStateFlow<Map<LocalDate, Int>> = MutableStateFlow(mapOf())
	val chapterFilteredNoteListDateCountMap: StateFlow<Map<LocalDate, Int>> = _chapterFilteredNoteListDateCountMap

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
				this@CalendarViewModel._chapterFilteredNoteListDateCountMap.tryEmit(noteListMap.mapValues { it.value.size })
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			combine(this@CalendarViewModel._chapterFilteredNoteListDateMap, this@CalendarViewModel._selectedDate) { noteListMap1, selectedDate1 ->
				noteListMap1[selectedDate1] ?: listOf()
			}.collectLatest { noteList1 ->
				this@CalendarViewModel._contextFilteredNoteList.tryEmit(noteList1)
			}
		}
	}

	fun onSelectDate(date: LocalDate) {
		this._selectedDate.tryEmit(date)
	}
}

package com.syncodec.graphite.presentation.main.composable.screen.explorerScreen

import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLngBounds
import com.syncodec.graphite.di.model.local.NoteObjectLite
import com.syncodec.graphite.di.repository.LockableRepo
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
class ExplorerViewModel(lockableRepo: LockableRepo) : AbstractExploreViewModel(lockableRepo) {


	private val _latLngBound: MutableStateFlow<LatLngBounds?> = MutableStateFlow(null)
	private val _locationFilteredNoteList: MutableStateFlow<List<NoteObjectLite>> = MutableStateFlow(listOf())
	val locationFilteredNoteList: StateFlow<List<NoteObjectLite>> = _locationFilteredNoteList

	private val _selectedDate: MutableStateFlow<LocalDate> = MutableStateFlow(LocalDate.now())
	val selectedDate: StateFlow<LocalDate> = _selectedDate

	private val _noteListDateMap: MutableStateFlow<Map<LocalDate, List<NoteObjectLite>>> = MutableStateFlow(mapOf())
	private val _noteListDateCountMap: MutableStateFlow<Map<LocalDate, Int>> = MutableStateFlow(mapOf())
	val noteListDateCountMap: StateFlow<Map<LocalDate, Int>> = _noteListDateCountMap

	private val _dateFilteredNoteList: MutableStateFlow<List<NoteObjectLite>> = MutableStateFlow(listOf())
	val dateFilteredNoteList: StateFlow<List<NoteObjectLite>> = _dateFilteredNoteList

	init {
		viewModelScope.launch(Dispatchers.Default) {
			launch { loadDefaultChapter() }
			launch { filterNoteByChapter() }
			launch { observeDateFilter() }
			launch { observeLocationFilter() }
		}
	}

	private suspend fun loadDefaultChapter() {
		super.defaultChapterId.collectLatest { super.loadChapter(chapterId = it) }
	}

	private suspend fun filterNoteByChapter() {
		super.chapterFilteredNoteList.collectLatest { noteList ->
			val noteListMap = noteList.groupBy { noteObjectLite -> LocalDateTime.ofEpochSecond(noteObjectLite.userTimestamp / 1000, 0, ZoneOffset.UTC).toLocalDate() }
			this@ExplorerViewModel._noteListDateMap.tryEmit(noteListMap)
			this@ExplorerViewModel._noteListDateCountMap.tryEmit(noteListMap.mapValues { it.value.size })
		}
	}

	private suspend fun observeDateFilter() {
		combine(this@ExplorerViewModel._noteListDateMap, this@ExplorerViewModel._selectedDate) { noteListMap1, selectedDate1 ->
			noteListMap1[selectedDate1] ?: listOf()
		}.collectLatest { noteList1 ->
			this@ExplorerViewModel._dateFilteredNoteList.tryEmit(noteList1)
		}
	}

	private suspend fun observeLocationFilter() {
		combine(super.chapterFilteredNoteList, this@ExplorerViewModel._latLngBound) { filteredNoteList1, latLngBound1 ->
			if (latLngBound1 == null) listOf()
			else filteredNoteList1.filter { noteObjectLite -> noteObjectLite.latLng?.toGLatLng()?.let { latLngBound1.contains(it) } ?: false }
		}.collectLatest { filteredNoteList1 ->
			this@ExplorerViewModel._locationFilteredNoteList.tryEmit(filteredNoteList1)
		}
	}

	fun onUpdateCameraBound(latLngBounds: LatLngBounds?) {
		this._latLngBound.tryEmit(latLngBounds)
	}

	fun onSelectDate(date: LocalDate) {
		this._selectedDate.tryEmit(date)
	}
}

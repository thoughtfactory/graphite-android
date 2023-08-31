package com.syncodec.graphite.presentation.main.composable.screen.explorerScreen

import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLngBounds
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
class ExplorerViewModel(repositoryStateFlow: MutableStateFlow<Repository.Companion.RepositoryStatus>) : AbstractExploreViewModel(repositoryStateFlow) {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _latLngBound: MutableStateFlow<LatLngBounds?> = MutableStateFlow(null)
	private val _locationFilteredNoteList: MutableStateFlow<List<NoteObjectLite>> = MutableStateFlow(listOf())
	val locationFilteredNoteList: StateFlow<List<NoteObjectLite>> = _locationFilteredNoteList

	private val _selectedDate: MutableStateFlow<LocalDate> = MutableStateFlow(LocalDate.now())
	val selectedDate : StateFlow<LocalDate> = _selectedDate

	private val _noteListDateMap: MutableStateFlow<Map<LocalDate, List<NoteObjectLite>>> = MutableStateFlow(mapOf())
	val noteListDateMap: StateFlow<Map<LocalDate, List<NoteObjectLite>>> = _noteListDateMap

	private val _dateFilteredNoteList : MutableStateFlow<List<NoteObjectLite>> = MutableStateFlow(listOf())
	val dateFilteredNoteList : StateFlow<List<NoteObjectLite>> = _dateFilteredNoteList

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryStateFlow.collect { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) _repository.tryEmit(repositoryStatus.repository)
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			_repository.collectLatest { repository ->
				repository?.getDefaultChapterIdAsFlow()?.collectLatest { defaultChapterId ->
					super.loadChapter(chapterId = defaultChapterId)
				}
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			combine(super.chapterFilteredNoteList, this@ExplorerViewModel._latLngBound) { filteredNoteList1, latLngBound ->
				Pair(filteredNoteList1, latLngBound)
			}.collectLatest { (filteredNoteList1, latLngBound) ->
				if (latLngBound != null) {
					this@ExplorerViewModel._locationFilteredNoteList.tryEmit(filteredNoteList1.filter { noteObjectLite -> noteObjectLite.latLng?.toGLatLng()?.let { latLngBound.contains(it) } ?: false })
				} else {
					this@ExplorerViewModel._locationFilteredNoteList.tryEmit(listOf())
				}
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			super.chapterFilteredNoteList.collectLatest { noteList ->
				val noteListMap = noteList.groupBy { noteObjectLite -> LocalDateTime.ofEpochSecond(noteObjectLite.userTimestamp / 1000, 0, ZoneOffset.UTC).toLocalDate() }
				this@ExplorerViewModel._noteListDateMap.tryEmit(noteListMap)
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			combine(this@ExplorerViewModel.noteListDateMap, this@ExplorerViewModel._selectedDate) { noteListMap1, selectedDate1 ->
				Pair(noteListMap1, selectedDate1)
			}.collectLatest { (noteListMap, date) ->
				this@ExplorerViewModel._dateFilteredNoteList.tryEmit(noteListMap[date] ?: listOf())
			}
		}
	}

	fun onUpdateCameraBound(latLngBounds: LatLngBounds?) {
		this._latLngBound.tryEmit(latLngBounds)
	}

	fun onSelectDate(date: LocalDate) {
		this._selectedDate.tryEmit(date)
	}
}

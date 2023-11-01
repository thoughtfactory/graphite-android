package com.syncodec.graphite.presentation.explorer.atlas

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


@KoinViewModel
class AtlasViewModel(lockableRepo: LockableRepo) : AbstractExploreViewModel(lockableRepo) {

	private val _contextFilteredNoteList: MutableStateFlow<List<NoteObjectLite>> = MutableStateFlow(listOf())
	val contextFilteredNoteList: StateFlow<List<NoteObjectLite>> = _contextFilteredNoteList

	private val _latLngBound: MutableStateFlow<LatLngBounds?> = MutableStateFlow(null)

	init {
		viewModelScope.launch(Dispatchers.Default) {
			combine(super.chapterFilteredNoteList, this@AtlasViewModel._latLngBound) { filteredNoteList1, latLngBound ->
				Pair(filteredNoteList1, latLngBound)
			}.collectLatest { (filteredNoteList1, latLngBound) ->
				if (latLngBound != null) this@AtlasViewModel._contextFilteredNoteList.tryEmit(filteredNoteList1.filter { noteObjectLite -> noteObjectLite.latLng?.toGLatLng()?.let { latLngBound.contains(it) } ?: false })
				else this@AtlasViewModel._contextFilteredNoteList.tryEmit(listOf())
			}
		}
	}

	fun onUpdateCameraBound(latLngBounds: LatLngBounds?) {
		this._latLngBound.tryEmit(latLngBounds)
	}
}
